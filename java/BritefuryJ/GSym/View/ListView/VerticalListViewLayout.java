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

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.ElementFactory;
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
	
	
	private DPWidget createLineParagraph(ElementContext ctx, int index, DPWidget child, SeparatorElementFactory separator)
	{
		if ( separator != null )
		{
			DPParagraph paragraph = new DPParagraph( ctx, lineParagraphStyleSheet );
			//paragraph.setChildren( Arrays.asList( new Element[] { child, separator.createElement(), new WhitespaceElement( "\n" ) } ) );
			paragraph.setChildren( Arrays.asList( new DPWidget[] { child, separator.createElement( ctx, index, child ) } ) );
			return paragraph;
		}
		else
		{
			return child;
		}
	}
	

	public DPWidget createListElement(ElementContext ctx, List<DPWidget> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator)
	{
		DPVBox vbox = new DPVBox( ctx, styleSheet );
		
		ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
		childElems.ensureCapacity( children.size() );
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				childElems.add( createLineParagraph( ctx, i, children.get( i ), separator ) );
			}

			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( ctx, children.size() - 1, children.get( children.size() - 1 ), separator ) );
			}
			else
			{
				childElems.add( children.get( children.size() - 1 ) );
			}
		}

		vbox.setChildren( childElems );
		
		DPWidget indented = indent( ctx, vbox );
		
		
		if ( beginDelim != null  ||  endDelim != null )
		{
			DPVBox outerVBox = new DPVBox( ctx, styleSheet );
			
			ArrayList<DPWidget> outerChildElems = new ArrayList<DPWidget>();
			outerChildElems.ensureCapacity( 3 );
			
			if ( beginDelim != null )
			{
				outerChildElems.add( beginDelim.createElement( ctx ) );
			}
			
			outerChildElems.add( indented );
			
			if ( endDelim != null )
			{
				outerChildElems.add(  endDelim.createElement( ctx ) );
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
