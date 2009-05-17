//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class FractionElement extends OrderedBranchElement
{
	public static class BarElement extends EditableEntryLeafElement
	{
		public BarElement(String textRepresentation)
		{
			this( FractionStyleSheet.BarStyleSheet.defaultStyleSheet, textRepresentation );
		}

		public BarElement(FractionStyleSheet.BarStyleSheet styleSheet, String textRepresentation)
		{
			super( new DPFraction.DPFractionBar( styleSheet ), textRepresentation );
		}



		public DPFraction.DPFractionBar getWidget()
		{
			return (DPFraction.DPFractionBar)widget;
		}
	}

	
	
	
	protected Element numeratorChild, barChild, denominatorChild;
	protected SegmentElement numSegment, denomSegment;
	
	
	
	public FractionElement()
	{
		this( FractionStyleSheet.defaultStyleSheet, ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, "/" );
	}
	
	public FractionElement(String barContent)
	{
		this( FractionStyleSheet.defaultStyleSheet, ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, barContent );
	}
	
	public FractionElement(FractionStyleSheet styleSheet)
	{
		this( styleSheet, ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, "/" );
	}
	
	public FractionElement(FractionStyleSheet styleSheet, String barContent)
	{
		this( styleSheet, ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, barContent );
	}

	public FractionElement(FractionStyleSheet styleSheet, ParagraphStyleSheet segmentParagraphStyleSheet, TextStyleSheet segmentTextStyleSheet, String barContent)
	{
		super( new DPFraction( styleSheet ) );
		
		numSegment = new SegmentElement( segmentParagraphStyleSheet, segmentTextStyleSheet, true, true );
		denomSegment = new SegmentElement( segmentParagraphStyleSheet, segmentTextStyleSheet, true, true );
		
		getWidget().setNumeratorChild( numSegment.getWidget() );
		numSegment.setParent( this );
		numSegment.setElementTree( tree );
		
		getWidget().setDenominatorChild( denomSegment.getWidget() );
		denomSegment.setParent( this );
		denomSegment.setElementTree( tree );
		
		setBarChild( new BarElement( styleSheet.getBarStyleSheet(), barContent ) );
	}



	public DPFraction getWidget()
	{
		return (DPFraction)widget;
	}



	public Element getNumeratorChild()
	{
		return numeratorChild;
	}
	
	public Element getBarChild()
	{
		return barChild;
	}
	
	public Element getDenominatorChild()
	{
		return denominatorChild;
	}
	
	
	public void setNumeratorChild(Element child)
	{
		Element existingChild = numeratorChild;
		if ( child != existingChild )
		{
			numSegment.setChild( child );
			numeratorChild = child;

			onChildListChanged();
		}
	}
	
	public void setBarChild(Element child)
	{
		Element existingChild = barChild;
		if ( child != existingChild )
		{
			if ( existingChild != null )
			{
				existingChild.setParent( null );
				existingChild.setElementTree( null );
			}
			
			barChild = child;
			DPWidget childWidget = null;
			if ( child != null )
			{
				childWidget = child.getWidget();
			}
			getWidget().setBarChild( childWidget );
			
			
			if ( child != null )
			{
				child.setParent( this );
				child.setElementTree( tree );
			}

			onChildListChanged();
		}
	}
	
	public void setDenominatorChild(Element child)
	{
		Element existingChild = denominatorChild;
		if ( child != existingChild )
		{
			denomSegment.setChild( child );
			denominatorChild = child;

			onChildListChanged();
		}
	}
	


	public List<Element> getChildren()
	{
		ArrayList<Element> xs = new ArrayList<Element>();
		
		if ( numSegment != null )
		{
			xs.add( numSegment );
		}
		
		if ( barChild != null )
		{
			xs.add( barChild );
		}
		
		if ( denomSegment != null )
		{
			xs.add( denomSegment );
		}
		
		return xs;
	}



	//
	// Text representation methods
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		StringBuilder builder = new StringBuilder();
		for (Element child: getChildren())
		{
			builder.append( child.getTextRepresentation() );
		}
		return builder.toString();
	}
}
