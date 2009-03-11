grammar Address;

@header {
package ca.sqlpower.matchmaker.address.parse;

import ca.sqlpower.matchmaker.address.*;
}

@lexer::header {
package ca.sqlpower.matchmaker.address.parse;
}

@members {

private PostalCode postalCode;

private AddressDatabase addressDatabase;

private Address address = new Address();

/**
 * This may be moved into a local variable later
 */
 private boolean hasStreetNameStarted = false;

public Address getAddress() { return address; }

public void setAddressDatabase(AddressDatabase addressDatabase) {
   this.addressDatabase = addressDatabase;
}

public void setPostalCode(String postalCodeString) throws DatabaseException {
   if (addressDatabase == null) throw new NullPointerException("No address database!");
   postalCode = addressDatabase.findPostalCode(postalCodeString);
}

private boolean couldBeUrban() {
   if (postalCode == null) return true;
   return postalCode.getRecordType() == PostalCode.RecordType.STREET || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
}

private boolean couldBeRural() {
   if (postalCode == null) return true;
   return postalCode.getRecordType() == PostalCode.RecordType.ROUTE || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
}

private boolean couldBeLockBox() {
   if (postalCode == null) return true;
   return postalCode.getRecordType() == PostalCode.RecordType.LOCK_BOX;
}

private boolean couldBeGD() {
   if (postalCode == null) return true;
   return postalCode.getRecordType() == PostalCode.RecordType.GENERAL_DELIVERY;
}

/**
 * Parses an integer value from a string, failing silently and returning
 * null if the value is not parseable.
 */
private Integer quietIntParse(String s) {
  try {
    return Integer.valueOf(s);
  } catch (NumberFormatException ex) {
    return null;
  }
}

private void appendStreetName(String name) {
   if (address.getStreet() != null && address.getStreet().trim().length() > 0) {
      address.setStreet(address.getStreet() + " " + name);
   } else {
      address.setStreet(name);
   }
}

public String getErrorMessage(RecognitionException e, 
String[] tokenNames) 
{ 
List stack = getRuleInvocationStack(e, this.getClass().getName()); 
String msg = null; 
if ( e instanceof NoViableAltException ) { 
NoViableAltException nvae = (NoViableAltException)e; 
msg = " no viable alt; token="+e.token+ 
" (decision="+nvae.decisionNumber+ 
" state "+nvae.stateNumber+")"+ 
" decision=<<"+nvae.grammarDecisionDescription+">>"; 
} 
else { 
msg = super.getErrorMessage(e, tokenNames); 
} 
return stack+" "+msg; 
} 
public String getTokenErrorDisplay(Token t) { 
return t.toString(); 
} 
}


address
	:	{couldBeUrban()}?=> streetAddress	
	|	{couldBeRural()}?=> ruralRouteAddress
	|	{couldBeLockBox()}?=> lockBoxAddress
	|	{couldBeGD()}?=> generalDeliveryAddress	
	|	failedParse				//Default to keep address information if all else fails
	;
	
failedParse
	:	failedToken*
	;

failedToken
	:	n=(ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME)
							{
							 address.setFailedParsingString(address.getFailedParsingString() + n);
							}
	;
	
streetAddress	
	:	sn=NUMBER '-' street			{ 
							  address.setSuitePrefix(true);
							  address.setSuite($sn.text);
							  address.setType(Address.Type.URBAN);
							}
	|	street s=SUITE sn=NUMBER		{ 
							  address.setSuitePrefix(false);
							  address.setSuiteType($s.text);
							  address.setSuite($sn.text);
							  address.setType(Address.Type.URBAN);
							}
	|	street					{address.setType(Address.Type.URBAN);}
	;
	
street
	:	n=NUMBER s=(STREETNUMSUFFIX|SUFFIXANDDIR) streetToken+	
							{address.setStreetNumber(quietIntParse($n.text));
							 address.setStreetNumberSuffix($s.text);
							}
	|	n=NUMANDSUFFIX streetToken+		{String streetNum = $n.text;
							 address.setStreetNumber(quietIntParse(streetNum.substring(0, streetNum.length() - 1)));
							 address.setStreetNumberSuffix(streetNum.substring(streetNum.length() - 1, streetNum.length()));
							}
	|	n=NUMBER streetToken+			{address.setStreetNumber(quietIntParse($n.text));}
	;
	
streetToken
	:	{hasStreetNameStarted}? d=(STREETDIR|SUFFIXANDDIR)	
							{
							 address.setStreetDirection($d.text);
							}
							
	|	{!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText())}? t=NAME
							{
							 if (address.getStreetType() != null) {
							    appendStreetName(address.getStreetType());
							 }
							 address.setStreetTypePrefix(!hasStreetNameStarted);
							 address.setStreetType($t.text);
							}
							
	|	n=(NAME|NUMBER|NUMANDSUFFIX)		{
							 if (!address.isStreetTypePrefix() && address.getStreetType() != null) {
							    appendStreetName(address.getStreetType());
							    address.setStreetType(null);
							 }
							 
							 hasStreetNameStarted = true;
							 appendStreetName($n.text);
							}
	;
	
ruralRouteAddress
	:	rs=ROUTESERVICETYPE n=NUMBER? di=DITYPE? stn=NAME?
							{
							 address.setRuralRouteType($rs.text);
							 address.setRuralRouteNumber(quietIntParse($rs.text));
							 address.setDeliveryInstallationType($di.text);
							 address.setDeliveryInstallationName($stn.text);
							 address.setType(Address.Type.RURAL);
							}
	|	rs=ROUTESERVICETYPE n=NUMBER street	{
							 address.setRuralRouteType($rs.text);
							 address.setRuralRouteNumber(quietIntParse($rs.text));
							 address.setType(Address.Type.RURAL);
							}
	;
	
lockBoxAddress
	:	lb=LOCKBOXTYPE n=NUMBER di=DITYPE? stn=NAME?
							{
							 address.setLockBoxType($lb.text);
							 address.setLockBoxNumber(quietIntParse($n.text));
							 address.setDeliveryInstallationType($di.text);
							 address.setDeliveryInstallationName($stn.text);
							 address.setType(Address.Type.LOCK_BOX);
							}
	;
	
generalDeliveryAddress
	:	gd=GD t=DITYPE? n=NAME?			{
							 address.setGeneralDeliveryName($gd.text);
							 address.setDeliveryInstallationType($t.text);
							 address.setDeliveryInstallationName($n.text);
							 address.setType(Address.Type.GD);
							}
	;
	
ROUTESERVICETYPE
	:	'RR' | 'SS' | 'MR';

LOCKBOXTYPE
	:	'PO BOX' | 'CP' ;

GD
	:	'GD' | 'GENERAL DELIVERY' | 'PR' ;

DITYPE
	:	'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC';

SUITE	:	'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE';

SUFFIXANDDIR
	:	'N' | 'S' | 'E' | 'W'; //Needed because STREETNUMSUFFIX would take the directions from STREETDIR
	
STREETNUMSUFFIX 
	:	('A'..'Z' | '1'..'3');

STREETDIR
	:	'NE' | 'NW' | 'NO'
	|	'SE' | 'SW' | 'SO';
	
NUMANDSUFFIX
	:	('0'..'9')+ ('A'..'Z');
	
NUMBER
	:	'0'..'9'+;
	
NAME	:	('A'..'Z' | '0'..'9' | '\'' | '-')+;
		/* TODO: allow multiple words (spaces!) */
	
WS	:	(' ' | '\t')+ {skip();};
