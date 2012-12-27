//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.AbstractHyperlink;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.Controls.OptionMenu;
import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.UI.BubblePopup;

public class OptionMenuTestPage extends TestPage
{
	protected OptionMenuTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Option menu test";
	}
	
	protected String getDescription()
	{
		return "Option menu control: choose from a list";
	}

	

	protected Pres createContents()
	{
		final LiveValue value = new LiveValue( 0 );
		LiveFunction.Function fn = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				return new Label( value.getValue().toString() );
			}
		};
		
		LiveFunction f = new LiveFunction( fn );
		
		
		Pres choices[] = new Pres[] { new Label( "Zero" ), new Label( "One" ), new Label( "Two" ), new Label( "Three" ), new Label( "Four" ) };
		OptionMenu optionMenu = new OptionMenu( choices, value );
		final Pres optionMenuBox = new SpaceBin( 100.0, -1.0, optionMenu.alignHExpand() ).padX( 5.0 );

		Hyperlink.LinkListener showBubblePopupListener = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink.AbstractHyperlinkControl link, PointerButtonClickedEvent event)
			{
				BubblePopup.popupInBubbleAdjacentTo( optionMenuBox, link.getElement(), Anchor.BOTTOM, true, false );
			}
		};
		
		AbstractHyperlink displayBubblePopup = new Hyperlink( "Display in nested bubble popup", showBubblePopupListener );
		
		
		Pres optionMenuSectionContents = new Column( new Object[] { new Row( new Object[] { new Label( "Value = " ), f } ), optionMenuBox, displayBubblePopup } );
		
		return new Body( new Pres[] { new Heading2( "Option menu" ), optionMenuSectionContents } );
	}
}
