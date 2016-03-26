//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSContentLeafEditable;

public abstract class EditableContentLeafLayoutNode extends ContentLeafLayoutNode implements EditableContentLeafLayoutNodeInterface
{
	public EditableContentLeafLayoutNode(LSContentLeafEditable element)
	{
		super( element );
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


