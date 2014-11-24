//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.IntRangeSlider;
import BritefuryJ.Controls.IntSlider;
import BritefuryJ.Controls.RealRangeSlider;
import BritefuryJ.Controls.RealSlider;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.UI.SectionHeading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class SliderTestPage extends TestPage
{
	protected SliderTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Slider test";
	}
	
	protected String getDescription()
	{
		return "Slider control: edit a numeric value";
	}


    private LiveFunction realRangeLowerLiveFn(final LiveValue rangeLive) {
        return new LiveFunction(new LiveFunction.Function() {
            public Object evaluate() {
                double arr[] = (double[])rangeLive.getValue();
                return arr[0];
            }
        });
    }
	
    private LiveFunction realRangeUpperLiveFn(final LiveValue rangeLive) {
        return new LiveFunction(new LiveFunction.Function() {
            public Object evaluate() {
                double arr[] = (double[])rangeLive.getValue();
                return arr[1];
            }
        });
    }

    private LiveFunction intRangeLowerLiveFn(final LiveValue rangeLive) {
        return new LiveFunction(new LiveFunction.Function() {
            public Object evaluate() {
                int arr[] = (int[])rangeLive.getValue();
                return arr[0];
            }
        });
    }

    private LiveFunction intRangeUpperLiveFn(final LiveValue rangeLive) {
        return new LiveFunction(new LiveFunction.Function() {
            public Object evaluate() {
                int arr[] = (int[])rangeLive.getValue();
                return arr[1];
            }
        });
    }

	protected Pres createContents()
	{
		LiveValue realValue = new LiveValue( -5.0 );
		LiveValue intValue = new LiveValue( -6 );
        final LiveValue realRangeValue = new LiveValue(new double[]{-5.0, 5.0});
        final LiveValue intRangeValue = new LiveValue(new int[]{-6, 6});
		RealSlider realSlider = new RealSlider( realValue, -10.0, 10.0, 0.5, 0.0, 300.0 );
		IntSlider intSlider = new IntSlider( intValue, -10, 10, 2, 0, 300.0 );
        RealRangeSlider realRangeSlider = new RealRangeSlider(realRangeValue, -10.0, 10.0, 0.5, 300.0);
        IntRangeSlider intRangeSlider = new IntRangeSlider(intRangeValue, -10, 10, 2, 300.0);

        LiveFunction realRangeLower = realRangeLowerLiveFn(realRangeValue);
        LiveFunction realRangeUpper = realRangeUpperLiveFn(realRangeValue);
        LiveFunction intRangeLower = intRangeLowerLiveFn(intRangeValue);
        LiveFunction intRangeUpper = intRangeUpperLiveFn(intRangeValue);


		Pres realLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Real number: " ),
			    new SpaceBin( 200.0, -1.0, realSlider.alignHExpand() ).alignVCentre(), realValue } ).padX( 5.0 ) );
		Pres intLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Integer: " ),
			    new SpaceBin( 200.0, -1.0, intSlider.alignHExpand() ).alignVCentre(), intValue } ).padX( 5.0 ) );
        Pres realRangeLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Real range: " ),
                new SpaceBin( 200.0, -1.0, realRangeSlider.alignHExpand() ).alignVCentre(),
                realRangeLower, new Label(":"), realRangeUpper } ).padX( 5.0 ) );
        Pres intRangeLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Integer range: " ),
                new SpaceBin( 200.0, -1.0, intRangeSlider.alignHExpand() ).alignVCentre(),
                intRangeLower, new Label(":"), intRangeUpper } ).padX( 5.0 ) );
		Pres spinEntrySectionContents = new Column( new Pres[] {
                new Label("Numeric value sliders:"),
                realLine, intLine,
                new Label("Ranged sliders:"),
                realRangeLine, intRangeLine } );
		
		return new Body( new Pres[] { new SectionHeading2( "Sliders" ), spinEntrySectionContents } );
	}
}
