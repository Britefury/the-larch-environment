//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.Layout.BoxPackingParams;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.Layout.VerticalLayout;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
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
	
	
	
	public int getInsertIndex(Point2 localPos)
	{
		//Return the index at which an item could be inserted.
		// localPos is checked against the contents of the box in order to determine the insert index
		
		if ( size() == 0 )
		{
			return 0;
		}
	
		double pos = localPos.y;
		
		double[] midPoints = new double[registeredChildren.size()];
		
		for (int i = 0; i < midPoints.length; i++)
		{
			DPWidget child = registeredChildren.get( i );
			midPoints[i] = child.getPositionInParentSpace().y  +  child.getAllocationInParentSpace().y * 0.5;
		}
		
		if ( pos < midPoints[0] )
		{
			return size();
		}
		else if ( pos > midPoints[midPoints.length-1] )
		{
			return 0;
		}
		else
		{
			for (int i = 0; i < midPoints.length-1; i++)
			{
				double lower = midPoints[i];
				double upper = midPoints[i+1];
				if ( pos >= lower  &&  pos <= upper )
				{
					return i + 1;
				}
			}
			
			throw new CouldNotFindInsertionPointException();
		}
	}
	

	
	
	
	protected void updateRequisitionX()
	{
		LReqBox[] childBoxes = new LReqBox[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionX();
		}

		VerticalLayout.computeRequisitionX( layoutReqBox, childBoxes );
	}

	protected void updateRequisitionY()
	{
		LReqBox[] childBoxes = new LReqBox[registeredChildren.size()];
		BoxPackingParams[] packingParams = new BoxPackingParams[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionY();
			packingParams[i] = (BoxPackingParams)registeredChildren.get( i ).getParentPacking();
		}

		VerticalLayout.computeRequisitionY( layoutReqBox, childBoxes, getTypesetting(), getSpacing(), packingParams );
	}

	

	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBox childBoxes[] = getChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getChildrenAllocationBoxes();
		double prevWidths[] = getChildrenAllocationX();
		
		VerticalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getAlignment() );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.refreshAllocationX( prevWidths[i] );
			i++;
		}
	}

	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBox childBoxes[] = getChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getChildrenAllocationBoxes();
		double prevHeights[] = getChildrenAllocationY();
		BoxPackingParams packing[] = (BoxPackingParams[])getChildrenPackingParams( new BoxPackingParams[registeredChildren.size()] );
		
		VerticalLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getSpacing(), packing );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.refreshAllocationY( prevHeights[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( localPos, filter );
	}


	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getChildren();
	}
	
	protected List<DPWidget> verticalNavigationList()
	{
		return getChildren();
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
