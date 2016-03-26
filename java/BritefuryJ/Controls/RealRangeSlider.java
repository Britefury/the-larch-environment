//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.awt.Paint;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class RealRangeSlider extends RangeSlider
{
    public static interface RealRangeSliderListener
    {
        public void onSliderValueChanged(RealRangeSliderControl realRangeSlider, double lower, double upper);
    }

    public static class RealRangeSliderControl extends RangeSliderControl
    {
        private double min, max, step;
        private RealRangeSliderListener listener;


        protected RealRangeSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                         Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter,
                                         double rounding, double min, double max, double step, RealRangeSliderListener listener)
        {
            super( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                    valueBoxPainter, valuePainter, valueHighlightPainter, rounding );

            this.min = min;
            this.max = max;
            this.step = step;
            this.listener = listener;

            element.setFixedValue( value.elementValueFunction() );
        }



        @Override
        protected double getSliderMin()
        {
            return min;
        }

        @Override
        protected double getSliderMax()
        {
            return max;
        }

        @Override
        protected double getSliderStep()
        {
            return step;
        }

        protected double[] getSliderValue()
        {
            Object x = value.getStaticValue();
            if (x instanceof List) {
                List<?> val = (List<?>)value.getStaticValue();
                if (val.size() == 2) {
                    Object a = val.get(0), b = val.get(1);
                    if (a instanceof Double  &&  b instanceof Double) {
                        return new double[] {(Double)a, (Double)b};
                    }
                }
            }
            else if (x instanceof double[]) {
                double arr[] = (double[])x;
                if (arr.length == 2) {
                    return arr;
                }
            }

            return new double[] {min, max};
        }


        protected void changeRange(double lower, double upper)
        {
            double currentValue[] = getSliderValue();

            lower = Math.min( Math.max( lower, min ), max );
            upper = Math.min( Math.max( upper, min ), max );
            if ( lower != currentValue[0]  ||  upper != currentValue[1] )
            {
                value.setLiteralValue( new double[] {lower, upper} );
                if ( listener != null )
                {
                    listener.onSliderValueChanged( this, lower, upper );
                }
            }
        }
    }


    private static class CommitListener implements RealRangeSliderListener
    {
        private LiveValue value;

        public CommitListener(LiveValue value)
        {
            this.value = value;
        }

        @Override
        public void onSliderValueChanged(RealRangeSliderControl spinEntry, double lower, double upper)
        {
            value.setLiteralValue( new double[] {Math.min(lower, upper), Math.max(lower, upper)} );
        }
    }



    private double min, max, step;
    private RealRangeSliderListener listener;


    private RealRangeSlider(LiveSource valueSource, double min, double max, double step, double width, RealRangeSliderListener listener)
    {
        super( valueSource, width );
        this.min = min;
        this.max = max;
        this.step = step;
        this.listener = listener;
    }

    public RealRangeSlider(double initialLower, double initialUpper, double min, double max, double step,
                           double width, RealRangeSliderListener listener)
    {
        this( new LiveSourceValue( new double[]{Math.min(initialLower, initialUpper), Math.max(initialLower, initialUpper)} ),
                min, max, step, width, listener );
    }

    public RealRangeSlider(LiveInterface value, double min, double max, double step, double width, RealRangeSliderListener listener)
    {
        this( new LiveSourceRef( value ), min, max, step, width, listener );
    }

    public RealRangeSlider(LiveValue value, double min, double max, double step, double width)
    {
        this( new LiveSourceRef( value ), min, max, step, width, new CommitListener( value ) );
    }



    @Override
    protected AbstractSliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element,
                                                        Painter backgroundPainter, Painter backgroundHoverPainter,
                                                        Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter, double rounding)
    {
        return new RealRangeSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valueBoxPainter, valuePainter, valueHighlightPainter, rounding, min, max, step, listener );
    }
}
