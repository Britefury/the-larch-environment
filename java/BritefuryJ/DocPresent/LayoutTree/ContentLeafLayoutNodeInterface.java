//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.Math.Point2;

public interface ContentLeafLayoutNodeInterface
{
	DPContentLeafEditable getEditableContentLeafAbove(Point2 localPos);
	DPContentLeafEditable getEditableContentLeafBelow(Point2 localPos);
	DPContentLeafEditable getEditableContentLeafAboveOrBelow(Point2 localPos, boolean bBelow);
	
	
	DPContentLeaf getLeftContentLeaf();
	DPContentLeaf getRightContentLeaf();
	DPContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace);
	DPElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter);
}
