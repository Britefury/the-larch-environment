//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;

public class MenuItem
{
	public enum SubmenuPopupDirection
	{
		RIGHT,
		DOWN
	}
	
	public interface MenuItemListener
	{
		public void onMenuItemClicked(MenuItem menuItem);
	}
	
	
	private static class SubMenuItemListener implements MenuItemListener
	{
		private PopupMenu subMenu;
		private SubmenuPopupDirection direction;
		
		public SubMenuItemListener(PopupMenu subMenu, SubmenuPopupDirection direction)
		{
			this.subMenu = subMenu;
			this.direction = direction;
		}
		
		
		@Override
		public void onMenuItemClicked(MenuItem menuItem)
		{
			if ( direction == SubmenuPopupDirection.RIGHT )
			{
				subMenu.popupToRightOf( menuItem.getElement() );
			}
			else
			{
				subMenu.popupBelow( menuItem.getElement() );
			}
		}
	}
	

	private class MenuItemInteractor extends ElementInteractor
	{
		public MenuItemInteractor()
		{	
		}
		
		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
		{
			return true;
		}

		public boolean onButtonUp(DPElement element, PointerButtonEvent event)
		{
			if ( element.isRealised() )
			{
				if ( bClosePopupOnActivate )
				{
					element.closeContainingPopupChain();
				}
				listener.onMenuItemClicked( MenuItem.this );
				return true;
			}
			
			return false;
		}
	}

	
	
	private DPBin element;
	private MenuItemListener listener;
	private boolean bClosePopupOnActivate;
	
	
	protected MenuItem(DPBin element, MenuItemListener listener, boolean bClosePopupOnActivate)
	{
		this.element = element;
		this.listener = listener;
		this.element.addInteractor( new MenuItemInteractor() );
		this.bClosePopupOnActivate = bClosePopupOnActivate;
	}
	
	protected MenuItem(DPBin element, PopupMenu subMenu, SubmenuPopupDirection direction, boolean bClosePopupOnActivate)
	{
		this( element, new SubMenuItemListener( subMenu, direction ), bClosePopupOnActivate );
	}
	
	
	public DPElement getElement()
	{
		return element;
	}
}
