/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
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
	
	public AddressCorrectionMungeProcessor(MungeProcess mungeProcess, AddressPool pool, AddressCorrectionEngineMode mode, Logger logger) {
		super(mungeProcess, logger);
		this.pool = pool;
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
				throw new IllegalStateException("Address Correction Munge Process has no Address Correction Step!");
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
						if (!((AddressCorrectionMungeStep) step).isAddressCorrected()) {
							break;
						}
					}
					
				}
				monitorableHelper.incrementProgress();
			}
			
			for (MungeStep step: processOrder) {
				step.commit();
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
					step.close();
				} catch (Exception e) {
					getLogger().error("Exception thrown while attempting to close step " + step.getName(), e);
				}
			}
			monitorableHelper.setFinished(true);
		}
		
		return Boolean.TRUE;
	}

}
