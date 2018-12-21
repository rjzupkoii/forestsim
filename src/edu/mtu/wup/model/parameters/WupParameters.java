package edu.mtu.wup.model.parameters;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.javatuples.Pair;

import edu.mtu.simulation.parameters.ParameterBase;
import edu.mtu.wup.vip.VipFactory;
import edu.mtu.wup.vip.VipFactory.VipRegime;

public abstract class WupParameters extends ParameterBase {
	// Path to default GIS files used in the simulation
	public static final String defaultCoverFile = "shapefiles/WUP Land Cover/WUPLandCover.asc";
	public static final String defaultParcelFile = "file:shapefiles/WUP Parcels/WUPParcels.shp";
	
	public static final String defaultSettingsFile = "settings.ini";
	
	/**
	 * Land Tenure phase in rate for the model.
	 */
	public final static double LandTenurePhaseInRate = 1.0;	// Immediate activation
	
	/**
	 * Base millage rate for the model.
	 */
	public final static int MillageRate = 100;
	
	private int loggingCapacity = 0;
	private double ecosystemsNipfoHarvestOdds = 0.0;
	private double mooIntendsToHavestOdds = 0.0;
	private long seed = 0;

	private Pair<Double, Double> nipfoTaxConcerns = Pair.with(0.626, 0.082);		// NWOS 2013, Michigan, Table MI-32, "More Favorable Tax Policies"
	private Pair<Double, Double> economicNvpDiscountRate = Pair.with(0.0, 0.0);
	private Pair<Double, Double> nipfoWth = Pair.with(0.0, 0.0);
	private String outputDirectory = null;
	private VipRegime vip;
			
	/***
	 * Read the settings in the file, use them to override 
	 * anything that is currently set.
	 * 
	 * @param fileName The full path of the file (ini format) to be read.
	 */
	public void readFile(String fileName) throws InvalidFileFormatException, IOException {
		Ini ini = new Ini(new File(fileName));
		Preferences prefs = new IniPreferences(ini);
		int logging = Integer.parseInt(prefs.node("settings").get("logging", null));
		setLoggingCapacity(logging);
	}
	
	/**
	 * Get the odds that an ecosystems NIPFO will harvest.
	 */
	public double getEcosystemsAgentHarvestOdds() { 
		return ecosystemsNipfoHarvestOdds; 
	}
	
	/**
	 * Percentage and error associated with NWOS data for tax concerns.
	 */
	public Pair<Double, Double> getNifpoTaxConcerns() {
		return nipfoTaxConcerns;
	}
	
	/**
	 * Return the NVP discount rate for economic NIPFOs as a pair of &lt;mean, SD&gt;
	 */
	public Pair<Double, Double> getEconomicNvpDiscountRate() {
		return economicNvpDiscountRate;
	}
		
	/**
	 * Get the total logging capacity for the region.
	 */
	public int getLoggingCapacity() {
		return loggingCapacity;
	}
	
	/**
	 * Get the odds a MOO intends to harvest.
	 */
	public double getMooIntendsToHavestOdds() {
		return mooIntendsToHavestOdds;
	}
	
	/**
	 * Return WTH for the NIPFOs as a pair of &lt;mean, SD&gt;
	 */
	public Pair<Double, Double> getNipfoWth() {
		return nipfoWth;
	}

	/**
	 * Get the output directory for the scorecard.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	/**
	 * Get the random seed for this model.
	 */
	public long getSeed() {
		return seed;
	}
		
	/**
	 * Get the VIP that is being run.
	 */
	public VipRegime getVipProgram() {
		return vip;
	}

	/**
	 * Set the mean and SD for the economic NVP discount rate.
	 */
	public void setEconomicNpvDiscountRate(double mean, double sd) {
		economicNvpDiscountRate = Pair.with(mean, sd);
	}
	
	/**
	 * Set the odds that an ecosystems NIPFO will harvest.
	 */
	public void setEcosystemsAgentHarvestOdds(double value) {
		if (value >= 0.0 && value <= 1.0) {
			ecosystemsNipfoHarvestOdds = value;
		}
	}
	
	/**
	 * Set the logging capacity for the harvester.
	 */
	public void setLoggingCapacity(int value) {
		loggingCapacity = value;
	}
	
	/**
	 * Set the odds a MOO intends to harvest.
	 */
	public void setMooIntendsToHavestOdds(double value) {
		mooIntendsToHavestOdds = value;
	}
	
	/**
	 * Set the mean and SD for the WTH per acre for NIPFOs.
	 */
	public void setNipfoWth(double mean, double sd) {
		nipfoWth = Pair.with(mean, sd);
	}
			
	/**
	 * Set the output directory for the scorecard.
	 */
	public void setOutputDirectory(String value) {
		outputDirectory = value;
	}

	/**
	 * Set the random seed for this model.
	 */
	public void setSeed(long value) {
		seed = value;
	}
	
	/**
	 * Set the VIP to run for the model.
	 */
	public void setVipProgram(VipRegime value) {
		VipFactory.getInstance().selectVip(value);
		vip = value;
	}
}
