//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.Controls.Expander;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPLabel;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.Heading3;

public class ExpanderTestPage extends SystemPage
{
	protected ExpanderTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Expander test";
	}
	
	protected String getDescription()
	{
		return "Expander control: show or hide content";
	}
	
	
	private static class ExpanderTextChanger implements DropDownExpander.ExpanderListener
	{
		private ElementRef textElementRef;
		
		
		public ExpanderTextChanger(ElementRef textElementRef)
		{
			this.textElementRef = textElementRef;
		}


		public void onExpander(DropDownExpander.ExpanderControl expander, boolean bExpanded)
		{
			String text = bExpanded  ?  "expanded"  :  "collapsed";
			for (DPElement element: textElementRef.getElements())
			{
				((DPLabel)element).setText( text );
			}
		}
	}

	

	protected Pres createContents()
	{
		Pres heading = new Label( "Click to expand" );
		Pres contents = new Border( new Heading3( "The contents of the expander control" ) );
		ElementRef expandedTextRef = new Label( "Collapsed" ).elementRef();
		ExpanderTextChanger listener = new ExpanderTextChanger( expandedTextRef );
		Expander expander = new DropDownExpander( heading, contents, listener );
		Pres optionMenuSectionContents = new Column( new Pres[] { expandedTextRef, expander } );
		
		return new Body( new Pres[] { new Heading2( "Expander" ), optionMenuSectionContents } );
	}
}
