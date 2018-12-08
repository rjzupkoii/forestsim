package edu.mtu.wup.launch;

import edu.mtu.wup.model.WupModel;
import edu.mtu.wup.model.parameters.WupAgglomeration;
import edu.mtu.wup.model.parameters.WupParameters;

@SuppressWarnings("serial")
public class WupModelAgglomeration extends WupModel {
	
	private WupParameters parameters = new WupAgglomeration();
	
	/**
	 * Constructor.
	 * @param seed
	 */
	public WupModelAgglomeration(long seed) {
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
