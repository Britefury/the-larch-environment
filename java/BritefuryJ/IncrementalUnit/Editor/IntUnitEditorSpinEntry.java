//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.IncrementalUnit.Editor;

import BritefuryJ.Controls.IntSpinEntry;
import BritefuryJ.IncrementalUnit.LiteralUnit;

public class IntUnitEditorSpinEntry extends LiteralUnitEditor
{
	protected class IntEditor extends Editor
	{
		protected class Listener implements IntSpinEntry.IntSpinEntryListener
		{
			public void onSpinEntryValueChanged(IntSpinEntry.IntSpinEntryControl spinEntry, int value)
			{
				setUnitValue( value );
			}
		}
		
		public IntEditor()
		{
			Integer value = getUnitValue( Integer.class );
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
			Integer value = getUnitValue( Integer.class );
			if ( value != null )
			{
				setPres( new IntSpinEntry( value, min, max, stepSize, pageSize, new Listener() ) );
			}
			else
			{
				error( "not an integer number" );
			}
		}
	}
	
	
	private int min, max, stepSize, pageSize;
	
	
	public IntUnitEditorSpinEntry(LiteralUnit cell, int min, int max, int stepSize, int pageSize)
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
