//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class LeafLayoutNode extends LayoutNode
{
	protected DPWidget element;
	protected LReqBox layoutReqBox;
	protected LAllocBox layoutAllocBox;
	
	
	public LeafLayoutNode(DPWidget element)
	{
		this.element = element;
		layoutReqBox = new LReqBox();
		layoutAllocBox = new LAllocBox( this );
	}


	public DPWidget getElement()
	{
		return element;
	}
	

	
	
	public LReqBoxInterface getRequisitionBox()
	{
		return layoutReqBox;
	}
	
	public LAllocBoxInterface getAllocationBox()
	{
		return layoutAllocBox;
	}
	
	
	
	
	
	public double getAllocPositionInParentSpaceX()
	{
		return layoutAllocBox.getAllocPositionInParentSpaceX();
	}
	
	public double getAllocPositionInParentSpaceY()
	{
		return layoutAllocBox.getAllocPositionInParentSpaceY();
	}
	
	public Point2 getPositionInParentSpace()
	{
		return layoutAllocBox.getPositionInParentSpace();
	}

	public double getAllocationX()
	{
		return layoutAllocBox.getAllocationX();
	}
	
	public double getAllocationY()
	{
		return layoutAllocBox.getAllocationY();
	}

	public Vector2 getAllocation()
	{
		return layoutAllocBox.getAllocation();
	}

	public LAllocV getAllocV()
	{
		return layoutAllocBox.getAllocV();
	}

	public double getAllocationInParentSpaceX()
	{
		return layoutAllocBox.getAllocationX()  *  getScale();
	}
	
	public double getAllocationInParentSpaceY()
	{
		return layoutAllocBox.getAllocationY()  *  getScale();
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		return layoutAllocBox.getAllocation().mul( getScale() );
	}
	
	
	
	
	
	public LReqBoxInterface refreshRequisitionX()
	{
		if ( !element.isSizeUpToDate() )
		{
			updateRequisitionX();
		}
		return layoutReqBox;
	}
	
	public LReqBoxInterface refreshRequisitionY()
	{
		if ( !element.isSizeUpToDate() )
		{
			updateRequisitionY();
		}
		return layoutReqBox;
	}
	

	
	protected abstract void updateRequisitionX();
	protected abstract void updateRequisitionY();


	
	
	
	public void refreshAllocationX(double prevWidth)
	{
		if ( !element.isSizeUpToDate()  ||  layoutAllocBox.getAllocationX() != prevWidth )
		{
			updateAllocationX();
			element.clearFlagSizeUpToDate();
		}
	}
	
	public void refreshAllocationY(LAllocV prevHeight)
	{
		if ( !element.isSizeUpToDate()  ||  !layoutAllocBox.getAllocV().equals( prevHeight ) )
		{
			updateAllocationY();
		}
		onSizeRefreshed();
	}
	

	
	
	protected void updateAllocationX()
	{
	}

	protected void updateAllocationY()
	{
	}

	
	

	
	
	
	protected void onAllocationXRefreshed()
	{
		element.clearFlagSizeUpToDate();
	}
	
	protected void onAllocationYRefreshed()
	{
		onSizeRefreshed();
	}
	
	protected void onSizeRefreshed()
	{
		element.clearFlagResizeQueued();
		element.setFlagSizeUpToDate();
		DPContainer parent = element.getParent();
		while ( parent != null )
		{
			LayoutNode parentLayout = parent.getLayoutNode();
			if ( parentLayout != null )
			{
				parentLayout.onChildSizeRefreshed();
				break;
			}
			parent = parent.getParent();
		}
	}
	
	protected void onChildSizeRefreshed()
	{
	}
	
	
	
	
	protected double getScale()
	{
		return element.getScale();
	}
}
