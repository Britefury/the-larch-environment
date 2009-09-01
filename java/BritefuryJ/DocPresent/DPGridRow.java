//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Layout.GridLayout;
import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.GridRowStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class DPGridRow extends DPContainerSequenceCollationRoot
{
	public DPGridRow()
	{
		this( GridRowStyleSheet.defaultStyleSheet );
	}
	
	public DPGridRow(GridRowStyleSheet syleSheet)
	{
		super( syleSheet );
	}
	
	
	
	
	protected void updateRequisitionX()
	{
		refreshCollation();
		
		LReqBox childBoxes[] = new LReqBox[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionX();
		}

		HorizontalLayout.computeRequisitionX( layoutReqBox, childBoxes, 0.0 );
	}

	protected void updateRequisitionY()
	{
		LReqBox childBoxes[] = new LReqBox[collationLeaves.length];
		int childAllocFlags[] = new int[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionY();
			childAllocFlags[i] = collationLeaves[i].getAlignmentFlags();
		}

		GridLayout.computeRowRequisitionY( layoutReqBox, childBoxes, childAllocFlags );
	}
	

	

	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		int childAllocFlags[] = getCollatedChildrenAlignmentFlags();
		double prevWidths[] = getCollatedChildrenAllocationX();
		
		HorizontalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags, 0.0 );
		
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
		int childAlignmentFlags[] = getCollatedChildrenAlignmentFlags();
		LAllocV prevAllocVs[] = getCollatedChildrenAllocV();
		
		GridLayout.allocateRowY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAlignmentFlags );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationY( prevAllocVs[i] );
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
	
	
	
	//
	// Style methods
	//


	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}
}
