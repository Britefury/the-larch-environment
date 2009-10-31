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
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.PyElementFactory;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;

public class ParagraphListViewLayout extends ListViewLayout
{
	private ParagraphStyleSheet styleSheet;
	private ElementFactory spacingFactory;
	boolean bAddParagraphIndentMarkers;
	private TrailingSeparator trailingSeparator;
	
	
	public ParagraphListViewLayout(ParagraphStyleSheet styleSheet, ElementFactory spacingFactory, boolean bAddParagraphIndentMarkers, TrailingSeparator trailingSeparator)
	{
		this.styleSheet = styleSheet;
		this.spacingFactory = spacingFactory;
		this.bAddParagraphIndentMarkers = bAddParagraphIndentMarkers;
		this.trailingSeparator = trailingSeparator;
	}
	
	public ParagraphListViewLayout(ParagraphStyleSheet styleSheet, PyObject spacingFactory, boolean bAddParagraphIndentMarkers, TrailingSeparator trailingSeparator)
	{
		this( styleSheet, PyElementFactory.pyToElementFactory( spacingFactory ), bAddParagraphIndentMarkers, trailingSeparator );
	}
	
	
	public DPWidget createListElement(ElementContext ctx, List<DPWidget> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator)
	{
		DPParagraph paragraph = new DPParagraph( ctx, styleSheet );
		
		ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
		childElems.ensureCapacity( children.size() + 2 );
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim.createElement( ctx ) );
		}
		
		if ( bAddParagraphIndentMarkers )
		{
			DPParagraphIndentMarker indent = new DPParagraphIndentMarker( ctx );
			childElems.add( indent );
		}
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				DPWidget child = children.get( i );
				childElems.add( child );
				if ( separator != null )
				{
					childElems.add( separator.createElement( ctx, i, child ) );
				}
				DPLineBreak lineBreak = new DPLineBreak( ctx );
				if ( spacingFactory != null )
				{
					lineBreak.setChild( spacingFactory.createElement( ctx ) );
				}
				childElems.add( lineBreak );
			}

			DPWidget lastChild = children.get( children.size() - 1 );
			childElems.add( lastChild );
			
			if ( trailingSeparatorRequired( children, trailingSeparator ) )
			{
				if ( separator != null )
				{
					childElems.add( separator.createElement( ctx, children.size() - 1, lastChild ) );
				}
			}
		}

		if ( bAddParagraphIndentMarkers )
		{
			DPParagraphDedentMarker dedent = new DPParagraphDedentMarker( ctx );
			childElems.add( dedent );
		}
		
		if ( endDelim != null )
		{
			childElems.add( endDelim.createElement( ctx ) );
		}
		
		paragraph.setChildren( childElems );
		
		return paragraph;
	}
}
