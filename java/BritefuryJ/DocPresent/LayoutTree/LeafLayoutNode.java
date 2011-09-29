//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class LeafLayoutNode extends LayoutNode
{
	protected LReqBox layoutReqBox;
	protected LAllocBox layoutAllocBox;
	
	
	public LeafLayoutNode(DPElement element)
	{
		super(element);
		layoutReqBox = new LReqBox();
		layoutAllocBox = new LAllocBox( this );
	}


	
	public LReqBoxInterface getRequisitionBox()
	{
		return layoutReqBox;
	}
	
	public LAllocBoxInterface getAllocationBox()
	{
		return layoutAllocBox;
	}
	
	
	
	
	
	public Point2 getPositionInParentSpace()
	{
		return getLocalToParentAllocationSpaceXform().transform( layoutAllocBox.getPositionInParentSpace() );
	}
	
	public double getAllocPositionInParentSpaceX()
	{
		return getLocalToParentAllocationSpaceXform().transformPointX( layoutAllocBox.getAllocPositionInParentSpaceX() );
	}
	
	public double getAllocPositionInParentSpaceY()
	{
		return getLocalToParentAllocationSpaceXform().transformPointY( layoutAllocBox.getAllocPositionInParentSpaceY() );
	}

	public Point2 getPositionInParentAllocationSpace()
	{
		return layoutAllocBox.getPositionInParentSpace();
	}
	
	public double getActualWidth()
	{
		return layoutAllocBox.getActualWidth();
	}
	
	public double getActualHeight()
	{
		return layoutAllocBox.getAllocHeight();
	}
	
	public Vector2 getActualSize()
	{
		return layoutAllocBox.getActualSize();
	}

	public double getActualWidthInParentSpace()
	{
		return layoutAllocBox.getActualWidth()  *  getLocalToParentAllocationSpaceXform().scale;
	}
	
	public double getActualHeightInParentSpace()
	{
		return layoutAllocBox.getAllocHeight()  *  getLocalToParentAllocationSpaceXform().scale;
	}
	
	public Vector2 getActualSizeInParentSpace()
	{
		return layoutAllocBox.getActualSize().mul( getLocalToParentAllocationSpaceXform().scale );
	}
	

	public double getAllocWidth()
	{
		return layoutAllocBox.getAllocWidth();
	}
	
	public double getAllocHeight()
	{
		return layoutAllocBox.getAllocHeight();
	}

	public Vector2 getAllocSize()
	{
		return layoutAllocBox.getAllocSize();
	}

	public LAllocV getAllocV()
	{
		return layoutAllocBox.getAllocV();
	}
}
