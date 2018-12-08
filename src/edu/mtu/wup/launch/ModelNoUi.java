package edu.mtu.wup.launch;

import edu.mtu.simulation.ForestSim;

public class ModelNoUi {
	/**
	 * Main entry point for the model.
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("ForestSim, LUP: No model provided!");
			return;
		}
		
		@SuppressWarnings("rawtypes")
		Class mode = null;
		
		// Parse out the arguments
		for (int ndx = 0; ndx < args.length; ndx++) {
			switch (args[ndx]) {
						
			// Various model modes
			case "--none":
				System.out.println("Starting WUP model with no VIP.");
				mode = WupModelNone.class;
			case "--discount":
				System.out.println("Starting WUP model with discount VIP.");
				mode = WupModelDiscount.class;
			case "--agglomeration":
				System.out.println("Starting WUP model with agglomeration VIP.");
				mode = WupModelAgglomeration.class;
			}
		}
		
		// Execute the model
		ForestSim.load(mode, args);
	}
}
