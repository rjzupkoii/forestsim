package edu.mtu.wup.model.parameters;

import edu.mtu.wup.vip.VipFactory.VipRegime;

/**
 * Model parameters for a VIP with straight tax discount.
 */
public class WupDiscount extends WupParameters {
	
	public WupDiscount() {
		// Select the policy
		setVipProgram(VipRegime.DISCOUNT);
		setVipCoolDown(10);
		setOutputDirectory("out/discount");
		
		// Set the model variables
		setEconomicAgentPercentage(0.3);
		setEconomicNpvDiscountRate(0.08, 0.02);		// http://www.sewall.com/files/timberlandreport/v8n3.pdf
		setEcosystemsAgentHarvestOdds(0.02);		// MR, 2% mean
		setMooIntendsToHavestOdds(0.26);			// 18% of all NIPFOs
		setNipfoWth(523.23, 123.12);
		
		setLoggingCapacity(1160);
		
		// Set the ForestSim configuration
		setPolicyActiviationStep(150);
		setFinalTimeStep(300);
	}
}
