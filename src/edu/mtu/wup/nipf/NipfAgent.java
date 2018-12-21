package edu.mtu.wup.nipf;

import edu.mtu.steppables.LandUseGeomWrapper;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.wup.model.parameters.WupParameters;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public abstract class NipfAgent extends ParcelAgent {

	// VIP attributes
	private boolean vipDisqualifed = false;
	private boolean vipAware = false;
	private boolean vipEnrollee = false;
	protected boolean vipHarvested = false;
	
	// Individual attributes
	private boolean taxConcern = false;
	
	// TODO Move these up to being set elsewhere
	private final static double vipAwarenessRate = 0.25;		// The odds that the NIPFO will accept VIP information from neighbors
	private final static double vipInformedRate = 0.026;		// The odds that the NIPFO will be informed of the VIP after activation
				
	// WTH attributes
	protected double wthPerAcre = 0.0;
			
	protected abstract void doAgentPolicyOperation();
	protected abstract double getMinimumDbh();
		
	public NipfAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		super(type, lu);
	}
	
	@Override
	public void doHarvestedOperation() {
		// Set the flag indicating we harvested since enrolling in the VIP
		vipHarvested = vipEnrollee;
	}
	
	@Override
	protected void doPolicyOperation() {
		// Return if the VIP doesn't apply to us
		if (vipDisqualifed) {
			return;
		}
		
		// Return if there is no policy
		if (!VipFactory.getInstance().policyExists()) {
			return;
		}
		
		// Return if the VIP is not introduced
		VipBase vip = VipFactory.getInstance().getVip();
		if (!vip.isIntroduced()) {
			return;
		}
		
		// If we aren't aware if the VIP see if we should be
		if (!vipAware) {
			if (vipInformedRate < state.random.nextDouble()) {
				return;
			}
			awareOfVip();
		}
		
		// Once we are aware, check to see if we have enough acreage
		// no point in checking status any more if we don't qualify
		if (getParcelArea() < vip.getMinimumAcerage()) {
			vipDisqualifed = true;
			return;
		}
				
		doAgentPolicyOperation();
	}

	
	/**
	 * Get the millage rate for the agent's parcel.
	 */
	public double getMillageRate() {
		if (vipEnrollee) {
			return WupParameters.MillageRate - VipFactory.getInstance().getVip().getMillageRateReduction(this, state);
		}
		return WupParameters.MillageRate;
	}
	
	public boolean inVip() { return vipEnrollee; }
		
	private void awareOfVip() {
		// Guard against multiple updates 
		if (vipAware) {
			return;
		}
		
		// Set our flag and inform the model
		vipAware = true;
		VipFactory.getInstance().getVip().nipfoInformed();
		getGeometry().setAwareOfVip(true);
		state.updateAgentGeography(this);
	}
	
	protected void enrollInVip() {
		vipEnrollee = true;
		vipHarvested = false;
		VipFactory.getInstance().getVip().enroll(this, state);
		getGeometry().setEnrolledInVip(true);
		state.updateAgentGeography(this);
	}

	protected void unenrollInVip() {
		vipEnrollee = false;
		VipFactory.getInstance().getVip().unenroll(getParcel());
		getGeometry().setEnrolledInVip(false);
		state.updateAgentGeography(this);
	}
	
	protected double getHarvestDbh() {
		if (getMinimumDbh() == 0) {
			throw new IllegalArgumentException("Minimum DBH cannot be zero.");
		}

		// Now determine what sort of DBH we will harvest at
		double dbh = getMinimumDbh();
		if (vipEnrollee) {
			dbh = VipFactory.getInstance().getVip().getMinimumHarvestingDbh();
		}

		return dbh;		
	}
	
	/**
	 * Inform this NIFPO of the VIP.
	 */
	public void informOfVip() {
		// If we already know, return
		if (vipAware) {
			return;
		}
		
		// If we haven't been fully activated yet, return
		if (!phasedIn()) {
			return;		
		}
		
		// Do we care about this information?
		if (state.random.nextDouble() <= vipAwarenessRate) {
			awareOfVip();
		}
	}

	/**
	 * Flag to indicate if the NIPFO is concerned about taxes.
	 */	
	public boolean getTaxConcerns() {
		return taxConcern;
	}
	
	/**
	 * Flag to indicate if the NIPFO is concerned about taxes.
	 */
	public void setTaxConcerns(boolean value) {
		taxConcern = value;
	}
	
	/**
	 * Set the WTH for the agent and calculate how much they want for the parcel
	 */
	public void setWthPerAcre(double value) {
		wthPerAcre = value;
	}
}
