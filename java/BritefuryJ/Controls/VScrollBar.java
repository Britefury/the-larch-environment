//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Box;

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
	
	protected Pres createDragBox(double scrollBarSize)
	{
		return new Box( scrollBarSize, 0.0 ).alignHCentre().alignVExpand();
	}
}
