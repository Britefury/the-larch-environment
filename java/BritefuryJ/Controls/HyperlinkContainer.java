//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

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
	
	
	@Override
	protected Control createHyperlinkControl(PresentationContext ctx, StyleValues style, LSElement contentsElement, boolean bClosePopupOnActivate, LinkListener listener,
			ContextMenuElementInteractor contextMenuInteractor)
	{
		return new HyperlinkContainerControl( ctx, style, contentsElement, listener, bClosePopupOnActivate );
	}
}
