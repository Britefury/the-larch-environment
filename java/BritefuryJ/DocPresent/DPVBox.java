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

import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
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
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionY();
		}

		VerticalLayout.computeRequisitionY( layoutReqBox, childBoxes, getTypesetting(), getSpacing() );
	}

	

	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		int childAllocFlags[] = getCollatedChildrenAlignmentFlags();
		double prevWidths[] = getCollatedChildrenAllocationX();
		
		VerticalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags );
		
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
		int childAllocFlags[] = getCollatedChildrenAlignmentFlags();
		LAllocV prevAllocVs[] = getCollatedChildrenAllocV();
		
		VerticalLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags, getSpacing() );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationY( prevAllocVs[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( Arrays.asList( collationLeaves ), localPos, filter );
	}


	
	protected AABox2[] computeCollatedBranchBoundsBoxes(DPContainer collatedBranch, int rangeStart, int rangeEnd)
	{
		refreshCollation();
		
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
}
