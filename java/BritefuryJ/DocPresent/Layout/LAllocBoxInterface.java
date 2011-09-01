//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import BritefuryJ.DocPresent.LayoutTree.LayoutNode;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public interface LAllocBoxInterface
{
	public abstract LayoutNode getAllocLayoutNode();

	public abstract double getAllocPositionInParentSpaceX();
	public abstract double getAllocPositionInParentSpaceY();
	public abstract double getActualWidth();
	public abstract double getAllocWidth();
	public abstract double getAllocHeight();
	public abstract Vector2 getActualSize();
	public abstract double getAllocRefY();
	public abstract LAllocV getAllocV();
	public abstract Vector2 getAllocSize();
	
	public abstract void setAllocPositionInParentSpaceX(double x);
	public abstract void setAllocPositionInParentSpaceY(double y);
	public abstract void setAllocationX(double allocWidth, double actualWidth);
	public abstract void setAllocationY(double allocHeight, double refY);
	
	public abstract void setPositionInParentSpaceAndAllocationX(double x, double allocWidth, double actualWidth);
	public abstract void setPositionInParentSpaceAndAllocationY(double y, double height);
	public abstract void setPositionInParentSpaceAndAllocationY(double y, double height, double refY);
	
	public abstract void transformAllocationX(Xform2 xform);
	public abstract void transformAllocationY(Xform2 xform);
}
