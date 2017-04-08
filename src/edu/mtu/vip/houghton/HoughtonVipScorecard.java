package edu.mtu.vip.houghton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.mtu.environment.Forest;
import edu.mtu.simulation.Scorecard;
import edu.mtu.steppables.marketplace.AggregateHarvester;

public class HoughtonVipScorecard implements Scorecard {

	private final static String biomassFile = "/biomass.csv";
	private final static String carbonFile = "/carbon.csv";
	private final static String recreationFile = "/recreation.csv";
	private final static String vipFile = "/vip.csv";

	private String outputDirectory;

	public HoughtonVipScorecard(String directory) {
		outputDirectory = directory;
	}

	@Override
	public void processTimeStep() {
		try {
			writeCarbonSequestration();
			writeHarvestedBiomass();
			writeRecreationalAccess();
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}
	}

	@Override
	public void processInitialization() {
		File directory = new File(outputDirectory);
		directory.mkdirs();
	}

	@Override
	public void processFinalization() {
		try {
			FileWriter writer = new FileWriter(outputDirectory + biomassFile, true);
			writer.write(System.lineSeparator());
			writer.close();
			
			writer = new FileWriter(outputDirectory + carbonFile, true);
			writer.write(System.lineSeparator());
			writer.close();
			
			writer = new FileWriter(outputDirectory + recreationFile, true);
			writer.write(System.lineSeparator());
			writer.close();
			
			writer = new FileWriter(outputDirectory + vipFile, true);
			writer.write(System.lineSeparator());
			writer.close();
		} catch (IOException ex) {
			System.err.println("Unhandled IOException: " + ex.toString());
			System.exit(-1);
		}
	}
	
	/**
	 * Find the approximate amount of carbon in the woody biomass.
	 * 
	 * @param biomass
	 *            The woody biomass in green tons.
	 * @return The carbon content of the vegetation in gigatons (Gt)
	 */
	private double carbonInBiomassEstiamte(double biomass) {
		// Use the approximation given by (Magnussen & Reed, 2015)
		// 62% moisture content is a rough approximation from (Wenger 1984)
		return (0.475 * (biomass * 0.62)) / 1000000000;
	}

	// Society: VIP Enrollment, Recreational Access
	private void writeRecreationalAccess() throws IOException {
		VIP vip = VIP.getInstance();
		FileWriter writer = new FileWriter(outputDirectory + recreationFile, true);
		writer.write(vip.getSubscribedArea() + ",");
		writer.close();
		
		writer = new FileWriter(outputDirectory + vipFile, true);
		writer.write(vip.getSubscriptionRate() + ",");
		writer.close();
	}

	// Environment: Carbon Sequestration
	private void writeCarbonSequestration() throws IOException {
		double biomass = Forest.getInstance().calculateTotalBiomass();
		double carbon = carbonInBiomassEstiamte(biomass);
		FileWriter writer = new FileWriter(outputDirectory + carbonFile, true);
		writer.write(carbon + ",");
		writer.close();
	}

	// Economic: Woody Biomass Availability, Reliability / consistent supply of
	// woody biomass
	private void writeHarvestedBiomass() throws IOException {
		double biomass = AggregateHarvester.getInstance().getBiomass();
		FileWriter writer = new FileWriter(outputDirectory + biomassFile, true);
		writer.write(biomass + ",");
		writer.close();
	}
}
