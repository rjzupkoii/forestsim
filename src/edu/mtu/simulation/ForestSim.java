package edu.mtu.simulation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import edu.mtu.steppables.Agent;
import edu.mtu.steppables.EconomicAgent;
import edu.mtu.steppables.EcosystemsAgent;
import edu.mtu.utilities.LandUseGeomWrapper;
import edu.mtu.utilities.NlcdClassification;
import sim.engine.SimState;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomGridField.GridDataType;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.io.geo.ArcInfoASCGridImporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.IntBag;

@SuppressWarnings("serial")
public class ForestSim extends SimState {

	// Path to GIS files used in the simulation
	private static final String coverFile = "shapefiles/WUP Land Cover/WUPLandCover.asc";
	private static final String parcelFile = "file:shapefiles/WUP Parcels/WUPParcels.shp";

	// Display width and height
	private static final int gridWidth = 1000;
	private static final int gridHeight = 900;

	// The set of woody biomass types that we are interested in
	private final static Set<Integer> woodyBiomass = new HashSet<Integer>(Arrays.asList(new Integer[] { 
			NlcdClassification.DeciduousForest.getValue(), 
			NlcdClassification.EvergreenForest.getValue(),
			NlcdClassification.MixedForest.getValue(),
			NlcdClassification.WoodyWetlands.getValue()
	}));
	
	// Geometry assigned to assigned to agents to geo-locate their parcel
	public GeomVectorField parcelLayer;

	// Geometry representing current land cover at high resolution
	public GeomGridField coverLayer = new GeomGridField();

	private Agent[] agents; // Array of all agents active in the simulation
	private double economicAgentPercentage = 0.5; 		// Initially 50% of the agents should be economic optimizers
	private double ecosystemsAgentHarvestOdds = 0.1; 	// Initially 10% of the time, eco-system services agent's will harvest

	/**
	 * Constructor.
	 */
	public ForestSim(long seed) {
		super(seed);
	}

	/**
	 * Return the interval for the economicAgentPercentage
	 */
	public Object domEconomicAgentPercentage() {
		return new sim.util.Interval(0.0, 1.0);
	}

	/**
	 * Return the interval for the ecosystemsAgentHarvestOdds
	 */
	public Object domEcosystemsAgentHarvestOdds() {
		return new sim.util.Interval(0.0, 1.0);
	}

	/**
	 * Get the current average forest cover for the model.
	 */
	public double getAverageCoverage() {
		// Return if there is nothing to do
		if (agents == null || agents.length == 0) {
			return 0.0;
		}

		double total = 0.0;
		for (Agent agent : agents) {
			total += agent.getLandUse();
		}
		return total / agents.length;
	}

	/**
	 * Get the target percentage of agents, as a double, that are economic
	 * optimizers.
	 */
	public double getEconomicAgentPercentage() {
		return economicAgentPercentage;
	}

	/**
	 * Get the odds that an ecosystems services optimizing agent will harvest.
	 */
	public double getEcosystemsAgentHarvestOdds() {
		return ecosystemsAgentHarvestOdds;
	}

	/**
	 * Set the target percentage of agents, as a double, that are economic
	 * optimizers.
	 */
	public void setEconomicAgentPercentage(double value) {
		if (value >= 0.0 && value <= 1.0) {
			economicAgentPercentage = value;
		}
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
	 * Main entry point for the model.
	 */
	public static void main(String[] args) {
		doLoop(ForestSim.class, args);
		System.exit(0);
	}

	/**
	 * Prepare the model to be run.
	 */
	public void start() {
		super.start();

		// Import all the GIS layers used in the simulation
		importRasterLayers();
		importVectorLayers();

		// Create the agents and assign one agent to each parcel
		createAgents();

		// Align the MBRs so layers line up in the display
		Envelope globalMBR = parcelLayer.getMBR();
		globalMBR.expandToInclude(coverLayer.getMBR());
		parcelLayer.setMBR(globalMBR);
		coverLayer.setMBR(globalMBR);
	}

	/**
	 * Import the ASCII grid file for NLCD (land cover)
	 */
	private void importRasterLayers() {
		try {
			InputStream inputStream = new FileInputStream(coverFile);
			ArcInfoASCGridImporter.read(inputStream, GridDataType.INTEGER, coverLayer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Import the parcel vector files for the model
	 */
	private void importVectorLayers() {
		// Create new GeomVectorFields to begin a new simulation
		parcelLayer = new GeomVectorField(gridWidth, gridHeight);

		// Specify GIS attributes to import with shapefile
		Bag desiredAttributes = new Bag();
		desiredAttributes.add("OWNER");

		// Import parcel layer shapefile
		try {
			ShapeFileImporter.read(new URL(parcelFile), parcelLayer, desiredAttributes, LandUseGeomWrapper.class);
		} catch (FileNotFoundException e) {
			System.out.println("Error opening shapefile:" + e);
			System.exit(-1);
		} catch (MalformedURLException e) {
			System.out.println("Error processing URL:" + e);
			System.exit(-1);
		}
	}

	/**
	 * Create a new agent.
	 * 
	 * @param lu The land use wrapper for the agent.
	 * @param probablity The probability that it should be a economic optimizing agent
	 * 
	 * @return The constructed agent.
	 */
	private Agent createAgent(LandUseGeomWrapper lu, double probablity) {
		double cover = random.nextDouble();
		Agent agent = (random.nextDouble() < probablity) ? new EconomicAgent(lu, cover) : new EcosystemsAgent(lu, cover);
		agent.setHarvestOdds(ecosystemsAgentHarvestOdds);
		return createAgentParcel(agent);
	}

	/**
	 * Get the NLCD pixels that the agent has control over.
	 * 
	 * @param agent The agent to get the pixels for.
	 * @return An updated agent.
	 */
	private Agent createAgentParcel(Agent agent) {
		// Get the agent's parcel 
		Geometry parcelPolygon = agent.getGeometry().getGeometry();

		// The bounding rectangle of the agent's parcel converted to an IntGrid2D index (min and max)
		int xMin = coverLayer.toXCoord(parcelPolygon.getEnvelopeInternal().getMinX());
		int yMin = coverLayer.toYCoord(parcelPolygon.getEnvelopeInternal().getMinY());
		int xMax = coverLayer.toXCoord(parcelPolygon.getEnvelopeInternal().getMaxX());
		int yMax = coverLayer.toYCoord(parcelPolygon.getEnvelopeInternal().getMaxY());

		// The pixels the agent's parcel covers will be stored here
		IntBag xPos = new IntBag();
		IntBag yPos = new IntBag();

		// Search all the pixels in the agent's parcel's bounding rectangle
		for (int x = xMin; x <= xMax; x++)
			for (int y = yMax; y <= yMin; y++) {
				// Skip ahead if the index is negative (no pixels here)
				if (x < 0 || y < 0) {
					continue;
				}

				// Get the value of the land cover at the current index
				int value = ((IntGrid2D) coverLayer.getGrid()).get(x, y);

				// Move to the next if this pixel is not woody biomass
				if (!woodyBiomass.contains(value)) {
					continue;
				}

				// Determine if the agent's parcel covers the current pixel
				Point point = coverLayer.toPoint(x, y);
				if (parcelPolygon.covers(point)) {
					// Store the index if the parcel covers the pixel
					xPos.add(x);
					yPos.add(y);
				}
			}

		// Pass the agent the indexes of the pixels the agent's parcel
		// covers
		agent.createCoverPoints(xPos, yPos);
		return agent;
	}

	/**
	 * Create all of the agents that are used in the model.
	 */
	private void createAgents() {
		// Assign one agent to each parcel and then schedule the agent
		Bag parcelGeoms = parcelLayer.getGeometries();
		agents = new Agent[parcelGeoms.numObjs];
		int index = 0;
		for (Object parcelPolygon : parcelGeoms) {
			Agent agent = createAgent((LandUseGeomWrapper) parcelPolygon, economicAgentPercentage);
			agents[index] = agent;
			schedule.scheduleRepeating(agent);
			index++;
		}
	}

	/**
	 * Store the agent information to the shape file and save it.
	 */
	public void finish() {
		super.finish();

		for (Agent agent : agents) {
			agent.updateShapefile();
		}

		// TODO Figure out why this is throwing an error
		// ShapeFileExporter.write(outputShapeFile, parcelLayer);
	}
}
