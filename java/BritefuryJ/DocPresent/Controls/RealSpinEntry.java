//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPElement;

public class RealSpinEntry extends SpinEntry
{
	public static interface RealSpinEntryListener
	{
		public void onSpinEntryValueChanged(RealSpinEntry spinEntry, double value);
	}

	
	
	private double value, min, max, stepSize, pageSize;
	private RealSpinEntryListener listener;
	

	protected RealSpinEntry(DPElement element, TextEntry textEntry, DPElement upSpinButton, DPElement downSpinButton, SpinEntryTextListener textListener,
			double value, double min, double max, double stepSize, double pageSize, RealSpinEntryListener listener)
	{
		super( element, textEntry, upSpinButton, downSpinButton, textListener );
		
		this.value = value;
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.pageSize = pageSize;
		this.listener = listener;
	}
	
	
	
	public double getValue()
	{
		return value;
	}
	
	public void setValue(double newValue)
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
		double textValue = Double.valueOf( text );
		double newValue = Math.min( Math.max( textValue, min ), max );
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
