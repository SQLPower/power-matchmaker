// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-13 11:08:33

package ca.sqlpower.matchmaker.address.parse;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.IntStream;
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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ROUTESERVICETYPE", "DITYPE", "SUITE", "SUFFIXANDDIR", "STREETNUMSUFFIX", "NUMANDSTREETSUFFIX", "STREETDIR", "NUMANDSUFFIX", "NUMBER", "NAME", "SUITEANDSTREETNUM", "WS", "'-'", "'#'"
    };
    public static final int SUITEANDSTREETNUM=14;
    public static final int STREETDIR=10;
    public static final int NAME=13;
    public static final int WS=15;
    public static final int T__16=16;
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


        public AddressParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public AddressParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return AddressParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g"; }



    private Set<PostalCode> postalCodes;

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
       postalCodes = addressDatabase.findPostalCode(postalCodeString);
    }

    private boolean couldBeUrban() {
       if (postalCodes.isEmpty()) return true;
       boolean isUrbanType = false;
       for (PostalCode postalCode : postalCodes) {
          isUrbanType |= postalCode.getRecordType() == PostalCode.RecordType.STREET || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
       }
       return isUrbanType;
    }

    private boolean couldBeRural() {
       if (postalCodes.isEmpty()) return true;
       boolean isRuralType = false;
       for (PostalCode postalCode : postalCodes) {
          isRuralType |= postalCode.getRecordType() == PostalCode.RecordType.ROUTE || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
       }
       return isRuralType;
    }

    private boolean couldBeLockBox() {
       if (postalCodes.isEmpty()) return true;
       boolean isLockBoxType = false;
       for (PostalCode postalCode : postalCodes) {
          isLockBoxType |= postalCode.getRecordType() == PostalCode.RecordType.LOCK_BOX;
       }
       return isLockBoxType;
    }

    private boolean couldBeGD() {
       if (postalCodes.isEmpty()) return true;
       boolean isGDType = false;
       for (PostalCode postalCode : postalCodes) {
          isGDType |= postalCode.getRecordType() == PostalCode.RecordType.GENERAL_DELIVERY;
       }
       return isGDType;
    }

    /**
     * Parses an integer value from a string, failing silently and returning
     * null if the value is not parseable.
     */
    private Integer quietIntParse(String s) {
      try {
        return Integer.valueOf(s);
      } catch (NumberFormatException ex) {
        if (s.charAt(0) == '#') {
          try {
            return Integer.valueOf(s.substring(1, s.length()));
          } catch (NumberFormatException ex1) {
            //return default value
          }
        }
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:123:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );
    public final void address() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:2: ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse )
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:4: {...}? => streetAddress
                    {
                    if ( !((couldBeUrban())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeUrban()");
                    }
                    pushFollow(FOLLOW_streetAddress_in_address36);
                    streetAddress();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:4: {...}? => ruralRouteAddress
                    {
                    if ( !((couldBeRural())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeRural()");
                    }
                    pushFollow(FOLLOW_ruralRouteAddress_in_address45);
                    ruralRouteAddress();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:126:4: {...}? => lockBoxAddress
                    {
                    if ( !((couldBeLockBox())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeLockBox()");
                    }
                    pushFollow(FOLLOW_lockBoxAddress_in_address53);
                    lockBoxAddress();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:127:4: {...}? => generalDeliveryAddress
                    {
                    if ( !((couldBeGD())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeGD()");
                    }
                    pushFollow(FOLLOW_generalDeliveryAddress_in_address61);
                    generalDeliveryAddress();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:128:4: failedParse
                    {
                    pushFollow(FOLLOW_failedParse_in_address67);
                    failedParse();

                    state._fsp--;


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


    // $ANTLR start "failedParse"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:131:1: failedParse : ( failedToken )* ;
    public final void failedParse() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:132:2: ( ( failedToken )* )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:132:4: ( failedToken )*
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:132:4: ( failedToken )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=ROUTESERVICETYPE && LA2_0<=NAME)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:132:4: failedToken
            	    {
            	    pushFollow(FOLLOW_failedToken_in_failedParse83);
            	    failedToken();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


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
    // $ANTLR end "failedParse"


    // $ANTLR start "failedToken"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:1: failedToken : n= ( ROUTESERVICETYPE | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | NUMANDSTREETSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME ) ;
    public final void failedToken() throws RecognitionException {
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:2: (n= ( ROUTESERVICETYPE | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | NUMANDSTREETSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:4: n= ( ROUTESERVICETYPE | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | NUMANDSTREETSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME )
            {
            n=(Token)input.LT(1);
            if ( (input.LA(1)>=ROUTESERVICETYPE && input.LA(1)<=NAME) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            							 address.setFailedParsingString(address.getFailedParsingString() + n);
            							

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
    // $ANTLR end "failedToken"


    // $ANTLR start "streetAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:142:1: streetAddress : (sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street | street s= SUITE sn= ( NUMBER | NUMANDSTREETSUFFIX ) | street );
    public final void streetAddress() throws RecognitionException {
        Token sn=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:143:2: (sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street | street s= SUITE sn= ( NUMBER | NUMANDSTREETSUFFIX ) | street )
            int alt3=3;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:143:4: sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street
                    {
                    sn=(Token)input.LT(1);
                    if ( input.LA(1)==NUMANDSTREETSUFFIX||input.LA(1)==NUMBER ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    match(input,16,FOLLOW_16_in_streetAddress165); 
                    pushFollow(FOLLOW_street_in_streetAddress167);
                    street();

                    state._fsp--;

                     
                    							  address.setSuitePrefix(true);
                    							  address.setSuite((sn!=null?sn.getText():null));
                    							  address.setType(Address.Type.URBAN);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:149:4: street s= SUITE sn= ( NUMBER | NUMANDSTREETSUFFIX )
                    {
                    pushFollow(FOLLOW_street_in_streetAddress184);
                    street();

                    state._fsp--;

                    s=(Token)match(input,SUITE,FOLLOW_SUITE_in_streetAddress188); 
                    sn=(Token)input.LT(1);
                    if ( input.LA(1)==NUMANDSTREETSUFFIX||input.LA(1)==NUMBER ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                     
                    							  address.setSuitePrefix(false);
                    							  address.setSuiteType((s!=null?s.getText():null));
                    							  address.setSuite((sn!=null?sn.getText():null));
                    							  address.setType(Address.Type.URBAN);
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:156:4: street
                    {
                    pushFollow(FOLLOW_street_in_streetAddress212);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:159:1: street : (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ );
    public final void street() throws RecognitionException {
        Token n=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:160:2: (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ )
            int alt8=4;
            alt8 = dfa8.predict(input);
            switch (alt8) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:160:4: n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+
                    {
                    n=(Token)match(input,SUITEANDSTREETNUM,FOLLOW_SUITEANDSTREETNUM_in_street232); 
                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=SUFFIXANDDIR && input.LA(1)<=NUMANDSTREETSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:160:76: ( streetToken )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0>=SUFFIXANDDIR && LA4_0<=NAME)) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:160:76: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street244);
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

                    String[] numbers = (n!=null?n.getText():null).split("-");
                    							 address.setSuitePrefix(true);
                    							 address.setSuite(numbers[0]);
                    							 address.setStreetNumber(quietIntParse(numbers[1]));
                    							 address.setStreetNumberSuffix((s!=null?s.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:167:4: n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+
                    {
                    n=(Token)input.LT(1);
                    if ( input.LA(1)==NUMANDSTREETSUFFIX||input.LA(1)==NUMBER ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=SUFFIXANDDIR && input.LA(1)<=NUMANDSTREETSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:167:86: ( streetToken )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( ((LA5_0>=SUFFIXANDDIR && LA5_0<=NAME)) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:167:86: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street277);
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
                    							 address.setStreetNumberSuffix((s!=null?s.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:171:4: n= NUMANDSUFFIX ( streetToken )+
                    {
                    n=(Token)match(input,NUMANDSUFFIX,FOLLOW_NUMANDSUFFIX_in_street295); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:171:19: ( streetToken )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0>=SUFFIXANDDIR && LA6_0<=NAME)) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:171:19: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street297);
                    	    streetToken();

                    	    state._fsp--;


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

                    String streetNum = (n!=null?n.getText():null);
                    							 address.setStreetNumber(quietIntParse(streetNum.substring(0, streetNum.length() - 1)));
                    							 address.setStreetNumberSuffix(streetNum.substring(streetNum.length() - 1, streetNum.length()));
                    							

                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:175:4: n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+
                    {
                    n=(Token)input.LT(1);
                    if ( input.LA(1)==NUMANDSTREETSUFFIX||input.LA(1)==NUMBER ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:175:34: ( streetToken )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( ((LA7_0>=SUFFIXANDDIR && LA7_0<=NAME)) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:175:34: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street314);
                    	    streetToken();

                    	    state._fsp--;


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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:179:1: streetToken : ({...}?d= ( STREETDIR | SUFFIXANDDIR ) | {...}?t= NAME | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX ) );
    public final void streetToken() throws RecognitionException {
        Token d=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:180:2: ({...}?d= ( STREETDIR | SUFFIXANDDIR ) | {...}?t= NAME | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX ) )
            int alt9=3;
            switch ( input.LA(1) ) {
            case SUFFIXANDDIR:
            case STREETDIR:
                {
                alt9=1;
                }
                break;
            case NAME:
                {
                int LA9_2 = input.LA(2);

                if ( ((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                    alt9=2;
                }
                else if ( (true) ) {
                    alt9=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 2, input);

                    throw nvae;
                }
                }
                break;
            case STREETNUMSUFFIX:
            case NUMANDSTREETSUFFIX:
            case NUMANDSUFFIX:
            case NUMBER:
                {
                alt9=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:180:4: {...}?d= ( STREETDIR | SUFFIXANDDIR )
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:185:4: {...}?t= NAME
                    {
                    if ( !((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText())");
                    }
                    t=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken374); 

                    							 if (address.getStreetType() != null) {
                    							    appendStreetName(address.getStreetType());
                    							 }
                    							 address.setStreetTypePrefix(!hasStreetNameStarted);
                    							 address.setStreetType((t!=null?t.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:194:4: n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX )
                    {
                    n=(Token)input.LT(1);
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NUMANDSTREETSUFFIX)||(input.LA(1)>=NUMANDSUFFIX && input.LA(1)<=NAME) ) {
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:206:1: ruralRouteAddress : (rs= ROUTESERVICETYPE n= NUMBER (di= DITYPE )? (stn= NAME )? | rs= ROUTESERVICETYPE n= NUMBER street );
    public final void ruralRouteAddress() throws RecognitionException {
        Token rs=null;
        Token n=null;
        Token di=null;
        Token stn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:2: (rs= ROUTESERVICETYPE n= NUMBER (di= DITYPE )? (stn= NAME )? | rs= ROUTESERVICETYPE n= NUMBER street )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==ROUTESERVICETYPE) ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1==NUMBER) ) {
                    int LA12_2 = input.LA(3);

                    if ( (LA12_2==NUMANDSTREETSUFFIX||(LA12_2>=NUMANDSUFFIX && LA12_2<=NUMBER)||LA12_2==SUITEANDSTREETNUM) ) {
                        alt12=2;
                    }
                    else if ( (LA12_2==EOF||LA12_2==DITYPE||LA12_2==NAME) ) {
                        alt12=1;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 2, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:4: rs= ROUTESERVICETYPE n= NUMBER (di= DITYPE )? (stn= NAME )?
                    {
                    rs=(Token)match(input,ROUTESERVICETYPE,FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress433); 
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRouteAddress437); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:35: (di= DITYPE )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==DITYPE) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:35: di= DITYPE
                            {
                            di=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_ruralRouteAddress441); 

                            }
                            break;

                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:47: (stn= NAME )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==NAME) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:47: stn= NAME
                            {
                            stn=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRouteAddress446); 

                            }
                            break;

                    }


                    							 address.setRuralRouteType((rs!=null?rs.getText():null));
                    							 address.setRuralRouteNumber(quietIntParse((rs!=null?rs.getText():null)));
                    							 address.setDeliveryInstallationType((di!=null?di.getText():null));
                    							 address.setDeliveryInstallationName((stn!=null?stn.getText():null));
                    							 address.setType(Address.Type.RURAL);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:215:4: rs= ROUTESERVICETYPE n= NUMBER street
                    {
                    rs=(Token)match(input,ROUTESERVICETYPE,FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress463); 
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRouteAddress467); 
                    pushFollow(FOLLOW_street_in_ruralRouteAddress469);
                    street();

                    state._fsp--;


                    							 address.setRuralRouteType((rs!=null?rs.getText():null));
                    							 address.setRuralRouteNumber(quietIntParse((rs!=null?rs.getText():null)));
                    							 address.setType(Address.Type.RURAL);
                    							

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
    // $ANTLR end "ruralRouteAddress"


    // $ANTLR start "lockBoxAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:222:1: lockBoxAddress : ({...}?lb= NAME ( '#' )? n= NUMBER di= DITYPE ( diName )+ | {...}?lb1= ( NAME | DITYPE ) lb2= NAME ( '#' )? n= NUMBER di= DITYPE ( diName )+ );
    public final void lockBoxAddress() throws RecognitionException {
        Token lb=null;
        Token n=null;
        Token di=null;
        Token lb1=null;
        Token lb2=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:2: ({...}?lb= NAME ( '#' )? n= NUMBER di= DITYPE ( diName )+ | {...}?lb1= ( NAME | DITYPE ) lb2= NAME ( '#' )? n= NUMBER di= DITYPE ( diName )+ )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==NAME) ) {
                int LA17_1 = input.LA(2);

                if ( (LA17_1==NAME) ) {
                    alt17=2;
                }
                else if ( (LA17_1==NUMBER||LA17_1==17) ) {
                    alt17=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA17_0==DITYPE) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:4: {...}?lb= NAME ( '#' )? n= NUMBER di= DITYPE ( diName )+
                    {
                    if ( !((Address.isLockBox(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "lockBoxAddress", "Address.isLockBox(input.LT(1).getText())");
                    }
                    lb=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress487); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:56: ( '#' )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==17) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:56: '#'
                            {
                            match(input,17,FOLLOW_17_in_lockBoxAddress489); 

                            }
                            break;

                    }

                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress494); 
                    di=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_lockBoxAddress498); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:80: ( diName )+
                    int cnt14=0;
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( ((LA14_0>=DITYPE && LA14_0<=SUITE)||(LA14_0>=STREETNUMSUFFIX && LA14_0<=NUMANDSTREETSUFFIX)||(LA14_0>=NUMANDSUFFIX && LA14_0<=NAME)) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:80: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_lockBoxAddress500);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt14 >= 1 ) break loop14;
                                EarlyExitException eee =
                                    new EarlyExitException(14, input);
                                throw eee;
                        }
                        cnt14++;
                    } while (true);


                    							 address.setLockBoxType((lb!=null?lb.getText():null));
                    							 address.setLockBoxNumber(quietIntParse((n!=null?n.getText():null)));
                    							 address.setDeliveryInstallationType((di!=null?di.getText():null));
                    							 address.setType(Address.Type.LOCK_BOX);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:4: {...}?lb1= ( NAME | DITYPE ) lb2= NAME ( '#' )? n= NUMBER di= DITYPE ( diName )+
                    {
                    if ( !((Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "lockBoxAddress", "Address.isLockBox(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    lb1=(Token)input.LT(1);
                    if ( input.LA(1)==DITYPE||input.LA(1)==NAME ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    lb2=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress529); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:107: ( '#' )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==17) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:107: '#'
                            {
                            match(input,17,FOLLOW_17_in_lockBoxAddress531); 

                            }
                            break;

                    }

                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress536); 
                    di=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_lockBoxAddress540); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:131: ( diName )+
                    int cnt16=0;
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( ((LA16_0>=DITYPE && LA16_0<=SUITE)||(LA16_0>=STREETNUMSUFFIX && LA16_0<=NUMANDSTREETSUFFIX)||(LA16_0>=NUMANDSUFFIX && LA16_0<=NAME)) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:131: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_lockBoxAddress542);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt16 >= 1 ) break loop16;
                                EarlyExitException eee =
                                    new EarlyExitException(16, input);
                                throw eee;
                        }
                        cnt16++;
                    } while (true);


                    							 address.setLockBoxType((lb1!=null?lb1.getText():null) + " " + (lb2!=null?lb2.getText():null));
                    							 address.setLockBoxNumber(quietIntParse((n!=null?n.getText():null)));
                    							 address.setDeliveryInstallationType((di!=null?di.getText():null));
                    							 address.setType(Address.Type.LOCK_BOX);
                    							

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
    // $ANTLR end "lockBoxAddress"


    // $ANTLR start "generalDeliveryAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:239:1: generalDeliveryAddress : ({...}?gd= NAME t= DITYPE ( diName )+ | {...}?gd1= ( NAME | DITYPE ) gd2= NAME t= DITYPE ( diName )+ );
    public final void generalDeliveryAddress() throws RecognitionException {
        Token gd=null;
        Token t=null;
        Token gd1=null;
        Token gd2=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:240:2: ({...}?gd= NAME t= DITYPE ( diName )+ | {...}?gd1= ( NAME | DITYPE ) gd2= NAME t= DITYPE ( diName )+ )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==NAME) ) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1==DITYPE) ) {
                    alt20=1;
                }
                else if ( (LA20_1==NAME) ) {
                    alt20=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA20_0==DITYPE) ) {
                alt20=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:240:4: {...}?gd= NAME t= DITYPE ( diName )+
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText())");
                    }
                    gd=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress568); 
                    t=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_generalDeliveryAddress572); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:240:73: ( diName )+
                    int cnt18=0;
                    loop18:
                    do {
                        int alt18=2;
                        int LA18_0 = input.LA(1);

                        if ( ((LA18_0>=DITYPE && LA18_0<=SUITE)||(LA18_0>=STREETNUMSUFFIX && LA18_0<=NUMANDSTREETSUFFIX)||(LA18_0>=NUMANDSUFFIX && LA18_0<=NAME)) ) {
                            alt18=1;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:240:73: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_generalDeliveryAddress574);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt18 >= 1 ) break loop18;
                                EarlyExitException eee =
                                    new EarlyExitException(18, input);
                                throw eee;
                        }
                        cnt18++;
                    } while (true);


                    							 address.setGeneralDeliveryName((gd!=null?gd.getText():null));
                    							 address.setDeliveryInstallationType((t!=null?t.getText():null));
                    							 address.setType(Address.Type.GD);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:246:4: {...}?gd1= ( NAME | DITYPE ) gd2= NAME t= DITYPE ( diName )+
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    gd1=(Token)input.LT(1);
                    if ( input.LA(1)==DITYPE||input.LA(1)==NAME ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    gd2=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress603); 
                    t=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_generalDeliveryAddress607); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:246:124: ( diName )+
                    int cnt19=0;
                    loop19:
                    do {
                        int alt19=2;
                        int LA19_0 = input.LA(1);

                        if ( ((LA19_0>=DITYPE && LA19_0<=SUITE)||(LA19_0>=STREETNUMSUFFIX && LA19_0<=NUMANDSTREETSUFFIX)||(LA19_0>=NUMANDSUFFIX && LA19_0<=NAME)) ) {
                            alt19=1;
                        }


                        switch (alt19) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:246:124: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_generalDeliveryAddress609);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt19 >= 1 ) break loop19;
                                EarlyExitException eee =
                                    new EarlyExitException(19, input);
                                throw eee;
                        }
                        cnt19++;
                    } while (true);


                    							 address.setGeneralDeliveryName((gd1!=null?gd1.getText():null) + " " + (gd2!=null?gd2.getText():null));
                    							 address.setDeliveryInstallationType((t!=null?t.getText():null));
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
    // $ANTLR end "generalDeliveryAddress"


    // $ANTLR start "diName"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:254:1: diName : stn= ( DITYPE | NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX | SUITE ) ;
    public final void diName() throws RecognitionException {
        Token stn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:255:2: (stn= ( DITYPE | NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX | SUITE ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:255:4: stn= ( DITYPE | NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX | SUITE )
            {
            stn=(Token)input.LT(1);
            if ( (input.LA(1)>=DITYPE && input.LA(1)<=SUITE)||(input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NUMANDSTREETSUFFIX)||(input.LA(1)>=NUMANDSUFFIX && input.LA(1)<=NAME) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            							 if (address.getDeliveryInstallationName() == null) {
            							    address.setDeliveryInstallationName((stn!=null?stn.getText():null));
            							 } else {
            							    address.setDeliveryInstallationName(address.getDeliveryInstallationName() + " " + (stn!=null?stn.getText():null));
            							 }
            							

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
    // $ANTLR end "diName"

    // Delegated rules


    protected DFA1 dfa1 = new DFA1(this);
    protected DFA3 dfa3 = new DFA3(this);
    protected DFA8 dfa8 = new DFA8(this);
    static final String DFA1_eotS =
        "\37\uffff";
    static final String DFA1_eofS =
        "\2\7\1\uffff\4\7\12\uffff\3\7\2\uffff\2\7\1\uffff\1\7\1\uffff\1"+
        "\7\3\uffff";
    static final String DFA1_minS =
        "\2\4\1\uffff\4\4\1\uffff\11\0\3\4\2\uffff\2\4\1\0\1\4\1\0\1\4\1"+
        "\uffff\2\0";
    static final String DFA1_maxS =
        "\1\16\1\20\1\uffff\2\15\1\21\1\15\1\uffff\11\0\1\21\2\15\2\uffff"+
        "\2\15\1\0\1\15\1\0\1\15\1\uffff\2\0";
    static final String DFA1_acceptS =
        "\2\uffff\1\1\4\uffff\1\5\14\uffff\1\3\1\2\6\uffff\1\4\2\uffff";
    static final String DFA1_specialS =
        "\1\6\1\13\3\uffff\1\12\2\uffff\1\4\1\10\1\7\1\16\1\14\1\20\1\17"+
        "\1\3\1\2\1\1\6\uffff\1\5\1\uffff\1\0\2\uffff\1\15\1\11}>";
    static final String[] DFA1_transitionS = {
            "\1\4\1\6\3\7\1\1\1\7\1\3\1\1\1\5\1\2",
            "\3\7\1\10\2\12\1\11\2\14\1\13\2\uffff\1\2",
            "",
            "\3\7\1\15\2\17\1\15\2\17\1\16",
            "\10\7\1\20\1\7",
            "\1\7\1\22\6\7\1\23\1\21\3\uffff\1\24",
            "\11\7\1\21",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\7\1\26\6\7\1\27\1\7\3\uffff\1\24",
            "\1\7\2\30\1\7\2\30\1\7\3\30",
            "\1\7\1\31\10\7",
            "",
            "",
            "\1\7\2\32\1\7\2\32\1\7\3\32",
            "\1\7\1\33\10\7",
            "\1\uffff",
            "\1\7\2\35\1\7\2\35\1\7\3\35",
            "\1\uffff",
            "\1\7\2\36\1\7\2\36\1\7\3\36",
            "",
            "\1\uffff",
            "\1\uffff"
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
            return "123:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA1_26 = input.LA(1);

                         
                        int index1_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeGD())&&(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText())))) ) {s = 28;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_26);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA1_17 = input.LA(1);

                         
                        int index1_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_17==DITYPE) ) {s = 22;}

                        else if ( (LA1_17==EOF||LA1_17==ROUTESERVICETYPE||(LA1_17>=SUITE && LA1_17<=NUMANDSUFFIX)||LA1_17==NAME) ) {s = 7;}

                        else if ( (LA1_17==NUMBER) ) {s = 23;}

                        else if ( (LA1_17==17) && ((couldBeLockBox()))) {s = 20;}

                         
                        input.seek(index1_17);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA1_16 = input.LA(1);

                         
                        int index1_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeRural())) ) {s = 21;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_16);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA1_15 = input.LA(1);

                         
                        int index1_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_15);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA1_8 = input.LA(1);

                         
                        int index1_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_8);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA1_24 = input.LA(1);

                         
                        int index1_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeGD())&&(Address.isGeneralDelivery(input.LT(1).getText())))) ) {s = 28;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_24);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA1_0 = input.LA(1);

                         
                        int index1_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_0==NUMANDSTREETSUFFIX||LA1_0==NUMBER) ) {s = 1;}

                        else if ( (LA1_0==SUITEANDSTREETNUM) && ((couldBeUrban()))) {s = 2;}

                        else if ( (LA1_0==NUMANDSUFFIX) ) {s = 3;}

                        else if ( (LA1_0==ROUTESERVICETYPE) ) {s = 4;}

                        else if ( (LA1_0==NAME) ) {s = 5;}

                        else if ( (LA1_0==DITYPE) ) {s = 6;}

                        else if ( (LA1_0==EOF||(LA1_0>=SUITE && LA1_0<=STREETNUMSUFFIX)||LA1_0==STREETDIR) ) {s = 7;}

                         
                        input.seek(index1_0);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA1_10 = input.LA(1);

                         
                        int index1_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_10);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA1_9 = input.LA(1);

                         
                        int index1_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA1_30 = input.LA(1);

                         
                        int index1_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeLockBox())&&(Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText())))) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_30);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA1_5 = input.LA(1);

                         
                        int index1_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_5==NAME) ) {s = 17;}

                        else if ( (LA1_5==DITYPE) ) {s = 18;}

                        else if ( (LA1_5==EOF||LA1_5==ROUTESERVICETYPE||(LA1_5>=SUITE && LA1_5<=NUMANDSUFFIX)) ) {s = 7;}

                        else if ( (LA1_5==NUMBER) ) {s = 19;}

                        else if ( (LA1_5==17) && ((couldBeLockBox()))) {s = 20;}

                         
                        input.seek(index1_5);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA1_1 = input.LA(1);

                         
                        int index1_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_1==16) && ((couldBeUrban()))) {s = 2;}

                        else if ( (LA1_1==SUFFIXANDDIR) ) {s = 8;}

                        else if ( (LA1_1==EOF||(LA1_1>=ROUTESERVICETYPE && LA1_1<=SUITE)) ) {s = 7;}

                        else if ( (LA1_1==STREETDIR) ) {s = 9;}

                        else if ( ((LA1_1>=STREETNUMSUFFIX && LA1_1<=NUMANDSTREETSUFFIX)) ) {s = 10;}

                        else if ( (LA1_1==NAME) ) {s = 11;}

                        else if ( ((LA1_1>=NUMANDSUFFIX && LA1_1<=NUMBER)) ) {s = 12;}

                         
                        input.seek(index1_1);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA1_12 = input.LA(1);

                         
                        int index1_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_12);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA1_29 = input.LA(1);

                         
                        int index1_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeLockBox())&&(Address.isLockBox(input.LT(1).getText())))) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_29);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA1_11 = input.LA(1);

                         
                        int index1_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_11);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA1_14 = input.LA(1);

                         
                        int index1_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_14);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA1_13 = input.LA(1);

                         
                        int index1_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_13);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 1, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA3_eotS =
        "\26\uffff";
    static final String DFA3_eofS =
        "\5\uffff\5\16\1\uffff\3\16\1\uffff\3\16\1\uffff\3\16";
    static final String DFA3_minS =
        "\1\11\3\7\1\uffff\5\6\1\7\3\6\1\uffff\3\6\1\uffff\3\6";
    static final String DFA3_maxS =
        "\1\16\1\20\1\11\1\15\1\uffff\11\15\1\uffff\3\15\1\uffff\3\15";
    static final String DFA3_acceptS =
        "\4\uffff\1\1\11\uffff\1\3\3\uffff\1\2\3\uffff";
    static final String DFA3_specialS =
        "\26\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\1\1\uffff\1\3\1\1\1\uffff\1\2",
            "\1\5\2\6\1\7\2\11\1\10\2\uffff\1\4",
            "\3\12",
            "\1\13\2\15\1\13\2\15\1\14",
            "",
            "\1\22\1\17\2\21\1\17\2\21\1\20",
            "\1\22\1\17\2\21\1\17\2\21\1\20",
            "\1\22\1\7\2\11\1\7\2\11\1\10",
            "\1\22\1\7\2\11\1\7\2\11\1\10",
            "\1\22\1\7\2\11\1\7\2\11\1\10",
            "\1\23\2\25\1\23\2\25\1\24",
            "\1\22\1\13\2\15\1\13\2\15\1\14",
            "\1\22\1\13\2\15\1\13\2\15\1\14",
            "\1\22\1\13\2\15\1\13\2\15\1\14",
            "",
            "\1\22\1\17\2\21\1\17\2\21\1\20",
            "\1\22\1\17\2\21\1\17\2\21\1\20",
            "\1\22\1\17\2\21\1\17\2\21\1\20",
            "",
            "\1\22\1\23\2\25\1\23\2\25\1\24",
            "\1\22\1\23\2\25\1\23\2\25\1\24",
            "\1\22\1\23\2\25\1\23\2\25\1\24"
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
            return "142:1: streetAddress : (sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street | street s= SUITE sn= ( NUMBER | NUMANDSTREETSUFFIX ) | street );";
        }
    }
    static final String DFA8_eotS =
        "\12\uffff";
    static final String DFA8_eofS =
        "\4\uffff\2\6\4\uffff";
    static final String DFA8_minS =
        "\1\11\1\uffff\1\7\1\uffff\2\6\4\uffff";
    static final String DFA8_maxS =
        "\1\16\1\uffff\1\15\1\uffff\2\15\4\uffff";
    static final String DFA8_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\2\uffff\1\4\3\2";
    static final String DFA8_specialS =
        "\12\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\2\1\uffff\1\3\1\2\1\uffff\1\1",
            "",
            "\1\4\2\5\4\6",
            "",
            "\1\6\1\7\2\11\1\7\2\11\1\10",
            "\1\6\1\7\2\11\1\7\2\11\1\10",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "159:1: street : (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ );";
        }
    }
 

    public static final BitSet FOLLOW_streetAddress_in_address36 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRouteAddress_in_address45 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lockBoxAddress_in_address53 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalDeliveryAddress_in_address61 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedParse_in_address67 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedToken_in_failedParse83 = new BitSet(new long[]{0x0000000000003FF2L});
    public static final BitSet FOLLOW_set_in_failedToken97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetAddress159 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_streetAddress165 = new BitSet(new long[]{0x0000000000005A00L});
    public static final BitSet FOLLOW_street_in_streetAddress167 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress184 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_SUITE_in_streetAddress188 = new BitSet(new long[]{0x0000000000001200L});
    public static final BitSet FOLLOW_set_in_streetAddress192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUITEANDSTREETNUM_in_street232 = new BitSet(new long[]{0x0000000000000380L});
    public static final BitSet FOLLOW_set_in_street236 = new BitSet(new long[]{0x0000000000003F80L});
    public static final BitSet FOLLOW_streetToken_in_street244 = new BitSet(new long[]{0x0000000000003F82L});
    public static final BitSet FOLLOW_set_in_street261 = new BitSet(new long[]{0x0000000000000380L});
    public static final BitSet FOLLOW_set_in_street269 = new BitSet(new long[]{0x0000000000003F80L});
    public static final BitSet FOLLOW_streetToken_in_street277 = new BitSet(new long[]{0x0000000000003F82L});
    public static final BitSet FOLLOW_NUMANDSUFFIX_in_street295 = new BitSet(new long[]{0x0000000000003F80L});
    public static final BitSet FOLLOW_streetToken_in_street297 = new BitSet(new long[]{0x0000000000003F82L});
    public static final BitSet FOLLOW_set_in_street308 = new BitSet(new long[]{0x0000000000003F80L});
    public static final BitSet FOLLOW_streetToken_in_street314 = new BitSet(new long[]{0x0000000000003F82L});
    public static final BitSet FOLLOW_set_in_streetToken343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetToken374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress433 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRouteAddress437 = new BitSet(new long[]{0x0000000000002022L});
    public static final BitSet FOLLOW_DITYPE_in_ruralRouteAddress441 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_NAME_in_ruralRouteAddress446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress463 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRouteAddress467 = new BitSet(new long[]{0x0000000000005A00L});
    public static final BitSet FOLLOW_street_in_ruralRouteAddress469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress487 = new BitSet(new long[]{0x0000000000021000L});
    public static final BitSet FOLLOW_17_in_lockBoxAddress489 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress494 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_DITYPE_in_lockBoxAddress498 = new BitSet(new long[]{0x0000000000003B60L});
    public static final BitSet FOLLOW_diName_in_lockBoxAddress500 = new BitSet(new long[]{0x0000000000003B62L});
    public static final BitSet FOLLOW_set_in_lockBoxAddress519 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress529 = new BitSet(new long[]{0x0000000000021000L});
    public static final BitSet FOLLOW_17_in_lockBoxAddress531 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress536 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_DITYPE_in_lockBoxAddress540 = new BitSet(new long[]{0x0000000000003B60L});
    public static final BitSet FOLLOW_diName_in_lockBoxAddress542 = new BitSet(new long[]{0x0000000000003B62L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress568 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_DITYPE_in_generalDeliveryAddress572 = new BitSet(new long[]{0x0000000000003B60L});
    public static final BitSet FOLLOW_diName_in_generalDeliveryAddress574 = new BitSet(new long[]{0x0000000000003B62L});
    public static final BitSet FOLLOW_set_in_generalDeliveryAddress593 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress603 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_DITYPE_in_generalDeliveryAddress607 = new BitSet(new long[]{0x0000000000003B60L});
    public static final BitSet FOLLOW_diName_in_generalDeliveryAddress609 = new BitSet(new long[]{0x0000000000003B62L});
    public static final BitSet FOLLOW_set_in_diName632 = new BitSet(new long[]{0x0000000000000002L});

}