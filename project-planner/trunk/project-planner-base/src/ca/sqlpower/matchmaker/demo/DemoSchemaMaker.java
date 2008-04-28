/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.demo;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

/**
 * A program to create a simple schema and populate with test
 * data for the purposes of demonstrating the features of the
 * Power*MatchMaker. 
 */
public class DemoSchemaMaker {

	private static final String DEMO_SCHEMA_NAME = "mm_demo";
	
	public static void main(String[] args) throws Exception {
		DataSourceCollection plini = new PlDotIni();
		plini.read(new File(System.getProperty("user.home"), "pl.ini"));
		SPDataSource ds = plini.getDataSource(DEMO_SCHEMA_NAME);
		
		Connection con = null;
		try {
			con = ds.createConnection();
			Statement stmt = con.createStatement();
			stmt.executeUpdate("CREATE TABLE " + DEMO_SCHEMA_NAME + ".CUSTOMER (" +
					"\n CUSTOMER_NO NUMBER(22,0) NOT NULL," +
					"\n CUSTOMER_TYPE VARCHAR2(20) NULL," +
					"\n SALUTATION_CODE VARCHAR2(20) NULL," +
					"\n LAST_NAME VARCHAR2(20) NULL," +
					"\n FIRST_NAME VARCHAR2(20) NULL," +
					"\n ADDRESS_LINE_1 VARCHAR2(20) NULL," +
					"\n ADDRESS_LINE_2 VARCHAR2(20) NULL," +
					"\n CITY VARCHAR2(20) NULL," +
					"\n PROVINCE VARCHAR2(20) NULL," +
					"\n COUNTRY VARCHAR2(20) NULL," +
					"\n POSTAL_CODE VARCHAR2(20) NULL," +
					"\n PHONE_NO VARCHAR2(20) NULL," +
					"\n FAX_NO VARCHAR2(20) NULL," +
					"\n MOBILE_NO VARCHAR2(20) NULL," +
					"\n EMAIL_ADDRESS VARCHAR2(20) NULL)");
			
			stmt.execute("ALTER TABLE " + DEMO_SCHEMA_NAME + ".CUSTOMER ADD CONSTRAINT SYS_C0011143" +
						" PRIMARY KEY (CUSTOMER_NO)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " +
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111000, NULL, NULL, 'Shuffler', 'Peter', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111111, NULL, NULL, 'Tsampas', 'John', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111222, NULL, NULL, 'Schmidt', 'Ted', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111333, NULL, NULL, 'Wood', 'William', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111444, NULL, NULL, 'Sanchez', 'Theresa', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111555, NULL, NULL, 'Morrison', 'John', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111666, NULL, NULL, 'Trainor', 'Terence', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111777, NULL, NULL, 'Novikoff', 'James', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111888, NULL, NULL, 'Wan', 'Kelly', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (111999, NULL, NULL, 'Sawchuk', 'Scott', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (112211, NULL, NULL, 'Mounsteven', 'Tom', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (112222, NULL, NULL, 'Stark', 'Susan', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (112244, NULL, NULL, 'Zimmerman', 'Wilburn', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
					
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (112255, NULL, NULL, 'Tomlinson', 'Torbin', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (112266, NULL, NULL, 'Treflik', 'Stephen', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (112277, NULL, NULL, 'Jurus', 'Wayne', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (112288, NULL, NULL, 'Wilson', 'Ruby', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (112299, NULL, NULL, 'Stafford', 'Tom', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (113300, NULL, NULL, 'Wells', 'Richard', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (113311, NULL, NULL, 'Grobman', 'William', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (222000, NULL, NULL, 'Owen', 'Susan', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (222111, NULL, NULL, 'Tight', 'Stacey', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (222333, NULL, NULL, 'Puddington', 'Wendy', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
					
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (222444, NULL, NULL, 'Villeneuve', 'Greg', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (222555, NULL, NULL, 'Tran', 'Ted', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (222666, NULL, NULL, 'Walman', 'Joe', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (222999, NULL, NULL, 'Silbernagel', 'Toni', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (333000, NULL, NULL, 'Wardlaw', 'Steve', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (333111, NULL, NULL, 'Vincenze', 'Jack', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (333333, NULL, NULL, 'Wong', 'Wayne', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (333444, NULL, NULL, 'Tiernay', 'Rejean', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (333555, NULL, NULL, 'Sumaher', 'Tony', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (333666, NULL, NULL, 'Vetter', 'Glenis', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (333888, NULL, NULL, 'Sivan', 'Nick', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (333999, NULL, NULL, 'Look', 'Stanley', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (444000, NULL, NULL, 'van Leeuwen', 'Roger', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
					
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (444111, NULL, NULL, 'Stephens', 'Joseph', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
					
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (444222, NULL, NULL, 'Tannous', 'SAMUEL YUK YAU', NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (444444, NULL, NULL, 'Sutherland', 'Mike', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (444555, NULL, NULL, 'Windman', 'Trungson', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (444666, NULL, NULL, 'Livingston', 'Terri', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (444777, NULL, NULL, 'Wilson', 'Mark', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (444999, NULL, NULL, 'McCarthy', 'Jerry', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555000, NULL, NULL, 'YANG', 'Patrick', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555222, NULL, NULL, 'Rybka-Becker', 'Tim', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555333, NULL, NULL, 'Watson', 'Terry', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555444, NULL, NULL, 'Reid', 'Rebecca', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555555, NULL, NULL, 'XIA', 'Ronald', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555666, NULL, NULL, 'Kilvert', 'Patrick', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555777, NULL, NULL, 'Tidd', 'Zohra', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555888, NULL, NULL, 'Langan', 'Ronald', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (555999, NULL, NULL, 'Shea', 'Greg', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (666000, NULL, NULL, 'Weir', 'Nancy', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (666111, NULL, NULL, 'Young', 'Sue', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (666222, NULL, NULL, 'WHITE', 'Pat', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (666333, NULL, NULL, 'WALTER', 'Kirk', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (666444, NULL, NULL, 'Meissner', 'John', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (666888, NULL, NULL, 'Reynolds', 'Ivan', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (666999, NULL, NULL, 'Trollop', 'THOMAS', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777000, NULL, NULL, 'Williams', 'Sandy', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777111, NULL, NULL, 'Vraets', 'Steve', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777222, NULL, NULL, 'Silman', 'TATIANA', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777333, NULL, NULL, 'Zachariah', 'Rick', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777444, NULL, NULL, 'Towers', 'Sherry', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777555, NULL, NULL, 'Sheehey', 'PETER', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777666, NULL, NULL, 'Rastogi', 'Martin', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777777, NULL, NULL, 'Thorne', 'Robyn', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777888, NULL, NULL, 'NOOIJ', 'Stephen', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (777999, NULL, NULL, 'Kramer', 'Kenneth', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (888000, NULL, NULL, 'Young', 'Martin', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (888111, NULL, NULL, 'OLIVEIRA', 'Nigel', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (888222, NULL, NULL, 'Pike', 'Sonny', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (888333, NULL, NULL, 'Hseuh', 'PETER', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (888555, NULL, NULL, 'Wilson', 'Ronald', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (888666, NULL, NULL, 'NG', 'Stephen', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (888777, NULL, NULL, 'Wallace', 'Vicky', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (888999, NULL, NULL, 'Milad', 'Peter', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (997700, NULL, NULL, 'Nashman', 'T.J. (Tony)', NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (997711, NULL, NULL, 'Weatherall', 'Ray', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (997722, NULL, NULL, 'Wynia', 'Vish', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (997733, NULL, NULL, 'Radcliffe', 'Tim', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (997744, NULL, NULL, 'Saltman', 'Ruben', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (997755, NULL, NULL, 'Steinmetz, Q.C.', 'Mike', NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (997788, NULL, NULL, 'Tang', 'Peter', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (997799, NULL, NULL, 'Sabbagh', 'Phil', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (998800, NULL, NULL, 'Van der Lugt', 'Vladimir', NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (998811, NULL, NULL, 'YU', 'Wim', NULL, NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (998822, NULL, NULL, 'Stevens', 'Walter', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (998833, NULL, NULL, 'Wightman', 'Walter', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
					
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (998844, NULL, NULL, 'Roth', 'Wayne', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999000, NULL, NULL, 'Wernham', 'Scott', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999111, NULL, NULL, 'McNabb', 'TONY', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
					
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999222, NULL, NULL, 'Pellegrino', 'Rick', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999333, NULL, NULL, 'Stephens', 'Tony', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999444, NULL, NULL, 'Wreford', 'Roger', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999555, NULL, NULL, 'McQuade', 'Robin', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999666, NULL, NULL, 'Sheikh', 'Scott', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999777, NULL, NULL, 'Myrie', 'Pierre', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999888, NULL, NULL, 'Tomasik', 'Roy', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
					
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999911, NULL, NULL, 'Rogers', 'Wayne', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999922, NULL, NULL, 'Kroen', 'Linda', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999933, NULL, NULL, 'Shanley', 'Jenny', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999944, NULL, NULL, 'Rideout', 'Steve', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999955, NULL, NULL, 'Wickens', 'R. Randall', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999966, NULL, NULL, 'Sawada', 'Peter', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999977, NULL, NULL, 'Munnings', 'Stan', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999988, NULL, NULL, 'Vasey', 'Steve', NULL, NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL)");
			
			stmt.execute("INSERT INTO customer (customer_no, customer_type, salutation_code, " + 
					" last_name, first_name, address_line_1, address_line_2, city, province, " +
					" country, postal_code, phone_no, fax_no, mobile_no, email_address) " +
					" VALUES (999999, NULL, NULL, 'Russill', 'Patricia', NULL, NULL, NULL, " +
					" NULL, NULL, NULL, NULL, NULL, NULL, NULL)");
		} finally {
			if (con != null) con.close();
		}
		
	}
}
