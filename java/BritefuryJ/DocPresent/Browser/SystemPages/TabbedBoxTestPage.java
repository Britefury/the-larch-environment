//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.Controls.TabbedBox;
import BritefuryJ.Controls.TabbedBox.TabbedBoxControl;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Shape;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class TabbedBoxTestPage extends SystemPage
{
	protected TabbedBoxTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Tabs test";
	}
	
	protected String getDescription()
	{
		return "Tabs control: a set of tabs to choose content";
	}
	
	
	private static class TabsTextChanger implements TabbedBox.TabbedBoxListener
	{
		private ElementRef textElementRef;
		
		
		public TabsTextChanger(ElementRef textElementRef)
		{
			this.textElementRef = textElementRef;
		}


		@Override
		public void onTab(TabbedBoxControl expander, int tab)
		{
			String text = "Tab " + String.valueOf( tab + 1 );
			for (DPElement element: textElementRef.getElements())
			{
				((DPText)element).setText( text, "" );
			}
		}
	}

	

	protected Pres createContents()
	{
		Pres one = new Label( "Ellipse" );
		Pres two = new Label( "Rounded rectangle" );
		Pres three = new Label( "Circle" );
		Pres oneContents = Shape.ellipse( 0.0, 0.0, 200.0, 100.0 );
		Pres twoContents = Shape.roundRectangle( 0.0, 0.0, 200.0, 100.0, 20.0, 20.0 );
		Pres threeContents = Shape.ellipse( 0.0, 0.0, 200.0, 200.0 );
		
		ElementRef expandedTextRef = new Label( "Tab 1" ).elementRef();
		TabsTextChanger listener = new TabsTextChanger( expandedTextRef );
		TabbedBox tabs = new TabbedBox( new Pres[][] { new Pres[] { one, oneContents },
				new Pres[] { two, twoContents },
				new Pres[] { three, threeContents } }, 0, listener );
		Pres optionMenuSectionContents = new Column( new Pres[] { expandedTextRef, new Spacer( 0.0, 15.0 ), tabs } );
		
		return new Body( new Pres[] { new Heading2( "Tabs" ), optionMenuSectionContents } );
	}
}
