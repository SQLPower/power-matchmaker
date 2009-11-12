// $ANTLR 3.2 Sep 23, 2009 12:02:23 /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-11-12 20:35:05

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
import org.apache.log4j.Logger;

public class AddressLexer extends Lexer {
    public static final int SUITEANDSTREETNUM=9;
    public static final int NAME=8;
    public static final int WS=10;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int NUMBER=7;
    public static final int NUMANDSUFFIX=6;
    public static final int NUMERICSTREETSUFFIX=5;
    public static final int EOF=-1;
    public static final int STREETNUMSUFFIX=4;


    private static final Logger logger = Logger.getLogger(AddressLexer.class);

    @Override
    public void emitErrorMessage(String msg) {
      logger.debug("ANTLR Error Message: " + msg);
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
    public String getGrammarFileName() { return "/Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g"; }

    // $ANTLR start "T__11"
    public final void mT__11() throws RecognitionException {
        try {
            int _type = T__11;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:18:7: ( '-' )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:18:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__11"

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:19:7: ( '#' )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:19:9: '#'
            {
            match('#'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "SUITEANDSTREETNUM"
    public final void mSUITEANDSTREETNUM() throws RecognitionException {
        try {
            int _type = SUITEANDSTREETNUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:405:2: ( ( '0' .. '9' )+ '-' ( '0' .. '9' )+ )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:405:4: ( '0' .. '9' )+ '-' ( '0' .. '9' )+
            {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:405:4: ( '0' .. '9' )+
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
            	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:405:5: '0' .. '9'
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
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:405:18: ( '0' .. '9' )+
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
            	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:405:19: '0' .. '9'
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

    // $ANTLR start "STREETNUMSUFFIX"
    public final void mSTREETNUMSUFFIX() throws RecognitionException {
        try {
            int _type = STREETNUMSUFFIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:408:2: ( ( 'A' .. 'Z' ) )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:408:4: ( 'A' .. 'Z' )
            {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:408:4: ( 'A' .. 'Z' )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:408:5: 'A' .. 'Z'
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

    // $ANTLR start "NUMERICSTREETSUFFIX"
    public final void mNUMERICSTREETSUFFIX() throws RecognitionException {
        try {
            int _type = NUMERICSTREETSUFFIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:411:2: ( ( '1/4' | '1/2' | '3/4' ) )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:411:4: ( '1/4' | '1/2' | '3/4' )
            {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:411:4: ( '1/4' | '1/2' | '3/4' )
            int alt3=3;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='1') ) {
                int LA3_1 = input.LA(2);

                if ( (LA3_1=='/') ) {
                    int LA3_3 = input.LA(3);

                    if ( (LA3_3=='4') ) {
                        alt3=1;
                    }
                    else if ( (LA3_3=='2') ) {
                        alt3=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 3, 3, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA3_0=='3') ) {
                alt3=3;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:411:5: '1/4'
                    {
                    match("1/4"); 


                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:411:11: '1/2'
                    {
                    match("1/2"); 


                    }
                    break;
                case 3 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:411:17: '3/4'
                    {
                    match("3/4"); 


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMERICSTREETSUFFIX"

    // $ANTLR start "NUMANDSUFFIX"
    public final void mNUMANDSUFFIX() throws RecognitionException {
        try {
            int _type = NUMANDSUFFIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:414:2: ( ( '0' .. '9' )+ ( 'A' .. 'Z' ) )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:414:4: ( '0' .. '9' )+ ( 'A' .. 'Z' )
            {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:414:4: ( '0' .. '9' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:414:5: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);

            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:414:16: ( 'A' .. 'Z' )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:414:17: 'A' .. 'Z'
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
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:417:2: ( ( '#' )? ( '0' .. '9' )+ )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:417:4: ( '#' )? ( '0' .. '9' )+
            {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:417:4: ( '#' )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='#') ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:417:4: '#'
                    {
                    match('#'); 

                    }
                    break;

            }

            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:417:8: ( '0' .. '9' )+
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
            	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:417:9: '0' .. '9'
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
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:419:6: ( ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' | '.' | '/' )+ )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:419:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' | '.' | '/' )+
            {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:419:8: ( 'A' .. 'Z' | '0' .. '9' | '\\'' | '-' | '.' | '/' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='\''||(LA7_0>='-' && LA7_0<='9')||(LA7_0>='A' && LA7_0<='Z')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:
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
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:422:4: ( ( ' ' | '\\t' )+ )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:422:6: ( ' ' | '\\t' )+
            {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:422:6: ( ' ' | '\\t' )+
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
            	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:
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
        // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:8: ( T__11 | T__12 | SUITEANDSTREETNUM | STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME | WS )
        int alt9=9;
        alt9 = dfa9.predict(input);
        switch (alt9) {
            case 1 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:10: T__11
                {
                mT__11(); 

                }
                break;
            case 2 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:16: T__12
                {
                mT__12(); 

                }
                break;
            case 3 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:22: SUITEANDSTREETNUM
                {
                mSUITEANDSTREETNUM(); 

                }
                break;
            case 4 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:40: STREETNUMSUFFIX
                {
                mSTREETNUMSUFFIX(); 

                }
                break;
            case 5 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:56: NUMERICSTREETSUFFIX
                {
                mNUMERICSTREETSUFFIX(); 

                }
                break;
            case 6 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:76: NUMANDSUFFIX
                {
                mNUMANDSUFFIX(); 

                }
                break;
            case 7 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:89: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 8 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:96: NAME
                {
                mNAME(); 

                }
                break;
            case 9 :
                // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:1:101: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA9 dfa9 = new DFA9(this);
    static final String DFA9_eotS =
        "\1\uffff\1\11\1\13\1\12\1\17\2\12\5\uffff\1\7\1\23\1\7\1\uffff\1"+
        "\7\2\26\1\uffff\1\27\1\26\2\uffff";
    static final String DFA9_eofS =
        "\30\uffff";
    static final String DFA9_minS =
        "\1\11\1\47\1\60\4\47\5\uffff\1\62\1\47\1\60\1\uffff\1\64\2\47\1"+
        "\uffff\2\47\2\uffff";
    static final String DFA9_maxS =
        "\2\132\1\71\4\132\5\uffff\1\64\1\132\1\71\1\uffff\1\64\2\132\1\uffff"+
        "\2\132\2\uffff";
    static final String DFA9_acceptS =
        "\7\uffff\1\10\1\11\1\1\1\7\1\2\3\uffff\1\4\3\uffff\1\6\2\uffff\1"+
        "\5\1\3";
    static final String DFA9_specialS =
        "\30\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\10\26\uffff\1\10\2\uffff\1\2\3\uffff\1\7\5\uffff\1\1\2\7"+
            "\1\6\1\3\1\6\1\5\6\6\7\uffff\32\4",
            "\1\7\5\uffff\15\7\7\uffff\32\7",
            "\12\12",
            "\1\7\5\uffff\1\16\1\7\1\14\12\6\7\uffff\32\15",
            "\1\7\5\uffff\15\7\7\uffff\32\7",
            "\1\7\5\uffff\1\16\1\7\1\20\12\6\7\uffff\32\15",
            "\1\7\5\uffff\1\16\2\7\12\6\7\uffff\32\15",
            "",
            "",
            "",
            "",
            "",
            "\1\22\1\uffff\1\21",
            "\1\7\5\uffff\15\7\7\uffff\32\7",
            "\12\24",
            "",
            "\1\25",
            "\1\7\5\uffff\15\7\7\uffff\32\7",
            "\1\7\5\uffff\15\7\7\uffff\32\7",
            "",
            "\1\7\5\uffff\3\7\12\24\7\uffff\32\7",
            "\1\7\5\uffff\15\7\7\uffff\32\7",
            "",
            ""
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
            return "1:1: Tokens : ( T__11 | T__12 | SUITEANDSTREETNUM | STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME | WS );";
        }
    }
 

}