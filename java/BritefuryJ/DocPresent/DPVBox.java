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
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.Layout.VerticalLayout;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;




public class DPVBox extends DPAbstractBox
{
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	

	
	public DPVBox()
	{
		this( VBoxStyleSheet.defaultStyleSheet );
	}
	
	public DPVBox(VBoxStyleSheet syleSheet)
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
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionX();
		}

		VerticalLayout.computeRequisitionX( layoutReqBox, childBoxes );
	}

	protected void updateRequisitionY()
	{
		LReqBox[] childBoxes = new LReqBox[collationLeaves.length];
		BoxPackingParams[] packingParams = new BoxPackingParams[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionY();
			packingParams[i] = (BoxPackingParams)collationLeaves[i].getParentPacking();
		}

		VerticalLayout.computeRequisitionY( layoutReqBox, childBoxes, getTypesetting(), getSpacing(), packingParams );
	}

	

	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		double prevWidths[] = getCollatedChildrenAllocationX();
		
		VerticalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getAlignment() );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationX( prevWidths[i] );
			i++;
		}
	}

	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		double prevHeights[] = getCollatedChildrenAllocationY();
		BoxPackingParams packing[] = (BoxPackingParams[])getCollatedChildrenPackingParams( new BoxPackingParams[collationLeaves.length] );
		
		VerticalLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getSpacing(), packing );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationY( prevHeights[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( Arrays.asList( collationLeaves ), localPos, filter );
	}


	
	protected AABox2[] computeCollatedBranchBoundsBoxes(DPContainer collatedBranch, int rangeStart, int rangeEnd)
	{
		DPWidget startLeaf = collationLeaves[rangeStart];
		DPWidget endLeaf = collationLeaves[rangeEnd-1];
		double yStart = startLeaf.getPositionInParentSpaceY();
		double yEnd = endLeaf.getPositionInParentSpaceY()  +  endLeaf.getAllocationInParentSpaceY();
		AABox2 box = new AABox2( 0.0, yStart, getAllocationX(), yEnd );
		return new AABox2[] { box };
	}

	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getCollatedChildren();
	}
	
	protected List<DPWidget> verticalNavigationList()
	{
		return getCollatedChildren();
	}



	
	
	
	
	protected VTypesetting getTypesetting()
	{
		return ((VBoxStyleSheet)styleSheet).getTypesetting();
	}

	protected HAlignment getAlignment()
	{
		return ((VBoxStyleSheet)styleSheet).getAlignment();
	}
}
