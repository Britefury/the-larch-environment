//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.DocModel.DMModule.UnknownClassException;
import BritefuryJ.DocModel.DMModuleResolver.CouldNotResolveModuleException;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;


public class DMIOReader
{
	public static class ParseErrorException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public ParseErrorException(int pos, String description)
		{
			super( "at " + pos + ": " + description );
		}
	}

	public static class ParseStackException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	public static class BadModuleNameException extends Exception
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
		int position;
		
		public MatchResult(String value, int position)
		{
			this.value = value;
			this.position = position;
		}
	}
	

	public static String unquotedStringPunctuationChars = "+-*/%^&|!$@.<>~";
	public static String quotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]{}~'()` ";
	public static String inStringUnescapedChars = "[0-9A-Za-z_" + Pattern.quote( quotedStringPunctuationChars ) + "]";
	
	public static String hexCharEscape = Pattern.quote( "\\x" ) + "[0-9A-Fa-f]+" + Pattern.quote( "x" );
	public static String whitespaceEscape = Pattern.quote( "\\" ) + "[nrt" + Pattern.quote( "\\" ) + "]";
	
	public static String escapeSequence = "(?:" + hexCharEscape + ")|(?:" + whitespaceEscape + ")";
	

	public static Pattern whitespace = Pattern.compile( "[" + Pattern.quote( " \t\n\r" ) + "]+" );
	public static Pattern unquotedString = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( unquotedStringPunctuationChars ) + "]+" );
	public static Pattern quotedString = Pattern.compile( Pattern.quote( "\"" ) + "(?:" + inStringUnescapedChars + "|" + escapeSequence + ")*" + Pattern.quote( "\"" ) );
	public static Pattern hexChar = Pattern.compile( hexCharEscape );
	public static Pattern identifier = Pattern.compile( "[A-Za-z_][0-9A-Za-z_]*" );
	
	
	
	protected static MatchResult match(Pattern pattern, String source, int position)
	{
		Matcher m = pattern.matcher( source.substring( position, source.length() ) );
		
		boolean bFound = m.find();
		if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
		{
			String matchString = m.group();
			int end = position + matchString.length();
			return new MatchResult( matchString, end );
		}
		else
		{
			return null;
		}
	}
	
	private static String evalString(String s)
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
	
	
	private static MatchResult matchAtom(String source, int position)
	{
		// Quoted string
		MatchResult res = match( quotedString, source, position );
		if ( res != null )
		{
			res.value = evalString( res.value );
			return res;
		}
		
		// Unquoted string
		res = match( unquotedString, source, position );
		if ( res != null )
		{
			return res;
		}
		
		return null;
	}
	
	
	private static MatchResult matchNull(String source, int position)
	{
		String nullString = "`null`";
		
		if ( source.substring( position, position + nullString.length() ).equals( nullString ) )
		{
			return new MatchResult( nullString, position + nullString.length() );
		}
		else
		{
			return null;
		}
	}
	
	
	private void eatWhitespace()
	{
		// Whitespace
		MatchResult res = match( whitespace, source, pos );
		if ( res != null )
		{
			pos = res.position;
		}
	}
	
	
	
	
	
	private ArrayList<Object> stack;
	private ArrayList<String> nameStack;
	private Object result;
	private HashMap<String, DMModule> moduleTable;
	private String source;
	private int pos;
	DMModuleResolver resolver;
	
	
	protected DMIOReader(String source, DMModuleResolver resolver)
	{
		stack = new ArrayList<Object>();
		nameStack = new ArrayList<String>();
		result = null;
		moduleTable = new HashMap<String, DMModule>();
		this.source = source;
		pos = 0;
		this.resolver = resolver;
	}
	
	
	private Object getTopOfStack()
	{
		return stack.get( stack.size() - 1 ); 
	}
	
	private String getTopOfNameStack()
	{
		return nameStack.get( nameStack.size() - 1 ); 
	}
	
	
	private void objectAcquireFieldName() throws ParseErrorException
	{
		eatWhitespace();

		
		// If the next character is not a ')', which would close the object
		if ( source.charAt( pos ) != ')' )
		{
			// Get the field name
			String fieldName;
			MatchResult res = match( identifier, source, pos );
			if ( res == null )
			{
				throw new ParseErrorException( pos, "Could not get field name in object key-value pair" );
			}
			pos = res.position;
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
				throw new ParseErrorException( pos, "Expected '=' in object key-value pair" );
			}
			
			// Consume whitespace
			eatWhitespace();
			
			// Put this name into the name stack
			nameStack.set( nameStack.size() - 1, fieldName );
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void closeItem(Object item) throws ParseErrorException
	{
		if ( stack.size() > 0 )
		{
			Object top = getTopOfStack();
			
			if ( top instanceof ArrayList )
			{
				((ArrayList<Object>)top).add( item );
				// Prepare for next element
				// Just eat whitespace
				eatWhitespace();
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

				objectAcquireFieldName();
			}
			else
			{
				throw new ParseStackException();
			}
		}
		else
		{
			result = item;
		}
	}
	
	
	
	private void openList()
	{
		Object item = new ArrayList<Object>();
		// Make the top of the stack the new list
		stack.add( item );
		nameStack.add( "" );
	}
	
	private void closeList() throws ParseErrorException
	{
		// Ensure that there is an item to close
		if ( stack.size() == 0 )
		{
			throw new ParseErrorException( pos, "'']' with no list to close" );
		}
		// Ensure that it is a list
		Object item = getTopOfStack();
		if ( !( item instanceof ArrayList<?> ) )
		{
			throw new ParseErrorException( pos, "']' attempting to close non-list" );
		}
		
		// Pop off stack
		stack.remove( stack.size() - 1 );
		nameStack.remove( nameStack.size() - 1 );
		
		closeItem( item );
	}
	
	private void openObject() throws ParseErrorException, BadModuleNameException, UnknownClassException
	{
		MatchResult res = null;
		
		// Get the module name
		String moduleName = null;
		res = match( identifier, source, pos );
		if ( res == null )
		{
			throw new ParseErrorException( pos, "Expected module name for object" );
		}
		else
		{
			pos = res.position;
			moduleName = res.value;
		}
		
		// Get the module
		DMModule module = moduleTable.get( moduleName );
		if ( module == null )
		{
			throw new BadModuleNameException();
		}
		
		// Need whitespace
		res = match( whitespace, source, pos );
		if ( res == null )
		{
			throw new ParseErrorException( pos, "Expected whitespace after module name for object" );
		}
		else
		{
			pos = res.position;
		}
		
		// Get class name
		String className = null;
		res = match( identifier, source, pos );
		if ( res == null )
		{
			throw new ParseErrorException( pos, "Expected class name for object" );
		}
		else
		{
			pos = res.position;
			className = res.value;
		}
		
		// Get the class
		DMObjectClass cls = module.get( className );
		
		DMObject item = cls.newInstance( new Object[] {} );
		
		// Make the top of the stack the new list
		stack.add( item );
		nameStack.add( "" );
		
		objectAcquireFieldName();
	}
	
	private void closeObject() throws ParseErrorException
	{
		// Ensure that there is an item to close
		if ( stack.size() == 0 )
		{
			throw new ParseErrorException( pos, "')' with no object to close" );
		}
		// Ensure that it is a list
		Object item = getTopOfStack();
		if ( !( item instanceof DMObject ) )
		{
			throw new ParseErrorException( pos, "')' attempting to close non-object" );
		}
		
		// Pop off stack
		stack.remove( stack.size() - 1 );
		nameStack.remove( nameStack.size() - 1 );
		
		closeItem( item );
	}
	
	
	public Object read() throws ParseErrorException, BadModuleNameException, UnknownClassException
	{
		while ( pos < source.length() )
		{
			if ( source.charAt( pos ) == '}' )
			{
				// Close bindings
				// Don't consume the character
				break;
			}
			else if ( source.charAt( pos ) == '[' )
			{
				// Open square-bracket; start new list
				pos++;
				openList();
			}
			else if ( source.charAt( pos ) == ']' )
			{
				pos++;
				closeList();
			}
			else if ( source.charAt( pos ) == '(' )
			{
				pos++;
				openObject();
			}
			else if ( source.charAt( pos ) == ')' )
			{
				pos++;
				closeObject();
			}
			else
			{
				MatchResult res;
				
				// Whitespace
				res = match( whitespace, source, pos );
				if ( res != null )
				{
					pos = res.position;
					continue;
				}
				
				// Atom
				res = matchAtom( source, pos );
				if ( res != null )
				{
					pos = res.position;
					String s = res.value;
					closeItem( s );
					continue;
				}
				
				// Null
				res = matchNull( source, pos );
				if ( res != null )
				{
					pos = res.position;
					closeItem( null );
					continue;
				}
				
				
				// Parse error, get the next token to build an error message
				String token = source.substring( pos );
				int space = token.indexOf( ' ' );
				int tab = token.indexOf( '\t' );
				int newline = token.indexOf( '\n' );
				if ( space == -1 )
				{
					space = token.length();
				}
				if ( tab == -1 )
				{
					tab = token.length();
				}
				if ( newline == -1 )
				{
					newline = token.length();
				}
				int end = Math.min( Math.min( space, tab ), newline );
				token = token.substring( 0, end );
				throw new ParseErrorException( pos, "Unknown token '" + token + "'" );
			}
		}
		
		if ( stack.size() != 0 )
		{
			throw new ParseErrorException( pos, "List or object remains to be closed" );
		}

		return result;
	}
	
	
	private Object readDocument() throws ParseErrorException, BadModuleNameException, UnknownClassException, CouldNotResolveModuleException
	{
		eatWhitespace();
		
		Object obj = null;
		
		// Do we have bindings?
		if ( source.charAt( pos ) == '{' )
		{
			pos++;
			// Read bindings
			
			eatWhitespace();
			
			// Read bindings until a ':' is encountered
			while ( source.charAt( pos ) != ':' )
			{
				String key, value;
				MatchResult res = null;
				
				// Get key
				res = match( identifier, source, pos );
				if ( res != null )
				{
					key = res.value;
					pos = res.position;
				}
				else
				{
					throw new ParseErrorException( pos, "Expected name for binding" );
				}
				
				
				// Consume the '=' character
				eatWhitespace();
				if ( source.charAt( pos ) != '=' )
				{
					throw new ParseErrorException( pos, "Expected '=' for binding" );
				}
				pos++;
				
				
				// Get value
				res = matchAtom( source, pos );
				if ( res != null )
				{
					value = res.value;
					pos = res.position;
				}
				else
				{
					throw new ParseErrorException( pos, "Expected location for binding" );
				}
				
				
				// Get the module, and add to the module table
				DMModule module = resolver.getModule( value );
				moduleTable.put( key, module );
				
				
				eatWhitespace();
			}
			
			// Consume the ':' and any whitespace
			pos++;
			eatWhitespace();
			
			obj = read();
			
			eatWhitespace();
			
			if ( source.charAt( pos ) != '}' )
			{
				throw new ParseErrorException( pos, "Expected '}'" );
			}
			
			pos++;
		}
		else
		{
			obj = read();
		}

		
		eatWhitespace();
		
		if ( pos < source.length() )
		{
			throw new ParseErrorException( pos, "Expected end, found " + source.substring( pos ) );
		}
		
		return obj;
	}
	
	
	public static Object readFromString(String source, DMModuleResolver resolver) throws ParseErrorException, BadModuleNameException, UnknownClassException, CouldNotResolveModuleException
	{
		DMIOReader reader = new DMIOReader( source, resolver );
		return reader.readDocument();
	}
}
