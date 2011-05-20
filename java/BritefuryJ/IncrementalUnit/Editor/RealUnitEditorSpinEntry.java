//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.IncrementalUnit.Editor;

import BritefuryJ.Controls.RealSpinEntry;
import BritefuryJ.IncrementalUnit.LiteralUnit;

public class RealUnitEditorSpinEntry extends LiteralUnitEditor
{
	protected class RealEditor extends Editor
	{
		protected class Listener implements RealSpinEntry.RealSpinEntryListener
		{
			@Override
			public void onSpinEntryValueChanged(RealSpinEntry.RealSpinEntryControl spinEntry, double value)
			{
				setCellValue( value );
			}
		}
		
		public RealEditor()
		{
			Double value = getCellValue( Double.class );
			double v = value != null  ?  value  :  0.0;
			
			if ( value != null )
			{
				setPres( new RealSpinEntry( v, min, max, stepSize, pageSize, new Listener() ) );
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
				setPres( new RealSpinEntry( value, min, max, stepSize, pageSize, new Listener() ) );
			}
			else
			{
				error( "not a string" );
			}
		}
	}


	private double min, max, stepSize, pageSize;
	
	
	public RealUnitEditorSpinEntry(LiteralUnit cell, double min, double max, double stepSize, double pageSize)
	{
		super( cell );
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
