// $ANTLR 3.1.1 /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-02-02 13:44:59

package ca.sqlpower.matchmaker.address.parse;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ca.sqlpower.matchmaker.address.Address;

public class AddressParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PROVINCE", "POSTALCODE", "NUMBER", "SUITE", "STREETTYPE", "STREETDIR", "NAME", "WS", "'-'"
    };
    public static final int T__12=12;
    public static final int STREETDIR=9;
    public static final int POSTALCODE=5;
    public static final int PROVINCE=4;
    public static final int SUITE=7;
    public static final int WS=11;
    public static final int EOF=-1;
    public static final int NUMBER=6;
    public static final int STREETTYPE=8;
    public static final int NAME=10;

    // delegates
    // delegators


        public AddressParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public AddressParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return AddressParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g"; }


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



    // $ANTLR start "fullAddress"
    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:28:1: fullAddress : streetAddress city p= PROVINCE c= POSTALCODE ;
    public final void fullAddress() throws RecognitionException {
        Token p=null;
        Token c=null;

        try {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:29:2: ( streetAddress city p= PROVINCE c= POSTALCODE )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:29:4: streetAddress city p= PROVINCE c= POSTALCODE
            {
            pushFollow(FOLLOW_streetAddress_in_fullAddress25);
            streetAddress();

            state._fsp--;

            pushFollow(FOLLOW_city_in_fullAddress27);
            city();

            state._fsp--;

            p=(Token)match(input,PROVINCE,FOLLOW_PROVINCE_in_fullAddress31); 
            c=(Token)match(input,POSTALCODE,FOLLOW_POSTALCODE_in_fullAddress35); 
             address.setProvince((p!=null?p.getText():null));
            							  address.setPostalCode((c!=null?c.getText():null));
            							

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "fullAddress"


    // $ANTLR start "streetAddress"
    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:35:1: streetAddress : (n= NUMBER street | suiteNum '-' n= NUMBER street | n= NUMBER street s= SUITE suiteNum );
    public final void streetAddress() throws RecognitionException {
        Token n=null;
        Token s=null;

        try {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:36:2: (n= NUMBER street | suiteNum '-' n= NUMBER street | n= NUMBER street s= SUITE suiteNum )
            int alt1=3;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:36:4: n= NUMBER street
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress58); 
                    pushFollow(FOLLOW_street_in_streetAddress60);
                    street();

                    state._fsp--;

                     address.setStreetNumber(Integer.valueOf((n!=null?n.getText():null))); 

                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:37:4: suiteNum '-' n= NUMBER street
                    {
                    pushFollow(FOLLOW_suiteNum_in_streetAddress70);
                    suiteNum();

                    state._fsp--;

                    match(input,12,FOLLOW_12_in_streetAddress72); 
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress76); 
                    pushFollow(FOLLOW_street_in_streetAddress78);
                    street();

                    state._fsp--;

                     address.setStreetNumber(Integer.valueOf((n!=null?n.getText():null)));
                    							  address.setSuitePrefix(true);
                    							

                    }
                    break;
                case 3 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:40:4: n= NUMBER street s= SUITE suiteNum
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress88); 
                    pushFollow(FOLLOW_street_in_streetAddress90);
                    street();

                    state._fsp--;

                    s=(Token)match(input,SUITE,FOLLOW_SUITE_in_streetAddress94); 
                    pushFollow(FOLLOW_suiteNum_in_streetAddress96);
                    suiteNum();

                    state._fsp--;

                     address.setStreetNumber(Integer.valueOf((n!=null?n.getText():null)));
                    							  address.setSuitePrefix(false);
                    							  address.setSuiteType((s!=null?s.getText():null));
                    							

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "streetAddress"


    // $ANTLR start "suiteNum"
    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:46:1: suiteNum : n= NUMBER ;
    public final void suiteNum() throws RecognitionException {
        Token n=null;

        try {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:46:9: (n= NUMBER )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:46:11: n= NUMBER
            {
            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_suiteNum109); 
             address.setSuite((n!=null?n.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "suiteNum"


    // $ANTLR start "street"
    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:49:1: street : ( streetName (t= STREETTYPE )? (d= STREETDIR )? | d= STREETDIR streetName (t= STREETTYPE )? );
    public final void street() throws RecognitionException {
        Token t=null;
        Token d=null;

        try {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:49:8: ( streetName (t= STREETTYPE )? (d= STREETDIR )? | d= STREETDIR streetName (t= STREETTYPE )? )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==NAME) ) {
                alt5=1;
            }
            else if ( (LA5_0==STREETDIR) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:49:10: streetName (t= STREETTYPE )? (d= STREETDIR )?
                    {
                    pushFollow(FOLLOW_streetName_in_street124);
                    streetName();

                    state._fsp--;

                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:49:22: (t= STREETTYPE )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==STREETTYPE) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:49:22: t= STREETTYPE
                            {
                            t=(Token)match(input,STREETTYPE,FOLLOW_STREETTYPE_in_street128); 

                            }
                            break;

                    }

                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:49:36: (d= STREETDIR )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0==STREETDIR) ) {
                        alt3=1;
                    }
                    switch (alt3) {
                        case 1 :
                            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:49:36: d= STREETDIR
                            {
                            d=(Token)match(input,STREETDIR,FOLLOW_STREETDIR_in_street133); 

                            }
                            break;

                    }

                     address.setStreetType((t!=null?t.getText():null));
                    							  address.setStreetDirection((d!=null?d.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:52:4: d= STREETDIR streetName (t= STREETTYPE )?
                    {
                    d=(Token)match(input,STREETDIR,FOLLOW_STREETDIR_in_street143); 
                    pushFollow(FOLLOW_streetName_in_street145);
                    streetName();

                    state._fsp--;

                    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:52:28: (t= STREETTYPE )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==STREETTYPE) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:52:28: t= STREETTYPE
                            {
                            t=(Token)match(input,STREETTYPE,FOLLOW_STREETTYPE_in_street149); 

                            }
                            break;

                    }

                     address.setStreetType((t!=null?t.getText():null));
                    							  address.setStreetDirection((d!=null?d.getText():null));
                    							

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "street"


    // $ANTLR start "streetName"
    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:57:1: streetName : (n+= NAME )+ ;
    public final void streetName() throws RecognitionException {
        Token n=null;
        List list_n=null;

        try {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:58:2: ( (n+= NAME )+ )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:58:4: (n+= NAME )+
            {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:58:4: (n+= NAME )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==NAME) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:58:5: n+= NAME
            	    {
            	    n=(Token)match(input,NAME,FOLLOW_NAME_in_streetName168); 
            	    if (list_n==null) list_n=new ArrayList();
            	    list_n.add(n);


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

             address.setStreet(wordList(list_n)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "streetName"


    // $ANTLR start "city"
    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:62:1: city : (n+= NAME )+ ;
    public final void city() throws RecognitionException {
        Token n=null;
        List list_n=null;

        try {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:62:6: ( (n+= NAME )+ )
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:62:8: (n+= NAME )+
            {
            // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:62:8: (n+= NAME )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==NAME) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/fuerth/prg/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:62:9: n+= NAME
            	    {
            	    n=(Token)match(input,NAME,FOLLOW_NAME_in_city195); 
            	    if (list_n==null) list_n=new ArrayList();
            	    list_n.add(n);


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

             address.setMunicipality(wordList(list_n)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "city"

    // Delegated rules


    protected DFA1 dfa1 = new DFA1(this);
    static final String DFA1_eotS =
        "\15\uffff";
    static final String DFA1_eofS =
        "\15\uffff";
    static final String DFA1_minS =
        "\1\6\1\11\1\7\1\12\1\uffff\2\7\1\4\1\uffff\1\7\1\uffff\1\7\1\4";
    static final String DFA1_maxS =
        "\1\6\1\14\2\12\1\uffff\3\12\1\uffff\1\12\1\uffff\2\12";
    static final String DFA1_acceptS =
        "\4\uffff\1\2\3\uffff\1\3\1\uffff\1\1\2\uffff";
    static final String DFA1_specialS =
        "\15\uffff}>";
    static final String[] DFA1_transitionS = {
            "\1\1",
            "\1\3\1\2\1\uffff\1\4",
            "\1\10\1\5\1\6\1\7",
            "\1\11",
            "",
            "\1\10\1\uffff\1\6\1\12",
            "\1\10\2\uffff\1\12",
            "\1\12\2\uffff\1\10\1\5\1\6\1\7",
            "",
            "\1\10\1\13\1\uffff\1\14",
            "",
            "\1\10\2\uffff\1\12",
            "\1\12\2\uffff\1\10\1\13\1\uffff\1\14"
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
            return "35:1: streetAddress : (n= NUMBER street | suiteNum '-' n= NUMBER street | n= NUMBER street s= SUITE suiteNum );";
        }
    }
 

    public static final BitSet FOLLOW_streetAddress_in_fullAddress25 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_city_in_fullAddress27 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_PROVINCE_in_fullAddress31 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_POSTALCODE_in_fullAddress35 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress58 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_street_in_streetAddress60 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_suiteNum_in_streetAddress70 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_12_in_streetAddress72 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress76 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_street_in_streetAddress78 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress88 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_street_in_streetAddress90 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_SUITE_in_streetAddress94 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_suiteNum_in_streetAddress96 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_suiteNum109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_streetName_in_street124 = new BitSet(new long[]{0x0000000000000302L});
    public static final BitSet FOLLOW_STREETTYPE_in_street128 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_STREETDIR_in_street133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STREETDIR_in_street143 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_streetName_in_street145 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_STREETTYPE_in_street149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetName168 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_NAME_in_city195 = new BitSet(new long[]{0x0000000000000402L});

}