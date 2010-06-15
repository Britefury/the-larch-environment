//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.OptionMenu;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class OptionMenuTestPage extends SystemPage
{
	protected OptionMenuTestPage()
	{
		register( "tests.controls.optionmenu" );
	}
	
	
	public String getTitle()
	{
		return "Option menu test";
	}
	
	protected String getDescription()
	{
		return "Option menu control: choose from a list";
	}
	
	
	private class OptionMenuTextChanger implements OptionMenu.OptionMenuListener
	{
		private DPText textElement;
		
		
		public OptionMenuTextChanger(DPText textElement)
		{
			this.textElement = textElement;
		}


		public void onOptionMenuChoice(OptionMenu optionMenu, int previousChoice, int choice)
		{
			textElement.setText( String.valueOf( choice ) );
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
	
	protected DPElement createContents()
	{
		DPText choiceText = styleSheet.staticText( "0" );
		DPElement optionChoices[] = new DPElement[] { styleSheet.staticText( "Zero" ), styleSheet.staticText( "One" ), styleSheet.staticText( "Two" ),
				styleSheet.staticText( "Three" ), styleSheet.staticText( "Four" ) };
		DPElement menuChoices[] = new DPElement[] { styleSheet.staticText( "Zero" ), styleSheet.staticText( "One" ), styleSheet.staticText( "Two" ),
				styleSheet.staticText( "Three" ), styleSheet.staticText( "Four" ) };
		OptionMenuTextChanger listener = new OptionMenuTextChanger( choiceText );
		OptionMenu optionMenu = controlsStyleSheet.optionMenu( optionChoices, menuChoices, 0, listener );
		DPElement optionMenus = styleSheet.withHBoxSpacing( 20.0 ).hbox( new DPElement[] { styleSheet.spaceBin( optionMenu.getElement().alignHExpand(), 100.0, -1.0 ) } ).padX( 5.0 );
		DPElement optionMenuSectionContents = styleSheet.vbox( new DPElement[] { choiceText, optionMenus } );
		DPElement optionMenuSection = section( "Option menu", optionMenuSectionContents );
		
		return optionMenuSection;
	}
}
