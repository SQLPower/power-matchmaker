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
	private static final String OUTPUT_STREET_NUMBER_SUFFIX_SEPARATE= "output_street_number_suffix_separate";
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
		SQLTable t = new SQLTable(resultTable.getSQLParent(), resultTable.getName(), resultTable.getRemarks(), "TABLE", true);
		
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
		SQLColumn outputStreetNumberSuffixSeparate = new SQLColumn(t, OUTPUT_STREET_NUMBER_SUFFIX_SEPARATE, Types.BOOLEAN, 1, 0);
		t.addColumn(outputStreetNumberSuffixSeparate);
		SQLColumn outputStreetType = new SQLColumn(t, OUTPUT_STREET_TYPE, Types.VARCHAR, 11, 0);
		t.addColumn(outputStreetType);
		SQLColumn outputStreetTypePrefix = new SQLColumn(t, OUTPUT_STREET_TYPE_PREFIX, Types.BOOLEAN, 1, 0);
		t.addColumn(outputStreetTypePrefix);
		SQLColumn outputSuite = new SQLColumn(t, OUTPUT_SUITE, Types.VARCHAR, 6, 0);
		t.addColumn(outputSuite);
		SQLColumn outputSuitePrefix = new SQLColumn(t, OUTPUT_SUITE_PREFIX, Types.BOOLEAN, 1, 0);
		t.addColumn(outputSuitePrefix);
		SQLColumn outputSuiteType = new SQLColumn(t, OUTPUT_SUITE_TYPE, Types.VARCHAR, 15, 0);
		t.addColumn(outputSuiteType);
		SQLColumn outputUnparsedAddress = new SQLColumn(t, OUTPUT_UNPARSED_ADDRESS, Types.VARCHAR, 150, 0);
		t.addColumn(outputUnparsedAddress);
		SQLColumn outputType = new SQLColumn(t, OUTPUT_TYPE, Types.VARCHAR, 20, 0);
		t.addColumn(outputType);
		SQLColumn outputUrbanBeforeRural = new SQLColumn(t, OUTPUT_URBAN_BEFORE_RURAL, Types.BOOLEAN, 1, 0);
		t.addColumn(outputUrbanBeforeRural);
		SQLColumn valid = new SQLColumn(t, OUTPUT_VALID, Types.BOOLEAN, 1, 0);
		t.addColumn(valid);
		
		SQLIndex newidx = new SQLIndex(t.getName()+"_uniq", true, null, null, null);
		for (int i = 0; i < si.getChildCount(); i++) {
			newidx.addChild(new Column(t.getColumn(i), AscendDescend.ASCENDING));
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
				// Transformations. Otherwise, when we cannot properly compare
				// the key values of these loaded. Addresses with the ones
				// coming through the transformations.
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
				address.setStreetNumberSuffix(rs.getString(OUTPUT_STREET_NUMBER_SUFFIX_SEPARATE));
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
		List<AddressResult> deleteAddresses = new ArrayList<AddressResult>();
		List<AddressResult> newAddresses = new ArrayList<AddressResult>();
		
		for (List<Object> key: addresses.keySet()) {
			AddressResult result = addresses.get(key);
			if (result.getStorageState() == StorageState.DELETE) {
				deleteAddresses.add(result);
			} else if (result.getStorageState() == StorageState.DIRTY) {
				dirtyAddresses.add(result);
			} else if (result.getStorageState() == StorageState.NEW) {
				newAddresses.add(result);
			}

		}

		setJobSize(deleteAddresses.size() + dirtyAddresses.size() + newAddresses.size());
		
		engineLogger.debug("# of Delete Address Records:" + deleteAddresses.size());
		engineLogger.debug("# of Dirty Address Records:" + dirtyAddresses.size());
		engineLogger.debug("# of New Address Records:" + newAddresses.size());
		
		Connection con = null;
		PreparedStatement ps = null;
		Statement stmt = null;
		StringBuilder sql = null;
		AddressResult result = null;
		
		try {
			con = project.createResultTableConnection();
			con.setAutoCommit(false);
			boolean useBatchUpdates = useBatchExecute && con.getMetaData().supportsBatchUpdates();
			SQLTable resultTable = project.getResultTable();
			int keySize = project.getSourceTableIndex().getChildCount();

			if (deleteAddresses.size() > 0) {
				stmt = con.createStatement();
				
				for (AddressResult currentResult : deleteAddresses) {
					sql = new StringBuilder("DELETE FROM ");
					appendFullyQualifiedTableName(sql, resultTable);
					sql.append(" WHERE ");
					
					int j = 0;
					for (Object keyValue: currentResult.getKeyValues()) {
						if (j > 0) {
							sql.append("AND ");
						}
						if (keyValue == null) {
							sql.append(SOURCE_ADDRESS_KEY_COLUMN_BASE).append(j).append(" is null ");
						} else if (keyValue instanceof String || keyValue instanceof Character) {
							sql.append(SOURCE_ADDRESS_KEY_COLUMN_BASE).append(j).append("=" + SQL.quote(keyValue.toString()) + " ");
						} else {
							sql.append(SOURCE_ADDRESS_KEY_COLUMN_BASE).append(j).append("=" + keyValue + " ");
						}
						j++;
					}
					
					engineLogger.debug("Preparing the following address result to be deleted: " + currentResult);
					engineLogger.debug("Executing statement " + sql);
					
					stmt.execute(sql.toString());
					incrementProgress();
				}
				
				if (stmt != null) stmt.close();
				stmt = null;
			}
			
			if (dirtyAddresses.size() > 0) {
				//First, create and UPDATE PreparedStatement to update dirty records
				sql = new StringBuilder();
				sql.append("UPDATE ");
				appendFullyQualifiedTableName(sql, resultTable);
				sql.append(" SET ");
				sql.append(INPUT_ADDRESS_LINE1).append("=?, ");					// 1
				sql.append(INPUT_ADDRESS_LINE2).append("=?, ");					// 2
				sql.append(INPUT_MUNICIPALITY).append("=?, ");					// 3
				sql.append(INPUT_PROVINCE).append("=?, ");						// 4
				sql.append(INPUT_COUNTRY).append("=?, ");						// 5
				sql.append(INPUT_POSTAL_CODE).append("=?, ");					// 6
				sql.append(OUTPUT_COUNTRY).append("=?, ");						// 7
				sql.append(OUTPUT_DELIVERY_INSTALLATION_NAME).append("=?, ");	// 8
				sql.append(OUTPUT_DELIVERY_INSTALLATION_TYPE).append("=?, ");	// 9			
				sql.append(OUTPUT_DIRECTION_PREFIX).append("=?, ");			// 10
				sql.append(OUTPUT_FAILED_PARSING_STRING).append("=?, ");		// 11
				sql.append(OUTPUT_GENERAL_DELIVERY_NAME).append("=?, ");		// 12
				sql.append(OUTPUT_LOCK_BOX_NUMBER).append("=?, ");				// 13
				sql.append(OUTPUT_LOCK_BOX_TYPE).append("=?, ");				// 14
				sql.append(OUTPUT_MUNICIPALITY).append("=?, ");					// 15
				sql.append(OUTPUT_POSTAL_CODE).append("=?, ");					// 16
				sql.append(OUTPUT_PROVINCE).append("=?, ");						// 17
				sql.append(OUTPUT_RURAL_ROUTE_NUMBER).append("=?, ");			// 18
				sql.append(OUTPUT_RURAL_ROUTE_TYPE).append("=?, ");				// 19
				sql.append(OUTPUT_STREET_DIRECTION).append("=?, ");				// 20
				sql.append(OUTPUT_STREET_NAME).append("=?, ");					// 21
				sql.append(OUTPUT_STREET_NUMBER).append("=?, ");				// 22
				sql.append(OUTPUT_STREET_NUMBER_SUFFIX).append("=?, ");			// 23
				sql.append(OUTPUT_STREET_NUMBER_SUFFIX_SEPARATE).append("=?, ");			// 23.5
				sql.append(OUTPUT_STREET_TYPE).append("=?, ");					// 24
				sql.append(OUTPUT_STREET_TYPE_PREFIX).append("=?, ");			// 25
				sql.append(OUTPUT_SUITE).append("=?, ");						// 26
				sql.append(OUTPUT_SUITE_PREFIX).append("=?, ");				// 27
				sql.append(OUTPUT_SUITE_TYPE).append("=?, ");					// 28
				sql.append(OUTPUT_TYPE).append("=?, ");							// 29
				sql.append(OUTPUT_UNPARSED_ADDRESS).append("=?, ");				// 30
				sql.append(OUTPUT_URBAN_BEFORE_RURAL).append("=?, ");			// 31
				sql.append(OUTPUT_VALID).append("=? ");			// 32
				sql.append("WHERE ");
				
				String baseStatement = sql.toString();
				
				int batchCount = 0;
				for (int i = 0; i < dirtyAddresses.size(); i++) {
					
					sql = new StringBuilder(baseStatement);
					result = dirtyAddresses.get(i);
					int j = 0;
					
					// I really wish there was a better way to handle this,
					// but unfortunately in SQL, <column> = null and <column> is
					// null are not the same thing, and you usually want 'is
					// null' Why they couldn't just use '= null' is beyond me.
					// Otherwise, we could just use a single prepared statement
					// for all the records. The main reason we had to switch
					// back to using prepared statements is because different RDBMS
					// platforms handle Booleans differently (some support
					// boolean explicitly, others use an integer (1 or 0)
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
					
					ps = con.prepareStatement(sql.toString());
					
					engineLogger.debug("Setting input unparsed address line 1 to " + result.getInputAddress().getUnparsedAddressLine1());
					ps.setString(1, result.getInputAddress().getUnparsedAddressLine1());
					engineLogger.debug("Setting input unparsed address line 2 to " + result.getInputAddress().getUnparsedAddressLine2());
					ps.setString(2, result.getInputAddress().getUnparsedAddressLine2());
					engineLogger.debug("Setting input municipality to " + result.getInputAddress().getMunicipality());
					ps.setString(3, result.getInputAddress().getMunicipality());
					engineLogger.debug("Setting input province to " + result.getInputAddress().getProvince());
					ps.setString(4, result.getInputAddress().getProvince());
					engineLogger.debug("Setting input country to " + result.getInputAddress().getCountry());
					ps.setString(5, result.getInputAddress().getCountry());
					engineLogger.debug("Setting input postal code to " + result.getInputAddress().getPostalCode());
					ps.setString(6, result.getInputAddress().getPostalCode());
				
					Address outputAddress = result.getOutputAddress();
					engineLogger.debug("Setting output suite to " + outputAddress.getSuite());
					ps.setString(7, outputAddress.getSuite());
					engineLogger.debug("Setting output delivery installation name to " + outputAddress.getDeliveryInstallationName());
					ps.setString(8, outputAddress.getDeliveryInstallationName());
					engineLogger.debug("Setting output delivery nstallation type to " + outputAddress.getDeliveryInstallationType());
					ps.setString(9, outputAddress.getDeliveryInstallationType());
					engineLogger.debug("Setting output direction prefix to " + outputAddress.isDirectionPrefix());
					ps.setBoolean(10, outputAddress.isDirectionPrefix());
					engineLogger.debug("Setting output failed parsing string to " + outputAddress.getFailedParsingString());
					ps.setString(11, outputAddress.getFailedParsingString());
					engineLogger.debug("Setting output general delivery name to " + outputAddress.getGeneralDeliveryName());
					ps.setString(12, outputAddress.getGeneralDeliveryName());
					engineLogger.debug("Setting output lock box number to " + outputAddress.getLockBoxNumber());
					ps.setString(13, outputAddress.getLockBoxNumber());
					engineLogger.debug("Setting output lock box type to " + outputAddress.getLockBoxType());
					ps.setString(14, outputAddress.getLockBoxType());
					engineLogger.debug("Setting output municipality to " + outputAddress.getMunicipality());
					ps.setString(15, outputAddress.getMunicipality());
					engineLogger.debug("Setting output postal code to " + outputAddress.getPostalCode());
					ps.setString(16, outputAddress.getPostalCode());
					engineLogger.debug("Setting output province to " + outputAddress.getProvince());
					ps.setString(17, outputAddress.getProvince());
					engineLogger.debug("Setting output rural route number to " + outputAddress.getRuralRouteNumber());
					ps.setString(18, outputAddress.getRuralRouteNumber());
					engineLogger.debug("Setting output rural route type to " + outputAddress.getRuralRouteType());
					ps.setString(19, outputAddress.getRuralRouteType());
					engineLogger.debug("Setting output street direciton to " + outputAddress.getStreetDirection());
					ps.setString(20, outputAddress.getStreetDirection());
					engineLogger.debug("Setting output street to " + outputAddress.getStreet());
					ps.setString(21, outputAddress.getStreet());
					engineLogger.debug("Setting output street number to " + outputAddress.getStreetNumber());
					Integer streetNumber = outputAddress.getStreetNumber();
					if (streetNumber == null) {
						ps.setNull(22, Types.INTEGER);
					} else {
						ps.setInt(22, streetNumber);
					}
					engineLogger.debug("Setting output street number suffix to " + outputAddress.getStreetNumberSuffix());
					ps.setString(23, outputAddress.getStreetNumberSuffix());
					engineLogger.debug("Setting output street number suffix separate to " + outputAddress.isStreetNumberSuffixSeparate());
					Boolean isStreetNumberSuffixSeparate = outputAddress.isStreetNumberSuffixSeparate();
					if (isStreetNumberSuffixSeparate == null) {
						ps.setNull(24, Types.BOOLEAN);
					} else {
						ps.setBoolean(24, outputAddress.isStreetNumberSuffixSeparate());
					}
					engineLogger.debug("Setting output suite to " + outputAddress.getSuite());
					ps.setString(25, outputAddress.getStreetType());
					engineLogger.debug("Setting output streetTypePrefix to " + outputAddress.isStreetTypePrefix());
					ps.setBoolean(26, outputAddress.isStreetTypePrefix());
					engineLogger.debug("Setting output suite to " + outputAddress.getSuite());
					ps.setString(27, outputAddress.getSuite());
					engineLogger.debug("Setting output suitePrefix to " + outputAddress.isSuitePrefix());
					ps.setBoolean(28, outputAddress.isSuitePrefix());
					engineLogger.debug("Setting output suiteType to " + outputAddress.getSuiteType());
					ps.setString(29, outputAddress.getSuiteType());
					engineLogger.debug("Setting output type to " + outputAddress.getType());
					RecordType type = outputAddress.getType();
					ps.setString(30, type == null ? null : type.toString());
					engineLogger.debug("Setting output unparsedAddressLine1 to " + outputAddress.getUnparsedAddressLine1());
					ps.setString(31, outputAddress.getUnparsedAddressLine1());
					engineLogger.debug("Setting output urbanBeforeRural to " + outputAddress.isUrbanBeforeRural());
					Boolean urbanBeforeRural = outputAddress.isUrbanBeforeRural();
					if (urbanBeforeRural == null) {
						ps.setNull(32, Types.BOOLEAN);
					} else {
						ps.setBoolean(32, outputAddress.isUrbanBeforeRural());
					}
					engineLogger.debug("Setting valid to " + result.isValid());
					ps.setBoolean(33, result.isValid());
										
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
				sql.append(OUTPUT_STREET_NUMBER_SUFFIX_SEPARATE).append(", ");
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
				sql.append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				ps = con.prepareStatement(sql.toString());
				int batchCount = 0;
				for (int i = 0; i < newAddresses.size(); i++) {
					result = newAddresses.get(i);
					int j = 1;
					
					for (Object keyValue: result.getKeyValues()) {
						ps.setObject(j, keyValue);
						j++;
						engineLogger.debug("Setting key value " + j + " to " + keyValue);
					}
					
					Address inputAddress = result.getInputAddress();
					engineLogger.debug("Setting input unparsed address line 1 to " + inputAddress.getUnparsedAddressLine1());
					ps.setString(j, inputAddress.getUnparsedAddressLine1());
					engineLogger.debug("Setting input unparsed address line 2 to " + inputAddress.getUnparsedAddressLine2());
					ps.setString(j + 1, inputAddress.getUnparsedAddressLine2());
					engineLogger.debug("Setting input municipality to " + inputAddress.getMunicipality());
					ps.setString(j + 2, inputAddress.getMunicipality());
					engineLogger.debug("Setting input province to " + inputAddress.getProvince());
					ps.setString(j + 3, inputAddress.getProvince());
					engineLogger.debug("Setting input country to " + inputAddress.getCountry());
					ps.setString(j + 4, inputAddress.getCountry());
					engineLogger.debug("Setting input postal code to " + inputAddress.getPostalCode());
					ps.setString(j + 5, inputAddress.getPostalCode());
				
					Address outputAddress = result.getOutputAddress();
					engineLogger.debug("Setting output suite to " + outputAddress.getSuite());
					ps.setString(j + 6, outputAddress.getSuite());
					engineLogger.debug("Setting output delivery installation name to " + outputAddress.getDeliveryInstallationName());
					ps.setString(j + 7, outputAddress.getDeliveryInstallationName());
					engineLogger.debug("Setting output delivery nstallation type to " + outputAddress.getDeliveryInstallationType());
					ps.setString(j + 8, outputAddress.getDeliveryInstallationType());
					engineLogger.debug("Setting output direction prefix to " + outputAddress.isDirectionPrefix());
					ps.setBoolean(j + 9, outputAddress.isDirectionPrefix());
					engineLogger.debug("Setting output failed parsing string to " + outputAddress.getFailedParsingString());
					ps.setString(j + 10, outputAddress.getFailedParsingString());
					engineLogger.debug("Setting output general delivery name to " + outputAddress.getGeneralDeliveryName());
					ps.setString(j + 11, outputAddress.getGeneralDeliveryName());
					engineLogger.debug("Setting output lock box number to " + outputAddress.getLockBoxNumber());
					ps.setString(j + 12, outputAddress.getLockBoxNumber());
					engineLogger.debug("Setting output lock box type to " + outputAddress.getLockBoxType());
					ps.setString(j + 13, outputAddress.getLockBoxType());
					engineLogger.debug("Setting output municipality to " + outputAddress.getMunicipality());
					ps.setString(j + 14, outputAddress.getMunicipality());
					engineLogger.debug("Setting output postal code to " + outputAddress.getPostalCode());
					ps.setString(j + 15, outputAddress.getPostalCode());
					engineLogger.debug("Setting output province to " + outputAddress.getProvince());
					ps.setString(j + 16, outputAddress.getProvince());
					engineLogger.debug("Setting output rural route number to " + outputAddress.getRuralRouteNumber());
					ps.setString(j + 17, outputAddress.getRuralRouteNumber());
					engineLogger.debug("Setting output rural route type to " + outputAddress.getRuralRouteType());
					ps.setString(j + 18, outputAddress.getRuralRouteType());
					engineLogger.debug("Setting output street direciton to " + outputAddress.getStreetDirection());
					ps.setString(j + 19, outputAddress.getStreetDirection());
					engineLogger.debug("Setting output street to " + outputAddress.getStreet());
					ps.setString(j + 20, outputAddress.getStreet());
					engineLogger.debug("Setting output street number to " + outputAddress.getStreetNumber());
					Integer streetNumber = outputAddress.getStreetNumber();
					if (streetNumber == null) {
						ps.setNull(j + 21, Types.INTEGER);
					} else {
						ps.setInt(j + 21, streetNumber);
					}
					engineLogger.debug("Setting output street number suffix to " + outputAddress.getStreetNumberSuffix());
					ps.setString(j + 22, outputAddress.getStreetNumberSuffix());
					engineLogger.debug("Setting output street number suffix separate to " + outputAddress.isStreetNumberSuffixSeparate());
					Boolean isStreetNumberSuffixSeparate = outputAddress.isStreetNumberSuffixSeparate();
					if (isStreetNumberSuffixSeparate == null) {
						ps.setNull(j + 23, Types.BOOLEAN);
					} else {
						ps.setBoolean(j + 23, outputAddress.isStreetNumberSuffixSeparate());
					}
					engineLogger.debug("Setting output suite to " + outputAddress.getSuite());
					ps.setString(j + 24, outputAddress.getStreetType());
					engineLogger.debug("Setting output streetTypePrefix to " + outputAddress.isStreetTypePrefix());
					ps.setBoolean(j + 25, outputAddress.isStreetTypePrefix());
					engineLogger.debug("Setting output suite to " + outputAddress.getSuite());
					ps.setString(j + 26, outputAddress.getSuite());
					engineLogger.debug("Setting output suitePrefix to " + outputAddress.isSuitePrefix());
					ps.setBoolean(j + 27, outputAddress.isSuitePrefix());
					engineLogger.debug("Setting output suiteType to " + outputAddress.getSuiteType());
					ps.setString(j + 28, outputAddress.getSuiteType());
					engineLogger.debug("Setting output type to " + outputAddress.getType());
					RecordType type = outputAddress.getType();
					ps.setString(j + 29, type == null ? null : type.toString());
					engineLogger.debug("Setting output unparsedAddressLine1 to " + outputAddress.getUnparsedAddressLine1());
					ps.setString(j + 30, outputAddress.getUnparsedAddressLine1());
					engineLogger.debug("Setting output urbanBeforeRural to " + outputAddress.isUrbanBeforeRural());
					Boolean urbanBeforeRural = outputAddress.isUrbanBeforeRural();
					if (urbanBeforeRural == null) {
						ps.setNull(j + 31, Types.BOOLEAN);
					} else {
						ps.setBoolean(j + 31, outputAddress.isUrbanBeforeRural());
					}
					engineLogger.debug("Setting valid to " + result.isValid());
					ps.setBoolean(j + 32, result.isValid());

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
			
			for (AddressResult ar: addresses.values()) {
				ar.markClean();
			}
		} catch (Exception ex) {
			try {
				con.rollback();
			} catch (SQLException sqlEx) {
				engineLogger.error("Error while rolling back. " +
						"Suppressing this exception to prevent it from overshadowing the orginal exception.", sqlEx);
			}
			throw new RuntimeException(
					"Unexpected exception while storing address validation results.\n" +
					"SQL statement: " + ((sql == null) ? "null" : sql.toString()) + "\n" +
					"Current result: " +
							((result == null) ? "null" :
							"Input Address:\n" + result.getInputAddress() + "\n" +
							"Output Address:\n" + result.getOutputAddress()), ex);
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

	/**
	 * Returns an AddressResult with the given list of unique key values. If one
	 * cannot be found, it returns null.
	 * 
	 * @param uniqueKeyValues
	 *            A {@link List} of unique key values that identify the
	 *            {@link AddressResult} to be returned
	 * @return An AddressResult with the given list of unique key values. If one
	 *         cannot be found, it returns null.
	 */
	public AddressResult findAddress(List<Object> uniqueKeyValues) {
		return addresses.get(uniqueKeyValues);
	}

	/**
	 * Tries to find an AddressResult with the given unique key values. If it
	 * can find one, it then calls {@link AddressResult#markDelete()} on it. On
	 * the next call to {@link AddressPool#store(Logger, boolean, boolean)},
	 * that {@link AddressResult} will be deleted from the persistent storage.
	 * If one cannot be found, then nothing further is done.
	 * 
	 * @param uniqueKeyValues
	 *            A {@link List} of unique key values that identify the
	 *            AddressResult to be marked for deletion.
	 */
	public void markAddressForDeletion(List<Object> uniqueKeyValues) {
		AddressResult result = addresses.get(uniqueKeyValues);
		if (result != null) {
			result.markDelete();
		}
	}
}
