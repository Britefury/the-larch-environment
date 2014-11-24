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

public class IntProgressBar extends ProgressBar
{
    public static class IntProgressBarControl extends ProgressBarControl
    {
        private int min, max;


        protected IntProgressBarControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                   Paint valuePaint, Painter valueHighlightPainter, double rounding, int min, int max)
        {
            super( ctx, style, value, element, backgroundPainter, backgroundHoverPainter, valuePaint, valueHighlightPainter, rounding );

            this.min = min;
            this.max = max;

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

        @Override
        protected double getProgressValue()
        {
            Integer val = (Integer)value.getStaticValue();
            return val;
        }
    }



    private int min, max;


    private IntProgressBar(LiveSource valueSource, int min, int max, double width)
    {
        super( valueSource, width );
        this.min = min;
        this.max = max;
    }

    public IntProgressBar(int initialValue, int min, int max, double width)
    {
        this( new LiveSourceValue( initialValue ), min, max, width );
    }

    public IntProgressBar(LiveInterface value, int min, int max, double width)
    {
        this( new LiveSourceRef( value ), min, max, width );
    }



    @Override
    protected AbstractSliderControl createProgressBarControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                                        Paint valuePaint, Painter valueHighlightPainter, double rounding)
    {
        return new IntProgressBarControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valuePaint, valueHighlightPainter, rounding, min, max);
    }
}
