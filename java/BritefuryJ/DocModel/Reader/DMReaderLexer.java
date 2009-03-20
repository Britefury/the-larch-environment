// $ANTLR 3.1.2 C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g 2009-03-19 22:59:43

package BritefuryJ.DocModel.Reader;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class DMReaderLexer extends Lexer {
    public static final int QUOTEDSTRING=7;
    public static final int WS=4;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int QUOTEDSTRING_PUNC=9;
    public static final int STRING_ESCAPE=10;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int UNQUOTEDSTRING=6;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int IDENTIFIER=5;
    public static final int UNQUOTEDSTRING_PUNC=8;
    public static final int EOF=-1;

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

    // $ANTLR start "T__11"
    public final void mT__11() throws RecognitionException {
        try {
            int _type = T__11;
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
    // $ANTLR end "T__11"

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
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
    // $ANTLR end "T__12"

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
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
    // $ANTLR end "T__13"

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
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
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
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
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
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
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:13:7: ( '}' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:13:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__17"

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
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:2: ( '\\\\' ( 'n' | 't' | 'r' | '\"' | ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' ) ) )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:4: '\\\\' ( 'n' | 't' | 'r' | '\"' | ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' ) )
            {
            match('\\'); 
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:9: ( 'n' | 't' | 'r' | '\"' | ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' ) )
            int alt2=5;
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
            case '\"':
                {
                alt2=4;
                }
                break;
            case 'x':
                {
                alt2=5;
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
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:35: '\"'
                    {
                    match('\"'); 

                    }
                    break;
                case 5 :
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:43: ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' )
                    {
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:43: ( 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x' )
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:44: 'x' ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+ 'x'
                    {
                    match('x'); 
                    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:182:48: ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )+
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

    // $ANTLR start "QUOTEDSTRING"
    public final void mQUOTEDSTRING() throws RecognitionException {
        try {
            int _type = QUOTEDSTRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:185:2: ( ( '\\\"' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' | QUOTEDSTRING_PUNC | STRING_ESCAPE )* '\\\"' ) )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:185:4: ( '\\\"' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' | QUOTEDSTRING_PUNC | STRING_ESCAPE )* '\\\"' )
            {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:185:4: ( '\\\"' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' | QUOTEDSTRING_PUNC | STRING_ESCAPE )* '\\\"' )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:186:4: '\\\"' ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' | QUOTEDSTRING_PUNC | STRING_ESCAPE )* '\\\"'
            {
            match('\"'); 
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:187:4: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' | QUOTEDSTRING_PUNC | STRING_ESCAPE )*
            loop3:
            do {
                int alt3=7;
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
                    alt3=1;
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
                    alt3=2;
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
                    alt3=3;
                    }
                    break;
                case '_':
                    {
                    alt3=4;
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
                    alt3=5;
                    }
                    break;
                case '\\':
                    {
                    alt3=6;
                    }
                    break;

                }

                switch (alt3) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:187:6: 'A' .. 'Z'
            	    {
            	    matchRange('A','Z'); 

            	    }
            	    break;
            	case 2 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:187:17: 'a' .. 'z'
            	    {
            	    matchRange('a','z'); 

            	    }
            	    break;
            	case 3 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:187:28: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;
            	case 4 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:187:39: '_'
            	    {
            	    match('_'); 

            	    }
            	    break;
            	case 5 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:187:45: QUOTEDSTRING_PUNC
            	    {
            	    mQUOTEDSTRING_PUNC(); 

            	    }
            	    break;
            	case 6 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:187:65: STRING_ESCAPE
            	    {
            	    mSTRING_ESCAPE(); 

            	    }
            	    break;

            	default :
            	    break loop3;
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
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:192:2: ( ( '\\n' | '\\t' | '\\r' | ' ' )+ )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:192:4: ( '\\n' | '\\t' | '\\r' | ' ' )+
            {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:192:4: ( '\\n' | '\\t' | '\\r' | ' ' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='\t' && LA4_0<='\n')||LA4_0=='\r'||LA4_0==' ') ) {
                    alt4=1;
                }


                switch (alt4) {
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
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
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
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:196:2: ( ( 'A' .. 'Z' | 'a' .. 'z' | '_' ) ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' )* )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:196:4: ( 'A' .. 'Z' | 'a' .. 'z' | '_' ) ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:196:34: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')||(LA5_0>='A' && LA5_0<='Z')||LA5_0=='_'||(LA5_0>='a' && LA5_0<='z')) ) {
                    alt5=1;
                }


                switch (alt5) {
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
            	    break loop5;
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

    // $ANTLR start "UNQUOTEDSTRING"
    public final void mUNQUOTEDSTRING() throws RecognitionException {
        try {
            int _type = UNQUOTEDSTRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:200:2: ( ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' | UNQUOTEDSTRING_PUNC )+ )
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:200:4: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' | UNQUOTEDSTRING_PUNC )+
            {
            // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:200:4: ( 'A' .. 'Z' | 'a' .. 'z' | '0' .. '9' | '_' | UNQUOTEDSTRING_PUNC )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='!'||(LA6_0>='$' && LA6_0<='&')||(LA6_0>='*' && LA6_0<='+')||(LA6_0>='-' && LA6_0<='9')||(LA6_0>='@' && LA6_0<='Z')||(LA6_0>='^' && LA6_0<='_')||(LA6_0>='a' && LA6_0<='z')||LA6_0=='|'||LA6_0=='~') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:
            	    {
            	    if ( input.LA(1)=='!'||(input.LA(1)>='$' && input.LA(1)<='&')||(input.LA(1)>='*' && input.LA(1)<='+')||(input.LA(1)>='-' && input.LA(1)<='9')||(input.LA(1)>='@' && input.LA(1)<='Z')||(input.LA(1)>='^' && input.LA(1)<='_')||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='|'||input.LA(1)=='~' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


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
    // $ANTLR end "UNQUOTEDSTRING"

    public void mTokens() throws RecognitionException {
        // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:8: ( T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | QUOTEDSTRING | WS | IDENTIFIER | UNQUOTEDSTRING )
        int alt7=11;
        alt7 = dfa7.predict(input);
        switch (alt7) {
            case 1 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:10: T__11
                {
                mT__11(); 

                }
                break;
            case 2 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:16: T__12
                {
                mT__12(); 

                }
                break;
            case 3 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:22: T__13
                {
                mT__13(); 

                }
                break;
            case 4 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:28: T__14
                {
                mT__14(); 

                }
                break;
            case 5 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:34: T__15
                {
                mT__15(); 

                }
                break;
            case 6 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:40: T__16
                {
                mT__16(); 

                }
                break;
            case 7 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:46: T__17
                {
                mT__17(); 

                }
                break;
            case 8 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:52: QUOTEDSTRING
                {
                mQUOTEDSTRING(); 

                }
                break;
            case 9 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:65: WS
                {
                mWS(); 

                }
                break;
            case 10 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:68: IDENTIFIER
                {
                mIDENTIFIER(); 

                }
                break;
            case 11 :
                // C:\\code\\gsym\\trunk\\java\\BritefuryJ\\DocModel\\Reader\\DMReader.g:1:79: UNQUOTEDSTRING
                {
                mUNQUOTEDSTRING(); 

                }
                break;

        }

    }


    protected DFA7 dfa7 = new DFA7(this);
    static final String DFA7_eotS =
        "\12\uffff\1\14\2\uffff\1\14";
    static final String DFA7_eofS =
        "\16\uffff";
    static final String DFA7_minS =
        "\1\11\11\uffff\1\41\2\uffff\1\41";
    static final String DFA7_maxS =
        "\1\176\11\uffff\1\176\2\uffff\1\176";
    static final String DFA7_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\uffff\1\13\1\12"+
        "\1\uffff";
    static final String DFA7_specialS =
        "\16\uffff}>";
    static final String[] DFA7_transitionS = {
            "\2\11\2\uffff\1\11\22\uffff\1\11\1\13\1\10\1\uffff\3\13\1\uffff"+
            "\1\3\1\5\2\13\1\uffff\15\13\3\uffff\1\4\2\uffff\1\13\32\12\1"+
            "\1\1\uffff\1\2\1\13\1\12\1\uffff\32\12\1\6\1\13\1\7\1\13",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\13\2\uffff\3\13\3\uffff\2\13\1\uffff\3\13\12\15\6\uffff"+
            "\1\13\32\15\3\uffff\1\13\1\15\1\uffff\32\15\1\uffff\1\13\1\uffff"+
            "\1\13",
            "",
            "",
            "\1\13\2\uffff\3\13\3\uffff\2\13\1\uffff\3\13\12\15\6\uffff"+
            "\1\13\32\15\3\uffff\1\13\1\15\1\uffff\32\15\1\uffff\1\13\1\uffff"+
            "\1\13"
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | QUOTEDSTRING | WS | IDENTIFIER | UNQUOTEDSTRING );";
        }
    }
 

}