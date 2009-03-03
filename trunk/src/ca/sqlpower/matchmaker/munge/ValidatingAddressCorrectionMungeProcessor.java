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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLIndex.Column;

/**
 * The {@link ValidatingAddressCorrectionMungeProcessor} differs from the
 * {@link MungeProcessor} in that insteaad of running through the entire
 * {@link MungeProcess}, when it hits an {@link AddressCorrectionMungeStep}, it
 * runs the Address Correction process on the input and then stores invalid
 * address data into the Address Correction Result table. Subsequent MungeSteps
 * will not be run until after an Address Validation process. After the
 * validation process has been finished, MungeSteps that follow after the
 * Address Correction step will be run to adapt the Address Correction step's
 * output to the structure of the table containing the Address data.
 */
public class ValidatingAddressCorrectionMungeProcessor extends MungeProcessor {
	
	private AddressPool pool;
	
	public ValidatingAddressCorrectionMungeProcessor(MungeProcess mungeProcess, AddressPool pool, Logger logger) {
		super(mungeProcess, logger);
		this.pool = pool;
	}

	@Override
	public Boolean call() throws Exception {
		return call(-1);
	}
	
	public Boolean call(int rowCount) throws Exception {
		
		monitorableHelper.setStarted(true);
		monitorableHelper.setFinished(false);
		monitorableHelper.setJobSize(rowCount);
		
		determineProcessOrder();
		List<MungeStep> processOrder = getProcessOrder();
		
		int addressStepIndex = -1;
		for (int index = 0; index < processOrder.size(); index++) {
			MungeStep step = processOrder.get(index);
			if (step instanceof AddressCorrectionMungeStep) {
				addressStepIndex = index;
				break;
			}
		}
		
		if (addressStepIndex < 0) {
			throw new IllegalStateException("Address Correction Munge Process has no Address Correction Step!");
		}
		
		MungeProcess process = getMungeProcess();
		
		List<MungeStep> preValidationSteps = processOrder.subList(0, addressStepIndex + 1);
		
		try {
			for (MungeStep step: preValidationSteps) {
				step.open(getLogger());
			}
			
			boolean finished = false;
			
			checkCancelled();
			
			while (!finished && (rowCount == -1 || monitorableHelper.getProgress() < rowCount)) {
				checkCancelled();
				
				for (MungeStep step: preValidationSteps) {
					checkCancelled();
					boolean continuing = step.call();
					
					if (step instanceof AddressCorrectionMungeStep) {
						AddressCorrectionMungeStep addressStep = (AddressCorrectionMungeStep) step;
						if (!addressStep.isAddressValid()) {
							List<MungeStepOutput> inputs = addressStep.getMSOInputs();
			
							SQLIndex uniqueKey = process.getParentProject().getSourceTableIndex();

							MungeStep inputStep = addressStep.getInputStep();
							
							List<Object> uniqueKeyValues = new ArrayList<Object>();

							for (Column col: uniqueKey.getChildren()) {
								MungeStepOutput output = inputStep.getOutputByName(col.getName());
								if (output == null) {
									throw new IllegalStateException("Input step is missing unique key column '" + col.getName() + "'");
								}
								uniqueKeyValues.add(output.getData());
							}
							
							MungeStepOutput addressLine1MSO = inputs.get(0);
							String addressLine1 = (addressLine1MSO != null) ? (String)addressLine1MSO.getData() : null;
							MungeStepOutput addressLine2MSO = inputs.get(1);
							String addressLine2 = (addressLine2MSO != null) ? (String)addressLine2MSO.getData() : null;
							MungeStepOutput municipalityMSO = inputs.get(2);
							String municipality = ( municipalityMSO != null) ? (String)municipalityMSO.getData() : null;
							MungeStepOutput provinceMSO = inputs.get(3);
							String province = (provinceMSO != null) ? (String)provinceMSO.getData() : null;
							MungeStepOutput countryMSO = inputs.get(4);
							String country = (countryMSO != null) ? (String)countryMSO.getData() : null;
							MungeStepOutput postalCodeMSO = inputs.get(5);
							String inPostalCode = (postalCodeMSO != null) ? (String)postalCodeMSO.getData() : null;
							
							AddressResult result = new AddressResult(uniqueKeyValues, addressLine1, addressLine2, municipality, province, inPostalCode, country, false);
							
							pool.addAddress(result, getLogger());
						}
					}
					
					if (!continuing) {
						finished = true;
						break;
					}
				}
				monitorableHelper.incrementProgress();
			}
			
			for (MungeStep step: preValidationSteps) {
				step.commit();
			}
			
			pool.store(getLogger());
			
		} catch (Throwable t) {
			for (MungeStep step: preValidationSteps) {
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
			for (MungeStep step: preValidationSteps) {
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
