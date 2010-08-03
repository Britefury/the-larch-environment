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
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.Util.Range;

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
		return new Arrow( Arrow.Direction.UP, arrowSize );
	}
	
	protected Pres createIncArrow(double arrowSize)
	{
		return new Arrow( Arrow.Direction.DOWN, arrowSize );
	}
	
	protected Pres createDragBox(double scrollBarSize)
	{
		return new Box( scrollBarSize, 0.0 );
	}

	protected Pres createScrollBarPres(double spacing, DPElement decArrowElement, DPElement dragBarElement, DPElement incArrowElement)
	{
		return StyleSheet.instance.withAttr( Primitive.vboxSpacing, spacing ).applyTo( new VBox( new Object[] {
				decArrowElement.alignHCentre(), dragBarElement.alignVExpand().alignHCentre(), incArrowElement.alignHCentre() } ) );
	}
}
