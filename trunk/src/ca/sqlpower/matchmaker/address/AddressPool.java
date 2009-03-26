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

package ca.sqlpower.matchmaker.address;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TypeMap;
import ca.sqlpower.matchmaker.address.AddressResult.StorageState;
import ca.sqlpower.matchmaker.address.PostalCode.RecordType;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.sqlobject.SQLIndex.Column;
import ca.sqlpower.util.MonitorableImpl;

/**
 * An object representation of the Address Correction result table. It is used
 * by the Address Correction Engine to batch new invalid address records before
 * storing them into the database, and by the Address Correction Validation
 * screen to populate the list of invalid addresses and their details.
 */
public class AddressPool extends MonitorableImpl{

	/**
	 * A list of constants for the Address Correction result table column names
	 */
	private static final String SOURCE_ADDRESS_KEY_COLUMN_BASE 		= "src_addr_key_col_";
	private static final String INPUT_ADDRESS_LINE1 				= "input_address_line1";
	private static final String INPUT_ADDRESS_LINE2 				= "input_address_line2";
	private static final String INPUT_MUNICIPALITY		 			= "input_municipality";
	private static final String INPUT_PROVINCE 						= "input_province";
	private static final String INPUT_COUNTRY 						= "input_country";
	private static final String INPUT_POSTAL_CODE 					= "input_postal_code";
	private static final String OUTPUT_COUNTRY						= "output_country";
	private static final String OUTPUT_DELIVERY_INSTALLATION_NAME 	= "output_delivery_installation_name";
	private static final String OUTPUT_DELIVERY_INSTALLATION_TYPE 	= "output_delivery_installation_type";
	private static final String OUTPUT_DIRECTION_PREFIX				= "output_direction_prefix";
	private static final String OUTPUT_FAILED_PARSING_STRING		= "output_failed_parsing_string";
	private static final String OUTPUT_GENERAL_DELIVERY_NAME		= "output_general_delivery_name";
	private static final String OUTPUT_LOCK_BOX_NUMBER				= "output_lock_box_number";
	private static final String OUTPUT_LOCK_BOX_TYPE				= "output_lock_box_type";
	private static final String OUTPUT_MUNICIPALITY 				= "output_municipality";
	private static final String OUTPUT_POSTAL_CODE		 			= "output_postal_code";
	private static final String OUTPUT_PROVINCE 					= "output_province";
	private static final String OUTPUT_RURAL_ROUTE_NUMBER			= "output_rural_route_number";
	private static final String OUTPUT_RURAL_ROUTE_TYPE				= "output_rural_route_type";
	private static final String OUTPUT_STREET_NAME	 				= "output_street_name";
	private static final String OUTPUT_STREET_DIRECTION 			= "output_street_direction";
	private static final String OUTPUT_STREET_NUMBER 				= "output_street_number";
	private static final String OUTPUT_STREET_NUMBER_SUFFIX 		= "output_street_number_suffix";
	private static final String OUTPUT_STREET_TYPE 					= "output_street_type";
	private static final String OUTPUT_STREET_TYPE_PREFIX			= "output_street_type_prefix";
	private static final String OUTPUT_SUITE 						= "output_suite";
	private static final String OUTPUT_SUITE_PREFIX					= "output_suite_prefix";
	private static final String OUTPUT_SUITE_TYPE					= "output_suite_type";
	private	static final String OUTPUT_TYPE							= "output_type";
	private static final String OUTPUT_UNPARSED_ADDRESS				= "output_unparsed_address";
	private static final String OUTPUT_URBAN_BEFORE_RURAL			= "output_urban_before_rural";
	private static final String OUTPUT_VALID						= "output_valid";
	
	private Map<List<Object>, AddressResult> addresses;
	
	private final Project project;
	
	public AddressPool(Project project) {
		this.project = project;
		this.addresses = new HashMap<List<Object>, AddressResult>();
	}
	
	/**
	 * Builds a result table specifically configured for Address Correction
	 * projects. Note that this only builds the SQLTable object representing the
	 * result table. It does not execute the SQL statements necessary to create
	 * such a table in a database.
	 * 
	 * @param resultTable
	 * @param si
	 * @return A {@link SQLTable} representing the result table.
	 */
	public static SQLTable buildAddressCorrectionResultTable(SQLTable resultTable, SQLIndex si) throws SQLObjectException {
		SQLTable t = new SQLTable(resultTable.getParent(), resultTable.getName(), resultTable.getRemarks(), "TABLE", true);
		
		for (int i = 0; i < si.getChildCount(); i++) {
			SQLColumn idxCol = ((Column) si.getChild(i)).getColumn();
			SQLColumn newCol = new SQLColumn(t, SOURCE_ADDRESS_KEY_COLUMN_BASE+i, idxCol.getType(), idxCol.getPrecision(), idxCol.getScale());
			t.addColumn(newCol);
		}
		
		SQLColumn inputAddressLine1 = new SQLColumn(t, INPUT_ADDRESS_LINE1, Types.VARCHAR, 70, 0);
		t.addColumn(inputAddressLine1);
		SQLColumn inputAddressLine2 = new SQLColumn(t, INPUT_ADDRESS_LINE2, Types.VARCHAR, 70, 0);
		t.addColumn(inputAddressLine2);
		SQLColumn inputMunicipality = new SQLColumn(t, INPUT_MUNICIPALITY, Types.VARCHAR, 30, 0);
		t.addColumn(inputMunicipality);
		SQLColumn inputProvince = new SQLColumn(t, INPUT_PROVINCE, Types.VARCHAR, 30, 0);
		t.addColumn(inputProvince);
		SQLColumn inputCountry = new SQLColumn(t, INPUT_COUNTRY, Types.VARCHAR, 30, 0);
		t.addColumn(inputCountry);
		SQLColumn inputPostalCode = new SQLColumn(t, INPUT_POSTAL_CODE, Types.VARCHAR, 10, 0);
		t.addColumn(inputPostalCode);

		SQLColumn outputCountry = new SQLColumn(t, OUTPUT_COUNTRY, Types.VARCHAR, 30, 0);
		t.addColumn(outputCountry);
		SQLColumn outputDeliveryInstallationName = new SQLColumn(t, OUTPUT_DELIVERY_INSTALLATION_NAME, Types.VARCHAR, 50, 0);
		t.addColumn(outputDeliveryInstallationName);
		SQLColumn outputDeliveryInstallationType = new SQLColumn(t, OUTPUT_DELIVERY_INSTALLATION_TYPE, Types.VARCHAR, 5, 0);
		t.addColumn(outputDeliveryInstallationType);
		SQLColumn outputDirectionPrefix = new SQLColumn(t, OUTPUT_DIRECTION_PREFIX, Types.BOOLEAN, 1, 0);
		t.addColumn(outputDirectionPrefix);
		SQLColumn outputFailedParsingString = new SQLColumn(t, OUTPUT_FAILED_PARSING_STRING, Types.VARCHAR, 150, 0);
		t.addColumn(outputFailedParsingString);
		SQLColumn outputGeneralDeliveryName = new SQLColumn(t, OUTPUT_GENERAL_DELIVERY_NAME, Types.VARCHAR, 70, 0);
		t.addColumn(outputGeneralDeliveryName);
		SQLColumn outputLockBoxNumber = new SQLColumn(t, OUTPUT_LOCK_BOX_NUMBER, Types.VARCHAR, 5, 0);
		t.addColumn(outputLockBoxNumber);
		SQLColumn outputLockBoxType = new SQLColumn(t, OUTPUT_LOCK_BOX_TYPE, Types.VARCHAR, 6, 0);
		t.addColumn(outputLockBoxType);
		SQLColumn outputMunicipality = new SQLColumn(t, OUTPUT_MUNICIPALITY, Types.VARCHAR, 30, 0);
		t.addColumn(outputMunicipality);
		SQLColumn outputPostalCode = new SQLColumn(t, OUTPUT_POSTAL_CODE, Types.VARCHAR, 10, 0);
		t.addColumn(outputPostalCode);
		SQLColumn outputProvince = new SQLColumn(t, OUTPUT_PROVINCE, Types.VARCHAR, 30, 0);
		t.addColumn(outputProvince);
		SQLColumn outputRuralRouteNumber = new SQLColumn(t, OUTPUT_RURAL_ROUTE_NUMBER, Types.VARCHAR, 5, 0);
		t.addColumn(outputRuralRouteNumber);
		SQLColumn outputRuralRouteType = new SQLColumn(t, OUTPUT_RURAL_ROUTE_TYPE, Types.VARCHAR, 2, 0);
		t.addColumn(outputRuralRouteType);
		SQLColumn outputStreetDirection = new SQLColumn(t, OUTPUT_STREET_DIRECTION, Types.VARCHAR, 5, 0);
		t.addColumn(outputStreetDirection);
		SQLColumn outputStreetName = new SQLColumn(t, OUTPUT_STREET_NAME, Types.VARCHAR, 30, 0);
		t.addColumn(outputStreetName);
		SQLColumn outputStreetNumber = new SQLColumn(t, OUTPUT_STREET_NUMBER, Types.INTEGER, 6, 0);
		t.addColumn(outputStreetNumber);
		SQLColumn outputStreetNumberSuffix = new SQLColumn(t, OUTPUT_STREET_NUMBER_SUFFIX, Types.VARCHAR, 6, 0);
		t.addColumn(outputStreetNumberSuffix);
		SQLColumn outputStreetType = new SQLColumn(t, OUTPUT_STREET_TYPE, Types.VARCHAR, 11, 0);
		t.addColumn(outputStreetType);
		SQLColumn outputStreetTypePrefix = new SQLColumn(t, OUTPUT_STREET_TYPE_PREFIX, Types.BOOLEAN, 1, 0);
		t.addColumn(outputStreetTypePrefix);
		SQLColumn outputSuite = new SQLColumn(t, OUTPUT_SUITE, Types.VARCHAR, 6, 0);
		t.addColumn(outputSuite);
		SQLColumn outputSuitePrefix = new SQLColumn(t, OUTPUT_SUITE_PREFIX, Types.BOOLEAN, 1, 0);
		t.addColumn(outputSuitePrefix);
		SQLColumn outputSuiteType = new SQLColumn(t, OUTPUT_SUITE_TYPE, Types.VARCHAR, 6, 0);
		t.addColumn(outputSuiteType);
		SQLColumn outputUnparsedAddress = new SQLColumn(t, OUTPUT_UNPARSED_ADDRESS, Types.VARCHAR, 150, 0);
		t.addColumn(outputUnparsedAddress);
		SQLColumn outputType = new SQLColumn(t, OUTPUT_TYPE, Types.VARCHAR, 8, 0);
		t.addColumn(outputType);
		SQLColumn outputUrbanBeforeRural = new SQLColumn(t, OUTPUT_URBAN_BEFORE_RURAL, Types.BOOLEAN, 1, 0);
		t.addColumn(outputUrbanBeforeRural);
		SQLColumn valid = new SQLColumn(t, OUTPUT_VALID, Types.BOOLEAN, 1, 0);
		t.addColumn(valid);
		
		SQLIndex newidx = new SQLIndex(t.getName()+"_uniq", true, null, null, null);
		for (int i = 0; i < si.getChildCount(); i++) {
			newidx.addChild(newidx.new Column(t.getColumn(i), AscendDescend.ASCENDING));
		}
		t.addIndex(newidx);
		
		return t;
	}
	
	public void addAddress(AddressResult result, Logger engineLogger) {
		List<Object> key = result.getKeyValues();
		if (addresses.containsKey(key)) {
			engineLogger.debug("Address added marked as dirty");
			result.markDirty();
		}
		addresses.put(key, result);
	}
	
	public void clear() throws SQLException {
		SQLTable resultTable = project.getResultTable();
		Connection con = null;
		Statement stmt = null;
		
		try {
			con = project.createResultTableConnection();
			stmt = con.createStatement();
			
			con.setAutoCommit(false);
			String sql = "DELETE FROM " + DDLUtils.toQualifiedName(resultTable) + " WHERE 1=1";
			stmt.execute(sql);
			con.commit();
		} catch (Exception ex) {
			con.rollback();
			if (ex instanceof SQLException) {
				throw (SQLException) ex;
			} else {
				throw new RuntimeException("An unexpected error occured while clearing the Address Pool", ex);
			}
		} finally {
			if (stmt != null) stmt.close();
			if (con != null) stmt.close();
		}
		
		addresses.clear();
	}
	
	public void load(Logger engineLogger) throws SQLException, SQLObjectException {
		setCancelled(false);
		setStarted(true);
		setFinished(false);
		setProgress(0);
		
		SQLTable resultTable = project.getResultTable();
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		setJobSize(getNumRowsToProcess());
		
		try {
			con = project.createResultTableConnection();
			
			stmt = con.createStatement();
			
			StringBuilder sql = new StringBuilder("SELECT * FROM ");
			appendFullyQualifiedTableName(sql, resultTable);
			
			rs = stmt.executeQuery(sql.toString()); 
			
			while (rs.next()) {
				List<Object> keyValues = new ArrayList<Object>();
				int numKeys = project.getSourceTableIndex().getChildCount();

				// We need to convert the column types to the base set of
				// String, Boolean, BigDecimal, and Date that we use in the
				// Munge Processes. Otherwise, when we cannot properly compare
				// the key values of these loaded. Addresses with the ones
				// coming through the munge process.
				for (int i = 0; i < numKeys; i++) {
					int type = project.getSourceTableIndex().getChild(i).getColumn().getType();
					Class c = TypeMap.typeClass(type);
					if (c == BigDecimal.class) {
						keyValues.add(rs.getBigDecimal(SOURCE_ADDRESS_KEY_COLUMN_BASE + i));
					} else if (c == Date.class) {
						keyValues.add(rs.getDate(SOURCE_ADDRESS_KEY_COLUMN_BASE + i));
		            } else if (c == Boolean.class) {
		            	keyValues.add(rs.getBoolean(SOURCE_ADDRESS_KEY_COLUMN_BASE + i));
		            } else {
		            	keyValues.add(rs.getString(SOURCE_ADDRESS_KEY_COLUMN_BASE + i));
		            }
				}
				
				String addressLine1 = rs.getString(INPUT_ADDRESS_LINE1);
				String addressLine2 = rs.getString(INPUT_ADDRESS_LINE2);
				String municipality = rs.getString(INPUT_MUNICIPALITY);
				String province = rs.getString(INPUT_PROVINCE);
				String country = rs.getString(INPUT_COUNTRY);
				String postalCode = rs.getString(INPUT_POSTAL_CODE);
				
				Address address = new Address();
				address.setCountry(rs.getString(OUTPUT_COUNTRY));
				address.setDeliveryInstallationName(rs.getString(OUTPUT_DELIVERY_INSTALLATION_NAME));
				address.setDeliveryInstallationType(rs.getString(OUTPUT_DELIVERY_INSTALLATION_TYPE));
				address.setDirectionPrefix(rs.getBoolean(OUTPUT_DIRECTION_PREFIX));
				address.setFailedParsingString(rs.getString(OUTPUT_FAILED_PARSING_STRING));
				address.setGeneralDeliveryName(rs.getString(OUTPUT_GENERAL_DELIVERY_NAME));
				address.setLockBoxNumber(rs.getString(OUTPUT_LOCK_BOX_NUMBER));
				address.setLockBoxType(rs.getString(OUTPUT_LOCK_BOX_TYPE));
				address.setMunicipality(rs.getString(OUTPUT_MUNICIPALITY));
				address.setPostalCode(rs.getString(OUTPUT_POSTAL_CODE));
				address.setProvince(rs.getString(OUTPUT_PROVINCE));
				address.setRuralRouteNumber(rs.getString(OUTPUT_RURAL_ROUTE_NUMBER));
				address.setRuralRouteType(rs.getString(OUTPUT_RURAL_ROUTE_TYPE));
				address.setStreet(rs.getString(OUTPUT_STREET_NAME));
				address.setStreetDirection(rs.getString(OUTPUT_STREET_DIRECTION));
				address.setStreetNumberSuffix(rs.getString(OUTPUT_STREET_NUMBER_SUFFIX));
				address.setStreetNumber(rs.getInt(OUTPUT_STREET_NUMBER));
				address.setStreetType(rs.getString(OUTPUT_STREET_TYPE));
				address.setStreetTypePrefix(rs.getBoolean(OUTPUT_STREET_TYPE_PREFIX));
				address.setSuite(rs.getString(OUTPUT_SUITE));
				address.setSuitePrefix(rs.getBoolean(OUTPUT_SUITE_PREFIX));
				address.setSuiteType(rs.getString(OUTPUT_SUITE_TYPE));
				String typeString = rs.getString(OUTPUT_TYPE);
				if (typeString != null) {
					address.setType(RecordType.valueOf(rs.getString(OUTPUT_TYPE)));
				}
				address.setUnparsedAddressLine1(rs.getString(OUTPUT_UNPARSED_ADDRESS));
				address.setUrbanBeforeRural(rs.getBoolean(OUTPUT_URBAN_BEFORE_RURAL));
				
				Boolean valid = rs.getBoolean(OUTPUT_VALID);
				
				AddressResult result = new AddressResult(keyValues, addressLine1,
						addressLine2, municipality, province, postalCode, country,
						address, valid);
				result.markClean();
				
				addresses.put(keyValues, result);
				incrementProgress();
			}
			engineLogger.debug("Loaded " + addresses.size() + " addresses from the result table");
		} finally { 
			setFinished(true);
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
			if (con != null) con.close();
		}
	}

	/**
	 * Inserts and updates the contents of the result table with the
	 * {@link AddressResult} contents in this {@link AddressPool}.
	 * AddressResults that are marked as {@link StorageState#DIRTY} are assumed
	 * to be already in the database and are updated. AddressResults that are
	 * marked as {@link StorageState#NEW} are assumed to be new entries that do
	 * no yet exist in the database and are inserted.
	 * 
	 * It is worth noting that at the moment, new Address results won't have an
	 * output street number yet (since they have not been validated yet) but a
	 * {@link NullPointerException} gets thrown if we try to insert a null
	 * Integer, so for the time being, I've set the 'null' steet number to be
	 * -1, since I don't believe there's anyone with a negative street number,
	 * but if I'm wrong, this will have to be changed.
	 * 
	 * @throws SQLException
	 * @throws SQLObjectException
	 */
	public void store(Logger engineLogger, boolean useBatchExecute, boolean debug) throws SQLException, SQLObjectException {
		setStarted(true);
		setFinished(false);
		setCancelled(false);
		setProgress(0);
		
		List<AddressResult> dirtyAddresses = new ArrayList<AddressResult>();
		List<AddressResult> newAddresses = new ArrayList<AddressResult>();
		
		for (List<Object> key: addresses.keySet()) {
			AddressResult result = addresses.get(key);
			if (result.getStorageState() == StorageState.DIRTY) {
				dirtyAddresses.add(result);
			} else if (result.getStorageState() == StorageState.NEW) {
				newAddresses.add(result);
			}

		}

		setJobSize(dirtyAddresses.size() + newAddresses.size());
		
		engineLogger.debug("# of Dirty Address Records:" + dirtyAddresses.size());
		engineLogger.debug("# of New Address Records:" + newAddresses.size());
		
		Connection con = null;
		PreparedStatement ps = null;
		Statement stmt = null;
		
		try {
			con = project.createResultTableConnection();
			con.setAutoCommit(false);
			boolean useBatchUpdates = useBatchExecute && con.getMetaData().supportsBatchUpdates();
			SQLTable resultTable = project.getResultTable();
			int keySize = project.getSourceTableIndex().getChildCount();

			StringBuilder sql;
			
			if (dirtyAddresses.size() > 0) {
				
				stmt = con.createStatement();
				
				for (int i = 0; i < dirtyAddresses.size(); i++) {
					
					sql= new StringBuilder();
					AddressResult result = dirtyAddresses.get(i);
					
					//First, create and UPDATE PreparedStatement to update dirty records
					sql.append("UPDATE ");
				
					appendFullyQualifiedTableName(sql, resultTable);
					
					sql.append(" SET ");
					sql.append(INPUT_ADDRESS_LINE1).append("=" + SQL.quote(result.getInputAddress().getUnparsedAddressLine1()) + ", ");					// 1
					sql.append(INPUT_ADDRESS_LINE2).append("=" + SQL.quote(result.getInputAddress().getUnparsedAddressLine2()) + ", ");					// 2
					sql.append(INPUT_MUNICIPALITY).append("=" + SQL.quote(result.getInputAddress().getMunicipality()) + ", ");					// 3
					sql.append(INPUT_PROVINCE).append("=" + SQL.quote(result.getInputAddress().getProvince()) + ", ");						// 4
					sql.append(INPUT_COUNTRY).append("=" + SQL.quote(result.getInputAddress().getCountry()) + ", ");						// 5
					sql.append(INPUT_POSTAL_CODE).append("=" + SQL.quote(result.getInputAddress().getPostalCode()) + ", ");					// 6
					
					Address outputAddress = result.getOutputAddress();
					sql.append(OUTPUT_COUNTRY).append("=" + SQL.quote(outputAddress.getSuite()) + ", ");						// 7
					sql.append(OUTPUT_DELIVERY_INSTALLATION_NAME).append("=" + SQL.quote(outputAddress.getDeliveryInstallationName()) + ", ");	// 8
					sql.append(OUTPUT_DELIVERY_INSTALLATION_TYPE).append("=" + SQL.quote(outputAddress.getDeliveryInstallationType()) + ", ");	// 9			
					sql.append(OUTPUT_DIRECTION_PREFIX).append("=" + outputAddress.isDirectionPrefix() + ", ");				// 10
					sql.append(OUTPUT_FAILED_PARSING_STRING).append("=" + SQL.quote(outputAddress.getFailedParsingString()) + ", ");		// 11
					sql.append(OUTPUT_GENERAL_DELIVERY_NAME).append("=" + SQL.quote(outputAddress.getGeneralDeliveryName()) + ", ");		// 12
					sql.append(OUTPUT_LOCK_BOX_NUMBER).append("=" + SQL.quote(outputAddress.getLockBoxNumber()) + ", ");				// 13
					sql.append(OUTPUT_LOCK_BOX_TYPE).append("=" + SQL.quote(outputAddress.getLockBoxType()) + ", ");				// 14
					sql.append(OUTPUT_MUNICIPALITY).append("=" + SQL.quote(outputAddress.getMunicipality()) + ", ");					// 15
					sql.append(OUTPUT_POSTAL_CODE).append("=" + SQL.quote(outputAddress.getPostalCode()) + ", ");					// 16
					sql.append(OUTPUT_PROVINCE).append("=" + SQL.quote(outputAddress.getProvince()) + ", ");						// 17
					sql.append(OUTPUT_RURAL_ROUTE_NUMBER).append("=" + SQL.quote(outputAddress.getRuralRouteNumber()) + ", ");			// 18
					sql.append(OUTPUT_RURAL_ROUTE_TYPE).append("=" + SQL.quote(outputAddress.getRuralRouteType()) + ", ");				// 19
					sql.append(OUTPUT_STREET_DIRECTION).append("=" + SQL.quote(outputAddress.getStreetDirection()) + ", ");				// 20
					sql.append(OUTPUT_STREET_NAME).append("=" + SQL.quote(outputAddress.getStreet()) + ", ");					// 21
					sql.append(OUTPUT_STREET_NUMBER).append("=" + outputAddress.getStreetNumber() + ", ");				// 22
					sql.append(OUTPUT_STREET_NUMBER_SUFFIX).append("=" + SQL.quote(outputAddress.getStreetNumberSuffix()) + ", ");			// 23
					sql.append(OUTPUT_STREET_TYPE).append("=" + SQL.quote(outputAddress.getStreetType()) + ", ");					// 24
					sql.append(OUTPUT_STREET_TYPE_PREFIX).append("=" + outputAddress.isStreetTypePrefix() + ", ");			// 25
					sql.append(OUTPUT_SUITE).append("=" + SQL.quote(outputAddress.getSuite()) + ", ");						// 26
					sql.append(OUTPUT_SUITE_PREFIX).append("=" + outputAddress.isSuitePrefix() + ", ");					// 27
					sql.append(OUTPUT_SUITE_TYPE).append("=" + SQL.quote(outputAddress.getSuiteType()) + ", ");					// 28
					RecordType type = outputAddress.getType();
					sql.append(OUTPUT_TYPE).append("=" + SQL.quote(type == null ? null : type.toString()) + ", ");							// 29
					sql.append(OUTPUT_UNPARSED_ADDRESS).append("=" + SQL.quote(outputAddress.getUnparsedAddressLine1()) + ", ");				// 30
					sql.append(OUTPUT_URBAN_BEFORE_RURAL).append("=" + outputAddress.isUrbanBeforeRural() + ", ");			// 31
					sql.append(OUTPUT_VALID).append("=" + result.isValid() + " ");			// 32
					
					sql.append("WHERE ");
					
					int j = 0;
					
					for (Object keyValue: result.getKeyValues()) {
						if (j > 0) {
							sql.append("AND ");
						}
						if (keyValue == null) {
							sql.append(SOURCE_ADDRESS_KEY_COLUMN_BASE).append(j).append(" is null "); // 18+
						} else if (keyValue instanceof String || keyValue instanceof Character) {
							sql.append(SOURCE_ADDRESS_KEY_COLUMN_BASE).append(j).append("=" + SQL.quote(keyValue.toString()) + " "); // 18+
						} else {
							sql.append(SOURCE_ADDRESS_KEY_COLUMN_BASE).append(j).append("=" + keyValue + " "); // 18+
						}
						j++;
					}
					
					engineLogger.debug("Preparing the following address result to be updated: " + result);
					engineLogger.error("Executing statement " + sql);
					
					engineLogger.debug("Executing update statement");
					stmt.execute(sql.toString());
					incrementProgress();
				}
				
				if (stmt != null) stmt.close();
				stmt = null;
			}
			
			if (newAddresses.size() > 0) {
				//Next, let's meke an INSERT PreparedStatement to insert new records
				sql = new StringBuilder();
				sql.append("INSERT INTO ");
				appendFullyQualifiedTableName(sql, resultTable);
				sql.append("(");
				for (int i = 0; i < keySize; i++) {
					sql.append(SOURCE_ADDRESS_KEY_COLUMN_BASE).append(i)
							.append(", ");
				}
				sql.append(INPUT_ADDRESS_LINE1).append(", ");					
				sql.append(INPUT_ADDRESS_LINE2).append(", ");					
				sql.append(INPUT_MUNICIPALITY).append(", ");					
				sql.append(INPUT_PROVINCE).append(", ");						
				sql.append(INPUT_COUNTRY).append(", ");							
				sql.append(INPUT_POSTAL_CODE).append(", ");						
				sql.append(OUTPUT_COUNTRY).append(", ");						
				sql.append(OUTPUT_DELIVERY_INSTALLATION_NAME).append(", ");	
				sql.append(OUTPUT_DELIVERY_INSTALLATION_TYPE).append(", ");		
				sql.append(OUTPUT_DIRECTION_PREFIX).append(", ");				
				sql.append(OUTPUT_FAILED_PARSING_STRING).append(", ");			
				sql.append(OUTPUT_GENERAL_DELIVERY_NAME).append(", ");			
				sql.append(OUTPUT_LOCK_BOX_NUMBER).append(", ");				
				sql.append(OUTPUT_LOCK_BOX_TYPE).append(", ");					
				sql.append(OUTPUT_MUNICIPALITY).append(", ");					
				sql.append(OUTPUT_POSTAL_CODE).append(", ");					
				sql.append(OUTPUT_PROVINCE).append(", ");						
				sql.append(OUTPUT_RURAL_ROUTE_NUMBER).append(", ");				
				sql.append(OUTPUT_RURAL_ROUTE_TYPE).append(", ");				
				sql.append(OUTPUT_STREET_DIRECTION).append(", ");				
				sql.append(OUTPUT_STREET_NAME).append(", ");					
				sql.append(OUTPUT_STREET_NUMBER).append(", ");					
				sql.append(OUTPUT_STREET_NUMBER_SUFFIX).append(", ");			
				sql.append(OUTPUT_STREET_TYPE).append(", ");			
				sql.append(OUTPUT_STREET_TYPE_PREFIX).append(", ");				
				sql.append(OUTPUT_SUITE).append(", ");							
				sql.append(OUTPUT_SUITE_PREFIX).append(", ");					
				sql.append(OUTPUT_SUITE_TYPE).append(", ");						
				sql.append(OUTPUT_TYPE).append(", ");							
				sql.append(OUTPUT_UNPARSED_ADDRESS).append(", ");				
				sql.append(OUTPUT_URBAN_BEFORE_RURAL).append(", ");		
				sql.append(OUTPUT_VALID).append(")");
				sql.append("VALUES(");
				for (int i = 0; i < keySize; i++) {
					sql.append("?, ");
				}
				sql.append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				ps = con.prepareStatement(sql.toString());
				int batchCount = 0;
				for (int i = 0; i < newAddresses.size(); i++) {
					AddressResult result = newAddresses.get(i);
					int j = 1;
					
					for (Object keyValue: result.getKeyValues()) {
						ps.setObject(j, keyValue);
						j++;
					}

					ps.setString(j, result.getInputAddress().getUnparsedAddressLine1());
					ps.setString(j + 1, result.getInputAddress().getUnparsedAddressLine2());
					ps.setString(j + 2, result.getInputAddress().getMunicipality());
					ps.setString(j + 3, result.getInputAddress().getProvince());
					ps.setString(j + 4, result.getInputAddress().getCountry());
					ps.setString(j + 5, result.getInputAddress().getPostalCode());
				
					Address outputAddress = result.getOutputAddress();
					ps.setString(j + 6, outputAddress.getSuite());
					ps.setString(j + 7, outputAddress.getDeliveryInstallationName());
					ps.setString(j + 8, outputAddress.getDeliveryInstallationType());
					ps.setBoolean(j + 9, outputAddress.isDirectionPrefix());
					ps.setString(j + 10, outputAddress.getFailedParsingString());
					ps.setString(j + 11, outputAddress.getGeneralDeliveryName());
					ps.setString(j + 12, outputAddress.getLockBoxNumber());
					ps.setString(j + 13, outputAddress.getLockBoxType());
					ps.setString(j + 14, outputAddress.getMunicipality());
					ps.setString(j + 15, outputAddress.getPostalCode());
					ps.setString(j + 16, outputAddress.getProvince());
					ps.setString(j + 17, outputAddress.getRuralRouteNumber());
					ps.setString(j + 18, outputAddress.getRuralRouteType());
					ps.setString(j + 19, outputAddress.getStreetDirection());
					ps.setString(j + 20, outputAddress.getStreet());
					Integer streetNumber = outputAddress.getStreetNumber();
					if (streetNumber == null) {
						ps.setNull(j + 21, Types.INTEGER);
					} else {
						ps.setInt(j + 21, streetNumber);
					}
					ps.setString(j + 22, outputAddress.getStreetNumberSuffix());
					ps.setString(j + 23, outputAddress.getStreetType());
					ps.setBoolean(j + 24, outputAddress.isStreetTypePrefix());
					ps.setString(j + 25, outputAddress.getSuite());
					ps.setBoolean(j + 26, outputAddress.isSuitePrefix());
					ps.setString(j + 27, outputAddress.getSuiteType());
					RecordType type = outputAddress.getType();
					ps.setString(j + 28, type == null ? null : type.toString());
					ps.setString(j + 29, outputAddress.getUnparsedAddressLine1());
					Boolean urbanBeforeRural = outputAddress.isUrbanBeforeRural();
					if (urbanBeforeRural == null) {
						ps.setNull(j + 30, Types.BOOLEAN);
					} else {
						ps.setBoolean(j + 30, outputAddress.isUrbanBeforeRural());
					}
					ps.setBoolean(j + 31, result.isValid());

					engineLogger.debug("Preparing the following address to be inserted: " + result);
					
					if (useBatchUpdates) {
						engineLogger.debug("Adding to batch");
						ps.addBatch();
						batchCount++;
						// TODO: The batchCount should be user setable
						if (batchCount > 1000) {
							engineLogger.debug("Executing batch");
							ps.executeBatch();
							batchCount = 0;
						}
					} else {
						engineLogger.debug("Executing statement");
						ps.execute();
					}
					incrementProgress();
				}
				
				// Execute remaining batch statements
				if (batchCount > 0 && useBatchUpdates) {
					ps.executeBatch();
				}
				
				if (ps != null) ps.close();
				ps = null;
			}
			
			if (debug) {
				engineLogger.debug("Rolling back changes");
				con.rollback();
			} else {
				engineLogger.debug("Committing changes");
				con.commit();
			}
			
			for (AddressResult result: addresses.values()) {
				result.markClean();
			}
		} catch (Exception ex) {
			try {
				con.rollback();
			} catch (SQLException sqlEx) {
				engineLogger.error("Error while rolling back. " +
						"Suppressing this exception to prevent it from overshadowing the orginal exception.", sqlEx);
			}
			throw new RuntimeException("Unexpected exception while storing address validation results", ex);
		} finally {
			setFinished(true);
			if (ps != null) try { ps.close(); } catch (SQLException e) { engineLogger.error("Error while closing PreparedStatement", e); }
			if (stmt != null) try { stmt.close(); } catch (SQLException e) { engineLogger.error("Error while closing Statement", e); }
			if (con != null) try { con.close(); } catch (SQLException e) { engineLogger.error("Error while closing Connection", e); }
		}
	}
	/**
	 * Returns a Collection of invalid addresses. Note that it will only return
	 * invalid addresses stored in the result table as of the last call to the
	 * {@link #load(Logger)} method, so if any new invalid addresses were added
	 * to the result table after calling {@link #load(Logger)}, then they won't
	 * appear in the Collection returned here. Furthermore, if this AddressPool
	 * was initialized without calling load, then it will not contain any
	 * invalid addresses that were stored in the result table.
	 * 
	 * @return A collection of invalid address results.
	 */
	public Collection<AddressResult> getAddressResults(Logger engineLogger)  {
		return addresses.values();
	}

	private void appendFullyQualifiedTableName(StringBuilder sql,
			SQLTable resultTable) {
		if (resultTable.getCatalogName() != null && resultTable.getCatalogName().length() > 0) {
			sql.append(resultTable.getCatalogName()).append(".");
		}
		if (resultTable.getSchemaName() != null && resultTable.getSchemaName().length() > 0) {
			sql.append(resultTable.getSchemaName()).append(".");
		}
		sql.append(resultTable.getName());
	}
	
	private int getNumRowsToProcess() throws SQLException {
		int rowCount;
		Connection con = null;
		Statement stmt = null;
		try {
			con = project.createResultTableConnection();
			stmt = con.createStatement();
			String rowCountSQL = "SELECT COUNT(*) AS ROW_COUNT FROM " + DDLUtils.toQualifiedName(project.getResultTable());
			ResultSet result = stmt.executeQuery(rowCountSQL);
			if (result.next()) {
				rowCount = result.getInt("ROW_COUNT");
			} else {
				throw new AssertionError("No rows came back from source table row count query!");
			}
		} finally {
			if (stmt != null) stmt.close();
			if (con != null) con.close();
		}
		return rowCount;
	}

	public AddressResult findAddress(List<Object> uniqueKeyValues) {
		return addresses.get(uniqueKeyValues);
	}
}
