//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.Checkbox;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class CheckboxTestPage extends TestPage
{
	protected CheckboxTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Checkbox test";
	}
	
	protected String getDescription()
	{
		return "Checkbox element: can be toggled";
	}
	
	
	protected Pres createContents()
	{
		LiveValue state = new LiveValue( false );
		Pres checkboxA = Checkbox.checkboxWithLabel( "Checkbox A", state );
		Pres checkboxB = Checkbox.checkboxWithLabel( "Checkbox B (linked to checkbox A)", state );
		Pres checkboxSectionContents = new Column( new Object[] { checkboxA.padX( 5.0 ), checkboxB.padX( 5.0 ), state } );

		return new Body( new Pres[] { new Heading2( "Checkbox" ), checkboxSectionContents } );
	}
}
