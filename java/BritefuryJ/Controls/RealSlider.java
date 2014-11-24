//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
		private double min, max, step, pivot;
		private RealSliderListener listener;
		
	
		protected RealSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
				Paint pivotPaint, Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter, double rounding, double min, double max, double step, double pivot, RealSliderListener listener)
		{
			super( ctx, style, value, element, backgroundPainter, backgroundHoverPainter, pivotPaint, valueBoxPainter, valuePainter, valueHighlightPainter, rounding );
			
			this.min = min;
			this.max = max;
			this.step = step;
			this.pivot = pivot;
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
		
		protected double getSliderPivot()
		{
			return pivot;
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


	
	private double min, max, step, pivot;
	private RealSliderListener listener;
	
	
	private RealSlider(LiveSource valueSource, double min, double max, double step, double pivot, double width, RealSliderListener listener)
	{
		super( valueSource, width );
		this.min = min;
		this.max = max;
		this.step = step;
		this.pivot = pivot;
		this.listener = listener;
	}
	
	public RealSlider(double initialValue, double min, double max, double step, double pivot, double width, RealSliderListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, step, pivot, width, listener );
	}
	
	public RealSlider(LiveInterface value, double min, double max, double step, double pivot, double width, RealSliderListener listener)
	{
		this( new LiveSourceRef( value ), min, max, step, pivot, width, listener );
	}
	
	public RealSlider(LiveValue value, double min, double max, double step, double pivot, double width)
	{
		this( new LiveSourceRef( value ), min, max, step, pivot, width, new CommitListener( value ) );
	}
	
	
	
	@Override
	protected AbstractSliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
			Paint pivotPaint, Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter, double rounding)
	{
		return new RealSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                pivotPaint, valueBoxPainter, valuePainter, valueHighlightPainter, rounding, min, max, step, pivot, listener );
	}
}
