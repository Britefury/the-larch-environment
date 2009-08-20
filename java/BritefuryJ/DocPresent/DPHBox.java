//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Layout.BoxPackingParams;
import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;



public class DPHBox extends DPAbstractBox
{
	public DPHBox()
	{
		this( HBoxStyleSheet.defaultStyleSheet );
	}
	
	public DPHBox(HBoxStyleSheet syleSheet)
	{
		super( syleSheet );
	}
	
	
	
	
	public void append(DPWidget child,  boolean bExpand, double padding)
	{
		append( child );
		child.setParentPacking( new BoxPackingParams( padding, bExpand ) );
	}

	
	public void insert(int index, DPWidget child, boolean bExpand, double padding)
	{
		insert( index, child );
		child.setParentPacking( new BoxPackingParams( padding, bExpand ) );
	}
	
	
	
	protected void updateRequisitionX()
	{
		refreshCollation();
		
		LReqBox[] childBoxes = new LReqBox[collationLeaves.length];
		BoxPackingParams[] packingParams = new BoxPackingParams[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionX();
			packingParams[i] = (BoxPackingParams)collationLeaves[i].getParentPacking();
		}

		HorizontalLayout.computeRequisitionX( layoutReqBox, childBoxes, getSpacing(), packingParams );
	}

	protected void updateRequisitionY()
	{
		LReqBox[] childBoxes = new LReqBox[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionY();
		}

		HorizontalLayout.computeRequisitionY( layoutReqBox, childBoxes, getAlignment() );
	}
	

	

	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		double prevWidths[] = getCollatedChildrenAllocationX();
		BoxPackingParams packing[] = (BoxPackingParams[])getCollatedChildrenPackingParams( new BoxPackingParams[collationLeaves.length] );
		
		HorizontalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getSpacing(), packing );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationX( prevWidths[i] );
			i++;
		}
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		double prevHeights[] = getCollatedChildrenAllocationY();
		
		HorizontalLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getAlignment() );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationY( prevHeights[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointHorizontal( Arrays.asList( collationLeaves ), localPos, filter );
	}



	protected AABox2[] computeCollatedBranchBoundsBoxes(DPContainer collatedBranch, int rangeStart, int rangeEnd)
	{
		refreshCollation();
		
		DPWidget startLeaf = collationLeaves[rangeStart];
		DPWidget endLeaf = collationLeaves[rangeEnd-1];
		double xStart = startLeaf.getPositionInParentSpaceX();
		double xEnd = endLeaf.getPositionInParentSpaceX()  +  endLeaf.getAllocationInParentSpaceX();
		AABox2 box = new AABox2( xStart, 0.0, xEnd, getAllocationY() );
		return new AABox2[] { box };
	}

	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getCollatedChildren();
	}
	
	
	protected VAlignment getAlignment()
	{
		return ((HBoxStyleSheet)styleSheet).getAlignment();
	}
}
