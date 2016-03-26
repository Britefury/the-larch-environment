//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
