//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.python.core.PyString;
import org.python.core.PyUnicode;



/*
 * gSym document model IO
 *
 *
 *Uses basic S-expressions

 *lists are (...) as normal
 *tokens inside are:
 *	atom:   A-Z a-z 0.9 _+-* /%^&|!$@.,<>=[]~
 *	quoted string
 *	double quoted string
 *	another list
 *
 */

public class DMIOWrite
{
	public static class InvalidDataTypeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public static String unquotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]~";
	public static String quotedStringPunctuationChars = "+-*/%^&|!$@.,<>=[]~'()` ";
	public static Pattern unquotedString = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( unquotedStringPunctuationChars ) + "]+" );
	public static Pattern quotedStringContents = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( quotedStringPunctuationChars ) + "]+" );

	
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
	
	public static void writeList(StringBuilder builder, List<Object> content) throws InvalidDataTypeException
	{
		builder.append( "[" );
		if ( content.size() > 0 )
		{
			for (Object v: content.subList( 0, content.size() - 1 ))
			{
				writeSX( builder, v );
				builder.append( " " );
			}
			writeSX( builder, content.get( content.size() - 1 ) );
		}
		builder.append( "]" );
	}
	
	
	@SuppressWarnings("unchecked")
	public static void writeSX(StringBuilder builder, Object content) throws InvalidDataTypeException
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
		else
		{
			System.out.println( "Content data type: " + content.getClass().getName() + ", content data: " + content.toString() );
			throw new InvalidDataTypeException();
		}
	}



	public static String writeSX(Object content) throws InvalidDataTypeException
	{
		StringBuilder builder = new StringBuilder();
		writeSX( builder, content );
		return builder.toString();
	}
}


