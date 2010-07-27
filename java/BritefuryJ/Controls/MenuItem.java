//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

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

public class MenuItem extends Pres
{
	public enum SubmenuPopupDirection
	{
		RIGHT,
		DOWN
	}
	
	public interface MenuItemListener
	{
		public void onMenuItemClicked(MenuItem menuItem, DPElement element);
	}
	
	
	private static class SubMenuItemListener implements MenuItemListener
	{
		private PopupMenu subMenu;
		private SubmenuPopupDirection direction;
		private PresentationContext ctx;
		
		public SubMenuItemListener(PopupMenu subMenu, SubmenuPopupDirection direction)
		{
			this.subMenu = subMenu;
			this.direction = direction;
		}
		
		
		@Override
		public void onMenuItemClicked(MenuItem menuItem, DPElement element)
		{
			if ( direction == SubmenuPopupDirection.RIGHT )
			{
				subMenu.popupToRightOf( element, ctx );
			}
			else
			{
				subMenu.popupBelow( element, ctx );
			}
		}
	}
	

	private class MenuItemInteractor extends ElementInteractor
	{
		private boolean bClosePopupOnActivate;
		
		
		public MenuItemInteractor(boolean bClosePopupOnActivate)
		{
			this.bClosePopupOnActivate = bClosePopupOnActivate;
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
				listener.onMenuItemClicked( MenuItem.this, element );
				return true;
			}
			
			return false;
		}
	}

	
	
	private Pres child;
	private MenuItemListener listener;
	
	
	protected MenuItem(Pres child, MenuItemListener listener)
	{
		this.child = child;
		this.listener = listener;
	}
	
	protected MenuItem(Pres child, PopupMenu subMenu, SubmenuPopupDirection direction)
	{
		this( child, new SubMenuItemListener( subMenu, direction ) );
	}
	
	
	protected MenuItem(String labelText, MenuItemListener listener)
	{
		this( new Label( labelText ), listener );
	}
	
	protected MenuItem(String labelText, PopupMenu subMenu, SubmenuPopupDirection direction)
	{
		this( new Label( labelText ), subMenu, direction );
	}
	
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		Pres childElem = presentAsCombinator( ctx, child );
		
		StyleSheetValues styleValues = ctx.getStyle();
		
		Painter hoverBackground = styleValues.get( Controls.menuItemHoverBackground, Painter.class );
		double padX = styleValues.get( Controls.menuItemXPadding, Double.class );
		double padY = styleValues.get( Controls.menuItemYPadding, Double.class );
		
		StyleSheetValues menuItemStyle = styleValues.withAttr( Primitive.hoverBackground, hoverBackground );
		Pres menuItem = new Bin( childElem.alignHExpand().pad( padX, padY ) );

		DPElement element = menuItem.present( ctx.withStyle( menuItemStyle ) );
		element.addInteractor( new MenuItemInteractor( ctx.getStyle().get( Controls.bClosePopupOnActivate, Boolean.class ) ) );
		
		return element;
	}
}
