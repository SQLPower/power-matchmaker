// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-11 15:35:17

package ca.sqlpower.matchmaker.address.parse;


import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class AddressLexer extends Lexer {
    public static final int STREETDIR=11;
    public static final int GD=6;
    public static final int NAME=14;
    public static final int T__16=16;
    public static final int WS=15;
    public static final int ROUTESERVICETYPE=4;
    public static final int SUFFIXANDDIR=9;
    public static final int NUMBER=13;
    public static final int NUMANDSUFFIX=12;
    public static final int LOCKBOXTYPE=5;
    public static final int EOF=-1;
    public static final int SUITE=8;
    public static final int DITYPE=7;
    public static final int STREETNUMSUFFIX=10;

    // delegates
    // delegators

    public AddressLexer() {;} 
    public AddressLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public AddressLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g"; }

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:7:7: ( '-' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:7:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "ROUTESERVICETYPE"
    public final void mROUTESERVICETYPE() throws RecognitionException {
        try {
            int _type = ROUTESERVICETYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:209:2: ( 'RR' | 'SS' | 'MR' )
            int alt1=3;
            switch ( input.LA(1) ) {
            case 'R':
                {
                alt1=1;
                }
                break;
            case 'S':
                {
                alt1=2;
                }
                break;
            case 'M':
                {
                alt1=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:209:4: 'RR'
                    {
                    match("RR"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:209:11: 'SS'
                    {
                    match("SS"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:209:18: 'MR'
                    {
                    match("MR"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ROUTESERVICETYPE"

    // $ANTLR start "LOCKBOXTYPE"
    public final void mLOCKBOXTYPE() throws RecognitionException {
        try {
            int _type = LOCKBOXTYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:212:2: ( 'PO BOX' | 'CP' )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='P') ) {
                alt2=1;
            }
            else if ( (LA2_0=='C') ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:212:4: 'PO BOX'
                    {
                    match("PO BOX"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:212:15: 'CP'
                    {
                    match("CP"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LOCKBOXTYPE"

    // $ANTLR start "GD"
    public final void mGD() throws RecognitionException {
        try {
            int _type = GD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:215:2: ( 'GD' | 'GENERAL DELIVERY' | 'PR' )
            int alt3=3;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='G') ) {
                int LA3_1 = input.LA(2);

                if ( (LA3_1=='D') ) {
                    alt3=1;
                }
                else if ( (LA3_1=='E') ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA3_0=='P') ) {
                alt3=3;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:215:4: 'GD'
                    {
                    match("GD"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:215:11: 'GENERAL DELIVERY'
                    {
                    match("GENERAL DELIVERY"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:215:32: 'PR'
                    {
                    match("PR"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GD"

    // $ANTLR start "DITYPE"
    public final void mDITYPE() throws RecognitionException {
        try {
            int _type = DITYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:2: ( 'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC' )
            int alt4=12;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:4: 'BDP'
                    {
                    match("BDP"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:12: 'CC'
                    {
                    match("CC"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:19: 'CDO'
                    {
                    match("CDO"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:27: 'CMC'
                    {
                    match("CMC"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:35: 'CPC'
                    {
                    match("CPC"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:43: 'CSP'
                    {
                    match("CSP"); 


                    }
                    break;
                case 7 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:51: 'LCD'
                    {
                    match("LCD"); 


                    }
                    break;
                case 8 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:59: 'PDF'
                    {
                    match("PDF"); 


                    }
                    break;
                case 9 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:67: 'PO'
                    {
                    match("PO"); 


                    }
                    break;
                case 10 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:74: 'RPO'
                    {
                    match("RPO"); 


                    }
                    break;
                case 11 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:82: 'STN'
                    {
                    match("STN"); 


                    }
                    break;
                case 12 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:90: 'SUCC'
                    {
                    match("SUCC"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DITYPE"

    // $ANTLR start "SUITE"
    public final void mSUITE() throws RecognitionException {
        try {
            int _type = SUITE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:7: ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' )
            int alt5=7;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:9: 'UNIT'
                    {
                    match("UNIT"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:18: 'APT'
                    {
                    match("APT"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:26: 'APARTMENT'
                    {
                    match("APARTMENT"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:40: 'SUITE'
                    {
                    match("SUITE"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:50: 'APP'
                    {
                    match("APP"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:58: 'BUREAU'
                    {
                    match("BUREAU"); 


                    }
                    break;
                case 7 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:69: 'UNITE'
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

    // $ANTLR start "SUFFIXANDDIR"
    public final void mSUFFIXANDDIR() throws RecognitionException {
        try {
            int _type = SUFFIXANDDIR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:2: ( 'N' | 'S' | 'E' | 'W' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:
            {
            if ( input.LA(1)=='E'||input.LA(1)=='N'||input.LA(1)=='S'||input.LA(1)=='W' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SUFFIXANDDIR"

    // $ANTLR start "STREETNUMSUFFIX"
    public final void mSTREETNUMSUFFIX() throws RecognitionException {
        try {
            int _type = STREETNUMSUFFIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:2: ( ( 'A' .. 'Z' | '1' .. '3' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:4: ( 'A' .. 'Z' | '1' .. '3' )
            {
            if ( (input.LA(1)>='1' && input.LA(1)<='3')||(input.LA(1)>='A' && input.LA(1)<='Z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STREETNUMSUFFIX"

    // $ANTLR start "STREETDIR"
    public final void mSTREETDIR() throws RecognitionException {
        try {
            int _type = STREETDIR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:229:2: ( 'NE' | 'NW' | 'NO' | 'SE' | 'SW' | 'SO' )
            int alt6=6;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='N') ) {
                switch ( input.LA(2) ) {
                case 'E':
                    {
                    alt6=1;
                    }
                    break;
                case 'W':
                    {
                    alt6=2;
                    }
                    break;
                case 'O':
                    {
                    alt6=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 1, input);

                    throw nvae;
                }

            }
            else if ( (LA6_0=='S') ) {
                switch ( input.LA(2) ) {
                case 'E':
                    {
                    alt6=4;
                    }
                    break;
                case 'W':
                    {
                    alt6=5;
                    }
                    break;
                case 'O':
                    {
                    alt6=6;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 2, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:229:4: 'NE'
                    {
                    match("NE"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:229:11: 'NW'
                    {
                    match("NW"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:229:18: 'NO'
                    {
                    match("NO"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:4: 'SE'
                    {
                    match("SE"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:11: 'SW'
                    {
                    match("SW"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:18: 'SO'
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

    // $ANTLR start "NUMANDSUFFIX"
    public final void mNUMANDSUFFIX() throws RecognitionException {
        try {
            int _type = NUMANDSUFFIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:233:2: ( ( '0' .. '9' )+ ( 'A' .. 'Z' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:233:4: ( '0' .. '9' )+ ( 'A' .. 'Z' )
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:233:4: ( '0' .. '9' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='0' && LA7_0<='9')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:233:5: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

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

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:233:16: ( 'A' .. 'Z' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:233:17: 'A' .. 'Z'
            {
            matchRange('A','Z'); 

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMANDSUFFIX"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:2: ( ( '0' .. '9' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:4: ( '0' .. '9' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:4: ( '0' .. '9' )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='0' && LA8_0<='9')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:4: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:238:6: ( ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:238:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:238:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0=='\''||LA9_0=='-'||(LA9_0>='0' && LA9_0<='9')||(LA9_0>='A' && LA9_0<='Z')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:
            	    {
            	    if ( input.LA(1)=='\''||input.LA(1)=='-'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:241:4: ( ( ' ' | '\\t' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:241:6: ( ' ' | '\\t' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:241:6: ( ' ' | '\\t' )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='\t'||LA10_0==' ') ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:
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
            	    if ( cnt10 >= 1 ) break loop10;
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
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
        // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:8: ( T__16 | ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME | WS )
        int alt11=13;
        alt11 = dfa11.predict(input);
        switch (alt11) {
            case 1 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:10: T__16
                {
                mT__16(); 

                }
                break;
            case 2 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:16: ROUTESERVICETYPE
                {
                mROUTESERVICETYPE(); 

                }
                break;
            case 3 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:33: LOCKBOXTYPE
                {
                mLOCKBOXTYPE(); 

                }
                break;
            case 4 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:45: GD
                {
                mGD(); 

                }
                break;
            case 5 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:48: DITYPE
                {
                mDITYPE(); 

                }
                break;
            case 6 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:55: SUITE
                {
                mSUITE(); 

                }
                break;
            case 7 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:61: SUFFIXANDDIR
                {
                mSUFFIXANDDIR(); 

                }
                break;
            case 8 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:74: STREETNUMSUFFIX
                {
                mSTREETNUMSUFFIX(); 

                }
                break;
            case 9 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:90: STREETDIR
                {
                mSTREETDIR(); 

                }
                break;
            case 10 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:100: NUMANDSUFFIX
                {
                mNUMANDSUFFIX(); 

                }
                break;
            case 11 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:113: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 12 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:120: NAME
                {
                mNAME(); 

                }
                break;
            case 13 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:125: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA5 dfa5 = new DFA5(this);
    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA4_eotS =
        "\20\uffff";
    static final String DFA4_eofS =
        "\20\uffff";
    static final String DFA4_minS =
        "\1\102\1\uffff\1\103\1\uffff\1\104\1\uffff\1\124\11\uffff";
    static final String DFA4_maxS =
        "\1\123\1\uffff\1\123\1\uffff\1\117\1\uffff\1\125\11\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\uffff\1\7\1\uffff\1\12\1\uffff\1\2\1\3\1\4\1\5\1"+
        "\6\1\10\1\11\1\13\1\14";
    static final String DFA4_specialS =
        "\20\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\1\1\2\10\uffff\1\3\3\uffff\1\4\1\uffff\1\5\1\6",
            "",
            "\1\7\1\10\10\uffff\1\11\2\uffff\1\12\2\uffff\1\13",
            "",
            "\1\14\12\uffff\1\15",
            "",
            "\1\16\1\17",
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
            return "217:1: DITYPE : ( 'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC' );";
        }
    }
    static final String DFA5_eotS =
        "\13\uffff\1\15\2\uffff";
    static final String DFA5_eofS =
        "\16\uffff";
    static final String DFA5_minS =
        "\1\101\1\116\1\120\2\uffff\1\111\1\101\1\124\3\uffff\1\105\2\uffff";
    static final String DFA5_maxS =
        "\1\125\1\116\1\120\2\uffff\1\111\2\124\3\uffff\1\105\2\uffff";
    static final String DFA5_acceptS =
        "\3\uffff\1\4\1\6\3\uffff\1\2\1\3\1\5\1\uffff\1\7\1\1";
    static final String DFA5_specialS =
        "\16\uffff}>";
    static final String[] DFA5_transitionS = {
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

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "220:1: SUITE : ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' );";
        }
    }
    static final String DFA11_eotS =
        "\1\uffff\1\23\1\26\1\35\10\26\1\35\1\26\1\35\1\26\1\62\3\uffff\1"+
        "\63\1\21\1\uffff\1\63\2\21\3\70\1\uffff\1\63\1\72\1\73\1\21\1\71"+
        "\1\72\3\21\1\73\6\21\3\70\1\111\2\uffff\2\72\2\21\4\uffff\5\72\1"+
        "\21\1\72\1\21\1\72\1\21\1\117\1\21\1\117\1\uffff\1\72\3\21\1\117"+
        "\1\uffff\1\21\1\117\2\21\1\117\2\21\1\117\4\21\1\117";
    static final String DFA11_eofS =
        "\135\uffff";
    static final String DFA11_minS =
        "\1\11\20\47\3\uffff\1\47\1\117\1\uffff\1\47\1\116\1\103\3\47\1\uffff"+
        "\1\47\1\40\1\47\1\106\2\47\1\117\1\103\1\120\1\47\1\116\1\120\1"+
        "\122\1\104\1\111\1\101\4\47\2\uffff\2\47\1\103\1\124\4\uffff\5\47"+
        "\1\105\1\47\1\105\1\47\1\124\1\47\1\122\1\47\1\uffff\1\47\1\105"+
        "\1\122\1\101\1\47\1\uffff\1\124\1\47\1\101\1\125\1\47\1\115\1\114"+
        "\1\47\1\105\1\40\1\116\1\124\1\47";
    static final String DFA11_maxS =
        "\21\132\3\uffff\1\132\1\117\1\uffff\1\132\1\116\1\111\3\132\1\uffff"+
        "\3\132\1\106\2\132\1\117\1\103\1\120\1\132\1\116\1\120\1\122\1\104"+
        "\1\111\1\124\4\132\2\uffff\2\132\1\103\1\124\4\uffff\5\132\1\105"+
        "\1\132\1\105\1\132\1\124\1\132\1\122\1\132\1\uffff\1\132\1\105\1"+
        "\122\1\101\1\132\1\uffff\1\124\1\132\1\101\1\125\1\132\1\115\1\114"+
        "\1\132\1\105\1\40\1\116\1\124\1\132";
    static final String DFA11_acceptS =
        "\21\uffff\1\14\1\15\1\1\2\uffff\1\10\6\uffff\1\7\24\uffff\1\13\1"+
        "\2\4\uffff\1\11\1\3\1\5\1\4\15\uffff\1\12\5\uffff\1\6\15\uffff";
    static final String DFA11_specialS =
        "\135\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\22\26\uffff\1\22\6\uffff\1\21\5\uffff\1\1\2\uffff\1\20\3"+
            "\15\6\20\7\uffff\1\13\1\10\1\6\1\17\1\16\1\17\1\7\4\17\1\11"+
            "\1\4\1\14\1\17\1\5\1\17\1\2\1\3\1\17\1\12\1\17\1\16\3\17",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\17\21\1\25\1\21\1"+
            "\24\10\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\4\21\1\32\11\21\1"+
            "\34\3\21\1\27\1\30\1\31\1\21\1\33\3\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\21\21\1\36\10\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\3\21\1\41\12\21\1"+
            "\37\2\21\1\40\10\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\2\21\1\43\1\44\10"+
            "\21\1\45\2\21\1\42\2\21\1\46\7\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\3\21\1\47\1\50\25"+
            "\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\3\21\1\51\20\21\1"+
            "\52\5\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\2\21\1\53\27\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\15\21\1\54\14\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\17\21\1\55\12\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\4\21\1\56\11\21\1"+
            "\60\7\21\1\57\3\21",
            "\1\21\5\uffff\1\21\2\uffff\12\20\7\uffff\32\61",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\20\7\uffff\32\61",
            "",
            "",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\64",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\65",
            "\1\66\5\uffff\1\67",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\71\6\uffff\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\74",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\2\21\1\75\27\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\105",
            "\1\107\16\uffff\1\110\3\uffff\1\106",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\112",
            "\1\113",
            "",
            "",
            "",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\114",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\115",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\116",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\120",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\121",
            "\1\122",
            "\1\123",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\4\21\1\124\25\21",
            "",
            "\1\125",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\126",
            "\1\127",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\130",
            "\1\131",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\132",
            "\1\73",
            "\1\133",
            "\1\134",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21"
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__16 | ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME | WS );";
        }
    }
 

}