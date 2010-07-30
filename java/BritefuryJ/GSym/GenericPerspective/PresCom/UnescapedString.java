//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.PresCom;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.LineBreak;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

abstract class UnescapedString extends Pres
{
	private String value;
	
	
	public UnescapedString(String value)
	{
		this.value = value;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet2 escapeStyle = style.get( GenericStyle.stringEscapeStyle, StyleSheet2.class );
		StyleSheet2 contentStyle = style.get( GenericStyle.stringContentStyle, StyleSheet2.class );

		ArrayList<Object> elements = new ArrayList<Object>();
		// Break the string up into escaped and not escaped items
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < value.length(); i++)
		{
			char c = value.charAt( i );
			
			Pres escapeItem = null;

			// Process a list of known escape sequences
			if ( c == '\r' )
			{
				escapeItem = escapeStyle.applyTo( new StaticText( "\\r" ) );
			}
			else if ( c == '\t' )
			{
				escapeItem = escapeStyle.applyTo( new StaticText( "\\t" ) );
			}
			else if ( c == '\n' )
			{
				escapeItem = escapeStyle.applyTo( new StaticText( "\\n" ) );
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
