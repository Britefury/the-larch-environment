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
//import BritefuryJ.DocPresent.ElementTree.WhitespaceElement;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class VerticalInlineListViewLayout extends IndentedListViewLayout
{
	private VBoxStyleSheet styleSheet;
	private ParagraphStyleSheet lineParagraphStyleSheet;
	private TrailingSeparator trailingSeparator;
	
	
	public VerticalInlineListViewLayout(VBoxStyleSheet styleSheet, ParagraphStyleSheet lineParagraphStyleSheet, float indentation, TrailingSeparator trailingSeparator)
	{
		super( indentation );
		this.styleSheet = styleSheet;
		this.lineParagraphStyleSheet = lineParagraphStyleSheet;
		this.trailingSeparator = trailingSeparator;
	}
	
	
	private Element createLineParagraph(Element child, ElementFactory separator)
	{
		if ( separator != null )
		{
			ParagraphElement paragraph = new ParagraphElement( lineParagraphStyleSheet );
			paragraph.setChildren( Arrays.asList( new Element[] { child, separator.createElement() } ) );
			return paragraph;
		}
		else
		{
			return child;
		}
	}
	

	public Element createListElement(List<Element> children, ElementFactory beginDelim, ElementFactory endDelim, ElementFactory separator)
	{
		if ( children.size() <= 1 )
		{
			// Paragraph with contents: [ beginDelim ] + children + [ endDelim ]
			ParagraphElement paragraph = new ParagraphElement( lineParagraphStyleSheet );
			ArrayList<Element> childElems = new ArrayList<Element>();
			if ( beginDelim != null )
			{
				childElems.add( beginDelim.createElement() );
			}
			
			childElems.addAll( children );
			
			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( separator.createElement() );
			}
			
			if ( endDelim != null )
			{
				childElems.add( endDelim.createElement() );
			}
			
			paragraph.setChildren( childElems );
			
			return paragraph;
		}
		else
		{
			// First line
			Element first = null;
			if ( beginDelim != null  ||  separator != null )
			{
				first = new ParagraphElement( lineParagraphStyleSheet );
				ArrayList<Element> firstChildElems = new ArrayList<Element>();
				firstChildElems.ensureCapacity( 3 );
				if ( beginDelim != null )
				{
					firstChildElems.add( beginDelim.createElement() );
				}
				firstChildElems.add( children.get( 0 ) );
				if ( separator != null )
				{
					firstChildElems.add( separator.createElement() );
				}
			}
			else
			{
				first = children.get( 0 );
			}
			
			
			// Middle lines
			ArrayList<Element> childElems = new ArrayList<Element>();
			childElems.ensureCapacity( children.size() );
			for (int i = 1; i < children.size() - 1; i++)
			{
				childElems.add( createLineParagraph( children.get( i ), separator ) );
			}
			
			// Last line
			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( children.get( children.size() - 1 ), separator ) );
			}
			else
			{
				childElems.add( createLineParagraph( children.get( children.size() - 1 ), null ) );
			}
			
			VBoxElement middleVBox = new VBoxElement( styleSheet );
			middleVBox.setChildren( childElems );
			Element indent = indent( middleVBox );
			
			
			VBoxElement mainVBox = new VBoxElement( styleSheet );
			if ( endDelim != null )
			{
				mainVBox.setChildren( Arrays.asList( new Element[] { first, indent, endDelim.createElement() } ) );
			}
			else
			{
				mainVBox.setChildren( Arrays.asList( new Element[] { first, indent } ) );
			}
			
			return mainVBox;
		}
	}
}
