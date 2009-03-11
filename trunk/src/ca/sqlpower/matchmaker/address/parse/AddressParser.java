// $ANTLR 3.1.2 /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-03-11 15:35:17

package ca.sqlpower.matchmaker.address.parse;

import java.util.List;

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ROUTESERVICETYPE", "LOCKBOXTYPE", "GD", "DITYPE", "SUITE", "SUFFIXANDDIR", "STREETNUMSUFFIX", "STREETDIR", "NUMANDSUFFIX", "NUMBER", "NAME", "WS", "'-'"
    };
    public static final int STREETDIR=11;
    public static final int GD=6;
    public static final int NAME=14;
    public static final int WS=15;
    public static final int T__16=16;
    public static final int ROUTESERVICETYPE=4;
    public static final int SUFFIXANDDIR=9;
    public static final int NUMBER=13;
    public static final int NUMANDSUFFIX=12;
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:100:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );
    public final void address() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:101:2: ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse )
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:101:4: {...}? => streetAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:102:4: {...}? => ruralRouteAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:103:4: {...}? => lockBoxAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:104:4: {...}? => generalDeliveryAddress
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:105:4: failedParse
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:108:1: failedParse : ( failedToken )* ;
    public final void failedParse() throws RecognitionException {
        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:2: ( ( failedToken )* )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:4: ( failedToken )*
            {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:4: ( failedToken )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=ROUTESERVICETYPE && LA2_0<=NAME)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:109:4: failedToken
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:112:1: failedToken : n= ( ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME ) ;
    public final void failedToken() throws RecognitionException {
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:2: (n= ( ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME ) )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:113:4: n= ( ROUTESERVICETYPE | LOCKBOXTYPE | GD | DITYPE | SUITE | SUFFIXANDDIR | STREETNUMSUFFIX | STREETDIR | NUMANDSUFFIX | NUMBER | NAME )
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:119:1: streetAddress : (sn= NUMBER '-' street | street s= SUITE sn= NUMBER | street );
    public final void streetAddress() throws RecognitionException {
        Token sn=null;
        Token s=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:120:2: (sn= NUMBER '-' street | street s= SUITE sn= NUMBER | street )
            int alt3=3;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:120:4: sn= NUMBER '-' street
                    {
                    sn=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress163); 
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:125:4: street s= SUITE sn= NUMBER
                    {
                    pushFollow(FOLLOW_street_in_streetAddress176);
                    street();

                    state._fsp--;

                    s=(Token)match(input,SUITE,FOLLOW_SUITE_in_streetAddress180); 
                    sn=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress184); 
                     
                    							  address.setSuitePrefix(false);
                    							  address.setSuiteType((s!=null?s.getText():null));
                    							  address.setSuite((sn!=null?sn.getText():null));
                    							  address.setType(Address.Type.URBAN);
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:131:4: street
                    {
                    pushFollow(FOLLOW_street_in_streetAddress192);
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
            int alt7=3;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==NUMBER) ) {
                switch ( input.LA(2) ) {
                case SUFFIXANDDIR:
                    {
                    switch ( input.LA(3) ) {
                    case SUFFIXANDDIR:
                    case STREETDIR:
                        {
                        alt7=1;
                        }
                        break;
                    case NAME:
                        {
                        alt7=1;
                        }
                        break;
                    case NUMANDSUFFIX:
                    case NUMBER:
                        {
                        alt7=1;
                        }
                        break;
                    case EOF:
                    case SUITE:
                        {
                        alt7=3;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 3, input);

                        throw nvae;
                    }

                    }
                    break;
                case STREETNUMSUFFIX:
                    {
                    alt7=1;
                    }
                    break;
                case STREETDIR:
                case NUMANDSUFFIX:
                case NUMBER:
                case NAME:
                    {
                    alt7=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }

            }
            else if ( (LA7_0==NUMANDSUFFIX) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:4: n= NUMBER s= ( STREETNUMSUFFIX | SUFFIXANDDIR ) ( streetToken )+
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_street212); 
                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=SUFFIXANDDIR && input.LA(1)<=STREETNUMSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:46: ( streetToken )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==SUFFIXANDDIR||(LA4_0>=STREETDIR && LA4_0<=NAME)) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:135:46: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street222);
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

                    address.setStreetNumber(quietIntParse((n!=null?n.getText():null)));
                    							 address.setStreetNumberSuffix((s!=null?s.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:139:4: n= NUMANDSUFFIX ( streetToken )+
                    {
                    n=(Token)match(input,NUMANDSUFFIX,FOLLOW_NUMANDSUFFIX_in_street240); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:139:19: ( streetToken )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==SUFFIXANDDIR||(LA5_0>=STREETDIR && LA5_0<=NAME)) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:139:19: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street242);
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

                    String streetNum = (n!=null?n.getText():null);
                    							 address.setStreetNumber(quietIntParse(streetNum.substring(0, streetNum.length() - 1)));
                    							 address.setStreetNumberSuffix(streetNum.substring(streetNum.length() - 1, streetNum.length()));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:143:4: n= NUMBER ( streetToken )+
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_street253); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:143:13: ( streetToken )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==SUFFIXANDDIR||(LA6_0>=STREETDIR && LA6_0<=NAME)) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:143:13: streetToken
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:146:1: streetToken : ({...}?d= ( STREETDIR | SUFFIXANDDIR ) | {...}?t= NAME | n= ( NAME | NUMBER | NUMANDSUFFIX ) );
    public final void streetToken() throws RecognitionException {
        Token d=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:147:2: ({...}?d= ( STREETDIR | SUFFIXANDDIR ) | {...}?t= NAME | n= ( NAME | NUMBER | NUMANDSUFFIX ) )
            int alt8=3;
            switch ( input.LA(1) ) {
            case SUFFIXANDDIR:
            case STREETDIR:
                {
                alt8=1;
                }
                break;
            case NAME:
                {
                int LA8_2 = input.LA(2);

                if ( ((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                    alt8=2;
                }
                else if ( (true) ) {
                    alt8=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 2, input);

                    throw nvae;
                }
                }
                break;
            case NUMANDSUFFIX:
            case NUMBER:
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:147:4: {...}?d= ( STREETDIR | SUFFIXANDDIR )
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:152:4: {...}?t= NAME
                    {
                    if ( !((!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "!address.isStreetTypePrefix() && addressDatabase.containsStreetType(input.LT(1).getText())");
                    }
                    t=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken307); 

                    							 if (address.getStreetType() != null) {
                    							    appendStreetName(address.getStreetType());
                    							 }
                    							 address.setStreetTypePrefix(!hasStreetNameStarted);
                    							 address.setStreetType((t!=null?t.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:161:4: n= ( NAME | NUMBER | NUMANDSUFFIX )
                    {
                    n=(Token)input.LT(1);
                    if ( (input.LA(1)>=NUMANDSUFFIX && input.LA(1)<=NAME) ) {
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:172:1: ruralRouteAddress : (rs= ROUTESERVICETYPE (n= NUMBER )? (di= DITYPE )? (stn= NAME )? | rs= ROUTESERVICETYPE n= NUMBER street );
    public final void ruralRouteAddress() throws RecognitionException {
        Token rs=null;
        Token n=null;
        Token di=null;
        Token stn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:2: (rs= ROUTESERVICETYPE (n= NUMBER )? (di= DITYPE )? (stn= NAME )? | rs= ROUTESERVICETYPE n= NUMBER street )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==ROUTESERVICETYPE) ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1==NUMBER) ) {
                    int LA12_2 = input.LA(3);

                    if ( (LA12_2==EOF||LA12_2==DITYPE||LA12_2==NAME) ) {
                        alt12=1;
                    }
                    else if ( ((LA12_2>=NUMANDSUFFIX && LA12_2<=NUMBER)) ) {
                        alt12=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA12_1==EOF||LA12_1==DITYPE||LA12_1==NAME) ) {
                    alt12=1;
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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:4: rs= ROUTESERVICETYPE (n= NUMBER )? (di= DITYPE )? (stn= NAME )?
                    {
                    rs=(Token)match(input,ROUTESERVICETYPE,FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress354); 
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:25: (n= NUMBER )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==NUMBER) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:25: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRouteAddress358); 

                            }
                            break;

                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:36: (di= DITYPE )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==DITYPE) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:36: di= DITYPE
                            {
                            di=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_ruralRouteAddress363); 

                            }
                            break;

                    }

                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:48: (stn= NAME )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==NAME) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:48: stn= NAME
                            {
                            stn=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRouteAddress368); 

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
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:181:4: rs= ROUTESERVICETYPE n= NUMBER street
                    {
                    rs=(Token)match(input,ROUTESERVICETYPE,FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress385); 
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRouteAddress389); 
                    pushFollow(FOLLOW_street_in_ruralRouteAddress391);
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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:188:1: lockBoxAddress : lb= LOCKBOXTYPE n= NUMBER (di= DITYPE )? (stn= NAME )? ;
    public final void lockBoxAddress() throws RecognitionException {
        Token lb=null;
        Token n=null;
        Token di=null;
        Token stn=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:2: (lb= LOCKBOXTYPE n= NUMBER (di= DITYPE )? (stn= NAME )? )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:4: lb= LOCKBOXTYPE n= NUMBER (di= DITYPE )? (stn= NAME )?
            {
            lb=(Token)match(input,LOCKBOXTYPE,FOLLOW_LOCKBOXTYPE_in_lockBoxAddress407); 
            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress411); 
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:30: (di= DITYPE )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==DITYPE) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:30: di= DITYPE
                    {
                    di=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_lockBoxAddress415); 

                    }
                    break;

            }

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:42: (stn= NAME )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==NAME) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:189:42: stn= NAME
                    {
                    stn=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress420); 

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
    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:199:1: generalDeliveryAddress : gd= GD (t= DITYPE )? (n= NAME )? ;
    public final void generalDeliveryAddress() throws RecognitionException {
        Token gd=null;
        Token t=null;
        Token n=null;

        try {
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:2: (gd= GD (t= DITYPE )? (n= NAME )? )
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:4: gd= GD (t= DITYPE )? (n= NAME )?
            {
            gd=(Token)match(input,GD,FOLLOW_GD_in_generalDeliveryAddress444); 
            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:11: (t= DITYPE )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==DITYPE) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:11: t= DITYPE
                    {
                    t=(Token)match(input,DITYPE,FOLLOW_DITYPE_in_generalDeliveryAddress448); 

                    }
                    break;

            }

            // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:21: (n= NAME )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==NAME) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /Users/thomas/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:200:21: n= NAME
                    {
                    n=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress453); 

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
    static final String DFA1_eotS =
        "\27\uffff";
    static final String DFA1_eofS =
        "\3\6\1\uffff\1\6\4\uffff\1\6\15\uffff";
    static final String DFA1_minS =
        "\3\4\1\0\1\4\1\0\2\uffff\1\0\1\4\6\0\1\uffff\1\0\1\uffff\3\0\1\uffff";
    static final String DFA1_maxS =
        "\1\16\1\20\1\16\1\0\1\16\1\0\2\uffff\1\0\1\16\6\0\1\uffff\1\0\1"+
        "\uffff\3\0\1\uffff";
    static final String DFA1_acceptS =
        "\6\uffff\1\5\1\1\10\uffff\1\2\1\uffff\1\4\3\uffff\1\3";
    static final String DFA1_specialS =
        "\1\uffff\1\12\1\uffff\1\2\1\uffff\1\15\2\uffff\1\10\1\uffff\1\5"+
        "\1\3\1\1\1\4\1\7\1\6\1\uffff\1\0\1\uffff\1\14\1\13\1\11\1\uffff}>";
    static final String[] DFA1_transitionS = {
            "\1\3\1\4\1\5\5\6\1\2\1\1\1\6",
            "\5\6\1\10\1\11\1\12\2\14\1\13\1\uffff\1\7",
            "\5\6\1\15\1\6\1\15\2\17\1\16",
            "\1\uffff",
            "\11\6\1\21\1\6",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "\5\6\1\23\1\6\1\23\2\25\1\24",
            "\1\uffff",
            "\1\uffff",
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
            return "100:1: address : ({...}? => streetAddress | {...}? => ruralRouteAddress | {...}? => lockBoxAddress | {...}? => generalDeliveryAddress | failedParse );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA1_17 = input.LA(1);

                         
                        int index1_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeLockBox())) ) {s = 22;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_17);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA1_12 = input.LA(1);

                         
                        int index1_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_12);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA1_3 = input.LA(1);

                         
                        int index1_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeRural())) ) {s = 16;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA1_11 = input.LA(1);

                         
                        int index1_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA1_13 = input.LA(1);

                         
                        int index1_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_13);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA1_10 = input.LA(1);

                         
                        int index1_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_10);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA1_15 = input.LA(1);

                         
                        int index1_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_15);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA1_14 = input.LA(1);

                         
                        int index1_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_14);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA1_8 = input.LA(1);

                         
                        int index1_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_8);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA1_21 = input.LA(1);

                         
                        int index1_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_21);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA1_1 = input.LA(1);

                         
                        int index1_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_1==16) && ((couldBeUrban()))) {s = 7;}

                        else if ( (LA1_1==SUFFIXANDDIR) ) {s = 8;}

                        else if ( (LA1_1==STREETNUMSUFFIX) ) {s = 9;}

                        else if ( (LA1_1==STREETDIR) ) {s = 10;}

                        else if ( (LA1_1==NAME) ) {s = 11;}

                        else if ( ((LA1_1>=NUMANDSUFFIX && LA1_1<=NUMBER)) ) {s = 12;}

                        else if ( (LA1_1==EOF||(LA1_1>=ROUTESERVICETYPE && LA1_1<=SUITE)) ) {s = 6;}

                         
                        input.seek(index1_1);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA1_20 = input.LA(1);

                         
                        int index1_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_20);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA1_19 = input.LA(1);

                         
                        int index1_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeUrban())) ) {s = 7;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_19);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA1_5 = input.LA(1);

                         
                        int index1_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((couldBeGD())) ) {s = 18;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index1_5);
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
        "\24\uffff";
    static final String DFA3_eofS =
        "\4\uffff\1\17\1\uffff\11\17\2\uffff\3\17";
    static final String DFA3_minS =
        "\1\14\2\11\1\uffff\1\10\1\11\11\10\2\uffff\3\10";
    static final String DFA3_maxS =
        "\1\15\1\20\1\16\1\uffff\13\16\2\uffff\3\16";
    static final String DFA3_acceptS =
        "\3\uffff\1\1\13\uffff\1\3\1\2\3\uffff";
    static final String DFA3_specialS =
        "\24\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\2\1\1",
            "\1\4\1\5\1\6\2\10\1\7\1\uffff\1\3",
            "\1\11\1\uffff\1\11\2\13\1\12",
            "",
            "\1\20\1\14\1\uffff\1\14\2\16\1\15",
            "\1\21\1\uffff\1\21\2\23\1\22",
            "\1\20\1\6\1\uffff\1\6\2\10\1\7",
            "\1\20\1\6\1\uffff\1\6\2\10\1\7",
            "\1\20\1\6\1\uffff\1\6\2\10\1\7",
            "\1\20\1\11\1\uffff\1\11\2\13\1\12",
            "\1\20\1\11\1\uffff\1\11\2\13\1\12",
            "\1\20\1\11\1\uffff\1\11\2\13\1\12",
            "\1\20\1\14\1\uffff\1\14\2\16\1\15",
            "\1\20\1\14\1\uffff\1\14\2\16\1\15",
            "\1\20\1\14\1\uffff\1\14\2\16\1\15",
            "",
            "",
            "\1\20\1\21\1\uffff\1\21\2\23\1\22",
            "\1\20\1\21\1\uffff\1\21\2\23\1\22",
            "\1\20\1\21\1\uffff\1\21\2\23\1\22"
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
            return "119:1: streetAddress : (sn= NUMBER '-' street | street s= SUITE sn= NUMBER | street );";
        }
    }
 

    public static final BitSet FOLLOW_streetAddress_in_address36 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRouteAddress_in_address45 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lockBoxAddress_in_address53 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalDeliveryAddress_in_address61 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedParse_in_address67 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedToken_in_failedParse83 = new BitSet(new long[]{0x0000000000007FF2L});
    public static final BitSet FOLLOW_set_in_failedToken97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress163 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_streetAddress165 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_street_in_streetAddress167 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress176 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_SUITE_in_streetAddress180 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_street212 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_set_in_street216 = new BitSet(new long[]{0x0000000000007A00L});
    public static final BitSet FOLLOW_streetToken_in_street222 = new BitSet(new long[]{0x0000000000007A02L});
    public static final BitSet FOLLOW_NUMANDSUFFIX_in_street240 = new BitSet(new long[]{0x0000000000007A00L});
    public static final BitSet FOLLOW_streetToken_in_street242 = new BitSet(new long[]{0x0000000000007A02L});
    public static final BitSet FOLLOW_NUMBER_in_street253 = new BitSet(new long[]{0x0000000000007A00L});
    public static final BitSet FOLLOW_streetToken_in_street255 = new BitSet(new long[]{0x0000000000007A02L});
    public static final BitSet FOLLOW_set_in_streetToken276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_streetToken307 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress354 = new BitSet(new long[]{0x0000000000006082L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRouteAddress358 = new BitSet(new long[]{0x0000000000004082L});
    public static final BitSet FOLLOW_DITYPE_in_ruralRouteAddress363 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_NAME_in_ruralRouteAddress368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUTESERVICETYPE_in_ruralRouteAddress385 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRouteAddress389 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_street_in_ruralRouteAddress391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOCKBOXTYPE_in_lockBoxAddress407 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress411 = new BitSet(new long[]{0x0000000000004082L});
    public static final BitSet FOLLOW_DITYPE_in_lockBoxAddress415 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GD_in_generalDeliveryAddress444 = new BitSet(new long[]{0x0000000000004082L});
    public static final BitSet FOLLOW_DITYPE_in_generalDeliveryAddress448 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress453 = new BitSet(new long[]{0x0000000000000002L});

}