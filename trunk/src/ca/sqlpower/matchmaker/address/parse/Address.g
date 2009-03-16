grammar Address;

@header {
package ca.sqlpower.matchmaker.address.parse;

import ca.sqlpower.matchmaker.address.*;
}

@lexer::header {
package ca.sqlpower.matchmaker.address.parse;
}

@members {

/**
 *  This is an odd tri state boolean. It will be null if the address starts as
 *  anything but an urban or rural address. It will be true if it starts as an
 *  urban address and false if it starts as a rural address. Only urban and rural
 *  addresses should care about this as it is used to distinguish between rural,
 *  urban, and mixed addresses. The fun part is mixed addresses can start as urban
 *  or rural and some parts from one could possiblly come up in the other.
 */
private Boolean startsUrbanNotRural = null;

private Set<PostalCode> postalCodes;

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
   postalCodes = addressDatabase.findPostalCode(postalCodeString);
}

private boolean couldBeUrban() {
   if (postalCodes.isEmpty()) return true;
   boolean isUrbanType = false;
   for (PostalCode postalCode : postalCodes) {
      isUrbanType |= postalCode.getRecordType() == PostalCode.RecordType.STREET || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
   }
   return isUrbanType;
}

private boolean couldBeRural() {
   if (postalCodes.isEmpty()) return true;
   boolean isRuralType = false;
   for (PostalCode postalCode : postalCodes) {
      isRuralType |= postalCode.getRecordType() == PostalCode.RecordType.ROUTE || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
   }
   return isRuralType;
}

private boolean couldBeLockBox() {
   if (postalCodes.isEmpty()) return true;
   boolean isLockBoxType = false;
   for (PostalCode postalCode : postalCodes) {
      isLockBoxType |= postalCode.getRecordType() == PostalCode.RecordType.LOCK_BOX;
   }
   return isLockBoxType;
}

private boolean couldBeGD() {
   if (postalCodes.isEmpty()) return true;
   boolean isGDType = false;
   for (PostalCode postalCode : postalCodes) {
      isGDType |= postalCode.getRecordType() == PostalCode.RecordType.GENERAL_DELIVERY;
   }
   return isGDType;
}

/**
 * Parses an integer value from a string, failing silently and returning
 * null if the value is not parseable.
 */
private Integer quietIntParse(String s) {
  if (s == null) return null;
  try {
    return Integer.valueOf(s);
  } catch (NumberFormatException ex) {
    if (s.charAt(0) == '#') {
      try {
        return Integer.valueOf(s.substring(1, s.length()));
      } catch (NumberFormatException ex1) {
        //return default value
      }
    }
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

//XXX: might be able to use a variable in some kind of global scope
public boolean setStartsUrbanNotRural(boolean b) {
  startsUrbanNotRural = b;
  return true;
}

}


address
	:	{couldBeUrban()}?=> streetAddressStart	
	|	{couldBeRural()}?=> ruralRouteAddress
	|	{couldBeLockBox()}?=> lockBoxAddress
	|	{couldBeGD()}?=> generalDeliveryAddress	
	|	failedParse				//Default to keep address information if all else fails
	;
	
failedParse
	:	failedToken*
	;

failedToken
	:	n=(STREETNUMSUFFIX | NUMANDSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME)
							{
							 address.setFailedParsingString(address.getFailedParsingString() + n);
							}
	;
streetAddressStart
	:	{setStartsUrbanNotRural(true)}? streetAddress				
							{
							  address.setType(Address.Type.URBAN);
							  if (address.isUrbanBeforeRural() != null) {
							    address.setType(Address.Type.MIXED);
							  }
							}
	;
	
streetAddress	
	:	sn=(NUMBER|NUMANDSTREETSUFFIX) '-' street			
							{ 
							  address.setSuitePrefix(true);
							  address.setSuite($sn.text);

							}
	|	street
	;
	
street
	:	n=SUITEANDSTREETNUM s=(STREETNUMSUFFIX|NUMANDSTREETSUFFIX) streetToken+
							{String[] numbers = $n.text.split("-");
							 address.setSuitePrefix(true);
							 address.setSuite(numbers[0]);
							 address.setStreetNumber(quietIntParse(numbers[1]));
							 address.setStreetNumberSuffix($s.text);
							}
	|	n=(NUMBER|NUMANDSTREETSUFFIX) s=(STREETNUMSUFFIX|NUMANDSTREETSUFFIX) streetToken+	
							{address.setStreetNumber(quietIntParse($n.text));
							 address.setStreetNumberSuffix($s.text);
							}
	|	n=NUMANDSUFFIX streetToken+		{String streetNum = $n.text;
							 address.setStreetNumber(quietIntParse(streetNum.substring(0, streetNum.length() - 1)));
							 address.setStreetNumberSuffix(streetNum.substring(streetNum.length() - 1, streetNum.length()));
							}
	|	n=(NUMBER|NUMANDSTREETSUFFIX) streetToken+			
							{address.setStreetNumber(quietIntParse($n.text));}
	;
	
streetToken
	:	{Address.isSuiteType(input.LT(1).getText())}?=> s=NAME sn=(NUMBER|NUMANDSTREETSUFFIX)
							{
							 address.setSuitePrefix(false);
							 address.setSuiteType($s.text);
							 address.setSuite($sn.text);
							}
	|	{hasStreetNameStarted && address.isStreetDirection(input.LT(1).getText())}? d=(NAME|STREETNUMSUFFIX)	
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
	|	{hasStreetNameStarted}?	n=(NUMBER|NUMANDSTREETSUFFIX)
							{
							 address.setSuitePrefix(false);
							 address.setSuite($n.text);
							}
	|	{hasStreetNameStarted && startsUrbanNotRural}?	ruralRoute      
							{
							 address.setType(Address.Type.MIXED);
							 address.setUrbanBeforeRural(true);
							}
							
	|	n=(NAME|NUMBER|NUMANDSUFFIX|NUMANDSTREETSUFFIX|STREETNUMSUFFIX)		
							{
							 if (!address.isStreetTypePrefix() && address.getStreetType() != null) {
							    appendStreetName(address.getStreetType());
							    address.setStreetType(null);
							 }
							 if (!address.isSuitePrefix()) {
							    if (address.getSuiteType() != null) {
							       appendStreetName(address.getSuiteType());
							       address.setSuiteType(null);
							    }
							    if (address.getSuite() != null) {
							       appendStreetName(address.getSuite());
							       address.setSuite(null);
							    }
							 }
							 
							 hasStreetNameStarted = true;
							 appendStreetName($n.text);
							}
	;
	
ruralRouteAddress
	:	{setStartsUrbanNotRural(false)}? ruralRoute				
							{address.setType(Address.Type.RURAL);
							  if (address.isUrbanBeforeRural() != null) {
							    address.setType(Address.Type.MIXED);
							  }
							}
	;
	
ruralRoute
	:	{Address.isRuralRoute(input.LT(1).getText())}? rs=NAME n=(NUMBER|NUMANDSTREETSUFFIX)? ruralRouteSuffix
							{
							 address.setRuralRouteType($rs.text);
							 address.setRuralRouteNumber(quietIntParse($n.text));
							}
	|	{Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText())}? rs1=NAME rs2=NAME n=(NUMBER|NUMANDSTREETSUFFIX)? ruralRouteSuffix
							{
							 address.setRuralRouteType($rs1.text + " " + $rs2.text);
							 address.setRuralRouteNumber(quietIntParse($n.text));
							}
	|	{Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())}? rs1=NAME rs2=NAME rs3=NAME n=(NUMBER|NUMANDSTREETSUFFIX)? ruralRouteSuffix
							{
							 address.setRuralRouteType($rs1.text + " " + $rs2.text + " " + $rs3.text);
							 address.setRuralRouteNumber(quietIntParse($n.text));
							}
	;

ruralRouteSuffix
	:	diTypeAndName?
	|	{!startsUrbanNotRural}? streetAddress				
							{
							 address.setType(Address.Type.MIXED);
							 address.setUrbanBeforeRural(false);
							}
	;
	
lockBoxAddress
	:	{Address.isLockBox(input.LT(1).getText())}? lb=NAME '#'? n=NUMBER diTypeAndName
							{
							 address.setLockBoxType($lb.text);
							 address.setLockBoxNumber(quietIntParse($n.text));
							 address.setType(Address.Type.LOCK_BOX);
							}
	|	{Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText())}? lb1=NAME lb2=NAME '#'? n=NUMBER diTypeAndName
							{
							 address.setLockBoxType($lb1.text + " " + $lb2.text);
							 address.setLockBoxNumber(quietIntParse($n.text));
							 address.setType(Address.Type.LOCK_BOX);
							}
	;
	
generalDeliveryAddress
	:	{Address.isGeneralDelivery(input.LT(1).getText())}? gd=NAME diTypeAndName
							{
							 address.setGeneralDeliveryName($gd.text);
							 address.setType(Address.Type.GD);
							}
	|	{Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText())}? gd1=NAME gd2=NAME diTypeAndName
							{
							 address.setGeneralDeliveryName($gd1.text + " " + $gd2.text);
							 address.setType(Address.Type.GD);
							}
	|	{Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())}? 
			gd1=NAME gd2=(STREETNUMSUFFIX|NAME) gd3=NAME diTypeAndName
							{
							 address.setGeneralDeliveryName($gd1.text + " " + $gd2.text + " " + $gd3.text);
							 address.setType(Address.Type.GD);
							}
	;
	
diTypeAndName
	:	{Address.isDeliveryInstallationType(input.LT(1).getText())}? dt=NAME diName*
							{
							 address.setDeliveryInstallationType($dt.text);
							}
	|	{Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText())}? dt1=NAME dt2=NAME diName*
							{
							 address.setDeliveryInstallationType($dt1.text + " " + $dt2.text);
							}
	|	{Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())}? dt1=NAME dt2=NAME dt3=NAME diName*
							{
							 address.setDeliveryInstallationType($dt1.text + " " + $dt2.text + " " + $dt3.text);
							}
	;

diName
	:	stn=(NAME|NUMBER|NUMANDSUFFIX|NUMANDSTREETSUFFIX|STREETNUMSUFFIX)
							{
							 if (address.getDeliveryInstallationName() == null) {
							    address.setDeliveryInstallationName($stn.text);
							 } else {
							    address.setDeliveryInstallationName(address.getDeliveryInstallationName() + " " + $stn.text);
							 }
							}
	;
	
SUITEANDSTREETNUM
	:	('0'..'9')+'-'('0'..'9')+;
	
NUMANDSTREETSUFFIX
	:	('1'..'3');
	
STREETNUMSUFFIX 
	:	('A'..'Z');
	
NUMANDSUFFIX
	:	('0'..'9')+ ('A'..'Z');
	
NUMBER
	:	'#'?('0'..'9')+;
	
NAME	:	('A'..'Z' | '0'..'9' | '\'' | '-' | '.' | '/')+;
		/* TODO: allow multiple words (spaces!) */
	
WS	:	(' ' | '\t')+ {skip();};
