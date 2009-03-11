// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-11 11:10:27

package ca.sqlpower.matchmaker.address.parse;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.PostalCode;

import com.sleepycat.je.DatabaseException;

public class AddressParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NUMBER", "SUITE", "STREETNUMSUFFIX", "SUFFIXANDDIR", "NUMANDSUFFIX", "STREETDIR", "NAME", "GD", "DITYPE", "WS", "'-'"
    };
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


        public AddressParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public AddressParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return AddressParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g"; }



    private PostalCode postalCode;

    private AddressDatabase addressDatabase;

    private Address address = new Address();

    /**
     * This may be moved into a local variable later
     */
     private boolean hasStreetNameStarted = false;

    public Address getAddress() { return address; }

    public void setAddressDatabase(AddressDatabase addressDatabase) {
       this.addressDatabase = addressDatabase;
    }

    public void setPostalCode(String postalCodeString) throws DatabaseException {
       if (addressDatabase == null) throw new NullPointerException("No address database!");
       postalCode = addressDatabase.findPostalCode(postalCodeString);
    }

    private boolean couldBeUrban() {
       if (postalCode == null) return true;
       return postalCode.getRecordType() == PostalCode.RecordType.STREET || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
    }

    private boolean couldBeRural() {
       if (postalCode == null) return true;
       return postalCode.getRecordType() == PostalCode.RecordType.ROUTE || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
    }

    private boolean couldBeLockBox() {
       if (postalCode == null) return true;
       return postalCode.getRecordType() == PostalCode.RecordType.LOCK_BOX;
    }

    private boolean couldBeGD() {
       if (postalCode == null) return true;
       return postalCode.getRecordType() == PostalCode.RecordType.GENERAL_DELIVERY;
    }

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

    private void appendStreetName(String name) {
       if (address.getStreet() != null && address.getStreet().trim().length() > 0) {
          address.setStreet(address.getStreet() + " " + name);
       } else {
          address.setStreet(name);
       }
    }

    public String getErrorMessage(RecognitionException e, 
    String[] tokenNames) 
    { 
    List stack = getRuleInvocationStack(e, this.getClass().getName()); 
    String msg = null; 
    if ( e instanceof NoViableAltException ) { 
    NoViableAltException nvae = (NoViableAltException)e; 
    msg = " no viable alt; token="+e.token+ 
    " (decision="+nvae.decisionNumber+ 
    " state "+nvae.stateNumber+")"+ 
    " decision=<<"+nvae.grammarDecisionDescription+">>"; 
    } 
    else { 
    msg = super.getErrorMessage(e, tokenNames); 
    } 
    return stack+" "+msg; 
    } 
    public String getTokenErrorDisplay(Token t) { 
    return t.toString(); 
    } 



    // $ANTLR start "address"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:112:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress );
    public final void address() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:2: ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress )
            int alt1=4;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==NUMBER||LA1_0==NUMANDSUFFIX) && ((couldBeUrban()))) {
                alt1=1;
            }
            else if ( (LA1_0==EOF) && (((couldBeLockBox())||(couldBeRural())))) {
                int LA1_2 = input.LA(2);

                if ( ((couldBeRural())) ) {
                    alt1=2;
                }
                else if ( ((couldBeLockBox())) ) {
                    alt1=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA1_0==GD) && ((couldBeGD()))) {
                alt1=4;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:4: {...}? => streetAddress
                    {
                    if ( !((couldBeUrban())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeUrban()");
                    }
                    pushFollow(FOLLOW_streetAddress_in_address36);
                    streetAddress();

                    state._fsp--;

                    address.setType(Address.Type.URBAN);

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:114:4: {...}? => ruralRouteAddress
                    {
                    if ( !((couldBeRural())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeRural()");
                    }
                    pushFollow(FOLLOW_ruralRouteAddress_in_address46);
                    ruralRouteAddress();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:115:4: {...}? => lockBoxAddress
                    {
                    if ( !((couldBeLockBox())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeLockBox()");
                    }
                    pushFollow(FOLLOW_lockBoxAddress_in_address54);
                    lockBoxAddress();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:4: {...}? => generalDeliveryAddress
                    {
                    if ( !((couldBeGD())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeGD()");
                    }
                    pushFollow(FOLLOW_generalDeliveryAddress_in_address62);
                    generalDeliveryAddress();

                    state._fsp--;

                    address.setType(Address.Type.GD);

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
    // $ANTLR end "address"


    // $ANTLR start "streetAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:1: streetAddress : (sn= NUMBER '-' street | street s= SUITE sn= NUMBER | street );
    public final void streetAddress() throws RecognitionException {
        Token sn=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:120:2: (sn= NUMBER '-' street | street s= SUITE sn= NUMBER | street )
            int alt2=3;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:120:4: sn= NUMBER '-' street
                    {
                    sn=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress79); 
                    match(input,14,FOLLOW_14_in_streetAddress81); 
                    pushFollow(FOLLOW_street_in_streetAddress83);
                    street();

                    state._fsp--;

                     
                    							  address.setSuitePrefix(true);
                    							  address.setSuite((sn!=null?sn.getText():null));
                    							  address.setType(Address.Type.URBAN);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:4: street s= SUITE sn= NUMBER
                    {
                    pushFollow(FOLLOW_street_in_streetAddress92);
                    street();

                    state._fsp--;

                    s=(Token)match(input,SUITE,FOLLOW_SUITE_in_streetAddress96); 
                    sn=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress100); 
                     
                    							  address.setSuitePrefix(false);
                    							  address.setSuiteType((s!=null?s.getText():null));
                    							  address.setSuite((sn!=null?sn.getText():null));
                    							  address.setType(Address.Type.URBAN);
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:131:4: street
                    {
                    pushFollow(FOLLOW_street_in_streetAddress108);
                    street();

                    state._fsp--;

                    address.setType(Address.Type.URBAN);

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


    // $ANTLR start "street"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:134:1: street : (n= NUMBER s= ( STREETNUMSUFFIX | SUFFIXANDDIR ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= NUMBER ( streetToken )+ );
    public final void street() throws RecognitionException {
        Token n=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:2: (n= NUMBER s= ( STREETNUMSUFFIX | SUFFIXANDDIR ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= NUMBER ( streetToken )+ )
            int alt6=3;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==NUMBER) ) {
                switch ( input.LA(2) ) {
                case SUFFIXANDDIR:
                    {
                    switch ( input.LA(3) ) {
                    case EOF:
                    case SUITE:
                        {
                        alt6=3;
                        }
                        break;
                    case SUFFIXANDDIR:
                    case STREETDIR:
                        {
                        alt6=1;
                        }
                        break;
                    case NAME:
                        {
                        alt6=1;
                        }
                        break;
                    case NUMBER:
                    case NUMANDSUFFIX:
                        {
                        alt6=1;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 3, input);

                        throw nvae;
                    }

                    }
                    break;
                case STREETNUMSUFFIX:
                    {
                    alt6=1;
                    }
                    break;
                case NUMBER:
                case NUMANDSUFFIX:
                case STREETDIR:
                case NAME:
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
            else if ( (LA6_0==NUMANDSUFFIX) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:4: n= NUMBER s= ( STREETNUMSUFFIX | SUFFIXANDDIR ) ( streetToken )+
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_street128); 
                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=SUFFIXANDDIR) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:46: ( streetToken )+
                    int cnt3=0;
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==NUMBER||(LA3_0>=SUFFIXANDDIR && LA3_0<=NAME)) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:46: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street138);
                    	    streetToken();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt3 >= 1 ) break loop3;
                                EarlyExitException eee =
                                    new EarlyExitException(3, input);
                                throw eee;
                        }
                        cnt3++;
                    } while (true);

                    address.setStreetNumber(quietIntParse((n!=null?n.getText():null)));
                    							 address.setStreetNumberSuffix((s!=null?s.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:138:4: n= NUMANDSUFFIX ( streetToken )+
                    {
                    n=(Token)match(input,NUMANDSUFFIX,FOLLOW_NUMANDSUFFIX_in_street148); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:138:19: ( streetToken )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==NUMBER||(LA4_0>=SUFFIXANDDIR && LA4_0<=NAME)) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:138:19: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street150);
                    	    streetToken();

                    	    state._fsp--;


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

                    String streetNum = (n!=null?n.getText():null);
                    							 address.setStreetNumber(quietIntParse(streetNum.substring(0, streetNum.length() - 1)));
                    							 address.setStreetNumberSuffix(streetNum.substring(streetNum.length() - 1, streetNum.length()));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:142:4: n= NUMBER ( streetToken )+
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_street161); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:142:13: ( streetToken )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==NUMBER||(LA5_0>=SUFFIXANDDIR && LA5_0<=NAME)) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:142:13: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street163);
                    	    streetToken();

                    	    state._fsp--;


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

                    address.setStreetNumber(quietIntParse((n!=null?n.getText():null)));

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


    // $ANTLR start "streetToken"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:145:1: streetToken : ({...}?d= ( STREETDIR | SUFFIXANDDIR ) | {...}?t= NAME | n= ( NAME | NUMBER | NUMANDSUFFIX ) );
    public final void streetToken() throws RecognitionException {
        Token d=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:146:2: ({...}?d= ( STREETDIR | SUFFIXANDDIR ) | {...}?t= NAME | n= ( NAME | NUMBER | NUMANDSUFFIX ) )
            int alt7=3;
            switch ( input.LA(1) ) {
            case SUFFIXANDDIR:
            case STREETDIR:
                {
                alt7=1;
                }
                break;
            case NAME:
                {
                int LA7_2 = input.LA(2);

                if ( ((addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                    alt7=2;
                }
                else if ( (true) ) {
                    alt7=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 2, input);

                    throw nvae;
                }
                }
                break;
            case NUMBER:
            case NUMANDSUFFIX:
                {
                alt7=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }

            switch (alt7) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:146:4: {...}?d= ( STREETDIR | SUFFIXANDDIR )
                    {
                    if ( !((hasStreetNameStarted)) ) {
                        throw new FailedPredicateException(input, "streetToken", "hasStreetNameStarted");
                    }
                    d=(Token)input.LT(1);
                    if ( input.LA(1)==SUFFIXANDDIR||input.LA(1)==STREETDIR ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    							 address.setStreetDirection((d!=null?d.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:149:4: {...}?t= NAME
                    {
                    if ( !((addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "addressDatabase.containsStreetType(input.LT(1).getText())");
                    }
                    t=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken199); 

                    							 if (!address.isStreetTypePrefix()) {
                    							    if (address.getStreetType() != null) {
                    							       appendStreetName(address.getStreetType());
                    							    }
                    							    address.setStreetTypePrefix(!hasStreetNameStarted);
                    							    address.setStreetType((t!=null?t.getText():null));
                    							 }
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:159:4: n= ( NAME | NUMBER | NUMANDSUFFIX )
                    {
                    n=(Token)input.LT(1);
                    if ( input.LA(1)==NUMBER||input.LA(1)==NUMANDSUFFIX||input.LA(1)==NAME ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    							 if (!address.isStreetTypePrefix() && address.getStreetType() != null) {
                    							    appendStreetName(address.getStreetType());
                    							    address.setStreetType(null);
                    							 }
                    							 hasStreetNameStarted = true;
                    							 appendStreetName((n!=null?n.getText():null));
                    							

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
    // $ANTLR end "streetToken"


    // $ANTLR start "ruralRouteAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:180:1: ruralRouteAddress : ;
    public final void ruralRouteAddress() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:181:2: ()
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:182:2: 
            {
            }

        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruralRouteAddress"


    // $ANTLR start "lockBoxAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:184:1: lockBoxAddress : ;
    public final void lockBoxAddress() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:185:2: ()
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:186:2: 
            {
            }

        }
        finally {
        }
        return ;
    }
    // $ANTLR end "lockBoxAddress"


    // $ANTLR start "generalDeliveryAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:188:1: generalDeliveryAddress : gd= GD (t= DITYPE )? (n= NAME )? ;
    public final void generalDeliveryAddress() throws RecognitionException {
        Token gd=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:2: (gd= GD (t= DITYPE )? (n= NAME )? )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:4: gd= GD (t= DITYPE )? (n= NAME )?
            {
            gd=(Token)match(input,GD,FOLLOW_GD_in_generalDeliveryAddress273); 
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:11: (t= DITYPE )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==DITYPE) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:11: t= DITYPE
                    {
                    t=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_generalDeliveryAddress277); 

                    }
                    break;

            }

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:21: (n= NAME )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==NAME) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:21: n= NAME
                    {
                    n=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress282); 

                    }
                    break;

            }


            							 address.setGeneralDeliveryName((gd!=null?gd.getText():null));
            							 address.setDeliveryInstallationType((t!=null?t.getText():null));
            							 address.setDeliveryInstallationName((n!=null?n.getText():null));
            							

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
    // $ANTLR end "generalDeliveryAddress"


    // $ANTLR start "streetType"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:196:1: streetType : {...}?n= NAME ;
    public final void streetType() throws RecognitionException {
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:197:2: ({...}?n= NAME )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:197:4: {...}?n= NAME
            {
            if ( !((addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                throw new FailedPredicateException(input, "streetType", "addressDatabase.containsStreetType(input.LT(1).getText())");
            }
            n=(Token)match(input,NAME,FOLLOW_NAME_in_streetType302); 
            address.setStreetType((n!=null?n.getText():null));

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
    // $ANTLR end "streetType"


    // $ANTLR start "streetName"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:1: streetName : (n+= NAME )+ ;
    public final void streetName() throws RecognitionException {
        Token n=null;
        List list_n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:201:2: ( (n+= NAME )+ )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:201:4: (n+= NAME )+
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:201:4: (n+= NAME )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==NAME) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:201:5: n+= NAME
            	    {
            	    n=(Token)match(input,NAME,FOLLOW_NAME_in_streetName320); 
            	    if (list_n==null) list_n=new ArrayList();
            	    list_n.add(n);


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

    // Delegated rules


    protected DFA2 dfa2 = new DFA2(this);
    static final String DFA2_eotS =
        "\24\uffff";
    static final String DFA2_eofS =
        "\4\uffff\1\20\1\uffff\6\20\1\uffff\3\20\1\uffff\3\20";
    static final String DFA2_minS =
        "\3\4\1\uffff\10\4\1\uffff\3\4\1\uffff\3\4";
    static final String DFA2_maxS =
        "\1\10\1\16\1\12\1\uffff\10\12\1\uffff\3\12\1\uffff\3\12";
    static final String DFA2_acceptS =
        "\3\uffff\1\1\10\uffff\1\2\3\uffff\1\3\3\uffff";
    static final String DFA2_specialS =
        "\24\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\1\3\uffff\1\2",
            "\1\10\1\uffff\1\5\1\4\1\10\1\6\1\7\3\uffff\1\3",
            "\1\13\2\uffff\1\11\1\13\1\11\1\12",
            "",
            "\1\17\1\14\1\uffff\1\15\1\17\1\15\1\16",
            "\1\23\2\uffff\1\21\1\23\1\21\1\22",
            "\1\10\1\14\1\uffff\1\6\1\10\1\6\1\7",
            "\1\10\1\14\1\uffff\1\6\1\10\1\6\1\7",
            "\1\10\1\14\1\uffff\1\6\1\10\1\6\1\7",
            "\1\13\1\14\1\uffff\1\11\1\13\1\11\1\12",
            "\1\13\1\14\1\uffff\1\11\1\13\1\11\1\12",
            "\1\13\1\14\1\uffff\1\11\1\13\1\11\1\12",
            "",
            "\1\17\1\14\1\uffff\1\15\1\17\1\15\1\16",
            "\1\17\1\14\1\uffff\1\15\1\17\1\15\1\16",
            "\1\17\1\14\1\uffff\1\15\1\17\1\15\1\16",
            "",
            "\1\23\1\14\1\uffff\1\21\1\23\1\21\1\22",
            "\1\23\1\14\1\uffff\1\21\1\23\1\21\1\22",
            "\1\23\1\14\1\uffff\1\21\1\23\1\21\1\22"
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
            return "119:1: streetAddress : (sn= NUMBER '-' street | street s= SUITE sn= NUMBER | street );";
        }
    }
 

    public static final BitSet FOLLOW_streetAddress_in_address36 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRouteAddress_in_address46 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lockBoxAddress_in_address54 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalDeliveryAddress_in_address62 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress79 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_streetAddress81 = new BitSet(new long[]{0x0000000000000110L});
    public static final BitSet FOLLOW_street_in_streetAddress83 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress92 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_SUITE_in_streetAddress96 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_street128 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_set_in_street132 = new BitSet(new long[]{0x0000000000000790L});
    public static final BitSet FOLLOW_streetToken_in_street138 = new BitSet(new long[]{0x0000000000000792L});
    public static final BitSet FOLLOW_NUMANDSUFFIX_in_street148 = new BitSet(new long[]{0x0000000000000790L});
    public static final BitSet FOLLOW_streetToken_in_street150 = new BitSet(new long[]{0x0000000000000792L});
    public static final BitSet FOLLOW_NUMBER_in_street161 = new BitSet(new long[]{0x0000000000000790L});
    public static final BitSet FOLLOW_streetToken_in_street163 = new BitSet(new long[]{0x0000000000000792L});
    public static final BitSet FOLLOW_set_in_streetToken184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetToken199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken215 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GD_in_generalDeliveryAddress273 = new BitSet(new long[]{0x0000000000001402L});
    public static final BitSet FOLLOW_DITYPE_in_generalDeliveryAddress277 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetType302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetName320 = new BitSet(new long[]{0x0000000000000402L});

}