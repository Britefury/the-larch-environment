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

public class RealProgressBar extends ProgressBar
{
    public static class RealProgressBarControl extends ProgressBarControl
    {
        private double min, max;


        protected RealProgressBarControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                    Paint valuePaint, Painter valueHighlightPainter, double rounding, double min, double max)
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
            return (Double)value.getStaticValue();
        }
    }



    private double min, max;


    private RealProgressBar(LiveSource valueSource, double min, double max, double width)
    {
        super( valueSource, width );
        this.min = min;
        this.max = max;
    }

    public RealProgressBar(double initialValue, double min, double max, double width)
    {
        this( new LiveSourceValue( initialValue ), min, max, width );
    }

    public RealProgressBar(LiveInterface value, double min, double max, double width)
    {
        this( new LiveSourceRef( value ), min, max, width );
    }



    @Override
    protected AbstractSliderControl createProgressBarControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, Painter backgroundPainter, Painter backgroundHoverPainter,
                                                        Paint valuePaint, Painter valueHighlightPainter, double rounding)
    {
        return new RealProgressBarControl( ctx, style, value, element, backgroundPainter, backgroundHoverPainter,
                valuePaint, valueHighlightPainter, rounding, min, max );
    }
}
