//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.IntSlider;
import BritefuryJ.Controls.RealSlider;
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
	
	
	protected Pres createContents()
	{
		LiveValue realValue = new LiveValue( -5.0 );
		LiveValue intValue = new LiveValue( -5 );
		RealSlider realSlider = new RealSlider( realValue, -10.0, 10.0, 0.0, 300.0 );
		IntSlider intSlider = new IntSlider( intValue, -10, 10, 0, 300.0 );
		
		Pres realLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Real number: " ),
			    new SpaceBin( 200.0, -1.0, realSlider.alignHExpand() ).alignVCentre(), realValue } ).padX( 5.0 ) );
		Pres intLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Integer: " ),
			    new SpaceBin( 200.0, -1.0, intSlider.alignHExpand() ).alignVCentre(), intValue } ).padX( 5.0 ) );
		Pres spinEntrySectionContents = new Column( new Pres[] { realLine, intLine } );
		
		return new Body( new Pres[] { new SectionHeading2( "Sliders" ), spinEntrySectionContents } );
	}
}
