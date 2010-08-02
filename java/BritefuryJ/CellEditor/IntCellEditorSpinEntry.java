//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.CellEditor;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.Controls.IntSpinEntry;

public class IntCellEditorSpinEntry extends LiteralCellEditor
{
	protected class IntEditor extends Editor
	{
		protected class Listener implements IntSpinEntry.IntSpinEntryListener
		{
			@Override
			public void onSpinEntryValueChanged(IntSpinEntry.IntSpinEntryControl spinEntry, int value)
			{
				setCellValue( value );
			}
		}
		
		public IntEditor()
		{
			Integer value = getCellValue( Integer.class );
			int v = value != null  ?  value  :  0;
			if ( value != null )
			{
				setPres( new IntSpinEntry( v, min, max, stepSize, pageSize, new Listener() ) );
			}
			else
			{
				error( "not an integer number" );
			}
		}
		
		protected void refreshEditor()
		{
			Integer value = getCellValue( Integer.class );
			if ( value != null )
			{
				setPres( new IntSpinEntry( value, min, max, stepSize, pageSize, new Listener() ) );
			}
			else
			{
				error( "not an integer number" );
			}
		}
	};
	
	
	private int min, max, stepSize, pageSize;
	
	
	public IntCellEditorSpinEntry(LiteralCell cell, int min, int max, int stepSize, int pageSize)
	{
		super( cell );
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.pageSize = pageSize;
	}
	
	
	protected Editor createEditor()
	{
		return new IntEditor();
	}
}
