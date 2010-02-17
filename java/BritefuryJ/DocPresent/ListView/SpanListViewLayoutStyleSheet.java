//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ListView;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class SpanListViewLayoutStyleSheet extends ListViewLayoutStyleSheet
{
	private static class SpanListViewLayoutParams
	{
		private boolean bAddLineBreaks, bAddParagraphIndentMarkers;
		
		private SpanListViewLayoutParams(boolean bAddLineBreaks, boolean bAddParagraphIndentMarkers)
		{
			this.bAddLineBreaks = bAddLineBreaks;
			this.bAddParagraphIndentMarkers = bAddParagraphIndentMarkers;
		}
	}

	
	
	private SpanListViewLayoutParams layoutParams = null;
	
	public static SpanListViewLayoutStyleSheet instance = new SpanListViewLayoutStyleSheet();
	
	
	
	public SpanListViewLayoutStyleSheet()
	{
		super();
		
		initAttr( "addLineBreaks", true );
		initAttr( "addParagraphIndentMarkers", false );
	}
	
	protected SpanListViewLayoutStyleSheet(StyleSheet prototype)
	{
		super( prototype );
	}
	
	
	public Object clone()
	{
		return new SpanListViewLayoutStyleSheet( this );
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


	
	//
	// PARAMS
	//
	private SpanListViewLayoutParams getLayoutParams()
	{
		if ( layoutParams == null )
		{
			layoutParams = new SpanListViewLayoutParams(
					get( "addLineBreaks", Boolean.class, true ),
					get( "addParagraphIndentMarkers", Boolean.class, false ) );
		}
		return layoutParams;
	}

	
	public DPWidget createListElement(List<DPWidget> children, PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
			ElementFactory spacing, ListViewStyleSheet.TrailingSeparator trailingSeparator)
	{
		SpanListViewLayoutParams params = getLayoutParams();
		
		ArrayList<DPWidget> childElems = new ArrayList<DPWidget>();
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
				DPWidget child = children.get( i );
				childElems.add( child );
				if ( separator != null )
				{
					childElems.add( separator.createElement( primitiveStyle, i, child ) );
				}
				if ( params.bAddLineBreaks )
				{
					DPLineBreak lineBreak = new DPLineBreak( );
					if ( spacing != null )
					{
						lineBreak.setChild( spacing.createElement( primitiveStyle ) );
					}
					childElems.add( lineBreak );
				}
				else
				{
					if ( spacing != null )
					{
						childElems.add( spacing.createElement( primitiveStyle ) );
					}
				}
			}

			DPWidget lastChild = children.get( children.size() - 1 );
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
		
		return primitiveStyle.span( childElems );
	}
}
