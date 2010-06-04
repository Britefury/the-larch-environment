//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Controls.Checkbox;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class CheckboxTestPage extends SystemPage
{
	protected CheckboxTestPage()
	{
		register( "tests.controls.checkbox" );
	}
	
	
	public String getTitle()
	{
		return "Checkbox test";
	}
	
	protected String getDescription()
	{
		return "Checkbox element: can be toggled";
	}
	
	
	private class CheckboxTextChanger implements Checkbox.CheckboxListener
	{
		private DPText textElement;
		
		
		public CheckboxTextChanger(DPText textElement)
		{
			this.textElement = textElement;
		}


		public boolean onCheckboxToggled(Checkbox checkbox, boolean state)
		{
			textElement.setText( String.valueOf( state ) );
			return true;
		}
	}

	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFontSize( 18 );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	protected DPElement section(String title, DPElement contents)
	{
		DPElement heading = headingStyleSheet.staticText( title );
		
		return styleSheet.vbox( new DPElement[] { heading.padY( 10.0 ), contents } );
	}
	
	protected DPElement colouredText(PrimitiveStyleSheet style)
	{
		return style.staticText( "Change the colour of this text using the buttons below." );
	}
	
	protected DPElement createContents()
	{
		DPText stateText = styleSheet.staticText( "false" );
		Checkbox checkbox = controlsStyleSheet.checkbox( styleSheet.staticText( "State" ), false, new CheckboxTextChanger( stateText ) );
		DPElement checkBoxes = styleSheet.withHBoxSpacing( 20.0 ).hbox( new DPElement[] { checkbox.getElement() } ).padX( 5.0 );
		DPElement checkboxSectionContents = styleSheet.vbox( new DPElement[] { stateText, checkBoxes } );
		DPElement checkboxSection = section( "Checkbox", checkboxSectionContents );
		
		return checkboxSection;
	}
}
