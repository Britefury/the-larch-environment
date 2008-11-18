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
import BritefuryJ.DocPresent.ElementTree.LineBreakElement;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.PyElementFactory;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;

public class ParagraphListViewLayout extends ListViewLayout
{
	private ParagraphStyleSheet styleSheet;
	private ElementFactory spacingFactory;
	private int lineBreakPriority;
	private TrailingSeparator trailingSeparator;
	
	
	public ParagraphListViewLayout(ParagraphStyleSheet styleSheet, ElementFactory spacingFactory, int lineBreakPriority, TrailingSeparator trailingSeparator)
	{
		this.styleSheet = styleSheet;
		this.spacingFactory = spacingFactory;
		this.lineBreakPriority = lineBreakPriority;
		this.trailingSeparator = trailingSeparator;
	}
	
	public ParagraphListViewLayout(ParagraphStyleSheet styleSheet, PyObject spacingFactory, int lineBreakPriority, TrailingSeparator trailingSeparator)
	{
		this( styleSheet, PyElementFactory.pyToElementFactory( spacingFactory ), lineBreakPriority, trailingSeparator );
	}
	
	
	public Element createListElement(List<Element> children, ElementFactory beginDelim, ElementFactory endDelim, ElementFactory separator)
	{
		ParagraphElement paragraph = new ParagraphElement( styleSheet );
		
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
				childElems.add( children.get( i ) );
				if ( separator != null )
				{
					childElems.add( separator.createElement() );
				}
				LineBreakElement lineBreak = new LineBreakElement( lineBreakPriority );
				if ( spacingFactory != null )
				{
					lineBreak.setChild( spacingFactory.createElement() );
				}
				childElems.add( lineBreak );
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
		
		paragraph.setChildren( childElems );
		
		return paragraph;
	}
}
