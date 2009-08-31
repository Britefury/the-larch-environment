//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.Layout.GridLayout;
import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.Math.Point2;

public class DPGridRow extends DPContainerSequence
{
	public DPGridRow()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}
	
	public DPGridRow(ContainerStyleSheet syleSheet)
	{
		super( syleSheet );
	}
	
	
	
	
	protected void updateRequisitionX()
	{
		LReqBox childBoxes[] = new LReqBox[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionX();
		}

		HorizontalLayout.computeRequisitionX( layoutReqBox, childBoxes, 0.0 );
	}

	protected void updateRequisitionY()
	{
		LReqBox childBoxes[] = new LReqBox[registeredChildren.size()];
		int childAllocFlags[] = new int[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionY();
			childAllocFlags[i] = registeredChildren.get( i ).getAlignmentFlags();
		}

		GridLayout.computeRowRequisitionY( layoutReqBox, childBoxes, childAllocFlags );
	}
	

	

	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBox childBoxes[] = getChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getChildrenAllocationBoxes();
		int childAllocFlags[] = getChildrenAlignmentFlags();
		double prevWidths[] = getChildrenAllocationX();
		
		HorizontalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags, 0.0 );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.refreshAllocationX( prevWidths[i] );
			i++;
		}
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBox childBoxes[] = getChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getChildrenAllocationBoxes();
		int childAlignmentFlags[] = getChildrenAlignmentFlags();
		LAllocV prevAllocVs[] = getChildrenAllocV();
		
		GridLayout.allocateRowY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAlignmentFlags );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.refreshAllocationY( prevAllocVs[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointHorizontal( registeredChildren, localPos, filter );
	}



	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getChildren();
	}


	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}
}
