//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Layout;

import BritefuryJ.Math.Xform2;

public interface LReqBoxInterface
{
	public abstract double getReqMinWidth();
	public abstract double getReqPrefWidth();
	public abstract double getReqMinHAdvance();
	public abstract double getReqPrefHAdvance();
	
	public abstract double getReqHeight();
	public abstract double getReqVSpacing();

	public abstract double getReqRefY();
	public abstract double getReqHeightBelowRefPoint();
	
	public abstract boolean isReqParagraphIndentMarker();
	public abstract boolean isReqParagraphDedentMarker();

	public abstract LReqBoxInterface transformedRequisition(Xform2 xform);


	public abstract void clearRequisitionX();
	public abstract void clearRequisitionY();
	
	public abstract void setRequisitionX(double width, double hAdvance);
	public abstract void setRequisitionX(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance);
	public abstract void setRequisitionX(LReqBoxInterface box);

	public abstract void setRequisitionY(double height, double vSpacing);
	public abstract void setRequisitionY(double height, double vSpacing, double refY);
	public abstract void setRequisitionY(LReqBoxInterface reqBox);

	public abstract void borderX(double leftMargin, double rightMargin);
	public abstract void borderY(double topMargin, double bottomMargin);
}