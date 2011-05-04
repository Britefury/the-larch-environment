//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.GridRow;

class ObjectListRow implements Presentable
{
	private ObjectListTableEditor editor;
	private Object modelRow;
	private PresentationStateListenerList listeners = null;
	
	
	public ObjectListRow(ObjectListTableEditor editor, Object modelRow)
	{
		this.editor = editor;
		this.modelRow = modelRow;
		this.listeners = null;
	}
	
	
	protected void onChanged()
	{
		listeners = PresentationStateListenerList.onPresentationStateChanged( listeners, this );
	}


	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		listeners = PresentationStateListenerList.addListener( listeners, fragment );

		Object children[] = new Object[editor.columns.length];
		for (int i = 0; i < editor.columns.length; i++)
		{
			children[i] = new ObjectListCell( modelRow, editor.columns[i] );
		}
		
		return new GridRow( children );
	}
}
