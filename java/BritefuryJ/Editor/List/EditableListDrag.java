//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2013.
//##************************
package BritefuryJ.Editor.List;

public class EditableListDrag extends AbstractEditableListDrag
{
	private Object editableList, item;


	public EditableListDrag(EditableListController controller, Object editableList, Object item)
	{
		super( controller );
		this.editableList = editableList;
		this.item = item;
	}


	@Override
	public Object getEditableList()
	{
		return editableList;
	}

	@Override
	public Object getItem()
	{
		return item;
	}
}
