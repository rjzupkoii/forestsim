package edu.mtu.vip.houghton;

import edu.mtu.simulation.ForestSim;
import edu.mtu.vip.houghton.model.HoughtonModel;

public class ModelNoUi {
	/**
	 * Main entry point for the model.
	 */
	public static void main(String[] args) {
		ForestSim.load(HoughtonModel.class, args);
	}
}
