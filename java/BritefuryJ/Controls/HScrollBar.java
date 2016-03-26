//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Box;

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
	
	protected Pres createDragBox(double scrollBarSize)
	{
		return new Box( 0.0, scrollBarSize ).alignHExpand().alignVCentre();
	}
}
