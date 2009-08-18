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
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;

public class VerticalListViewLayout extends IndentedListViewLayout
{
	private ElementStyleSheet vboxStyleSheet, paraStyleSheet;
	private TrailingSeparator trailingSeparator;
	
	
	public VerticalListViewLayout(ElementStyleSheet vboxStyleSheet, ElementStyleSheet paraStyleSheet, float indentation, TrailingSeparator trailingSeparator)
	{
		super( indentation );
		this.vboxStyleSheet = vboxStyleSheet;
		this.paraStyleSheet = paraStyleSheet;
		this.trailingSeparator = trailingSeparator;
	}
	
	
	private DPWidget createLineParagraph(int index, DPWidget child, SeparatorElementFactory separator)
	{
		if ( separator != null )
		{
			DPParagraph paragraph = new DPParagraph( paraStyleSheet );
			//paragraph.setChildren( Arrays.asList( new Element[] { child, separator.createElement(), new WhitespaceElement( "\n" ) } ) );
			paragraph.setChildren( Arrays.asList( new DPWidget[] { child, separator.createElement( index, child ) } ) );
			return paragraph;
		}
		else
		{
			return child;
		}
	}
	

	public DPWidget createListElement(List<DPWidget> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator)
	{
		DPVBox vbox = new DPVBox( vboxStyleSheet );
		
		ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
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
		
		DPWidget indented = indent( vbox );
		
		
		if ( beginDelim != null  ||  endDelim != null )
		{
			DPVBox outerVBox = new DPVBox( vboxStyleSheet );
			
			ArrayList<DPWidget> outerChildElems = new ArrayList<DPWidget>();
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
