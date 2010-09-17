/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MungeSettings;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressCorrectionEngine.AddressCorrectionEngineMode;

/**
 * The {@link AddressCorrectionMungeProcessor} differs from the
 * {@link MungeProcessor} in that insteaad of running through the entire
 * {@link MungeProcess}, when it hits an {@link AddressCorrectionMungeStep}, it
 * runs the Address Correction process on the input and then stores invalid
 * address data into the Address Correction Result table. Subsequent MungeSteps
 * will not be run until after an Address Validation process. After the
 * validation process has been finished, MungeSteps that follow after the
 * Address Correction step will be run to adapt the Address Correction step's
 * output to the structure of the table containing the Address data.
 */
public class AddressCorrectionMungeProcessor extends MungeProcessor {
	
	private AddressPool pool;
	
	private AddressCorrectionEngineMode mode;
	
	private int numValid = 0;
	private int numCorrectable = 0;
	private int numIncorrectable = 0;
	private int numWritten = 0;

	private final MungeSettings settings;
	
	public AddressCorrectionMungeProcessor(MungeProcess mungeProcess, AddressPool pool, MungeSettings settings, AddressCorrectionEngineMode mode, Logger logger) {
		super(mungeProcess, logger);
		this.pool = pool;
		this.settings = settings;
		this.mode = mode;
	}

	public Boolean call(int rowCount) throws Exception {
		monitorableHelper.setStarted(true);
		monitorableHelper.setFinished(false);
		monitorableHelper.setJobSize(rowCount);
		
		determineProcessOrder();
		List<MungeStep> processOrder = getProcessOrder();
		
		try {
			boolean addressStepExists = false;
			
			for (MungeStep step: processOrder) {
				if (step instanceof AddressCorrectionMungeStep) {
					addressStepExists = true;
					((AddressCorrectionMungeStep) step).setAddressPool(pool, getLogger());
				}
				step.open(mode, getLogger());
			}

			if (!addressStepExists ) {
				throw new IllegalStateException("Address Correction Transformation has no Address Correction Step!");
			}
			
			boolean finished = false;
			
			checkCancelled();
			
			while (!finished && (rowCount == -1 || monitorableHelper.getProgress() < rowCount)) {
				checkCancelled();
				
				for (MungeStep step: processOrder) {
					checkCancelled();

					boolean continuing = step.call();

					if (!continuing) {
						finished = true;
						break;
					}
					
					if (step instanceof AddressCorrectionMungeStep) {
						AddressCorrectionMungeStep addressStep = (AddressCorrectionMungeStep) step;
						if (mode == AddressCorrectionEngineMode.ADDRESS_CORRECTION_PARSE_AND_CORRECT_ADDRESSES) {
							switch (addressStep.getAddressStatus()) {
								case VALID:
									numValid++;
									break;
								case CORRECTABLE:
									numCorrectable++;
									break;
								case INCORRECTABLE:
									numIncorrectable++;
							}
						}
						if (!addressStep.isAddressCorrected()) {
							getLogger().debug("Not writing anything to the source table for this record");
							break;
						} else {
							getLogger().debug("Writing this corrected address back to the source table");
							numWritten++;
						}
					}
					
				}
				monitorableHelper.incrementProgress();
			}
			
			if (settings.getDebug()) {
				for (MungeStep step: processOrder) {
					step.mungeRollback();
				}
			} else {
				for (MungeStep step: processOrder) {
					step.mungeCommit();
				}
			}
		} catch (Throwable t) {
			for (MungeStep step: processOrder) {
				try {
					step.rollback();
				} catch (Exception e) {
					getLogger().error("Execption thrown while attempting to rollback step " + step.getName(), e);
				}
			}
			if (t instanceof Exception) {
				throw (Exception) t;
			} else {
				throw new Exception(t);
			}
		} finally {
			for (MungeStep step: processOrder) {
				try {
					step.mungeClose();
				} catch (Exception e) {
					getLogger().error("Exception thrown while attempting to close step " + step.getName(), e);
				}
			}
			monitorableHelper.setFinished(true);
		}
		
		return Boolean.TRUE;
	}

	public int getNumValid() {
		return numValid;
	}

	public int getNumCorrectable() {
		return numCorrectable;
	}

	public int getNumIncorrectable() {
		return numIncorrectable;
	}
	
	public int getNumWritten() {
		return numWritten;
	}
}
