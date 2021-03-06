package edu.mtu.steppables.marketplace;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.simulation.ForestSim;
import edu.mtu.utilities.Randomizers;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class acts as an aggregation point for agents that need to be accessed 
 * by other agents. For example, NIPF owners need to know how to get in touch
 * with the loggers.
 */
@SuppressWarnings("serial")
public class Marketplace implements Steppable {

	private static Marketplace instance = new Marketplace();
	
	private List<Harvester> harvesters = new ArrayList<Harvester>();
	private List<Transporter> transporters = new ArrayList<Transporter>();
	private List<Processor> processors = new ArrayList<Processor>();
		
	/**
	 * Constructor
	 */
	private Marketplace() { }
	
	/**
	 * Get a reference to the marketplace object.
	 * @return
	 */
	public static Marketplace getInstance() { return instance; }

	/**
	 * Get the list of registered harvesters.
	 */
	public List<Harvester> getHarvesters() { return harvesters; }
	
	/**
	 * Get the list of registered biomass processors.
	 */
	public List<Processor> getProcessors() { return processors; }
	
	/**
	 * Get the list of registered biomass transporters.
	 */
	public List<Transporter> getTransporters() { return transporters; } 
	
	/**
	 * Register a logger with the marketplace.
	 */
	public void registerHarvester(Harvester agent) {
		harvesters.add(agent);
	}
	
	/**
	 * Register a biomass processor with the marketplace.
	 */
	public void registerProcessor(Processor agent) {
		processors.add(agent);
	}
	
	/**
	 * Register a biomass transporter with the marketplace.
	 */
	public void registerTransporter(Transporter agent) {
		transporters.add(agent);
	}

	/**
	 * Schedule the marketplace, then re-enqueue the marketplace steppable.
	 */
	@Override
	public void step(SimState state) {
		scheduleMarketplace((ForestSim)state);
		state.schedule.scheduleOnce(this);
	}
	
	/**
	 * Iterate through the marketplace and make sure all agents are scheduled.
	 */
	private void scheduleMarketplace(ForestSim state) {
		scheduleSteppables(harvesters.toArray(), state);
		scheduleSteppables(transporters.toArray(), state);
		scheduleSteppables(processors.toArray(), state);
	}
	
	/**
	 * Iterate through the items provided and add them to the schedule in a randomized fashion. 
	 */
	private void scheduleSteppables(Object[] items, ForestSim state) {
		// Exit if there is nothing to do
		if (items.length == 0) {
			return;
		}
		
		// Shuffle the array of objects
		Randomizers.shuffle(items, state.getRandom());
		
		// Add them to the schedule
		for (Object item : items) {
			state.schedule.scheduleOnce((Steppable)item);
		}
	}
}
