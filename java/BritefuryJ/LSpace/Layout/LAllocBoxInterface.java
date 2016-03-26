//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Layout;

import BritefuryJ.LSpace.LayoutTree.LayoutNode;
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
