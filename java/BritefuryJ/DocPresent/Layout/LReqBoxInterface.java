//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

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
	
	public abstract boolean isReqLineBreak();
	public abstract boolean isReqParagraphIndentMarker();
	public abstract boolean isReqParagraphDedentMarker();
	public abstract int getReqLineBreakCost();
	
	public abstract LReqBoxInterface scaledRequisition(double scale);


	public abstract void clearRequisitionX();
	public abstract void clearRequisitionY();
	
	public abstract void setRequisitionX(double width, double hAdvance);
	public abstract void setRequisitionX(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance);
	public abstract void setRequisitionX(LReqBoxInterface box);

	public abstract void setRequisitionY(double height, double vSpacing);
	public abstract void setRequisitionY(double height, double vSpacing, double refY);
	public abstract void setRequisitionY(LReqBoxInterface reqBox);

	public abstract void setLineBreakCost(int cost);

	public abstract void borderX(double leftMargin, double rightMargin);
	public abstract void borderY(double topMargin, double bottomMargin);
}