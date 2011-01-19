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
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;

public class HScrollBar extends ScrollBar
{
	public HScrollBar(Range range)
	{
		super( range );
	}
	
	
	protected ScrollBarHelper.Axis getAxis()
	{
		return ScrollBarHelper.Axis.HORIZONTAL;
	}
	
	protected Pres createDecArrow(double arrowSize)
	{
		return new Arrow( Arrow.Direction.LEFT, arrowSize );
	}
	
	protected Pres createIncArrow(double arrowSize)
	{
		return new Arrow( Arrow.Direction.RIGHT, arrowSize );
	}
	
	protected Pres createDragBox(double scrollBarSize)
	{
		return new Box( 0.0, scrollBarSize );
	}

	protected Pres createScrollBarPres(double spacing, DPElement decArrowElement, DPElement dragBarElement, DPElement incArrowElement)
	{
		return StyleSheet.instance.withAttr( Primitive.rowSpacing, spacing ).applyTo( new Row( new Object[] {
				decArrowElement.alignVCentre(), dragBarElement.alignHExpand().alignVCentre(), incArrowElement.alignVCentre() } ) );
	}
}
