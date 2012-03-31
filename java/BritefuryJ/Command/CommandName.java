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
import BritefuryJ.Graphics.SolidBorder;
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

	public Pres autocompleteVisual(String text)
	{
		String name = getName();
		int start = name.toLowerCase().indexOf( text );
		int end = start + text.length();
		
		ArrayList<Pres> elements = new ArrayList<Pres>();
		int pos = 0;
		for (int x: getCharIndices())
		{
			addSegment( elements, name, pos, x, start, end );
			
			addElement( elements, name, x, x + 1, ( x >= start  &&  x < end )  ?  autocompleteHighlightMnemonicStyle  :  autocompleteMnemonicStyle );
			pos = x + 1;
		}
		addSegment( elements, name, pos, name.length(), start, end );
		
		/*Pres before = autocompleteStyle.applyTo( new Label( name.substring( 0, index ) ) );
		Pres highlight = autocompleteHighlightStyle.applyTo( new Label( name.substring( index, end ) ) );
		Pres after = autocompleteStyle.applyTo( new Label( name.substring( end ) ) );*/
		Pres auto = new Row( elements.toArray( new Pres[0] ) );
		return autocompleteBorder.surround( auto );
	}
	
	private void addSegment(List<Pres> elements, String name, int pos, int x, int start, int end)
	{
		if ( pos < x )
		{
			if ( start >= pos  &&  start < x )
			{
				if ( start > pos )
				{
					addElement( elements, name, pos, start, autocompleteStyle );
				}
				addElement( elements, name, start, x, autocompleteHighlightStyle );
			}
			else if ( end >= pos  &&  end < x )
			{
				if ( end > pos )
				{
					addElement( elements, name, pos, end, autocompleteHighlightStyle );
				}
				addElement( elements, name, end, x, autocompleteStyle );
			}
			else
			{
				if ( pos > start  &&  pos < end )
				{
					addElement( elements, name, pos, x, autocompleteHighlightStyle );
				}
				else
				{
					addElement( elements, name, pos, x, autocompleteStyle );
				}
			}
		}
	}

	private void addElement(List<Pres> elements, String name, int start, int end, StyleSheet style)
	{
		elements.add( style.applyTo( new Label( name.substring( start, end ) ) ) );
	}


	private static final AbstractBorder cmdBorder = Command.cmdBorder( new Color( 0.0f, 0.5f, 0.0f, 0.8f ), new Color( 0.25f, 0.5f, 0.25f, 0.5f ) );
	private static final AbstractBorder autocompleteBorder = new SolidBorder( 1.0, 1.0, 4.0, 4.0, new Color( 1.0f, 1.0f, 1.0f, 0.35f ), new Color( 1.0f, 1.0f, 1.0f, 0.15f ) );
	private static final StyleSheet autocompleteStyle = StyleSheet.style( Primitive.foreground.as( new Color( 1.0f, 1.0f, 1.0f, 0.75f ) ) );
	private static final StyleSheet autocompleteHighlightStyle = StyleSheet.style( Primitive.foreground.as( new Color( 1.0f, 1.0f, 1.0f, 1.0f ) ), Primitive.fontBold.as( true ) );
	private static final StyleSheet autocompleteMnemonicStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.5f, 1.0f, 0.5f, 0.75f ) ) );
	private static final StyleSheet autocompleteHighlightMnemonicStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.75f, 1.0f, 0.75f, 1.0f ) ), Primitive.fontBold.as( true ) );
}
