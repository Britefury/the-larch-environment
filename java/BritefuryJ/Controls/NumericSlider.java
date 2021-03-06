//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.DragElementInteractor;
import BritefuryJ.LSpace.Interactor.HoverElementInteractor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public abstract class NumericSlider extends AbstractSlider {
    public abstract static class NumericSliderControl extends AbstractSlider.AbstractSliderControl
    {
        protected class NumericSliderInteractor extends AbstractSlider.AbstractSliderControl.AbstractSliderInteractor
                implements HoverElementInteractor, DragElementInteractor
        {
            private boolean highlight = false;

            @Override
            protected void drawValue(Graphics2D graphics, Vector2 size, double min, double max) {
                double val = getSliderValue();


                double valueFrac = ( val - min ) / ( max - min );
                double valuePos = size.x * valueFrac;

                Shape valueShape = new Arc2D.Double(valuePos - size.y*0.5, 0.0, size.y, size.y, 0.0, 360.0, Arc2D.CHORD);
                Painter valPainter = highlight ? valueHighlightPainter  :  valuePainter;
                valPainter.drawShape( graphics, valueShape );
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


            @Override
            public void pointerEnter(LSElement element, PointerMotionEvent event)
            {
                highlight = true;
                element.queueFullRedraw();
            }

            @Override
            public void pointerLeave(LSElement element, PointerMotionEvent event)
            {
                highlight = false;
                element.queueFullRedraw();
            }


            private void change(LSElement element, Point2 pos)
            {
                Vector2 size = element.getActualSize();

                double valueFrac = pos.x / size.x;

                valueFrac = Math.min( Math.max( valueFrac, 0.0 ), 1.0 );

                double min = getSliderMin();
                double max = getSliderMax();

                double value = min + (max - min) * valueFrac;
                value = applyStep(value, getSliderStep());

                changeValue( value );
            }
        }



        private Painter valuePainter, valueHighlightPainter;



        public NumericSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element,
                                    Painter backgroundPainter, Painter backgroundHoverPainter,
                                    Painter valuePainter, Painter valueHighlightPainter, double rounding)
        {
            super(ctx, style, value, element, backgroundPainter, backgroundHoverPainter, rounding);

            this.valuePainter = valuePainter;
            this.valueHighlightPainter = valueHighlightPainter;

            NumericSliderInteractor sliderInteractor = new NumericSliderInteractor();
            element.addPainter( sliderInteractor );
            element.addElementInteractor( sliderInteractor );
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



        protected abstract double getSliderValue();
        protected abstract double getSliderStep();
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

    public NumericSlider(LiveSource valueSource, double width)
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
        Painter valuePainter = style.get( Controls.sliderValuePainter, Painter.class );
        Painter valueHighlightPainter = style.get(Controls.sliderValueHighlightPainter, Painter.class);
        double rounding = style.get( Controls.sliderRounding, Double.class );
        double size = style.get( Controls.sliderSize, Double.class );

        double w = width > 0.0  ?  width  :  size;

        Pres slider = boxStyle.applyTo( new Spacer( w, size ).alignHExpand().alignVExpand() );


        LSElement element = slider.present( ctx, style );

        return createSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valuePainter, valueHighlightPainter, rounding );
    }

    private static final StyleSheet boxStyle = StyleSheet.style( Primitive.shapePainter.as( null ), Primitive.hoverShapePainter.as( null ) );


    protected abstract AbstractSliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value,
                                                                 LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                                                 Painter valuePainter, Painter valueHighlightPainter, double rounding);}
