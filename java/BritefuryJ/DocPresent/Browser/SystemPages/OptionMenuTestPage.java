//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.Controls.OptionMenu;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Combinators.ElementRef;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.SpaceBin;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;

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
		private ElementRef textElementRef;
		
		
		public OptionMenuTextChanger(ElementRef textElementRef)
		{
			this.textElementRef = textElementRef;
		}


		public void onOptionMenuChoice(OptionMenu.OptionMenuControl optionMenu, int previousChoice, int choice)
		{
			for (DPElement element: textElementRef.getElements())
			{
				((DPText)element).setText( String.valueOf( choice ) );
			}
		}
	}

	

	protected DPElement createContents()
	{
		ElementRef choiceTextRef = new StaticText( "0" ).elementRef();
		Pres choices[] = new Pres[] { new StaticText( "Zero" ), new StaticText( "One" ), new StaticText( "Two" ), new StaticText( "Three" ), new StaticText( "Four" ) };
		OptionMenuTextChanger listener = new OptionMenuTextChanger( choiceTextRef );
		OptionMenu optionMenu = new OptionMenu( choices, 0, listener );
		Pres optionMenuBox = new SpaceBin( optionMenu.alignHExpand(), 100.0, -1.0 ).padX( 5.0 );
		Pres optionMenuSectionContents = new VBox( new Pres[] { choiceTextRef, optionMenuBox } );
		
		return new Body( new Pres[] { new Heading2( "Option menu" ), optionMenuSectionContents } ).present();
	}
}
