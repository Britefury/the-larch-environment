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

public class IntSlider extends Slider
{
	public static interface IntSliderListener
	{
		public void onSliderValueChanged(IntSliderControl spinEntry, int value);
	}

	public static class IntSliderControl extends SliderControl
	{
		private int min, max, pivot;
		private IntSliderListener listener;
		
	
		protected IntSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
				Paint pivotPaint, Painter valuePainter, double rounding, int min, int max, int pivot, IntSliderListener listener)
		{
			super( ctx, style, value, element, backgroundPainter, backgroundHoverPainter, pivotPaint, valuePainter, rounding );
			
			this.min = min;
			this.max = max;
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
			int val = Math.min( Math.max( (int)( newValue + 0.5 ), min ), max );
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


	
	private int min, max, pivot;
	private IntSliderListener listener;
	
	
	private IntSlider(LiveSource valueSource, int min, int max, int pivot, double width, IntSliderListener listener)
	{
		super( valueSource, width );
		this.min = min;
		this.max = max;
		this.pivot = pivot;
		this.listener = listener;
	}
	
	public IntSlider(int initialValue, int min, int max, int pivot, double width, IntSliderListener listener)
	{
		this( new LiveSourceValue( initialValue ), min, max, pivot, width, listener );
	}
	
	public IntSlider(LiveInterface value, int min, int max, int pivot, double width, IntSliderListener listener)
	{
		this( new LiveSourceRef( value ), min, max, pivot, width, listener );
	}
	
	public IntSlider(LiveValue value, int min, int max, int pivot, double width)
	{
		this( new LiveSourceRef( value ), min, max, pivot, width, new CommitListener( value ) );
	}
	
	
	
	@Override
	protected SliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
			Paint pivotPaint, Painter valuePainter, double rounding)
	{
		return new IntSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter, pivotPaint, valuePainter, rounding, min, max, pivot, listener );
	}
}
