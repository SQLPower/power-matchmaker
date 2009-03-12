// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-12 15:00:43

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
    public static final int SUITEANDSTREETNUM=16;
    public static final int STREETDIR=12;
    public static final int GD=6;
    public static final int NAME=15;
    public static final int WS=17;
    public static final int ROUTESERVICETYPE=4;
    public static final int T__18=18;
    public static final int SUFFIXANDDIR=9;
    public static final int NUMBER=14;
    public static final int NUMANDSTREETSUFFIX=11;
    public static final int NUMANDSUFFIX=13;
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

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
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
    // $ANTLR end "T__18"

    // $ANTLR start "SUITEANDSTREETNUM"
    public final void mSUITEANDSTREETNUM() throws RecognitionException {
        try {
            int _type = SUITEANDSTREETNUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:2: ( ( '0' .. '9' )+ '-' ( '0' .. '9' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:4: ( '0' .. '9' )+ '-' ( '0' .. '9' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:4: ( '0' .. '9' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:5: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);

            match('-'); 
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:18: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:19: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SUITEANDSTREETNUM"

    // $ANTLR start "ROUTESERVICETYPE"
    public final void mROUTESERVICETYPE() throws RecognitionException {
        try {
            int _type = ROUTESERVICETYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:239:2: ( 'RR' | 'SS' | 'MR' )
            int alt3=3;
            switch ( input.LA(1) ) {
            case 'R':
                {
                alt3=1;
                }
                break;
            case 'S':
                {
                alt3=2;
                }
                break;
            case 'M':
                {
                alt3=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:239:4: 'RR'
                    {
                    match("RR"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:239:11: 'SS'
                    {
                    match("SS"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:239:18: 'MR'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:242:2: ( 'PO BOX' | 'CP' )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='P') ) {
                alt4=1;
            }
            else if ( (LA4_0=='C') ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:242:4: 'PO BOX'
                    {
                    match("PO BOX"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:242:15: 'CP'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:245:2: ( 'GD' | 'GENERAL DELIVERY' | 'PR' )
            int alt5=3;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='G') ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1=='D') ) {
                    alt5=1;
                }
                else if ( (LA5_1=='E') ) {
                    alt5=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA5_0=='P') ) {
                alt5=3;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:245:4: 'GD'
                    {
                    match("GD"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:245:11: 'GENERAL DELIVERY'
                    {
                    match("GENERAL DELIVERY"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:245:32: 'PR'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:2: ( 'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC' )
            int alt6=12;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:4: 'BDP'
                    {
                    match("BDP"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:12: 'CC'
                    {
                    match("CC"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:19: 'CDO'
                    {
                    match("CDO"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:27: 'CMC'
                    {
                    match("CMC"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:35: 'CPC'
                    {
                    match("CPC"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:43: 'CSP'
                    {
                    match("CSP"); 


                    }
                    break;
                case 7 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:51: 'LCD'
                    {
                    match("LCD"); 


                    }
                    break;
                case 8 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:59: 'PDF'
                    {
                    match("PDF"); 


                    }
                    break;
                case 9 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:67: 'PO'
                    {
                    match("PO"); 


                    }
                    break;
                case 10 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:74: 'RPO'
                    {
                    match("RPO"); 


                    }
                    break;
                case 11 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:82: 'STN'
                    {
                    match("STN"); 


                    }
                    break;
                case 12 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:90: 'SUCC'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:250:7: ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' )
            int alt7=7;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:250:9: 'UNIT'
                    {
                    match("UNIT"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:250:18: 'APT'
                    {
                    match("APT"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:250:26: 'APARTMENT'
                    {
                    match("APARTMENT"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:250:40: 'SUITE'
                    {
                    match("SUITE"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:250:50: 'APP'
                    {
                    match("APP"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:250:58: 'BUREAU'
                    {
                    match("BUREAU"); 


                    }
                    break;
                case 7 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:250:69: 'UNITE'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:253:2: ( 'N' | 'S' | 'E' | 'W' )
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

    // $ANTLR start "NUMANDSTREETSUFFIX"
    public final void mNUMANDSTREETSUFFIX() throws RecognitionException {
        try {
            int _type = NUMANDSTREETSUFFIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:256:2: ( ( '1' .. '3' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:256:4: ( '1' .. '3' )
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:256:4: ( '1' .. '3' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:256:5: '1' .. '3'
            {
            matchRange('1','3'); 

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMANDSTREETSUFFIX"

    // $ANTLR start "STREETNUMSUFFIX"
    public final void mSTREETNUMSUFFIX() throws RecognitionException {
        try {
            int _type = STREETNUMSUFFIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:259:2: ( ( 'A' .. 'Z' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:259:4: ( 'A' .. 'Z' )
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:259:4: ( 'A' .. 'Z' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:259:5: 'A' .. 'Z'
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
    // $ANTLR end "STREETNUMSUFFIX"

    // $ANTLR start "STREETDIR"
    public final void mSTREETDIR() throws RecognitionException {
        try {
            int _type = STREETDIR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:262:2: ( 'NE' | 'NW' | 'NO' | 'SE' | 'SW' | 'SO' )
            int alt8=6;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='N') ) {
                switch ( input.LA(2) ) {
                case 'E':
                    {
                    alt8=1;
                    }
                    break;
                case 'W':
                    {
                    alt8=2;
                    }
                    break;
                case 'O':
                    {
                    alt8=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }

            }
            else if ( (LA8_0=='S') ) {
                switch ( input.LA(2) ) {
                case 'E':
                    {
                    alt8=4;
                    }
                    break;
                case 'W':
                    {
                    alt8=5;
                    }
                    break;
                case 'O':
                    {
                    alt8=6;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 2, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:262:4: 'NE'
                    {
                    match("NE"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:262:11: 'NW'
                    {
                    match("NW"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:262:18: 'NO'
                    {
                    match("NO"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:263:4: 'SE'
                    {
                    match("SE"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:263:11: 'SW'
                    {
                    match("SW"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:263:18: 'SO'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:2: ( ( '0' .. '9' )+ ( 'A' .. 'Z' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:4: ( '0' .. '9' )+ ( 'A' .. 'Z' )
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:4: ( '0' .. '9' )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='0' && LA9_0<='9')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:5: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

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

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:16: ( 'A' .. 'Z' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:17: 'A' .. 'Z'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:2: ( ( '0' .. '9' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:4: ( '0' .. '9' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:4: ( '0' .. '9' )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='0' && LA10_0<='9')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:4: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:271:6: ( ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:271:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:271:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0=='\''||LA11_0=='-'||(LA11_0>='0' && LA11_0<='9')||(LA11_0>='A' && LA11_0<='Z')) ) {
                    alt11=1;
                }


                switch (alt11) {
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
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:4: ( ( ' ' | '\\t' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:6: ( ' ' | '\\t' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:6: ( ' ' | '\\t' )+
            int cnt12=0;
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0=='\t'||LA12_0==' ') ) {
                    alt12=1;
                }


                switch (alt12) {
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
            	    if ( cnt12 >= 1 ) break loop12;
                        EarlyExitException eee =
                            new EarlyExitException(12, input);
                        throw eee;
                }
                cnt12++;
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
        // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:8: ( T__18 | SUITEANDSTREETNUM | ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | NUMANDSTREETSUFFIX | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME | WS )
        int alt13=15;
        alt13 = dfa13.predict(input);
        switch (alt13) {
            case 1 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:10: T__18
                {
                mT__18(); 

                }
                break;
            case 2 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:16: SUITEANDSTREETNUM
                {
                mSUITEANDSTREETNUM(); 

                }
                break;
            case 3 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:34: ROUTESERVICETYPE
                {
                mROUTESERVICETYPE(); 

                }
                break;
            case 4 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:51: LOCKBOXTYPE
                {
                mLOCKBOXTYPE(); 

                }
                break;
            case 5 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:63: GD
                {
                mGD(); 

                }
                break;
            case 6 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:66: DITYPE
                {
                mDITYPE(); 

                }
                break;
            case 7 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:73: SUITE
                {
                mSUITE(); 

                }
                break;
            case 8 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:79: SUFFIXANDDIR
                {
                mSUFFIXANDDIR(); 

                }
                break;
            case 9 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:92: NUMANDSTREETSUFFIX
                {
                mNUMANDSTREETSUFFIX(); 

                }
                break;
            case 10 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:111: STREETNUMSUFFIX
                {
                mSTREETNUMSUFFIX(); 

                }
                break;
            case 11 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:127: STREETDIR
                {
                mSTREETDIR(); 

                }
                break;
            case 12 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:137: NUMANDSUFFIX
                {
                mNUMANDSUFFIX(); 

                }
                break;
            case 13 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:150: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 14 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:157: NAME
                {
                mNAME(); 

                }
                break;
            case 15 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:162: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA6 dfa6 = new DFA6(this);
    protected DFA7 dfa7 = new DFA7(this);
    protected DFA13 dfa13 = new DFA13(this);
    static final String DFA6_eotS =
        "\20\uffff";
    static final String DFA6_eofS =
        "\20\uffff";
    static final String DFA6_minS =
        "\1\102\1\uffff\1\103\1\uffff\1\104\1\uffff\1\124\11\uffff";
    static final String DFA6_maxS =
        "\1\123\1\uffff\1\123\1\uffff\1\117\1\uffff\1\125\11\uffff";
    static final String DFA6_acceptS =
        "\1\uffff\1\1\1\uffff\1\7\1\uffff\1\12\1\uffff\1\2\1\3\1\4\1\5\1"+
        "\6\1\10\1\11\1\13\1\14";
    static final String DFA6_specialS =
        "\20\uffff}>";
    static final String[] DFA6_transitionS = {
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

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "247:1: DITYPE : ( 'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC' );";
        }
    }
    static final String DFA7_eotS =
        "\13\uffff\1\15\2\uffff";
    static final String DFA7_eofS =
        "\16\uffff";
    static final String DFA7_minS =
        "\1\101\1\116\1\120\2\uffff\1\111\1\101\1\124\3\uffff\1\105\2\uffff";
    static final String DFA7_maxS =
        "\1\125\1\116\1\120\2\uffff\1\111\2\124\3\uffff\1\105\2\uffff";
    static final String DFA7_acceptS =
        "\3\uffff\1\4\1\6\3\uffff\1\2\1\3\1\5\1\uffff\1\7\1\1";
    static final String DFA7_specialS =
        "\16\uffff}>";
    static final String[] DFA7_transitionS = {
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

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "250:1: SUITE : ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' );";
        }
    }
    static final String DFA13_eotS =
        "\1\uffff\1\23\1\24\1\31\1\40\10\31\1\40\1\64\1\31\1\40\4\uffff\1"+
        "\65\1\21\1\67\1\21\1\uffff\1\67\2\21\3\74\1\uffff\1\67\1\76\1\77"+
        "\1\21\1\75\1\76\3\21\1\77\6\21\3\74\2\uffff\1\115\1\uffff\2\76\2"+
        "\21\4\uffff\5\76\1\21\1\76\1\21\1\76\1\21\1\123\1\21\1\123\1\uffff"+
        "\1\76\3\21\1\123\1\uffff\1\21\1\123\2\21\1\123\2\21\1\123\4\21\1"+
        "\123";
    static final String DFA13_eofS =
        "\141\uffff";
    static final String DFA13_minS =
        "\1\11\20\47\4\uffff\1\47\1\60\1\47\1\117\1\uffff\1\47\1\116\1\103"+
        "\3\47\1\uffff\1\47\1\40\1\47\1\106\2\47\1\117\1\103\1\120\1\47\1"+
        "\116\1\120\1\122\1\104\1\111\1\101\3\47\2\uffff\1\47\1\uffff\2\47"+
        "\1\103\1\124\4\uffff\5\47\1\105\1\47\1\105\1\47\1\124\1\47\1\122"+
        "\1\47\1\uffff\1\47\1\105\1\122\1\101\1\47\1\uffff\1\124\1\47\1\101"+
        "\1\125\1\47\1\115\1\114\1\47\1\105\1\40\1\116\1\124\1\47";
    static final String DFA13_maxS =
        "\21\132\4\uffff\1\132\1\71\1\132\1\117\1\uffff\1\132\1\116\1\111"+
        "\3\132\1\uffff\3\132\1\106\2\132\1\117\1\103\1\120\1\132\1\116\1"+
        "\120\1\122\1\104\1\111\1\124\3\132\2\uffff\1\132\1\uffff\2\132\1"+
        "\103\1\124\4\uffff\5\132\1\105\1\132\1\105\1\132\1\124\1\132\1\122"+
        "\1\132\1\uffff\1\132\1\105\1\122\1\101\1\132\1\uffff\1\124\1\132"+
        "\1\101\1\125\1\132\1\115\1\114\1\132\1\105\1\40\1\116\1\124\1\132";
    static final String DFA13_acceptS =
        "\21\uffff\1\16\1\17\1\1\1\11\4\uffff\1\12\6\uffff\1\10\23\uffff"+
        "\1\15\1\14\1\uffff\1\3\4\uffff\1\13\1\4\1\6\1\5\15\uffff\1\2\5\uffff"+
        "\1\7\15\uffff";
    static final String DFA13_specialS =
        "\141\uffff}>";
    static final String[] DFA13_transitionS = {
            "\1\22\26\uffff\1\22\6\uffff\1\21\5\uffff\1\1\2\uffff\1\16\3"+
            "\2\6\16\7\uffff\1\14\1\11\1\7\1\17\1\20\1\17\1\10\4\17\1\12"+
            "\1\5\1\15\1\17\1\6\1\17\1\3\1\4\1\17\1\13\1\17\1\20\3\17",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\26\2\uffff\12\16\7\uffff\32\25",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\17\21\1\30\1\21\1"+
            "\27\10\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\4\21\1\35\11\21\1"+
            "\37\3\21\1\32\1\33\1\34\1\21\1\36\3\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\21\21\1\41\10\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\3\21\1\44\12\21\1"+
            "\42\2\21\1\43\10\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\2\21\1\46\1\47\10"+
            "\21\1\50\2\21\1\45\2\21\1\51\7\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\3\21\1\52\1\53\25"+
            "\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\3\21\1\54\20\21\1"+
            "\55\5\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\2\21\1\56\27\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\15\21\1\57\14\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\17\21\1\60\12\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\4\21\1\61\11\21\1"+
            "\63\7\21\1\62\3\21",
            "\1\21\5\uffff\1\26\2\uffff\12\16\7\uffff\32\25",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "",
            "",
            "",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\12\66",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\70",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\71",
            "\1\72\5\uffff\1\73",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\75\6\uffff\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\100",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\2\21\1\101\27\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\113\16\uffff\1\114\3\uffff\1\112",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\66\7\uffff\32\21",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\116",
            "\1\117",
            "",
            "",
            "",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\120",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\121",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\122",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\124",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\125",
            "\1\126",
            "\1\127",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\4\21\1\130\25\21",
            "",
            "\1\131",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\132",
            "\1\133",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\134",
            "\1\135",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21",
            "\1\136",
            "\1\77",
            "\1\137",
            "\1\140",
            "\1\21\5\uffff\1\21\2\uffff\12\21\7\uffff\32\21"
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__18 | SUITEANDSTREETNUM | ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | NUMANDSTREETSUFFIX | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME | WS );";
        }
    }
 

}