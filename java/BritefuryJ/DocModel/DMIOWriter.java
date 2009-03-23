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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.python.core.PyString;
import org.python.core.PyUnicode;

public class DMIOWriter
{
	public static class InvalidDataTypeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public static String unquotedStringPunctuationChars = "+-*/%^&|!$@.,<>~";
	public static String quotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]{}~'()` ";
	public static Pattern unquotedString = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( unquotedStringPunctuationChars ) + "]+" );
	public static Pattern quotedStringContents = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( quotedStringPunctuationChars ) + "]+" );
	
	
	
	private HashMap<DMModule, String> moduleToName;
	private HashSet<String> names;
	private ArrayList<DMModule> modulesInOrder;
	
	
	
	
	private DMIOWriter()
	{
		moduleToName = new HashMap<DMModule, String>();
		names = new HashSet<String>();
		modulesInOrder = new ArrayList<DMModule>();
	}
	

	

	
	public static void writeString(StringBuilder builder, String content)
	{
		Matcher m = unquotedString.matcher( content );
		boolean bFound = m.find();
		if ( bFound  &&  m.start() == 0  &&  m.end() == content.length() )
		{
			builder.append( content );
		}
		else
		{
			builder.append( quoteString( content ) );
		}
	}
	
	public void writeList(StringBuilder builder, List<Object> content) throws InvalidDataTypeException
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
	
	public void writeObject(StringBuilder builder, DMObject obj) throws InvalidDataTypeException
	{
		DMObjectClass cls = obj.getDMClass();
		DMModule mod = cls.getModule();
		
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
			if ( !DMNode.isNull( x ) )
			{
				builder.append( " " );
				builder.append( cls.getField( i ).getName() );
				builder.append( "=" );
				writeItem( builder, x );
			}
		}
		builder.append( ")" );
	}
	
	
	@SuppressWarnings("unchecked")
	public void writeItem(StringBuilder builder, Object content) throws InvalidDataTypeException
	{
		if ( content instanceof String )
		{
			writeString( builder, (String)content );
		}
		else if ( content instanceof PyString )
		{
			writeString( builder, ((PyString)content).toString() );
		}
		else if ( content instanceof PyUnicode )
		{
			writeString( builder, ((PyUnicode)content).toString() );
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
			System.out.println( "Content data type: " + content.getClass().getName() + ", content data: " + content.toString() );
			throw new InvalidDataTypeException();
		}
	}
	
	
	public String writeDocument(Object content) throws InvalidDataTypeException
	{
		StringBuilder builder = new StringBuilder();
		writeItem( builder, content );
		
		if ( moduleToName.size() > 0 )
		{
			StringBuilder docBuilder = new StringBuilder();
			docBuilder.append( "{" );
			
			for (DMModule mod: modulesInOrder)
			{
				String name = moduleToName.get( mod );
				docBuilder.append( name );
				docBuilder.append( "=" );
				docBuilder.append( mod.getLocation() );
				docBuilder.append( " ");
			}
			
			docBuilder.append( ": " );
			
			docBuilder.append( builder );
			
			docBuilder.append( "}" );
			
			return docBuilder.toString();
		}
		else
		{
			return builder.toString();
		}
	}



	public static String write(Object content) throws InvalidDataTypeException
	{
		DMIOWriter writer = new DMIOWriter();
		return writer.writeDocument( content );
	}












	public static void escape(StringBuilder builder, String x)
	{
		for (int i = 0; i < x.length(); i++)
		{
			char c = x.charAt( i );
			
			if ( c == '\\' )
			{
				builder.append( "\\\\" );
			}
			else if ( c == '\n' )
			{
				builder.append( "\\n" );
			}
			else if ( c == '\r' )
			{
				builder.append( "\\r" );
			}
			else if ( c == '\t' )
			{
				builder.append( "\\t" );
			}
			else
			{
				builder.append(  "\\x" + Integer.toString( (int)c, 16 ) + "x" );
			}
		}
	}
	
	public static String quoteString(String s)
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append( "\"" );
		// Escape newlines, CRs, tabs, and backslashes
		int pos = 0;
		
		while ( pos < s.length() )
		{
			Matcher m = quotedStringContents.matcher( s.substring( pos ) );
			
			if ( m.find()  &&  m.end() > m.start() )
			{
				if ( m.start() > 0 )
				{
					escape( builder, s.substring( pos, pos+m.start() ) );
				}
				String x = m.group();
				builder.append( x );
				pos += m.end();
			}
			else
			{
				escape( builder, s.substring( pos ) );
				break;
			}
		}
		
		builder.append( "\"" );

		return builder.toString();
	}
}
