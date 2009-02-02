package ca.sqlpower.matchmaker.address.parse;

// $ANTLR 3.1.1 /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-02-02 14:28:55

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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:3:7: ( '-' )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:3:9: '-'
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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:77:7: ( 'UNIT' | 'APT' | 'APARTMENT' | 'SUITE' )
            int alt1=4;
            switch ( input.LA(1) ) {
            case 'U':
                {
                alt1=1;
                }
                break;
            case 'A':
                {
                int LA1_2 = input.LA(2);

                if ( (LA1_2=='P') ) {
                    int LA1_4 = input.LA(3);

                    if ( (LA1_4=='T') ) {
                        alt1=2;
                    }
                    else if ( (LA1_4=='A') ) {
                        alt1=3;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 1, 4, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 2, input);

                    throw nvae;
                }
                }
                break;
            case 'S':
                {
                alt1=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:77:9: 'UNIT'
                    {
                    match("UNIT"); 


                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:77:18: 'APT'
                    {
                    match("APT"); 


                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:77:26: 'APARTMENT'
                    {
                    match("APARTMENT"); 


                    }
                    break;
                case 4 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:77:40: 'SUITE'
                    {
                    match("SUITE"); 


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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:2: ( 'AVE' | 'BLVD' | 'CR' | 'CRT' | 'PKY' | 'RD' | 'ST' | 'TERR' | 'WAY' )
            int alt2=9;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:4: 'AVE'
                    {
                    match("AVE"); 


                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:12: 'BLVD'
                    {
                    match("BLVD"); 


                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:21: 'CR'
                    {
                    match("CR"); 


                    }
                    break;
                case 4 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:28: 'CRT'
                    {
                    match("CRT"); 


                    }
                    break;
                case 5 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:36: 'PKY'
                    {
                    match("PKY"); 


                    }
                    break;
                case 6 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:44: 'RD'
                    {
                    match("RD"); 


                    }
                    break;
                case 7 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:51: 'ST'
                    {
                    match("ST"); 


                    }
                    break;
                case 8 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:58: 'TERR'
                    {
                    match("TERR"); 


                    }
                    break;
                case 9 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:80:67: 'WAY'
                    {
                    match("WAY"); 


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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:84:2: ( 'N' | 'S' | 'E' | 'W' | 'NE' | 'NW' | 'NO' | 'SE' | 'SW' | 'SO' )
            int alt3=10;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:84:4: 'N'
                    {
                    match('N'); 

                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:84:10: 'S'
                    {
                    match('S'); 

                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:84:16: 'E'
                    {
                    match('E'); 

                    }
                    break;
                case 4 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:84:22: 'W'
                    {
                    match('W'); 

                    }
                    break;
                case 5 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:85:4: 'NE'
                    {
                    match("NE"); 


                    }
                    break;
                case 6 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:85:11: 'NW'
                    {
                    match("NW"); 


                    }
                    break;
                case 7 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:85:18: 'NO'
                    {
                    match("NO"); 


                    }
                    break;
                case 8 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:86:4: 'SE'
                    {
                    match("SE"); 


                    }
                    break;
                case 9 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:86:11: 'SW'
                    {
                    match("SW"); 


                    }
                    break;
                case 10 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:86:18: 'SO'
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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:9: ( 'BC' | 'AB' | 'SK' | 'MB' | 'ON' | 'QC' | 'NS' | 'NB' | 'PE' | 'NL' | 'NU' | 'NT' | 'YU' )
            int alt4=13;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:11: 'BC'
                    {
                    match("BC"); 


                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:18: 'AB'
                    {
                    match("AB"); 


                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:25: 'SK'
                    {
                    match("SK"); 


                    }
                    break;
                case 4 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:32: 'MB'
                    {
                    match("MB"); 


                    }
                    break;
                case 5 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:39: 'ON'
                    {
                    match("ON"); 


                    }
                    break;
                case 6 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:46: 'QC'
                    {
                    match("QC"); 


                    }
                    break;
                case 7 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:53: 'NS'
                    {
                    match("NS"); 


                    }
                    break;
                case 8 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:60: 'NB'
                    {
                    match("NB"); 


                    }
                    break;
                case 9 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:67: 'PE'
                    {
                    match("PE"); 


                    }
                    break;
                case 10 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:74: 'NL'
                    {
                    match("NL"); 


                    }
                    break;
                case 11 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:81: 'NU'
                    {
                    match("NU"); 


                    }
                    break;
                case 12 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:88: 'NT'
                    {
                    match("NT"); 


                    }
                    break;
                case 13 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:88:95: 'YU'
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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:91:2: ( 'A' .. 'Z' '0' .. '9' 'A' .. 'Z' ( WS )* '0' .. '9' 'A' .. 'Z' '0' .. '9' )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:91:4: 'A' .. 'Z' '0' .. '9' 'A' .. 'Z' ( WS )* '0' .. '9' 'A' .. 'Z' '0' .. '9'
            {
            matchRange('A','Z'); 
            matchRange('0','9'); 
            matchRange('A','Z'); 
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:91:31: ( WS )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\t'||LA5_0==' ') ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:91:31: WS
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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:94:2: ( ( '0' .. '9' )+ )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:94:4: ( '0' .. '9' )+
            {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:94:4: ( '0' .. '9' )+
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
            	    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:94:4: '0' .. '9'
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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:96:6: ( ( 'A' .. 'Z' | '0' .. '9' )+ )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:96:8: ( 'A' .. 'Z' | '0' .. '9' )+
            {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:96:8: ( 'A' .. 'Z' | '0' .. '9' )+
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
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:99:4: ( ( ' ' | '\\t' )+ )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:99:6: ( ' ' | '\\t' )+
            {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:99:6: ( ' ' | '\\t' )+
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


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA3 dfa3 = new DFA3(this);
    protected DFA4 dfa4 = new DFA4(this);
    protected DFA9 dfa9 = new DFA9(this);
    static final String DFA2_eotS =
        "\11\uffff\1\13\2\uffff";
    static final String DFA2_eofS =
        "\14\uffff";
    static final String DFA2_minS =
        "\1\101\2\uffff\1\122\5\uffff\1\124\2\uffff";
    static final String DFA2_maxS =
        "\1\127\2\uffff\1\122\5\uffff\1\124\2\uffff";
    static final String DFA2_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\5\1\6\1\7\1\10\1\11\1\uffff\1\4\1\3";
    static final String DFA2_specialS =
        "\14\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\1\1\2\1\3\14\uffff\1\4\1\uffff\1\5\1\6\1\7\2\uffff\1\10",
            "",
            "",
            "\1\11",
            "",
            "",
            "",
            "",
            "",
            "\1\12",
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
            return "79:1: STREETTYPE : ( 'AVE' | 'BLVD' | 'CR' | 'CRT' | 'PKY' | 'RD' | 'ST' | 'TERR' | 'WAY' );";
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
            return "83:1: STREETDIR : ( 'N' | 'S' | 'E' | 'W' | 'NE' | 'NW' | 'NO' | 'SE' | 'SW' | 'SO' );";
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
            return "88:1: PROVINCE : ( 'BC' | 'AB' | 'SK' | 'MB' | 'ON' | 'QC' | 'NS' | 'NB' | 'PE' | 'NL' | 'NU' | 'NT' | 'YU' );";
        }
    }
    static final String DFA9_eotS =
        "\2\uffff\2\26\1\40\5\26\3\40\5\26\1\65\1\uffff\2\26\1\uffff\2\26"+
        "\1\73\1\26\1\75\3\40\1\73\1\uffff\1\26\1\73\1\75\1\26\1\73\1\75"+
        "\2\26\3\40\11\73\1\uffff\2\26\1\106\1\26\1\75\1\uffff\1\26\1\uffff"+
        "\1\26\2\75\1\26\1\75\1\106\1\26\2\uffff\2\26\2\75\2\26\1\106\1\105"+
        "\3\26\1\106";
    static final String DFA9_eofS =
        "\123\uffff";
    static final String DFA9_minS =
        "\1\11\1\uffff\21\60\1\uffff\1\111\1\101\1\uffff\1\101\1\105\1\60"+
        "\1\111\5\60\1\uffff\1\126\2\60\1\131\2\60\1\122\1\131\14\60\1\uffff"+
        "\1\124\1\11\1\60\1\122\1\60\1\uffff\1\124\1\uffff\1\104\2\60\1\122"+
        "\2\60\1\101\2\uffff\1\124\1\105\3\60\1\115\2\60\1\105\1\116\1\124"+
        "\1\60";
    static final String DFA9_maxS =
        "\1\132\1\uffff\1\116\1\126\1\132\1\114\1\122\1\113\1\104\1\105\3"+
        "\132\1\102\1\116\1\103\1\125\1\71\1\132\1\uffff\1\111\1\132\1\uffff"+
        "\1\124\1\105\1\132\1\111\5\132\1\uffff\1\126\2\132\1\131\2\132\1"+
        "\122\1\131\14\132\1\uffff\1\124\1\71\1\132\1\122\1\132\1\uffff\1"+
        "\124\1\uffff\1\104\2\132\1\122\3\132\2\uffff\1\124\1\105\2\132\1"+
        "\71\1\115\2\132\1\105\1\116\1\124\1\132";
    static final String DFA9_acceptS =
        "\1\uffff\1\1\21\uffff\1\11\2\uffff\1\10\11\uffff\1\4\24\uffff\1"+
        "\7\5\uffff\1\5\1\uffff\1\3\7\uffff\1\6\1\2\14\uffff";
    static final String DFA9_specialS =
        "\123\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\23\26\uffff\1\23\14\uffff\1\1\2\uffff\12\22\7\uffff\1\3\1"+
            "\5\1\6\1\21\1\14\7\21\1\15\1\13\1\16\1\7\1\17\1\10\1\4\1\11"+
            "\1\2\1\21\1\12\1\21\1\20\1\21",
            "",
            "\12\25\24\uffff\1\24",
            "\12\25\10\uffff\1\31\15\uffff\1\27\5\uffff\1\30",
            "\12\25\7\uffff\4\26\1\34\5\26\1\37\3\26\1\36\4\26\1\33\1\32"+
            "\1\26\1\35\3\26",
            "\12\25\11\uffff\1\42\10\uffff\1\41",
            "\12\25\30\uffff\1\43",
            "\12\25\13\uffff\1\45\5\uffff\1\44",
            "\12\25\12\uffff\1\46",
            "\12\25\13\uffff\1\47",
            "\12\25\7\uffff\1\50\31\26",
            "\12\25\7\uffff\1\26\1\55\2\26\1\51\6\26\1\56\2\26\1\53\3\26"+
            "\1\54\1\60\1\57\1\26\1\52\3\26",
            "\12\25\7\uffff\32\26",
            "\12\25\10\uffff\1\61",
            "\12\25\24\uffff\1\62",
            "\12\25\11\uffff\1\63",
            "\12\25\33\uffff\1\64",
            "\12\25",
            "\12\22\7\uffff\32\26",
            "",
            "\1\66",
            "\32\67",
            "",
            "\1\71\22\uffff\1\70",
            "\1\72",
            "\12\26\7\uffff\32\26",
            "\1\74",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "",
            "\1\76",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\23\26\1\77\6\26",
            "\1\100",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\1\101",
            "\1\102",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "",
            "\1\103",
            "\1\105\26\uffff\1\105\17\uffff\12\104",
            "\12\26\7\uffff\32\26",
            "\1\107",
            "\12\26\7\uffff\32\26",
            "",
            "\1\110",
            "",
            "\1\111",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\1\112",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\32\113",
            "",
            "",
            "\1\114",
            "\1\115",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\12\116",
            "\1\117",
            "\12\26\7\uffff\32\26",
            "\12\26\7\uffff\32\26",
            "\1\120",
            "\1\121",
            "\1\122",
            "\12\26\7\uffff\32\26"
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