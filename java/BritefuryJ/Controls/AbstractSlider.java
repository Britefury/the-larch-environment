//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2014.
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

public abstract class AbstractSlider extends ControlPres
{
	public abstract static class AbstractSliderControl extends Control implements IncrementalMonitorListener
	{
		protected abstract class AbstractSliderInteractor implements ElementPainter
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

                drawValue(graphics, size, min, max);
				
				
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

            protected abstract void drawValue(Graphics2D graphics, Vector2 size, double min, double max);
		}
		
		
		
		protected LiveInterface value;

		protected LSElement element;
		
		private Painter backgroundPainter, backgroundHoverPainter;
		private double rounding;
		
		
		
		public AbstractSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element,
                                     Painter backgroundPainter, Painter backgroundHoverPainter, double rounding)
		{
			super( ctx, style );
			
			this.value = value;
			this.value.addListener(this);

			this.element = element;
			this.backgroundPainter = backgroundPainter;
			this.backgroundHoverPainter = backgroundHoverPainter;
			this.rounding = rounding;

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


        protected double applyStep(double value, double step) {
            double min = getSliderMin();
            double valueOffset = value - min;

            if (step != 0.0) {
                double steps = Math.round(valueOffset / step);
                valueOffset = steps * step;
            }

            return min + valueOffset;
        }



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
	
	public AbstractSlider(LiveSource valueSource, double width)
	{
		this.valueSource = valueSource;
		this.width = width;
	}



	private static final StyleSheet boxStyle = StyleSheet.style( Primitive.shapePainter.as( null ), Primitive.hoverShapePainter.as( null ) );
}
