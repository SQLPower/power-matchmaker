grammar Address;

/* XXX: this has no effect! need to figure out the right way to include package statement */
@header {
package ca.sqlpower.matchmaker.address.parse;

import ca.sqlpower.matchmaker.address.*;
}

@members {
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
}

fullAddress
	:	streetAddress city p=PROVINCE c=POSTALCODE
							{ address.setProvince($p.text);
							  address.setPostalCode($c.text);
							}
	;
	
streetAddress
	:	n=NUMBER street				{ address.setStreetNumber(Integer.valueOf($n.text)); }
	|	suiteNum '-' n=NUMBER street		{ address.setStreetNumber(Integer.valueOf($n.text));
							  address.setSuitePrefix(true);
							}
	|	n=NUMBER street s=SUITE suiteNum	{ address.setStreetNumber(Integer.valueOf($n.text));
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
	:	'AVE' | 'BLVD' | 'CR' | 'CRT' | 'PKY' | 'RD' | 'ST' | 'TERR' | 'WAY';
		/* TODO: complete list from Canada Post */

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
