//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.DragElementInteractor;
import BritefuryJ.LSpace.Interactor.HoverElementInteractor;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class Slider extends ControlPres
{
	public abstract static class SliderControl extends Control implements IncrementalMonitorListener
	{
		protected class SliderInteractor implements ElementPainter, HoverElementInteractor, DragElementInteractor
		{
			@Override
			public void drawBackground(LSElement element, Graphics2D graphics)
			{
			}

			@Override
			public void draw(LSElement element, Graphics2D graphics)
			{
				Vector2 size = element.getActualSize();
				
				Shape backgroundShape = new RoundRectangle2D.Double( 0.0, 0.0, size.x, size.y, rounding, rounding );
				
				Shape clipShape = graphics.getClip();
				Stroke stroke = graphics.getStroke();
				Paint paint = graphics.getPaint();
				
				
				graphics.clip( backgroundShape );
				
				
				double min = getSliderMin();
				double max = getSliderMax();
				double pivot = getSliderPivot();
				double val = getSliderValue();
				
				
				double pivotFrac = ( pivot - min )  /  ( max - min );
				double pivotPos = size.x * pivotFrac;

				
				// Draw value
				double valueFrac = ( val - min ) / ( max - min );
				double valuePos = size.x * valueFrac;
				
				double x = Math.min( pivotPos, valuePos );
				double w = Math.abs( valuePos - pivotPos );
				Shape valueBoxShape = new Rectangle2D.Double( x, 0.0, w, size.y );
				valueBoxPainter.drawShape( graphics, valueBoxShape );

                Shape valueShape = new Arc2D.Double(valuePos - size.y*0.5, 0.0, size.y, size.y, 0.0, 360.0, Arc2D.CHORD);
                valuePainter.drawShape( graphics, valueShape );


				// Draw pivot
				if ( pivot != min  &&  pivot != max )
				{
					Shape pivotShape = new Line2D.Double( pivotPos, 0.0, pivotPos, size.y );
					graphics.setPaint( pivotPaint );
					graphics.setStroke( new BasicStroke( 1.0f ) );
					graphics.draw( pivotShape );
				}
				
				
				graphics.setPaint( paint );
				graphics.setStroke( stroke );
				graphics.setClip( clipShape );
			
				if ( element.isHoverActive() )
				{
					backgroundHoverPainter.drawShape( graphics, backgroundShape );
				}
				else
				{
					backgroundPainter.drawShape( graphics, backgroundShape );
				}
			}

			
			
			@Override
			public void pointerEnter(LSElement element, PointerMotionEvent event)
			{
				element.queueFullRedraw();
			}

			@Override
			public void pointerLeave(LSElement element, PointerMotionEvent event)
			{
				element.queueFullRedraw();
			}

			
			
			@Override
			public boolean dragBegin(LSElement element, PointerButtonEvent event)
			{
				if ( event.getButton() == 1 )
				{
					change( element, event.getLocalPointerPos() );
					return true;
				}
				else
				{
					return false;
				}
			}

			@Override
			public void dragEnd(LSElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
			{
				change( element, event.getLocalPointerPos() );
			}

			@Override
			public void dragMotion(LSElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
			{
				change( element, event.getLocalPointerPos() );
			}
			
			
			private void change(LSElement element, Point2 pos)
			{
				Vector2 size = element.getActualSize();

				double valueFrac = pos.x / size.x;
                double step = getSliderStep();
				
				valueFrac = Math.min( Math.max( valueFrac, 0.0 ), 1.0 );
				
				double min = getSliderMin();
				double max = getSliderMax();
                double valueOffset = (max - min) * valueFrac;

                if (step != 0.0) {
                    double steps = Math.round(valueOffset / step);
                    valueOffset = steps * step;
                }

                double value = min + valueOffset;

				changeValue( value );
			}
		}
		
		
		
		protected LiveInterface value;
		
		protected LSElement element;
		
		private Painter backgroundPainter, backgroundHoverPainter, valueBoxPainter, valuePainter;
		private Paint pivotPaint;
		private double rounding; 
		
		
		
		public SliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
				Paint pivotPaint, Painter valueBoxPainter, Painter valuePainter, double rounding)
		{
			super( ctx, style );
			
			this.value = value;
			this.value.addListener(this);

			this.element = element;
			this.backgroundPainter = backgroundPainter;
			this.backgroundHoverPainter = backgroundHoverPainter;
			this.pivotPaint = pivotPaint;
            this.valueBoxPainter = valueBoxPainter;
			this.valuePainter = valuePainter;
			this.rounding = rounding;

			SliderInteractor sliderInteractor = new SliderInteractor();
			element.addElementInteractor( sliderInteractor );
			element.addPainter( sliderInteractor );
			
			element.setFixedValue( value.elementValueFunction() );
		}
		
		
		public LiveInterface getValue()
		{
			return value;
		}
		
		
		@Override
		public LSElement getElement()
		{
			return element;
		}
		
		
		
		protected abstract double getSliderMin();
		protected abstract double getSliderMax();
		protected abstract double getSliderStep();
		protected abstract double getSliderPivot();
		protected abstract double getSliderValue();
		protected abstract void changeValue(double value);

	
	



		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			// Use getValue() so that @state reports further value changes
			Object val = value.getValue();
			
			element.setFixedValue( val );

			element.queueFullRedraw();
		}
	}
	
	
	private LiveSource valueSource;
	private double width;
	
	public Slider(LiveSource valueSource, double width)
	{
		this.valueSource = valueSource;
		this.width = width;
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		LiveInterface value = valueSource.getLive();

		Painter backgroundPainter = style.get( Controls.sliderBackgroundPainter, Painter.class );
		Painter backgroundHoverPainter = style.get( Controls.sliderBackgroundHoverPainter, Painter.class );
		Paint pivotPaint = style.get( Controls.sliderPivotPaint, Paint.class );
		Painter valueBoxPainter = style.get( Controls.sliderValueBoxPainter, Painter.class );
		Painter valuePainter = style.get( Controls.sliderValuePainter, Painter.class );
		double rounding = style.get( Controls.sliderRounding, Double.class );
		double size = style.get( Controls.sliderSize, Double.class ); 
		
		double w = width > 0.0  ?  width  :  size;

		Pres slider = boxStyle.applyTo( new Spacer( w, size ).alignHExpand().alignVExpand() );
		
		
		LSElement element = slider.present( ctx, style );
		
		return createSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                pivotPaint, valueBoxPainter, valuePainter, rounding );
	}
	
	private static final StyleSheet boxStyle = StyleSheet.style( Primitive.shapePainter.as( null ), Primitive.hoverShapePainter.as( null ) );
	
	
	protected abstract SliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
			Paint pivotPaint, Painter valueBoxPainter, Painter valuePainter, double rounding);
}
