//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementFactory;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class VerticalListViewLayout extends IndentedListViewLayout
{
	private VBoxStyleSheet styleSheet;
	private ParagraphStyleSheet lineParagraphStyleSheet;
	private TrailingSeparator trailingSeparator;
	
	
	public VerticalListViewLayout(VBoxStyleSheet styleSheet, ParagraphStyleSheet lineParagraphStyleSheet, float indentation, TrailingSeparator trailingSeparator)
	{
		super( indentation );
		this.styleSheet = styleSheet;
		this.lineParagraphStyleSheet = lineParagraphStyleSheet;
		this.trailingSeparator = trailingSeparator;
	}
	
	
	private Element createLineParagraph(int index, Element child, SeparatorElementFactory separator)
	{
		if ( separator != null )
		{
			ParagraphElement paragraph = new ParagraphElement( lineParagraphStyleSheet );
			//paragraph.setChildren( Arrays.asList( new Element[] { child, separator.createElement(), new WhitespaceElement( "\n" ) } ) );
			paragraph.setChildren( Arrays.asList( new Element[] { child, separator.createElement( index, child ) } ) );
			return paragraph;
		}
		else
		{
			return child;
		}
	}
	

	public Element createListElement(List<Element> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator)
	{
		VBoxElement vbox = new VBoxElement( styleSheet );
		
		ArrayList<Element> childElems = new ArrayList<Element>();
		childElems.ensureCapacity( children.size() );
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				childElems.add( createLineParagraph( i, children.get( i ), separator ) );
			}

			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( children.size() - 1, children.get( children.size() - 1 ), separator ) );
			}
			else
			{
				childElems.add( children.get( children.size() - 1 ) );
			}
		}

		vbox.setChildren( childElems );
		
		Element indented = indent( vbox );
		
		
		if ( beginDelim != null  ||  endDelim != null )
		{
			VBoxElement outerVBox = new VBoxElement( styleSheet );
			
			ArrayList<Element> outerChildElems = new ArrayList<Element>();
			outerChildElems.ensureCapacity( 3 );
			
			if ( beginDelim != null )
			{
				outerChildElems.add( beginDelim.createElement() );
			}
			
			outerChildElems.add( indented );
			
			if ( endDelim != null )
			{
				outerChildElems.add(  endDelim.createElement() );
			}
			
			outerVBox.setChildren( outerChildElems );
			
			return outerVBox;
		}
		else
		{
			return indented;
		}
	}
}
