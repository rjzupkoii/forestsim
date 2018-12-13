package edu.mtu.steppables.marketplace;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.vip.VipBase;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This agent accepts requests from NIPF agents and harvests their land based upon 
 * various rules.
 */
@SuppressWarnings("serial")
public class AggregateHarvester implements Steppable {
	private static AggregateHarvester instance = new AggregateHarvester();
	private List<HarvestRequest> requests = new ArrayList<HarvestRequest>();
	
	public final static double MinimumHarvestArea = VipBase.baseAcerage;		// Minimum area that can be harvested.
	
	private double stemBiomass;
	private double totalBiomass;
	private int harvestsRequested;
	private int pracelsHarvested;
	
	/**
	 * Constructor.
	 */
	private AggregateHarvester() { }
	
	/**
	 * 
	 */
	public void step(SimState state) {
		int capacity = ((ForestSim)state).getHarvestCapacity();
		processHarvestRequests(capacity);
	}
	
	/**
	 * Get the stem biomass, in kg (dry weight) 
	 * @return
	 */
	public double getStemBiomass() {
		return stemBiomass;
	}
	
	/**
	 * Get the harvested biomass, in kg (dry weight)
	 */
	public double getTotalBiomass() {
		return totalBiomass;
	}
	
	/**
	 * Get the number of harvest requests.
	 */
	public int getHarvestedRequested() {
		return harvestsRequested;
	}
	
	/**
	 * Get the number of parcels harvested.
	 */
	public int getPracelsHarvested() {
		return pracelsHarvested;
	}
	
	/**
	 * Get an instance of the harvester agent.
	 */
	public static AggregateHarvester getInstance() {
		return instance;
	}
	
	/**
	 * For ForestSim only, force a new harvester into existence.
	 */
	public static AggregateHarvester getNewInstance() {
		instance = new AggregateHarvester();
		return instance;
	}
	
	/**
	 * Process the list of harvest requests and harvest the stands that result
	 * in the most economic returns for the company. Note that this method is
	 * provided as an example and can be overridden to provide more flexibility.
	 * 
	 * @param capacity The total capacity for harvests.
	 * @return The total biomass harvested in kilograms dry weight (kg)
	 */
	protected void processHarvestRequests(int capacity) {
		harvestsRequested = requests.size();
		
		// Reset
		stemBiomass = 0;
		totalBiomass = 0;
		pracelsHarvested = 0;
		
		Forest forest = Forest.getInstance();		
		while (!requests.isEmpty()) {
			
			// Harvest the biomass from the parcel, update the totals
			HarvestRequest request = requests.remove(0);
			Pair<Double, Double> result = forest.harvest(request.stands);
			stemBiomass += result.getValue0();
			totalBiomass += result.getValue1();
			pracelsHarvested++;
			
			// Inform the agent
			request.agent.doHarvestedOperation();
			
			// Return if we are done
			if (pracelsHarvested >= capacity) {
				requests.clear();
				return;
			}
		}
		
//		// Adjust the capacity based upon the demand, note that 5% is just a rough number for adjustments
//		int difference = harvestsRequested - pracelsHarvested;
//		int adjustment = (int)((double)difference * 0.05);
//		capacity += adjustment;
//		int capacity = ((ForestSim)state).getHarvestCapacity();
	}
	
	/**
	 * Allow an agent to request that the forest stand indicated be harvested.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stand The points that are associated with the stand to be harvested.
	 */
	public void requestHarvest(ParcelAgent agent, Point[] stand) {
		requestHarvest(agent, stand, null);
	}
	
	/**
	 * Allow an agent to request that the forest stands indicated be harvested
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stands The stands that are to be harvested.
	 */
	public void requestHarvest(ParcelAgent agent, List<Stand> stands) {
		Point[] points = new Point[stands.size()];
		for (int ndx = 0; ndx < stands.size(); ndx++) {
			points[ndx] = stands.get(ndx).point; 
		}
		requestHarvest(agent, points, null);
	}	
	
	/**
	 * Allow an agent to request that the forest stand indicated be harvested.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stand The points that are associated with the stand to be harvested.
	 * @param deliverTo The BiomassConsumer that the harvested biomass should be delivered to.
	 */
	public void requestHarvest(ParcelAgent agent, Point[] stand, BiomassConsumer deliverTo) {
		// Wrap the data in a request
		HarvestRequest request = new HarvestRequest();
		request.agent = agent;
		request.stands = stand;
		request.deliverTo = null;
		
		// Queue it to be processed later
		request.queueOrder = requests.size();
		requests.add(request);
	}
}
