//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.Math.Point2;

public interface ContentLeafLayoutNodeInterface
{
	DPContentLeaf getContentLeafAbove(Point2 localPos, boolean bSkipWhitespace);
	DPContentLeaf getContentLeafBelow(Point2 localPos, boolean bSkipWhitespace);
	DPContentLeaf getContentLeafAboveOrBelow(Point2 localPos, boolean bBelow, boolean bSkipWhitespace);
	
	
	DPContentLeaf getLeftContentLeaf();
	DPContentLeaf getRightContentLeaf();
	DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace);
	DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter);
}
