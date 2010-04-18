//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import BritefuryJ.DocPresent.LayoutTree.LayoutNode;
import BritefuryJ.Math.Vector2;

public interface LAllocBoxInterface
{
	public abstract LayoutNode getAllocLayoutNode();

	public abstract double getAllocPositionInParentAllocationSpaceX();
	public abstract double getAllocPositionInParentAllocationSpaceY();
	public abstract double getAllocationX();
	public abstract double getAllocationY();
	public abstract double getAllocRefY();
	public abstract LAllocV getAllocV();
	public abstract Vector2 getAllocation();
	
	public abstract void setAllocPositionInParentAllocationSpaceX(double x);
	public abstract void setAllocPositionInParentAllocationSpaceY(double y);
	public abstract void setAllocationX(double width);
	public abstract void setAllocationY(double height, double refY);
	public abstract void setAllocation(double width, double height, double refY);
	
	public abstract void setPositionInParentAllocationSpaceAndAllocationX(double x, double width);
	public abstract void setPositionInParentAllocationSpaceAndAllocationY(double y, double height);
	public abstract void setPositionInParentAllocationSpaceAndAllocationY(double y, double height, double refY);
	
	public abstract void scaleAllocationX(double scale);
	public abstract void scaleAllocationY(double scale);
}
