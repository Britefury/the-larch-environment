//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
