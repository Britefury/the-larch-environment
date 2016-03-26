//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.List;

import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSFraction;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.FractionLayout;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.StyleParams.FractionStyleParams;
import BritefuryJ.Math.Point2;

public class LayoutNodeFraction extends ArrangedLayoutNode
{
	public static class LayoutNodeFractionBar extends EditableContentLeafLayoutNode
	{
		public static double BAR_HEIGHT = 1.5;
		
		
		public LayoutNodeFractionBar(LSFraction.DPFractionBar element)
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

	
	public LayoutNodeFraction(LSFraction element)
	{
		super( element );
	}
	
	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSFraction frac = (LSFraction)element;
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = frac.getWrappedChild( i );
			boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionX()  :  null;
		}

		FractionLayout.computeRequisitionX( layoutReqBox, boxes[NUMERATOR], boxes[BAR], boxes[DENOMINATOR], getHPadding(), getVSpacing(), getYOffset() );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSFraction frac = (LSFraction)element;
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = frac.getWrappedChild( i );
			boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionY()  :  null;
		}

		FractionLayout.computeRequisitionY( layoutReqBox, boxes[NUMERATOR], boxes[BAR], boxes[DENOMINATOR], getHPadding(), getVSpacing(), getYOffset() );
	}
	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSFraction frac = (LSFraction)element;
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		double prevChildWidths[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = frac.getWrappedChild( i );
			reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildWidths[i] = child != null  ?  allocBoxes[i].getAllocWidth()  :  0.0;
		}
		

		FractionLayout.allocateX( layoutReqBox, reqBoxes[NUMERATOR], reqBoxes[BAR], reqBoxes[DENOMINATOR],
				getAllocationBox(), allocBoxes[NUMERATOR], allocBoxes[BAR], allocBoxes[DENOMINATOR], 
				getHPadding(), getVSpacing(), getYOffset() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = frac.getWrappedChild( i );
			if ( child != null )
			{
				child.getLayoutNode().refreshAllocationX( prevChildWidths[i] );
			}
		}
	}

	
	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSFraction frac = (LSFraction)element;
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		LAllocV prevChildAllocVs[] = new LAllocV[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = frac.getWrappedChild( i );
			reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildAllocVs[i] = child != null  ?  allocBoxes[i].getAllocV()  :  null;
		}


		FractionLayout.allocateY( layoutReqBox, reqBoxes[NUMERATOR], reqBoxes[BAR], reqBoxes[DENOMINATOR],
				getAllocationBox(), allocBoxes[NUMERATOR], allocBoxes[BAR], allocBoxes[DENOMINATOR], 
				getHPadding(), getVSpacing(), getYOffset() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = frac.getWrappedChild( i );
			if ( child != null )
			{
				child.getLayoutNode().refreshAllocationY( prevChildAllocVs[i] );
			}
		}
	}
	
	

	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( ( (LSContainer)element ).getLayoutChildren(), localPos, filter );
	}



	//
	// Focus navigation methods
	//
	
	public List<LSElement> horizontalNavigationList()
	{
		return verticalNavigationList();
	}

	public List<LSElement> verticalNavigationList()
	{
		return ( (LSContainer)element ).getLayoutChildren();
	}
	
	
	
	
	//
	// STYLESHEET METHODS
	//
	
	protected double getVSpacing()
	{
		return ((FractionStyleParams)element.getStyleParams()).getVSpacing();
	}

	protected double getHPadding()
	{
		return ((FractionStyleParams)element.getStyleParams()).getHPadding();
	}

	protected double getYOffset()
	{
		return ((FractionStyleParams)element.getStyleParams()).getYOffset();
	}
}
