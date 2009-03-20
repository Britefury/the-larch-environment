//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DMIORead
{
	public static class ParseSXErrorException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	/*
	gSym document model IO
	
	
	Uses basic S-expressions
	
	lists are (...) as normal
	tokens inside are:
		atom:   A-Z a-z 0-9 _ + - * / % ^ & | ! $ @ . , < > = [ ] ~
		another list
	 */

	
	public static class MatchResult
	{
		public String value;
		int pos;
		
		public MatchResult(String value, int pos)
		{
			this.value = value;
			this.pos = pos;
		}
		
		
		public boolean isSuccess()
		{
			return value != null;
		}
		
		
		public static MatchResult failure(int pos)
		{
			return new MatchResult( null, pos );			
		}
	}
	

	public static String unquotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]~";
	public static String quotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]~'()` ";
	public static String inStringUnescapedChars = "[0-9A-Za-z_" + Pattern.quote( quotedStringPunctuationChars ) + "]";
	
	public static String hexCharEscape = Pattern.quote( "\\x" ) + "[0-9A-Fa-f]+" + Pattern.quote( "x" );
	public static String whitespaceEscape = Pattern.quote( "\\" ) + "[nrt" + Pattern.quote( "\\" ) + "]";
	
	public static String escapeSequence = "(?:" + hexCharEscape + ")|(?:" + whitespaceEscape + ")";
	

	public static Pattern whitespace = Pattern.compile( "[" + Pattern.quote( " \t\n\r" ) + "]+" );
	public static Pattern unquotedString = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( unquotedStringPunctuationChars ) + "]+" );
	public static Pattern quotedString = Pattern.compile( Pattern.quote( "\"" ) + "(?:" + inStringUnescapedChars + "|" + escapeSequence + ")*" + Pattern.quote( "\"" ) );
	public static Pattern hexChar = Pattern.compile( hexCharEscape );
	
	
	
	public static MatchResult match(Pattern pattern, String source, int pos)
	{
		Matcher m = pattern.matcher( source.substring( pos, source.length() ) );
		
		boolean bFound = m.find();
		if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
		{
			String matchString = m.group();
			int end = pos + matchString.length();
			return new MatchResult( matchString, end );
		}
		else
		{
			return MatchResult.failure( pos );
		}
	}
	
	public static String evalString(String s)
	{
		assert( s.charAt( 0 ) == '\"' );
		assert( s.charAt( s.length() - 1 ) == '\"');
		
		s = s.substring( 1, s.length() - 1 );
		
		s = s.replace( "\\n", "\n" ).replace( "\\r", "\r" ).replace( "\\t", "\t" ).replace( "\\\\", "\\" );
		
		boolean bScanAgain = true;
		while ( bScanAgain )
		{
			Matcher m = hexChar.matcher( s );
			
			bScanAgain = false;
			
			if ( m.find() )
			{
				if ( m.end() > m.start() )
				{
					String hexString = m.group();
					hexString = hexString.substring( 2, hexString.length() - 1 );
					char c = (char)Integer.valueOf( hexString, 16 ).intValue();
					s = s.substring( 0, m.start() ) + new Character( c ).toString() + s.substring( m.end(), s.length() );
					bScanAgain = true;
				}
			}
		}
		
		return s;
	}
	
	
	public static MatchResult matchAtom(String source, int pos)
	{
		MatchResult res = null;
		
		// Quoted string
		res = match( quotedString, source, pos );
		if ( res.value != null )
		{
			res.value = evalString( res.value );
			return res;
		}
		
		// Unquoted string
		res = match( unquotedString, source, pos );
		if ( res.value != null )
		{
			return res;
		}
		
		return MatchResult.failure( pos );
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static Object readSX(String source) throws ParseSXErrorException
	{
		int pos = 0;
		ArrayList<Object> stack = new ArrayList<Object>();
		Object last = null;
		while ( pos < source.length() )
		{
			if ( source.charAt( pos ) == '(' )
			{
				// Open paren; start new list
				Object xs = new Vector<Object>();
				// Append the new list to the list that is on the top of the stach; this builds the structure
				if ( stack.size() > 0 )
				{
					Vector<Object> top = (Vector<Object>)stack.get( stack.size() - 1 );
					top.add( xs );
				}
				// Make the top of the stack the new list
				stack.add( xs );
				pos++;
			}
			else if ( source.charAt( pos ) == ')' )
			{
				// Close paren; end current list, pop off stack
				if ( stack.size() == 0 )
				{
					throw new ParseSXErrorException();
				}
				last = stack.get( stack.size() - 1 );
				stack.remove( stack.size() - 1 );
				pos++;
			}
			else
			{
				// Try looking for:
				MatchResult res;
				
				// Whitespace
				res = match( whitespace, source, pos );
				if ( res.value != null )
				{
					pos = res.pos;
					continue;
				}
				
				// Atom
				res = matchAtom( source, pos );
				if ( res.value != null )
				{
					String s = res.value;
					if ( stack.size() > 0 )
					{
						Vector<Object> top = (Vector<Object>)stack.get( stack.size() - 1 );
						top.add( s );
					}
					else
					{
						last = s;
					}
					pos = res.pos;
					continue;
				}
				
				
				throw new ParseSXErrorException();
			}
		}

		return last;
	}
}


