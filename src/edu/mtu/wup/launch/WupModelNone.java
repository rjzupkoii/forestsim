package edu.mtu.wup.launch;

import edu.mtu.wup.model.WupModel;
import edu.mtu.wup.model.parameters.NoneParameters;
import edu.mtu.wup.model.parameters.WupParameters;

@SuppressWarnings("serial")
public class WupModelNone extends WupModel {
	
	protected WupParameters parameters = new NoneParameters();
	
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModelNone(long seed) {
		super(seed);
		
		try {
			parameters.setSeed(seed);
			parameters.readFile(WupParameters.defaultSettingsFile);
		} catch (Exception ex) {
			System.err.println("Error loading the model.");
			System.exit(-1);
		}
	}

	@Override
	public WupParameters getParameters() {
		return parameters;
	}
}
