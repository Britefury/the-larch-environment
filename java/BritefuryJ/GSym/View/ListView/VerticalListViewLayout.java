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
import BritefuryJ.DocPresent.ElementTree.WhitespaceElement;
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
	
	
	private Element createLineParagraph(Element child)
	{
		ParagraphElement paragraph = new ParagraphElement( lineParagraphStyleSheet );
		paragraph.setChildren( Arrays.asList( new Element[] { child, new WhitespaceElement( "\n" ) } ) );
		return paragraph;
	}
	
	private Element createLineParagraph(Element child, ElementFactory separator)
	{
		if ( separator != null )
		{
			ParagraphElement paragraph = new ParagraphElement( lineParagraphStyleSheet );
			paragraph.setChildren( Arrays.asList( new Element[] { child, separator.createElement(), new WhitespaceElement( "\n" ) } ) );
			return paragraph;
		}
		else
		{
			return createLineParagraph( child );
		}
	}
	

	public Element layoutChildren(List<Element> children, ElementFactory beginDelim, ElementFactory endDelim, ElementFactory separator)
	{
		VBoxElement vbox = new VBoxElement( styleSheet );
		
		ArrayList<Element> childElems = new ArrayList<Element>();
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				childElems.add( createLineParagraph( children.get( i ), separator ) );
			}

			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( children.get( children.size() - 1 ), separator ) );
			}
			else
			{
				childElems.add( createLineParagraph( children.get( children.size() - 1 ) ) );
			}
		}

		vbox.setChildren( childElems );
		
		Element indented = indent( vbox );
		
		
		if ( beginDelim != null  ||  endDelim != null )
		{
			VBoxElement outerVBox = new VBoxElement( styleSheet );
			
			ArrayList<Element> outerChildElems = new ArrayList<Element>();
			
			if ( beginDelim != null )
			{
				outerChildElems.add( createLineParagraph( beginDelim.createElement() ) );
			}
			
			outerChildElems.add( indented );
			
			if ( endDelim != null )
			{
				outerChildElems.add( createLineParagraph( endDelim.createElement() ) );
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
