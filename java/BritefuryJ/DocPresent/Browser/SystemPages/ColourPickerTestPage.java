//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.Controls.ColourPicker;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class ColourPickerTestPage extends SystemPage
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
