// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-16 14:48:47

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "STREETNUMSUFFIX", "NUMANDSTREETSUFFIX", "NUMANDSUFFIX", "NUMBER", "NAME", "SUITEANDSTREETNUM", "WS", "'-'", "'#'"
    };
    public static final int SUITEANDSTREETNUM=9;
    public static final int NAME=8;
    public static final int WS=10;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int NUMBER=7;
    public static final int NUMANDSTREETSUFFIX=5;
    public static final int NUMANDSUFFIX=6;
    public static final int EOF=-1;
    public static final int STREETNUMSUFFIX=4;

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
      if (s == null) return null;
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );
    public final void address() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:2: ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse )
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:4: {...}? => streetAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:126:4: {...}? => ruralRouteAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:127:4: {...}? => lockBoxAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:128:4: {...}? => generalDeliveryAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:129:4: failedParse
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:132:1: failedParse : ( failedToken )* ;
    public final void failedParse() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:2: ( ( failedToken )* )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:4: ( failedToken )*
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:4: ( failedToken )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=STREETNUMSUFFIX && LA2_0<=NAME)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:133:4: failedToken
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:1: failedToken : n= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME ) ;
    public final void failedToken() throws RecognitionException {
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:137:2: (n= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:137:4: n= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME )
            {
            n=(Token)input.LT(1);
            if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NAME) ) {
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:143:1: streetAddress : (sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street | street );
    public final void streetAddress() throws RecognitionException {
        Token sn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:144:2: (sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street | street )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==NUMANDSTREETSUFFIX||LA3_0==NUMBER) ) {
                int LA3_1 = input.LA(2);

                if ( (LA3_1==11) ) {
                    alt3=1;
                }
                else if ( ((LA3_1>=STREETNUMSUFFIX && LA3_1<=NAME)) ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA3_0==NUMANDSUFFIX||LA3_0==SUITEANDSTREETNUM) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:144:4: sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street
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

                    match(input,11,FOLLOW_11_in_streetAddress145); 
                    pushFollow(FOLLOW_street_in_streetAddress147);
                    street();

                    state._fsp--;

                     
                    							  address.setSuitePrefix(true);
                    							  address.setSuite((sn!=null?sn.getText():null));
                    							  address.setType(Address.Type.URBAN);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:150:4: street
                    {
                    pushFollow(FOLLOW_street_in_streetAddress164);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:153:1: street : (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ );
    public final void street() throws RecognitionException {
        Token n=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:154:2: (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ )
            int alt8=4;
            alt8 = dfa8.predict(input);
            switch (alt8) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:154:4: n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX ) ( streetToken )+
                    {
                    n=(Token)match(input,SUITEANDSTREETNUM,FOLLOW_SUITEANDSTREETNUM_in_street184); 
                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NUMANDSTREETSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:154:63: ( streetToken )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0>=STREETNUMSUFFIX && LA4_0<=NAME)) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:154:63: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street194);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:161:4: n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX ) ( streetToken )+
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
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NUMANDSTREETSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:161:73: ( streetToken )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( ((LA5_0>=STREETNUMSUFFIX && LA5_0<=NAME)) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:161:73: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street225);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:165:4: n= NUMANDSUFFIX ( streetToken )+
                    {
                    n=(Token)match(input,NUMANDSUFFIX,FOLLOW_NUMANDSUFFIX_in_street243); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:165:19: ( streetToken )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0>=STREETNUMSUFFIX && LA6_0<=NAME)) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:165:19: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street245);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:169:4: n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+
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

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:169:34: ( streetToken )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( ((LA7_0>=STREETNUMSUFFIX && LA7_0<=NAME)) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:169:34: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street262);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:1: streetToken : ({...}? =>s= NAME sn= ( NUMBER | NUMANDSTREETSUFFIX ) | {...}?d= ( NAME | STREETNUMSUFFIX ) | {...}?t= NAME | {...}?n= ( NUMBER | NUMANDSTREETSUFFIX ) | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX ) );
    public final void streetToken() throws RecognitionException {
        Token s=null;
        Token sn=null;
        Token d=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:174:2: ({...}? =>s= NAME sn= ( NUMBER | NUMANDSTREETSUFFIX ) | {...}?d= ( NAME | STREETNUMSUFFIX ) | {...}?t= NAME | {...}?n= ( NUMBER | NUMANDSTREETSUFFIX ) | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX ) )
            int alt9=5;
            switch ( input.LA(1) ) {
            case NAME:
                {
                int LA9_1 = input.LA(2);

                if ( (LA9_1==NUMANDSTREETSUFFIX||LA9_1==NUMBER) && ((Address.isSuiteType(input.LT(1).getText())))) {
                    alt9=1;
                }
                else if ( ((hasStreetNameStarted && address.isStreetDirection(input.LT(1).getText()))) ) {
                    alt9=2;
                }
                else if ( ((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                    alt9=3;
                }
                else if ( (true) ) {
                    alt9=5;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;
                }
                }
                break;
            case STREETNUMSUFFIX:
                {
                int LA9_2 = input.LA(2);

                if ( ((hasStreetNameStarted && address.isStreetDirection(input.LT(1).getText()))) ) {
                    alt9=2;
                }
                else if ( (true) ) {
                    alt9=5;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 2, input);

                    throw nvae;
                }
                }
                break;
            case NUMANDSTREETSUFFIX:
            case NUMBER:
                {
                int LA9_3 = input.LA(2);

                if ( ((hasStreetNameStarted)) ) {
                    alt9=4;
                }
                else if ( (true) ) {
                    alt9=5;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 3, input);

                    throw nvae;
                }
                }
                break;
            case NUMANDSUFFIX:
                {
                alt9=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:174:4: {...}? =>s= NAME sn= ( NUMBER | NUMANDSTREETSUFFIX )
                    {
                    if ( !((Address.isSuiteType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "Address.isSuiteType(input.LT(1).getText())");
                    }
                    s=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken292); 
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
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:180:4: {...}?d= ( NAME | STREETNUMSUFFIX )
                    {
                    if ( !((hasStreetNameStarted && address.isStreetDirection(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "hasStreetNameStarted && address.isStreetDirection(input.LT(1).getText())");
                    }
                    d=(Token)input.LT(1);
                    if ( input.LA(1)==STREETNUMSUFFIX||input.LA(1)==NAME ) {
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
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:185:4: {...}?t= NAME
                    {
                    if ( !((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText())");
                    }
                    t=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken349); 

                    							 if (address.getStreetType() != null) {
                    							    appendStreetName(address.getStreetType());
                    							 }
                    							 address.setStreetTypePrefix(!hasStreetNameStarted);
                    							 address.setStreetType((t!=null?t.getText():null));
                    							

                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:193:4: {...}?n= ( NUMBER | NUMANDSTREETSUFFIX )
                    {
                    if ( !((hasStreetNameStarted)) ) {
                        throw new FailedPredicateException(input, "streetToken", "hasStreetNameStarted");
                    }
                    n=(Token)input.LT(1);
                    if ( input.LA(1)==NUMANDSTREETSUFFIX||input.LA(1)==NUMBER ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    							 address.setSuitePrefix(false);
                    							 address.setSuite((n!=null?n.getText():null));
                    							

                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:199:4: n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX )
                    {
                    n=(Token)input.LT(1);
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NAME) ) {
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
                    							 if (!address.isSuitePrefix()) {
                    							    if (address.getSuiteType() != null) {
                    							       appendStreetName(address.getSuiteType());
                    							       address.setSuiteType(null);
                    							    }
                    							    if (address.getSuite() != null) {
                    							       appendStreetName(address.getSuite());
                    							       address.setSuite(null);
                    							    }
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:221:1: ruralRouteAddress : ruralRoute ;
    public final void ruralRouteAddress() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:222:2: ( ruralRoute )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:222:4: ruralRoute
            {
            pushFollow(FOLLOW_ruralRoute_in_ruralRouteAddress428);
            ruralRoute();

            state._fsp--;

            address.setType(Address.Type.RURAL);

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


    // $ANTLR start "ruralRoute"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:225:1: ruralRoute : ({...}?rs= NAME (n= NUMBER )? ruralRouteSuffix | {...}?rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix | {...}?rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix );
    public final void ruralRoute() throws RecognitionException {
        Token rs=null;
        Token n=null;
        Token rs1=null;
        Token rs2=null;
        Token rs3=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:2: ({...}?rs= NAME (n= NUMBER )? ruralRouteSuffix | {...}?rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix | {...}?rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix )
            int alt13=3;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==NAME) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==NAME) ) {
                    int LA13_2 = input.LA(3);

                    if ( (LA13_2==NAME) ) {
                        alt13=3;
                    }
                    else if ( ((Address.isRuralRoute(input.LT(1).getText()))) ) {
                        alt13=1;
                    }
                    else if ( ((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        alt13=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 13, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA13_1==EOF||(LA13_1>=NUMANDSTREETSUFFIX && LA13_1<=NUMBER)||LA13_1==SUITEANDSTREETNUM) ) {
                    alt13=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:4: {...}?rs= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText())");
                    }
                    rs=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute449); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:60: (n= NUMBER )?
                    int alt10=2;
                    alt10 = dfa10.predict(input);
                    switch (alt10) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:60: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute453); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute456);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs!=null?rs.getText():null));
                    							 address.setRuralRouteNumber(quietIntParse((n!=null?n.getText():null)));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:231:4: {...}?rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    rs1=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute474); 
                    rs2=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute478); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:231:100: (n= NUMBER )?
                    int alt11=2;
                    alt11 = dfa11.predict(input);
                    switch (alt11) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:231:100: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute482); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute485);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs1!=null?rs1.getText():null) + " " + (rs2!=null?rs2.getText():null));
                    							 address.setRuralRouteNumber(quietIntParse((n!=null?n.getText():null)));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:4: {...}?rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    rs1=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute503); 
                    rs2=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute507); 
                    rs3=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute511); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:139: (n= NUMBER )?
                    int alt12=2;
                    alt12 = dfa12.predict(input);
                    switch (alt12) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:236:139: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute515); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute518);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs1!=null?rs1.getText():null) + " " + (rs2!=null?rs2.getText():null) + " " + (rs3!=null?rs3.getText():null));
                    							 address.setRuralRouteNumber(quietIntParse((n!=null?n.getText():null)));
                    							

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
    // $ANTLR end "ruralRoute"


    // $ANTLR start "ruralRouteSuffix"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:243:1: ruralRouteSuffix : ( ( diTypeAndName )? | street );
    public final void ruralRouteSuffix() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:244:2: ( ( diTypeAndName )? | street )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==EOF||LA15_0==NAME) ) {
                alt15=1;
            }
            else if ( ((LA15_0>=NUMANDSTREETSUFFIX && LA15_0<=NUMBER)||LA15_0==SUITEANDSTREETNUM) ) {
                alt15=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:244:4: ( diTypeAndName )?
                    {
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:244:4: ( diTypeAndName )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==NAME) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:244:4: diTypeAndName
                            {
                            pushFollow(FOLLOW_diTypeAndName_in_ruralRouteSuffix538);
                            diTypeAndName();

                            state._fsp--;


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:245:4: street
                    {
                    pushFollow(FOLLOW_street_in_ruralRouteSuffix544);
                    street();

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
    // $ANTLR end "ruralRouteSuffix"


    // $ANTLR start "lockBoxAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:248:1: lockBoxAddress : ({...}?lb= NAME ( '#' )? n= NUMBER diTypeAndName | {...}?lb1= NAME lb2= NAME ( '#' )? n= NUMBER diTypeAndName );
    public final void lockBoxAddress() throws RecognitionException {
        Token lb=null;
        Token n=null;
        Token lb1=null;
        Token lb2=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:249:2: ({...}?lb= NAME ( '#' )? n= NUMBER diTypeAndName | {...}?lb1= NAME lb2= NAME ( '#' )? n= NUMBER diTypeAndName )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==NAME) ) {
                int LA18_1 = input.LA(2);

                if ( (LA18_1==NAME) ) {
                    alt18=2;
                }
                else if ( (LA18_1==NUMBER||LA18_1==12) ) {
                    alt18=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:249:4: {...}?lb= NAME ( '#' )? n= NUMBER diTypeAndName
                    {
                    if ( !((Address.isLockBox(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "lockBoxAddress", "Address.isLockBox(input.LT(1).getText())");
                    }
                    lb=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress561); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:249:56: ( '#' )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==12) ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:249:56: '#'
                            {
                            match(input,12,FOLLOW_12_in_lockBoxAddress563); 

                            }
                            break;

                    }

                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress568); 
                    pushFollow(FOLLOW_diTypeAndName_in_lockBoxAddress570);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setLockBoxType((lb!=null?lb.getText():null));
                    							 address.setLockBoxNumber(quietIntParse((n!=null?n.getText():null)));
                    							 address.setType(Address.Type.LOCK_BOX);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:255:4: {...}?lb1= NAME lb2= NAME ( '#' )? n= NUMBER diTypeAndName
                    {
                    if ( !((Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "lockBoxAddress", "Address.isLockBox(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    lb1=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress588); 
                    lb2=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress592); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:255:96: ( '#' )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==12) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:255:96: '#'
                            {
                            match(input,12,FOLLOW_12_in_lockBoxAddress594); 

                            }
                            break;

                    }

                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress599); 
                    pushFollow(FOLLOW_diTypeAndName_in_lockBoxAddress601);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setLockBoxType((lb1!=null?lb1.getText():null) + " " + (lb2!=null?lb2.getText():null));
                    							 address.setLockBoxNumber(quietIntParse((n!=null?n.getText():null)));
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:263:1: generalDeliveryAddress : ({...}?gd= NAME diTypeAndName | {...}?gd1= NAME gd2= NAME diTypeAndName | {...}?gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME diTypeAndName );
    public final void generalDeliveryAddress() throws RecognitionException {
        Token gd=null;
        Token gd1=null;
        Token gd2=null;
        Token gd3=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:264:2: ({...}?gd= NAME diTypeAndName | {...}?gd1= NAME gd2= NAME diTypeAndName | {...}?gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME diTypeAndName )
            int alt19=3;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==NAME) ) {
                int LA19_1 = input.LA(2);

                if ( (LA19_1==NAME) ) {
                    int LA19_2 = input.LA(3);

                    if ( (LA19_2==NAME) ) {
                        int LA19_4 = input.LA(4);

                        if ( (LA19_4==NAME) ) {
                            alt19=3;
                        }
                        else if ( ((Address.isGeneralDelivery(input.LT(1).getText()))) ) {
                            alt19=1;
                        }
                        else if ( ((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                            alt19=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 19, 4, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA19_2==EOF||(LA19_2>=STREETNUMSUFFIX && LA19_2<=NUMBER)) ) {
                        alt19=1;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 19, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA19_1==STREETNUMSUFFIX) ) {
                    alt19=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:264:4: {...}?gd= NAME diTypeAndName
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText())");
                    }
                    gd=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress626); 
                    pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress628);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setGeneralDeliveryName((gd!=null?gd.getText():null));
                    							 address.setType(Address.Type.GD);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:4: {...}?gd1= NAME gd2= NAME diTypeAndName
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    gd1=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress646); 
                    gd2=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress650); 
                    pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress652);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setGeneralDeliveryName((gd1!=null?gd1.getText():null) + " " + (gd2!=null?gd2.getText():null));
                    							 address.setType(Address.Type.GD);
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:274:4: {...}?gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME diTypeAndName
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    gd1=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress674); 
                    gd2=(Token)input.LT(1);
                    if ( input.LA(1)==STREETNUMSUFFIX||input.LA(1)==NAME ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    gd3=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress686); 
                    pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress688);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setGeneralDeliveryName((gd1!=null?gd1.getText():null) + " " + (gd2!=null?gd2.getText():null) + " " + (gd3!=null?gd3.getText():null));
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


    // $ANTLR start "diTypeAndName"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:282:1: diTypeAndName : ({...}?dt= NAME ( diName )* | {...}?dt1= NAME dt2= NAME ( diName )* | {...}?dt1= NAME dt2= NAME dt3= NAME ( diName )* );
    public final void diTypeAndName() throws RecognitionException {
        Token dt=null;
        Token dt1=null;
        Token dt2=null;
        Token dt3=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:2: ({...}?dt= NAME ( diName )* | {...}?dt1= NAME dt2= NAME ( diName )* | {...}?dt1= NAME dt2= NAME dt3= NAME ( diName )* )
            int alt23=3;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==NAME) ) {
                int LA23_1 = input.LA(2);

                if ( (LA23_1==NAME) ) {
                    int LA23_2 = input.LA(3);

                    if ( (LA23_2==NAME) ) {
                        alt23=3;
                    }
                    else if ( ((Address.isDeliveryInstallationType(input.LT(1).getText()))) ) {
                        alt23=1;
                    }
                    else if ( ((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        alt23=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 23, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA23_1==EOF||(LA23_1>=STREETNUMSUFFIX && LA23_1<=NUMBER)) ) {
                    alt23=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }
            switch (alt23) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:4: {...}?dt= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText())");
                    }
                    dt=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName713); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:73: ( diName )*
                    loop20:
                    do {
                        int alt20=2;
                        int LA20_0 = input.LA(1);

                        if ( ((LA20_0>=STREETNUMSUFFIX && LA20_0<=NAME)) ) {
                            alt20=1;
                        }


                        switch (alt20) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:73: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName715);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop20;
                        }
                    } while (true);


                    							 address.setDeliveryInstallationType((dt!=null?dt.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:287:4: {...}?dt1= NAME dt2= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    dt1=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName734); 
                    dt2=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName738); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:287:113: ( diName )*
                    loop21:
                    do {
                        int alt21=2;
                        int LA21_0 = input.LA(1);

                        if ( ((LA21_0>=STREETNUMSUFFIX && LA21_0<=NAME)) ) {
                            alt21=1;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:287:113: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName740);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop21;
                        }
                    } while (true);


                    							 address.setDeliveryInstallationType((dt1!=null?dt1.getText():null) + " " + (dt2!=null?dt2.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:291:4: {...}?dt1= NAME dt2= NAME dt3= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    dt1=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName759); 
                    dt2=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName763); 
                    dt3=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName767); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:291:152: ( diName )*
                    loop22:
                    do {
                        int alt22=2;
                        int LA22_0 = input.LA(1);

                        if ( ((LA22_0>=STREETNUMSUFFIX && LA22_0<=NAME)) ) {
                            alt22=1;
                        }


                        switch (alt22) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:291:152: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName769);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop22;
                        }
                    } while (true);


                    							 address.setDeliveryInstallationType((dt1!=null?dt1.getText():null) + " " + (dt2!=null?dt2.getText():null) + " " + (dt3!=null?dt3.getText():null));
                    							

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
    // $ANTLR end "diTypeAndName"


    // $ANTLR start "diName"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:297:1: diName : stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX ) ;
    public final void diName() throws RecognitionException {
        Token stn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:298:2: (stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:298:4: stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX | STREETNUMSUFFIX )
            {
            stn=(Token)input.LT(1);
            if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NAME) ) {
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
    protected DFA8 dfa8 = new DFA8(this);
    protected DFA10 dfa10 = new DFA10(this);
    protected DFA11 dfa11 = new DFA11(this);
    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA1_eotS =
        "\25\uffff";
    static final String DFA1_eofS =
        "\2\5\1\uffff\1\5\13\uffff\1\20\3\uffff\1\20\1\uffff";
    static final String DFA1_minS =
        "\2\4\1\uffff\2\4\1\uffff\11\0\1\4\3\uffff\1\4\1\0";
    static final String DFA1_maxS =
        "\1\11\1\13\1\uffff\1\10\1\14\1\uffff\11\0\1\14\3\uffff\1\10\1\0";
    static final String DFA1_acceptS =
        "\2\uffff\1\1\2\uffff\1\5\12\uffff\1\4\1\3\1\2\2\uffff";
    static final String DFA1_specialS =
        "\1\14\1\10\2\uffff\1\13\1\uffff\1\15\1\1\1\11\1\2\1\16\1\3\1\6\1"+
        "\0\1\4\1\12\3\uffff\1\7\1\5}>";
    static final String[] DFA1_transitionS = {
            "\1\5\1\1\1\3\1\1\1\4\1\2",
            "\1\6\1\11\1\12\1\10\1\7\2\uffff\1\2",
            "",
            "\1\14\1\15\1\16\1\15\1\13",
            "\1\20\2\uffff\1\21\1\17\3\uffff\1\21",
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
            "\3\20\1\23\1\20\3\uffff\1\21",
            "",
            "",
            "",
            "\4\20\1\24",
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
            return "124:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA1_13 = input.LA(1);

                         
                        int index1_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_13);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA1_7 = input.LA(1);

                         
                        int index1_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA1_9 = input.LA(1);

                         
                        int index1_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA1_11 = input.LA(1);

                         
                        int index1_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA1_14 = input.LA(1);

                         
                        int index1_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_14);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA1_20 = input.LA(1);

                         
                        int index1_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeLockBox())&&(Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText())))) ) {s = 17;}

                        else if ( (((couldBeGD())&&(Address.isGeneralDelivery(input.LT(1).getText())))) ) {s = 16;}

                         
                        input.seek(index1_20);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA1_12 = input.LA(1);

                         
                        int index1_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_12);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA1_19 = input.LA(1);

                         
                        int index1_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_19==EOF||(LA1_19>=STREETNUMSUFFIX && LA1_19<=NUMBER)) && ((couldBeGD()))) {s = 16;}

                        else if ( (LA1_19==NAME) && (((couldBeLockBox())||(couldBeGD())))) {s = 20;}

                         
                        input.seek(index1_19);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA1_1 = input.LA(1);

                         
                        int index1_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_1==11) && ((couldBeUrban()))) {s = 2;}

                        else if ( (LA1_1==STREETNUMSUFFIX) ) {s = 6;}

                        else if ( (LA1_1==EOF) ) {s = 5;}

                        else if ( (LA1_1==NAME) ) {s = 7;}

                        else if ( (LA1_1==NUMBER) ) {s = 8;}

                        else if ( (LA1_1==NUMANDSTREETSUFFIX) ) {s = 9;}

                        else if ( (LA1_1==NUMANDSUFFIX) ) {s = 10;}

                         
                        input.seek(index1_1);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA1_8 = input.LA(1);

                         
                        int index1_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_8);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA1_15 = input.LA(1);

                         
                        int index1_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_15==EOF||(LA1_15>=STREETNUMSUFFIX && LA1_15<=NUMANDSUFFIX)||LA1_15==NAME) && ((couldBeGD()))) {s = 16;}

                        else if ( (LA1_15==12) && ((couldBeLockBox()))) {s = 17;}

                        else if ( (LA1_15==NUMBER) && (((couldBeLockBox())||(couldBeGD())))) {s = 19;}

                         
                        input.seek(index1_15);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA1_4 = input.LA(1);

                         
                        int index1_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_4==NAME) && (((couldBeLockBox())||(couldBeGD())))) {s = 15;}

                        else if ( (LA1_4==STREETNUMSUFFIX) && ((couldBeGD()))) {s = 16;}

                        else if ( (LA1_4==NUMBER||LA1_4==12) && ((couldBeLockBox()))) {s = 17;}

                        else if ( ((((couldBeRural())&&(Address.isRuralRoute(input.LT(1).getText())))||((couldBeRural())&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText())))||((couldBeRural())&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))))) ) {s = 18;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_4);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA1_0 = input.LA(1);

                         
                        int index1_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_0==NUMANDSTREETSUFFIX||LA1_0==NUMBER) ) {s = 1;}

                        else if ( (LA1_0==SUITEANDSTREETNUM) && ((couldBeUrban()))) {s = 2;}

                        else if ( (LA1_0==NUMANDSUFFIX) ) {s = 3;}

                        else if ( (LA1_0==NAME) ) {s = 4;}

                        else if ( (LA1_0==EOF||LA1_0==STREETNUMSUFFIX) ) {s = 5;}

                         
                        input.seek(index1_0);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA1_6 = input.LA(1);

                         
                        int index1_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_6);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA1_10 = input.LA(1);

                         
                        int index1_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_10);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 1, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA8_eotS =
        "\13\uffff";
    static final String DFA8_eofS =
        "\4\uffff\1\5\1\uffff\1\5\4\uffff";
    static final String DFA8_minS =
        "\1\5\1\uffff\1\4\1\uffff\1\4\1\uffff\1\4\4\uffff";
    static final String DFA8_maxS =
        "\1\11\1\uffff\1\10\1\uffff\1\10\1\uffff\1\10\4\uffff";
    static final String DFA8_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\1\uffff\1\4\1\uffff\4\2";
    static final String DFA8_specialS =
        "\13\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\2\1\3\1\2\1\uffff\1\1",
            "",
            "\1\4\1\6\3\5",
            "",
            "\1\10\1\11\1\12\1\11\1\7",
            "",
            "\1\10\1\11\1\12\1\11\1\7",
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
            return "153:1: street : (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ );";
        }
    }
    static final String DFA10_eotS =
        "\13\uffff";
    static final String DFA10_eofS =
        "\1\2\1\5\4\uffff\1\2\4\uffff";
    static final String DFA10_minS =
        "\1\5\1\4\4\uffff\1\4\4\uffff";
    static final String DFA10_maxS =
        "\2\11\4\uffff\1\10\4\uffff";
    static final String DFA10_acceptS =
        "\2\uffff\1\2\3\1\1\uffff\4\1";
    static final String DFA10_specialS =
        "\13\uffff}>";
    static final String[] DFA10_transitionS = {
            "\2\2\1\1\2\2",
            "\1\2\1\4\1\6\1\5\1\3\1\5",
            "",
            "",
            "",
            "",
            "\1\10\1\11\1\12\1\11\1\7",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "226:60: (n= NUMBER )?";
        }
    }
    static final String DFA11_eotS =
        "\13\uffff";
    static final String DFA11_eofS =
        "\1\2\1\4\4\uffff\1\2\4\uffff";
    static final String DFA11_minS =
        "\1\5\1\4\4\uffff\1\4\4\uffff";
    static final String DFA11_maxS =
        "\2\11\4\uffff\1\10\4\uffff";
    static final String DFA11_acceptS =
        "\2\uffff\1\2\3\1\1\uffff\4\1";
    static final String DFA11_specialS =
        "\13\uffff}>";
    static final String[] DFA11_transitionS = {
            "\2\2\1\1\2\2",
            "\1\2\1\3\1\6\1\5\2\4",
            "",
            "",
            "",
            "",
            "\1\10\1\11\1\12\1\11\1\7",
            "",
            "",
            "",
            ""
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
            return "231:100: (n= NUMBER )?";
        }
    }
    static final String DFA12_eotS =
        "\13\uffff";
    static final String DFA12_eofS =
        "\1\2\1\4\4\uffff\1\2\4\uffff";
    static final String DFA12_minS =
        "\1\5\1\4\4\uffff\1\4\4\uffff";
    static final String DFA12_maxS =
        "\2\11\4\uffff\1\10\4\uffff";
    static final String DFA12_acceptS =
        "\2\uffff\1\2\3\1\1\uffff\4\1";
    static final String DFA12_specialS =
        "\13\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\2\1\1\2\2",
            "\1\2\1\3\1\6\1\5\2\4",
            "",
            "",
            "",
            "",
            "\1\10\1\11\1\12\1\11\1\7",
            "",
            "",
            "",
            ""
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
            return "236:139: (n= NUMBER )?";
        }
    }
 

    public static final BitSet FOLLOW_streetAddress_in_address36 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRouteAddress_in_address45 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lockBoxAddress_in_address53 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalDeliveryAddress_in_address61 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedParse_in_address67 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedToken_in_failedParse83 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_set_in_failedToken97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetAddress139 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_streetAddress145 = new BitSet(new long[]{0x00000000000002E0L});
    public static final BitSet FOLLOW_street_in_streetAddress147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUITEANDSTREETNUM_in_street184 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_set_in_street188 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street194 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_set_in_street211 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_set_in_street219 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street225 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NUMANDSUFFIX_in_street243 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street245 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_set_in_street256 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street262 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_streetToken292 = new BitSet(new long[]{0x00000000000000A0L});
    public static final BitSet FOLLOW_set_in_streetToken296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetToken349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRoute_in_ruralRouteAddress428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute449 = new BitSet(new long[]{0x00000000000003E0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute453 = new BitSet(new long[]{0x00000000000003E0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute474 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute478 = new BitSet(new long[]{0x00000000000003E0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute482 = new BitSet(new long[]{0x00000000000003E0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute503 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute507 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute511 = new BitSet(new long[]{0x00000000000003E0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute515 = new BitSet(new long[]{0x00000000000003E0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute518 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_diTypeAndName_in_ruralRouteSuffix538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_ruralRouteSuffix544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress561 = new BitSet(new long[]{0x0000000000001080L});
    public static final BitSet FOLLOW_12_in_lockBoxAddress563 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress568 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_diTypeAndName_in_lockBoxAddress570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress588 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress592 = new BitSet(new long[]{0x0000000000001080L});
    public static final BitSet FOLLOW_12_in_lockBoxAddress594 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress599 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_diTypeAndName_in_lockBoxAddress601 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress626 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress646 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress650 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress652 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress674 = new BitSet(new long[]{0x0000000000000110L});
    public static final BitSet FOLLOW_set_in_generalDeliveryAddress678 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress686 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName713 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName715 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName734 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName738 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName740 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName759 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName763 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName767 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName769 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_set_in_diName792 = new BitSet(new long[]{0x0000000000000002L});

}