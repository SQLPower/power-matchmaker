// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-11 11:10:27

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
    public static final int GD=11;
    public static final int STREETDIR=9;
    public static final int NAME=10;
    public static final int WS=13;
    public static final int T__14=14;
    public static final int SUFFIXANDDIR=7;
    public static final int NUMBER=4;
    public static final int NUMANDSUFFIX=8;
    public static final int EOF=-1;
    public static final int SUITE=5;
    public static final int DITYPE=12;
    public static final int STREETNUMSUFFIX=6;

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

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
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
    // $ANTLR end "T__14"

    // $ANTLR start "GD"
    public final void mGD() throws RecognitionException {
        try {
            int _type = GD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:205:2: ( 'GD' | 'GENERAL DELIVERY' | 'PR' )
            int alt1=3;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='G') ) {
                int LA1_1 = input.LA(2);

                if ( (LA1_1=='D') ) {
                    alt1=1;
                }
                else if ( (LA1_1=='E') ) {
                    alt1=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA1_0=='P') ) {
                alt1=3;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:205:4: 'GD'
                    {
                    match("GD"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:205:11: 'GENERAL DELIVERY'
                    {
                    match("GENERAL DELIVERY"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:205:32: 'PR'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:2: ( 'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC' )
            int alt2=12;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:4: 'BDP'
                    {
                    match("BDP"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:12: 'CC'
                    {
                    match("CC"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:19: 'CDO'
                    {
                    match("CDO"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:27: 'CMC'
                    {
                    match("CMC"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:35: 'CPC'
                    {
                    match("CPC"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:43: 'CSP'
                    {
                    match("CSP"); 


                    }
                    break;
                case 7 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:51: 'LCD'
                    {
                    match("LCD"); 


                    }
                    break;
                case 8 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:59: 'PDF'
                    {
                    match("PDF"); 


                    }
                    break;
                case 9 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:67: 'PO'
                    {
                    match("PO"); 


                    }
                    break;
                case 10 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:74: 'RPO'
                    {
                    match("RPO"); 


                    }
                    break;
                case 11 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:82: 'STN'
                    {
                    match("STN"); 


                    }
                    break;
                case 12 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:90: 'SUCC'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:210:7: ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' )
            int alt3=7;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:210:9: 'UNIT'
                    {
                    match("UNIT"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:210:18: 'APT'
                    {
                    match("APT"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:210:26: 'APARTMENT'
                    {
                    match("APARTMENT"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:210:40: 'SUITE'
                    {
                    match("SUITE"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:210:50: 'APP'
                    {
                    match("APP"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:210:58: 'BUREAU'
                    {
                    match("BUREAU"); 


                    }
                    break;
                case 7 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:210:69: 'UNITE'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:213:2: ( 'N' | 'S' | 'E' | 'W' )
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:216:2: ( ( 'A' .. 'Z' | '1' .. '3' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:216:4: ( 'A' .. 'Z' | '1' .. '3' )
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:219:2: ( 'NE' | 'NW' | 'NO' | 'SE' | 'SW' | 'SO' )
            int alt4=6;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='N') ) {
                switch ( input.LA(2) ) {
                case 'E':
                    {
                    alt4=1;
                    }
                    break;
                case 'W':
                    {
                    alt4=2;
                    }
                    break;
                case 'O':
                    {
                    alt4=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;
                }

            }
            else if ( (LA4_0=='S') ) {
                switch ( input.LA(2) ) {
                case 'E':
                    {
                    alt4=4;
                    }
                    break;
                case 'W':
                    {
                    alt4=5;
                    }
                    break;
                case 'O':
                    {
                    alt4=6;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 2, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:219:4: 'NE'
                    {
                    match("NE"); 


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:219:11: 'NW'
                    {
                    match("NW"); 


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:219:18: 'NO'
                    {
                    match("NO"); 


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:4: 'SE'
                    {
                    match("SE"); 


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:11: 'SW'
                    {
                    match("SW"); 


                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:220:18: 'SO'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:2: ( ( '0' .. '9' )+ ( 'A' .. 'Z' ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:4: ( '0' .. '9' )+ ( 'A' .. 'Z' )
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:4: ( '0' .. '9' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:5: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:16: ( 'A' .. 'Z' )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:17: 'A' .. 'Z'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:2: ( ( '0' .. '9' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:4: ( '0' .. '9' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:4: ( '0' .. '9' )+
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
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:4: '0' .. '9'
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:228:6: ( ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:228:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:228:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='\''||LA7_0=='-'||(LA7_0>='0' && LA7_0<='9')||(LA7_0>='A' && LA7_0<='Z')) ) {
                    alt7=1;
                }


                switch (alt7) {
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
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:231:4: ( ( ' ' | '\\t' )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:231:6: ( ' ' | '\\t' )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:231:6: ( ' ' | '\\t' )+
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
        // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:8: ( T__14 | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME | WS )
        int alt9=11;
        alt9 = dfa9.predict(input);
        switch (alt9) {
            case 1 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:10: T__14
                {
                mT__14(); 

                }
                break;
            case 2 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:16: GD
                {
                mGD(); 

                }
                break;
            case 3 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:19: DITYPE
                {
                mDITYPE(); 

                }
                break;
            case 4 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:26: SUITE
                {
                mSUITE(); 

                }
                break;
            case 5 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:32: SUFFIXANDDIR
                {
                mSUFFIXANDDIR(); 

                }
                break;
            case 6 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:45: STREETNUMSUFFIX
                {
                mSTREETNUMSUFFIX(); 

                }
                break;
            case 7 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:61: STREETDIR
                {
                mSTREETDIR(); 

                }
                break;
            case 8 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:71: NUMANDSUFFIX
                {
                mNUMANDSUFFIX(); 

                }
                break;
            case 9 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:84: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 10 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:91: NAME
                {
                mNAME(); 

                }
                break;
            case 11 :
                // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:96: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA3 dfa3 = new DFA3(this);
    protected DFA9 dfa9 = new DFA9(this);
    static final String DFA2_eotS =
        "\20\uffff";
    static final String DFA2_eofS =
        "\20\uffff";
    static final String DFA2_minS =
        "\1\102\1\uffff\1\103\1\uffff\1\104\1\uffff\1\124\11\uffff";
    static final String DFA2_maxS =
        "\1\123\1\uffff\1\123\1\uffff\1\117\1\uffff\1\125\11\uffff";
    static final String DFA2_acceptS =
        "\1\uffff\1\1\1\uffff\1\7\1\uffff\1\12\1\uffff\1\2\1\3\1\4\1\5\1"+
        "\6\1\10\1\11\1\13\1\14";
    static final String DFA2_specialS =
        "\20\uffff}>";
    static final String[] DFA2_transitionS = {
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
            return "207:1: DITYPE : ( 'BDP' | 'CC' | 'CDO' | 'CMC' | 'CPC' | 'CSP' | 'LCD' | 'PDF' | 'PO' | 'RPO' | 'STN' | 'SUCC' );";
        }
    }
    static final String DFA3_eotS =
        "\13\uffff\1\15\2\uffff";
    static final String DFA3_eofS =
        "\16\uffff";
    static final String DFA3_minS =
        "\1\101\1\116\1\120\2\uffff\1\111\1\101\1\124\3\uffff\1\105\2\uffff";
    static final String DFA3_maxS =
        "\1\125\1\116\1\120\2\uffff\1\111\2\124\3\uffff\1\105\2\uffff";
    static final String DFA3_acceptS =
        "\3\uffff\1\4\1\6\3\uffff\1\2\1\3\1\5\1\uffff\1\7\1\1";
    static final String DFA3_specialS =
        "\16\uffff}>";
    static final String[] DFA3_transitionS = {
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
            return "210:1: SUITE : ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' | 'APP' | 'BUREAU' | 'UNITE' );";
        }
    }
    static final String DFA9_eotS =
        "\1\uffff\1\22\6\25\1\47\2\25\1\47\1\25\1\47\1\25\1\56\3\uffff\1"+
        "\57\1\20\1\uffff\1\57\1\20\1\62\2\20\1\62\10\20\3\76\1\uffff\2\20"+
        "\3\76\1\103\2\uffff\1\20\1\62\1\uffff\1\62\1\20\7\62\2\20\1\uffff"+
        "\1\20\1\111\1\20\1\111\1\uffff\2\20\1\62\1\20\1\111\1\uffff\3\20"+
        "\2\111\2\20\1\111\4\20\1\111";
    static final String DFA9_eofS =
        "\127\uffff";
    static final String DFA9_minS =
        "\1\11\17\47\3\uffff\1\47\1\116\1\uffff\1\47\1\106\1\47\1\120\1\122"+
        "\1\47\1\117\2\103\1\120\1\104\1\117\1\116\1\103\3\47\1\uffff\1\111"+
        "\1\101\4\47\2\uffff\1\105\1\47\1\uffff\1\47\1\105\7\47\1\103\1\124"+
        "\1\uffff\1\124\1\47\1\122\1\47\1\uffff\1\122\1\101\1\47\1\105\1"+
        "\47\1\uffff\1\124\1\101\1\125\2\47\1\115\1\114\1\47\1\105\1\40\1"+
        "\116\1\124\1\47";
    static final String DFA9_maxS =
        "\20\132\3\uffff\1\132\1\116\1\uffff\1\132\1\106\1\132\1\120\1\122"+
        "\1\132\1\117\2\103\1\120\1\104\1\117\1\116\1\111\3\132\1\uffff\1"+
        "\111\1\124\4\132\2\uffff\1\105\1\132\1\uffff\1\132\1\105\7\132\1"+
        "\103\1\124\1\uffff\1\124\1\132\1\122\1\132\1\uffff\1\122\1\101\1"+
        "\132\1\105\1\132\1\uffff\1\124\1\101\1\125\2\132\1\115\1\114\1\132"+
        "\1\105\1\40\1\116\1\124\1\132";
    static final String DFA9_acceptS =
        "\20\uffff\1\12\1\13\1\1\2\uffff\1\6\21\uffff\1\5\6\uffff\1\11\1"+
        "\2\2\uffff\1\3\13\uffff\1\7\4\uffff\1\10\5\uffff\1\4\15\uffff";
    static final String DFA9_specialS =
        "\127\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\21\26\uffff\1\21\6\uffff\1\20\5\uffff\1\1\2\uffff\1\17\3"+
            "\14\6\17\7\uffff\1\12\1\4\1\5\1\16\1\15\1\16\1\2\4\16\1\6\1"+
            "\16\1\13\1\16\1\3\1\16\1\7\1\10\1\16\1\11\1\16\1\15\3\16",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\3\20\1\23\1\24\25"+
            "\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\3\20\1\27\12\20\1"+
            "\30\2\20\1\26\10\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\3\20\1\31\20\20\1"+
            "\32\5\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\2\20\1\33\1\34\10"+
            "\20\1\35\2\20\1\36\2\20\1\37\7\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\2\20\1\40\27\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\17\20\1\41\12\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\4\20\1\44\11\20\1"+
            "\46\4\20\1\42\1\43\1\20\1\45\3\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\15\20\1\50\14\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\17\20\1\51\12\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\4\20\1\52\11\20\1"+
            "\54\7\20\1\53\3\20",
            "\1\20\5\uffff\1\20\2\uffff\12\17\7\uffff\32\55",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\17\7\uffff\32\55",
            "",
            "",
            "",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\60",
            "",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\61",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\63",
            "\1\64",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\73",
            "\1\74\5\uffff\1\75",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "",
            "\1\77",
            "\1\101\16\uffff\1\102\3\uffff\1\100",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "",
            "",
            "\1\104",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\105",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\106",
            "\1\107",
            "",
            "\1\110",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\112",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "",
            "\1\113",
            "\1\114",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\115",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\4\20\1\116\25\20",
            "",
            "\1\117",
            "\1\120",
            "\1\121",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\122",
            "\1\123",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20",
            "\1\124",
            "\1\57",
            "\1\125",
            "\1\126",
            "\1\20\5\uffff\1\20\2\uffff\12\20\7\uffff\32\20"
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
            return "1:1: Tokens : ( T__14 | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME | WS );";
        }
    }
 

}