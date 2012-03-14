//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Browser.SystemPages;

import BritefuryJ.Controls.Button;
import BritefuryJ.Controls.Button.ButtonControl;
import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.Pres.UI.BubblePopup;
import BritefuryJ.StyleSheet.StyleSheet;

public class BubblePopupTestPage extends SystemPage
{
	public String getTitle()
	{
		return "Bubble popup test";
	}
	
	protected String getDescription()
	{
		return "Displays content in a popup bubble.";
	}
	
	
	private static StyleSheet contentsStyle = StyleSheet.style( Primitive.fontSize.as( 24 ) );
	
	
	private Pres createPopupButton(String label, final String popupContent, final Anchor targetAnchor)
	{
		Button.ButtonListener listener = new Button.ButtonListener()
		{
			@Override
			public void onButtonClicked(ButtonControl button, PointerButtonEvent event)
			{
				Pres contents = contentsStyle.applyTo( new Label( popupContent ) );
				
				BubblePopup.popupInBubbleAdjacentTo( contents, button.getElement(), targetAnchor, true, true );				
			}
		};
		
		return Button.buttonWithLabel( label, listener ).alignHExpand();
	}
	

	@Override
	protected Pres createContents()
	{
		String popupContent = "The quick brown fox jumps over the lazy dog";
		Pres tbl = new Table( new Object[][] {
				{ createPopupButton( "Up-left", popupContent, Anchor.TOP_LEFT ), createPopupButton( "Up", popupContent, Anchor.TOP ), createPopupButton( "Up-right", popupContent, Anchor.TOP_RIGHT ) },
				{ createPopupButton( "Left", popupContent, Anchor.LEFT ), null, createPopupButton( "Right", popupContent, Anchor.RIGHT ) },
				{ createPopupButton( "Down-left", popupContent, Anchor.BOTTOM_LEFT ), createPopupButton( "Down", popupContent, Anchor.BOTTOM ), createPopupButton( "Down-right", popupContent, Anchor.BOTTOM_RIGHT ) }
				} );
		return tbl;
	}
}
