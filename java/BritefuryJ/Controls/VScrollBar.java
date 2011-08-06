//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Arrow;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class VScrollBar extends ScrollBar
{
	public VScrollBar(Range range)
	{
		super( range );
	}
	
	
	protected ScrollBarHelper.Axis getAxis()
	{
		return ScrollBarHelper.Axis.VERTICAL;
	}
	
	protected Pres createDecArrow(double arrowSize)
	{
		return new Arrow( Arrow.Direction.UP, arrowSize ).alignHCentre().alignVRefY();
	}
	
	protected Pres createIncArrow(double arrowSize)
	{
		return new Arrow( Arrow.Direction.DOWN, arrowSize ).alignHCentre().alignVRefY();
	}
	
	protected Pres createDragBox(double scrollBarSize)
	{
		return new Box( scrollBarSize, 0.0 ).alignHCentre().alignVExpand();
	}

	protected Pres createScrollBarPres(double spacing, DPElement decArrowElement, DPElement dragBarElement, DPElement incArrowElement)
	{
		return StyleSheet.style( Primitive.columnSpacing.as( spacing ) ).applyTo( new Column( new Object[] {
			    decArrowElement, dragBarElement, incArrowElement } ) );
	}
}
