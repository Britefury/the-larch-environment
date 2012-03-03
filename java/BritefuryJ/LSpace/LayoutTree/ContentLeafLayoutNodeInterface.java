//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSContentLeaf;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.Math.Point2;

public interface ContentLeafLayoutNodeInterface
{
	LSContentLeafEditable getEditableContentLeafAbove(Point2 localPos);
	LSContentLeafEditable getEditableContentLeafBelow(Point2 localPos);
	LSContentLeafEditable getEditableContentLeafAboveOrBelow(Point2 localPos, boolean bBelow);
	
	
	LSContentLeaf getLeftContentLeaf();
	LSContentLeaf getRightContentLeaf();
	LSContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace);
	LSElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter);
}
