//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public abstract class LReqBoxInterface
{
	public abstract double getMinWidth();
	public abstract double getPrefWidth();
	public abstract double getMinHAdvance();
	public abstract double getPrefHAdvance();
	
	public abstract double getReqHeight();
	public abstract double getReqAscent();
	public abstract double getReqDescent();
	public abstract double getReqVSpacing();
	
	public abstract boolean hasBaseline();

	public abstract boolean isLineBreak();
	public abstract boolean isParagraphIndentMarker();
	public abstract boolean isParagraphDedentMarker();
	public abstract int getLineBreakCost();
	
	public abstract LReqBoxInterface scaled(double scale);
}