//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.Layout.LReqBox;

public abstract class EditableContentLeafLayoutNodeSharedReq extends ContentLeafLayoutNodeSharedReq implements EditableContentLeafLayoutNodeInterface
{
	public EditableContentLeafLayoutNodeSharedReq(DPContentLeafEditable element, LReqBox reqBox)
	{
		super( element, reqBox );
	}
	
	
	public DPContentLeafEditable getLeftEditableContentLeaf()
	{
		return (DPContentLeafEditable)element;
	}

	public DPContentLeafEditable getRightEditableContentLeaf()
	{
		return (DPContentLeafEditable)element;
	}
}
