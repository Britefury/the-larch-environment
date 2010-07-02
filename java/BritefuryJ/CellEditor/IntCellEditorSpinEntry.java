//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.CellEditor;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.IntSpinEntry;

public class IntCellEditorSpinEntry extends LiteralCellEditor
{
	protected class IntEditor extends Editor
	{
		protected class Listener implements IntSpinEntry.IntSpinEntryListener
		{
			@Override
			public void onSpinEntryValueChanged(IntSpinEntry spinEntry, int value)
			{
				setCellValue( value );
			}
		}
		
		private IntSpinEntry spinEntry;
		
		public IntEditor()
		{
			Integer value = getCellValue( Integer.class );
			int v = value != null  ?  value  :  0;
			spinEntry = styleSheet.intSpinEntry( v, min, max, stepSize, pageSize, new Listener() );
			if ( value != null )
			{
				setElement( spinEntry.getElement() );
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
				spinEntry.setValue( value );
			}
			else
			{
				error( "not a string" );
			}
		}
	};
	
	
	private ControlsStyleSheet styleSheet;
	private int min, max, stepSize, pageSize;
	
	
	public IntCellEditorSpinEntry(LiteralCell cell, ControlsStyleSheet styleSheet, int min, int max, int stepSize, int pageSize)
	{
		super( cell );
		this.styleSheet = styleSheet;
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
