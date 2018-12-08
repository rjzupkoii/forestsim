package edu.mtu.wup.model.parameters;

import edu.mtu.wup.vip.VipFactory.VipRegime;

/**
 * Model parameters for the no VIP regime.
 */
public class NoneParameters extends WupParameters {

	public NoneParameters() {	
		// Select the policy
		setVipProgram(VipRegime.NONE);
		setVipCoolDown(10);
		setOutputDirectory("out/none");
		
		// Set the model variables
		setEconomicAgentPercentage(0.3);
		setEconomicNpvDiscountRate(0.08, 0.02);		// http://www.sewall.com/files/timberlandreport/v8n3.pdf
		setEcosystemsAgentHarvestOdds(0.02);		// MR, 2% mean
		setMooIntendsToHavestOdds(0.26);			// 18% of all NIPFOs
		setNipfoWth(523.23, 123.12);
		
		setLoggingCapacity(2500);					// About 10% of the NIPFOs
		
		// Set the ForestSim configuration
		setPolicyActiviationStep(60);				// We expect agents to be fully activated by T+50
		setFinalTimeStep(200);
	}
}
