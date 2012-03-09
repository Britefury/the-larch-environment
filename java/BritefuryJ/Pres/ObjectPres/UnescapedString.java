//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.ObjectPres;

import java.util.ArrayList;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class UnescapedString extends Pres
{
	private String value;
	
	
	public UnescapedString(String value)
	{
		this.value = value;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet escapeStyle = style.get( ObjectPresStyle.stringEscapeStyle, StyleSheet.class );
		StyleSheet contentStyle = style.get( ObjectPresStyle.stringContentStyle, StyleSheet.class );

		ArrayList<Object> elements = new ArrayList<Object>();
		// Break the string up into escaped and not escaped items
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < value.length(); i++)
		{
			char c = value.charAt( i );
			
			Pres escapeItem = null;

			// Process a list of known escape sequences
			switch ( c )
			{
				case '\r':
					escapeItem = escapeStyle.applyTo( new StaticText( "\\r" ) );
					break;
				case '\t':
					escapeItem = escapeStyle.applyTo( new StaticText( "\\t" ) );
					break;
				case '\n':
					escapeItem = escapeStyle.applyTo( new StaticText( "\\n" ) );
					break;
				case '\b':
					escapeItem = escapeStyle.applyTo( new StaticText( "\\b" ) );
					break;
				case '\f':
					escapeItem = escapeStyle.applyTo( new StaticText( "\\f" ) );
					break;
				default:
					break;
			}
			
			if ( escapeItem != null )
			{
				// We have an escape sequence item
				// First, add any non-escaped content to the element list, then add the escape item
				if ( builder.length() > 0 )
				{
					elements.add( new StaticText( builder.toString() ) );
					elements.add( new LineBreak() );
					builder = new StringBuilder();
				}
				elements.add( escapeItem );
				elements.add( new LineBreak() );
			}
			else
			{
				// Non-escaped character - add to the string
				builder.append( c );
			}
		}
		
		if ( builder.length() > 0 )
		{
			// Non-escaped content remains - create a text element and add
			elements.add( new StaticText( builder.toString() ) );
		}
		
		return contentStyle.applyTo( createContainer( elements ) ).present( ctx, style );
	}
	
	
	protected abstract Pres createContainer(ArrayList<Object> contents);
}
