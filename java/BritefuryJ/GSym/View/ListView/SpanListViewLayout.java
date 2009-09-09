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

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.PyElementFactory;

public class SpanListViewLayout extends ListViewLayout
{
	private ElementFactory spacingFactory;
	private TrailingSeparator trailingSeparator;
	boolean bAddLineBreaks, bAddParagraphIndentMarkers;
	int lineBreakCost;
	
	
	public SpanListViewLayout(ElementFactory spacingFactory, boolean bAddLineBreaks, boolean bAddParagraphIndentMarkers, TrailingSeparator trailingSeparator)
	{
		this.spacingFactory = spacingFactory;
		this.bAddLineBreaks = bAddLineBreaks;
		this.bAddParagraphIndentMarkers = bAddParagraphIndentMarkers;
		this.trailingSeparator = trailingSeparator;
	}
	
	public SpanListViewLayout(PyObject spacingFactory, boolean bAddLineBreaks, boolean bAddParagraphIndentMarkers, TrailingSeparator trailingSeparator)
	{
		this( PyElementFactory.pyToElementFactory( spacingFactory ), bAddLineBreaks, bAddParagraphIndentMarkers, trailingSeparator );
	}
	
	
	public DPWidget createListElement(List<DPWidget> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator)
	{
		DPSpan span = new DPSpan();
		
		ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
		childElems.ensureCapacity( children.size() + 2 );
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim.createElement() );
		}
		
		if ( bAddParagraphIndentMarkers )
		{
			childElems.add( new DPParagraphIndentMarker() );
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
				if ( bAddLineBreaks )
				{
					DPLineBreak lineBreak = new DPLineBreak();
					if ( spacingFactory != null )
					{
						lineBreak.setChild( spacingFactory.createElement() );
					}
					childElems.add( lineBreak );
				}
				else
				{
					if ( spacingFactory != null )
					{
						childElems.add( spacingFactory.createElement() );
					}
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

		if ( bAddParagraphIndentMarkers )
		{
			childElems.add( new DPParagraphDedentMarker() );
		}
		
		if ( endDelim != null )
		{
			childElems.add( endDelim.createElement() );
		}
		
		span.setChildren( childElems );
		
		return span;
	}
}
