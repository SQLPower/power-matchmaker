// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-13 11:08:33

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
    public static final int SUITEANDSTREETNUM=14;
    public static final int STREETDIR=10;
    public static final int NAME=13;
    public static final int T__16=16;
    public static final int WS=15;
    public static final int ROUTESERVICETYPE=4;
    public static final int T__17=17;
    public static final int SUFFIXANDDIR=7;
    public static final int NUMBER=12;
    public static final int NUMANDSTREETSUFFIX=9;
    public static final int NUMANDSUFFIX=11;
    public static final int EOF=-1;
    public static final int SUITE=6;
    public static final int DITYPE=5;
    public static final int STREETNUMSUFFIX=8;

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

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:8:7: ( '#' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:8:9: '#'
            {
            match('#'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "SUITEANDSTREETNUM"
    public final void mSUITEANDSTREETNUM() throws RecognitionException {
        try {
            int _type = SUITEANDSTREETNUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:2: ( ( '0' .. '9' )+ '-' ( '0' .. '9' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:4: ( '0' .. '9' )+ '-' ( '0' .. '9' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:4: ( '0' .. '9' )+
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
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:5: '0' .. '9'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:18: ( '0' .. '9' )+
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
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:266:19: '0' .. '9'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:2: ( 'RR' | 'SS' | 'MR' )
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:4: 'RR'
                    {
                    match("RR"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:11: 'SS'
                    {
                    match("SS"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:18: 'MR'
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

    // $ANTLR start "DITYPE"
    public final void mDITYPE() throws RecognitionException {
        try {
            int _type = DITYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:2: ( 'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC' )
            int alt4=12;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:4: 'BDP'
                    {
                    match("BDP"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:12: 'CC'
                    {
                    match("CC"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:19: 'CDO'
                    {
                    match("CDO"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:27: 'CMC'
                    {
                    match("CMC"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:35: 'CPC'
                    {
                    match("CPC"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:43: 'CSP'
                    {
                    match("CSP"); 


                    }
                    break;
                case 7 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:51: 'LCD'
                    {
                    match("LCD"); 


                    }
                    break;
                case 8 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:59: 'PDF'
                    {
                    match("PDF"); 


                    }
                    break;
                case 9 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:67: 'PO'
                    {
                    match("PO"); 


                    }
                    break;
                case 10 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:74: 'RPO'
                    {
                    match("RPO"); 


                    }
                    break;
                case 11 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:82: 'STN'
                    {
                    match("STN"); 


                    }
                    break;
                case 12 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:90: 'SUCC'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:7: ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' )
            int alt5=7;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:9: 'UNIT'
                    {
                    match("UNIT"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:18: 'APT'
                    {
                    match("APT"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:26: 'APARTMENT'
                    {
                    match("APARTMENT"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:40: 'SUITE'
                    {
                    match("SUITE"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:50: 'APP'
                    {
                    match("APP"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:58: 'BUREAU'
                    {
                    match("BUREAU"); 


                    }
                    break;
                case 7 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:69: 'UNITE'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:277:2: ( 'N' | 'S' | 'E' | 'W' )
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:280:2: ( ( '1' .. '3' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:280:4: ( '1' .. '3' )
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:280:4: ( '1' .. '3' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:280:5: '1' .. '3'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:2: ( ( 'A' .. 'Z' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:4: ( 'A' .. 'Z' )
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:4: ( 'A' .. 'Z' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:5: 'A' .. 'Z'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:286:2: ( 'NE' | 'NW' | 'NO' | 'SE' | 'SW' | 'SO' )
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:286:4: 'NE'
                    {
                    match("NE"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:286:11: 'NW'
                    {
                    match("NW"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:286:18: 'NO'
                    {
                    match("NO"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:287:4: 'SE'
                    {
                    match("SE"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:287:11: 'SW'
                    {
                    match("SW"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:287:18: 'SO'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:290:2: ( ( '0' .. '9' )+ ( 'A' .. 'Z' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:290:4: ( '0' .. '9' )+ ( 'A' .. 'Z' )
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:290:4: ( '0' .. '9' )+
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
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:290:5: '0' .. '9'
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

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:290:16: ( 'A' .. 'Z' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:290:17: 'A' .. 'Z'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:293:2: ( ( '#' )? ( '0' .. '9' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:293:4: ( '#' )? ( '0' .. '9' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:293:4: ( '#' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='#') ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:293:4: '#'
                    {
                    match('#'); 

                    }
                    break;

            }

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:293:8: ( '0' .. '9' )+
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
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:293:9: '0' .. '9'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:295:6: ( ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' | '.' | '/' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:295:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' | '.' | '/' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:295:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' | '.' | '/' )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='\''||(LA10_0>='-' && LA10_0<='9')||(LA10_0>='A' && LA10_0<='Z')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:
            	    {
            	    if ( input.LA(1)=='\''||(input.LA(1)>='-' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z') ) {
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:298:4: ( ( ' ' | '\\t' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:298:6: ( ' ' | '\\t' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:298:6: ( ' ' | '\\t' )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0=='\t'||LA11_0==' ') ) {
                    alt11=1;
                }


                switch (alt11) {
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
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
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
        // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:8: ( T__16 | T__17 | SUITEANDSTREETNUM | ROUTESERVICETYPE | DITYPE | SUITE | SUFFIXANDDIR | NUMANDSTREETSUFFIX | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME | WS )
        int alt12=14;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:10: T__16
                {
                mT__16(); 

                }
                break;
            case 2 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:16: T__17
                {
                mT__17(); 

                }
                break;
            case 3 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:22: SUITEANDSTREETNUM
                {
                mSUITEANDSTREETNUM(); 

                }
                break;
            case 4 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:40: ROUTESERVICETYPE
                {
                mROUTESERVICETYPE(); 

                }
                break;
            case 5 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:57: DITYPE
                {
                mDITYPE(); 

                }
                break;
            case 6 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:64: SUITE
                {
                mSUITE(); 

                }
                break;
            case 7 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:70: SUFFIXANDDIR
                {
                mSUFFIXANDDIR(); 

                }
                break;
            case 8 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:83: NUMANDSTREETSUFFIX
                {
                mNUMANDSTREETSUFFIX(); 

                }
                break;
            case 9 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:102: STREETNUMSUFFIX
                {
                mSTREETNUMSUFFIX(); 

                }
                break;
            case 10 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:118: STREETDIR
                {
                mSTREETDIR(); 

                }
                break;
            case 11 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:128: NUMANDSUFFIX
                {
                mNUMANDSUFFIX(); 

                }
                break;
            case 12 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:141: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 13 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:148: NAME
                {
                mNAME(); 

                }
                break;
            case 14 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:153: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA5 dfa5 = new DFA5(this);
    protected DFA12 dfa12 = new DFA12(this);
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
            return "271:1: DITYPE : ( 'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC' );";
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
            return "274:1: SUITE : ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' );";
        }
    }
    static final String DFA12_eotS =
        "\1\uffff\1\23\1\25\1\26\1\33\1\42\7\33\1\42\1\24\1\33\1\42\6\uffff"+
        "\1\21\1\64\1\65\1\21\1\uffff\1\65\2\21\3\72\1\uffff\1\65\2\21\1"+
        "\75\6\21\1\75\2\21\3\72\1\110\2\uffff\2\75\2\21\1\uffff\1\75\1\21"+
        "\1\uffff\6\75\1\21\1\115\1\21\1\115\1\uffff\1\75\2\21\1\115\1\uffff"+
        "\1\21\1\115\1\21\1\115\1\21\1\115\3\21\1\115";
    static final String DFA12_eofS =
        "\130\uffff";
    static final String DFA12_minS =
        "\1\11\1\47\1\60\16\47\6\uffff\1\60\2\47\1\117\1\uffff\1\47\1\116"+
        "\1\103\3\47\1\uffff\1\47\1\120\1\122\1\47\1\117\2\103\1\120\1\104"+
        "\1\106\1\47\1\111\1\101\4\47\2\uffff\2\47\1\103\1\124\1\uffff\1"+
        "\47\1\105\1\uffff\6\47\1\124\1\47\1\122\1\47\1\uffff\1\47\1\105"+
        "\1\101\1\47\1\uffff\1\124\1\47\1\125\1\47\1\115\1\47\1\105\1\116"+
        "\1\124\1\47";
    static final String DFA12_maxS =
        "\2\132\1\71\16\132\6\uffff\1\71\2\132\1\117\1\uffff\1\132\1\116"+
        "\1\111\3\132\1\uffff\1\132\1\120\1\122\1\132\1\117\2\103\1\120\1"+
        "\104\1\106\1\132\1\111\1\124\4\132\2\uffff\2\132\1\103\1\124\1\uffff"+
        "\1\132\1\105\1\uffff\6\132\1\124\1\132\1\122\1\132\1\uffff\1\132"+
        "\1\105\1\101\1\132\1\uffff\1\124\1\132\1\125\1\132\1\115\1\132\1"+
        "\105\1\116\1\124\1\132";
    static final String DFA12_acceptS =
        "\21\uffff\1\15\1\16\1\1\1\14\1\2\1\10\4\uffff\1\11\6\uffff\1\7\21"+
        "\uffff\1\13\1\4\4\uffff\1\12\2\uffff\1\5\12\uffff\1\3\4\uffff\1"+
        "\6\12\uffff";
    static final String DFA12_specialS =
        "\130\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\22\26\uffff\1\22\2\uffff\1\2\3\uffff\1\21\5\uffff\1\1\2\21"+
            "\1\16\3\3\6\16\7\uffff\1\14\1\7\1\10\1\17\1\20\6\17\1\11\1\6"+
            "\1\15\1\17\1\12\1\17\1\4\1\5\1\17\1\13\1\17\1\20\3\17",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\12\24",
            "\1\21\5\uffff\1\27\2\21\12\16\7\uffff\32\30",
            "\1\21\5\uffff\15\21\7\uffff\17\21\1\32\1\21\1\31\10\21",
            "\1\21\5\uffff\15\21\7\uffff\4\21\1\37\11\21\1\41\3\21\1\34"+
            "\1\35\1\36\1\21\1\40\3\21",
            "\1\21\5\uffff\15\21\7\uffff\21\21\1\43\10\21",
            "\1\21\5\uffff\15\21\7\uffff\3\21\1\44\20\21\1\45\5\21",
            "\1\21\5\uffff\15\21\7\uffff\2\21\1\46\1\47\10\21\1\50\2\21"+
            "\1\51\2\21\1\52\7\21",
            "\1\21\5\uffff\15\21\7\uffff\2\21\1\53\27\21",
            "\1\21\5\uffff\15\21\7\uffff\3\21\1\54\12\21\1\55\13\21",
            "\1\21\5\uffff\15\21\7\uffff\15\21\1\56\14\21",
            "\1\21\5\uffff\15\21\7\uffff\17\21\1\57\12\21",
            "\1\21\5\uffff\15\21\7\uffff\4\21\1\60\11\21\1\62\7\21\1\61"+
            "\3\21",
            "\1\21\5\uffff\1\27\2\21\12\16\7\uffff\32\30",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\63",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\66",
            "",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\67",
            "\1\70\5\uffff\1\71",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\73",
            "\1\74",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\104",
            "\1\106\16\uffff\1\107\3\uffff\1\105",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\3\21\12\63\7\uffff\32\21",
            "",
            "",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\111",
            "\1\112",
            "",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\113",
            "",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\114",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\116",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\117",
            "\1\120",
            "\1\21\5\uffff\15\21\7\uffff\4\21\1\121\25\21",
            "",
            "\1\122",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\123",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\124",
            "\1\21\5\uffff\15\21\7\uffff\32\21",
            "\1\125",
            "\1\126",
            "\1\127",
            "\1\21\5\uffff\15\21\7\uffff\32\21"
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__16 | T__17 | SUITEANDSTREETNUM | ROUTESERVICETYPE | DITYPE | SUITE | SUFFIXANDDIR | NUMANDSTREETSUFFIX | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME | WS );";
        }
    }
 

}