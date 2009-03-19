// $ANTLR 3.1.2 C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g 2009-03-18 20:46:46

package BritefuryJ.DocModel.Reader;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DMReaderLexer extends Lexer {
    public static final int QUOTEDSTRING=7;
    public static final int QUOTEDSTRING_PUNC=9;
    public static final int STRING_ESCAPE=10;
    public static final int UNQUOTEDSTRING_PUNC=8;
    public static final int EOF=-1;
    public static final int T__19=19;
    public static final int T__16=16;
    public static final int WS=4;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int UNQUOTEDSTRING=6;
    public static final int DOTTED_IDENTIFIER=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int IDENTIFIER=5;

    // delegates
    // delegators

    public DMReaderLexer() {;} 
    public DMReaderLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public DMReaderLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g"; }

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:7:7: ( '[' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:7:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:8:7: ( ']' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:8:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__13"

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:9:7: ( '(' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:9:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:10:7: ( '=' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:10:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:11:7: ( ')' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:11:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:12:7: ( '{' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:12:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:13:7: ( ':' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:13:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:14:7: ( '}' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:14:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "UNQUOTEDSTRING_PUNC"
    public final void mUNQUOTEDSTRING_PUNC() throws RecognitionException {
        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:175:2: ( ( '+' | '-' | '*' | '/' | '%' | '^' | '&' | '|' | '!' | '~' | '$' | '@' | '.' ) )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:175:4: ( '+' | '-' | '*' | '/' | '%' | '^' | '&' | '|' | '!' | '~' | '$' | '@' | '.' )
            {
            if ( input.LA(1)=='!'||(input.LA(1)>='$' && input.LA(1)<='&')||(input.LA(1)>='*' && input.LA(1)<='+')||(input.LA(1)>='-' && input.LA(1)<='/')||input.LA(1)=='@'||input.LA(1)=='^'||input.LA(1)=='|'||input.LA(1)=='~' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "UNQUOTEDSTRING_PUNC"

    // $ANTLR start "QUOTEDSTRING_PUNC"
    public final void mQUOTEDSTRING_PUNC() throws RecognitionException {
        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:178:2: ( ( '+' | '-' | '*' | '/' | '%' | '^' | '&' | '|' | '!' | '~' | '$' | '@' | '.' | '\\'' | '(' | ')' | '[' | ']' | '{' | '}' ) )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:178:4: ( '+' | '-' | '*' | '/' | '%' | '^' | '&' | '|' | '!' | '~' | '$' | '@' | '.' | '\\'' | '(' | ')' | '[' | ']' | '{' | '}' )
            {
            if ( input.LA(1)=='!'||(input.LA(1)>='$' && input.LA(1)<='+')||(input.LA(1)>='-' && input.LA(1)<='/')||input.LA(1)=='@'||input.LA(1)=='['||(input.LA(1)>=']' && input.LA(1)<='^')||(input.LA(1)>='{' && input.LA(1)<='~') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "QUOTEDSTRING_PUNC"

    // $ANTLR start "STRING_ESCAPE"
    public final void mSTRING_ESCAPE() throws RecognitionException {
        try {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:2: ( '\\\\' ( 'n' | 't' | 'r' | ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' ) ) )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:4: '\\\\' ( 'n' | 't' | 'r' | ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' ) )
            {
            match('\\'); 
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:9: ( 'n' | 't' | 'r' | ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' ) )
            int alt2=4;
            switch ( input.LA(1) ) {
            case 'n':
                {
                alt2=1;
                }
                break;
            case 't':
                {
                alt2=2;
                }
                break;
            case 'r':
                {
                alt2=3;
                }
                break;
            case 'x':
                {
                alt2=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:11: 'n'
                    {
                    match('n'); 

                    }
                    break;
                case 2 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:19: 't'
                    {
                    match('t'); 

                    }
                    break;
                case 3 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:27: 'r'
                    {
                    match('r'); 

                    }
                    break;
                case 4 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:35: ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' )
                    {
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:35: ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' )
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:36: 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x'
                    {
                    match('x'); 
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:40: ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+
                    int cnt1=0;
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='F')||(LA1_0>='a' && LA1_0<='f')) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:
                    	    {
                    	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


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

                    match('x'); 

                    }


                    }
                    break;

            }


            }

        }
        finally {
        }
    }
    // $ANTLR end "STRING_ESCAPE"

    // $ANTLR start "UNQUOTEDSTRING"
    public final void mUNQUOTEDSTRING() throws RecognitionException {
        try {
            int _type = UNQUOTEDSTRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:185:2: ( ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | UNQUOTEDSTRING_PUNC )+ )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:185:4: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | UNQUOTEDSTRING_PUNC )+
            {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:185:4: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | UNQUOTEDSTRING_PUNC )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='!'||(LA3_0>='$' && LA3_0<='&')||(LA3_0>='*' && LA3_0<='+')||(LA3_0>='-' && LA3_0<='9')||(LA3_0>='@' && LA3_0<='Z')||LA3_0=='^'||(LA3_0>='a' && LA3_0<='z')||LA3_0=='|'||LA3_0=='~') ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:
            	    {
            	    if ( input.LA(1)=='!'||(input.LA(1)>='$' && input.LA(1)<='&')||(input.LA(1)>='*' && input.LA(1)<='+')||(input.LA(1)>='-' && input.LA(1)<='9')||(input.LA(1)>='@' && input.LA(1)<='Z')||input.LA(1)=='^'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='|'||input.LA(1)=='~' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


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


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNQUOTEDSTRING"

    // $ANTLR start "QUOTEDSTRING"
    public final void mQUOTEDSTRING() throws RecognitionException {
        try {
            int _type = QUOTEDSTRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:188:2: ( ( '\\\"' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | QUOTEDSTRING_PUNC | STRING_ESCAPE )* '\\\"' ) )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:188:4: ( '\\\"' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | QUOTEDSTRING_PUNC | STRING_ESCAPE )* '\\\"' )
            {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:188:4: ( '\\\"' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | QUOTEDSTRING_PUNC | STRING_ESCAPE )* '\\\"' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:189:4: '\\\"' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | QUOTEDSTRING_PUNC | STRING_ESCAPE )* '\\\"'
            {
            match('\"'); 
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:190:4: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | QUOTEDSTRING_PUNC | STRING_ESCAPE )*
            loop4:
            do {
                int alt4=6;
                switch ( input.LA(1) ) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                    {
                    alt4=1;
                    }
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt4=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt4=3;
                    }
                    break;
                case '!':
                case '$':
                case '%':
                case '&':
                case '\'':
                case '(':
                case ')':
                case '*':
                case '+':
                case '-':
                case '.':
                case '/':
                case '@':
                case '[':
                case ']':
                case '^':
                case '{':
                case '|':
                case '}':
                case '~':
                    {
                    alt4=4;
                    }
                    break;
                case '\\':
                    {
                    alt4=5;
                    }
                    break;

                }

                switch (alt4) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:190:6: 'A' .. 'Z'
            	    {
            	    matchRange('A','Z'); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:190:17: 'a' .. 'z'
            	    {
            	    matchRange('a','z'); 

            	    }
            	    break;
            	case 3 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:190:28: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;
            	case 4 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:190:39: QUOTEDSTRING_PUNC
            	    {
            	    mQUOTEDSTRING_PUNC(); 

            	    }
            	    break;
            	case 5 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:190:59: STRING_ESCAPE
            	    {
            	    mSTRING_ESCAPE(); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match('\"'); 

            }

             setText( getText().substring( 1, getText().length() - 1 ) ); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOTEDSTRING"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:195:2: ( ( '\\n' | '\\t' | '\\r' | ' ' )+ )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:195:4: ( '\\n' | '\\t' | '\\r' | ' ' )+
            {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:195:4: ( '\\n' | '\\t' | '\\r' | ' ' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='\t' && LA5_0<='\n')||LA5_0=='\r'||LA5_0==' ') ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


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

            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:199:2: ( ( 'A' .. 'Z' | 'a' .. 'z' | '_' ) ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' )* )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:199:4: ( 'A' .. 'Z' | 'a' .. 'z' | '_' ) ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:199:34: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')||(LA6_0>='A' && LA6_0<='Z')||LA6_0=='_'||(LA6_0>='a' && LA6_0<='z')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "DOTTED_IDENTIFIER"
    public final void mDOTTED_IDENTIFIER() throws RecognitionException {
        try {
            int _type = DOTTED_IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:204:2: ( IDENTIFIER ( '.' IDENTIFIER )* )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:204:4: IDENTIFIER ( '.' IDENTIFIER )*
            {
            mIDENTIFIER(); 
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:204:15: ( '.' IDENTIFIER )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='.') ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:204:17: '.' IDENTIFIER
            	    {
            	    match('.'); 
            	    mIDENTIFIER(); 

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOTTED_IDENTIFIER"

    public void mTokens() throws RecognitionException {
        // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:8: ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | UNQUOTEDSTRING | QUOTEDSTRING | WS | IDENTIFIER | DOTTED_IDENTIFIER )
        int alt8=13;
        alt8 = dfa8.predict(input);
        switch (alt8) {
            case 1 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:10: T__12
                {
                mT__12(); 

                }
                break;
            case 2 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:16: T__13
                {
                mT__13(); 

                }
                break;
            case 3 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:22: T__14
                {
                mT__14(); 

                }
                break;
            case 4 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:28: T__15
                {
                mT__15(); 

                }
                break;
            case 5 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:34: T__16
                {
                mT__16(); 

                }
                break;
            case 6 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:40: T__17
                {
                mT__17(); 

                }
                break;
            case 7 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:46: T__18
                {
                mT__18(); 

                }
                break;
            case 8 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:52: T__19
                {
                mT__19(); 

                }
                break;
            case 9 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:58: UNQUOTEDSTRING
                {
                mUNQUOTEDSTRING(); 

                }
                break;
            case 10 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:73: QUOTEDSTRING
                {
                mQUOTEDSTRING(); 

                }
                break;
            case 11 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:86: WS
                {
                mWS(); 

                }
                break;
            case 12 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:89: IDENTIFIER
                {
                mIDENTIFIER(); 

                }
                break;
            case 13 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:100: DOTTED_IDENTIFIER
                {
                mDOTTED_IDENTIFIER(); 

                }
                break;

        }

    }


    protected DFA8 dfa8 = new DFA8(this);
    static final String DFA8_eotS =
        "\11\uffff\1\14\3\uffff\1\21\2\14\1\21\2\uffff\2\14";
    static final String DFA8_eofS =
        "\25\uffff";
    static final String DFA8_minS =
        "\1\11\10\uffff\1\56\3\uffff\2\56\1\101\1\56\2\uffff\2\56";
    static final String DFA8_maxS =
        "\1\176\10\uffff\1\172\3\uffff\4\172\2\uffff\2\172";
    static final String DFA8_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\uffff\1\12\1\13\1\11"+
        "\4\uffff\1\14\1\15\2\uffff";
    static final String DFA8_specialS =
        "\25\uffff}>";
    static final String[] DFA8_transitionS = {
            "\2\13\2\uffff\1\13\22\uffff\1\13\1\14\1\12\1\uffff\3\14\1\uffff"+
            "\1\3\1\5\2\14\1\uffff\15\14\1\7\2\uffff\1\4\2\uffff\1\14\32"+
            "\11\1\1\1\uffff\1\2\1\14\1\15\1\uffff\32\11\1\6\1\14\1\10\1"+
            "\14",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\17\1\uffff\12\16\7\uffff\32\16\4\uffff\1\20\1\uffff\32"+
            "\16",
            "",
            "",
            "",
            "\1\22\1\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32"+
            "\20",
            "\1\17\1\uffff\12\16\7\uffff\32\16\4\uffff\1\20\1\uffff\32"+
            "\16",
            "\32\23\4\uffff\1\22\1\uffff\32\23",
            "\1\22\1\uffff\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32"+
            "\20",
            "",
            "",
            "\1\17\1\uffff\12\24\7\uffff\32\24\4\uffff\1\22\1\uffff\32"+
            "\24",
            "\1\17\1\uffff\12\24\7\uffff\32\24\4\uffff\1\22\1\uffff\32"+
            "\24"
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
            return "1:1: Tokens : ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | UNQUOTEDSTRING | QUOTEDSTRING | WS | IDENTIFIER | DOTTED_IDENTIFIER );";
        }
    }
 

}