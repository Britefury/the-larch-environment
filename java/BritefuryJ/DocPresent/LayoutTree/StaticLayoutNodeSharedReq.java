//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPStatic;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.Math.Point2;

public abstract class StaticLayoutNodeSharedReq extends LeafLayoutNodeSharedReq
{
	public StaticLayoutNodeSharedReq(DPStatic element, LReqBox reqBox)
	{
		super( element, reqBox );
	}

	public DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return null;
	}
}
