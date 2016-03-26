//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import BritefuryJ.Cell.AbstractBlankCell;
import BritefuryJ.Cell.EditableTextCell;

public class ObjectListBlankCell extends AbstractBlankCell
{
	protected ObjectListTableEditorInstance editorInstance;
	protected AbstractColumn column;
	
	
	public ObjectListBlankCell(ObjectListTableEditorInstance editorInstance, AbstractColumn column)
	{
		super( EditableTextCell.blankTextCell( "", column.createImportFn() ) );
		this.editorInstance = editorInstance;
		this.column = column;
	}
	
	
	@Override
	public void setValue(Object value)
	{
		try
		{
			Object modelRow = editorInstance.newRow(); 
			column.set( modelRow, value );
		}
		catch (UnsupportedOperationException e)
		{
		}
	}
}
