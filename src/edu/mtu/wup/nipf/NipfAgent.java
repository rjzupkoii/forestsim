package edu.mtu.wup.nipf;

import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.model.VIP;

@SuppressWarnings("serial")
public abstract class NipfAgent extends ParcelAgent {
	private final static double initalMillageRate = 33.1577;	// Based upon the average rate for Houghton county
	
	protected boolean vipEnrollee = false;
	protected int vipAge = 0;
	
	protected double harvestOdds = 0.0;
	protected double willingnessToJoinVip = 0.1;
	protected double minimumDbh;
	protected double profitMagin = 0.1;
	
	protected double taxesPaid = 0.0;
	
	protected abstract void doAgentPolicyOperation();
	
	public NipfAgent(ParcelAgentType type) {
		super(type);
	}
	
	@Override
	public void doHarvestedOperation() {
		// Just reset the taxes paid on a harvest
		taxesPaid = 0;
	}
	
	@Override
	protected void doPolicyOperation() {
		VIP vip = VIP.getInstance();
		if (getParcelArea() < vip.getMinimumAcerage()) {
			return;
		}
		
		// Return if there is no VIP
		if (!vip.getIsActive()) {
			return;
		}

		// Return if the VIP is not introduced
		if (!vip.isIntroduced()) {
			return;
		}
		
		doAgentPolicyOperation();
	}
	
	/**
	 * Get the millage rate for the agent's parcel.
	 */
	public double getMillageRate() {
		if (vipEnrollee) {
			return initalMillageRate - VIP.getInstance().getMillageRateReduction();
		}
		return initalMillageRate;
	}
	
	/**
	 * Set the odds that the agent will harvest once there is full coverage.
	 */
	public void setHarvestOdds(double value) { harvestOdds = value; }
	
	/**
	 * Set the profit margin that the agent will want to get when harvesting.
	 */
	public void setProfitMargin(double value) { profitMagin = value; }
	
	/**
	 * Have the agent investigate if the should harvest or not.
	 */
	protected void investigateHarvesting() {
		if (minimumDbh == 0) {
			throw new IllegalArgumentException("Minimum DBH cannot be zero.");
		}
		
		// Nobody will work with us if the parcel is too small
		if (getParcelArea() < AggregateHarvester.MinimumHarvestArea) {
			return;
		}
		
		// Now determine what sort of DBH we will harvest at
		double dbh = minimumDbh;
		if (vipEnrollee) {
			dbh = VIP.getInstance().getMinimumHarvestingDbh();
		}
		
		// See how much can be harvested at the DBH, this overrides the policy 
		List<Stand> stands = Harvesting.getHarvestableStands(getParcel(), dbh);
		double area = stands.size() * Forest.getInstance().getAcresPerPixel();
		if (area < AggregateHarvester.MinimumHarvestArea) {
			return;
		}
		
		// Did we turn a profit compared to our taxes?
		double value = Harvesting.getHarvestValue(stands);
		double profit = value - taxesPaid;
		
		// If we aren't in a VIP keep an eye on the profit margin
		if (!vipEnrollee) {
			if (value < taxesPaid * (1 + profitMagin)) { 
				return;
			}
		}

		// We can harvest, so enqueue the harvest
		AggregateHarvester.getInstance().requestHarvest(this, getParcel());
		
		// Nobody likes loosing money, if taxes paid exceed the profit from 
		// harvesting and we are n the VIP, leave it
		if (vipEnrollee && profit < 0) {
			VIP.getInstance().unenroll(getParcel());
			vipEnrollee = false;
			vipAge = 0;
		}
	}		
}
