//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2014.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public abstract class ProgressBar extends AbstractSlider {
    public abstract static class ProgressBarControl extends AbstractSlider.AbstractSliderControl
    {
        protected class ProgressBarInteractor extends AbstractSlider.AbstractSliderControl.AbstractSliderInteractor
        {
            @Override
            protected void drawValue(Graphics2D graphics, Vector2 size, double min, double max) {
                double val = getProgressValue();


                // Draw value
                double valueFrac = ( val - min ) / ( max - min );
                double valuePos = size.x * valueFrac;

                Shape valueBoxShape = new Rectangle2D.Double( 0.0, 0.0, valuePos, size.y );
                valueBoxPainter.drawShape( graphics, valueBoxShape );

                Shape valueShape = new Line2D.Double( valuePos, 0.0, valuePos, size.y );
                graphics.setPaint( valuePaint );
                graphics.setStroke( new BasicStroke( 1.0f ) );
                graphics.draw( valueShape );
            }
        }



        private Painter valueBoxPainter;
        private Paint valuePaint;



        public ProgressBarControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element,
                                    Painter backgroundPainter, Painter backgroundHoverPainter,
                                    Paint valuePaint, Painter valueBoxPainter, double rounding)
        {
            super(ctx, style, value, element, backgroundPainter, backgroundHoverPainter, rounding);

            this.valuePaint = valuePaint;
            this.valueBoxPainter = valueBoxPainter;

            ProgressBarInteractor sliderInteractor = new ProgressBarInteractor();
            element.addPainter( sliderInteractor );
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



        protected abstract double getProgressValue();


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

    public ProgressBar(LiveSource valueSource, double width)
    {
        super(valueSource, width);
        this.valueSource = valueSource;
        this.width = width;
    }



    @Override
    public Control createControl(PresentationContext ctx, StyleValues style)
    {
        LiveInterface value = valueSource.getLive();

        Painter backgroundPainter = style.get( Controls.sliderBackgroundPainter, Painter.class );
        Painter backgroundHoverPainter = style.get( Controls.sliderBackgroundHoverPainter, Painter.class );
        Paint valuePaint = style.get( Controls.progressBarValuePaint, Paint.class );
        Painter valueBoxPainter = style.get( Controls.progressValueBoxPainter, Painter.class );
        double rounding = style.get( Controls.sliderRounding, Double.class );
        double size = style.get( Controls.sliderSize, Double.class );

        double w = width > 0.0  ?  width  :  size;

        Pres slider = boxStyle.applyTo( new Spacer( w, size ).alignHExpand().alignVExpand() );


        LSElement element = slider.present( ctx, style );

        return createProgressBarControl(ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valuePaint, valueBoxPainter, rounding);
    }

    private static final StyleSheet boxStyle = StyleSheet.style( Primitive.shapePainter.as( null ), Primitive.hoverShapePainter.as( null ) );


    protected abstract AbstractSliderControl createProgressBarControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                                                      Paint valuePaint, Painter valueBoxPainter, double rounding);}
