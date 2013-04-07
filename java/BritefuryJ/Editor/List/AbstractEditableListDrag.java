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
