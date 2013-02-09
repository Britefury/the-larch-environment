//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import BritefuryJ.Isolation.IsolationBarrier;
import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;

public class DMIOWriter extends DMIO
{
	public static class InvalidDataTypeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static class CannotEmbedValue extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public static final Pattern quotedStringContents = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( quotedStringPunctuationChars ) + "]+" );
	
	
	
	private HashMap<DMSchema, String> moduleToName;
	private HashSet<String> names;
	private ArrayList<DMSchema> modulesInOrder;
	private PyList embeddedValues;
	
	
	
	
	protected DMIOWriter()
	{
		moduleToName = new HashMap<DMSchema, String>();
		names = new HashSet<String>();
		modulesInOrder = new ArrayList<DMSchema>();
	}
	
	
	private void initEmbeddedValues()
	{
		embeddedValues = new PyList();
	}
	
	private int embedValue(Object value)
	{
		if ( embeddedValues == null )
		{
			throw new CannotEmbedValue();
		}
		
		int index = embeddedValues.size();
		embeddedValues.append( Py.java2py( value ) );
		return index;
	}


	

	private static void writeNull(StringBuilder builder)
	{
		builder.append( "`null`" );
	}
	
	private static void writeString(StringBuilder builder, String content)
	{
		builder.append( stringAsAtom( content ) );
	}
	
	private static boolean canUnquoteString(String s)
	{
		if ( s.length() == 0 )
		{
			return false;
		}
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt( i );
			if ( !unquotedCharBits.get( (int)c ) )
			{
				return false;
			}
		}
		
		return true;
	}

	public static String stringAsAtom(String content)
	{
		if ( canUnquoteString( content ) )
		{
			return content;
		}
		else
		{
			return quoteString( content );
		}
	}
	
	private void writeList(StringBuilder builder, List<Object> content) throws InvalidDataTypeException
	{
		builder.append( "[" );
		if ( content.size() > 0 )
		{
			for (Object v: content.subList( 0, content.size() - 1 ))
			{
				writeItem( builder, v );
				builder.append( " " );
			}
			writeItem( builder, content.get( content.size() - 1 ) );
		}
		builder.append( "]" );
	}
	
	private void writeObject(StringBuilder builder, DMObject obj) throws InvalidDataTypeException
	{
		DMObjectClass cls = obj.getDMObjectClass();
		DMSchema mod = cls.getSchema();
		
		builder.append( "(" );
		
		String modName = moduleToName.get( mod );
		if ( modName == null )
		{
			String shortName = mod.getShortName();
			modName = shortName;
			int index = 2;
			while ( names.contains( modName ) )
			{
				modName = shortName + index;
				index++;
			}
			
			moduleToName.put( mod, modName );
			names.add( modName );
			modulesInOrder.add( mod );
		}
		
		builder.append( modName );
		builder.append( " " );
		builder.append( cls.getName() );
		
		for (int i = 0; i < cls.getNumFields(); i++)
		{
			Object x = obj.get( i );
			if ( x != null )
			{
				builder.append( " " );
				builder.append( cls.getField( i ).getName() );
				builder.append( "=" );
				writeItem( builder, x );
			}
		}
		builder.append( ")" );
	}
	
	
	private void writeEmbeddedObject(StringBuilder builder, Object embed) throws InvalidDataTypeException
	{
		builder.append( "<<Em:" );
		int index = embedValue( embed );
		builder.append( Integer.toString( index ) );
		builder.append( ">>" );
	}


	
	@SuppressWarnings("unchecked")
	private void writeItem(StringBuilder builder, Object content) throws InvalidDataTypeException
	{
		if ( content == null )
		{
			writeNull( builder );
		}
		else if ( content instanceof String )
		{
			writeString( builder, (String)content );
		}
		else if ( content instanceof PyString )
		{
			writeString( builder, content.toString() );
		}
		else if ( content instanceof PyUnicode )
		{
			writeString( builder, content.toString() );
		}
		else if ( content instanceof List )
		{
			writeList( builder, (List<Object>)content );
		}
		else if ( content instanceof DMObject )
		{
			writeObject( builder, (DMObject)content );
		}
		else
		{
			writeEmbeddedObject( builder, content );
		}
	}
	
	
	private String writeDocument(Object content) throws InvalidDataTypeException
	{
		StringBuilder builder = new StringBuilder();
		writeItem( builder, content );
		
		if ( moduleToName.size() > 0 )
		{
			StringBuilder docBuilder = new StringBuilder();
			docBuilder.append( "{" );
			
			for (DMSchema mod: modulesInOrder)
			{
				String name = moduleToName.get( mod );
				docBuilder.append( name );
				docBuilder.append( "=" );
				docBuilder.append( mod.getLocation() );
				if ( mod.getVersion() > 1 )
				{
					docBuilder.append( "<" );
					docBuilder.append( String.valueOf( mod.getVersion() ) );
					docBuilder.append( ">" );
				}
				docBuilder.append( " ");
			}
			
			docBuilder.append( ": " );
			
			docBuilder.append( builder );
			
			docBuilder.append( "}" );
			
			builder = docBuilder;
		}
		
		return builder.toString();
	}

	
	public static PyObject writeAsState(Object content) throws InvalidDataTypeException
	{
		DMIOWriter writer = new DMIOWriter();
		writer.initEmbeddedValues();
		String s = writer.writeDocument( content );
		if ( ( writer.embeddedValues != null  &&  writer.embeddedValues.size() > 0 ) )
		{
			return new PyTuple( Py.newString( s ), writer.embeddedValues );
		}
		else
		{
			return Py.newString( s );
		}
	}


	public static String writeAsString(Object content) throws InvalidDataTypeException
	{
		DMIOWriter writer = new DMIOWriter();
		return writer.writeDocument( content );
	}












	protected static void escape(StringBuilder builder, char c)
	{
		switch ( c )
		{
			case '\\':
				builder.append( "\\\\" );
				break;
			case '\n':
				builder.append( "\\n" );
				break;
			case '\r':
				builder.append( "\\r" );
				break;
			case '\t':
				builder.append( "\\t" );
				break;
			default:
				builder.append( "\\x" );
				builder.append( Integer.toString( (int)c, 16 ) );
				builder.append( "x" );
				break;
		}
	}
	
	protected static String quoteString(String s)
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append( "\"" );
		// Escape newlines, CRs, tabs, and backslashes
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt( i );
			
			if ( quotedCharBits.get( (int)c ) )
			{
				builder.append( c );
			}
			else
			{
				escape( builder, c );
			}
		}
		
		builder.append( "\"" );

		return builder.toString();
	}
}
