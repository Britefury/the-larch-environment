//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.net.URI;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Interactor.ContextMenuElementInteractor;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleValues;

public class HyperlinkContainer extends AbstractHyperlink
{
	public static class HyperlinkContainerControl extends AbstractHyperlink.AbstractHyperlinkControl
	{
		protected HyperlinkContainerControl(PresentationContext ctx, StyleValues style, LSElement element, LinkListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style, element, listener, bClosePopupOnActivate );
		}
	}

	
	
	public HyperlinkContainer(Object contents, LinkListener listener)
	{
		super( contents, listener );
	}
	
	public HyperlinkContainer(Object contents, Subject targetSubject)
	{
		super( contents, targetSubject );
	}
	
	public HyperlinkContainer(Object contents, URI uri)
	{
		super( contents, uri );
	}
	
	
	@Override
	protected Control createHyperlinkControl(PresentationContext ctx, StyleValues style, LSElement contentsElement, boolean bClosePopupOnActivate, LinkListener listener,
			ContextMenuElementInteractor contextMenuInteractor)
	{
		return new HyperlinkContainerControl( ctx, style, contentsElement, listener, bClosePopupOnActivate );
	}
}
