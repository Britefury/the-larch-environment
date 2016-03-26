//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.List;

public abstract class AbstractEditableListDrag
{
	protected EditableListController controller;


	public AbstractEditableListDrag(EditableListController controller)
	{
		this.controller = controller;
	}


	public EditableListController getController()
	{
		return controller;
	}

	public abstract Object getEditableList();
	public abstract Object getItem();
}
