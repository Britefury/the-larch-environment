//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils;

import java.util.regex.Pattern;

import BritefuryJ.Parser.ParseResult;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.RegEx;
import BritefuryJ.Parser.TerminalString;
import BritefuryJ.Util.RichString.RichStringAccessor;

public class Tokens
{
	private static int consumeJavaEscape(CharSequence input, int start)
	{
		int pos = start;
		char c = input.charAt( pos );
		if ( c == '\\' )
		{
			pos++;
			
			if ( pos >= input.length() )
			{
				return -1;
			}
			
			c = input.charAt( pos );
			if ( c == 'b'  ||  c == 't'  |  c == 'n'  ||  c == 'f'  ||  c == 'r'  ||  c == '"'  ||  c == '\''  || c == '\\' )
			{
					pos++;
					return pos;
			}
			else if ( c >= '0'  &&  c <= '3' )
			{
				// Octal escape
				// Consume a 0-3, plus two octal chars
				pos++;
				if ( pos < input.length() )
				{
					c = input.charAt( pos );
					if ( c >= '0'  &&  c <= '7' )
					{
						pos++;
						if ( pos < input.length() )
						{
							c = input.charAt( pos );
							if ( c >= '0'  &&  c <= '7' )
							{
								pos++;
							}
						}
					}
				}
				return pos;
			}
			else if ( c >= '4'  &&  c <= '7' )
			{
				// Octal escape
				// Consume 1 or two octal chars
				pos++;
				if ( pos < input.length() )
				{
					c = input.charAt( pos );
					if ( c >= '0'  &&  c <= '7' )
					{
						pos++;
					}
				}
				return pos;
			}
			else if ( c == 'u' )
			{
				// Unicode escape
				pos++;
				
				// Consume 4 hex digits
				for (int i = 0; i < 4; i++)
				{
					if ( pos >= input.length() )
					{
						return -1;
					}
					c = input.charAt( pos );
					if ( ( c >= '0' && c <= '9' )  ||  ( c >= 'a' && c <= 'f' )  ||  ( c >= 'A' && c <= 'F' ) )
					{
						pos++;
					}
					else
					{
						return -1;
					}
				}

				return pos;
			}
			else
			{
				return -1;
			}
		}
		return -1;
	}
		
	
	private static class JavaIdentifier extends TerminalString
	{
		public JavaIdentifier()
		{
		}
		
		
		protected ParseResult consumeString(String input, int start)
		{
			int offset = start; 
			
			if ( offset < input.length()  &&  Character.isJavaIdentifierStart( input.charAt( offset ) ) )
			{
				offset++;
				
				while ( offset < input.length()  &&  Character.isJavaIdentifierPart( input.charAt( offset ) ) )
				{
					offset++;
				}
				
				return new ParseResult( input.subSequence( start, offset ).toString(), start, offset );
			}
			
			return ParseResult.failure( start );
		}

		protected ParseResult consumeRichString(RichStringAccessor input, int start)
		{
			CharSequence itemText = input.getItemTextFrom( start );
			
			if ( itemText != null )
			{
				int offset = 0; 
				
				if ( offset < itemText.length()  &&  Character.isJavaIdentifierStart( itemText.charAt( offset ) ) )
				{
					offset++;
					
					while ( offset < itemText.length()  &&  Character.isJavaIdentifierPart( itemText.charAt( offset ) ) )
					{
						offset++;
					}
					
					return new ParseResult( itemText.subSequence( 0, offset ).toString(), start, start + offset );
				}
			}
			
			return ParseResult.failure( start );
		}


		public boolean isEquivalentTo(ParserExpression x)
		{
			return this == x  ||  x instanceof JavaIdentifier;
		}
		
		public String toString()
		{
			return "JavaIdentifier()";
		}
	}

	
	private static class JavaCharacterLiteral extends TerminalString
	{
		public JavaCharacterLiteral()
		{
		}
		
		
		protected ParseResult consumeString(String input, int start)
		{
			int offset = start; 
			
			if ( offset < input.length()  &&  input.charAt( offset ) == '\'' )
			{
				offset++;
				
				char c = input.charAt( offset );
				if ( c == '\\' )
				{
					offset = consumeJavaEscape( input, offset );
				}
				else
				{
					offset++;
				}
				if ( offset != -1  &&  offset < input.length() )
				{
					c = input.charAt( offset );
					if ( c == '\'' )
					{
						offset++;
						return new ParseResult( input.subSequence( start, offset ).toString(), start, offset );
					}
				}
			}
			
			return ParseResult.failure( start );
		}


		protected ParseResult consumeRichString(RichStringAccessor input, int start)
		{
			CharSequence itemText = input.getItemTextFrom( start );
			
			if ( itemText != null )
			{
				int offset = 0; 
				
				if ( offset < itemText.length()  &&  itemText.charAt( offset ) == '\'' )
				{
					offset++;
					
					char c = itemText.charAt( offset );
					if ( c == '\\' )
					{
						offset = consumeJavaEscape( itemText, offset );
					}
					else
					{
						offset++;
					}
					if ( offset != -1  &&  offset < itemText.length() )
					{
						c = itemText.charAt( offset );
						if ( c == '\'' )
						{
							offset++;
							return new ParseResult( itemText.subSequence( 0, offset ).toString(), start, start + offset );
						}
					}
				}
			}
			
			return ParseResult.failure( start );
		}


		public boolean isEquivalentTo(ParserExpression x)
		{
			return this == x  ||  x instanceof JavaCharacterLiteral;
		}
		
		public String toString()
		{
			return "JavaCharacterLiteral()";
		}
	}
	
	

	private static class JavaStringLiteral extends TerminalString
	{
		public JavaStringLiteral()
		{
		}
		
		
		protected ParseResult consumeString(String input, int start)
		{
			int offset = start; 
			
			if ( offset < input.length()  &&  input.charAt( offset ) == '"' )
			{
				offset++;
				
				char c = input.charAt( offset );
				while ( offset < input.length()  &&  c != '"' )
				{
					if ( c == '\\' )
					{
						int escape = consumeJavaEscape( input, offset );
						if ( escape == -1 )
						{
							return ParseResult.failure( start );
						}
						offset = escape;
					}
					else
					{
						offset++;
					}
					
					c = input.charAt( offset );
				}
				
				if ( offset < input.length()  &&  c == '"' )
				{
					offset++;
					return new ParseResult( input.subSequence( start, offset ).toString(), start, offset );
				}
			}

			return ParseResult.failure( start );
		}


		protected ParseResult consumeRichString(RichStringAccessor input, int start)
		{
			CharSequence itemText = input.getItemTextFrom( start );
			
			if ( itemText != null )
			{
				int offset = 0; 
				
				if ( offset < itemText.length()  &&  itemText.charAt( offset ) == '"' )
				{
					offset++;
					
					char c = itemText.charAt( offset );
					while ( offset < itemText.length()  &&  c != '"' )
					{
						if ( c == '\\' )
						{
							int escape = consumeJavaEscape( itemText, offset );
							if ( escape == -1 )
							{
								return ParseResult.failure( start );
							}
							offset = escape;
						}
						else
						{
							offset++;
						}
						
						c = itemText.charAt( offset );
					}
					
					if ( offset < itemText.length()  &&  c == '"' )
					{
						offset++;
						return new ParseResult( itemText.subSequence( 0, offset ).toString(), start, start + offset );
					}
				}
			}

			return ParseResult.failure( start );
		}


		public boolean isEquivalentTo(ParserExpression x)
		{
			return this == x  ||  x instanceof JavaStringLiteral;
		}
		
		public String toString()
		{
			return "JavaStringLiteral()";
		}
	}
	
	
	public static Pattern identifierPattern = Pattern.compile( "[A-Za-z_][A-Za-z0-9_]*" );
	public static Pattern decimalIntegerPattern = Pattern.compile( "[\\-]?[0-9]+" );
	public static Pattern decimalIntegerNoOctalPattern = Pattern.compile( "(?:[1-9][0-9]*)|0" );
	public static Pattern hexIntegerPattern = Pattern.compile( "0(x|X)[0-9A-Fa-f]+" );
	public static Pattern hexIntegerWithoutPrefixPattern = Pattern.compile( "[0-9A-Fa-f]+" );
	public static Pattern binIntegerPattern = Pattern.compile( "0(b|B)[01]+" );
	public static Pattern binIntegerWithoutPrefixPattern = Pattern.compile( "[01]+" );
	public static Pattern octalIntegerPattern = Pattern.compile( "0[0-7]+" );
	public static Pattern oct0xIntegerPattern = Pattern.compile( "0(o|O)[0-7]+" );
	public static Pattern floatingPointPattern = Pattern.compile( "[\\-]?(([0-9]+\\.[0-9]*)|(\\.[0-9]+))(e[\\-]?[0-9]+)?" );
	
	
	public static final ParserExpression identifier = new RegEx( identifierPattern );
	public static final ParserExpression javaIdentifier = new JavaIdentifier();
	
	public static final ParserExpression decimalInteger = new RegEx( decimalIntegerPattern );
	public static final ParserExpression decimalIntegerNoOctal = new RegEx( decimalIntegerNoOctalPattern );
	
	public static final ParserExpression hexInteger = new RegEx( hexIntegerPattern );
	public static final ParserExpression hexIntegerWithoutPrefix = new RegEx( hexIntegerWithoutPrefixPattern );

	public static final ParserExpression binInteger = new RegEx( binIntegerPattern );
	public static final ParserExpression binIntegerWithoutPrefix = new RegEx( binIntegerWithoutPrefixPattern );

	public static final ParserExpression octalInteger = new RegEx( octalIntegerPattern );

	public static final ParserExpression oct0xInteger = new RegEx( oct0xIntegerPattern );

	public static final ParserExpression integer = decimalInteger.__or__( hexInteger );
	
	public static final ParserExpression floatingPoint = new RegEx( floatingPointPattern );

	public static final ParserExpression javaCharacterLiteral = new JavaCharacterLiteral();

	public static final ParserExpression singleQuotedString = new RegEx( "\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\.))*\'" );
	public static final ParserExpression doubleQuotedString = new RegEx( "\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\.))*\"" );
	public static final ParserExpression quotedString = new RegEx( "(?:\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\.))*\")|(?:\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\.))*\')" );
	public static final ParserExpression unicodeString = new RegEx( "(u|U)((?:\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\.))*\")|(?:\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\.))*\'))" );
	
	public static final ParserExpression javaStringLiteral = new JavaStringLiteral();
}
