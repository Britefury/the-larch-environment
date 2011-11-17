//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class RealSpinEntry extends SpinEntry
{
	public static interface RealSpinEntryListener
	{
		public void onSpinEntryValueChanged(RealSpinEntryControl spinEntry, double value);
	}

	public static class RealSpinEntryControl extends SpinEntryControl
	{
		private double min, max, stepSize, pageSize;
		private RealSpinEntryListener listener;
		
	
		protected RealSpinEntryControl(PresentationContext ctx, StyleValues style, LiveInterface value, DPElement element, TextEntry.TextEntryControl textEntry,
				DPElement upSpinButton, DPElement downSpinButton, SpinEntryTextListener textListener,
				double min, double max, double stepSize, double pageSize, RealSpinEntryListener listener)
		{
			super( ctx, style, value, element, textEntry, upSpinButton, downSpinButton, textListener );
			
			this.min = min;
			this.max = max;
			this.stepSize = stepSize;
			this.pageSize = pageSize;
			this.listener = listener;
	
			element.setFixedValue( value.elementValueFunction() );
		}
		
		
		
		public double getValue()
		{
			return (Double)value.getStaticValue();
		}
		
		private void changeValue(double newValue)
		{
			double currentValue = getValue();
			newValue = Math.min( Math.max( newValue, min ), max );
			if ( newValue != currentValue )
			{
				value.setLiteralValue( newValue );
				if ( listener != null )
				{
					listener.onSpinEntryValueChanged( this, newValue );
				}
			}
		}
		
		
		protected void onTextChanged(String text)
		{
			double currentValue = getValue();
			double textValue = Double.valueOf( text );
			double newValue = Math.min( Math.max( textValue, min ), max );
			if ( newValue != currentValue )
			{
				value.setLiteralValue( newValue );
				if ( listener != null )
				{
					listener.onSpinEntryValueChanged( this, newValue );
				}
			}
		}
		
		protected void onStep(boolean bUp)
		{
			changeValue( getValue()   +   ( bUp  ?  stepSize  :  -stepSize ) ); 
		}
		
		protected void onPage(boolean bUp)
		{
			changeValue( getValue()   +   ( bUp  ?  pageSize  :  -pageSize ) ); 
		}
		
		protected void onDrag(Object startValue, double delta)
		{
			Double start = (Double)startValue;
			changeValue( start + delta );
		}
		
		protected Object storeValue()
		{
			return getValue();
		}
	}
	
	
	private static class CommitListener implements RealSpinEntryListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onSpinEntryValueChanged(RealSpinEntryControl spinEntry, double value)
		{
			this.value.setLiteralValue( value );
		}
	}


	
	private double min, max, stepSize, pageSize;
	private RealSpinEntryListener listener;
	
	
	private RealSpinEntry(LiveSource valueSource, double min, double max, double stepSize, double pageSize, RealSpinEntryListener listener)
	{
		super( valueSource );
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.pageSize = pageSize;
		this.listener = listener;
	}
	
	public RealSpinEntry(double initialValue, double min, double max, double stepSize, double pageSize, RealSpinEntryListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, stepSize, pageSize, listener );
	}
	
	public RealSpinEntry(LiveInterface value, double min, double max, double stepSize, double pageSize, RealSpinEntryListener listener)
	{
		this( new LiveSourceRef( value ), min, max, stepSize, pageSize, listener );
	}
	
	public RealSpinEntry(LiveValue value, double min, double max, double stepSize, double pageSize)
	{
		this( new LiveSourceRef( value ), min, max, stepSize, pageSize, new CommitListener( value ) );
	}
	
	
	
	protected Pattern getValidationPattern()
	{
		return Pattern.compile( "[\\-]?(([0-9]+\\.[0-9]*)|(\\.[0-9]+))(e[\\-]?[0-9]+)?" );
	}
	
	protected String getValidationFailMessage()
	{
		return "Please enter a real number.";
	}
	
	
	@Override
	protected SpinEntryControl createSpinEntryControl(PresentationContext ctx, StyleValues style, LiveInterface value, DPElement element, TextEntry.TextEntryControl entryControl, DPElement upArrow,
			DPElement downArrow, SpinEntryControl.SpinEntryTextListener textListener)
	{
		return new RealSpinEntryControl( ctx, style, value, element, entryControl, upArrow, downArrow, textListener, min, max, stepSize, pageSize, listener );
	}
}
