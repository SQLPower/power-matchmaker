package ca.sqlpower.matchmaker;

/**
 * The set of possible result codes for an engine run.
 */
public enum EngineInvocationResult {
	
	/**
	 * The result code for a completely successful engine invocation.
	 */
	SUCCESS,
	
	/**
	 * The result code for an engine invocation that ran completely, but
	 * encountered some recoverable error conditions or other undesirable
	 * conditions.
	 */
	IFFY,
	
	/**
	 * The result code for an engine invocation which did not run to completion.
	 */
	FAILURE,
	
	/**
	 * The result code when the engine run was aborted explicitly.
	 */
	ABORTED
}