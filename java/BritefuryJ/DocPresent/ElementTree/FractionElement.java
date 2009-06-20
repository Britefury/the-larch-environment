//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class FractionElement extends BranchElement
{
	public static class BarElement extends EditableEntryLeafElement
	{
		public BarElement(String textRepresentation)
		{
			this( FractionStyleSheet.BarStyleSheet.defaultStyleSheet, textRepresentation );
		}

		public BarElement(FractionStyleSheet.BarStyleSheet styleSheet, String textRepresentation)
		{
			super( new DPFraction.DPFractionBar( styleSheet, textRepresentation ) );
		}



		public DPFraction.DPFractionBar getWidget()
		{
			return (DPFraction.DPFractionBar)widget;
		}
	}

	
	
	
	protected Element numeratorChild, barChild, denominatorChild;
	protected SegmentElement numSegment, denomSegment;
	protected ParagraphElement numPara, denomPara;
	
	
	
	public FractionElement()
	{
		this( FractionStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, "/" );
	}
	
	public FractionElement(String barContent)
	{
		this( FractionStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, barContent );
	}
	
	public FractionElement(FractionStyleSheet styleSheet)
	{
		this( styleSheet, TextStyleSheet.defaultStyleSheet, "/" );
	}
	
	public FractionElement(FractionStyleSheet styleSheet, String barContent)
	{
		this( styleSheet, TextStyleSheet.defaultStyleSheet, barContent );
	}

	public FractionElement(FractionStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet, String barContent)
	{
		super( new DPFraction( styleSheet ) );
		
		numSegment = new SegmentElement( segmentTextStyleSheet, true, true );
		denomSegment = new SegmentElement( segmentTextStyleSheet, true, true );
		
		numPara = new ParagraphElement();
		denomPara = new ParagraphElement();
		
		numPara.setChildren( Arrays.asList( new Element[] { numSegment } ) );
		denomPara.setChildren( Arrays.asList( new Element[] { denomSegment } ) );

		getWidget().setNumeratorChild( numPara.getWidget() );
		numPara.setParent( this );
		numPara.setElementTree( tree );
		
		getWidget().setDenominatorChild( denomPara.getWidget() );
		denomPara.setParent( this );
		denomPara.setElementTree( tree );
		
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
			xs.add( numPara );
		}
		
		if ( barChild != null )
		{
			xs.add( barChild );
		}
		
		if ( denomSegment != null )
		{
			xs.add( denomPara );
		}
		
		return xs;
	}
}
