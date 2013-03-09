//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2013.
//##************************
package BritefuryJ.Editor.List;

public class EditableListDrag
{
	private EditableListController controller;
	private Object editableList, item;


	public EditableListDrag(EditableListController controller, Object editableList, Object item)
	{
		this.controller = controller;
		this.editableList = editableList;
		this.item = item;
	}


	public Object getController()
	{
		return controller;
	}

	public Object getEditableList()
	{
		return editableList;
	}

	public Object getItem()
	{
		return item;
	}
}
