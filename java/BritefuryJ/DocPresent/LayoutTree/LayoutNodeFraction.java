//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.FractionLayout;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.Math.Point2;

public class LayoutNodeFraction extends ArrangedLayoutNode
{
	public static class LayoutNodeFractionBar extends ContentLeafLayoutNode
	{
		public static double BAR_HEIGHT = 1.5;
		
		
		public LayoutNodeFractionBar(DPFraction.DPFractionBar element)
		{
			super( element );
		}


		protected void updateRequisitionX()
		{
			layoutReqBox.clearRequisitionX();
		}

		protected void updateRequisitionY()
		{
			layoutReqBox.setRequisitionY( BAR_HEIGHT, 0.0 );
		}
	}
	
	
	
	
	public static int NUMERATOR = 0;
	public static int BAR = 1;
	public static int DENOMINATOR = 2;

	public static int NUMCHILDREN = 3;

	
	public LayoutNodeFraction(DPFraction element)
	{
		super( element );
	}
	
	
	private static double getChildScale()
	{
		return DPFraction.getChildScale();
	}
	

	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPFraction frac = (DPFraction)element;
		double childScale = getChildScale();
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = frac.getWrappedChild( i );
			if ( i != BAR )
			{
				boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionX().scaledRequisition( childScale )  :  null;
			}
			else
			{
				boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionX()  :  null;
			}
		}

		FractionLayout.computeRequisitionX( layoutReqBox, boxes[NUMERATOR], boxes[BAR], boxes[DENOMINATOR], getHPadding(), getVSpacing(), getYOffset() );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPFraction frac = (DPFraction)element;
		double childScale = getChildScale();
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = frac.getWrappedChild( i );
			if ( i != BAR )
			{
				boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionY().scaledRequisition( childScale )  :  null;
			}
			else
			{
				boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionY()  :  null;
			}
		}

		FractionLayout.computeRequisitionY( layoutReqBox, boxes[NUMERATOR], boxes[BAR], boxes[DENOMINATOR], getHPadding(), getVSpacing(), getYOffset() );
	}
	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPFraction frac = (DPFraction)element;
		double childScale = getChildScale();
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		double prevChildWidths[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = frac.getWrappedChild( i );
			if ( i != BAR )
			{
				reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox().scaledRequisition( childScale )  :  null;
			}
			else
			{
				reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			}
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildWidths[i] = child != null  ?  allocBoxes[i].getAllocationX()  :  0.0;
		}
		

		FractionLayout.allocateX( layoutReqBox, reqBoxes[NUMERATOR], reqBoxes[BAR], reqBoxes[DENOMINATOR],
				getAllocationBox(), allocBoxes[NUMERATOR], allocBoxes[BAR], allocBoxes[DENOMINATOR], 
				getHPadding(), getVSpacing(), getYOffset() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = frac.getWrappedChild( i );
			if ( child != null )
			{
				if ( i != BAR )
				{
					allocBoxes[i].scaleAllocationX( 1.0 / childScale );
				}
				child.getLayoutNode().refreshAllocationX( prevChildWidths[i] );
			}
		}
	}

	
	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPFraction frac = (DPFraction)element;
		double childScale = getChildScale();
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		LAllocV prevChildAllocVs[] = new LAllocV[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = frac.getWrappedChild( i );
			if ( i != BAR )
			{
				reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox().scaledRequisition( childScale )  :  null;
			}
			else
			{
				reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			}
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildAllocVs[i] = child != null  ?  allocBoxes[i].getAllocV()  :  null;
		}


		FractionLayout.allocateY( layoutReqBox, reqBoxes[NUMERATOR], reqBoxes[BAR], reqBoxes[DENOMINATOR],
				getAllocationBox(), allocBoxes[NUMERATOR], allocBoxes[BAR], allocBoxes[DENOMINATOR], 
				getHPadding(), getVSpacing(), getYOffset() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = frac.getWrappedChild( i );
			if ( child != null )
			{
				if ( i != BAR )
				{
					allocBoxes[i].scaleAllocationY( 1.0 / childScale );
				}
				child.getLayoutNode().refreshAllocationY( prevChildAllocVs[i] );
			}
		}
	}
	
	

	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( element.getLayoutChildren(), localPos, filter );
	}



	//
	// Focus navigation methods
	//
	
	public List<DPWidget> horizontalNavigationList()
	{
		return verticalNavigationList();
	}

	public List<DPWidget> verticalNavigationList()
	{
		return element.getLayoutChildren();
	}
	
	
	
	
	//
	// STYLESHEET METHODS
	//
	
	protected double getVSpacing()
	{
		return ((FractionStyleSheet)element.getStyleSheet()).getVSpacing();
	}

	protected double getHPadding()
	{
		return ((FractionStyleSheet)element.getStyleSheet()).getHPadding();
	}

	protected double getYOffset()
	{
		return ((FractionStyleSheet)element.getStyleSheet()).getYOffset();
	}
}