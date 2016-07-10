package edu.mtu.simulation;

import java.awt.Color;

import javax.swing.JFrame;

import edu.mtu.models.Forest;
import edu.mtu.utilities.NlcdClassification;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;

public class ForestSimWithUI extends GUIState {

	private Display2D display;
	private JFrame displayFrame;
	
	// Parcel layer portrayal
	private GeomVectorFieldPortrayal parcelPortrayal = new GeomVectorFieldPortrayal();
	
	// Land cover layer portrayal
	private FastValueGridPortrayal2D coverPortrayal = new FastValueGridPortrayal2D();
	
	// Stand height portrayal
	private FastValueGridPortrayal2D heightPortrayal = new FastValueGridPortrayal2D();
	
	public ForestSimWithUI(SimState state) {
		super(state);
	}
	
	public ForestSimWithUI() {
		super(new ForestSim(System.currentTimeMillis()));
	}
	
	public void init(Controller controller) {
		super.init(controller);
		
		display = new Display2D(1000, 900, this);
		
		// Attach the land cover layers and then overlay the parcel layer
		display.attach(heightPortrayal, "Current Forest Stand Height", false);
		display.attach(coverPortrayal, "Current Land Cover");
		display.attach(parcelPortrayal, "Parcels Layer");
				
		displayFrame = display.createFrame();
		controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
	}
	
	/**
	 * Prepare a model inspector for the UI. 
	 */
	public Inspector getInspector() {
		Inspector inspector = super.getInspector();
		inspector.setVolatile(true);
		return inspector;
	}
	
	/**
	 * Get a state object for the UI.
	 */
	public Object getSimulationInspectedObject() { return state; }
	
	public void start() {
		super.start();
		setupPortrayals();
	}
	
	private void setupPortrayals() {
		ForestSim world = (ForestSim)state;
		
		// Portray the parcel as an unfilled polygon with black borders
		//parcelPortrayal.setField(world.parcelLayer);
		parcelPortrayal.setField(world.parcelLayer);
		parcelPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, false));

		// Portray the current stand height
		heightPortrayal.setField(world.forest.getStandHeight().getGrid());
		heightPortrayal.setMap(new SimpleColorMap(Forest.InitialHeight, Forest.MaximumHeight, Color.WHITE, Color.DARK_GRAY));
		
		// Portray the current land cover based on the cover type scheme of NLCD
		coverPortrayal.setField(world.forest.getLandCover().getGrid());
		Color[] coverColors = NlcdClassification.getColorMap();
		coverColors[0] = Color.WHITE;
		coverPortrayal.setMap(new SimpleColorMap(coverColors));
		
		display.reset();
		display.setBackdrop(Color.WHITE);
		
		display.repaint();
	}
	
	public static void main(String[] args) {
		ForestSimWithUI fs = new ForestSimWithUI();
		Console c = new Console(fs);
		c.setVisible(true);
	}
}
