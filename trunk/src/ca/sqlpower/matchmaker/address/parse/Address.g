grammar Address;

/* XXX: this has no effect! need to figure out the right way to include package statement */
@header {
package ca.sqlpower.matchmaker.address.parse;

import ca.sqlpower.matchmaker.address.*;
}

@members {

/* Shut up warning about unused import */
static { Stack s; s = null; }

private Address address = new Address();

public Address getAddress() { return address; }

private static String wordList(List<?> words) {
   StringBuilder sb = new StringBuilder();
   boolean first = true;
   for (Object word : words) {
     Token t = (Token) word;
     if (!first) sb.append(" ");
     sb.append(t.getText());
     first = false;
   }
   return sb.toString();
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
}

fullAddress
	:	streetAddress city p=PROVINCE c=POSTALCODE
							{ address.setProvince($p.text);
							  address.setPostalCode($c.text);
							}
	;
	
streetAddress
	:	n=NUMBER street				{ address.setStreetNumber(quietIntParse($n.text)); }
	|	suiteNum '-' n=NUMBER street		{ address.setStreetNumber(quietIntParse($n.text));
							  address.setSuitePrefix(true);
							}
	|	n=NUMBER street s=SUITE suiteNum	{ address.setStreetNumber(quietIntParse($n.text));
							  address.setSuitePrefix(false);
							  address.setSuiteType($s.text);
							}
	;

suiteNum:	n=NUMBER				{ address.setSuite($n.text); }
	;

street	:	streetName t=STREETTYPE? d=STREETDIR?	{ address.setStreetType($t.text);
							  address.setStreetDirection($d.text);
							}
	|	d=STREETDIR streetName t=STREETTYPE?	{ address.setStreetType($t.text);
							  address.setStreetDirection($d.text);
							}
	;

streetName
	:	(n += NAME)+				{ address.setStreet(wordList($n)); }
	;
		/* XXX: this is ambiguous with city because all tokens in between are optional */
	
city	:	(n += NAME)+				{ address.setMunicipality(wordList($n)); }
	;

SUITE	:	'UNIT' | 'APT' | 'APARTMENT' | 'SUITE';

STREETTYPE
	:	'ABBEY' | 'ACRES' | 'ALLEE' | 'ALLEY' | 'AUT' | 'AVE' | 'AV'
	| 	'BAY' | 'BEACH' | 'BEND' | 'BLVD' | 'BOUL' | 'BYPASS' | 'BYWAY'
	|	'CAMPUS' | 'CAPE' | 'CAR' | 'CARREF' | 'CTR' | 'C' | 'CERCLE' | 'CHASE' | 'CH' | 'CIR' | 'CIRCT' | 'CLOSE' | 'COMMON' | 'CONC' | 'CRNRS' | 'COTE' | 'COUR' | 'COURS' | 'CRT' | 'COVE' | 'CRES' | 'CROIS' | 'CROSS' | 'CDS'
	|	'DALE' | 'DELL' | 'DIVERS' | 'DOWNS' | 'DR'
	|	'ECH' | 'END' | 'ESPL' | 'ESTATE' | 'EXPY' | 'EXTEN'
	|	'FARM' | 'FIELD' | 'FOREST' | 'FWY' | 'FRONT'
	|	'GDNS' | 'GATE' | 'GLADE' | 'GLEN' | 'GREEN' | 'GROVE'
	|	'HARBR' | 'HEATH' | 'HTS' | 'HGHLDS' | 'HWY' | 'HILL' | 'HOLLOW'
	|	'ILE' | 'IMP' | 'INLET' | 'ISLAND'
	|	'KEY' | 'KNOLL'
	|	'LANDING' | 'LANE' | 'LMTS' | 'LINE' | 'LINK' | 'LKOUT' | 'LOOP'
	|	'MALL' | 'MANOR' | 'MAZE' | 'MEADOW' | 'MEWS' | 'MONTEE' | 'MOOR' | 'MOUNT' | 'MTN'
	|	'ORCH'
	|	'PARADE' | 'PARC' | 'PK' | 'PKY' | 'PASS' | 'PATH' | 'PTWAY' | 'PINES' | 'PL' | 'PLACE' | 'PLAT' | 'PLAZA' | 'PT' | 'POINTE' | 'PORT' | 'PVT' | 'PROM'
	|	'QUAI' | 'QUAY'
	|	'RAMP' | 'RANG' | 'RG' | 'RIDGE' | 'RISE' | 'RD' | 'RDPT' | 'RTE' | 'ROW' | 'RUE' | 'RLE' | 'RUN'
	|	'SENT' | 'SQ' | 'ST' | 'SUBDIV'
	|	'TERR' | 'TSSE' | 'THICK' | 'TOWERS' | 'TLINE' | 'TRAIL' | 'TRNABT'
	|	'VALE' | 'VIA' | 'VIEW' | 'VILLAGE' | 'VILLAS' | 'VISTA' | 'VOIE'
	|	'WALK' | 'WAY' | 'WHARF' | 'WOOD' | 'WYND'
	;

STREETDIR
	:	'N' | 'S' | 'E' | 'W'
	|	'NE' | 'NW' | 'NO'
	|	'SE' | 'SW' | 'SO';

PROVINCE:	'BC' | 'AB' | 'SK' | 'MB' | 'ON' | 'QC' | 'NS' | 'NB' | 'PE' | 'NL' | 'NU' | 'NT' | 'YU';

POSTALCODE
	:	'A'..'Z' '0'..'9' 'A'..'Z' WS* '0'..'9' 'A'..'Z' '0'..'9';

NUMBER
	:	'0'..'9'+;

NAME	:	('A'..'Z' | '0'..'9')+;
		/* TODO: allow multiple words (spaces!) */
	
WS	:	(' ' | '\t')+ {skip();};
