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
import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LBox;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
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
	
	
	
	public int getInsertIndex(Point2 localPos)
	{
		//Return the index at which an item could be inserted.
		// localPos is checked against the contents of the box in order to determine the insert index
		
		if ( size() == 0 )
		{
			return 0;
		}
	
		double pos = localPos.x;
		
		double[] midPoints = new double[registeredChildren.size()];
		
		for (int i = 0; i < midPoints.length; i++)
		{
			DPWidget child = registeredChildren.get( i );
			midPoints[i] = child.getPositionInParentSpace().x  +  child.getAllocationInParentSpace().x * 0.5;
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
		LBox[] childBoxes = new LBox[registeredChildren.size()];
		BoxPackingParams[] packingParams = new BoxPackingParams[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionX();
			packingParams[i] = (BoxPackingParams)registeredChildren.get( i ).getParentPacking();
		}

		HorizontalLayout.computeRequisitionX( layoutBox, childBoxes, getSpacing(), packingParams );
	}

	protected void updateRequisitionY()
	{
		LBox[] childBoxes = new LBox[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionY();
		}

		HorizontalLayout.computeRequisitionY( layoutBox, childBoxes, getAlignment() );
	}
	

	

	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LBox childBoxes[] = getChildrenLayoutBoxes();
		double prevWidths[] = getChildrenAllocationX();
		BoxPackingParams packing[] = (BoxPackingParams[])getChildrenPackingParams( new BoxPackingParams[registeredChildren.size()] );
		
		HorizontalLayout.allocateX( layoutBox, childBoxes, getSpacing(), packing );
		
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
		
		LBox childBoxes[] = getChildrenLayoutBoxes();
		double prevHeights[] = getChildrenAllocationY();
		
		HorizontalLayout.allocateY( layoutBox, childBoxes, getAlignment() );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.refreshAllocationY( prevHeights[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointHorizontal( localPos, filter );
	}



	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getChildren();
	}
	
	
	protected VAlignment getAlignment()
	{
		return ((HBoxStyleSheet)styleSheet).getAlignment();
	}
}
