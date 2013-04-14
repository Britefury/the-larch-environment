//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.FragmentContext;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class PopupMenu extends Pres
{
	private static final StyleValues defaultPopupMenuContentsStyleValues = StyleValues.getRootStyle().withAttr( Controls.bClosePopupOnActivate, true );
	private static final StyleSheet popupMenuContentsStyle = StyleSheet.style( Controls.bClosePopupOnActivate.as( true ) );
	
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


	public abstract void addSeparator();

	
	//
	// Popup methods
	//

	public void popupMenu(LSElement element, StyleValues style, Anchor targetAnchor, Anchor popupAnchor)
	{
		chainPopup( element, style, targetAnchor, popupAnchor, true, true );
	}
	
	public void popupMenu(LSElement element, Anchor targetAnchor, Anchor popupAnchor)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupMenu( element, ctx.getStyleValues().withAttrs( popupMenuContentsStyle ), targetAnchor, popupAnchor );
		}
		else
		{
			popupMenu( element, defaultPopupMenuContentsStyleValues, targetAnchor, popupAnchor );
		}
	}
	
	public void popupMenuAtMousePosition(LSElement element, StyleValues style, Anchor popupAnchor)
	{
		chainPopupAtMousePosition( element, style, popupAnchor, true, true );
	}
	
	public void popupMenuAtMousePosition(LSElement element, Anchor popupAnchor)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupMenuAtMousePosition( element, ctx.getStyleValues().withAttrs( popupMenuContentsStyle ), popupAnchor );
		}
		else
		{
			popupMenuAtMousePosition( element, defaultPopupMenuContentsStyleValues, popupAnchor );
		}
	}
	
	public void popupAtMousePosition(LSElement element, StyleValues style)
	{
		popupMenuAtMousePosition( element, style, Anchor.TOP_LEFT );
	}
	
	public void popupMenuAtMousePosition(LSElement element)
	{
		popupMenuAtMousePosition( element, Anchor.TOP_LEFT );
	}

	
	
	
	//
	// Internal element creation method
	//
	
	protected abstract Pres createMenuBox(Pres boxItems[]);
	
	
	
	
	

	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
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
