//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public abstract class LeafLayoutNodeSharedReq extends LayoutNode implements LAllocBoxInterface
{
	protected LReqBox layoutReqBox;
	
	
	public LeafLayoutNodeSharedReq(DPElement element, LReqBox reqBox)
	{
		super( element );
		layoutReqBox = reqBox;
	}


	
	
	public LReqBoxInterface getRequisitionBox()
	{
		return layoutReqBox;
	}
	
	public LAllocBoxInterface getAllocationBox()
	{
		return this;
	}
	
	
	
	
	
	public double getActualWidthInParentSpace()
	{
		return getActualWidth()  *  getLocalToParentAllocationSpaceXform().scale;
	}
	
	public double getActualHeightInParentSpace()
	{
		return getActualHeight()  *  getLocalToParentAllocationSpaceXform().scale;
	}
	
	public Vector2 getActualSizeInParentSpace()
	{
		return getActualSize().mul( getLocalToParentAllocationSpaceXform().scale );
	}

	
	
	
	
	//
	//
	//
	//
	//
	// ALLOCATION BOX IMPLEMENTATION
	//
	//
	//
	//
	//

	protected double alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY;
	protected double alloc_actualWidth, alloc_allocWidth, alloc_allocHeight;
	protected double alloc_refY;

	
	public LayoutNode getAllocLayoutNode()
	{
		return this;
	}
	
	
	
	public Point2 getPositionInParentSpace()
	{
		return getLocalToParentAllocationSpaceXform().transform( new Point2( alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY ) );
	}
	
	public double getAllocPositionInParentSpaceX()
	{
		return getLocalToParentAllocationSpaceXform().transformPointX( alloc_positionInParentAllocationSpaceX );
	}
	
	public double getAllocPositionInParentSpaceY()
	{
		return getLocalToParentAllocationSpaceXform().transformPointY( alloc_positionInParentAllocationSpaceY );
	}
	
	public Point2 getPositionInParentAllocationSpace()
	{
		return new Point2( alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY );
	}
	
	public double getActualWidth()
	{
		return alloc_actualWidth;
	}
	
	public double getActualHeight()
	{
		return alloc_allocHeight;
	}
	
	public Vector2 getActualSize()
	{
		return new Vector2( alloc_actualWidth, alloc_allocHeight );
	}
	

	public double getAllocWidth()
	{
		return alloc_allocWidth;
	}
	
	public double getAllocHeight()
	{
		return alloc_allocHeight;
	}
	
	public double getAllocRefY()
	{
		return alloc_refY;
	}
	
	public LAllocV getAllocV()
	{
		return new LAllocV( alloc_allocHeight, alloc_refY );
	}
	
	public Vector2 getAllocSize()
	{
		return new Vector2( alloc_allocWidth, alloc_allocHeight );
	}


	
	
	
	//
	// SETTERS
	//
	
	public void setAllocPositionInParentSpaceX(double x)
	{
		alloc_positionInParentAllocationSpaceX = x;
	}
	
	public void setAllocPositionInParentSpaceY(double y)
	{
		alloc_positionInParentAllocationSpaceY = y;
	}
	
	public void setAllocationX(double allocWidth, double actualWidth)
	{
		alloc_allocWidth = allocWidth;
		alloc_actualWidth = actualWidth;
	}

	public void setAllocationY(double allocHeight, double refY)
	{
		alloc_allocHeight = allocHeight;
		this.alloc_refY = refY;
	}

	public void setPositionInParentSpaceAndAllocationX(double x, double allocWidth, double actualWidth)
	{
		alloc_positionInParentAllocationSpaceX = x;
		alloc_allocWidth = allocWidth;
		alloc_actualWidth = actualWidth;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height)
	{
		alloc_positionInParentAllocationSpaceY = y;
		alloc_allocHeight = height;
		alloc_refY = height * 0.5;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height, double refY)
	{
		alloc_positionInParentAllocationSpaceY = y;
		alloc_allocHeight = height;
		this.alloc_refY = refY;
	}



	public void transformAllocationX(Xform2 xform)
	{
		alloc_allocWidth = xform.scale( alloc_allocWidth );
	}

	public void transformAllocationY(Xform2 xform)
	{
		alloc_allocHeight = xform.scale( alloc_allocHeight );
		alloc_refY = xform.scale( alloc_refY );
	}
}
