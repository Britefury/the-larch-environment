//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

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
	}
	

	public static String unquotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]~";
	public static String quotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]~'() ";
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
			return new MatchResult( null, pos );
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
	
	
	
	@SuppressWarnings("unchecked")
	public static Object readSX(String source) throws ParseSXErrorException
	{
		int pos = 0;
		Vector<Object> stack = new Vector<Object>();
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
					Vector<Object> top = (Vector<Object>)stack.lastElement();
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
				last = stack.lastElement();
				stack.removeElementAt( stack.size() - 1 );
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
				
				// Quoted string
				res = match( quotedString, source, pos );
				if ( res.value != null )
				{
					String s = evalString( res.value );
					if ( stack.size() > 0 )
					{
						Vector<Object> top = (Vector<Object>)stack.lastElement();
						top.add( s );
					}
					else
					{
						last = s;
					}
					pos = res.pos;
					continue;
				}
				
				// Unquoted string
				res = match( unquotedString, source, pos );
				if ( res.value != null )
				{
					String s = res.value;
					if ( stack.size() > 0 )
					{
						Vector<Object> top = (Vector<Object>)stack.lastElement();
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


/*

_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )
_whitespace = re.compile( '[%s]+'  %  re.escape( ' \t\n' ), 0 )
_unquotedString = re.compile( '[%s]+'  %  re.escape( _unquotedStringChars ), 0 )
_quotedString = re.compile( r'''(?:"(?:[^"\n\r\\]|(?:"")|(?:\\x[0-9a-fA-F]+)|(?:\\.))*")|(?:'(?:[^'\n\r\\]|(?:'')|(?:\\x[0-9a-fA-F]+)|(?:\\.))*')''', 0 )


def _match(regexp, source, pos):
	m = regexp.match( source, pos )
	if m is not None:
		matchString = m.group()
		if len( matchString ) > 0:
			return matchString, pos + len( matchString )
	return None, pos


def readSX(source):
	if isinstance( source, file ):
		source = source.read()
	pos = 0
	stack = []
	last = None
	while pos < len( source ):
		if source[pos] == '(':
			# Open paren; start new list
			xs = []
			# Append the new list to the list that is on the top of the stack; this builds the structure
			if len( stack ) > 0:
				stack[-1].append( xs )
			# Make the top of the stack the new list
			stack.append( xs )
			pos += 1
		elif source[pos] == ')':
			# Close parent; end current list, pop off stack
			last = stack.pop()
			pos += 1
		else:
			# Try looking for:
			
			# White space; skip
			w, pos = _match( _whitespace, source, pos )
			if w is not None:
				continue
			
			# Unicode string
			if source[pos].lower() == 'u':
				pos += 1
				u, pos = _match( _quotedString, source, pos )
				if u is not None:
					u = eval( 'u' + u )
					if len( stack ) > 0:
						stack[-1].append( u )
					else:
						last = u
					continue
					
			# Quoted string
			q, pos = _match( _quotedString, source, pos )
			if q is not None:
				q = eval( q )
				if len( stack ) > 0:
					stack[-1].append( q )
				else:
					last = q
				continue
		
			# Unquoted string / atom
			uq, pos = _match( _unquotedString, source, pos )
			if uq is not None:
				if len( stack ) > 0:
					stack[-1].append( uq )
				else:
					last = uq
				continue
		
			raise ValueError

	return last
				
			
			
*/