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

public class IntSpinEntry extends SpinEntry
{
	public static interface IntSpinEntryListener
	{
		public void onSpinEntryValueChanged(IntSpinEntryControl spinEntry, int value);
	}

	
	
	public static class IntSpinEntryControl extends SpinEntryControl
	{
		private int min, max, stepSize, pageSize;
		private IntSpinEntryListener listener;
		
	
		protected IntSpinEntryControl(PresentationContext ctx, StyleValues style, LiveInterface value, DPElement element, TextEntry.TextEntryControl textEntry,
				DPElement upSpinButton, DPElement downSpinButton, SpinEntryTextListener textListener,
				int min, int max, int stepSize, int pageSize, IntSpinEntryListener listener)
		{
			super( ctx, style, value, element, textEntry, upSpinButton, downSpinButton, textListener );
			
			this.min = min;
			this.max = max;
			this.stepSize = stepSize;
			this.pageSize = pageSize;
			this.listener = listener;
			
			element.setFixedValue( value.elementValueFunction() );
		}
		
		
		
		public int getValue()
		{
			return (Integer)value.getStaticValue();
		}
		
		
		private void changeValue(int newValue)
		{
			int currentValue = getValue();
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
			int currentValue = getValue();
			int textValue = Integer.valueOf( text );
			int newValue = Math.min( Math.max( textValue, min ), max );
			if ( newValue != currentValue )
			{
				value.setLiteralValue( newValue );
				listener.onSpinEntryValueChanged( this, newValue );
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
			Integer start = (Integer)startValue;
			changeValue( start + (int)( delta + 0.5 ) );
		}
		
		protected Object storeValue()
		{
			return getValue();
		}
	}
	
	
	private static class CommitListener implements IntSpinEntryListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onSpinEntryValueChanged(IntSpinEntryControl spinEntry, int value)
		{
			this.value.setLiteralValue( value );
		}
	}


	
	private int min, max, stepSize, pageSize;
	private IntSpinEntryListener listener;
	
	
	private IntSpinEntry(LiveSource valueSource, int min, int max, int stepSize, int pageSize, IntSpinEntryListener listener)
	{
		super( valueSource );
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.pageSize = pageSize;
		this.listener = listener;
	}
	
	public IntSpinEntry(int initialValue, int min, int max, int stepSize, int pageSize, IntSpinEntryListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, stepSize, pageSize, listener );
	}
	
	public IntSpinEntry(LiveInterface value, int min, int max, int stepSize, int pageSize, IntSpinEntryListener listener)
	{
		this( new LiveSourceRef( value ), min, max, stepSize, pageSize, listener );
	}
	
	public IntSpinEntry(LiveValue value, int min, int max, int stepSize, int pageSize)
	{
		this( new LiveSourceRef( value ), min, max, stepSize, pageSize, new CommitListener( value ) );
	}
	
	
	
	protected Pattern getValidationPattern()
	{
		return Pattern.compile( "[\\-]?[0-9]+" );
	}
	
	protected String getValidationFailMessage()
	{
		return "Please enter an integer.";
	}
	
	
	@Override
	protected SpinEntryControl createSpinEntryControl(PresentationContext ctx, StyleValues style, LiveInterface value, DPElement element, TextEntry.TextEntryControl entryControl, DPElement upArrow,
			DPElement downArrow, SpinEntryControl.SpinEntryTextListener textListener)
	{
		return new IntSpinEntryControl( ctx, style, value, element, entryControl, upArrow, downArrow, textListener, min, max, stepSize, pageSize, listener );
	}
}
