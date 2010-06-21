//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CellEditor;

import java.util.WeakHashMap;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;

public abstract class LiteralCellEditor implements Presentable, IncrementalMonitorListener
{
	protected abstract class Editor
	{
		protected abstract void onCellChanged();
		protected abstract DPElement getElement();
	};
	
	
	protected LiteralCell cell;
	protected WeakHashMap<Editor, Object> editors = new WeakHashMap<Editor, Object>();
	
	
	public LiteralCellEditor(LiteralCell cell)
	{
		this.cell = cell;
		this.cell.addListener( this );
	}
	
	
	protected abstract Editor createEditor();
	

	protected <V extends Object> V getCellValue(Class<V> valueClass)
	{
		Object v = cell.getLiteralValue();
		
		if ( v == null )
		{
			return null;
		}
		
		V typedV = null;
		try
		{
			typedV = valueClass.cast( v );
		}
		catch (ClassCastException e)
		{
			return null;
		}
		
		return typedV;
	}
	
	protected void setCellValue(Object value)
	{
		cell.setLiteralValue( value );
	}
	

	
	@Override
	public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return createEditor().getElement();
	}


	@Override
	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		for (Editor editor: editors.keySet())
		{
			editor.onCellChanged();
		}
	}
}
