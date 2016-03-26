//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.awt.Paint;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class RealSlider extends NumericSlider
{
	public static interface RealSliderListener
	{
		public void onSliderValueChanged(RealSliderControl spinEntry, double value);
	}

	public static class RealSliderControl extends NumericSliderControl
	{
		private double min, max, step;
		private RealSliderListener listener;
		
	
		protected RealSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
				Painter valuePainter, Painter valueHighlightPainter, double rounding, double min, double max, double step, RealSliderListener listener)
		{
			super( ctx, style, value, element, backgroundPainter, backgroundHoverPainter, valuePainter, valueHighlightPainter, rounding );
			
			this.min = min;
			this.max = max;
			this.step = step;
			this.listener = listener;
	
			element.setFixedValue( value.elementValueFunction() );
		}
		
		
		
		protected double getSliderMin()
		{
			return min;
		}
		
		protected double getSliderMax()
		{
			return max;
		}
		
		protected double getSliderStep()
		{
			return step;
		}
		
		protected double getSliderValue()
		{
			return (Double)value.getStaticValue();
		}

		
		protected void changeValue(double newValue)
		{
			double currentValue = getSliderValue();
			newValue = Math.min( Math.max( newValue, min ), max );
			if ( newValue != currentValue )
			{
				value.setLiteralValue( newValue );
				if ( listener != null )
				{
					listener.onSliderValueChanged( this, newValue );
				}
			}
		}
	}
	
	
	private static class CommitListener implements RealSliderListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onSliderValueChanged(RealSliderControl spinEntry, double value)
		{
			this.value.setLiteralValue( value );
		}
	}


	
	private double min, max, step;
	private RealSliderListener listener;
	
	
	private RealSlider(LiveSource valueSource, double min, double max, double step, double width, RealSliderListener listener)
	{
		super( valueSource, width );
		this.min = min;
		this.max = max;
		this.step = step;
		this.listener = listener;
	}
	
	public RealSlider(double initialValue, double min, double max, double step, double width, RealSliderListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, step, width, listener );
	}
	
	public RealSlider(LiveInterface value, double min, double max, double step, double width, RealSliderListener listener)
	{
		this( new LiveSourceRef( value ), min, max, step, width, listener );
	}
	
	public RealSlider(LiveValue value, double min, double max, double step, double width)
	{
		this( new LiveSourceRef( value ), min, max, step, width, new CommitListener( value ) );
	}
	
	
	
	@Override
	protected AbstractSliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
			Painter valuePainter, Painter valueHighlightPainter, double rounding)
	{
		return new RealSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valuePainter, valueHighlightPainter, rounding, min, max, step, listener );
	}
}
