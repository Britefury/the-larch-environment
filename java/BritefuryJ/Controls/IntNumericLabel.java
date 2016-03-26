//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class IntNumericLabel extends NumericLabel
{
	public static interface IntNumericLabelListener
	{
		public void onNumericLabelValueChanged(IntNumericLabelControl spinEntry, int value);
	}

	
	
	public static class IntNumericLabelControl extends NumericLabelControl
	{
		private int min, max;
		private IntNumericLabelListener listener;
		
	
		protected IntNumericLabelControl(PresentationContext ctx, StyleValues style, LiveInterface value, LiveInterface text, LiveFunction display, LSElement element,
				int min, int max, IntNumericLabelListener listener)
		{
			super( ctx, style, value, text, display, element );
			
			this.min = min;
			this.max = max;
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
					listener.onNumericLabelValueChanged( this, newValue );
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
				listener.onNumericLabelValueChanged( this, newValue );
			}
		}
		
		protected void onDrag(Object startValue, double delta)
		{
			int start = (Integer)startValue;
			changeValue( start + (int)( delta + 0.5 ) );
		}
		
		protected Object storeValue()
		{
			return getValue();
		}
	
	
		@Override
		protected Pattern getValidationPattern()
		{
			return Pattern.compile( "[\\-]?[0-9]+" );
		}
		
		@Override
		protected String getValidationFailMessage()
		{
			return "Please enter an integer.";
		}
	}
	
	
	private static class CommitListener implements IntNumericLabelListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onNumericLabelValueChanged(IntNumericLabelControl spinEntry, int value)
		{
			this.value.setLiteralValue( value );
		}
	}


	
	private int min, max;
	private IntNumericLabelListener listener;
	
	
	private IntNumericLabel(LiveSource valueSource, int min, int max, IntNumericLabelListener listener)
	{
		super( valueSource );
		this.min = min;
		this.max = max;
		this.listener = listener;
	}
	
	public IntNumericLabel(int initialValue, int min, int max, IntNumericLabelListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, listener );
	}
	
	public IntNumericLabel(LiveInterface value, int min, int max, IntNumericLabelListener listener)
	{
		this( new LiveSourceRef( value ), min, max, listener );
	}
	
	public IntNumericLabel(LiveValue value, int min, int max)
	{
		this( new LiveSourceRef( value ), min, max, new CommitListener( value ) );
	}
	
	
	
	@Override
	protected NumericLabelControl createNumericLabelControl(PresentationContext ctx, StyleValues style, LiveInterface value, LiveInterface text, LiveFunction display, LSElement element)
	{
		return new IntNumericLabelControl( ctx, style, value, text, display, element, min, max, listener );
	}
}
