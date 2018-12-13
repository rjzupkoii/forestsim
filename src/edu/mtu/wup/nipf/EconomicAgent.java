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
	private double targetHarvest = -1;
	
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
		
		// We always want to harvest our entire parcel, not realistic, but meh
		if (targetHarvest == -1) {
			targetHarvest = getParcelArea();
		}
		
		// If we haven't determined when the next NVP calculation should be, do so
		if (nextNpv == -1) {
			nextNpv = projectNvp();
			nextNpv = (nextNpv != -1) ? state.schedule.getSteps() + nextNpv : -1;
		}
		
		// Should we do a harvest projection?
		if (nextNpv >= state.schedule.getSteps()) {
			projectHarvests();
			
			// Shouldn't happen, edge case
			if (nextHarvest == -1) {
				return;
			}
		}
		
		// If it is time for the next harvest, do so
		if (nextHarvest != -1 && state.schedule.getSteps() >= nextHarvest) {
			List<Stand> stands = Harvesting.getHarvestableStands(getParcel(), getHarvestDbh());
			AggregateHarvester.getInstance().requestHarvest(this, stands);
		}
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
	private int projectNvp() {
		// Note the stands for the projection
		Forest forest = Forest.getInstance();
		Point[] points = getParcel();
		Stand[] projection = new Stand[points.length];
		for (int ndx = 0; ndx < points.length; ndx++) {
			projection[ndx] = forest.getStand(points[ndx]);
		}
		
		// Prime things with the current year
		double[] values = new double[projectionWindow];
		values[0] = getMean(projection);
		
		for (int ndx = 1; ndx < projectionWindow; ndx++) {
			// Check the previous years work
			if (values[ndx - 1] >= Harvesting.PulpwoodDbh) {
				return (ndx - 1);
			}
			
			// Advance by one year
			for (int ndy = 0; ndy < projection.length; ndy++) {
				projection[ndy] = forest.getGrowthModel().growStand(projection[ndy]);
			}
			values[ndx] = getMean(projection);
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