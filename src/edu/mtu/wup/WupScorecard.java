package edu.mtu.wup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.mtu.environment.Forest;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import sim.field.geo.GeomGridField;
import sim.io.geo.ArcInfoASCGridExporter;

public class WupScorecard implements Scorecard {
	
	private final static String biomassFile = "/biomass.csv";
	private final static String carbonFile = "/carbon.csv";
	private final static String stockingFile = "/stocking%1$d.asc";
	private final static String vipFile = "/vip.csv";
	
	private static int step = 0;
	private static int nextExport = 10;
	
	private String outputDirectory;
	
	public WupScorecard(String directory) {
		outputDirectory = directory;
	}
	
	@Override
	public void processTimeStep() {
		try {
			writeCarbonSequestration();
			writeHarvestedBiomass();
			writeRecreationalAccess();
			
			// Check the step and export as needed
			step++;
			if (step == nextExport) {
				writeRasterFiles();
				nextExport += 10;
			}
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}
	}

	@Override
	public void processInitialization() {
		try {
			// Bootstrap any relevant paths
			File directory = new File(outputDirectory);
			directory.mkdirs();
						
			// Store the initial stocking
			BufferedWriter output = new BufferedWriter(new FileWriter(outputDirectory + "/stocking0.asc"));
			GeomGridField stocking = Forest.getInstance().getStocking();
			ArcInfoASCGridExporter.write(stocking, output);
			output.close();		
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}	
	}
	
	/**
	 * Find the approximate amount of carbon in the woody biomass.
	 * 
	 * @param biomass The woody biomass in green tons.
	 * @return The carbon content of the vegetation in gigatons (Gt)
	 */
	private double carbonInBiomassEstiamte(double biomass) {
		// Use the approximation given by (Magnussen & Reed, 2015) 
		// 62% moisture content is a rough approximation from (Wenger 1984)
		return (0.475 * (biomass * 0.62)) / 1000000000;		
	}
	
	// Society: Aesthetics, Environment: Habitat Connectivity
	private void writeRasterFiles() throws IOException {
		// TODO Most likely for this we need to know DBH as well as stocking
		
		GeomGridField stocking = Forest.getInstance().getStocking();
		BufferedWriter output = new BufferedWriter(new FileWriter(String.format(outputDirectory + stockingFile, step)));
		ArcInfoASCGridExporter.write(stocking, output);
		output.close();
	}

	// Society: Recreational Access
	private void writeRecreationalAccess() throws IOException {
		VIP vip = VIP.getInstance();
		FileWriter writer = new FileWriter(outputDirectory + vipFile, true);
		writer.write(vip.getSubscriptionRate() + "," + vip.getSubscribedArea() + ",");
		writer.write(System.lineSeparator());
		writer.close();
	}

	// Environment: Carbon Sequestration
	private void writeCarbonSequestration() throws IOException {
		double biomass = Forest.getInstance().calculateTotalBiomass();
		double carbon = carbonInBiomassEstiamte(biomass);
		FileWriter writer = new FileWriter(outputDirectory + carbonFile, true);
		writer.write(carbon + "," + System.lineSeparator());
		writer.close();
	}

	// Economic: Woody Biomass Availability, Reliability / consistent supply of woody biomass
	private void writeHarvestedBiomass() throws IOException {
		double biomass = AggregateHarvester.getInstance().getBiomass();
		FileWriter writer = new FileWriter(outputDirectory + biomassFile, true);
		writer.write(biomass + "," + System.lineSeparator());
		writer.close();
	}
}