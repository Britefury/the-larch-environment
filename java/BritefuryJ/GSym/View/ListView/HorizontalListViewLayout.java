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

import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementFactory;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.ElementTree.PyElementFactory;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;

public class HorizontalListViewLayout extends ListViewLayout
{
	private HBoxStyleSheet styleSheet;
	private ElementFactory spacingFactory;
	private TrailingSeparator trailingSeparator;
	
	
	public HorizontalListViewLayout(HBoxStyleSheet styleSheet, ElementFactory spacingFactory, TrailingSeparator trailingSeparator)
	{
		this.styleSheet = styleSheet;
		this.spacingFactory = spacingFactory;
		this.trailingSeparator = trailingSeparator;
	}
	
	public HorizontalListViewLayout(HBoxStyleSheet styleSheet, PyObject spacingFactory, TrailingSeparator trailingSeparator)
	{
		this( styleSheet, PyElementFactory.pyToElementFactory( spacingFactory ), trailingSeparator );
	}

		
		
	public Element createListElement(List<Element> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator)
	{
		HBoxElement hbox = new HBoxElement( styleSheet );
		
		ArrayList<Element> childElems = new ArrayList<Element>();
		childElems.ensureCapacity( children.size() + 2 );
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim.createElement() );
		}
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				Element child = children.get( i );
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

			Element lastChild = children.get( children.size() - 1 );
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
