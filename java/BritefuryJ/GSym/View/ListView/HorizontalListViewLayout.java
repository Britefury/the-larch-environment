//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View.ListView;

import java.util.ArrayList;
import java.util.List;

import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.PyElementFactory;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;

public class HorizontalListViewLayout extends ListViewLayout
{
	private ElementStyleSheet styleSheet;
	private ElementFactory spacingFactory;
	private TrailingSeparator trailingSeparator;
	
	
	public HorizontalListViewLayout(ElementStyleSheet styleSheet, ElementFactory spacingFactory, TrailingSeparator trailingSeparator)
	{
		this.styleSheet = styleSheet;
		this.spacingFactory = spacingFactory;
		this.trailingSeparator = trailingSeparator;
	}
	
	public HorizontalListViewLayout(ElementStyleSheet styleSheet, PyObject spacingFactory, TrailingSeparator trailingSeparator)
	{
		this( styleSheet, PyElementFactory.pyToElementFactory( spacingFactory ), trailingSeparator );
	}

		
		
	public DPWidget createListElement(List<DPWidget> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator)
	{
		DPHBox hbox = new DPHBox( styleSheet );
		
		ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
		childElems.ensureCapacity( children.size() + 2 );
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim.createElement() );
		}
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				DPWidget child = children.get( i );
				childElems.add( child );
				if ( separator != null )
				{
					childElems.add( separator.createElement( i, child ) );
				}
				if ( spacingFactory != null )
				{
					childElems.add( spacingFactory.createElement() );
				}
			}

			DPWidget lastChild = children.get( children.size() - 1 );
			childElems.add( lastChild );
			
			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				if ( separator != null )
				{
					childElems.add( separator.createElement( children.size() - 1, lastChild ) );
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
