//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class RealNumericLabel extends NumericLabel
{
	public static interface RealNumericLabelListener
	{
		public void onNumericLabelValueChanged(RealNumericLabelControl spinEntry, double value);
	}

	
	
	public static class RealNumericLabelControl extends NumericLabelControl
	{
		private double min, max;
		private RealNumericLabelListener listener;
		
	
		protected RealNumericLabelControl(PresentationContext ctx, StyleValues style, LiveInterface value, LiveInterface text, LiveFunction display, LSElement element,
				double min, double max, RealNumericLabelListener listener)
		{
			super( ctx, style, value, text, display, element );
			
			this.min = min;
			this.max = max;
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
					listener.onNumericLabelValueChanged( this, newValue );
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
				listener.onNumericLabelValueChanged( this, newValue );
			}
		}
		
		protected void onDrag(Object startValue, double delta)
		{
			double start = (Double)startValue;
			changeValue( start + delta );
		}
		
		protected Object storeValue()
		{
			return getValue();
		}
	
	
		@Override
		protected Pattern getValidationPattern()
		{
			return Pattern.compile( "[\\-]?(([0-9]+\\.[0-9]*)|(\\.[0-9]+))(e[\\-]?[0-9]+)?" );
		}
		
		@Override
		protected String getValidationFailMessage()
		{
			return "Please enter a real number.";
		}
	}
	
	
	private static class CommitListener implements RealNumericLabelListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onNumericLabelValueChanged(RealNumericLabelControl spinEntry, double value)
		{
			this.value.setLiteralValue( value );
		}
	}


	
	private double min, max;
	private RealNumericLabelListener listener;
	
	
	private RealNumericLabel(LiveSource valueSource, double min, double max, RealNumericLabelListener listener)
	{
		super( valueSource );
		this.min = min;
		this.max = max;
		this.listener = listener;
	}
	
	public RealNumericLabel(double initialValue, double min, double max, RealNumericLabelListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, listener );
	}
	
	public RealNumericLabel(LiveInterface value, double min, double max, RealNumericLabelListener listener)
	{
		this( new LiveSourceRef( value ), min, max, listener );
	}
	
	public RealNumericLabel(LiveValue value, double min, double max)
	{
		this( new LiveSourceRef( value ), min, max, new CommitListener( value ) );
	}
	
	
	
	@Override
	protected NumericLabelControl createNumericLabelControl(PresentationContext ctx, StyleValues style, LiveInterface value, LiveInterface text, LiveFunction display, LSElement element)
	{
		return new RealNumericLabelControl( ctx, style, value, text, display, element, min, max, listener );
	}
}
