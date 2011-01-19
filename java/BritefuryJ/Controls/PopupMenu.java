//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class PopupMenu extends Pres
{
	private static final StyleValues defaultPopupMenuContentsStyleValues = StyleValues.instance.withAttr( Controls.bClosePopupOnActivate, true );
	private static final StyleSheet popupMenuContentsStyle = StyleSheet.instance.withAttr( Controls.bClosePopupOnActivate, true );
	
	protected ArrayList<Pres> items = new ArrayList<Pres>();
	private boolean bEmpty;
	
	
	protected PopupMenu()
	{
		bEmpty = true;
	}
	
	protected PopupMenu(Object items[])
	{
		this.items.addAll( Arrays.asList( mapCoerce( items ) ) );
		bEmpty = this.items.size() == 0;
	}
	
	
	
	public void add(Object item)
	{
		items.add( coerce( item ) );
		bEmpty = false;
	}
	
	
	//
	// Popup methods
	//

	public void popupToRightOf(DPElement element, PresentationContext ctx, StyleValues style)
	{
		popupToRightOf( element, ctx, style, true, true );
	}
	
	public void popupToRightOf(DPElement element)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupToRightOf( element, ctx.createPresentationContext(), ctx.getStyleValues().withAttrs( popupMenuContentsStyle ) );
		}
		else
		{
			popupToRightOf( element, new PresentationContext(), defaultPopupMenuContentsStyleValues );
		}
	}
	
	public void popupBelow(DPElement element, PresentationContext ctx, StyleValues style)
	{
		popupBelow( element, ctx, style, true, true );
	}
	
	public void popupBelow(DPElement element)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupBelow( element, ctx.createPresentationContext(), ctx.getStyleValues().withAttrs( popupMenuContentsStyle ) );
		}
		else
		{
			popupBelow( element, new PresentationContext(), defaultPopupMenuContentsStyleValues );
		}
	}
	
	public void popupAtMousePosition(DPElement element, PresentationContext ctx, StyleValues style)
	{
		popupAtMousePosition( element, ctx, style, true, true );
	}
	
	public void popupAtMousePosition(DPElement element)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupAtMousePosition( element, ctx.createPresentationContext(), ctx.getStyleValues().withAttrs( popupMenuContentsStyle ) );
		}
		else
		{
			popupAtMousePosition( element, new PresentationContext(), defaultPopupMenuContentsStyleValues );
		}
	}

	
	
	
	//
	// Internal element creation method
	//
	
	protected abstract Pres createMenuBox(Pres boxItems[]);
	
	
	
	
	

	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		Pres itemCombinators[] = mapCoerce( mapPresent( ctx, style, items ) );
		Pres menuElement = new Border( createMenuBox( itemCombinators ) );
		StyleValues menuStyleValues = Controls.popupMenuStyle( style );
		return menuElement.present( ctx, menuStyleValues );
	}

	
	
	public boolean isEmpty()
	{
		return bEmpty;
	}
}
