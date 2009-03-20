//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.DocModel.DMModule.UnknownClassException;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;


public class DMIOReader
{
	public static class ParseErrorException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	public static class ParseStackException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	public static class UnknownModuleException extends Exception
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
	public static String quotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]~'()` ";
	public static String inStringUnescapedChars = "[0-9A-Za-z_" + Pattern.quote( quotedStringPunctuationChars ) + "]";
	
	public static String hexCharEscape = Pattern.quote( "\\x" ) + "[0-9A-Fa-f]+" + Pattern.quote( "x" );
	public static String whitespaceEscape = Pattern.quote( "\\" ) + "[nrt" + Pattern.quote( "\\" ) + "]";
	
	public static String escapeSequence = "(?:" + hexCharEscape + ")|(?:" + whitespaceEscape + ")";
	

	public static Pattern whitespace = Pattern.compile( "[" + Pattern.quote( " \t\n\r" ) + "]+" );
	public static Pattern unquotedString = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( unquotedStringPunctuationChars ) + "]+" );
	public static Pattern quotedString = Pattern.compile( Pattern.quote( "\"" ) + "(?:" + inStringUnescapedChars + "|" + escapeSequence + ")*" + Pattern.quote( "\"" ) );
	public static Pattern hexChar = Pattern.compile( hexCharEscape );
	public static Pattern identifier = Pattern.compile( "[A-Za-z_][0-9A-Za-z_]*" );
	
	
	
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
			return null;
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
		// Quoted string
		MatchResult res = match( quotedString, source, pos );
		if ( res != null )
		{
			res.value = evalString( res.value );
			return res;
		}
		
		// Unquoted string
		res = match( unquotedString, source, pos );
		if ( res != null )
		{
			return res;
		}
		
		return null;
	}
	
	
	public void eatWhitespace()
	{
		// Whitespace
		MatchResult res = match( whitespace, source, pos );
		if ( res != null )
		{
			pos = res.pos;
		}
	}
	
	
	
	
	
	private ArrayList<Object> stack;
	private ArrayList<String> nameStack;
	private Object result;
	private HashMap<String, DMModule> moduleTable;
	private String source;
	private int pos;
	
	
	private DMIOReader(String source)
	{
		stack = new ArrayList<Object>();
		nameStack = new ArrayList<String>();
		result = null;
		moduleTable = new HashMap<String, DMModule>();
		this.source = source;
		pos = 0;
	}
	
	
	private Object getTopOfStack()
	{
		return stack.get( stack.size() - 1 ); 
	}
	
	private String getTopOfNameStack()
	{
		return nameStack.get( nameStack.size() - 1 ); 
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void addItemToItemAtTopOfStack(Object item)
	{
		if ( stack.size() > 0 )
		{
			Object top = getTopOfStack();
			
			if ( top instanceof ArrayList )
			{
				((ArrayList<Object>)top).add( item );
			}
			else if ( top instanceof DMObject )
			{
				try
				{
					((DMObject)top).set( getTopOfNameStack(), item );
				}
				catch (InvalidFieldNameException e)
				{
					System.out.println( "Could not set a field named " + getTopOfNameStack() );
				}
			}
			else
			{
				throw new ParseStackException();
			}
		}
	}
	
	
	private void commitItem(Object item) throws ParseErrorException
	{
		if ( stack.size() == 0 )
		{
			result = item;
		}
		else
		{
			Object top = getTopOfStack();
			
			if ( top instanceof ArrayList )
			{
				// Nothing to do; just eat whitespace
				eatWhitespace();
			}
			else if ( top instanceof DMObject )
			{
				// Consume any leading whitespace
				eatWhitespace();
				
				// Get the field name
				String fieldName;
				MatchResult res = match( identifier, source, pos );
				if ( res == null )
				{
					throw new ParseErrorException();
				}
				fieldName = res.value;
				
				// Consume whitespace
				eatWhitespace();
				
				// Consume the '=' character
				if ( source.charAt( pos ) == '=' )
				{
					pos++;
				}
				else
				{
					throw new ParseErrorException();
				}
				
				// Consume whitespace
				eatWhitespace();
				
				nameStack.set( nameStack.size() - 1, fieldName );
			}
			else
			{
				throw new ParseStackException();
			}
		}
	}
	
	
	private void openList()
	{
		Object item = new ArrayList<Object>();
		// Append the new list to the list that is on the top of the stach; this builds the structure
		addItemToItemAtTopOfStack( item );
		// Make the top of the stack the new list
		stack.add( item );
		nameStack.add( "" );
	}
	
	private void closeList() throws ParseErrorException
	{
		// Ensure that there is an item to close
		if ( stack.size() == 0 )
		{
			throw new ParseErrorException();
		}
		// Ensure that it is a list
		Object item = getTopOfStack();
		if ( !( item instanceof ArrayList ) )
		{
			throw new ParseErrorException();
		}
		
		// Pop off stack
		stack.remove( stack.size() - 1 );
		nameStack.remove( nameStack.size() - 1 );
		
		commitItem( item );
	}
	
	private void openObject() throws ParseErrorException, UnknownModuleException, UnknownClassException
	{
		MatchResult res = null;
		
		// Get the module name
		String moduleName = null;
		res = match( identifier, source, pos );
		if ( res == null )
		{
			throw new ParseErrorException();
		}
		else
		{
			moduleName = res.value;
		}
		
		// Get the module
		DMModule module = moduleTable.get( moduleName );
		if ( module == null )
		{
			throw new UnknownModuleException();
		}
		
		// Need whitespace
		res = match( whitespace, source, pos );
		if ( res == null )
		{
			throw new ParseErrorException();
		}
		
		// Get class name
		String className = null;
		res = match( identifier, source, pos );
		if ( res == null )
		{
			throw new ParseErrorException();
		}
		else
		{
			className = res.value;
		}
		
		// Get the class
		DMObjectClass cls = module.get( className );
		
		DMObject item = cls.newInstance( new Object[] {} );
		
		// Append the new list to the list that is on the top of the stach; this builds the structure
		addItemToItemAtTopOfStack( item );

		// Make the top of the stack the new list
		stack.add( item );
		nameStack.add( "" );
	}
	
	private void closeObject() throws ParseErrorException
	{
		// Ensure that there is an item to close
		if ( stack.size() == 0 )
		{
			throw new ParseErrorException();
		}
		// Ensure that it is a list
		Object item = getTopOfStack();
		if ( !( item instanceof DMObject ) )
		{
			throw new ParseErrorException();
		}
		
		// Pop off stack
		stack.remove( stack.size() - 1 );
		nameStack.remove( nameStack.size() - 1 );
		
		commitItem( item );
	}
	
	
	@SuppressWarnings("unchecked")
	public Object readFromString(String source) throws ParseErrorException
	{
		int pos = 0;
		ArrayList<Object> stack = new ArrayList<Object>();
		while ( pos < source.length() )
		{
			if ( source.charAt( pos ) == '[' )
			{
				// Open square-bracket; start new list
				openList();
				pos++;
			}
			else if ( source.charAt( pos ) == ')' )
			{
				closeList();
				pos++;
			}
			else if ( source.charAt( pos ) == '(' )
			{
				openObject();
				pos++;
			}
			else if ( source.charAt( pos ) == ')' )
			{
				closeObject();
				pos++;
			}
			else
			{
				// Try looking for:
				MatchResult res;
				
				// Whitespace
				eatWhitespace();
				res = match( whitespace, source, pos );
				if ( res != null )
				{
					pos = res.pos;
					continue;
				}
				
				// Atom
				res = matchAtom( source, pos );
				if ( res != null )
				{
					String s = res.value;
					addItemToItemAtTopOfStack( s );
					pos = res.pos;
					continue;
				}
				
				
				throw new ParseErrorException();
			}
		}

		return result;
	}
}
