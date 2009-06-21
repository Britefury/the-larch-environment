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
		super( new DPFraction( styleSheet, segmentTextStyleSheet ) );
				
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
		numeratorChild = child;
		getWidget().setNumeratorChild( child.getWidget() );
	}
	
	public void setBarChild(Element child)
	{
		barChild = child;
		getWidget().setBarChild( child.getWidget() );
	}
	
	public void setDenominatorChild(Element child)
	{
		denominatorChild = child;
		getWidget().setDenominatorChild( child.getWidget() );
	}
	


	public List<Element> getChildren()
	{
		ArrayList<Element> xs = new ArrayList<Element>();
		
		if ( numeratorChild != null )
		{
			xs.add( numeratorChild );
		}
		
		if ( barChild != null )
		{
			xs.add( barChild );
		}
		
		if ( denominatorChild != null )
		{
			xs.add( denominatorChild );
		}
		
		return xs;
	}
}
