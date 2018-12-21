package edu.mtu.wup.model;

/**
 * This class provides a means of encapsulating some basic economics for the simulation.
 */
public class Economics {
		
	/**
	 * Net present value
	 * 
	 * @param c Current value
	 * @param r Discount rate
	 * @param t Time periods
	 * @return
	 */
	public static double npv(double c, double r, long t) {
		return c / Math.pow(1 + r, t);
	}
}
