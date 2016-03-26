//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.Checkbox;
import BritefuryJ.Controls.RadioButton;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class RadioButtonTestPage extends TestPage
{
	protected RadioButtonTestPage()
	{
	}


	public String getTitle()
	{
		return "Radio button test";
	}

	protected String getDescription()
	{
		return "Radio button control: can be toggled";
	}


	protected Pres createContents()
	{
		LiveValue state = new LiveValue( "one" );
		Pres radioButtonOne = RadioButton.radioButtonWithLabel("One", "one", state);
		Pres radioButtonTwo = RadioButton.radioButtonWithLabel("Two", "two", state);
		Pres radioButtonThree = RadioButton.radioButtonWithLabel("Three", "three", state);
		Pres radioButtonFour = RadioButton.radioButtonWithLabel("Four", "four", state);
		Pres radioButtonSectionContents = new Column( new Object[] { radioButtonOne.padX( 5.0 ), radioButtonTwo.padX( 5.0 ), radioButtonThree.padX( 5.0 ), radioButtonFour.padX( 5.0 ), state } );

		return new Body( new Pres[] { new Heading2( "Radio button" ), radioButtonSectionContents } );
	}
}
