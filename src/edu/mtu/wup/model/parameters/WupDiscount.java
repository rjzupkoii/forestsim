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
		setEconomicAgentPercentage(0);
		setEconomicNpvDiscountRate(0.08, 0.02);		// http://www.sewall.com/files/timberlandreport/v8n3.pdf
//		setEcosystemsAgentHarvestOdds(0.04);		// MR, 2% mean
//		setEcosystemsAgentHarvestOdds(0.075);		// RZ, 3.75% mean
		setEcosystemsAgentHarvestOdds(0.1);
		setNipfoWth(523.23, 123.12);
		setLoggingCapacity(2500);					// About 10% of the NIPFOs
		
		// Set the ForestSim configuration
		setPolicyActiviationStep(60);				// We expect agents to be fully activated by T+50
		setFinalTimeStep(200);
	}
}
