//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;

public class CommandName extends CommandMnemonic
{
	public CommandName(String charSequence, String name)
	{
		super( charSequence, name );
	}

	public CommandName(String annotatedName)
	{
		super( annotatedName );
	}




	public Pres executableVisual()
	{
		return cmdBorder.surround( completePres );
	}

	public Pres autocompleteVisual(String autocompleteText)
	{
		String name = getName().toLowerCase();
		int highlighStart;
		if ( name.startsWith( autocompleteText ) )
		{
			highlighStart = 0;
		}
		else
		{
			highlighStart = name.indexOf( " " + autocompleteText ) + 1;
		}
		int highlightEnd = highlighStart + autocompleteText.length();
		
		ArrayList<Pres> elements = new ArrayList<Pres>();
		int pos = 0;
		for (int x: getCharIndices())
		{
			addSegment( elements, name, pos, x, highlighStart, highlightEnd );
			
			addElement( elements, name, x, x + 1, ( x >= highlighStart  &&  x < highlightEnd )  ?  autocompleteHighlightMnemonicStyle  :  autocompleteMnemonicStyle );
			pos = x + 1;
		}
		addSegment( elements, name, pos, name.length(), highlighStart, highlightEnd );
		
		return new Row( elements.toArray( new Pres[0] ) );
	}
	
	private void addSegment(List<Pres> elements, String name, int segStart, int segEnd, int highlightStart, int highlightEnd)
	{
		if ( segStart < segEnd )
		{
			// The length of the segment is > 0
			
			if ( highlightEnd <= segStart )
			{
				// Highlight before segment; no overlap
				addElement( elements, name, segStart, segEnd, autocompleteStyle );
			}
			else if ( highlightStart >= segEnd )
			{
				// Highlight after segment; no overlap
				addElement( elements, name, segStart, segEnd, autocompleteStyle );
			}
			else
			{
				// There is some overlap
				
				if ( highlightStart > segStart )
				{
					// There is some non-highlighted text before the highlight region start
					addElement( elements, name, segStart, highlightStart, autocompleteStyle );
				}
				
				// Highlighted text
				addElement( elements, name, Math.max( segStart, highlightStart ), Math.min( segEnd, highlightEnd ), autocompleteHighlightStyle );
				
				if ( highlightEnd < segEnd )
				{
					// There is some non-highlighted text after the highlight region end
					addElement( elements, name, highlightEnd, segEnd, autocompleteStyle );
				}
			}
		}
	}

	private void addElement(List<Pres> elements, String name, int start, int end, StyleSheet style)
	{
		elements.add( style.applyTo( new Label( name.substring( start, end ) ) ) );
	}


	private static final AbstractBorder cmdBorder = Command.cmdBorder( new Color( 0.0f, 0.5f, 0.0f, 0.8f ), new Color( 0.25f, 0.5f, 0.25f, 0.5f ) );
	private static final StyleSheet autocompleteStyle = StyleSheet.style( Primitive.foreground.as( new Color( 1.0f, 1.0f, 1.0f, 0.75f ) ) );
	private static final StyleSheet autocompleteHighlightStyle = StyleSheet.style( Primitive.foreground.as( new Color( 1.0f, 1.0f, 1.0f, 1.0f ) ), Primitive.fontBold.as( true ) );
	private static final StyleSheet autocompleteMnemonicStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.5f, 1.0f, 0.5f, 0.75f ) ) );
	private static final StyleSheet autocompleteHighlightMnemonicStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.75f, 1.0f, 0.75f, 1.0f ) ), Primitive.fontBold.as( true ) );
}
