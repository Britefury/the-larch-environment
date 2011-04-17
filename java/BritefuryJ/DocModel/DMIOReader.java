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

import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;

import BritefuryJ.DocModel.Resource.DMJavaResource;
import BritefuryJ.DocModel.Resource.DMPyResource;
import BritefuryJ.Isolation.IsolationBarrier;


public class DMIOReader extends DMIO
{
	public static class ParseErrorException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public ParseErrorException(int pos, String description)
		{
			super( "at " + pos + ": " + description );
		}
	}
	
	public static class InvalidEscapeSequenceException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	public static class ParseStackException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	public static class CannotReadEmbeddedValuesException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	public static class BadModuleNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	
	/*
	Document model IO
	
	
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
	
	
	
	
	private static class SchemaRef
	{
		private DMSchema schema;
		private int version;
		
		
		public SchemaRef(DMSchema schema, int version)
		{
			this.schema = schema;
			this.version = version;
		}
		
		
		public DMObjectReader getReader(String className)
		{
			return schema.getReader( className, version );
		}
		
		
		public String toString()
		{
			return "SchemaRef( name='" + schema.getLocation() + "', version=" + version + " )";
		}
	}
	
	
	
	private static class ObjectBuilder
	{
		private SchemaRef schemaRef;
		private String className;
		private HashMap<String, Object> fieldValues = new HashMap<String, Object>();;
		
		private String currentFieldName = null;
		
		
		public ObjectBuilder(SchemaRef schemaRef, String className)
		{
			this.schemaRef = schemaRef;
			this.className = className;
		}
		
		
		public DMObject create()
		{
			DMObjectReader reader = schemaRef.getReader( className );
			return reader.readObject( fieldValues );
		}
		
		
		public void beginField(String fieldName)
		{
			if ( currentFieldName != null )
			{
				throw new RuntimeException( "Field name already acquired" );
			}
			else
			{
				currentFieldName = fieldName;
			}
		}

		public void endField(Object value)
		{
			if ( currentFieldName == null )
			{
				throw new RuntimeException( "No field name acquired" );
			}
			else
			{
				fieldValues.put( currentFieldName, value );
				currentFieldName = null;
			}
		}
	}
	

	
	protected static MatchResult matchWhitespace(String s, int pos)
	{
		StringBuilder result = new StringBuilder();
		while ( pos < s.length() )
		{
			char c = s.charAt( pos );
			if ( c == ' ' || c == '\t' || c == '\r' || c == '\n' )
			{
				result.append( c );
				pos++;
			}
			else
			{
				break;
			}
		}
		
		if ( result.length() > 0 )
		{
			return new MatchResult( result.toString(), pos );
		}
		else
		{
			return null;
		}
	}
	
	protected static MatchResult match(Pattern pattern, String source, int position)
	{
		Matcher m = pattern.matcher( source.substring( position, source.length() ) );
		
		boolean bFound = m.lookingAt();
		if ( bFound  &&  m.end() > 0 )
		{
			int end = position + m.end();
			return new MatchResult( source.substring( position, end ), end );
		}
		else
		{
			return null;
		}
	}
	
	private static MatchResult matchQuotedString(String s, int pos)
	{
		if ( s.charAt( pos ) == '\"' )
		{
			StringBuilder result = new StringBuilder();
			pos++;
			while ( pos < s.length() )
			{
				char c = s.charAt( pos );
				if ( quotedCharBits.get( (int)c ) )
				{
					result.append( c );
					pos++;
				}
				else if ( c == '\\'  &&  pos < s.length()-1 )
				{
					char escapeCode = s.charAt( pos + 1 );
					if ( escapeCode == 'n' )
					{
						result.append( '\n' );
						pos += 2;
					}
					else if ( escapeCode == 'r' )
					{
						result.append( '\r' );
						pos += 2;
					}
					else if ( escapeCode == 't' )
					{
						result.append( '\t' );
						pos += 2;
					}
					else if ( escapeCode == '\\' )
					{
						result.append( '\\' );
						pos += 2;
					}
					else if ( escapeCode == 'x' )
					{
						StringBuilder hexString = new StringBuilder();
						int hexIndex = pos + 2;
						char h = '0';
						while ( hexIndex < s.length() )
						{
							h = s.charAt( hexIndex );
							if ( ( h >= '0' && h <= '9' )  ||  ( h >= 'A' && h <= 'F' )  ||  ( h >= 'a' && h <= 'f' ) )
							{
								hexString.append( h );
								hexIndex++;
							}
							else
							{
								break;
							}
						}
						if ( h == 'x'  &&  hexString.length() > 0 )
						{
							// Found the terminating 'x' character
							char hexChar = (char)Integer.valueOf( hexString.toString(), 16 ).intValue();
							result.append( hexChar );
							pos = hexIndex + 1;
						}
						else
						{
							throw new InvalidEscapeSequenceException();
						}
					}
					else
					{
						// Did not recognise the escape sequence
						throw new InvalidEscapeSequenceException();
					}
				}
				else if ( c == '\"' )
				{
					pos++;
					return new MatchResult( result.toString(), pos );
				}
			}
		}

		return null;
	}
	
	
	private static MatchResult matchUnquotedString(String s, int pos)
	{
		StringBuilder result = new StringBuilder();
		while ( pos < s.length() )
		{
			char c = s.charAt( pos );
			if ( unquotedCharBits.get( (int)c ) )
			{
				result.append( c );
				pos++;
			}
			else
			{
				break;
			}
		}
		
		if ( result.length() > 0 )
		{
			return new MatchResult( result.toString(), pos );
		}
		else
		{
			return null;
		}
	}
	
	
	private static MatchResult matchAtom(String source, int position)
	{
		// Quoted string
		MatchResult res = matchQuotedString( source, position );
		if ( res != null )
		{
			return res;
		}
		
		// Unquoted string
		res = matchUnquotedString( source, position );
		if ( res != null )
		{
			return res;
		}
		
		return null;
	}
	
	
	private static MatchResult matchPositiveDecimalInteger(String s, int pos)
	{
		StringBuilder result = new StringBuilder();
		while ( pos < s.length() )
		{
			char c = s.charAt( pos );
			if ( c >= '0' && c <= '9' )
			{
				result.append( c );
				pos++;
			}
			else
			{
				break;
			}
		}
		
		if ( result.length() > 0 )
		{
			return new MatchResult( result.toString(), pos );
		}
		else
		{
			return null;
		}
	}
	
	private static MatchResult matchIdentifier(String s, int pos)
	{
		StringBuilder result = new StringBuilder();
		if ( pos < s.length() )
		{
			char c = s.charAt( pos );
			if ( c >= 'A' && c <= 'Z'  ||  c >= 'a' && c <= 'z'  ||  c == '_' )
			{
				result.append( c );
				pos++;
			}
			else
			{
				return null;
			}
		}
		while ( pos < s.length() )
		{
			char c = s.charAt( pos );
			if ( c >= 'A' && c <= 'Z'  ||  c >= 'a' && c <= 'z'  ||  c == '_'  ||  c >= '0' && c <= '9' )
			{
				result.append( c );
				pos++;
			}
			else
			{
				break;
			}
		}
		
		if ( result.length() > 0 )
		{
			return new MatchResult( result.toString(), pos );
		}
		else
		{
			return null;
		}
	}
	
	
	private static MatchResult matchNull(String source, int position)
	{
		String nullString = "`null`";
		
		if ( source.length()  >=  ( position + nullString.length() ) )
		{
			if ( source.substring( position, position + nullString.length() ).equals( nullString ) )
			{
				return new MatchResult( nullString, position + nullString.length() );
			}
		}
		return null;
	}
	
	
	private void eatWhitespace()
	{
		MatchResult res = matchWhitespace( source, pos );
		if ( res != null )
		{
			pos = res.position;
		}
	}
	
	
	
	
	
	private ArrayList<Object> stack;
	private Object result;
	private HashMap<String, SchemaRef> moduleTable = new HashMap<String, SchemaRef>();
	private ArrayList<Object> embeddedValues;
	private PyList embeddedPyValues;
	private String source;
	private int pos;
	
	
	protected DMIOReader(String source)
	{
		stack = new ArrayList<Object>();
		result = null;
		this.source = source;
		pos = 0;
	}
	
	
	private void initEmbeddedValues()
	{
		embeddedValues = new ArrayList<Object>();
	}
	
	private void initEmbeddedPyValues(PyList values)
	{
		embeddedPyValues = values;
	}
	
	private Object getEmbeddedValue(int index)
	{
		if ( embeddedValues == null )
		{
			throw new CannotReadEmbeddedValuesException();
		}
		
		return embeddedValues.get( index );
	}
	
	private PyObject getEmbeddedPyValue(int index)
	{
		if ( embeddedPyValues == null )
		{
			throw new CannotReadEmbeddedValuesException();
		}
		
		return embeddedPyValues.pyget( index );
	}
	
	
	private Object getTopOfStack()
	{
		return stack.get( stack.size() - 1 ); 
	}
	
	
	private void objectAcquireFieldName() throws ParseErrorException
	{
		eatWhitespace();

		
		// If the next character is not a ')', which would close the object
		if ( source.charAt( pos ) != ')' )
		{
			// Get the field name
			String fieldName;
			MatchResult res = matchIdentifier( source, pos );
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
			ObjectBuilder obj = (ObjectBuilder)getTopOfStack();
			obj.beginField( fieldName );
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
			else if ( top instanceof ObjectBuilder )
			{
				((ObjectBuilder)top).endField( item );

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
			throw new ParseErrorException( pos, "']' attempting to close " + item.getClass().getName() );
		}
		
		// Pop off stack
		stack.remove( stack.size() - 1 );
		
		closeItem( item );
	}
	
	private void openObject() throws ParseErrorException
	{
		MatchResult res = null;
		
		// Get the schema name
		String moduleName = null;
		res = matchIdentifier( source, pos );
		if ( res == null )
		{
			throw new ParseErrorException( pos, "Expected schema name for object" );
		}
		else
		{
			pos = res.position;
			moduleName = res.value;
		}
		
		// Get the schema reference
		SchemaRef schemaRef = moduleTable.get( moduleName );
		if ( schemaRef == null )
		{
			throw new BadModuleNameException();
		}
		
		// Need whitespace
		res = matchWhitespace( source, pos );
		if ( res == null )
		{
			throw new ParseErrorException( pos, "Expected whitespace after schema name for object" );
		}
		pos = res.position;
		
		// Get class name
		String className = null;
		res = matchIdentifier( source, pos );
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
		ObjectBuilder item = new ObjectBuilder( schemaRef, className );
		
		// Make the top of the stack the new list
		stack.add( item );
		
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
		Object builder = getTopOfStack();
		if ( !( builder instanceof ObjectBuilder ) )
		{
			throw new ParseErrorException( pos, "')' attempting to close " + builder.getClass().getName() );
		}
		
		// Pop off stack
		stack.remove( stack.size() - 1 );
		
		DMObject value = ((ObjectBuilder)builder).create();
		
		closeItem( value );
	}
	
	
	
	@SuppressWarnings("unchecked")
	private Object read() throws ParseErrorException
	{
		while ( pos < source.length() )
		{
			eatWhitespace();
			
			if ( pos >= source.length() )
			{
				break;
			}
			
			if ( source.charAt( pos ) == '}' )
			{
				// This character is used to close the bindings; the objects are contained *within* this bindings list,
				// so don't consume this character.
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
			else if ( source.charAt( pos ) == '<' )
			{
				try
				{
					if ( source.substring( pos, pos+5 ).equals( "<<Ja:" ) )
					{
						pos += 5;
		
						MatchResult res;
		
						// Whitespace
						res = matchWhitespace( source, pos );
						if ( res == null )
						{
							throw new ParseErrorException( pos, "Expected whitespace after opening Java resource" );
						}
						pos = res.position;
		
						// Atom
						res = matchAtom( source, pos );
						if ( res == null )
						{
							throw new ParseErrorException( pos, "Expected content after opening Java resource" );
						}
						pos = res.position;
						String serialised = res.value;
						
						if ( !source.substring( pos, pos+2 ).equals( ">>" ) )
						{
							throw new ParseErrorException( pos, "Expected >> to close Java resource" );
						}
						pos += 2;
						
						DMJavaResource resource = DMJavaResource.serialisedResource( serialised );
						closeItem( resource );
					}
					else if ( source.substring( pos, pos+5 ).equals( "<<Py:" ) )
					{
						pos += 5;
		
						MatchResult res;
		
						// Whitespace
						res = matchWhitespace( source, pos );
						if ( res == null )
						{
							throw new ParseErrorException( pos, "Expected whitespace after opening Python resource" );
						}
						pos = res.position;
		
						// Atom
						res = matchAtom( source, pos );
						if ( res == null )
						{
							throw new ParseErrorException( pos, "Expected content after opening Python resource" );
						}
						pos = res.position;
						String serialised = res.value;
						
						if ( !source.substring( pos, pos+2 ).equals( ">>" ) )
						{
							throw new ParseErrorException( pos, "Expected >> to close Python resource" );
						}
						pos += 2;
						
						DMPyResource resource = DMPyResource.serialisedResource( serialised );
						closeItem( resource );
					}
					else if ( source.substring( pos, pos+5 ).equals( "<<Em:" ) )
					{
						pos += 5;
		
						MatchResult res;
		
						// Index
						res = matchPositiveDecimalInteger( source, pos );
						if ( res == null )
						{
							throw new ParseErrorException( pos, "Expected index after opening embedded object" );
						}
						pos = res.position;
						int index = Integer.parseInt( res.value );
						
						if ( !source.substring( pos, pos+2 ).equals( ">>" ) )
						{
							throw new ParseErrorException( pos, "Expected >> to close embedded object" );
						}
						pos += 2;
						
						PyObject value = getEmbeddedPyValue( index );
						DMEmbeddedObject embed = new DMEmbeddedObject( value );
						closeItem( embed );
					}
					else if ( source.substring( pos, pos+8 ).equals( "<<EmIso:" ) )
					{
						pos += 8;
		
						MatchResult res;
		
						// Index
						res = matchPositiveDecimalInteger( source, pos );
						if ( res == null )
						{
							throw new ParseErrorException( pos, "Expected index after opening embedded isolated object" );
						}
						pos = res.position;
						int index = Integer.parseInt( res.value );
						
						if ( !source.substring( pos, pos+2 ).equals( ">>" ) )
						{
							throw new ParseErrorException( pos, "Expected >> to close embedded object" );
						}
						pos += 2;
						
						Object value = getEmbeddedValue( index );
						DMEmbeddedIsolatedObject embed = new DMEmbeddedIsolatedObject();
						embed.setIsolationBarrier( (IsolationBarrier<PyObject>)value );
						closeItem( embed );
					}
					else
					{
						throw new ParseErrorException( pos, "Invalid resource" );
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					throw new ParseErrorException( pos, "Insufficient data for resource" );
				}
			}
			else
			{
				MatchResult res;
				
				// Consume any whitespace
				res = matchWhitespace( source, pos );
				if ( res != null )
				{
					pos = res.position;
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
			StringBuilder unclosedItemClasses = new StringBuilder();
			for (Object item: stack)
			{
				unclosedItemClasses.append( item.getClass().getName() + "\n" );
			}
			throw new ParseErrorException( pos, "Unclosed items:\n" + unclosedItemClasses.toString() );
		}

		return result;
	}
	
	
	private Object readDocument() throws ParseErrorException, BadModuleNameException
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
				int version = 1;
				MatchResult res = null;
				
				// Get key
				res = matchIdentifier( source, pos );
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
				eatWhitespace();
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
				
				
				// Get the version number
				eatWhitespace();
				if ( source.charAt( pos ) == '<' )
				{
					pos++;
					
					// Get version
					eatWhitespace();
					res = matchPositiveDecimalInteger( source, pos );
					if ( res != null )
					{
						version = Integer.valueOf( res.value );
						pos = res.position;
					}
					else
					{
						throw new ParseErrorException( pos, "Expected version number" );
					}
					
					
					// Consume the '>' character
					eatWhitespace();
					if ( source.charAt( pos ) != '>' )
					{
						throw new ParseErrorException( pos, "Expected '>' to close version number" );
					}
					pos++;
				}
				
				
				// Get the schema, and add to the schema table
				DMSchema schema = DMSchemaResolver.getSchema( value );
				
				if ( schema == null )
				{
					throw new DMSchema.UnknownSchemaException( value );
				}
				
				// Ensure that the requested version is supported
				if ( version > schema.getVersion() )
				{
					// This input data uses a newer schema version than the one we have available here.
					// We cannot load this.
					throw new DMSchema.UnsupportedSchemaVersionException( value, schema.getVersion(), version );
				}
				SchemaRef schemaRef = new SchemaRef( schema, version );
				moduleTable.put( key, schemaRef );
				
				
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
	
	
	
	public static Object readFromState(PyObject state) throws ParseErrorException, BadModuleNameException
	{
		DMIOReader reader;
		if ( state instanceof PyTuple )
		{
			PyTuple tup = (PyTuple)state;
			
			String source = tup.pyget( 0 ).asString();
			reader = new DMIOReader( source );

			
			PyList embedded = (PyList)tup.pyget( 1 );
			reader.initEmbeddedValues();
			for (Object e: embedded)
			{
				reader.embeddedValues.add( e );
			}

			PyList embeddedPy = (PyList)tup.pyget( 2 );
			reader.initEmbeddedPyValues( embeddedPy );
		}
		else if ( state instanceof PyString  ||  state instanceof PyUnicode )
		{
			String source = state.asString();
			reader = new DMIOReader( source );
		}
		else
		{
			throw Py.TypeError( "DMIOReader.readFromState(): Pickle state should be a Python string, unicode, or tuple" );
		}

		
		try
		{
			return reader.readDocument();
		}
		catch (StackOverflowError e)
		{
			e.printStackTrace();
			return null;
		}
	}
		
	public static Object readFromString(String source) throws ParseErrorException, BadModuleNameException
	{
		try
		{
			DMIOReader reader = new DMIOReader( source );
			return reader.readDocument();
		}
		catch (StackOverflowError e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
