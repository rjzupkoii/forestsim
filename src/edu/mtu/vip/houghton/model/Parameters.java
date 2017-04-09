package edu.mtu.vip.houghton.model;

public class Parameters {
	
	public static final String defaultCoverFile 		= "shapefiles/Houghton Land Cover/houghtonlandcover.asc";
	public static final String defaultParcelFile 		= "file:shapefiles/Houghton Parcels/houghton_parcels.shp";
	public static final String defaultOutputDirectory 	= "out/vip-bonus"; 
	
	// Based upon yellowbook listings http://www.yellowbook.com/s/logging-companies/surrounding-houghton-county-mi/
	public final static int loggingCompanies = 24;		
	public final static int totalLoggingCapablity = loggingCompanies * 2;	

	public static final double defaultEconomicAgentPercentage = 0.3; 		// Initially 30% of the agents should be economic optimizers
	
	private double ecosystemsAgentHarvestOdds = 0.1; 						// Initially 10% of the time, eco-system services agent's will harvest
	
	/**
	 * Get the agglomeration bonus as mills reduction per 1,000 enrolled.
	 */
	public double getAgglomerationBonus() { 
		return VIP.getInstance().getAgglomerationBonus(); 
	}
	
	/**
	 * Get the odds that an ecosystems services optimizing agent will harvest.
	 */
	public double getEcosystemsAgentHarvestOdds() { 
		return ecosystemsAgentHarvestOdds; 
	}
	
	/**
	 * Get how old the stand may be before it must be harvested.
	 */
	public int getMustHarvestBy() { 
		return VIP.getInstance().getMustHarvestBy();
	}

	/**
	 * Get the number of sq.m. enrolled in the VIP program.
	 */
	public double getVipArea() {
		return VIP.getInstance().getSubscribedArea();
	}
	
	/**
	 * Get the flag that indicates if the VIP agglomeration is active or not.
	 */
	public Boolean getVipBonusEnabled() {
		return VIP.getInstance().getIsBonusActive();
	}
	
	/**
	 * Get the flag that indicates if the VIP is active or not.
	 */
	public Boolean getVipEnabled() { 
		return VIP.getInstance().getIsActive();	
	}
	
	/**
	 * Get the number of agents enrolled in the VIP program.
	 */
	public int getVipMembership() {
		return VIP.getInstance().getSubscriptionRate();
	}
	
	/**
	 * Set the agglomeration bonus as mills reduction per 1,000 enrolled.
	 */
	public void setAgglomerationBonus(double value) { 
		VIP.getInstance().setAgglomerationBonus(value); 
	}
	
	/**
	 * Set the odds that an ecosystems services optimizing agent will harvest.
	 */
	public void setEcosystemsAgentHarvestOdds(double value) {
		if (value >= 0.0 && value <= 1.0) {
			ecosystemsAgentHarvestOdds = value;
		}
	}
	
	/**
	 * Set how old the stand may be before it must be harvested.
	 */
	public void setMustHarvestBy(int value) {
		VIP.getInstance().setMustHarvestBy(value);
	}
	
	/**
	 * Flag to indicate if the VIP bonus should be enabled or not.
	 */
	public void setVipBonusEnabled(Boolean value) {
		VIP.getInstance().setIsBonusActive(value);
	}
	
	/**
	 * Flag to indicate if the VIP should be enabled or not.
	 */
	public void setVipEnabled(Boolean value) { 
		VIP.getInstance().setIsActive(value);
	}
}
