//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Arrow;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.Util.Range;

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
		return StyleSheet.instance.withAttr( Primitive.hboxSpacing, spacing ).applyTo( new HBox( new Object[] {
				decArrowElement.alignVCentre(), dragBarElement.alignHExpand().alignVCentre(), incArrowElement.alignVCentre() } ) );
	}
}
