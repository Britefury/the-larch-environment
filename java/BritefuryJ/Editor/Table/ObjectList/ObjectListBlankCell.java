//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
		super( EditableTextCell.textCell( "", column.createConversionFn() ) );
		this.editorInstance = editorInstance;
		this.column = column;
	}
	
	
	@Override
	public void setValue(Object value)
	{
		Object modelRow = editorInstance.newRow(); 
		column.set( modelRow, value );
	}
}
