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

public class IntSlider extends NumericSlider
{
	public static interface IntSliderListener
	{
		public void onSliderValueChanged(IntSliderControl spinEntry, int value);
	}

	public static class IntSliderControl extends NumericSliderControl
	{
		private int min, max, step, pivot;
		private IntSliderListener listener;
		
	
		protected IntSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
				Paint pivotPaint, Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter, double rounding, int min, int max, int step, int pivot, IntSliderListener listener)
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
			Integer val = (Integer)value.getStaticValue();
			return val;
		}

		
		protected void changeValue(double newValue)
		{
			int currentValue = (Integer)value.getStaticValue();
            double offset = newValue - min;
            int intOffset = (int)(offset + 0.5);
			int val = Math.min( Math.max( min+intOffset, min ), max );
			if ( val != currentValue )
			{
				//value.setLiteralValue( val );
				if ( listener != null )
				{
					listener.onSliderValueChanged( this, val );
				}
			}
		}
	}
	
	
	private static class CommitListener implements IntSliderListener
	{
		private LiveValue value;
		
		public CommitListener(LiveValue value)
		{
			this.value = value;
		}
		
		@Override
		public void onSliderValueChanged(IntSliderControl spinEntry, int value)
		{
			this.value.setLiteralValue( value );
		}
	}


	
	private int min, max, step, pivot;
	private IntSliderListener listener;
	
	
	private IntSlider(LiveSource valueSource, int min, int max, int step, int pivot, double width, IntSliderListener listener)
	{
		super( valueSource, width );
		this.min = min;
		this.max = max;
		this.step = step;
		this.pivot = pivot;
		this.listener = listener;
	}
	
	public IntSlider(int initialValue, int min, int max, int step, int pivot, double width, IntSliderListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, step, pivot, width, listener );
	}
	
	public IntSlider(LiveInterface value, int min, int max, int step, int pivot, double width, IntSliderListener listener)
	{
		this( new LiveSourceRef( value ), min, max, step, pivot, width, listener );
	}
	
	public IntSlider(LiveValue value, int min, int max, int step, int pivot, double width)
	{
		this( new LiveSourceRef( value ), min, max, step, pivot, width, new CommitListener( value ) );
	}
	
	
	
	@Override
	protected AbstractSliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
			Paint pivotPaint, Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter, double rounding)
	{
		return new IntSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                pivotPaint, valueBoxPainter, valuePainter, valueHighlightPainter, rounding, min, max, step, pivot, listener );
	}



    protected static int convertDoubleToInt(double x, int min, int max) {
        double offset = x - min;
        int intOffset = (int)(offset + 0.5);
        return Math.min( Math.max( min+intOffset, min ), max );
    }
}
