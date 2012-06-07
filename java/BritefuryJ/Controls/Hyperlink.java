//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;


import BritefuryJ.Browser.Location;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSText;
import BritefuryJ.LSpace.Interactor.ContextMenuElementInteractor;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleValues;

public class Hyperlink extends AbstractHyperlink
{
	public static class HyperlinkControl extends AbstractHyperlink.AbstractHyperlinkControl
	{
		private LSText textElement;
		
		
		protected HyperlinkControl(PresentationContext ctx, StyleValues style, LSText element, LinkListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style, element, listener, bClosePopupOnActivate );
			this.textElement = element;
		}
		
		
		public String getText()
		{
			return textElement.getText();
		}
		
		public void setText(String text)
		{
			textElement.setText( text );
		}
	}

	
	
	public Hyperlink(String text, LinkListener listener)
	{
		super( new Text( text ), new FixedLinkListenerFactory( listener ) );
	}
	
	public Hyperlink(String text, Location targetLocation)
	{
		super( new Text( text ), new LocationLinkListenerFactory( targetLocation ) );
	}
	
	
	@Override
	protected Control createHyperlinkControl(PresentationContext ctx, StyleValues style, LSElement contentsElement, boolean bClosePopupOnActivate, LinkListener listener,
			ContextMenuElementInteractor contextMenuInteractor)
	{
		return new HyperlinkControl( ctx, style, (LSText)contentsElement, listener, bClosePopupOnActivate );
	}
}
