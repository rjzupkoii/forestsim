package edu.mtu.steppables;

import edu.mtu.simulation.Scorecard;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This steppable performs any aggregation steps that are needed for the engine
 * as well as insuring the Scorecard is invoked.
 */
@SuppressWarnings("serial")
public class AggregationStep implements Steppable {

	private Scorecard scorecard = null;
	
	@Override
	public void step(SimState state) {
		// Run the scorecard, if provided
		if (scorecard != null) {
			scorecard.processTimeStep();
		}
		
		// Should we end the model?
		if (state.schedule.getSteps() > 40) {		// TODO Make this configurable
			state.finish();
			return;
		} 
		
		// Put us back in the queue
		state.schedule.scheduleOnce(this);
	}
	
	/**
	 * Set the score card to be used for aggregation.
	 */
	public void setScorecard(Scorecard scorecard) {
		this.scorecard = scorecard;
	}
}
