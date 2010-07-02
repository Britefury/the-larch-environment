//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPElement;

public class IntSpinEntry extends SpinEntry
{
	public static interface IntSpinEntryListener
	{
		public void onSpinEntryValueChanged(IntSpinEntry spinEntry, int value);
	}

	
	
	private int value, min, max, stepSize, pageSize;
	private IntSpinEntryListener listener;
	

	protected IntSpinEntry(DPElement element, TextEntry textEntry, DPElement upSpinButton, DPElement downSpinButton, SpinEntryTextListener textListener,
			int value, int min, int max, int stepSize, int pageSize, IntSpinEntryListener listener)
	{
		super( element, textEntry, upSpinButton, downSpinButton, textListener );
		
		this.value = value;
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.pageSize = pageSize;
		this.listener = listener;
	}
	
	
	
	public int getValue()
	{
		return value;
	}
	
	public void setValue(int newValue)
	{
		newValue = Math.min( Math.max( newValue, min ), max );
		if ( newValue != value )
		{
			value = newValue;
			textEntry.setText( String.valueOf( value ) );
			listener.onSpinEntryValueChanged( this, value );
		}
	}
	
	
	protected void onTextChanged(String text)
	{
		int textValue = Integer.valueOf( text );
		int newValue = Math.min( Math.max( textValue, min ), max );
		if ( newValue != value )
		{
			value = newValue;
			listener.onSpinEntryValueChanged( this, value );
		}
		if ( value != textValue )
		{
			textEntry.setText( String.valueOf( value ) );
		}
	}
	
	protected void onStep(boolean bUp)
	{
		setValue( value   +   ( bUp  ?  stepSize  :  -stepSize ) ); 
	}
	
	protected void onPage(boolean bUp)
	{
		setValue( value   +   ( bUp  ?  pageSize  :  -pageSize ) ); 
	}
}
