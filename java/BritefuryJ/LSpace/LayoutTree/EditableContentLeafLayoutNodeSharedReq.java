//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.Layout.LReqBox;

public abstract class EditableContentLeafLayoutNodeSharedReq extends ContentLeafLayoutNodeSharedReq implements EditableContentLeafLayoutNodeInterface
{
	public EditableContentLeafLayoutNodeSharedReq(LSContentLeafEditable element, LReqBox reqBox)
	{
		super( element, reqBox );
	}
	
	
	public LSContentLeafEditable getLeftEditableContentLeaf()
	{
		return (LSContentLeafEditable)element;
	}

	public LSContentLeafEditable getRightEditableContentLeaf()
	{
		return (LSContentLeafEditable)element;
	}
}
