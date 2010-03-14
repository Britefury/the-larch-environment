//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ListView;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class SpanListViewLayoutStyleSheet extends ListViewLayoutStyleSheet
{
	private static class SpanListViewLayoutParams
	{
		private boolean bAddLineBreaks, bAddParagraphIndentMarkers, bAddLineBreakCost;
		
		private SpanListViewLayoutParams(boolean bAddLineBreaks, boolean bAddParagraphIndentMarkers, boolean bAddLineBreakCost)
		{
			this.bAddLineBreaks = bAddLineBreaks;
			this.bAddParagraphIndentMarkers = bAddParagraphIndentMarkers;
			this.bAddLineBreakCost = bAddLineBreakCost;
		}
	}

	
	
	private SpanListViewLayoutParams layoutParams = null;
	
	public static SpanListViewLayoutStyleSheet instance = new SpanListViewLayoutStyleSheet();
	
	
	
	public SpanListViewLayoutStyleSheet()
	{
		super();
		
		initAttr( "addLineBreaks", true );
		initAttr( "addParagraphIndentMarkers", false );
		initAttr( "addLineBreakCost", false );
	}
	

	protected StyleSheet newInstance()
	{
		return new SpanListViewLayoutStyleSheet();
	}
	
	
	
	//
	// GENERAL
	//
	
	public SpanListViewLayoutStyleSheet withAddLineBreaks(boolean addLineBreaks)
	{
		return (SpanListViewLayoutStyleSheet)withAttr( "addLineBreaks", addLineBreaks );
	}
	
	public SpanListViewLayoutStyleSheet withAddParagraphIndentMarkers(boolean addParagraphIndentMarkers)
	{
		return (SpanListViewLayoutStyleSheet)withAttr( "addParagraphIndentMarkers", addParagraphIndentMarkers );
	}

	public SpanListViewLayoutStyleSheet withAddLineBreakCost(boolean addLineBreakCost)
	{
		return (SpanListViewLayoutStyleSheet)withAttr( "addLineBreakCost", addLineBreakCost );
	}


	
	//
	// PARAMS
	//
	private SpanListViewLayoutParams getLayoutParams()
	{
		if ( layoutParams == null )
		{
			layoutParams = new SpanListViewLayoutParams(
					getNonNull( "addLineBreaks", Boolean.class, true ),
					getNonNull( "addParagraphIndentMarkers", Boolean.class, false ),
					getNonNull( "addLineBreakCost", Boolean.class, false ) );
		}
		return layoutParams;
	}

	
	public DPElement createListElement(List<DPElement> children, PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
			ElementFactory spacing, TrailingSeparator trailingSeparator)
	{
		SpanListViewLayoutParams params = getLayoutParams();
		
		ArrayList<DPElement> childElems = new ArrayList<DPElement>();
		childElems.ensureCapacity( children.size() + 2 );
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim.createElement( primitiveStyle ) );
		}
		
		if ( params.bAddParagraphIndentMarkers )
		{
			DPParagraphIndentMarker indent = new DPParagraphIndentMarker( );
			childElems.add( indent );
		}
		
		if ( children.size() > 0 )
		{
			for (int i = 0; i < children.size() - 1; i++)
			{
				DPElement child = children.get( i );
				childElems.add( child );
				if ( separator != null )
				{
					childElems.add( separator.createElement( primitiveStyle, i, child ) );
				}
				if ( spacing != null )
				{
					childElems.add( spacing.createElement( primitiveStyle ) );
				}
				if ( params.bAddLineBreaks )
				{
					childElems.add( primitiveStyle.lineBreak() );
				}
			}

			DPElement lastChild = children.get( children.size() - 1 );
			childElems.add( lastChild );
			
			if ( ListViewStyleSheet.trailingSeparatorRequired( children, trailingSeparator ) )
			{
				if ( separator != null )
				{
					childElems.add( separator.createElement( primitiveStyle, children.size() - 1, lastChild ) );
				}
			}
		}

		if ( params.bAddParagraphIndentMarkers )
		{
			DPParagraphDedentMarker dedent = new DPParagraphDedentMarker( );
			childElems.add( dedent );
		}
		
		if ( endDelim != null )
		{
			childElems.add( endDelim.createElement( primitiveStyle ) );
		}
		
		if ( params.bAddLineBreakCost )
		{
			return primitiveStyle.lineBreakCostSpan( childElems );
		}
		else
		{
			return primitiveStyle.span( childElems );
		}
	}
}
