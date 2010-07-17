//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.CellEditor;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.RealSpinEntry;

public class RealCellEditorSpinEntry extends LiteralCellEditor
{
	protected class RealEditor extends Editor
	{
		protected class Listener implements RealSpinEntry.RealSpinEntryListener
		{
			@Override
			public void onSpinEntryValueChanged(RealSpinEntry spinEntry, double value)
			{
				setCellValue( value );
			}
		}
		
		private RealSpinEntry spinEntry;
		
		public RealEditor()
		{
			Double value = getCellValue( Double.class );
			double v = value != null  ?  value  :  0.0;
			spinEntry = styleSheet.realSpinEntry( v, min, max, stepSize, pageSize, new Listener() );
			if ( value != null )
			{
				setElement( spinEntry.getElement() );
			}
			else
			{
				error( "not a real number" );
			}
		}
		
		protected void refreshEditor()
		{
			Double value = getCellValue( Double.class );
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
	private double min, max, stepSize, pageSize;
	
	
	public RealCellEditorSpinEntry(LiteralCell cell, ControlsStyleSheet styleSheet, double min, double max, double stepSize, double pageSize)
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
		return new RealEditor();
	}
}
