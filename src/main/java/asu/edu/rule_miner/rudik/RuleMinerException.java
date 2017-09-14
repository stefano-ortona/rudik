package asu.edu.rule_miner.rudik;

import org.slf4j.Logger;

@SuppressWarnings("serial")
public class RuleMinerException extends RuntimeException {
	public RuleMinerException(final String message, final Logger logger) {
		super(message);
		if (logger != null) {
			logger.error(message);
		}
	}

	public RuleMinerException(final String message, final Throwable cause, 
			final Logger logger) {
		super(message, cause);
		if (logger != null) {
			logger.error(message);
		}
	}
	
	public RuleMinerException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
