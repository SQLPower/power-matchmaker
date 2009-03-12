// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-12 15:00:43

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ROUTESERVICETYPE", "LOCKBOXTYPE", "GD", "DITYPE", "SUITE", "SUFFIXANDDIR", "STREETNUMSUFFIX", "NUMANDSTREETSUFFIX", "STREETDIR", "NUMANDSUFFIX", "NUMBER", "NAME", "SUITEANDSTREETNUM", "WS", "'-'"
    };
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:116:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );
    public final void address() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:2: ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse )
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:117:4: {...}? => streetAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:118:4: {...}? => ruralRouteAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:4: {...}? => lockBoxAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:120:4: {...}? => generalDeliveryAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:121:4: failedParse
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:124:1: failedParse : ( failedToken )* ;
    public final void failedParse() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:2: ( ( failedToken )* )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:4: ( failedToken )*
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:4: ( failedToken )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=ROUTESERVICETYPE && LA2_0<=NAME)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:4: failedToken
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:128:1: failedToken : n= ( ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | NUMANDSTREETSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME ) ;
    public final void failedToken() throws RecognitionException {
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:129:2: (n= ( ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | NUMANDSTREETSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:129:4: n= ( ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | NUMANDSTREETSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME )
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:1: streetAddress : (sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street | street s= SUITE sn= ( NUMBER | NUMANDSTREETSUFFIX ) | street );
    public final void streetAddress() throws RecognitionException {
        Token sn=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:2: (sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street | street s= SUITE sn= ( NUMBER | NUMANDSTREETSUFFIX ) | street )
            int alt3=3;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:136:4: sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street
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

                    match(input,18,FOLLOW_18_in_streetAddress173); 
                    pushFollow(FOLLOW_street_in_streetAddress175);
                    street();

                    state._fsp--;

                     
                    							  address.setSuitePrefix(true);
                    							  address.setSuite((sn!=null?sn.getText():null));
                    							  address.setType(Address.Type.URBAN);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:142:4: street s= SUITE sn= ( NUMBER | NUMANDSTREETSUFFIX )
                    {
                    pushFollow(FOLLOW_street_in_streetAddress192);
                    street();

                    state._fsp--;

                    s=(Token)match(input,SUITE,FOLLOW_SUITE_in_streetAddress196); 
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:149:4: street
                    {
                    pushFollow(FOLLOW_street_in_streetAddress220);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:152:1: street : (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ );
    public final void street() throws RecognitionException {
        Token n=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:153:2: (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ )
            int alt8=4;
            alt8 = dfa8.predict(input);
            switch (alt8) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:153:4: n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+
                    {
                    n=(Token)match(input,SUITEANDSTREETNUM,FOLLOW_SUITEANDSTREETNUM_in_street240); 
                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=SUFFIXANDDIR && input.LA(1)<=NUMANDSTREETSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:153:76: ( streetToken )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==SUFFIXANDDIR||(LA4_0>=NUMANDSTREETSUFFIX && LA4_0<=NAME)) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:153:76: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street252);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:160:4: n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+
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

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:160:86: ( streetToken )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==SUFFIXANDDIR||(LA5_0>=NUMANDSTREETSUFFIX && LA5_0<=NAME)) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:160:86: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street285);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:164:4: n= NUMANDSUFFIX ( streetToken )+
                    {
                    n=(Token)match(input,NUMANDSUFFIX,FOLLOW_NUMANDSUFFIX_in_street303); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:164:19: ( streetToken )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==SUFFIXANDDIR||(LA6_0>=NUMANDSTREETSUFFIX && LA6_0<=NAME)) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:164:19: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street305);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:168:4: n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+
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

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:168:34: ( streetToken )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==SUFFIXANDDIR||(LA7_0>=NUMANDSTREETSUFFIX && LA7_0<=NAME)) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:168:34: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street322);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:172:1: streetToken : ({...}?d= ( STREETDIR | SUFFIXANDDIR ) | {...}?t= NAME | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX ) );
    public final void streetToken() throws RecognitionException {
        Token d=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:2: ({...}?d= ( STREETDIR | SUFFIXANDDIR ) | {...}?t= NAME | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX ) )
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:4: {...}?d= ( STREETDIR | SUFFIXANDDIR )
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:178:4: {...}?t= NAME
                    {
                    if ( !((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText())");
                    }
                    t=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken382); 

                    							 if (address.getStreetType() != null) {
                    							    appendStreetName(address.getStreetType());
                    							 }
                    							 address.setStreetTypePrefix(!hasStreetNameStarted);
                    							 address.setStreetType((t!=null?t.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:187:4: n= ( NAME | NUMBER | NUMANDSUFFIX | NUMANDSTREETSUFFIX )
                    {
                    n=(Token)input.LT(1);
                    if ( input.LA(1)==NUMANDSTREETSUFFIX||(input.LA(1)>=NUMANDSUFFIX && input.LA(1)<=NAME) ) {
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:199:1: ruralRouteAddress : (rs= ROUTESERVICETYPE (n= NUMBER )? (di= DITYPE )? (stn= NAME )? | rs= ROUTESERVICETYPE n= NUMBER street );
    public final void ruralRouteAddress() throws RecognitionException {
        Token rs=null;
        Token n=null;
        Token di=null;
        Token stn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:2: (rs= ROUTESERVICETYPE (n= NUMBER )? (di= DITYPE )? (stn= NAME )? | rs= ROUTESERVICETYPE n= NUMBER street )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==ROUTESERVICETYPE) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==NUMBER) ) {
                    int LA13_2 = input.LA(3);

                    if ( (LA13_2==EOF||LA13_2==DITYPE||LA13_2==NAME) ) {
                        alt13=1;
                    }
                    else if ( (LA13_2==NUMANDSTREETSUFFIX||(LA13_2>=NUMANDSUFFIX && LA13_2<=NUMBER)||LA13_2==SUITEANDSTREETNUM) ) {
                        alt13=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 13, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA13_1==EOF||LA13_1==DITYPE||LA13_1==NAME) ) {
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:4: rs= ROUTESERVICETYPE (n= NUMBER )? (di= DITYPE )? (stn= NAME )?
                    {
                    rs=(Token)match(input,ROUTESERVICETYPE,FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress439); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:25: (n= NUMBER )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==NUMBER) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:25: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRouteAddress443); 

                            }
                            break;

                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:36: (di= DITYPE )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==DITYPE) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:36: di= DITYPE
                            {
                            di=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_ruralRouteAddress448); 

                            }
                            break;

                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:48: (stn= NAME )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==NAME) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:48: stn= NAME
                            {
                            stn=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRouteAddress453); 

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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:208:4: rs= ROUTESERVICETYPE n= NUMBER street
                    {
                    rs=(Token)match(input,ROUTESERVICETYPE,FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress470); 
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRouteAddress474); 
                    pushFollow(FOLLOW_street_in_ruralRouteAddress476);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:215:1: lockBoxAddress : lb= LOCKBOXTYPE n= NUMBER (di= DITYPE )? (stn= NAME )? ;
    public final void lockBoxAddress() throws RecognitionException {
        Token lb=null;
        Token n=null;
        Token di=null;
        Token stn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:216:2: (lb= LOCKBOXTYPE n= NUMBER (di= DITYPE )? (stn= NAME )? )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:216:4: lb= LOCKBOXTYPE n= NUMBER (di= DITYPE )? (stn= NAME )?
            {
            lb=(Token)match(input,LOCKBOXTYPE,FOLLOW_LOCKBOXTYPE_in_lockBoxAddress492); 
            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress496); 
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:216:30: (di= DITYPE )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==DITYPE) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:216:30: di= DITYPE
                    {
                    di=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_lockBoxAddress500); 

                    }
                    break;

            }

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:216:42: (stn= NAME )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==NAME) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:216:42: stn= NAME
                    {
                    stn=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress505); 

                    }
                    break;

            }


            							 address.setLockBoxType((lb!=null?lb.getText():null));
            							 address.setLockBoxNumber(quietIntParse((n!=null?n.getText():null)));
            							 address.setDeliveryInstallationType((di!=null?di.getText():null));
            							 address.setDeliveryInstallationName((stn!=null?stn.getText():null));
            							 address.setType(Address.Type.LOCK_BOX);
            							

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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:226:1: generalDeliveryAddress : gd= GD (t= DITYPE )? (n= NAME )? ;
    public final void generalDeliveryAddress() throws RecognitionException {
        Token gd=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:227:2: (gd= GD (t= DITYPE )? (n= NAME )? )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:227:4: gd= GD (t= DITYPE )? (n= NAME )?
            {
            gd=(Token)match(input,GD,FOLLOW_GD_in_generalDeliveryAddress529); 
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:227:11: (t= DITYPE )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==DITYPE) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:227:11: t= DITYPE
                    {
                    t=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_generalDeliveryAddress533); 

                    }
                    break;

            }

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:227:21: (n= NAME )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==NAME) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:227:21: n= NAME
                    {
                    n=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress538); 

                    }
                    break;

            }


            							 address.setGeneralDeliveryName((gd!=null?gd.getText():null));
            							 address.setDeliveryInstallationType((t!=null?t.getText():null));
            							 address.setDeliveryInstallationName((n!=null?n.getText():null));
            							 address.setType(Address.Type.GD);
            							

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

    // Delegated rules


    protected DFA1 dfa1 = new DFA1(this);
    protected DFA3 dfa3 = new DFA3(this);
    protected DFA8 dfa8 = new DFA8(this);
    static final String DFA1_eotS =
        "\30\uffff";
    static final String DFA1_eofS =
        "\2\7\1\uffff\1\7\1\uffff\1\7\6\uffff\1\7\13\uffff";
    static final String DFA1_minS =
        "\2\4\1\uffff\1\4\1\0\1\4\1\0\1\uffff\4\0\1\4\4\0\1\uffff\1\0\1\uffff"+
        "\3\0\1\uffff";
    static final String DFA1_maxS =
        "\1\20\1\22\1\uffff\1\17\1\0\1\17\1\0\1\uffff\4\0\1\17\4\0\1\uffff"+
        "\1\0\1\uffff\3\0\1\uffff";
    static final String DFA1_acceptS =
        "\2\uffff\1\1\4\uffff\1\5\11\uffff\1\2\1\uffff\1\4\3\uffff\1\3";
    static final String DFA1_specialS =
        "\1\16\1\17\2\uffff\1\0\1\uffff\1\6\1\uffff\1\4\1\13\1\15\1\7\1\uffff"+
        "\1\11\1\2\1\3\1\12\1\uffff\1\5\1\uffff\1\14\1\10\1\1\1\uffff}>";
    static final String[] DFA1_transitionS = {
            "\1\4\1\5\1\6\4\7\1\1\1\7\1\3\1\1\1\7\1\2",
            "\5\7\1\10\1\14\1\11\1\12\2\15\1\13\2\uffff\1\2",
            "",
            "\5\7\1\16\1\7\1\20\1\16\2\20\1\17",
            "\1\uffff",
            "\12\7\1\22\1\7",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\5\7\1\24\1\7\1\26\1\24\2\26\1\25",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            ""
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
            return "116:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA1_4 = input.LA(1);

                         
                        int index1_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeRural())) ) {s = 17;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_4);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA1_22 = input.LA(1);

                         
                        int index1_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_22);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA1_14 = input.LA(1);

                         
                        int index1_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_14);
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
                        int LA1_18 = input.LA(1);

                         
                        int index1_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeLockBox())) ) {s = 23;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_18);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA1_6 = input.LA(1);

                         
                        int index1_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeGD())) ) {s = 19;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA1_11 = input.LA(1);

                         
                        int index1_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_11);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA1_21 = input.LA(1);

                         
                        int index1_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_21);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA1_13 = input.LA(1);

                         
                        int index1_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_13);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA1_16 = input.LA(1);

                         
                        int index1_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_16);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA1_9 = input.LA(1);

                         
                        int index1_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_9);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA1_20 = input.LA(1);

                         
                        int index1_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_20);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA1_10 = input.LA(1);

                         
                        int index1_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 2;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index1_10);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA1_0 = input.LA(1);

                         
                        int index1_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_0==NUMANDSTREETSUFFIX||LA1_0==NUMBER) ) {s = 1;}

                        else if ( (LA1_0==SUITEANDSTREETNUM) && ((couldBeUrban()))) {s = 2;}

                        else if ( (LA1_0==NUMANDSUFFIX) ) {s = 3;}

                        else if ( (LA1_0==ROUTESERVICETYPE) ) {s = 4;}

                        else if ( (LA1_0==LOCKBOXTYPE) ) {s = 5;}

                        else if ( (LA1_0==GD) ) {s = 6;}

                        else if ( (LA1_0==EOF||(LA1_0>=DITYPE && LA1_0<=STREETNUMSUFFIX)||LA1_0==STREETDIR||LA1_0==NAME) ) {s = 7;}

                         
                        input.seek(index1_0);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA1_1 = input.LA(1);

                         
                        int index1_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_1==18) && ((couldBeUrban()))) {s = 2;}

                        else if ( (LA1_1==SUFFIXANDDIR) ) {s = 8;}

                        else if ( (LA1_1==NUMANDSTREETSUFFIX) ) {s = 9;}

                        else if ( (LA1_1==STREETDIR) ) {s = 10;}

                        else if ( (LA1_1==NAME) ) {s = 11;}

                        else if ( (LA1_1==STREETNUMSUFFIX) ) {s = 12;}

                        else if ( ((LA1_1>=NUMANDSUFFIX && LA1_1<=NUMBER)) ) {s = 13;}

                        else if ( (LA1_1==EOF||(LA1_1>=ROUTESERVICETYPE && LA1_1<=SUITE)) ) {s = 7;}

                         
                        input.seek(index1_1);
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
        "\32\uffff";
    static final String DFA3_eofS =
        "\5\uffff\4\22\1\uffff\1\22\1\uffff\6\22\2\uffff\6\22";
    static final String DFA3_minS =
        "\1\13\3\11\1\uffff\4\10\1\11\1\10\1\11\6\10\2\uffff\6\10";
    static final String DFA3_maxS =
        "\1\20\1\22\1\13\1\17\1\uffff\15\17\2\uffff\6\17";
    static final String DFA3_acceptS =
        "\4\uffff\1\1\15\uffff\1\3\1\2\6\uffff";
    static final String DFA3_specialS =
        "\32\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\1\1\uffff\1\3\1\1\1\uffff\1\2",
            "\1\5\1\11\1\6\1\7\2\12\1\10\2\uffff\1\4",
            "\3\13",
            "\1\14\1\uffff\1\16\1\14\2\16\1\15",
            "",
            "\1\23\1\17\1\uffff\1\21\1\17\2\21\1\20",
            "\1\23\1\17\1\uffff\1\21\1\17\2\21\1\20",
            "\1\23\1\7\1\uffff\1\12\1\7\2\12\1\10",
            "\1\23\1\7\1\uffff\1\12\1\7\2\12\1\10",
            "\1\24\1\uffff\1\26\1\24\2\26\1\25",
            "\1\23\1\7\1\uffff\1\12\1\7\2\12\1\10",
            "\1\27\1\uffff\1\31\1\27\2\31\1\30",
            "\1\23\1\14\1\uffff\1\16\1\14\2\16\1\15",
            "\1\23\1\14\1\uffff\1\16\1\14\2\16\1\15",
            "\1\23\1\14\1\uffff\1\16\1\14\2\16\1\15",
            "\1\23\1\17\1\uffff\1\21\1\17\2\21\1\20",
            "\1\23\1\17\1\uffff\1\21\1\17\2\21\1\20",
            "\1\23\1\17\1\uffff\1\21\1\17\2\21\1\20",
            "",
            "",
            "\1\23\1\24\1\uffff\1\26\1\24\2\26\1\25",
            "\1\23\1\24\1\uffff\1\26\1\24\2\26\1\25",
            "\1\23\1\24\1\uffff\1\26\1\24\2\26\1\25",
            "\1\23\1\27\1\uffff\1\31\1\27\2\31\1\30",
            "\1\23\1\27\1\uffff\1\31\1\27\2\31\1\30",
            "\1\23\1\27\1\uffff\1\31\1\27\2\31\1\30"
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
            return "135:1: streetAddress : (sn= ( NUMBER | NUMANDSTREETSUFFIX ) '-' street | street s= SUITE sn= ( NUMBER | NUMANDSTREETSUFFIX ) | street );";
        }
    }
    static final String DFA8_eotS =
        "\13\uffff";
    static final String DFA8_eofS =
        "\4\uffff\2\6\5\uffff";
    static final String DFA8_minS =
        "\1\13\1\uffff\1\11\1\uffff\2\10\5\uffff";
    static final String DFA8_maxS =
        "\1\20\1\uffff\1\17\1\uffff\2\17\5\uffff";
    static final String DFA8_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\2\uffff\1\4\4\2";
    static final String DFA8_specialS =
        "\13\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\2\1\uffff\1\3\1\2\1\uffff\1\1",
            "",
            "\1\4\1\7\1\5\4\6",
            "",
            "\1\6\1\10\1\uffff\1\12\1\10\2\12\1\11",
            "\1\6\1\10\1\uffff\1\12\1\10\2\12\1\11",
            "",
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
            return "152:1: street : (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) s= ( STREETNUMSUFFIX | SUFFIXANDDIR | NUMANDSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= ( NUMBER | NUMANDSTREETSUFFIX ) ( streetToken )+ );";
        }
    }
 

    public static final BitSet FOLLOW_streetAddress_in_address36 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRouteAddress_in_address45 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lockBoxAddress_in_address53 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalDeliveryAddress_in_address61 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedParse_in_address67 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedToken_in_failedParse83 = new BitSet(new long[]{0x000000000000FFF2L});
    public static final BitSet FOLLOW_set_in_failedToken97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetAddress167 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_18_in_streetAddress173 = new BitSet(new long[]{0x0000000000016800L});
    public static final BitSet FOLLOW_street_in_streetAddress175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress192 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_SUITE_in_streetAddress196 = new BitSet(new long[]{0x0000000000004800L});
    public static final BitSet FOLLOW_set_in_streetAddress200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress220 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUITEANDSTREETNUM_in_street240 = new BitSet(new long[]{0x0000000000000E00L});
    public static final BitSet FOLLOW_set_in_street244 = new BitSet(new long[]{0x000000000000FA00L});
    public static final BitSet FOLLOW_streetToken_in_street252 = new BitSet(new long[]{0x000000000000FA02L});
    public static final BitSet FOLLOW_set_in_street269 = new BitSet(new long[]{0x0000000000000E00L});
    public static final BitSet FOLLOW_set_in_street277 = new BitSet(new long[]{0x000000000000FA00L});
    public static final BitSet FOLLOW_streetToken_in_street285 = new BitSet(new long[]{0x000000000000FA02L});
    public static final BitSet FOLLOW_NUMANDSUFFIX_in_street303 = new BitSet(new long[]{0x000000000000FA00L});
    public static final BitSet FOLLOW_streetToken_in_street305 = new BitSet(new long[]{0x000000000000FA02L});
    public static final BitSet FOLLOW_set_in_street316 = new BitSet(new long[]{0x000000000000FA00L});
    public static final BitSet FOLLOW_streetToken_in_street322 = new BitSet(new long[]{0x000000000000FA02L});
    public static final BitSet FOLLOW_set_in_streetToken351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetToken382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress439 = new BitSet(new long[]{0x000000000000C082L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRouteAddress443 = new BitSet(new long[]{0x0000000000008082L});
    public static final BitSet FOLLOW_DITYPE_in_ruralRouteAddress448 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_NAME_in_ruralRouteAddress453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress470 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRouteAddress474 = new BitSet(new long[]{0x0000000000016800L});
    public static final BitSet FOLLOW_street_in_ruralRouteAddress476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOCKBOXTYPE_in_lockBoxAddress492 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress496 = new BitSet(new long[]{0x0000000000008082L});
    public static final BitSet FOLLOW_DITYPE_in_lockBoxAddress500 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GD_in_generalDeliveryAddress529 = new BitSet(new long[]{0x0000000000008082L});
    public static final BitSet FOLLOW_DITYPE_in_generalDeliveryAddress533 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress538 = new BitSet(new long[]{0x0000000000000002L});

}