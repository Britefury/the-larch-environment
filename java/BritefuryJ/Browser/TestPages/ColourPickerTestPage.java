//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Controls.ColourPicker;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class ColourPickerTestPage extends TestPage
{
	protected ColourPickerTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Colour picker test";
	}
	
	protected String getDescription()
	{
		return "Colour picker control";
	}
	
	
	protected Pres createContents()
	{
		LiveValue liveColour = new LiveValue( Color.black );
		Pres colourPickerA = new Row( new Pres[] { new Label( "Colour picker A" ), new ColourPicker( liveColour ) } );
		Pres colourPickerB = new Row( new Pres[] { new Label( "Colour picker B (linked to A)" ), new ColourPicker( liveColour ) } );
		Pres colourPickerSectionContents = new Column( new Object[] { colourPickerA.padX( 5.0 ), colourPickerB.padX( 5.0 ), liveColour } );

		return new Body( new Pres[] { new Heading2( "Colour picker" ), colourPickerSectionContents } );
	}
}
