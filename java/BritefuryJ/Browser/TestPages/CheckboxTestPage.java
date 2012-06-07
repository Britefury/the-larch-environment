//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.Checkbox;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class CheckboxTestPage extends SystemPage
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
