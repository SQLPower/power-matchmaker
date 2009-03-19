// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-18 17:21:33

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "STREETNUMSUFFIX", "NUMERICSTREETSUFFIX", "NUMANDSUFFIX", "NUMBER", "NAME", "SUITEANDSTREETNUM", "WS", "'-'", "'#'"
    };
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



    /**
     *  This is an odd tri state boolean. It will be null if the address starts as
     *  anything but an urban or rural address. It will be true if it starts as an
     *  urban address and false if it starts as a rural address. Only urban and rural
     *  addresses should care about this as it is used to distinguish between rural,
     *  urban, and mixed addresses. The fun part is mixed addresses can start as urban
     *  or rural and some parts from one could possiblly come up in the other.
     */
    private Boolean startsUrbanNotRural = null;

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

    //XXX: might be able to use a variable in some kind of global scope
    public boolean setStartsUrbanNotRural(boolean b) {
      startsUrbanNotRural = b;
      return true;
    }




    // $ANTLR start "address"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:140:1: address : ({...}? => streetAddressStart | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );
    public final void address() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:141:2: ({...}? => streetAddressStart | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse )
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:141:4: {...}? => streetAddressStart
                    {
                    if ( !((couldBeUrban())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeUrban()");
                    }
                    pushFollow(FOLLOW_streetAddressStart_in_address35);
                    streetAddressStart();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:142:4: {...}? => ruralRouteAddress
                    {
                    if ( !((couldBeRural())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeRural()");
                    }
                    pushFollow(FOLLOW_ruralRouteAddress_in_address44);
                    ruralRouteAddress();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:143:4: {...}? => lockBoxAddress
                    {
                    if ( !((couldBeLockBox())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeLockBox()");
                    }
                    pushFollow(FOLLOW_lockBoxAddress_in_address52);
                    lockBoxAddress();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:144:4: {...}? => generalDeliveryAddress
                    {
                    if ( !((couldBeGD())) ) {
                        throw new FailedPredicateException(input, "address", "couldBeGD()");
                    }
                    pushFollow(FOLLOW_generalDeliveryAddress_in_address60);
                    generalDeliveryAddress();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:145:4: failedParse
                    {
                    pushFollow(FOLLOW_failedParse_in_address66);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:148:1: failedParse : ( failedToken )* ;
    public final void failedParse() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:149:2: ( ( failedToken )* )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:149:4: ( failedToken )*
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:149:4: ( failedToken )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=STREETNUMSUFFIX && LA2_0<=NAME)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:149:4: failedToken
            	    {
            	    pushFollow(FOLLOW_failedToken_in_failedParse82);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:152:1: failedToken : n= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME ) ;
    public final void failedToken() throws RecognitionException {
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:153:2: (n= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:153:4: n= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME )
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


            							 if (address.getFailedParsingString() == null) {
            							    address.setFailedParsingString((n!=null?n.getText():null));
            							 } else {
            							    address.setFailedParsingString(address.getFailedParsingString() + " " + (n!=null?n.getText():null));
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
    // $ANTLR end "failedToken"


    // $ANTLR start "streetAddressStart"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:162:1: streetAddressStart : {...}? streetAddress ;
    public final void streetAddressStart() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:163:2: ({...}? streetAddress )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:163:4: {...}? streetAddress
            {
            if ( !((setStartsUrbanNotRural(true))) ) {
                throw new FailedPredicateException(input, "streetAddressStart", "setStartsUrbanNotRural(true)");
            }
            pushFollow(FOLLOW_streetAddress_in_streetAddressStart135);
            streetAddress();

            state._fsp--;


            							  address.setType(PostalCode.RecordType.STREET);
            							  if (address.isUrbanBeforeRural() != null) {
            							    address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
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
    // $ANTLR end "streetAddressStart"


    // $ANTLR start "streetAddress"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:172:1: streetAddress : (sn= NUMBER '-' street | street );
    public final void streetAddress() throws RecognitionException {
        Token sn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:2: (sn= NUMBER '-' street | street )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==NUMBER) ) {
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:4: sn= NUMBER '-' street
                    {
                    sn=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress163); 
                    match(input,11,FOLLOW_11_in_streetAddress165); 
                    pushFollow(FOLLOW_street_in_streetAddress167);
                    street();

                    state._fsp--;

                     
                    							  address.setSuitePrefix(true);
                    							  address.setSuite((sn!=null?sn.getText():null));

                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:179:4: street
                    {
                    pushFollow(FOLLOW_street_in_streetAddress184);
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
    // $ANTLR end "streetAddress"


    // $ANTLR start "street"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:182:1: street : (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) ( streetToken )+ | n= NUMBER s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= NUMBER ( streetToken )+ );
    public final void street() throws RecognitionException {
        Token n=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:183:2: (n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) ( streetToken )+ | n= NUMBER s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= NUMBER ( streetToken )+ )
            int alt8=4;
            switch ( input.LA(1) ) {
            case SUITEANDSTREETNUM:
                {
                alt8=1;
                }
                break;
            case NUMBER:
                {
                switch ( input.LA(2) ) {
                case STREETNUMSUFFIX:
                    {
                    alt8=2;
                    }
                    break;
                case NUMANDSUFFIX:
                case NUMBER:
                case NAME:
                    {
                    alt8=4;
                    }
                    break;
                case NUMERICSTREETSUFFIX:
                    {
                    alt8=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 2, input);

                    throw nvae;
                }

                }
                break;
            case NUMANDSUFFIX:
                {
                alt8=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:183:4: n= SUITEANDSTREETNUM s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) ( streetToken )+
                    {
                    n=(Token)match(input,SUITEANDSTREETNUM,FOLLOW_SUITEANDSTREETNUM_in_street198); 
                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NUMERICSTREETSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:183:64: ( streetToken )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt4=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt4=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt4=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                        case NUMANDSUFFIX:
                            {
                            alt4=1;
                            }
                            break;

                        }

                        switch (alt4) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:183:64: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street208);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:190:4: n= NUMBER s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) ( streetToken )+
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_street225); 
                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NUMERICSTREETSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:190:53: ( streetToken )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt5=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt5=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt5=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                        case NUMANDSUFFIX:
                            {
                            alt5=1;
                            }
                            break;

                        }

                        switch (alt5) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:190:53: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street235);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:194:4: n= NUMANDSUFFIX ( streetToken )+
                    {
                    n=(Token)match(input,NUMANDSUFFIX,FOLLOW_NUMANDSUFFIX_in_street253); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:194:19: ( streetToken )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt6=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt6=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt6=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                        case NUMANDSUFFIX:
                            {
                            alt6=1;
                            }
                            break;

                        }

                        switch (alt6) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:194:19: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street255);
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:198:4: n= NUMBER ( streetToken )+
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_street266); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:198:13: ( streetToken )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt7=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt7=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt7=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                        case NUMANDSUFFIX:
                            {
                            alt7=1;
                            }
                            break;

                        }

                        switch (alt7) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:198:13: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street268);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:202:1: streetToken : ({...}? =>s= NAME sn= NUMBER | {...}?d= ( NAME | STREETNUMSUFFIX ) | {...}?t= NAME | {...}?n= NUMBER | {...}? ruralRoute | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) );
    public final void streetToken() throws RecognitionException {
        Token s=null;
        Token sn=null;
        Token d=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:203:2: ({...}? =>s= NAME sn= NUMBER | {...}?d= ( NAME | STREETNUMSUFFIX ) | {...}?t= NAME | {...}?n= NUMBER | {...}? ruralRoute | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) )
            int alt9=6;
            alt9 = dfa9.predict(input);
            switch (alt9) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:203:4: {...}? =>s= NAME sn= NUMBER
                    {
                    if ( !((Address.isSuiteType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "Address.isSuiteType(input.LT(1).getText())");
                    }
                    s=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken298); 
                    sn=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetToken302); 

                    							 address.setSuitePrefix(false);
                    							 address.setSuiteType((s!=null?s.getText():null));
                    							 address.setSuite((sn!=null?sn.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:209:4: {...}?d= ( NAME | STREETNUMSUFFIX )
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:214:4: {...}?t= NAME
                    {
                    if ( !((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText())");
                    }
                    t=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken351); 

                    							 if (address.getStreetType() != null) {
                    							    appendStreetName(address.getStreetType());
                    							 }
                    							 if (!address.isSuitePrefix() && address.getSuite() != null) {
                    							    if (address.getSuiteType() != null) {
                    							       appendStreetName(address.getSuiteType());
                    							       address.setSuiteType(null);
                    							    }
                    							    appendStreetName(address.getSuite());
                    							    address.setSuite(null);
                    							 }
                    							 address.setStreetTypePrefix(!hasStreetNameStarted);
                    							 address.setStreetType((t!=null?t.getText():null));
                    							

                    }
                    break;
                case 4 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:4: {...}?n= NUMBER
                    {
                    if ( !((hasStreetNameStarted)) ) {
                        throw new FailedPredicateException(input, "streetToken", "hasStreetNameStarted");
                    }
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetToken369); 

                    							 address.setSuitePrefix(false);
                    							 address.setSuite((n!=null?n.getText():null));
                    							

                    }
                    break;
                case 5 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:235:4: {...}? ruralRoute
                    {
                    if ( !((hasStreetNameStarted && startsUrbanNotRural)) ) {
                        throw new FailedPredicateException(input, "streetToken", "hasStreetNameStarted && startsUrbanNotRural");
                    }
                    pushFollow(FOLLOW_ruralRoute_in_streetToken385);
                    ruralRoute();

                    state._fsp--;


                    							 address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
                    							 address.setUrbanBeforeRural(true);
                    							

                    }
                    break;
                case 6 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:241:4: n= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX )
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:263:1: ruralRouteAddress : {...}? ruralRoute ;
    public final void ruralRouteAddress() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:264:2: ({...}? ruralRoute )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:264:4: {...}? ruralRoute
            {
            if ( !((setStartsUrbanNotRural(false))) ) {
                throw new FailedPredicateException(input, "ruralRouteAddress", "setStartsUrbanNotRural(false)");
            }
            pushFollow(FOLLOW_ruralRoute_in_ruralRouteAddress450);
            ruralRoute();

            state._fsp--;

            address.setType(PostalCode.RecordType.ROUTE);
            							  if (address.isUrbanBeforeRural() != null) {
            							    address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
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
    // $ANTLR end "ruralRouteAddress"


    // $ANTLR start "ruralRoute"
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:272:1: ruralRoute : ({...}? =>rs= NAME (n= NUMBER )? ruralRouteSuffix | {...}? =>rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix | {...}? =>rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix );
    public final void ruralRoute() throws RecognitionException {
        Token rs=null;
        Token n=null;
        Token rs1=null;
        Token rs2=null;
        Token rs3=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:273:2: ({...}? =>rs= NAME (n= NUMBER )? ruralRouteSuffix | {...}? =>rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix | {...}? =>rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix )
            int alt13=3;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==NAME) && (((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isRuralRoute(input.LT(1).getText()))||(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==NAME) && (((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isRuralRoute(input.LT(1).getText()))||(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                    int LA13_2 = input.LA(3);

                    if ( (LA13_2==NAME) && ((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())))) {
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
                else if ( (LA13_1==EOF||(LA13_1>=STREETNUMSUFFIX && LA13_1<=NUMBER)||LA13_1==SUITEANDSTREETNUM) && ((Address.isRuralRoute(input.LT(1).getText())))) {
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:273:4: {...}? =>rs= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText())");
                    }
                    rs=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute480); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:273:62: (n= NUMBER )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==NUMBER) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:273:62: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute484); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute487);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs!=null?rs.getText():null));
                    							 address.setRuralRouteNumber((n!=null?n.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:278:4: {...}? =>rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    rs1=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute506); 
                    rs2=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute510); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:278:102: (n= NUMBER )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==NUMBER) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:278:102: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute514); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute517);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs1!=null?rs1.getText():null) + " " + (rs2!=null?rs2.getText():null));
                    							 address.setRuralRouteNumber((n!=null?n.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:4: {...}? =>rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    rs1=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute536); 
                    rs2=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute540); 
                    rs3=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute544); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:141: (n= NUMBER )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==NUMBER) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:283:141: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute548); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute551);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs1!=null?rs1.getText():null) + " " + (rs2!=null?rs2.getText():null) + " " + (rs3!=null?rs3.getText():null));
                    							 address.setRuralRouteNumber((n!=null?n.getText():null));
                    							

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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:290:1: ruralRouteSuffix : ( ( diTypeAndName )? | {...}? streetAddress );
    public final void ruralRouteSuffix() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:291:2: ( ( diTypeAndName )? | {...}? streetAddress )
            int alt15=2;
            switch ( input.LA(1) ) {
            case EOF:
            case STREETNUMSUFFIX:
            case NUMERICSTREETSUFFIX:
            case NAME:
                {
                alt15=1;
                }
                break;
            case NUMBER:
                {
                int LA15_2 = input.LA(2);

                if ( (!(((!startsUrbanNotRural)))) ) {
                    alt15=1;
                }
                else if ( ((!startsUrbanNotRural)) ) {
                    alt15=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 2, input);

                    throw nvae;
                }
                }
                break;
            case NUMANDSUFFIX:
                {
                int LA15_3 = input.LA(2);

                if ( (!(((!startsUrbanNotRural)))) ) {
                    alt15=1;
                }
                else if ( ((!startsUrbanNotRural)) ) {
                    alt15=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 3, input);

                    throw nvae;
                }
                }
                break;
            case SUITEANDSTREETNUM:
                {
                alt15=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }

            switch (alt15) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:291:4: ( diTypeAndName )?
                    {
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:291:4: ( diTypeAndName )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==NAME) ) {
                        int LA14_1 = input.LA(2);

                        if ( (((Address.isDeliveryInstallationType(input.LT(1).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText())))) ) {
                            alt14=1;
                        }
                    }
                    switch (alt14) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:291:4: diTypeAndName
                            {
                            pushFollow(FOLLOW_diTypeAndName_in_ruralRouteSuffix571);
                            diTypeAndName();

                            state._fsp--;


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:292:4: {...}? streetAddress
                    {
                    if ( !((!startsUrbanNotRural)) ) {
                        throw new FailedPredicateException(input, "ruralRouteSuffix", "!startsUrbanNotRural");
                    }
                    pushFollow(FOLLOW_streetAddress_in_ruralRouteSuffix579);
                    streetAddress();

                    state._fsp--;


                    							 address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
                    							 address.setUrbanBeforeRural(false);
                    							

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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:299:1: lockBoxAddress : ({...}? =>lb= NAME ( '#' )? n= NUMBER ( diTypeAndName )? | {...}? =>lb1= NAME lb2= NAME ( '#' )? n= NUMBER ( diTypeAndName )? );
    public final void lockBoxAddress() throws RecognitionException {
        Token lb=null;
        Token n=null;
        Token lb1=null;
        Token lb2=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:300:2: ({...}? =>lb= NAME ( '#' )? n= NUMBER ( diTypeAndName )? | {...}? =>lb1= NAME lb2= NAME ( '#' )? n= NUMBER ( diTypeAndName )? )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==NAME) && (((Address.isLockBox(input.LT(1).getText()))||(Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1==NAME) && ((Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText())))) {
                    alt20=2;
                }
                else if ( (LA20_1==NUMBER||LA20_1==12) && ((Address.isLockBox(input.LT(1).getText())))) {
                    alt20=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:300:4: {...}? =>lb= NAME ( '#' )? n= NUMBER ( diTypeAndName )?
                    {
                    if ( !((Address.isLockBox(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "lockBoxAddress", "Address.isLockBox(input.LT(1).getText())");
                    }
                    lb=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress609); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:300:58: ( '#' )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==12) ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:300:58: '#'
                            {
                            match(input,12,FOLLOW_12_in_lockBoxAddress611); 

                            }
                            break;

                    }

                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress616); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:300:72: ( diTypeAndName )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:300:72: diTypeAndName
                            {
                            pushFollow(FOLLOW_diTypeAndName_in_lockBoxAddress618);
                            diTypeAndName();

                            state._fsp--;


                            }
                            break;

                    }


                    							 address.setLockBoxType((lb!=null?lb.getText():null));
                    							 address.setLockBoxNumber((n!=null?n.getText():null));
                    							 address.setType(PostalCode.RecordType.LOCK_BOX);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:306:4: {...}? =>lb1= NAME lb2= NAME ( '#' )? n= NUMBER ( diTypeAndName )?
                    {
                    if ( !((Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "lockBoxAddress", "Address.isLockBox(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    lb1=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress638); 
                    lb2=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress642); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:306:98: ( '#' )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==12) ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:306:98: '#'
                            {
                            match(input,12,FOLLOW_12_in_lockBoxAddress644); 

                            }
                            break;

                    }

                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress649); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:306:112: ( diTypeAndName )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:306:112: diTypeAndName
                            {
                            pushFollow(FOLLOW_diTypeAndName_in_lockBoxAddress651);
                            diTypeAndName();

                            state._fsp--;


                            }
                            break;

                    }


                    							 address.setLockBoxType((lb1!=null?lb1.getText():null) + " " + (lb2!=null?lb2.getText():null));
                    							 address.setLockBoxNumber((n!=null?n.getText():null));
                    							 address.setType(PostalCode.RecordType.LOCK_BOX);
                    							

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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:314:1: generalDeliveryAddress : ({...}? =>gd= NAME ( diTypeAndName )? | {...}? =>gd1= NAME gd2= NAME ( diTypeAndName )? | {...}? =>gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME ( diTypeAndName )? );
    public final void generalDeliveryAddress() throws RecognitionException {
        Token gd=null;
        Token gd1=null;
        Token gd2=null;
        Token gd3=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:315:2: ({...}? =>gd= NAME ( diTypeAndName )? | {...}? =>gd1= NAME gd2= NAME ( diTypeAndName )? | {...}? =>gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME ( diTypeAndName )? )
            int alt24=3;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==NAME) && (((Address.isGeneralDelivery(input.LT(1).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                int LA24_1 = input.LA(2);

                if ( (LA24_1==NAME) && (((Address.isGeneralDelivery(input.LT(1).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                    int LA24_2 = input.LA(3);

                    if ( (LA24_2==NAME) && ((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())))) {
                        alt24=3;
                    }
                    else if ( ((Address.isGeneralDelivery(input.LT(1).getText()))) ) {
                        alt24=1;
                    }
                    else if ( ((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        alt24=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 24, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA24_1==STREETNUMSUFFIX) && ((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())))) {
                    alt24=3;
                }
                else if ( (LA24_1==EOF) && ((Address.isGeneralDelivery(input.LT(1).getText())))) {
                    alt24=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 24, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:315:4: {...}? =>gd= NAME ( diTypeAndName )?
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText())");
                    }
                    gd=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress678); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:315:66: ( diTypeAndName )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:315:66: diTypeAndName
                            {
                            pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress680);
                            diTypeAndName();

                            state._fsp--;


                            }
                            break;

                    }


                    							 address.setGeneralDeliveryName((gd!=null?gd.getText():null));
                    							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:320:4: {...}? =>gd1= NAME gd2= NAME ( diTypeAndName )?
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    gd1=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress700); 
                    gd2=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress704); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:320:106: ( diTypeAndName )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:320:106: diTypeAndName
                            {
                            pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress706);
                            diTypeAndName();

                            state._fsp--;


                            }
                            break;

                    }


                    							 address.setGeneralDeliveryName((gd1!=null?gd1.getText():null) + " " + (gd2!=null?gd2.getText():null));
                    							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:325:4: {...}? =>gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME ( diTypeAndName )?
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    gd1=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress730); 
                    gd2=(Token)input.LT(1);
                    if ( input.LA(1)==STREETNUMSUFFIX||input.LA(1)==NAME ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    gd3=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress742); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:326:49: ( diTypeAndName )?
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( (LA23_0==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                        alt23=1;
                    }
                    switch (alt23) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:326:49: diTypeAndName
                            {
                            pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress744);
                            diTypeAndName();

                            state._fsp--;


                            }
                            break;

                    }


                    							 address.setGeneralDeliveryName((gd1!=null?gd1.getText():null) + " " + (gd2!=null?gd2.getText():null) + " " + (gd3!=null?gd3.getText():null));
                    							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
                    							

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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:333:1: diTypeAndName : ({...}? =>dt= NAME ( diName )* | {...}? =>dt1= NAME dt2= NAME ( diName )* | {...}? =>dt1= NAME dt2= NAME dt3= NAME ( diName )* );
    public final void diTypeAndName() throws RecognitionException {
        Token dt=null;
        Token dt1=null;
        Token dt2=null;
        Token dt3=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:334:2: ({...}? =>dt= NAME ( diName )* | {...}? =>dt1= NAME dt2= NAME ( diName )* | {...}? =>dt1= NAME dt2= NAME dt3= NAME ( diName )* )
            int alt28=3;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                int LA28_1 = input.LA(2);

                if ( (LA28_1==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                    int LA28_2 = input.LA(3);

                    if ( (LA28_2==NAME) && ((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())))) {
                        alt28=3;
                    }
                    else if ( ((Address.isDeliveryInstallationType(input.LT(1).getText()))) ) {
                        alt28=1;
                    }
                    else if ( ((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        alt28=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 28, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA28_1==EOF||(LA28_1>=STREETNUMSUFFIX && LA28_1<=NUMBER)) && ((Address.isDeliveryInstallationType(input.LT(1).getText())))) {
                    alt28=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 28, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:334:4: {...}? =>dt= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText())");
                    }
                    dt=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName771); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:334:75: ( diName )*
                    loop25:
                    do {
                        int alt25=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt25=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt25=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt25=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                        case NUMANDSUFFIX:
                            {
                            alt25=1;
                            }
                            break;

                        }

                        switch (alt25) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:334:75: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName773);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);


                    							 address.setDeliveryInstallationType((dt!=null?dt.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:338:4: {...}? =>dt1= NAME dt2= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    dt1=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName793); 
                    dt2=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName797); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:338:115: ( diName )*
                    loop26:
                    do {
                        int alt26=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt26=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt26=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt26=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                        case NUMANDSUFFIX:
                            {
                            alt26=1;
                            }
                            break;

                        }

                        switch (alt26) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:338:115: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName799);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);


                    							 address.setDeliveryInstallationType((dt1!=null?dt1.getText():null) + " " + (dt2!=null?dt2.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:342:4: {...}? =>dt1= NAME dt2= NAME dt3= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    dt1=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName819); 
                    dt2=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName823); 
                    dt3=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName827); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:342:154: ( diName )*
                    loop27:
                    do {
                        int alt27=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt27=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt27=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt27=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                        case NUMANDSUFFIX:
                            {
                            alt27=1;
                            }
                            break;

                        }

                        switch (alt27) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:342:154: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName829);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop27;
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:348:1: diName : stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) ;
    public final void diName() throws RecognitionException {
        Token stn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:349:2: (stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:349:4: stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX )
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
    protected DFA9 dfa9 = new DFA9(this);
    static final String DFA1_eotS =
        "\24\uffff";
    static final String DFA1_eofS =
        "\2\5\1\uffff\1\5\20\uffff";
    static final String DFA1_minS =
        "\2\4\1\uffff\1\4\1\7\1\uffff\11\0\5\uffff";
    static final String DFA1_maxS =
        "\1\11\1\13\1\uffff\1\10\1\14\1\uffff\11\0\5\uffff";
    static final String DFA1_acceptS =
        "\2\uffff\1\1\2\uffff\1\5\11\uffff\3\3\1\2\1\4";
    static final String DFA1_specialS =
        "\1\5\1\12\2\uffff\1\3\1\uffff\1\13\1\6\1\0\1\2\1\4\1\1\1\7\1\10"+
        "\1\11\5\uffff}>";
    static final String[] DFA1_transitionS = {
            "\2\5\1\3\1\1\1\4\1\2",
            "\1\6\1\10\1\12\1\11\1\7\2\uffff\1\2",
            "",
            "\1\14\2\16\1\15\1\13",
            "\1\20\1\17\3\uffff\1\21",
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
            "",
            "",
            "",
            "",
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
            return "140:1: address : ({...}? => streetAddressStart | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA1_8 = input.LA(1);

                         
                        int index1_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA1_11 = input.LA(1);

                         
                        int index1_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA1_9 = input.LA(1);

                         
                        int index1_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA1_4 = input.LA(1);

                         
                        int index1_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_4==NAME) && (((couldBeLockBox())&&(Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText()))))) {s = 15;}

                        else if ( (LA1_4==NUMBER) && (((couldBeLockBox())&&(Address.isLockBox(input.LT(1).getText()))))) {s = 16;}

                        else if ( (LA1_4==12) && (((couldBeLockBox())&&(Address.isLockBox(input.LT(1).getText()))))) {s = 17;}

                        else if ( (((((couldBeRural())&&(setStartsUrbanNotRural(false)))&&(Address.isRuralRoute(input.LT(1).getText())))||(((couldBeRural())&&(setStartsUrbanNotRural(false)))&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText())))||(((couldBeRural())&&(setStartsUrbanNotRural(false)))&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))))) ) {s = 18;}

                        else if ( ((((couldBeGD())&&(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText())))||((couldBeGD())&&(Address.isGeneralDelivery(input.LT(1).getText())))||((couldBeGD())&&(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))))) ) {s = 19;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA1_10 = input.LA(1);

                         
                        int index1_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_10);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA1_0 = input.LA(1);

                         
                        int index1_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_0==NUMBER) ) {s = 1;}

                        else if ( (LA1_0==SUITEANDSTREETNUM) && ((couldBeUrban()))) {s = 2;}

                        else if ( (LA1_0==NUMANDSUFFIX) ) {s = 3;}

                        else if ( (LA1_0==NAME) ) {s = 4;}

                        else if ( (LA1_0==EOF||(LA1_0>=STREETNUMSUFFIX && LA1_0<=NUMERICSTREETSUFFIX)) ) {s = 5;}

                         
                        input.seek(index1_0);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA1_7 = input.LA(1);

                         
                        int index1_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA1_12 = input.LA(1);

                         
                        int index1_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_12);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA1_13 = input.LA(1);

                         
                        int index1_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_13);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA1_14 = input.LA(1);

                         
                        int index1_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_14);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA1_1 = input.LA(1);

                         
                        int index1_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_1==11) && ((couldBeUrban()))) {s = 2;}

                        else if ( (LA1_1==STREETNUMSUFFIX) ) {s = 6;}

                        else if ( (LA1_1==NAME) ) {s = 7;}

                        else if ( (LA1_1==NUMERICSTREETSUFFIX) ) {s = 8;}

                        else if ( (LA1_1==NUMBER) ) {s = 9;}

                        else if ( (LA1_1==NUMANDSUFFIX) ) {s = 10;}

                        else if ( (LA1_1==EOF) ) {s = 5;}

                         
                        input.seek(index1_1);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA1_6 = input.LA(1);

                         
                        int index1_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((couldBeUrban())&&(setStartsUrbanNotRural(true)))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_6);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 1, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA9_eotS =
        "\12\uffff";
    static final String DFA9_eofS =
        "\12\uffff";
    static final String DFA9_minS =
        "\1\4\1\7\2\0\6\uffff";
    static final String DFA9_maxS =
        "\1\10\1\7\2\0\6\uffff";
    static final String DFA9_acceptS =
        "\4\uffff\1\6\1\1\1\2\1\3\1\5\1\4";
    static final String DFA9_specialS =
        "\1\uffff\1\1\1\2\1\0\6\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\2\2\4\1\3\1\1",
            "\1\5",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
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
            return "202:1: streetToken : ({...}? =>s= NAME sn= NUMBER | {...}?d= ( NAME | STREETNUMSUFFIX ) | {...}?t= NAME | {...}?n= NUMBER | {...}? ruralRoute | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA9_3 = input.LA(1);

                         
                        int index9_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((hasStreetNameStarted)) ) {s = 9;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index9_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA9_1 = input.LA(1);

                         
                        int index9_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA9_1==NUMBER) && ((Address.isSuiteType(input.LT(1).getText())))) {s = 5;}

                        else if ( ((hasStreetNameStarted && address.isStreetDirection(input.LT(1).getText()))) ) {s = 6;}

                        else if ( ((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {s = 7;}

                        else if ( ((((hasStreetNameStarted && startsUrbanNotRural)&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText())))||((hasStreetNameStarted && startsUrbanNotRural)&&(Address.isRuralRoute(input.LT(1).getText())))||((hasStreetNameStarted && startsUrbanNotRural)&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))))) ) {s = 8;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index9_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA9_2 = input.LA(1);

                         
                        int index9_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((hasStreetNameStarted && address.isStreetDirection(input.LT(1).getText()))) ) {s = 6;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index9_2);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 9, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_streetAddressStart_in_address35 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRouteAddress_in_address44 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lockBoxAddress_in_address52 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalDeliveryAddress_in_address60 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedParse_in_address66 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedToken_in_failedParse82 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_set_in_failedToken96 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_streetAddress_in_streetAddressStart135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress163 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_streetAddress165 = new BitSet(new long[]{0x00000000000002C0L});
    public static final BitSet FOLLOW_street_in_streetAddress167 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUITEANDSTREETNUM_in_street198 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_set_in_street202 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street208 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NUMBER_in_street225 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_set_in_street229 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street235 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NUMANDSUFFIX_in_street253 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street255 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NUMBER_in_street266 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street268 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_streetToken298 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NUMBER_in_streetToken302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetToken351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_streetToken369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRoute_in_streetToken385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRoute_in_ruralRouteAddress450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute480 = new BitSet(new long[]{0x00000000000003C0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute484 = new BitSet(new long[]{0x00000000000003C0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute506 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute510 = new BitSet(new long[]{0x00000000000003C0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute514 = new BitSet(new long[]{0x00000000000003C0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute517 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute536 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute540 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute544 = new BitSet(new long[]{0x00000000000003C0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute548 = new BitSet(new long[]{0x00000000000003C0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute551 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_diTypeAndName_in_ruralRouteSuffix571 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_streetAddress_in_ruralRouteSuffix579 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress609 = new BitSet(new long[]{0x0000000000001080L});
    public static final BitSet FOLLOW_12_in_lockBoxAddress611 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress616 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_diTypeAndName_in_lockBoxAddress618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress638 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress642 = new BitSet(new long[]{0x0000000000001080L});
    public static final BitSet FOLLOW_12_in_lockBoxAddress644 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress649 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_diTypeAndName_in_lockBoxAddress651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress678 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress700 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress704 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress706 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress730 = new BitSet(new long[]{0x0000000000000110L});
    public static final BitSet FOLLOW_set_in_generalDeliveryAddress734 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress742 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName771 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName773 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName793 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName797 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName799 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName819 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName823 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName827 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName829 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_set_in_diName852 = new BitSet(new long[]{0x0000000000000002L});

}