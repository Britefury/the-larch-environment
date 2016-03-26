//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.LSpace.LSElement;
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
		private int min, max, stepSize, jumpSize;
		private IntSpinEntryListener listener;
		
	
		protected IntSpinEntryControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, TextEntry.TextEntryControl textEntry,
				LSElement upSpinButton, LSElement downSpinButton, SpinEntryTextListener textListener,
				int min, int max, int stepSize, int jumpSize, IntSpinEntryListener listener)
		{
			super( ctx, style, value, element, textEntry, upSpinButton, downSpinButton, textListener );
			
			this.min = min;
			this.max = max;
			this.stepSize = stepSize;
			this.jumpSize = jumpSize;
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
		
		protected void onJump(boolean bUp)
		{
			changeValue( getValue()   +   ( bUp  ? jumpSize :  -jumpSize) );
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


	
	private int min, max, stepSize, jumpSize;
	private IntSpinEntryListener listener;
	
	
	private IntSpinEntry(LiveSource valueSource, int min, int max, int stepSize, int jumpSize, IntSpinEntryListener listener)
	{
		super( valueSource );
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.jumpSize = jumpSize;
		this.listener = listener;
	}
	
	public IntSpinEntry(int initialValue, int min, int max, int stepSize, int jumpSize, IntSpinEntryListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, stepSize, jumpSize, listener );
	}
	
	public IntSpinEntry(LiveInterface value, int min, int max, int stepSize, int jumpSize, IntSpinEntryListener listener)
	{
		this( new LiveSourceRef( value ), min, max, stepSize, jumpSize, listener );
	}
	
	public IntSpinEntry(LiveValue value, int min, int max, int stepSize, int jumpSize)
	{
		this( new LiveSourceRef( value ), min, max, stepSize, jumpSize, new CommitListener( value ) );
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
	protected SpinEntryControl createSpinEntryControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, TextEntry.TextEntryControl entryControl, LSElement upArrow,
			LSElement downArrow, SpinEntryControl.SpinEntryTextListener textListener)
	{
		return new IntSpinEntryControl( ctx, style, value, element, entryControl, upArrow, downArrow, textListener, min, max, stepSize, jumpSize, listener );
	}
}
