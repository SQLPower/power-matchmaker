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
	:	n=(STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME)
							{
							 if (address.getFailedParsingString() == null) {
							    address.setFailedParsingString($n.text);
							 } else {
							    address.setFailedParsingString(address.getFailedParsingString() + " " + $n.text);
							 }
							}
	;
streetAddressStart
	:	{setStartsUrbanNotRural(true)}? streetAddress				
							{
							  address.setType(PostalCode.RecordType.STREET);
							  if (address.isUrbanBeforeRural() != null) {
							    address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
							  }
							}
	;
	
streetAddress	
	:	sn=NUMBER '-' street			
							{ 
							  address.setSuitePrefix(true);
							  address.setSuite($sn.text);

							}
	|	street
	;
	
street
	:	n=SUITEANDSTREETNUM streetToken+
							{String[] numbers = $n.text.split("-");
							 address.setSuitePrefix(true);
							 address.setSuite(numbers[0]);
							 address.setStreetNumber(quietIntParse(numbers[1]));
							}
	|	n=NUMANDSUFFIX streetToken+		{String streetNum = $n.text;
							 address.setStreetNumber(quietIntParse(streetNum.substring(0, streetNum.length() - 1)));
							 address.setStreetNumberSuffix(streetNum.substring(streetNum.length() - 1, streetNum.length()));
							}
	|	n=NUMBER streetToken+			
							{address.setStreetNumber(quietIntParse($n.text));}
	;
	
streetToken
	:	{Address.isSuiteType(input.LT(1).getText())}?=> s=NAME sn=NUMBER
							{
							 address.setSuitePrefix(false);
							 address.setSuiteType($s.text);
							 address.setSuite($sn.text);
							}
	|	{hasStreetNameStarted && address.isStreetDirection(input.LT(1).getText())}?=> d=(NAME|STREETNUMSUFFIX)	
							{
							 address.setStreetDirection($d.text);
							}
							
	|	{(!address.isStreetTypePrefix() || ("C".equals(address.getStreetType()) && address.getStreetNumberSuffix() == null)) && addressDatabase.containsStreetType(input.LT(1).getText())}?=> 
							t=(NAME|STREETNUMSUFFIX)
							{
							 //Fun special case where the street type C can come before the street name
							 //like a street type, somtimes it's a street type, sometimes it's a street
							 //number suffix. It's to be considered a street type unless there's another
							 //street type then it's a street number suffix if it comes after the street
							 //number (ie before the street name) and the street number suffix does not
							 //exist yet (may be a fun case of 118 C C Avenue = 118C Center Avenue).
							 if ("C".equals(address.getStreetType()) && address.getStreetNumberSuffix() == null) {
							    address.setStreetNumberSuffix("C");
							    address.setStreetType(null);
							    address.setStreetTypePrefix(false);
							 }
							 if (address.getStreetType() != null) {
							    appendStreetName(address.getStreetType());
							 }
							 if (!address.isSuitePrefix() && address.getSuite() != null) {
							    if (address.getSuiteType() != null) {
							       appendStreetName(address.getSuiteType());
							       address.setSuiteType(null);
							    }
							    appendStreetName(address.getSuite());
							    address.setSuite(null);
							 }
							 address.setStreetTypePrefix(!hasStreetNameStarted);
							 address.setStreetType($t.text);
							}
	|	{!hasStreetNameStarted}?=> s=(STREETNUMSUFFIX|NUMERICSTREETSUFFIX)
							{
							 address.setStreetNumberSuffix($s.text);
							}
	|	{hasStreetNameStarted}?=>	n=NUMBER
							{
							 address.setSuitePrefix(false);
							 address.setSuite($n.text);
							}
	|	{hasStreetNameStarted && startsUrbanNotRural}?=> ruralRoute      
							{
							 address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
							 address.setUrbanBeforeRural(true);
							}
							
	|	n=(NAME|NUMBER|NUMANDSUFFIX|NUMERICSTREETSUFFIX|STREETNUMSUFFIX)		
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
							{address.setType(PostalCode.RecordType.ROUTE);
							  if (address.isUrbanBeforeRural() != null) {
							    address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
							  }
							}
	;
	
ruralRoute
	:	{Address.isRuralRoute(input.LT(1).getText())}?=> rs=NAME n=NUMBER? ruralRouteSuffix
							{
							 address.setRuralRouteType($rs.text);
							 address.setRuralRouteNumber($n.text);
							}
	|	{Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText())}?=> rs1=NAME rs2=NAME n=NUMBER? ruralRouteSuffix
							{
							 address.setRuralRouteType($rs1.text + " " + $rs2.text);
							 address.setRuralRouteNumber($n.text);
							}
	|	{Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())}?=> rs1=NAME rs2=NAME rs3=NAME n=NUMBER? ruralRouteSuffix
							{
							 address.setRuralRouteType($rs1.text + " " + $rs2.text + " " + $rs3.text);
							 address.setRuralRouteNumber($n.text);
							}
	;

ruralRouteSuffix
	:	{!startsUrbanNotRural}?=> streetAddress				
							{
							 address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
							 address.setUrbanBeforeRural(false);
							}
	|	diTypeAndName
	;
	
lockBoxAddress
	:	{Address.isLockBox(input.LT(1).getText())}?=> lb=NAME '#'? n=NUMBER diTypeAndName
							{
							 address.setLockBoxType($lb.text);
							 address.setLockBoxNumber($n.text);
							 address.setType(PostalCode.RecordType.LOCK_BOX);
							}
	|	{Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText())}?=> lb1=NAME lb2=NAME '#'? n=NUMBER diTypeAndName
							{
							 address.setLockBoxType($lb1.text + " " + $lb2.text);
							 address.setLockBoxNumber($n.text);
							 address.setType(PostalCode.RecordType.LOCK_BOX);
							}
	;
	
generalDeliveryAddress
	:	{Address.isGeneralDelivery(input.LT(1).getText())}?=> gd=NAME diTypeAndName
							{
							 address.setGeneralDeliveryName($gd.text);
							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
							}
	|	{Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText())}?=> gd1=NAME gd2=NAME diTypeAndName
							{
							 address.setGeneralDeliveryName($gd1.text + " " + $gd2.text);
							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
							}
	|	{Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())}?=> 
			gd1=NAME gd2=(STREETNUMSUFFIX|NAME) gd3=NAME diTypeAndName
							{
							 address.setGeneralDeliveryName($gd1.text + " " + $gd2.text + " " + $gd3.text);
							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
							}
	;
	
diTypeAndName
	:	{Address.isDeliveryInstallationType(input.LT(1).getText())}?=> dt=NAME diName*
							{
							 address.setDeliveryInstallationType($dt.text);
							}
	|	{Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText())}?=> dt1=NAME dt2=NAME diName*
							{
							 address.setDeliveryInstallationType($dt1.text + " " + $dt2.text);
							}
	|	{Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())}?=> dt1=NAME dt2=NAME dt3=NAME diName*
							{
							 address.setDeliveryInstallationType($dt1.text + " " + $dt2.text + " " + $dt3.text);
							}
	|	diName*
	;

diName
	:	stn=(NAME|NUMBER|NUMANDSUFFIX|NUMERICSTREETSUFFIX|STREETNUMSUFFIX)
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
	
STREETNUMSUFFIX 
	:	('A'..'Z');

NUMERICSTREETSUFFIX
	:	('1/4'|'1/2'|'3/4');
	
NUMANDSUFFIX
	:	('0'..'9')+ ('A'..'Z');
	
NUMBER
	:	'#'?('0'..'9')+;
	
NAME	:	('A'..'Z' | '0'..'9' | '\'' | '-' | '.' | '/')+;
		/* TODO: allow multiple words (spaces!) */
	
WS	:	(' ' | '\t')+ {skip();};
