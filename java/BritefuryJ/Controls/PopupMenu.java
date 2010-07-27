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
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public abstract class PopupMenu extends Pres
{
	private static final StyleSheetValues defaultPopupMenuContentsStyle = StyleSheetValues.instance.withAttr( Controls.bClosePopupOnActivate, true );
	
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
	// Popup to right of
	//
	
	public void popupToRightOf(DPElement element, PresentationContext ctx)
	{
		DPElement menuElement = present( ctx );
		menuElement.popupToRightOf( element, true, true );
	}
	
	public void popupToRightOf(DPElement element, StyleSheetValues style)
	{
		popupToRightOf( element, new PresentationContext( style ) );
	}
	
	public void popupToRightOf(DPElement element)
	{
		popupToRightOf( element, new PresentationContext( defaultPopupMenuContentsStyle ) );
	}
	
	
	
	//
	// Popup below
	//
	
	public void popupBelow(DPElement element, PresentationContext ctx)
	{
		DPElement menuElement = present( ctx );
		menuElement.popupBelow( element, true, true );
	}
	
	public void popupBelow(DPElement element, StyleSheetValues style)
	{
		popupBelow( element, new PresentationContext( style ) );
	}
	
	public void popupBelow(DPElement element)
	{
		popupBelow( element, new PresentationContext( defaultPopupMenuContentsStyle ) );
	}
	
	
	
	//
	// Popup at mouse position
	//
	
	public void popupAtMousePosition(DPElement element, PresentationContext ctx)
	{
		DPElement menuElement = present( ctx );
		element.getRootElement().createPopupAtMousePosition( menuElement, true, true );
	}
	
	public void popupAtMousePosition(DPElement element, StyleSheetValues style)
	{
		popupAtMousePosition( element, new PresentationContext( style ) );
	}
	
	public void popupAtMousePosition(DPElement element)
	{
		popupAtMousePosition( element, new PresentationContext( defaultPopupMenuContentsStyle ) );
	}

	
	
	
	//
	// Internal element creation method
	//
	
	protected abstract Pres createMenuBox(Pres boxItems[]);
	
	
	
	
	

	@Override
	public DPElement present(PresentationContext ctx)
	{
		Pres itemCombinators[] = mapPresentAsCombinators( ctx, items );
		Pres menuElement = new Border( createMenuBox( itemCombinators ) );
		StyleSheetValues menuStyleValues = Controls.popupMenuStyle( ctx.getStyle() );
		return menuElement.present( ctx.withStyle( menuStyleValues ) );
	}

	
	
	public boolean isEmpty()
	{
		return bEmpty;
	}
}
