//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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

public class ProgressBar extends AbstractSlider {
    public static class ProgressBarControl extends AbstractSlider.AbstractSliderControl
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
        protected double min, max;



        public ProgressBarControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element,
                                  Painter backgroundPainter, Painter backgroundHoverPainter,
                                  Paint valuePaint, Painter valueBoxPainter, double rounding, double min, double max)
        {
            super(ctx, style, value, element, backgroundPainter, backgroundHoverPainter, rounding);

            this.valuePaint = valuePaint;
            this.valueBoxPainter = valueBoxPainter;
            this.max = max;
            this.min = min;

            ProgressBarInteractor sliderInteractor = new ProgressBarInteractor();
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


        protected double getSliderMin()
        {
            return min;
        }

        protected double getSliderMax()
        {
            return max;
        }

        protected double getProgressValue()
        {
            return (Double)value.getStaticValue();
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
    private double min, max;

    private ProgressBar(LiveSource valueSource, double min, double max, double width)
    {
        super(valueSource, width);
        this.valueSource = valueSource;
        this.width = width;
        this.min = min;
        this.max = max;
    }

    public ProgressBar(double initialValue, double min, double max, double width)
    {
        this( new LiveSourceValue( initialValue ), min, max, width );
    }

    public ProgressBar(LiveInterface value, double min, double max, double width)
    {
        this( new LiveSourceRef( value ), min, max, width );
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


    protected ProgressBarControl createProgressBarControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                                                      Paint valuePaint, Painter valueBoxPainter, double rounding)
    {
        return new ProgressBarControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valuePaint, valueBoxPainter, rounding, min, max );
    }
}
