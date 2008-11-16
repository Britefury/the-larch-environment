//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View.ListView;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementFactory;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;

public class HorizontalListViewLayout extends ListViewLayout
{
	private HBoxStyleSheet styleSheet;
	private ElementFactory spacingFactory;
	private TrailingSeparator trailingSeparator;
	
	
	public HorizontalListViewLayout(HBoxStyleSheet styleSheet, ElementFactory spacingFactory, int lineBreakPriority, TrailingSeparator trailingSeparator)
	{
		this.styleSheet = styleSheet;
		this.spacingFactory = spacingFactory;
		this.trailingSeparator = trailingSeparator;
	}
	
	
	public Element layoutChildren(List<Element> children, ElementFactory beginDelim, ElementFactory endDelim, ElementFactory separator)
	{
		HBoxElement hbox = new HBoxElement( styleSheet );
		
		ArrayList<Element> childElems = new ArrayList<Element>();
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim.createElement() );
		}
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				childElems.add( children.get( i ) );
				if ( separator != null )
				{
					childElems.add( separator.createElement() );
				}
				if ( spacingFactory != null )
				{
					childElems.add( spacingFactory.createElement() );
				}
			}

			childElems.add( children.get( children.size() - 1 ) );
			
			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				if ( separator != null )
				{
					childElems.add( separator.createElement() );
				}
			}
		}

		if ( endDelim != null )
		{
			childElems.add( endDelim.createElement() );
		}
		
		hbox.setChildren( childElems );
		
		return hbox;
	}
}
