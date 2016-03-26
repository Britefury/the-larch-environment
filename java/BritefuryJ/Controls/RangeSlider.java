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
import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.LSpace.Interactor.DragElementInteractor;
import BritefuryJ.LSpace.Interactor.MotionElementInteractor;
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
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

public abstract class RangeSlider extends AbstractSlider {
    public abstract static class RangeSliderControl extends AbstractSlider.AbstractSliderControl
    {
        private enum RangeFocus {
            FOCUS_LOWER,
            FOCUS_UPPER,
            FOCUS_BOTH
        }

        private abstract class RangeDrag {
            public abstract void apply(LSElement element, Point2 pos);
        }

        private class RangeDragEndPoint extends RangeDrag {
            private boolean targetUpper;

            private RangeDragEndPoint(boolean targetUpper) {
                this.targetUpper = targetUpper;
            }

            @Override
            public void apply(LSElement element, Point2 pos) {
                Vector2 size = element.getActualSize();

                double valueFrac = pos.x / size.x;

                valueFrac = Math.min( Math.max( valueFrac, 0.0 ), 1.0 );

                double min = getSliderMin();
                double max = getSliderMax();
                double value = min + (max - min) * valueFrac;
                value = applyStep(value, getSliderStep());

                double range[] = getSliderValue();
                double lower = range[0], upper = range[1];

                if (targetUpper && value < lower  ||  !targetUpper && value > upper) {
                    // Swap
                    targetUpper = !targetUpper;
                    double swap = lower;
                    lower = upper;
                    upper = swap;
                }

                if (targetUpper) {
                    upper = value;
                }
                else {
                    lower = value;
                }
                changeRange(lower, upper);
            }
        }

        private class RangeDragBoth extends RangeDrag {
            private Point2 pointerStart;
            private double startLower, startUpper;

            private RangeDragBoth(Point2 pointerStart, double startLower, double startUpper) {
                this.pointerStart = pointerStart;
                this.startLower = startLower;
                this.startUpper = startUpper;
            }

            @Override
            public void apply(LSElement element, Point2 pos) {
                Vector2 size = element.getActualSize();

                double min = getSliderMin();
                double max = getSliderMax();

                double deltaX = pos.x - pointerStart.x;
                double deltaFrac = deltaX / size.x;
                double deltaValue = (max - min) * deltaFrac;

                double lower = applyStep(startLower + deltaValue, getSliderStep());
                double upper = applyStep(startUpper + deltaValue, getSliderStep());

                lower = Math.min(Math.max(lower, min), max);
                upper = Math.min(Math.max(upper, min), max);

                changeRange(lower, upper);
            }
        }

        protected class RangeSliderInteractor extends AbstractSlider.AbstractSliderControl.AbstractSliderInteractor
                implements MotionElementInteractor, DragElementInteractor
        {
            private HashMap<PointerInterface, RangeDrag> pointerToRangeDrag = new HashMap<PointerInterface, RangeDrag>();
            private RangeFocus highlightFocus;


            @Override
            protected void drawValue(Graphics2D graphics, Vector2 size, double min, double max) {
                double val[] = getSliderValue();


                // Draw value
                double lowerValFrac = ( val[0] - min ) / ( max - min );
                double lowerValPos = size.x * lowerValFrac;
                double upperValFrac = ( val[1] - min ) / ( max - min );
                double upperValPos = size.x * upperValFrac;

                Shape valueBoxShape = new Rectangle2D.Double( lowerValPos, 0.0, upperValPos - lowerValPos, size.y );
                valueBoxPainter.drawShape( graphics, valueBoxShape );

                Painter lowerPainter = highlightFocus == RangeFocus.FOCUS_LOWER || highlightFocus == RangeFocus.FOCUS_BOTH ?
                        valueHighlightPainter : valuePainter;
                Painter upperPainter = highlightFocus == RangeFocus.FOCUS_UPPER || highlightFocus == RangeFocus.FOCUS_BOTH ?
                        valueHighlightPainter : valuePainter;
                Shape lowerShape = new Arc2D.Double(lowerValPos - size.y*0.5, 0.0, size.y, size.y, 90.0, 180, Arc2D.CHORD);
                Shape upperShape = new Arc2D.Double(upperValPos - size.y*0.5, 0.0, size.y, size.y, -90, 180.0, Arc2D.CHORD);
                lowerPainter.drawShape( graphics, lowerShape );
                upperPainter.drawShape( graphics, upperShape );
            }


            public RangeFocus getFocus(LSElement element, Point2 pos) {
                Vector2 size = element.getActualSize();

                double min = getSliderMin();
                double max = getSliderMax();
                double val[] = getSliderValue();

                double lowerValFrac = ( val[0] - min ) / ( max - min );
                double lowerValPos = size.x * lowerValFrac;
                double upperValFrac = ( val[1] - min ) / ( max - min );
                double upperValPos = size.x * upperValFrac;

                double lowerThreshold = lowerValPos * 0.75 + upperValPos * 0.25;
                double upperThreshold = lowerValPos * 0.25 + upperValPos * 0.75;

                if (pos.x < lowerThreshold) {
                    return RangeFocus.FOCUS_LOWER;
                }
                else if (pos.x > upperThreshold) {
                    return RangeFocus.FOCUS_UPPER;
                }
                else {
                    return RangeFocus.FOCUS_BOTH;
                }
            }


            @Override
            public boolean dragBegin(LSElement element, PointerButtonEvent event)
            {
                if ( event.getButton() == 1 )
                {
                    RangeFocus focus = getFocus(element, event.getLocalPointerPos());

                    RangeDrag d;
                    if (focus == RangeFocus.FOCUS_LOWER) {
                        d = new RangeDragEndPoint(false);
                    }
                    else if (focus == RangeFocus.FOCUS_UPPER) {
                        d = new RangeDragEndPoint(true);
                    }
                    else {
                        double val[] = getSliderValue();
                        d = new RangeDragBoth(event.getLocalPointerPos(), val[0], val[1]);
                    }

                    pointerToRangeDrag.put(event.getPointer().concretePointer(), d);
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
                PointerInterface pointer = event.getPointer().concretePointer();
                RangeDrag d = pointerToRangeDrag.get(pointer);
                d.apply(element, event.getLocalPointerPos());
                pointerToRangeDrag.remove(event.getPointer().concretePointer());
            }

            @Override
            public void dragMotion(LSElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
            {
                PointerInterface pointer = event.getPointer().concretePointer();
                RangeDrag d = pointerToRangeDrag.get(pointer);
                d.apply(element, event.getLocalPointerPos());
            }

            @Override
            public void pointerMotion(LSElement element, PointerMotionEvent event) {
                RangeFocus f = getFocus(element, event.getLocalPointerPos());
                if (f != highlightFocus) {
                    highlightFocus = f;
                    element.queueFullRedraw();
                }
            }

            @Override
            public void pointerLeaveIntoChild(LSElement element, PointerMotionEvent event) {
            }

            @Override
            public void pointerEnterFromChild(LSElement element, PointerMotionEvent event) {
            }

            @Override
            public void pointerEnter(LSElement element, PointerMotionEvent event) {
                highlightFocus = getFocus(element, event.getLocalPointerPos());
                element.queueFullRedraw();
            }

            @Override
            public void pointerLeave(LSElement element, PointerMotionEvent event) {
                highlightFocus = null;
                element.queueFullRedraw();
            }
        }



        private Painter valueBoxPainter, valuePainter, valueHighlightPainter;



        public RangeSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element,
                                   Painter backgroundPainter, Painter backgroundHoverPainter,
                                   Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter, double rounding)
        {
            super(ctx, style, value, element, backgroundPainter, backgroundHoverPainter, rounding);

            this.valueBoxPainter = valueBoxPainter;
            this.valuePainter = valuePainter;
            this.valueHighlightPainter = valueHighlightPainter;

            RangeSliderInteractor sliderInteractor = new RangeSliderInteractor();
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



        protected abstract double[] getSliderValue();
        protected abstract double getSliderStep();
        protected abstract void changeRange(double lower, double upper);


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

    public RangeSlider(LiveSource valueSource, double width)
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
        Painter valueBoxPainter = style.get( Controls.sliderValueBoxPainter, Painter.class );
        Painter valuePainter = style.get( Controls.sliderValuePainter, Painter.class );
        Painter valueHighlightPainter = style.get(Controls.sliderValueHighlightPainter, Painter.class);
        double rounding = style.get( Controls.sliderRounding, Double.class );
        double size = style.get( Controls.sliderSize, Double.class );

        double w = width > 0.0  ?  width  :  size;

        Pres slider = boxStyle.applyTo( new Spacer( w, size ).alignHExpand().alignVExpand() );


        LSElement element = slider.present( ctx, style );

        return createSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valueBoxPainter, valuePainter, valueHighlightPainter, rounding );
    }

    private static final StyleSheet boxStyle = StyleSheet.style( Primitive.shapePainter.as( null ), Primitive.hoverShapePainter.as( null ) );


    protected abstract AbstractSliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element,
                                                                 Painter backgroundPainter, Painter backgroundHoverPainter,
                                                                 Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter,
                                                                 double rounding);
}
