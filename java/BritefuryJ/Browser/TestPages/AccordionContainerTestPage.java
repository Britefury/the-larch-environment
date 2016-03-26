//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.AccordionContainer;
import BritefuryJ.Controls.TabbedBox;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSLabel;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.*;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class AccordionContainerTestPage extends TestPage
{
	protected AccordionContainerTestPage()
	{
	}


	public String getTitle()
	{
		return "Accordion container test";
	}

	protected String getDescription()
	{
		return "Accordion container control: show and hide sections one at a time";
	}


	private static class AccordionTextChanger implements AccordionContainer.AccordionContainerListener
	{
		private ElementRef textElementRef;


		public AccordionTextChanger(ElementRef textElementRef)
		{
			this.textElementRef = textElementRef;
		}


		@Override
		public void onChoice(AccordionContainer.AccordionContainerControl accordion, int choice)
		{
			String text = "Section " + String.valueOf( choice + 1 );
			for (LSElement element: textElementRef.getElements())
			{
				((LSLabel)element).setText( text );
			}
		}
	}



	protected Pres createContents()
	{
		Pres one = new Label( "One" );
		Pres two = new Label( "Two" );
		Pres three = new Label( "Three" );
		Pres oneContents = Shape.ellipse(0.0, 0.0, 200.0, 100.0);
		Pres twoContents = Shape.roundRectangle( 0.0, 0.0, 200.0, 100.0, 20.0, 20.0 );
		Pres threeContents = Shape.ellipse( 0.0, 0.0, 200.0, 200.0 );

		ElementRef expandedTextRef = new Label( "Section 1" ).elementRef();
		AccordionTextChanger listener = new AccordionTextChanger( expandedTextRef );
		AccordionContainer tabs = new AccordionContainer( new Pres[][] { new Pres[] { one, oneContents },
				new Pres[] { two, twoContents },
				new Pres[] { three, threeContents } }, 0, listener );
		Pres optionMenuSectionContents = new Column( new Pres[] { expandedTextRef, new Spacer( 0.0, 15.0 ), tabs } );

		return new Body( new Pres[] { new Heading2( "Accordion" ), optionMenuSectionContents } );
	}
}
