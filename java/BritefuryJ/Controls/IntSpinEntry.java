//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class IntSpinEntry extends SpinEntry
{
	public static interface IntSpinEntryListener
	{
		public void onSpinEntryValueChanged(IntSpinEntryControl spinEntry, int value);
	}

	
	
	public static class IntSpinEntryControl extends SpinEntryControl
	{
		private int value, min, max, stepSize, pageSize;
		private IntSpinEntryListener listener;
		
	
		protected IntSpinEntryControl(PresentationContext ctx, StyleValues style, DPElement element, TextEntry.TextEntryControl textEntry,
				DPElement upSpinButton, DPElement downSpinButton, SpinEntryTextListener textListener,
				int value, int min, int max, int stepSize, int pageSize, IntSpinEntryListener listener)
		{
			super( ctx, style, element, textEntry, upSpinButton, downSpinButton, textListener );
			
			this.value = value;
			this.min = min;
			this.max = max;
			this.stepSize = stepSize;
			this.pageSize = pageSize;
			this.listener = listener;
			
			element.setFixedValue( value );
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
				element.setFixedValue( value );
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
				element.setFixedValue( value );
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
		
		protected void onDrag(Object startValue, double delta)
		{
			Integer start = (Integer)startValue;
			setValue( start + (int)( delta + 0.5 ) );
		}
		
		protected Object storeValue()
		{
			return value;
		}
	}


	
	private int initialValue, min, max, stepSize, pageSize;
	private IntSpinEntryListener listener;
	
	
	public IntSpinEntry(int initialValue, int min, int max, int stepSize, int pageSize, IntSpinEntryListener listener)
	{
		this.initialValue = initialValue;
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.pageSize = pageSize;
		this.listener = listener;
	}
	
	
	
	protected String getInitialValueString()
	{
		return String.valueOf( initialValue );
	}
	
	protected Pattern getValidationPattern()
	{
		return Pattern.compile( "[\\-]?[0-9]+" );
	}
	
	protected String getValidationFailMessage()
	{
		return "Please enter an integer.";
	}
	
	
	protected SpinEntryControl createSpinEntryControl(PresentationContext ctx, StyleValues style, DPElement element, TextEntry.TextEntryControl entryControl, DPElement upArrow,
			DPElement downArrow, SpinEntryControl.SpinEntryTextListener textListener)
	{
		return new IntSpinEntryControl( ctx, style, element, entryControl, upArrow, downArrow, textListener, initialValue, min, max, stepSize, pageSize, listener );
	}
}
