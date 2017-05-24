package edu.mtu.wup.model.parameters;

import org.javatuples.Pair;

import edu.mtu.simulation.parameters.ParameterBase;
import edu.mtu.wup.vip.VipFactory;
import edu.mtu.wup.vip.VipFactory.VipRegime;

public abstract class WupParameters extends ParameterBase {
	// Path to default GIS files used in the simulation
	public static final String defaultCoverFile = "shapefiles/WUP Land Cover/WUPLandCover.asc";
	public static final String defaultParcelFile = "file:shapefiles/WUP Parcels/WUPParcels.shp";
	
	/**
	 * Base millage rate for the model.
	 */
	public final static int MillageRate = 35;
	
	/**
	 * Assessed property value per acre.
	 */
	public final static double PropertyValue = 1000.0;

	private int loggingCapacity = 0;
	private int vipCoolDown = 0;
	private double ecosystemsNipfoHarvestOdds = 0.0;
	private long seed = 0;

	private Pair<Double, Double> economicNvpDiscountRate = Pair.with(0.0, 0.0);
	private Pair<Double, Double> nipfoWth = Pair.with(0.0, 0.0);
	private String outputDirectory = null;
	private VipRegime vip;
			
	/**
	 * Get the odds that an ecosystems NIPFO will harvest.
	 */
	public double getEcosystemsAgentHarvestOdds() { 
		return ecosystemsNipfoHarvestOdds; 
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
	 * Get the VIP cool down rate.
	 */
	public int getVipCoolDown() {
		return vipCoolDown;
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
		this.seed = value;
	}
	
	/**
	 * Set the VIP to run for the model.
	 */
	public void setVipProgram(VipRegime value) {
		VipFactory.getInstance().selectVip(value);
		vip = value;
	}

	/**
	 * Set the VIP cool down rate.
	 */
	public void setVipCoolDown(int value) {
		vipCoolDown = value;
	}
}
