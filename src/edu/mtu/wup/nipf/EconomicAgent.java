package edu.mtu.wup.nipf;

import java.awt.Point;
import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.wup.model.Economics;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public class EconomicAgent extends NipfAgent {
			
	private final static int projectionWindow = 100;
	
	private double rate = 0.0;	
	private double targetHarvest = 40;
	
	private long nextHarvest = -1;
	private long nextNpv = -1;
		
	/**
	 * Constructor.
	 */
	public EconomicAgent(LandUseGeomWrapper lu) {
		super(ParcelAgentType.ECONOMIC, lu);
	}
		
	@Override
	protected void doAgentPolicyOperation() {
		// Return if they are already a member
		if (inVip()) {
			return;
		}					
		
		// We want lower taxes, does the VIP give us that?
		VipBase vip = VipFactory.getInstance().getVip();
		if (vip.getMillageRateReduction(this, state) > 0) {
			enrollInVip();
		}
	}
	
	@Override
	protected void doHarvestOperation() {

		// If it is time for the next harvest, do so
		if (nextHarvest != -1 && state.schedule.getSteps() >= nextHarvest) {
			List<Stand> stands = Harvesting.getHarvestableStands(getParcel(), getHarvestDbh());
			AggregateHarvester.getInstance().requestHarvest(this, stands);
			return;
		}
		
		// Should we do a harvest projection?
		if (nextNpv != -1 && state.schedule.getSteps() >= nextNpv) {
			projectHarvests();
			
			// Shouldn't happen, edge case
			if (nextHarvest == -1) {
				return;
			}
		}
		
		// If we haven't determined when the next NVP calculation should be, do so
		nextNpv = projectGrowth();
		nextNpv = (nextNpv != -1) ? state.schedule.getSteps() + nextNpv : -1;
	}
	
	@Override
	public void doHarvestedOperation() {
		super.doHarvestedOperation();
		nextHarvest = -1;
		nextNpv = -1;
	}

	@Override
	protected double getMinimumDbh() {
		return Harvesting.SawtimberDbh;
	}
	
	/**
	 * Project when the next NVP calculation should be, basically when the mean DBH 
	 * for our parcels is at least pulpwood.
	 * 
	 * @return Number of time steps from now.
	 */
	private int projectGrowth() {
		// Note the stands for the projection
		Forest forest = Forest.getInstance();
		Point[] points = getParcel();
		Stand[] projection = new Stand[points.length];
		for (int ndx = 0; ndx < points.length; ndx++) {
			projection[ndx] = forest.getStand(points[ndx]);
		}
		
		// Prime things with the current year
		double meanDbh = getMean(projection);
		
		for (int year = 1; year < projectionWindow; year++) {
			// Check the previous years work
			if (meanDbh >= Harvesting.PulpwoodDbh) {
				return (year - 1);
			}
			
			// Advance by one year
			for (int ndy = 0; ndy < projection.length; ndy++) {
				projection[ndy] = forest.getGrowthModel().growStand(projection[ndy]);
			}
			meanDbh = getMean(projection);
		}
		
		// In theory, impossible, but guard code
		return -1;
	}
	
	/**
	 * Calculate the mean DBH for the parcel provided.
	 */
	private double getMean(Stand[] projection) {
		double dbh = 0;
		for (Stand stand : projection) {
			dbh += stand.arithmeticMeanDiameter;
		}
		return (dbh / projection.length);
	}
	
	/**
	 * Project the value of future harvests and select the one with the 
	 */
	private void projectHarvests() {
		// Note the stands for the projection
		Forest forest = Forest.getInstance();
		Point[] points = getParcel();
		Stand[] projection = new Stand[points.length];
		for (int ndx = 0; ndx < points.length; ndx++) {
			projection[ndx] = forest.getStand(points[ndx]);
		}
			
		// Prime things with the current year
		double dbh = getHarvestDbh();
		double[] values = new double[projectionWindow];
		values[0] = getBid(projection,  dbh, 0);
		
		// Project from T+1 until the window, note that we are updating our projection
		// list of stands every year by only one increment
		for (int ndx = 1; ndx < projectionWindow; ndx++) {

			// Advance the stands by one year
			for (int ndy = 0; ndy < projection.length; ndy++) {
				projection[ndy] = forest.getGrowthModel().growStand(projection[ndy]);
			}
			
			// Get the bid for the projection
			values[ndx] = getBid(projection, dbh, ndx);
		}
		
		// Find the best year to harvest
		long steps = state.schedule.getSteps();
		double value = -1;
		for (int ndx = 0; ndx < projectionWindow; ndx++) {
			if (value < values[ndx]) {
				nextHarvest = steps + ndx;
				value = values[ndx];
			}
		}			
	}
	
	/**
	 * Get the bid for the projected growth.
	 */
	private double getBid(Stand[] projection, double dbh, long year) {
		// See what can be harvested
		List<Stand> harvestable = Harvesting.getHarvestableStands(projection, dbh);
		
		// Make sure the area meets the target
		double area = harvestable.size() * Forest.getInstance().getAcresPerPixel();
		if (area < targetHarvest) {
			return 0.0;
		}
		
		// Get the bid and return
		double bid = Harvesting.getHarvestValue(harvestable);
		double npv = Economics.npv(bid, rate, year);
		return npv;
	}
	
	/**
	 * Set the NVP discount rate.
	 */
	public void setDiscountRate(double value) {
		rate = value;
	}
}