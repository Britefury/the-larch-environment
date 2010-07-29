//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class MenuItem extends ControlPres
{
	public enum SubmenuPopupDirection
	{
		RIGHT,
		DOWN
	}
	
	public interface MenuItemListener
	{
		public void onMenuItemClicked(MenuItemControl menuItem);
	}
	
	
	public static class MenuItemControl extends Control
	{
		private class SubMenuItemListener implements MenuItemListener
		{
			private PopupMenu subMenu;
			private SubmenuPopupDirection direction;
			
			public SubMenuItemListener(PopupMenu subMenu, SubmenuPopupDirection direction)
			{
				this.subMenu = subMenu;
				this.direction = direction;
			}
			
			
			@Override
			public void onMenuItemClicked(MenuItemControl menuItem)
			{
				if ( direction == SubmenuPopupDirection.RIGHT )
				{
					subMenu.popupToRightOf( menuItem.getElement(), ctx );
				}
				else
				{
					subMenu.popupBelow( menuItem.getElement(), ctx );
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
					listener.onMenuItemClicked( MenuItemControl.this );
					if ( bClosePopupOnActivate )
					{
						element.closeContainingPopupChain();
					}
					return true;
				}
				
				return false;
			}
		}
	
		
		
		private DPBin element;
		private MenuItemListener listener;
		private boolean bClosePopupOnActivate;
		
		
		protected MenuItemControl(PresentationContext ctx, DPBin element, MenuItemListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx );
			this.element = element;
			this.listener = listener;
			this.element.addInteractor( new MenuItemInteractor() );
			this.bClosePopupOnActivate = bClosePopupOnActivate;
		}
		
		protected MenuItemControl(PresentationContext ctx, DPBin element, PopupMenu subMenu, SubmenuPopupDirection direction, boolean bClosePopupOnActivate)
		{
			super( ctx );
			this.element = element;
			this.listener = new SubMenuItemListener( subMenu, direction );
			this.element.addInteractor( new MenuItemInteractor() );
			this.bClosePopupOnActivate = bClosePopupOnActivate;
		}
		
		
		public DPElement getElement()
		{
			return element;
		}
	}

	private Pres child;
	private MenuItemListener listener;
	private PopupMenu subMenu;
	private SubmenuPopupDirection direction;
	
	
	public MenuItem(Object child, MenuItemListener listener)
	{
		this.child = coerce( child );
		this.listener = listener;
	}
	
	public MenuItem(Object child, PopupMenu subMenu, SubmenuPopupDirection direction)
	{
		this.child = coerce( child );
		this.subMenu = subMenu;
		this.direction = direction;
	}
	
	
	public static MenuItem menuItemWithLabel(String labelText, MenuItemListener listener)
	{
		return new MenuItem( new Label( labelText ), listener );
	}
	
	public static MenuItem menuItemWithLabel(String labelText, PopupMenu subMenu, SubmenuPopupDirection direction)
	{
		return new MenuItem( new Label( labelText ), subMenu, direction );
	}
	
	

	
	@Override
	public Control createControl(PresentationContext ctx)
	{
		StyleSheetValues styleValues = ctx.getStyle();

		Pres childElem = presentAsCombinator( Controls.useMenuItemAttrs( ctx ), child );
		
		Painter hoverBackground = styleValues.get( Controls.menuItemHoverBackground, Painter.class );
		double padX = styleValues.get( Controls.menuItemXPadding, Double.class );
		double padY = styleValues.get( Controls.menuItemYPadding, Double.class );
		boolean bClosePopupOnActivate = styleValues.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		StyleSheetValues menuItemStyle = styleValues.withAttr( Primitive.hoverBackground, hoverBackground );
		Pres menuItem = new Bin( childElem.alignHExpand().pad( padX, padY ) );

		DPBin element = (DPBin)menuItem.present( ctx.withStyle( menuItemStyle ) );
		
		
		if ( subMenu != null )
		{
			return new MenuItemControl( ctx, element, subMenu, direction, false );
		}
		else
		{
			return new MenuItemControl( ctx, element, listener, bClosePopupOnActivate );
		}
	}
}
