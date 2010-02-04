//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import BritefuryJ.DocPresent.LayoutTree.LayoutNode;

public abstract class LAllocBoxInterface
{
	public abstract double getPositionInParentSpaceX();
	public abstract double getPositionInParentSpaceY();
	public abstract double getAllocationX();
	public abstract double getAllocationY();
	public abstract double getRefY();
	public abstract LAllocV getAllocV();
	
	public abstract void setPositionInParentSpaceX(double x);
	public abstract void setPositionInParentSpaceY(double y);
	public abstract void setAllocationX(double width);
	public abstract void setAllocationY(double height);
	public abstract void setAllocationY(double height, double refY);
	
	protected abstract void setPositionInParentSpaceAndAllocationX(double x, double width);
	protected abstract void setPositionInParentSpaceAndAllocationY(double y, double height);
	protected abstract void setPositionInParentSpaceAndAllocationY(double y, double height, double refY);
	
	public abstract void scaleAllocationX(double scale);
	public abstract void scaleAllocationY(double scale);
	
	public abstract LayoutNode getLayoutNode();
}
