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

public class IntRangeSlider extends RangeSlider
{
    public static interface IntRangeSliderListener
    {
        public void onSliderValueChanged(IntRangeSliderControl realRangeSlider, int lower, int upper);
    }

    public static class IntRangeSliderControl extends RangeSliderControl
    {
        private int min, max, step;
        private IntRangeSliderListener listener;


        protected IntRangeSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                         Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter,
                                         double rounding, int min, int max, int step, IntRangeSliderListener listener)
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

        @Override
        protected double[] getSliderValue()
        {
            Object x = value.getStaticValue();
            if (x instanceof List) {
                List<?> val = (List<?>)value.getStaticValue();
                if (val.size() == 2) {
                    Object a = val.get(0), b = val.get(1);
                    if (a instanceof Integer  &&  b instanceof Integer) {
                        int intA = (Integer)a, intB = (Integer)b;
                        return new double[] {(Double)(double)intA, (Double)(double)intB};
                    }
                }
            }
            else if (x instanceof int[]) {
                int arr[] = (int[])x;
                if (arr.length == 2) {
                    return new double[] {arr[0], arr[1]};
                }
            }

            return new double[] {min, max};
        }

        protected int[] getSliderValueAsInt()
        {
            Object x = value.getStaticValue();
            if (x instanceof List) {
                List<?> val = (List<?>)value.getStaticValue();
                if (val.size() == 2) {
                    Object a = val.get(0), b = val.get(1);
                    if (a instanceof Integer  &&  b instanceof Integer) {
                        return new int[] {(Integer)a, (Integer)b};
                    }
                }
            }
            else if (x instanceof int[]) {
                int arr[] = (int[])x;
                if (arr.length == 2) {
                    return arr;
                }
            }

            return new int[] {min, max};
        }

        @Override
        protected void changeRange(double lower, double upper)
        {
            int currentValue[] = getSliderValueAsInt();

            int iLower = IntSlider.convertDoubleToInt(lower, min, max);
            int iUpper = IntSlider.convertDoubleToInt(upper, min, max);

            if ( iLower != currentValue[0]  ||  iUpper != currentValue[1] )
            {
                value.setLiteralValue( new int[] {iLower, iUpper} );
                if ( listener != null )
                {
                    listener.onSliderValueChanged( this, iLower, iUpper );
                }
            }
        }
    }


    private static class CommitListener implements IntRangeSliderListener
    {
        private LiveValue value;

        public CommitListener(LiveValue value)
        {
            this.value = value;
        }

        @Override
        public void onSliderValueChanged(IntRangeSliderControl spinEntry, int lower, int upper)
        {
            value.setLiteralValue( new int[] {Math.min(lower, upper), Math.max(lower, upper)} );
        }
    }



    private int min, max, step;
    private IntRangeSliderListener listener;


    private IntRangeSlider(LiveSource valueSource, int min, int max, int step, double width, IntRangeSliderListener listener)
    {
        super( valueSource, width );
        this.min = min;
        this.max = max;
        this.step = step;
        this.listener = listener;
    }

    public IntRangeSlider(int initialLower, int initialUpper, int min, int max, int step,
                           double width, IntRangeSliderListener listener)
    {
        this( new LiveSourceValue( new int[]{Math.min(initialLower, initialUpper), Math.max(initialLower, initialUpper)} ),
                min, max, step, width, listener );
    }

    public IntRangeSlider(LiveInterface value, int min, int max, int step, double width, IntRangeSliderListener listener)
    {
        this( new LiveSourceRef( value ), min, max, step, width, listener );
    }

    public IntRangeSlider(LiveValue value, int min, int max, int step, double width)
    {
        this( new LiveSourceRef( value ), min, max, step, width, new CommitListener( value ) );
    }



    @Override
    protected AbstractSliderControl createSliderControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element,
                                                        Painter backgroundPainter, Painter backgroundHoverPainter,
                                                        Painter valueBoxPainter, Painter valuePainter, Painter valueHighlightPainter, double rounding)
    {
        return new IntRangeSliderControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valueBoxPainter, valuePainter, valueHighlightPainter, rounding, min, max, step, listener );
    }
}
