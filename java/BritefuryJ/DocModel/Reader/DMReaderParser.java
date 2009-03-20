// $ANTLR 3.1.2 C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g 2009-03-19 22:59:42

package BritefuryJ.DocModel.Reader;

import java.util.HashMap;

import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMObjectClass;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DMReaderParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "IDENTIFIER", "UNQUOTEDSTRING", "QUOTEDSTRING", "UNQUOTEDSTRING_PUNC", "QUOTEDSTRING_PUNC", "STRING_ESCAPE", "'['", "']'", "'('", "'='", "')'", "'{'", "'}'"
    };
    public static final int QUOTEDSTRING=7;
    public static final int WS=4;
    public static final int T__16=16;
    public static final int QUOTEDSTRING_PUNC=9;
    public static final int T__15=15;
    public static final int STRING_ESCAPE=10;
    public static final int T__17=17;
    public static final int UNQUOTEDSTRING=6;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int IDENTIFIER=5;
    public static final int T__13=13;
    public static final int UNQUOTEDSTRING_PUNC=8;
    public static final int EOF=-1;

    // delegates
    // delegators


        public DMReaderParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DMReaderParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return DMReaderParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g"; }


    	public static class CouldNotGetModuleException extends RuntimeException
    	{
    		private static final long serialVersionUID = 1L;
    	}

    	public static class CouldNotGetClassException extends RuntimeException
    	{
    		private static final long serialVersionUID = 1L;
    	}

    	public static class InvalidFieldException extends RuntimeException
    	{
    		private static final long serialVersionUID = 1L;
    	}

    	protected String unescape(String s)
    	{
    		s = s.replace( "\\n", "\n" ).replace( "\\r", "\r" ).replace( "\\t", "\t" ).replace( "\\\\", "\\" ).replace( "\\\"", "\"" );
    		
    		boolean bScanAgain = true;
    		while ( bScanAgain )
    		{
    			bScanAgain = false;
    			int start = s.indexOf( "\\x" );
    			if ( start != -1 )
    			{
    				int end = s.substring( start+2 ).indexOf( "x" );
    				if ( end != -1 )
    				{
    					end += start+2;
    					String hexString = s.substring( start+2, end );
    					char c = (char)Integer.valueOf( hexString, 16 ).intValue();
    					s = s.substring( 0, start ) + new Character( c ).toString() + s.substring( end+1, s.length() );
    					bScanAgain = true;
    				}
    			}
    		}
    		
    		return s;
    	}
    	
    	protected Object createObject(String moduleName, String className, ArrayList<String> names, ArrayList<Object> values)
    	{
    		DMModule module = bindings.get( moduleName );
    		
    		if ( module == null )
    		{
    			throw new CouldNotGetModuleException();
    		}
    		
    		DMObjectClass cls;
    		try
    		{
    			cls = module.get( className );
    		}
    		catch (DMModule.UnknownClassException e)
    		{
    			throw new CouldNotGetClassException();
    		}
    		
    		try
    		{
    			return cls.newInstance( (String[])names.toArray(), values.toArray() );
    		}
    		catch (DMObjectClass.InvalidFieldNameException e)
    		{
    			throw new InvalidFieldException();
    		}
    	}
    	
    	protected void bind(String name, String moduleLocation)
    	{
    		DMModule module = moduleResolver.getModule( moduleLocation );
    		bindings.put( name, module );
    	}
    	
    	
    	
    	protected HashMap<String, DMModule> bindings = new HashMap<String, DMModule>();
    	public DMModuleResolver moduleResolver = null;



    // $ANTLR start "prog"
    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:110:1: prog returns [Object value] : x= document ;
    public final Object prog() throws RecognitionException {
        Object value = null;

        Object x = null;


        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:111:2: (x= document )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:111:4: x= document
            {
            pushFollow(FOLLOW_document_in_prog45);
            x=document();

            state._fsp--;

             value = x; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "prog"


    // $ANTLR start "document"
    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:113:1: document returns [Object value] : (x= item | x= bindings );
    public final Object document() throws RecognitionException {
        Object value = null;

        Object x = null;


        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:114:2: (x= item | x= bindings )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( ((LA1_0>=UNQUOTEDSTRING && LA1_0<=QUOTEDSTRING)||LA1_0==11||LA1_0==13) ) {
                alt1=1;
            }
            else if ( (LA1_0==16) ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:114:4: x= item
                    {
                    pushFollow(FOLLOW_item_in_document62);
                    x=item();

                    state._fsp--;

                     value = x; 

                    }
                    break;
                case 2 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:115:3: x= bindings
                    {
                    pushFollow(FOLLOW_bindings_in_document74);
                    x=bindings();

                    state._fsp--;

                     value = x; 

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
        return value;
    }
    // $ANTLR end "document"


    // $ANTLR start "item"
    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:117:1: item returns [Object value] : (x= atom | x= list | x= object );
    public final Object item() throws RecognitionException {
        Object value = null;

        Object x = null;


        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:118:2: (x= atom | x= list | x= object )
            int alt2=3;
            switch ( input.LA(1) ) {
            case UNQUOTEDSTRING:
            case QUOTEDSTRING:
                {
                alt2=1;
                }
                break;
            case 11:
                {
                alt2=2;
                }
                break;
            case 13:
                {
                alt2=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:118:4: x= atom
                    {
                    pushFollow(FOLLOW_atom_in_item92);
                    x=atom();

                    state._fsp--;

                     value = x; 

                    }
                    break;
                case 2 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:119:3: x= list
                    {
                    pushFollow(FOLLOW_list_in_item105);
                    x=list();

                    state._fsp--;

                     value = x; 

                    }
                    break;
                case 3 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:120:3: x= object
                    {
                    pushFollow(FOLLOW_object_in_item118);
                    x=object();

                    state._fsp--;

                     value = x; 

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
        return value;
    }
    // $ANTLR end "item"


    // $ANTLR start "list"
    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:122:1: list returns [Object value] : '[' ( WS )? (i= item ( WS )? )* ']' ;
    public final Object list() throws RecognitionException {
        Object value = null;

        Object i = null;


        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:123:2: ( '[' ( WS )? (i= item ( WS )? )* ']' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:123:4: '[' ( WS )? (i= item ( WS )? )* ']'
            {
             ArrayList<Object> xs = new ArrayList<Object>(); 
            match(input,11,FOLLOW_11_in_list137); 
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:125:4: ( WS )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==WS) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:125:4: WS
                    {
                    match(input,WS,FOLLOW_WS_in_list142); 

                    }
                    break;

            }

            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:126:4: (i= item ( WS )? )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>=UNQUOTEDSTRING && LA5_0<=QUOTEDSTRING)||LA5_0==11||LA5_0==13) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:126:6: i= item ( WS )?
            	    {
            	    pushFollow(FOLLOW_item_in_list152);
            	    i=item();

            	    state._fsp--;

            	     xs.add( i ); 
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:126:37: ( WS )?
            	    int alt4=2;
            	    int LA4_0 = input.LA(1);

            	    if ( (LA4_0==WS) ) {
            	        alt4=1;
            	    }
            	    switch (alt4) {
            	        case 1 :
            	            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:126:37: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_list156); 

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match(input,12,FOLLOW_12_in_list164); 
             value = xs; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "list"


    // $ANTLR start "object"
    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:130:1: object returns [Object value] : '(' ( WS )? moduleName= IDENTIFIER WS className= IDENTIFIER ( WS )? (k= IDENTIFIER ( WS )? '=' ( WS )? v= item ( WS )? )* ')' ;
    public final Object object() throws RecognitionException {
        Object value = null;

        Token moduleName=null;
        Token className=null;
        Token k=null;
        Object v = null;


        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:131:2: ( '(' ( WS )? moduleName= IDENTIFIER WS className= IDENTIFIER ( WS )? (k= IDENTIFIER ( WS )? '=' ( WS )? v= item ( WS )? )* ')' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:132:3: '(' ( WS )? moduleName= IDENTIFIER WS className= IDENTIFIER ( WS )? (k= IDENTIFIER ( WS )? '=' ( WS )? v= item ( WS )? )* ')'
            {
            match(input,13,FOLLOW_13_in_object186); 

            				ArrayList<String> keys = new ArrayList<String>();
            				ArrayList<Object> values = new ArrayList<Object>();
            			
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:137:4: ( WS )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==WS) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:137:4: WS
                    {
                    match(input,WS,FOLLOW_WS_in_object198); 

                    }
                    break;

            }

            moduleName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_object206); 
            match(input,WS,FOLLOW_WS_in_object211); 
            className=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_object218); 
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:141:4: ( WS )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==WS) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:141:4: WS
                    {
                    match(input,WS,FOLLOW_WS_in_object223); 

                    }
                    break;

            }

            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:142:4: (k= IDENTIFIER ( WS )? '=' ( WS )? v= item ( WS )? )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==IDENTIFIER) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:143:5: k= IDENTIFIER ( WS )? '=' ( WS )? v= item ( WS )?
            	    {
            	    k=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_object237); 
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:143:18: ( WS )?
            	    int alt8=2;
            	    int LA8_0 = input.LA(1);

            	    if ( (LA8_0==WS) ) {
            	        alt8=1;
            	    }
            	    switch (alt8) {
            	        case 1 :
            	            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:143:18: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_object239); 

            	            }
            	            break;

            	    }

            	    match(input,14,FOLLOW_14_in_object242); 
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:143:26: ( WS )?
            	    int alt9=2;
            	    int LA9_0 = input.LA(1);

            	    if ( (LA9_0==WS) ) {
            	        alt9=1;
            	    }
            	    switch (alt9) {
            	        case 1 :
            	            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:143:26: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_object244); 

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_item_in_object249);
            	    v=item();

            	    state._fsp--;

            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:143:37: ( WS )?
            	    int alt10=2;
            	    int LA10_0 = input.LA(1);

            	    if ( (LA10_0==WS) ) {
            	        alt10=1;
            	    }
            	    switch (alt10) {
            	        case 1 :
            	            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:143:37: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_object251); 

            	            }
            	            break;

            	    }


            	    					keys.add( k.getText() );
            	    					values.add( v );
            	    				

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);

             value = createObject( moduleName.getText(), className.getText(), keys, values ); 
            match(input,15,FOLLOW_15_in_object273); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "object"


    // $ANTLR start "bindings"
    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:152:1: bindings returns [Object value] : '{' ( WS )? (name= IDENTIFIER ( WS )? '=' ( WS )? moduleLocation= atom WS )* x= item ( WS )? '}' ;
    public final Object bindings() throws RecognitionException {
        Object value = null;

        Token name=null;
        String moduleLocation = null;

        Object x = null;


        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:153:2: ( '{' ( WS )? (name= IDENTIFIER ( WS )? '=' ( WS )? moduleLocation= atom WS )* x= item ( WS )? '}' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:154:3: '{' ( WS )? (name= IDENTIFIER ( WS )? '=' ( WS )? moduleLocation= atom WS )* x= item ( WS )? '}'
            {
            match(input,16,FOLLOW_16_in_bindings291); 
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:155:4: ( WS )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==WS) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:155:4: WS
                    {
                    match(input,WS,FOLLOW_WS_in_bindings296); 

                    }
                    break;

            }

            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:156:4: (name= IDENTIFIER ( WS )? '=' ( WS )? moduleLocation= atom WS )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==IDENTIFIER) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:157:5: name= IDENTIFIER ( WS )? '=' ( WS )? moduleLocation= atom WS
            	    {
            	    name=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_bindings310); 
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:157:21: ( WS )?
            	    int alt13=2;
            	    int LA13_0 = input.LA(1);

            	    if ( (LA13_0==WS) ) {
            	        alt13=1;
            	    }
            	    switch (alt13) {
            	        case 1 :
            	            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:157:21: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_bindings312); 

            	            }
            	            break;

            	    }

            	    match(input,14,FOLLOW_14_in_bindings315); 
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:157:29: ( WS )?
            	    int alt14=2;
            	    int LA14_0 = input.LA(1);

            	    if ( (LA14_0==WS) ) {
            	        alt14=1;
            	    }
            	    switch (alt14) {
            	        case 1 :
            	            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:157:29: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_bindings317); 

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_atom_in_bindings322);
            	    moduleLocation=atom();

            	    state._fsp--;

            	    match(input,WS,FOLLOW_WS_in_bindings324); 

            	    					bind( name.getText(), moduleLocation );
            	    				

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            pushFollow(FOLLOW_item_in_bindings343);
            x=item();

            state._fsp--;

            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:163:4: ( WS )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==WS) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:163:4: WS
                    {
                    match(input,WS,FOLLOW_WS_in_bindings348); 

                    }
                    break;

            }

             value = x; 
            match(input,17,FOLLOW_17_in_bindings358); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "bindings"


    // $ANTLR start "atom"
    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:167:1: atom returns [String value] : (s= UNQUOTEDSTRING | s= QUOTEDSTRING );
    public final String atom() throws RecognitionException {
        String value = null;

        Token s=null;

        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:168:2: (s= UNQUOTEDSTRING | s= QUOTEDSTRING )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==UNQUOTEDSTRING) ) {
                alt17=1;
            }
            else if ( (LA17_0==QUOTEDSTRING) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:168:4: s= UNQUOTEDSTRING
                    {
                    s=(Token)match(input,UNQUOTEDSTRING,FOLLOW_UNQUOTEDSTRING_in_atom376); 
                     value = s.getText(); 

                    }
                    break;
                case 2 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:169:3: s= QUOTEDSTRING
                    {
                    s=(Token)match(input,QUOTEDSTRING,FOLLOW_QUOTEDSTRING_in_atom387); 
                     value = unescape(s.getText()); 

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
        return value;
    }
    // $ANTLR end "atom"

    // Delegated rules


 

    public static final BitSet FOLLOW_document_in_prog45 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_item_in_document62 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bindings_in_document74 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_item92 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_list_in_item105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_object_in_item118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_11_in_list137 = new BitSet(new long[]{0x00000000000038D0L});
    public static final BitSet FOLLOW_WS_in_list142 = new BitSet(new long[]{0x00000000000038C0L});
    public static final BitSet FOLLOW_item_in_list152 = new BitSet(new long[]{0x00000000000038D0L});
    public static final BitSet FOLLOW_WS_in_list156 = new BitSet(new long[]{0x00000000000038C0L});
    public static final BitSet FOLLOW_12_in_list164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_object186 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_WS_in_object198 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENTIFIER_in_object206 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_WS_in_object211 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENTIFIER_in_object218 = new BitSet(new long[]{0x0000000000008030L});
    public static final BitSet FOLLOW_WS_in_object223 = new BitSet(new long[]{0x0000000000008020L});
    public static final BitSet FOLLOW_IDENTIFIER_in_object237 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_WS_in_object239 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_object242 = new BitSet(new long[]{0x00000000000028D0L});
    public static final BitSet FOLLOW_WS_in_object244 = new BitSet(new long[]{0x00000000000028C0L});
    public static final BitSet FOLLOW_item_in_object249 = new BitSet(new long[]{0x0000000000008030L});
    public static final BitSet FOLLOW_WS_in_object251 = new BitSet(new long[]{0x0000000000008020L});
    public static final BitSet FOLLOW_15_in_object273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_16_in_bindings291 = new BitSet(new long[]{0x00000000000028F0L});
    public static final BitSet FOLLOW_WS_in_bindings296 = new BitSet(new long[]{0x00000000000028E0L});
    public static final BitSet FOLLOW_IDENTIFIER_in_bindings310 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_WS_in_bindings312 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_bindings315 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_WS_in_bindings317 = new BitSet(new long[]{0x00000000000000C0L});
    public static final BitSet FOLLOW_atom_in_bindings322 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_WS_in_bindings324 = new BitSet(new long[]{0x00000000000028E0L});
    public static final BitSet FOLLOW_item_in_bindings343 = new BitSet(new long[]{0x0000000000020010L});
    public static final BitSet FOLLOW_WS_in_bindings348 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_17_in_bindings358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNQUOTEDSTRING_in_atom376 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTEDSTRING_in_atom387 = new BitSet(new long[]{0x0000000000000002L});

}