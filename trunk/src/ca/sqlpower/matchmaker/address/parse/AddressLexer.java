// $ANTLR 3.1.1 /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-02-02 17:48:20

package ca.sqlpower.matchmaker.address.parse;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class AddressLexer extends Lexer {
    public static final int T__12=12;
    public static final int STREETDIR=9;
    public static final int POSTALCODE=5;
    public static final int PROVINCE=4;
    public static final int SUITE=7;
    public static final int NUMBER=6;
    public static final int WS=11;
    public static final int EOF=-1;
    public static final int STREETTYPE=8;
    public static final int NAME=10;


    /* Shut up warnings about unused imports */
    static {
      Stack s = null;
      List l = null;
      ArrayList al = null;
      if (s != null && l != null && al != null) {
        /*wow!*/ 
      }
    }


    // delegates
    // delegators

    public AddressLexer() {;} 
    public AddressLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public AddressLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g"; }

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:19:7: ( '-' )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:19:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "SUITE"
    public final void mSUITE() throws RecognitionException {
        try {
            int _type = SUITE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:7: ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' )
            int alt1=7;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:9: 'UNIT'
                    {
                    match("UNIT"); 


                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:18: 'APT'
                    {
                    match("APT"); 


                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:26: 'APARTMENT'
                    {
                    match("APARTMENT"); 


                    }
                    break;
                case 4 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:40: 'SUITE'
                    {
                    match("SUITE"); 


                    }
                    break;
                case 5 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:50: 'APP'
                    {
                    match("APP"); 


                    }
                    break;
                case 6 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:58: 'BUREAU'
                    {
                    match("BUREAU"); 


                    }
                    break;
                case 7 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:69: 'UNITE'
                    {
                    match("UNITE"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SUITE"

    // $ANTLR start "STREETTYPE"
    public final void mSTREETTYPE() throws RecognitionException {
        try {
            int _type = STREETTYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:106:2: ( 'ABBEY' | 'ACRES' | 'ALLEE' | 'ALLEY' | 'AUT' | 'AVE' | 'AV' | 'BAY' | 'BEACH' | 'BEND' | 'BLVD' | 'BOUL' | 'BYPASS' | 'BYWAY' | 'CAMPUS' | 'CAPE' | 'CAR' | 'CARREF' | 'CTR' | 'C' | 'CERCLE' | 'CHASE' | 'CH' | 'CIR' | 'CIRCT' | 'CLOSE' | 'COMMON' | 'CONC' | 'CRNRS' | 'COTE' | 'COUR' | 'COURS' | 'CRT' | 'COVE' | 'CRES' | 'CROIS' | 'CROSS' | 'CDS' | 'DALE' | 'DELL' | 'DIVERS' | 'DOWNS' | 'DR' | 'ECH' | 'END' | 'ESPL' | 'ESTATE' | 'EXPY' | 'EXTEN' | 'FARM' | 'FIELD' | 'FOREST' | 'FWY' | 'FRONT' | 'GDNS' | 'GATE' | 'GLADE' | 'GLEN' | 'GREEN' | 'GROVE' | 'HARBR' | 'HEATH' | 'HTS' | 'HGHLDS' | 'HWY' | 'HILL' | 'HOLLOW' | 'ILE' | 'IMP' | 'INLET' | 'ISLAND' | 'KEY' | 'KNOLL' | 'LANDING' | 'LANE' | 'LMTS' | 'LINE' | 'LINK' | 'LKOUT' | 'LOOP' | 'MALL' | 'MANOR' | 'MAZE' | 'MEADOW' | 'MEWS' | 'MONTEE' | 'MOOR' | 'MOUNT' | 'MTN' | 'ORCH' | 'PARADE' | 'PARC' | 'PK' | 'PKY' | 'PASS' | 'PATH' | 'PTWAY' | 'PINES' | 'PL' | 'PLACE' | 'PLAT' | 'PLAZA' | 'PT' | 'POINTE' | 'PORT' | 'PVT' | 'PROM' | 'QUAI' | 'QUAY' | 'RAMP' | 'RANG' | 'RG' | 'RIDGE' | 'RISE' | 'RD' | 'RDPT' | 'RTE' | 'ROW' | 'RUE' | 'RLE' | 'RUN' | 'SENT' | 'SQ' | 'ST' | 'SUBDIV' | 'TERR' | 'TSSE' | 'THICK' | 'TOWERS' | 'TLINE' | 'TRAIL' | 'TRNABT' | 'VALE' | 'VIA' | 'VIEW' | 'VILLAGE' | 'VILLAS' | 'VISTA' | 'VOIE' | 'WALK' | 'WAY' | 'WHARF' | 'WOOD' | 'WYND' )
            int alt2=144;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:106:4: 'ABBEY'
                    {
                    match("ABBEY"); 


                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:106:14: 'ACRES'
                    {
                    match("ACRES"); 


                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:106:24: 'ALLEE'
                    {
                    match("ALLEE"); 


                    }
                    break;
                case 4 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:106:34: 'ALLEY'
                    {
                    match("ALLEY"); 


                    }
                    break;
                case 5 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:106:44: 'AUT'
                    {
                    match("AUT"); 


                    }
                    break;
                case 6 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:106:52: 'AVE'
                    {
                    match("AVE"); 


                    }
                    break;
                case 7 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:106:60: 'AV'
                    {
                    match("AV"); 


                    }
                    break;
                case 8 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:107:5: 'BAY'
                    {
                    match("BAY"); 


                    }
                    break;
                case 9 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:107:13: 'BEACH'
                    {
                    match("BEACH"); 


                    }
                    break;
                case 10 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:107:23: 'BEND'
                    {
                    match("BEND"); 


                    }
                    break;
                case 11 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:107:32: 'BLVD'
                    {
                    match("BLVD"); 


                    }
                    break;
                case 12 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:107:41: 'BOUL'
                    {
                    match("BOUL"); 


                    }
                    break;
                case 13 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:107:50: 'BYPASS'
                    {
                    match("BYPASS"); 


                    }
                    break;
                case 14 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:107:61: 'BYWAY'
                    {
                    match("BYWAY"); 


                    }
                    break;
                case 15 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:4: 'CAMPUS'
                    {
                    match("CAMPUS"); 


                    }
                    break;
                case 16 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:15: 'CAPE'
                    {
                    match("CAPE"); 


                    }
                    break;
                case 17 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:24: 'CAR'
                    {
                    match("CAR"); 


                    }
                    break;
                case 18 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:32: 'CARREF'
                    {
                    match("CARREF"); 


                    }
                    break;
                case 19 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:43: 'CTR'
                    {
                    match("CTR"); 


                    }
                    break;
                case 20 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:51: 'C'
                    {
                    match('C'); 

                    }
                    break;
                case 21 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:57: 'CERCLE'
                    {
                    match("CERCLE"); 


                    }
                    break;
                case 22 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:68: 'CHASE'
                    {
                    match("CHASE"); 


                    }
                    break;
                case 23 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:78: 'CH'
                    {
                    match("CH"); 


                    }
                    break;
                case 24 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:85: 'CIR'
                    {
                    match("CIR"); 


                    }
                    break;
                case 25 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:93: 'CIRCT'
                    {
                    match("CIRCT"); 


                    }
                    break;
                case 26 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:103: 'CLOSE'
                    {
                    match("CLOSE"); 


                    }
                    break;
                case 27 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:113: 'COMMON'
                    {
                    match("COMMON"); 


                    }
                    break;
                case 28 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:124: 'CONC'
                    {
                    match("CONC"); 


                    }
                    break;
                case 29 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:133: 'CRNRS'
                    {
                    match("CRNRS"); 


                    }
                    break;
                case 30 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:143: 'COTE'
                    {
                    match("COTE"); 


                    }
                    break;
                case 31 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:152: 'COUR'
                    {
                    match("COUR"); 


                    }
                    break;
                case 32 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:161: 'COURS'
                    {
                    match("COURS"); 


                    }
                    break;
                case 33 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:171: 'CRT'
                    {
                    match("CRT"); 


                    }
                    break;
                case 34 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:179: 'COVE'
                    {
                    match("COVE"); 


                    }
                    break;
                case 35 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:188: 'CRES'
                    {
                    match("CRES"); 


                    }
                    break;
                case 36 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:197: 'CROIS'
                    {
                    match("CROIS"); 


                    }
                    break;
                case 37 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:207: 'CROSS'
                    {
                    match("CROSS"); 


                    }
                    break;
                case 38 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:217: 'CDS'
                    {
                    match("CDS"); 


                    }
                    break;
                case 39 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:4: 'DALE'
                    {
                    match("DALE"); 


                    }
                    break;
                case 40 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:13: 'DELL'
                    {
                    match("DELL"); 


                    }
                    break;
                case 41 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:22: 'DIVERS'
                    {
                    match("DIVERS"); 


                    }
                    break;
                case 42 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:33: 'DOWNS'
                    {
                    match("DOWNS"); 


                    }
                    break;
                case 43 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:43: 'DR'
                    {
                    match("DR"); 


                    }
                    break;
                case 44 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:110:4: 'ECH'
                    {
                    match("ECH"); 


                    }
                    break;
                case 45 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:110:12: 'END'
                    {
                    match("END"); 


                    }
                    break;
                case 46 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:110:20: 'ESPL'
                    {
                    match("ESPL"); 


                    }
                    break;
                case 47 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:110:29: 'ESTATE'
                    {
                    match("ESTATE"); 


                    }
                    break;
                case 48 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:110:40: 'EXPY'
                    {
                    match("EXPY"); 


                    }
                    break;
                case 49 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:110:49: 'EXTEN'
                    {
                    match("EXTEN"); 


                    }
                    break;
                case 50 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:111:4: 'FARM'
                    {
                    match("FARM"); 


                    }
                    break;
                case 51 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:111:13: 'FIELD'
                    {
                    match("FIELD"); 


                    }
                    break;
                case 52 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:111:23: 'FOREST'
                    {
                    match("FOREST"); 


                    }
                    break;
                case 53 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:111:34: 'FWY'
                    {
                    match("FWY"); 


                    }
                    break;
                case 54 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:111:42: 'FRONT'
                    {
                    match("FRONT"); 


                    }
                    break;
                case 55 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:112:4: 'GDNS'
                    {
                    match("GDNS"); 


                    }
                    break;
                case 56 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:112:13: 'GATE'
                    {
                    match("GATE"); 


                    }
                    break;
                case 57 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:112:22: 'GLADE'
                    {
                    match("GLADE"); 


                    }
                    break;
                case 58 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:112:32: 'GLEN'
                    {
                    match("GLEN"); 


                    }
                    break;
                case 59 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:112:41: 'GREEN'
                    {
                    match("GREEN"); 


                    }
                    break;
                case 60 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:112:51: 'GROVE'
                    {
                    match("GROVE"); 


                    }
                    break;
                case 61 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:4: 'HARBR'
                    {
                    match("HARBR"); 


                    }
                    break;
                case 62 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:14: 'HEATH'
                    {
                    match("HEATH"); 


                    }
                    break;
                case 63 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:24: 'HTS'
                    {
                    match("HTS"); 


                    }
                    break;
                case 64 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:32: 'HGHLDS'
                    {
                    match("HGHLDS"); 


                    }
                    break;
                case 65 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:43: 'HWY'
                    {
                    match("HWY"); 


                    }
                    break;
                case 66 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:51: 'HILL'
                    {
                    match("HILL"); 


                    }
                    break;
                case 67 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:60: 'HOLLOW'
                    {
                    match("HOLLOW"); 


                    }
                    break;
                case 68 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:114:4: 'ILE'
                    {
                    match("ILE"); 


                    }
                    break;
                case 69 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:114:12: 'IMP'
                    {
                    match("IMP"); 


                    }
                    break;
                case 70 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:114:20: 'INLET'
                    {
                    match("INLET"); 


                    }
                    break;
                case 71 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:114:30: 'ISLAND'
                    {
                    match("ISLAND"); 


                    }
                    break;
                case 72 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:115:4: 'KEY'
                    {
                    match("KEY"); 


                    }
                    break;
                case 73 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:115:12: 'KNOLL'
                    {
                    match("KNOLL"); 


                    }
                    break;
                case 74 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:4: 'LANDING'
                    {
                    match("LANDING"); 


                    }
                    break;
                case 75 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:16: 'LANE'
                    {
                    match("LANE"); 


                    }
                    break;
                case 76 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:25: 'LMTS'
                    {
                    match("LMTS"); 


                    }
                    break;
                case 77 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:34: 'LINE'
                    {
                    match("LINE"); 


                    }
                    break;
                case 78 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:43: 'LINK'
                    {
                    match("LINK"); 


                    }
                    break;
                case 79 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:52: 'LKOUT'
                    {
                    match("LKOUT"); 


                    }
                    break;
                case 80 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:62: 'LOOP'
                    {
                    match("LOOP"); 


                    }
                    break;
                case 81 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:4: 'MALL'
                    {
                    match("MALL"); 


                    }
                    break;
                case 82 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:13: 'MANOR'
                    {
                    match("MANOR"); 


                    }
                    break;
                case 83 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:23: 'MAZE'
                    {
                    match("MAZE"); 


                    }
                    break;
                case 84 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:32: 'MEADOW'
                    {
                    match("MEADOW"); 


                    }
                    break;
                case 85 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:43: 'MEWS'
                    {
                    match("MEWS"); 


                    }
                    break;
                case 86 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:52: 'MONTEE'
                    {
                    match("MONTEE"); 


                    }
                    break;
                case 87 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:63: 'MOOR'
                    {
                    match("MOOR"); 


                    }
                    break;
                case 88 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:72: 'MOUNT'
                    {
                    match("MOUNT"); 


                    }
                    break;
                case 89 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:82: 'MTN'
                    {
                    match("MTN"); 


                    }
                    break;
                case 90 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:118:4: 'ORCH'
                    {
                    match("ORCH"); 


                    }
                    break;
                case 91 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:4: 'PARADE'
                    {
                    match("PARADE"); 


                    }
                    break;
                case 92 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:15: 'PARC'
                    {
                    match("PARC"); 


                    }
                    break;
                case 93 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:24: 'PK'
                    {
                    match("PK"); 


                    }
                    break;
                case 94 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:31: 'PKY'
                    {
                    match("PKY"); 


                    }
                    break;
                case 95 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:39: 'PASS'
                    {
                    match("PASS"); 


                    }
                    break;
                case 96 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:48: 'PATH'
                    {
                    match("PATH"); 


                    }
                    break;
                case 97 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:57: 'PTWAY'
                    {
                    match("PTWAY"); 


                    }
                    break;
                case 98 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:67: 'PINES'
                    {
                    match("PINES"); 


                    }
                    break;
                case 99 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:77: 'PL'
                    {
                    match("PL"); 


                    }
                    break;
                case 100 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:84: 'PLACE'
                    {
                    match("PLACE"); 


                    }
                    break;
                case 101 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:94: 'PLAT'
                    {
                    match("PLAT"); 


                    }
                    break;
                case 102 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:103: 'PLAZA'
                    {
                    match("PLAZA"); 


                    }
                    break;
                case 103 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:113: 'PT'
                    {
                    match("PT"); 


                    }
                    break;
                case 104 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:120: 'POINTE'
                    {
                    match("POINTE"); 


                    }
                    break;
                case 105 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:131: 'PORT'
                    {
                    match("PORT"); 


                    }
                    break;
                case 106 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:140: 'PVT'
                    {
                    match("PVT"); 


                    }
                    break;
                case 107 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:148: 'PROM'
                    {
                    match("PROM"); 


                    }
                    break;
                case 108 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:120:4: 'QUAI'
                    {
                    match("QUAI"); 


                    }
                    break;
                case 109 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:120:13: 'QUAY'
                    {
                    match("QUAY"); 


                    }
                    break;
                case 110 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:4: 'RAMP'
                    {
                    match("RAMP"); 


                    }
                    break;
                case 111 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:13: 'RANG'
                    {
                    match("RANG"); 


                    }
                    break;
                case 112 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:22: 'RG'
                    {
                    match("RG"); 


                    }
                    break;
                case 113 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:29: 'RIDGE'
                    {
                    match("RIDGE"); 


                    }
                    break;
                case 114 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:39: 'RISE'
                    {
                    match("RISE"); 


                    }
                    break;
                case 115 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:48: 'RD'
                    {
                    match("RD"); 


                    }
                    break;
                case 116 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:55: 'RDPT'
                    {
                    match("RDPT"); 


                    }
                    break;
                case 117 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:64: 'RTE'
                    {
                    match("RTE"); 


                    }
                    break;
                case 118 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:72: 'ROW'
                    {
                    match("ROW"); 


                    }
                    break;
                case 119 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:80: 'RUE'
                    {
                    match("RUE"); 


                    }
                    break;
                case 120 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:88: 'RLE'
                    {
                    match("RLE"); 


                    }
                    break;
                case 121 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:96: 'RUN'
                    {
                    match("RUN"); 


                    }
                    break;
                case 122 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:122:4: 'SENT'
                    {
                    match("SENT"); 


                    }
                    break;
                case 123 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:122:13: 'SQ'
                    {
                    match("SQ"); 


                    }
                    break;
                case 124 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:122:20: 'ST'
                    {
                    match("ST"); 


                    }
                    break;
                case 125 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:122:27: 'SUBDIV'
                    {
                    match("SUBDIV"); 


                    }
                    break;
                case 126 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:123:4: 'TERR'
                    {
                    match("TERR"); 


                    }
                    break;
                case 127 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:123:13: 'TSSE'
                    {
                    match("TSSE"); 


                    }
                    break;
                case 128 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:123:22: 'THICK'
                    {
                    match("THICK"); 


                    }
                    break;
                case 129 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:123:32: 'TOWERS'
                    {
                    match("TOWERS"); 


                    }
                    break;
                case 130 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:123:43: 'TLINE'
                    {
                    match("TLINE"); 


                    }
                    break;
                case 131 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:123:53: 'TRAIL'
                    {
                    match("TRAIL"); 


                    }
                    break;
                case 132 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:123:63: 'TRNABT'
                    {
                    match("TRNABT"); 


                    }
                    break;
                case 133 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:4: 'VALE'
                    {
                    match("VALE"); 


                    }
                    break;
                case 134 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:13: 'VIA'
                    {
                    match("VIA"); 


                    }
                    break;
                case 135 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:21: 'VIEW'
                    {
                    match("VIEW"); 


                    }
                    break;
                case 136 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:30: 'VILLAGE'
                    {
                    match("VILLAGE"); 


                    }
                    break;
                case 137 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:42: 'VILLAS'
                    {
                    match("VILLAS"); 


                    }
                    break;
                case 138 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:53: 'VISTA'
                    {
                    match("VISTA"); 


                    }
                    break;
                case 139 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:63: 'VOIE'
                    {
                    match("VOIE"); 


                    }
                    break;
                case 140 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:4: 'WALK'
                    {
                    match("WALK"); 


                    }
                    break;
                case 141 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:13: 'WAY'
                    {
                    match("WAY"); 


                    }
                    break;
                case 142 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:21: 'WHARF'
                    {
                    match("WHARF"); 


                    }
                    break;
                case 143 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:31: 'WOOD'
                    {
                    match("WOOD"); 


                    }
                    break;
                case 144 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:40: 'WYND'
                    {
                    match("WYND"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STREETTYPE"

    // $ANTLR start "STREETDIR"
    public final void mSTREETDIR() throws RecognitionException {
        try {
            int _type = STREETDIR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:129:2: ( 'N' | 'S' | 'E' | 'W' | 'NE' | 'NW' | 'NO' | 'SE' | 'SW' | 'SO' )
            int alt3=10;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:129:4: 'N'
                    {
                    match('N'); 

                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:129:10: 'S'
                    {
                    match('S'); 

                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:129:16: 'E'
                    {
                    match('E'); 

                    }
                    break;
                case 4 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:129:22: 'W'
                    {
                    match('W'); 

                    }
                    break;
                case 5 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:130:4: 'NE'
                    {
                    match("NE"); 


                    }
                    break;
                case 6 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:130:11: 'NW'
                    {
                    match("NW"); 


                    }
                    break;
                case 7 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:130:18: 'NO'
                    {
                    match("NO"); 


                    }
                    break;
                case 8 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:131:4: 'SE'
                    {
                    match("SE"); 


                    }
                    break;
                case 9 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:131:11: 'SW'
                    {
                    match("SW"); 


                    }
                    break;
                case 10 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:131:18: 'SO'
                    {
                    match("SO"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STREETDIR"

    // $ANTLR start "PROVINCE"
    public final void mPROVINCE() throws RecognitionException {
        try {
            int _type = PROVINCE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:9: ( 'BC' | 'AB' | 'SK' | 'MB' | 'ON' | 'QC' | 'NS' | 'NB' | 'PE' | 'NL' | 'NU' | 'NT' | 'YU' )
            int alt4=13;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:11: 'BC'
                    {
                    match("BC"); 


                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:18: 'AB'
                    {
                    match("AB"); 


                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:25: 'SK'
                    {
                    match("SK"); 


                    }
                    break;
                case 4 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:32: 'MB'
                    {
                    match("MB"); 


                    }
                    break;
                case 5 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:39: 'ON'
                    {
                    match("ON"); 


                    }
                    break;
                case 6 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:46: 'QC'
                    {
                    match("QC"); 


                    }
                    break;
                case 7 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:53: 'NS'
                    {
                    match("NS"); 


                    }
                    break;
                case 8 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:60: 'NB'
                    {
                    match("NB"); 


                    }
                    break;
                case 9 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:67: 'PE'
                    {
                    match("PE"); 


                    }
                    break;
                case 10 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:74: 'NL'
                    {
                    match("NL"); 


                    }
                    break;
                case 11 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:81: 'NU'
                    {
                    match("NU"); 


                    }
                    break;
                case 12 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:88: 'NT'
                    {
                    match("NT"); 


                    }
                    break;
                case 13 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:95: 'YU'
                    {
                    match("YU"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PROVINCE"

    // $ANTLR start "POSTALCODE"
    public final void mPOSTALCODE() throws RecognitionException {
        try {
            int _type = POSTALCODE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:2: ( 'A' .. 'Z' '0' .. '9' 'A' .. 'Z' ( WS )* '0' .. '9' 'A' .. 'Z' '0' .. '9' )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:4: 'A' .. 'Z' '0' .. '9' 'A' .. 'Z' ( WS )* '0' .. '9' 'A' .. 'Z' '0' .. '9'
            {
            matchRange('A','Z'); 
            matchRange('0','9'); 
            matchRange('A','Z'); 
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:31: ( WS )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\t'||LA5_0==' ') ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:31: WS
            	    {
            	    mWS(); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            matchRange('0','9'); 
            matchRange('A','Z'); 
            matchRange('0','9'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "POSTALCODE"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:139:2: ( ( '0' .. '9' )+ )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:139:4: ( '0' .. '9' )+
            {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:139:4: ( '0' .. '9' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:139:4: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "NAME"
    public final void mNAME() throws RecognitionException {
        try {
            int _type = NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:141:6: ( ( 'A' .. 'Z' | '0' .. '9' )+ )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:141:8: ( 'A' .. 'Z' | '0' .. '9' )+
            {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:141:8: ( 'A' .. 'Z' | '0' .. '9' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='0' && LA7_0<='9')||(LA7_0>='A' && LA7_0<='Z')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAME"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:144:4: ( ( ' ' | '\\t' )+ )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:144:6: ( ' ' | '\\t' )+
            {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:144:6: ( ' ' | '\\t' )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='\t'||LA8_0==' ') ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);

            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:8: ( T__12 | SUITE | STREETTYPE | STREETDIR | PROVINCE | POSTALCODE | NUMBER | NAME | WS )
        int alt9=9;
        alt9 = dfa9.predict(input);
        switch (alt9) {
            case 1 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:10: T__12
                {
                mT__12(); 

                }
                break;
            case 2 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:16: SUITE
                {
                mSUITE(); 

                }
                break;
            case 3 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:22: STREETTYPE
                {
                mSTREETTYPE(); 

                }
                break;
            case 4 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:33: STREETDIR
                {
                mSTREETDIR(); 

                }
                break;
            case 5 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:43: PROVINCE
                {
                mPROVINCE(); 

                }
                break;
            case 6 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:52: POSTALCODE
                {
                mPOSTALCODE(); 

                }
                break;
            case 7 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:63: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 8 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:70: NAME
                {
                mNAME(); 

                }
                break;
            case 9 :
                // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:75: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA1 dfa1 = new DFA1(this);
    protected DFA2 dfa2 = new DFA2(this);
    protected DFA3 dfa3 = new DFA3(this);
    protected DFA4 dfa4 = new DFA4(this);
    protected DFA9 dfa9 = new DFA9(this);
    static final String DFA1_eotS =
        "\13\uffff\1\15\2\uffff";
    static final String DFA1_eofS =
        "\16\uffff";
    static final String DFA1_minS =
        "\1\101\1\116\1\120\2\uffff\1\111\1\101\1\124\3\uffff\1\105\2\uffff";
    static final String DFA1_maxS =
        "\1\125\1\116\1\120\2\uffff\1\111\2\124\3\uffff\1\105\2\uffff";
    static final String DFA1_acceptS =
        "\3\uffff\1\4\1\6\3\uffff\1\2\1\3\1\5\1\uffff\1\7\1\1";
    static final String DFA1_specialS =
        "\16\uffff}>";
    static final String[] DFA1_transitionS = {
            "\1\2\1\4\20\uffff\1\3\1\uffff\1\1",
            "\1\5",
            "\1\6",
            "",
            "",
            "\1\7",
            "\1\11\16\uffff\1\12\3\uffff\1\10",
            "\1\13",
            "",
            "",
            "",
            "\1\14",
            "",
            ""
    };

    static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
    static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
    static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
    static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
    static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
    static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
    static final short[][] DFA1_transition;

    static {
        int numStates = DFA1_transitionS.length;
        DFA1_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
        }
    }

    class DFA1 extends DFA {

        public DFA1(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 1;
            this.eot = DFA1_eot;
            this.eof = DFA1_eof;
            this.min = DFA1_min;
            this.max = DFA1_max;
            this.accept = DFA1_accept;
            this.special = DFA1_special;
            this.transition = DFA1_transition;
        }
        public String getDescription() {
            return "103:1: SUITE : ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' );";
        }
    }
    static final String DFA2_eotS =
        "\3\uffff\1\50\25\uffff\1\165\10\uffff\1\176\57\uffff\1\u009f\1\u00a1"+
        "\1\uffff\1\u00a3\7\uffff\1\u00ac\36\uffff\1\u00b9\2\uffff\1\u00bb"+
        "\74\uffff\1\u00ce\25\uffff";
    static final String DFA2_eofS =
        "\u00d2\uffff";
    static final String DFA2_minS =
        "\1\101\1\102\3\101\1\103\3\101\1\114\1\105\2\101\1\uffff\1\101\1"+
        "\125\1\101\2\105\2\101\2\uffff\1\114\1\uffff\1\105\1\uffff\1\101"+
        "\2\uffff\1\120\1\115\2\uffff\1\101\1\122\1\uffff\1\115\1\105\11"+
        "\uffff\2\120\7\uffff\1\101\1\105\15\uffff\1\116\1\uffff\1\116\2"+
        "\uffff\1\114\1\101\1\116\1\uffff\1\122\1\131\1\127\1\uffff\1\101"+
        "\1\111\2\uffff\1\101\1\115\1\uffff\1\104\1\120\2\uffff\1\105\12"+
        "\uffff\1\101\1\uffff\1\101\1\uffff\1\114\3\uffff\1\105\10\uffff"+
        "\1\122\2\uffff\1\103\3\uffff\1\122\4\uffff\1\111\10\uffff\1\104"+
        "\1\105\10\uffff\1\101\6\uffff\1\103\3\uffff\1\111\14\uffff\1\114"+
        "\3\uffff\1\105\4\uffff\1\123\15\uffff\1\101\4\uffff\1\107\2\uffff";
    static final String DFA2_maxS =
        "\1\127\1\126\1\131\1\124\1\122\1\130\1\127\1\122\1\127\1\123\1\116"+
        "\1\117\1\124\1\uffff\1\126\3\125\1\123\1\117\1\131\2\uffff\1\114"+
        "\1\uffff\1\105\1\uffff\1\116\2\uffff\1\127\1\122\2\uffff\1\101\1"+
        "\122\1\uffff\1\126\1\124\11\uffff\2\124\7\uffff\1\105\1\117\15\uffff"+
        "\1\116\1\uffff\1\116\2\uffff\1\132\1\127\1\125\1\uffff\1\124\1\131"+
        "\1\127\1\uffff\1\101\1\122\2\uffff\1\101\1\116\1\uffff\1\123\1\120"+
        "\2\uffff\1\116\12\uffff\1\116\1\uffff\1\123\1\uffff\1\131\3\uffff"+
        "\1\105\10\uffff\1\122\2\uffff\1\103\3\uffff\1\122\4\uffff\1\123"+
        "\10\uffff\1\105\1\113\10\uffff\1\103\6\uffff\1\132\3\uffff\1\131"+
        "\14\uffff\1\114\3\uffff\1\131\4\uffff\1\123\15\uffff\1\101\4\uffff"+
        "\1\123\2\uffff";
    static final String DFA2_acceptS =
        "\15\uffff\1\132\7\uffff\1\1\1\2\1\uffff\1\5\1\uffff\1\10\1\uffff"+
        "\1\13\1\14\2\uffff\1\23\1\25\2\uffff\1\32\2\uffff\1\46\1\24\1\47"+
        "\1\50\1\51\1\52\1\53\1\54\1\55\2\uffff\1\62\1\63\1\64\1\65\1\66"+
        "\1\67\1\70\2\uffff\1\75\1\76\1\77\1\100\1\101\1\102\1\103\1\104"+
        "\1\105\1\106\1\107\1\110\1\111\1\uffff\1\114\1\uffff\1\117\1\120"+
        "\3\uffff\1\131\3\uffff\1\142\2\uffff\1\152\1\153\2\uffff\1\160\2"+
        "\uffff\1\165\1\166\1\uffff\1\170\1\172\1\173\1\174\1\175\1\176\1"+
        "\177\1\u0080\1\u0081\1\u0082\1\uffff\1\u0085\1\uffff\1\u008b\1\uffff"+
        "\1\u008e\1\u008f\1\u0090\1\uffff\1\6\1\7\1\11\1\12\1\15\1\16\1\17"+
        "\1\20\1\uffff\1\26\1\27\1\uffff\1\33\1\34\1\36\1\uffff\1\42\1\35"+
        "\1\41\1\43\1\uffff\1\56\1\57\1\60\1\61\1\71\1\72\1\73\1\74\2\uffff"+
        "\1\121\1\122\1\123\1\124\1\125\1\126\1\127\1\130\1\uffff\1\137\1"+
        "\140\1\136\1\135\1\141\1\147\1\uffff\1\143\1\150\1\151\1\uffff\1"+
        "\156\1\157\1\161\1\162\1\164\1\163\1\167\1\171\1\u0083\1\u0084\1"+
        "\u0086\1\u0087\1\uffff\1\u008a\1\u008c\1\u008d\1\uffff\1\22\1\21"+
        "\1\31\1\30\1\uffff\1\44\1\45\1\112\1\113\1\115\1\116\1\133\1\134"+
        "\1\144\1\145\1\146\1\154\1\155\1\uffff\1\3\1\4\1\40\1\37\1\uffff"+
        "\1\u0088\1\u0089";
    static final String DFA2_specialS =
        "\u00d2\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\uffff\1\12\1\13\1\14"+
            "\1\uffff\1\15\1\16\1\17\1\20\1\21\1\22\1\uffff\1\23\1\24",
            "\1\25\1\26\10\uffff\1\27\10\uffff\1\30\1\31",
            "\1\32\3\uffff\1\33\6\uffff\1\34\2\uffff\1\35\11\uffff\1\36",
            "\1\37\2\uffff\1\47\1\41\2\uffff\1\42\1\43\2\uffff\1\44\2\uffff"+
            "\1\45\2\uffff\1\46\1\uffff\1\40",
            "\1\51\3\uffff\1\52\3\uffff\1\53\5\uffff\1\54\2\uffff\1\55",
            "\1\56\12\uffff\1\57\4\uffff\1\60\4\uffff\1\61",
            "\1\62\7\uffff\1\63\5\uffff\1\64\2\uffff\1\66\4\uffff\1\65",
            "\1\70\2\uffff\1\67\7\uffff\1\71\5\uffff\1\72",
            "\1\73\3\uffff\1\74\1\uffff\1\76\1\uffff\1\100\5\uffff\1\101"+
            "\4\uffff\1\75\2\uffff\1\77",
            "\1\102\1\103\1\104\4\uffff\1\105",
            "\1\106\10\uffff\1\107",
            "\1\110\7\uffff\1\112\1\uffff\1\113\1\uffff\1\111\1\uffff\1"+
            "\114",
            "\1\115\3\uffff\1\116\11\uffff\1\117\4\uffff\1\120",
            "",
            "\1\121\7\uffff\1\124\1\uffff\1\122\1\125\2\uffff\1\126\2\uffff"+
            "\1\130\1\uffff\1\123\1\uffff\1\127",
            "\1\131",
            "\1\132\2\uffff\1\135\2\uffff\1\133\1\uffff\1\134\2\uffff\1"+
            "\141\2\uffff\1\137\4\uffff\1\136\1\140",
            "\1\142\13\uffff\1\143\2\uffff\1\144\1\145",
            "\1\146\2\uffff\1\150\3\uffff\1\152\2\uffff\1\151\2\uffff\1"+
            "\153\1\147",
            "\1\154\7\uffff\1\155\5\uffff\1\156",
            "\1\157\6\uffff\1\160\6\uffff\1\161\11\uffff\1\162",
            "",
            "",
            "\1\163",
            "",
            "\1\164",
            "",
            "\1\166\14\uffff\1\167",
            "",
            "",
            "\1\170\6\uffff\1\171",
            "\1\172\2\uffff\1\173\1\uffff\1\174",
            "",
            "",
            "\1\175",
            "\1\177",
            "",
            "\1\u0080\1\u0081\5\uffff\1\u0082\1\u0083\1\u0084",
            "\1\u0087\10\uffff\1\u0085\1\u0088\4\uffff\1\u0086",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u0089\3\uffff\1\u008a",
            "\1\u008b\3\uffff\1\u008c",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u008d\3\uffff\1\u008e",
            "\1\u008f\11\uffff\1\u0090",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u0091",
            "",
            "\1\u0092",
            "",
            "",
            "\1\u0093\1\uffff\1\u0094\13\uffff\1\u0095",
            "\1\u0096\25\uffff\1\u0097",
            "\1\u0098\1\u0099\5\uffff\1\u009a",
            "",
            "\1\u009b\1\u009c\1\u009d",
            "\1\u009e",
            "\1\u00a0",
            "",
            "\1\u00a2",
            "\1\u00a4\10\uffff\1\u00a5",
            "",
            "",
            "\1\u00a6",
            "\1\u00a7\1\u00a8",
            "",
            "\1\u00a9\16\uffff\1\u00aa",
            "\1\u00ab",
            "",
            "",
            "\1\u00ad\10\uffff\1\u00ae",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00af\14\uffff\1\u00b0",
            "",
            "\1\u00b1\3\uffff\1\u00b2\6\uffff\1\u00b3\6\uffff\1\u00b4",
            "",
            "\1\u00b5\14\uffff\1\u00b6",
            "",
            "",
            "",
            "\1\u00b7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00b8",
            "",
            "",
            "\1\u00ba",
            "",
            "",
            "",
            "\1\u00bc",
            "",
            "",
            "",
            "",
            "\1\u00bd\11\uffff\1\u00be",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00bf\1\u00c0",
            "\1\u00c1\5\uffff\1\u00c2",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00c3\1\uffff\1\u00c4",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00c5\20\uffff\1\u00c6\5\uffff\1\u00c7",
            "",
            "",
            "",
            "\1\u00c8\17\uffff\1\u00c9",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00ca",
            "",
            "",
            "",
            "\1\u00cb\23\uffff\1\u00cc",
            "",
            "",
            "",
            "",
            "\1\u00cd",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00cf",
            "",
            "",
            "",
            "",
            "\1\u00d0\13\uffff\1\u00d1",
            "",
            ""
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "105:1: STREETTYPE : ( 'ABBEY' | 'ACRES' | 'ALLEE' | 'ALLEY' | 'AUT' | 'AVE' | 'AV' | 'BAY' | 'BEACH' | 'BEND' | 'BLVD' | 'BOUL' | 'BYPASS' | 'BYWAY' | 'CAMPUS' | 'CAPE' | 'CAR' | 'CARREF' | 'CTR' | 'C' | 'CERCLE' | 'CHASE' | 'CH' | 'CIR' | 'CIRCT' | 'CLOSE' | 'COMMON' | 'CONC' | 'CRNRS' | 'COTE' | 'COUR' | 'COURS' | 'CRT' | 'COVE' | 'CRES' | 'CROIS' | 'CROSS' | 'CDS' | 'DALE' | 'DELL' | 'DIVERS' | 'DOWNS' | 'DR' | 'ECH' | 'END' | 'ESPL' | 'ESTATE' | 'EXPY' | 'EXTEN' | 'FARM' | 'FIELD' | 'FOREST' | 'FWY' | 'FRONT' | 'GDNS' | 'GATE' | 'GLADE' | 'GLEN' | 'GREEN' | 'GROVE' | 'HARBR' | 'HEATH' | 'HTS' | 'HGHLDS' | 'HWY' | 'HILL' | 'HOLLOW' | 'ILE' | 'IMP' | 'INLET' | 'ISLAND' | 'KEY' | 'KNOLL' | 'LANDING' | 'LANE' | 'LMTS' | 'LINE' | 'LINK' | 'LKOUT' | 'LOOP' | 'MALL' | 'MANOR' | 'MAZE' | 'MEADOW' | 'MEWS' | 'MONTEE' | 'MOOR' | 'MOUNT' | 'MTN' | 'ORCH' | 'PARADE' | 'PARC' | 'PK' | 'PKY' | 'PASS' | 'PATH' | 'PTWAY' | 'PINES' | 'PL' | 'PLACE' | 'PLAT' | 'PLAZA' | 'PT' | 'POINTE' | 'PORT' | 'PVT' | 'PROM' | 'QUAI' | 'QUAY' | 'RAMP' | 'RANG' | 'RG' | 'RIDGE' | 'RISE' | 'RD' | 'RDPT' | 'RTE' | 'ROW' | 'RUE' | 'RLE' | 'RUN' | 'SENT' | 'SQ' | 'ST' | 'SUBDIV' | 'TERR' | 'TSSE' | 'THICK' | 'TOWERS' | 'TLINE' | 'TRAIL' | 'TRNABT' | 'VALE' | 'VIA' | 'VIEW' | 'VILLAGE' | 'VILLAS' | 'VISTA' | 'VOIE' | 'WALK' | 'WAY' | 'WHARF' | 'WOOD' | 'WYND' );";
        }
    }
    static final String DFA3_eotS =
        "\1\uffff\1\10\1\14\12\uffff";
    static final String DFA3_eofS =
        "\15\uffff";
    static final String DFA3_minS =
        "\3\105\12\uffff";
    static final String DFA3_maxS =
        "\3\127\12\uffff";
    static final String DFA3_acceptS =
        "\3\uffff\1\3\1\4\1\5\1\6\1\7\1\1\1\10\1\11\1\12\1\2";
    static final String DFA3_specialS =
        "\15\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\3\10\uffff\1\1\4\uffff\1\2\3\uffff\1\4",
            "\1\5\11\uffff\1\7\7\uffff\1\6",
            "\1\11\11\uffff\1\13\7\uffff\1\12",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "128:1: STREETDIR : ( 'N' | 'S' | 'E' | 'W' | 'NE' | 'NW' | 'NO' | 'SE' | 'SW' | 'SO' );";
        }
    }
    static final String DFA4_eotS =
        "\17\uffff";
    static final String DFA4_eofS =
        "\17\uffff";
    static final String DFA4_minS =
        "\1\101\6\uffff\1\102\7\uffff";
    static final String DFA4_maxS =
        "\1\131\6\uffff\1\125\7\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\uffff\1\11\1\15\1\7\1\10\1\12"+
        "\1\13\1\14";
    static final String DFA4_specialS =
        "\17\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\2\1\1\12\uffff\1\4\1\7\1\5\1\10\1\6\1\uffff\1\3\5\uffff\1"+
            "\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\13\11\uffff\1\14\6\uffff\1\12\1\16\1\15",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "133:1: PROVINCE : ( 'BC' | 'AB' | 'SK' | 'MB' | 'ON' | 'QC' | 'NS' | 'NB' | 'PE' | 'NL' | 'NU' | 'NT' | 'YU' );";
        }
    }
    static final String DFA9_eotS =
        "\2\uffff\2\36\1\54\1\36\1\75\1\36\1\54\15\36\2\54\2\36\1\u0092\1"+
        "\uffff\2\36\1\uffff\1\36\1\u0099\3\36\1\75\1\36\1\54\2\75\2\54\1"+
        "\u0099\1\uffff\6\36\1\u0099\3\36\1\75\5\36\1\uffff\4\36\1\75\43"+
        "\36\1\u0099\1\36\1\u0099\1\36\2\75\1\36\1\75\3\36\1\u0099\1\36\1"+
        "\u0099\1\36\1\75\1\36\1\75\21\36\3\54\6\u0099\1\uffff\2\36\1\u0117"+
        "\1\36\1\u0117\1\36\1\uffff\2\36\2\75\4\36\1\75\10\36\2\75\2\36\1"+
        "\75\7\36\1\75\2\36\1\75\4\36\2\75\7\36\1\75\11\36\1\75\1\36\1\75"+
        "\2\36\2\75\2\36\1\75\16\36\1\75\4\36\1\75\5\36\1\75\7\36\5\75\10"+
        "\36\1\75\5\36\1\75\3\36\1\u0117\1\36\2\uffff\6\36\1\75\2\36\3\75"+
        "\3\36\1\75\6\36\4\75\1\36\1\75\2\36\2\75\2\36\1\75\1\36\1\75\1\36"+
        "\1\75\3\36\2\75\1\36\1\75\5\36\1\75\5\36\4\75\1\36\2\75\1\36\1\75"+
        "\1\36\1\75\1\36\1\75\1\36\1\75\1\36\3\75\3\36\1\75\2\36\6\75\1\36"+
        "\4\75\5\36\2\75\2\36\2\75\1\36\2\75\1\u0117\2\36\4\75\1\u0117\2"+
        "\36\1\75\1\36\1\75\3\36\3\75\1\36\4\75\1\36\1\75\1\36\2\75\1\36"+
        "\6\75\2\36\1\75\1\36\1\75\1\36\2\75\2\36\1\75\1\36\4\75\1\36\2\75"+
        "\1\36\2\75\2\36\2\75\1\u0116\1\36\1\75\1\u0117\13\75\1\36\6\75\1"+
        "\36\1\75\1\36\2\75\1\36\1\u0117";
    static final String DFA9_eofS =
        "\u01de\uffff";
    static final String DFA9_minS =
        "\1\11\1\uffff\31\60\1\uffff\1\111\1\101\1\uffff\1\101\1\60\1\122"+
        "\1\114\1\124\1\60\1\102\6\60\1\uffff\1\122\1\131\1\101\1\126\1\125"+
        "\1\120\1\60\1\115\2\122\1\60\1\122\1\117\1\115\1\105\1\123\1\uffff"+
        "\2\114\1\126\1\127\1\60\1\110\1\104\2\120\1\122\1\105\1\122\1\131"+
        "\1\117\1\116\1\124\1\101\1\105\1\122\1\101\1\123\1\110\1\131\2\114"+
        "\1\105\1\120\2\114\1\131\1\117\1\116\1\124\1\116\2\117\1\114\1\101"+
        "\2\116\1\60\1\103\1\60\1\122\2\60\1\116\1\60\1\111\1\124\1\117\1"+
        "\60\1\101\1\60\1\115\1\60\1\104\1\60\1\105\1\127\2\105\1\122\1\123"+
        "\1\111\1\127\1\111\1\101\1\114\1\101\1\111\1\114\1\101\1\117\1\116"+
        "\11\60\1\uffff\1\124\1\11\1\60\1\122\1\60\1\105\1\uffff\2\105\2"+
        "\60\1\124\1\104\1\124\1\105\1\60\1\103\2\104\1\114\2\101\1\120\1"+
        "\105\2\60\1\103\1\123\1\60\1\123\1\115\1\103\1\105\1\122\1\105\1"+
        "\122\1\60\1\123\1\111\1\60\1\105\1\114\1\105\1\116\2\60\1\114\1"+
        "\101\1\131\1\105\1\115\1\114\1\105\1\60\1\116\1\123\1\105\1\104"+
        "\1\116\1\105\1\126\1\102\1\124\1\60\1\114\1\60\2\114\2\60\1\105"+
        "\1\101\1\60\1\114\1\104\1\123\1\105\1\125\1\120\1\114\1\117\1\105"+
        "\1\104\1\123\1\124\1\122\1\116\1\60\1\110\1\101\1\123\1\110\1\60"+
        "\1\101\1\105\1\103\1\116\1\124\1\60\1\115\1\111\1\120\2\107\1\105"+
        "\1\124\5\60\1\122\1\105\1\103\1\105\1\116\1\111\1\101\1\105\1\60"+
        "\1\127\1\114\1\124\1\105\1\113\1\60\1\122\2\104\1\60\1\101\2\uffff"+
        "\1\124\1\131\1\123\2\105\1\111\1\60\1\101\1\110\3\60\1\123\1\131"+
        "\1\125\1\60\1\105\1\114\1\105\1\124\1\105\1\117\4\60\1\123\1\60"+
        "\2\123\2\60\1\122\1\123\1\60\1\124\1\60\1\116\1\60\1\104\1\123\1"+
        "\124\2\60\1\105\1\60\1\116\1\105\1\122\1\110\1\104\1\60\1\117\1"+
        "\124\1\116\1\114\1\111\4\60\1\124\2\60\1\122\1\60\1\117\1\60\1\105"+
        "\1\60\1\124\1\60\1\104\3\60\1\131\1\123\1\105\1\60\1\101\1\124\6"+
        "\60\1\105\4\60\1\113\1\122\1\105\1\114\1\102\2\60\2\101\2\60\1\106"+
        "\4\60\1\115\5\60\1\126\1\125\1\60\1\123\1\60\1\123\1\106\1\105\3"+
        "\60\1\116\4\60\1\123\1\60\1\105\2\60\1\124\6\60\1\123\1\127\1\60"+
        "\1\104\1\60\1\116\2\60\1\127\1\105\1\60\1\105\4\60\1\105\2\60\1"+
        "\123\2\60\1\124\1\107\3\60\1\105\15\60\1\107\6\60\1\105\1\60\1\116"+
        "\2\60\1\124\1\60";
    static final String DFA9_maxS =
        "\1\132\1\uffff\1\116\1\126\1\132\1\131\1\132\1\122\1\132\1\127\1"+
        "\122\1\127\1\123\1\116\1\117\1\124\1\122\1\126\2\125\1\123\1\117"+
        "\2\132\1\125\1\71\1\132\1\uffff\1\111\1\132\1\uffff\1\124\1\132"+
        "\1\122\1\114\1\124\1\132\1\111\6\132\1\uffff\1\122\1\131\1\116\1"+
        "\126\1\125\1\127\1\132\3\122\1\132\1\122\1\117\1\126\1\124\1\123"+
        "\1\uffff\2\114\1\126\1\127\1\132\1\110\1\104\2\124\1\122\1\105\1"+
        "\122\1\131\1\117\1\116\1\124\1\105\1\117\1\122\1\101\1\123\1\110"+
        "\1\131\2\114\1\105\1\120\2\114\1\131\1\117\1\116\1\124\1\116\2\117"+
        "\1\132\1\127\1\125\1\116\1\132\1\103\1\132\1\124\2\132\1\116\1\132"+
        "\1\122\1\124\1\117\1\132\1\101\1\132\1\116\1\132\1\123\1\132\1\105"+
        "\1\127\1\116\1\105\1\122\1\123\1\111\1\127\1\111\1\116\1\114\1\123"+
        "\1\111\1\131\1\101\1\117\1\116\11\132\1\uffff\1\124\1\71\1\132\1"+
        "\122\1\132\1\105\1\uffff\2\105\2\132\1\124\1\104\1\124\1\105\1\132"+
        "\1\103\2\104\1\114\2\101\1\120\1\105\2\132\1\103\1\123\1\132\1\123"+
        "\1\115\1\103\1\105\1\122\1\105\1\122\1\132\2\123\1\132\1\105\1\114"+
        "\1\105\1\116\2\132\1\114\1\101\1\131\1\105\1\115\1\114\1\105\1\132"+
        "\1\116\1\123\1\105\1\104\1\116\1\105\1\126\1\102\1\124\1\132\1\114"+
        "\1\132\2\114\2\132\1\105\1\101\1\132\1\114\1\105\1\123\1\113\1\125"+
        "\1\120\1\114\1\117\1\105\1\104\1\123\1\124\1\122\1\116\1\132\1\110"+
        "\1\103\1\123\1\110\1\132\1\101\1\105\1\132\1\116\1\124\1\132\1\115"+
        "\1\131\1\120\2\107\1\105\1\124\5\132\1\122\1\105\1\103\1\105\1\116"+
        "\1\111\1\101\1\105\1\132\1\127\1\114\1\124\1\105\1\113\1\132\1\122"+
        "\2\104\2\132\2\uffff\1\124\1\131\1\123\1\131\1\105\1\111\1\132\1"+
        "\101\1\110\3\132\1\123\1\131\1\125\1\132\1\105\1\114\1\105\1\124"+
        "\1\105\1\117\4\132\1\123\1\132\2\123\2\132\1\122\1\123\1\132\1\124"+
        "\1\132\1\116\1\132\1\104\1\123\1\124\2\132\1\105\1\132\1\116\1\105"+
        "\1\122\1\110\1\104\1\132\1\117\1\124\1\116\1\114\1\111\4\132\1\124"+
        "\2\132\1\122\1\132\1\117\1\132\1\105\1\132\1\124\1\132\1\104\3\132"+
        "\1\131\1\123\1\105\1\132\1\101\1\124\6\132\1\105\4\132\1\113\1\122"+
        "\1\105\1\114\1\102\2\132\2\101\2\132\1\106\3\132\1\71\1\115\5\132"+
        "\1\126\1\125\1\132\1\123\1\132\1\123\1\106\1\105\3\132\1\116\4\132"+
        "\1\123\1\132\1\105\2\132\1\124\6\132\1\123\1\127\1\132\1\104\1\132"+
        "\1\116\2\132\1\127\1\105\1\132\1\105\4\132\1\105\2\132\1\123\2\132"+
        "\1\124\1\123\3\132\1\105\15\132\1\107\6\132\1\105\1\132\1\116\2"+
        "\132\1\124\1\132";
    static final String DFA9_acceptS =
        "\1\uffff\1\1\31\uffff\1\11\2\uffff\1\10\15\uffff\1\4\20\uffff\1"+
        "\3\124\uffff\1\7\6\uffff\1\5\174\uffff\1\6\1\2\u00c6\uffff";
    static final String DFA9_specialS =
        "\u01de\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\33\26\uffff\1\33\14\uffff\1\1\2\uffff\12\32\7\uffff\1\3\1"+
            "\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\31\1\15\1\16\1\17\1\27"+
            "\1\20\1\21\1\22\1\23\1\4\1\24\1\2\1\25\1\26\1\31\1\30\1\31",
            "",
            "\12\35\24\uffff\1\34",
            "\12\35\10\uffff\1\40\1\41\10\uffff\1\42\3\uffff\1\37\4\uffff"+
            "\1\43\1\44",
            "\12\35\7\uffff\4\36\1\46\5\36\1\53\3\36\1\52\1\36\1\47\2\36"+
            "\1\50\1\45\1\36\1\51\3\36",
            "\12\35\7\uffff\1\56\1\uffff\1\63\1\uffff\1\57\6\uffff\1\60"+
            "\2\uffff\1\61\5\uffff\1\55\3\uffff\1\62",
            "\12\35\7\uffff\1\64\2\36\1\74\1\66\2\36\1\67\1\70\2\36\1\71"+
            "\2\36\1\72\2\36\1\73\1\36\1\65\6\36",
            "\12\35\7\uffff\1\76\3\uffff\1\77\3\uffff\1\100\5\uffff\1\101"+
            "\2\uffff\1\102",
            "\12\35\7\uffff\2\36\1\103\12\36\1\104\4\36\1\105\4\36\1\106"+
            "\2\36",
            "\12\35\7\uffff\1\107\7\uffff\1\110\5\uffff\1\111\2\uffff\1"+
            "\113\4\uffff\1\112",
            "\12\35\7\uffff\1\115\2\uffff\1\114\7\uffff\1\116\5\uffff\1"+
            "\117",
            "\12\35\7\uffff\1\120\3\uffff\1\121\1\uffff\1\123\1\uffff\1"+
            "\125\5\uffff\1\126\4\uffff\1\122\2\uffff\1\124",
            "\12\35\22\uffff\1\127\1\130\1\131\4\uffff\1\132",
            "\12\35\13\uffff\1\133\10\uffff\1\134",
            "\12\35\7\uffff\1\135\7\uffff\1\137\1\uffff\1\140\1\uffff\1"+
            "\136\1\uffff\1\141",
            "\12\35\7\uffff\1\142\1\146\2\uffff\1\143\11\uffff\1\144\4\uffff"+
            "\1\145",
            "\12\35\24\uffff\1\150\3\uffff\1\147",
            "\12\35\7\uffff\1\151\3\uffff\1\161\3\uffff\1\154\1\uffff\1"+
            "\152\1\155\2\uffff\1\156\2\uffff\1\160\1\uffff\1\153\1\uffff"+
            "\1\157",
            "\12\35\11\uffff\1\163\21\uffff\1\162",
            "\12\35\7\uffff\1\164\2\uffff\1\167\2\uffff\1\165\1\uffff\1"+
            "\166\2\uffff\1\173\2\uffff\1\171\4\uffff\1\170\1\172",
            "\12\35\13\uffff\1\174\2\uffff\1\176\3\uffff\1\u0080\2\uffff"+
            "\1\177\2\uffff\1\u0081\1\175",
            "\12\35\7\uffff\1\u0082\7\uffff\1\u0083\5\uffff\1\u0084",
            "\12\35\7\uffff\1\u0085\6\36\1\u0086\6\36\1\u0087\11\36\1\u0088"+
            "\1\36",
            "\12\35\7\uffff\1\36\1\u008d\2\36\1\u0089\6\36\1\u008e\2\36"+
            "\1\u008b\3\36\1\u008c\1\u0090\1\u008f\1\36\1\u008a\3\36",
            "\12\35\33\uffff\1\u0091",
            "\12\35",
            "\12\32\7\uffff\32\36",
            "",
            "\1\u0093",
            "\32\u0094",
            "",
            "\1\u0096\16\uffff\1\u0097\3\uffff\1\u0095",
            "\12\36\7\uffff\1\36\1\u0098\30\36",
            "\1\u009a",
            "\1\u009b",
            "\1\u009c",
            "\12\36\7\uffff\4\36\1\u009d\25\36",
            "\1\u009f\6\uffff\1\u009e",
            "\12\36\7\uffff\15\36\1\u00a0\14\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "",
            "\1\u00a1",
            "\1\u00a2",
            "\1\u00a3\14\uffff\1\u00a4",
            "\1\u00a5",
            "\1\u00a6",
            "\1\u00a7\6\uffff\1\u00a8",
            "\12\36\7\uffff\32\36",
            "\1\u00a9\2\uffff\1\u00aa\1\uffff\1\u00ab",
            "\1\u00ac",
            "\1\u00ad",
            "\12\36\7\uffff\1\u00ae\31\36",
            "\1\u00af",
            "\1\u00b0",
            "\1\u00b1\1\u00b2\5\uffff\1\u00b3\1\u00b4\1\u00b5",
            "\1\u00b8\10\uffff\1\u00b6\1\u00b9\4\uffff\1\u00b7",
            "\1\u00ba",
            "",
            "\1\u00bb",
            "\1\u00bc",
            "\1\u00bd",
            "\1\u00be",
            "\12\36\7\uffff\32\36",
            "\1\u00bf",
            "\1\u00c0",
            "\1\u00c1\3\uffff\1\u00c2",
            "\1\u00c3\3\uffff\1\u00c4",
            "\1\u00c5",
            "\1\u00c6",
            "\1\u00c7",
            "\1\u00c8",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\1\u00cc\3\uffff\1\u00cd",
            "\1\u00ce\11\uffff\1\u00cf",
            "\1\u00d0",
            "\1\u00d1",
            "\1\u00d2",
            "\1\u00d3",
            "\1\u00d4",
            "\1\u00d5",
            "\1\u00d6",
            "\1\u00d7",
            "\1\u00d8",
            "\1\u00d9",
            "\1\u00da",
            "\1\u00db",
            "\1\u00dc",
            "\1\u00dd",
            "\1\u00de",
            "\1\u00df",
            "\1\u00e0",
            "\1\u00e1",
            "\1\u00e2\1\uffff\1\u00e3\13\uffff\1\u00e4",
            "\1\u00e5\25\uffff\1\u00e6",
            "\1\u00e7\1\u00e8\5\uffff\1\u00e9",
            "\1\u00ea",
            "\12\36\7\uffff\32\36",
            "\1\u00eb",
            "\12\36\7\uffff\32\36",
            "\1\u00ec\1\u00ed\1\u00ee",
            "\12\36\7\uffff\30\36\1\u00ef\1\36",
            "\12\36\7\uffff\26\36\1\u00f0\3\36",
            "\1\u00f1",
            "\12\36\7\uffff\1\u00f2\31\36",
            "\1\u00f3\10\uffff\1\u00f4",
            "\1\u00f5",
            "\1\u00f6",
            "\12\36\7\uffff\32\36",
            "\1\u00f7",
            "\12\36\7\uffff\32\36",
            "\1\u00f8\1\u00f9",
            "\12\36\7\uffff\32\36",
            "\1\u00fa\16\uffff\1\u00fb",
            "\12\36\7\uffff\17\36\1\u00fc\12\36",
            "\1\u00fd",
            "\1\u00fe",
            "\1\u00ff\10\uffff\1\u0100",
            "\1\u0101",
            "\1\u0102",
            "\1\u0103",
            "\1\u0104",
            "\1\u0105",
            "\1\u0106",
            "\1\u0107\14\uffff\1\u0108",
            "\1\u0109",
            "\1\u010a\3\uffff\1\u010b\6\uffff\1\u010c\6\uffff\1\u010d",
            "\1\u010e",
            "\1\u010f\14\uffff\1\u0110",
            "\1\u0111",
            "\1\u0112",
            "\1\u0113",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "",
            "\1\u0114",
            "\1\u0116\26\uffff\1\u0116\17\uffff\12\u0115",
            "\12\36\7\uffff\32\36",
            "\1\u0118",
            "\12\36\7\uffff\32\36",
            "\1\u0119",
            "",
            "\1\u011a",
            "\1\u011b",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u011c",
            "\1\u011d",
            "\1\u011e",
            "\1\u011f",
            "\12\36\7\uffff\32\36",
            "\1\u0120",
            "\1\u0121",
            "\1\u0122",
            "\1\u0123",
            "\1\u0124",
            "\1\u0125",
            "\1\u0126",
            "\1\u0127",
            "\12\36\7\uffff\21\36\1\u0128\10\36",
            "\12\36\7\uffff\32\36",
            "\1\u0129",
            "\1\u012a",
            "\12\36\7\uffff\2\36\1\u012b\27\36",
            "\1\u012c",
            "\1\u012d",
            "\1\u012e",
            "\1\u012f",
            "\1\u0130",
            "\1\u0131",
            "\1\u0132",
            "\12\36\7\uffff\32\36",
            "\1\u0133",
            "\1\u0134\11\uffff\1\u0135",
            "\12\36\7\uffff\32\36",
            "\1\u0136",
            "\1\u0137",
            "\1\u0138",
            "\1\u0139",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u013a",
            "\1\u013b",
            "\1\u013c",
            "\1\u013d",
            "\1\u013e",
            "\1\u013f",
            "\1\u0140",
            "\12\36\7\uffff\32\36",
            "\1\u0141",
            "\1\u0142",
            "\1\u0143",
            "\1\u0144",
            "\1\u0145",
            "\1\u0146",
            "\1\u0147",
            "\1\u0148",
            "\1\u0149",
            "\12\36\7\uffff\32\36",
            "\1\u014a",
            "\12\36\7\uffff\32\36",
            "\1\u014b",
            "\1\u014c",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u014d",
            "\1\u014e",
            "\12\36\7\uffff\32\36",
            "\1\u014f",
            "\1\u0150\1\u0151",
            "\1\u0152",
            "\1\u0153\5\uffff\1\u0154",
            "\1\u0155",
            "\1\u0156",
            "\1\u0157",
            "\1\u0158",
            "\1\u0159",
            "\1\u015a",
            "\1\u015b",
            "\1\u015c",
            "\1\u015d",
            "\1\u015e",
            "\12\36\7\uffff\32\36",
            "\1\u015f",
            "\1\u0160\1\uffff\1\u0161",
            "\1\u0162",
            "\1\u0163",
            "\12\36\7\uffff\32\36",
            "\1\u0164",
            "\1\u0165",
            "\1\u0166\20\uffff\1\u0167\5\uffff\1\u0168",
            "\1\u0169",
            "\1\u016a",
            "\12\36\7\uffff\32\36",
            "\1\u016b",
            "\1\u016c\17\uffff\1\u016d",
            "\1\u016e",
            "\1\u016f",
            "\1\u0170",
            "\1\u0171",
            "\1\u0172",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u0173",
            "\1\u0174",
            "\1\u0175",
            "\1\u0176",
            "\1\u0177",
            "\1\u0178",
            "\1\u0179",
            "\1\u017a",
            "\12\36\7\uffff\32\36",
            "\1\u017b",
            "\1\u017c",
            "\1\u017d",
            "\1\u017e",
            "\1\u017f",
            "\12\36\7\uffff\32\36",
            "\1\u0180",
            "\1\u0181",
            "\1\u0182",
            "\12\36\7\uffff\4\36\1\u0183\25\36",
            "\32\u0184",
            "",
            "",
            "\1\u0185",
            "\1\u0186",
            "\1\u0187",
            "\1\u0188\23\uffff\1\u0189",
            "\1\u018a",
            "\1\u018b",
            "\12\36\7\uffff\32\36",
            "\1\u018c",
            "\1\u018d",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u018e",
            "\1\u018f",
            "\1\u0190",
            "\12\36\7\uffff\32\36",
            "\1\u0191",
            "\1\u0192",
            "\1\u0193",
            "\1\u0194",
            "\1\u0195",
            "\1\u0196",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\22\36\1\u0197\7\36",
            "\12\36\7\uffff\32\36",
            "\1\u0198",
            "\12\36\7\uffff\32\36",
            "\1\u0199",
            "\1\u019a",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u019b",
            "\1\u019c",
            "\12\36\7\uffff\32\36",
            "\1\u019d",
            "\12\36\7\uffff\32\36",
            "\1\u019e",
            "\12\36\7\uffff\32\36",
            "\1\u019f",
            "\1\u01a0",
            "\1\u01a1",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01a2",
            "\12\36\7\uffff\32\36",
            "\1\u01a3",
            "\1\u01a4",
            "\1\u01a5",
            "\1\u01a6",
            "\1\u01a7",
            "\12\36\7\uffff\32\36",
            "\1\u01a8",
            "\1\u01a9",
            "\1\u01aa",
            "\1\u01ab",
            "\1\u01ac",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01ad",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01ae",
            "\12\36\7\uffff\32\36",
            "\1\u01af",
            "\12\36\7\uffff\32\36",
            "\1\u01b0",
            "\12\36\7\uffff\32\36",
            "\1\u01b1",
            "\12\36\7\uffff\32\36",
            "\1\u01b2",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01b3",
            "\1\u01b4",
            "\1\u01b5",
            "\12\36\7\uffff\32\36",
            "\1\u01b6",
            "\1\u01b7",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01b8",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01b9",
            "\1\u01ba",
            "\1\u01bb",
            "\1\u01bc",
            "\1\u01bd",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01be",
            "\1\u01bf",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01c0",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\u01c1",
            "\1\u01c2",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01c3",
            "\1\u01c4",
            "\12\36\7\uffff\32\36",
            "\1\u01c5",
            "\12\36\7\uffff\32\36",
            "\1\u01c6",
            "\1\u01c7",
            "\1\u01c8",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01c9",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01ca",
            "\12\36\7\uffff\32\36",
            "\1\u01cb",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01cc",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01cd",
            "\1\u01ce",
            "\12\36\7\uffff\32\36",
            "\1\u01cf",
            "\12\36\7\uffff\32\36",
            "\1\u01d0",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01d1",
            "\1\u01d2",
            "\12\36\7\uffff\32\36",
            "\1\u01d3",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01d4",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01d5",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01d6",
            "\1\u01d7\13\uffff\1\u01d8",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01d9",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01da",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01db",
            "\12\36\7\uffff\32\36",
            "\1\u01dc",
            "\12\36\7\uffff\32\36",
            "\12\36\7\uffff\32\36",
            "\1\u01dd",
            "\12\36\7\uffff\32\36"
    };

    static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
    static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
    static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
    static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
    static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
    static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
    static final short[][] DFA9_transition;

    static {
        int numStates = DFA9_transitionS.length;
        DFA9_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
        }
    }

    class DFA9 extends DFA {

        public DFA9(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 9;
            this.eot = DFA9_eot;
            this.eof = DFA9_eof;
            this.min = DFA9_min;
            this.max = DFA9_max;
            this.accept = DFA9_accept;
            this.special = DFA9_special;
            this.transition = DFA9_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__12 | SUITE | STREETTYPE | STREETDIR | PROVINCE | POSTALCODE | NUMBER | NAME | WS );";
        }
    }
 

}