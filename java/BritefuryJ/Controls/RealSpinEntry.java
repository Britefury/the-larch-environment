//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class RealSpinEntry extends SpinEntry
{
	public static interface RealSpinEntryListener
	{
		public void onSpinEntryValueChanged(RealSpinEntryControl spinEntry, double value);
	}

	public static class RealSpinEntryControl extends SpinEntryControl
	{
		private double value, min, max, stepSize, pageSize;
		private RealSpinEntryListener listener;
		
	
		protected RealSpinEntryControl(PresentationContext ctx, StyleValues style, DPElement element, TextEntry.TextEntryControl textEntry,
				DPElement upSpinButton, DPElement downSpinButton, SpinEntryTextListener textListener,
				double value, double min, double max, double stepSize, double pageSize, RealSpinEntryListener listener)
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
				element.setFixedValue( value );
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
			Double start = (Double)startValue;
			setValue( start + delta );
		}
		
		protected Object storeValue()
		{
			return value;
		}
	}


	
	private double initialValue, min, max, stepSize, pageSize;
	private RealSpinEntryListener listener;
	
	
	public RealSpinEntry(double initialValue, double min, double max, double stepSize, double pageSize, RealSpinEntryListener listener)
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
		return Pattern.compile( "[\\-]?(([0-9]+\\.[0-9]*)|(\\.[0-9]+))(e[\\-]?[0-9]+)?" );
	}
	
	protected String getValidationFailMessage()
	{
		return "Please enter a real number.";
	}
	
	
	protected SpinEntryControl createSpinEntryControl(PresentationContext ctx, StyleValues style, DPElement element, TextEntry.TextEntryControl entryControl, DPElement upArrow,
			DPElement downArrow, SpinEntryControl.SpinEntryTextListener textListener)
	{
		return new RealSpinEntryControl( ctx, style, element, entryControl, upArrow, downArrow, textListener, initialValue, min, max, stepSize, pageSize, listener );
	}
}
