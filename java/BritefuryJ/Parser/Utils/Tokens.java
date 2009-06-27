//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.ParserOld.ParseResult;
import BritefuryJ.ParserOld.ParserExpression;
import BritefuryJ.ParserOld.ParserState;
import BritefuryJ.ParserOld.RegEx;

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
		
	
	private static class JavaIdentifier extends ParserExpression
	{
		public JavaIdentifier()
		{
		}
		
		
		protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
		{
			start = state.skipJunkChars( input, start );
			
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


		public boolean compareTo(ParserExpression x)
		{
			return this == x  ||  x instanceof JavaIdentifier;
		}
		
		public String toString()
		{
			return "JavaIdentifier()";
		}
	}

	
	private static class JavaCharacterLiteral extends ParserExpression
	{
		public JavaCharacterLiteral()
		{
		}
		
		
		protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
		{
			start = state.skipJunkChars( input, start );
			
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


		public boolean compareTo(ParserExpression x)
		{
			return this == x  ||  x instanceof JavaCharacterLiteral;
		}
		
		public String toString()
		{
			return "JavaCharacterLiteral()";
		}
	}
	
	

	private static class JavaStringLiteral extends ParserExpression
	{
		public JavaStringLiteral()
		{
		}
		
		
		protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
		{
			start = state.skipJunkChars( input, start );
			
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


		public boolean compareTo(ParserExpression x)
		{
			return this == x  ||  x instanceof JavaStringLiteral;
		}
		
		public String toString()
		{
			return "JavaStringLiteral()";
		}
	}
	
	
	public static ParserExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );
	public static ParserExpression javaIdentifier = new JavaIdentifier();
	
	public static ParserExpression decimalInteger = new RegEx( "[\\-]?[0-9]+" );
	public static ParserExpression decimalIntegerNoOctal = new RegEx( "(?:[1-9][0-9]*)|0" );
	
	public static ParserExpression hexInteger = new RegEx( "0(x|X)[0-9A-Fa-f]+" );

	public static ParserExpression octalInteger = new RegEx( "0[0-7]+" );

	public static ParserExpression integer = decimalInteger.__or__( hexInteger );
	
	public static ParserExpression floatingPoint = new RegEx( "[\\-]?(([0-9]+\\.[0-9]*)|(\\.[0-9]+))(e[\\-]?[0-9]+)?" );

	public static ParserExpression javaCharacterLiteral = new JavaCharacterLiteral();

	public static ParserExpression singleQuotedString = new RegEx( "\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\'" );
	public static ParserExpression doubleQuotedString = new RegEx( "\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\"" );
	public static ParserExpression quotedString = new RegEx( "(?:\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\")|(?:\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\')" );
	public static ParserExpression unicodeString = new RegEx( "(u|U)((?:\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\")|(?:\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\'))" );
	
	public static ParserExpression javaStringLiteral = new JavaStringLiteral();
}
