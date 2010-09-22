//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Arrow;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;
import BritefuryJ.DocPresent.Event.AbstractPointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.ClickElementInteractor;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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
					subMenu.popupToRightOf( menuItem.getElement(), ctx, style );
				}
				else
				{
					subMenu.popupBelow( menuItem.getElement(), ctx, style );
				}
			}
		}
		
	
		private class MenuItemInteractor implements ClickElementInteractor
		{
			public MenuItemInteractor()
			{	
			}
			
			@Override
			public boolean testClickEvent(PointerInputElement element, AbstractPointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public boolean buttonClicked(PointerInputElement element, PointerButtonClickedEvent event)
			{
				DPElement menuItemElement = (DPElement)element;
				if ( menuItemElement.isRealised() )
				{
					if ( bClosePopupOnActivate )
					{
						menuItemElement.closeContainingPopupChain();
					}
					listener.onMenuItemClicked( MenuItemControl.this );
					return true;
				}
				
				return false;
			}
		}
	
		
		
		private DPBin element;
		private MenuItemListener listener;
		private boolean bClosePopupOnActivate;
		
		
		protected MenuItemControl(PresentationContext ctx, StyleValues style, DPBin element, MenuItemListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.element = element;
			this.listener = listener;
			this.element.addElementInteractor( new MenuItemInteractor() );
			this.bClosePopupOnActivate = bClosePopupOnActivate;
		}
		
		protected MenuItemControl(PresentationContext ctx, StyleValues style, DPBin element, PopupMenu subMenu, SubmenuPopupDirection direction, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.element = element;
			this.listener = new SubMenuItemListener( subMenu, direction );
			this.element.addElementInteractor( new MenuItemInteractor() );
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
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useMenuItemAttrs( style );
		Pres childElem;
		if ( subMenu != null )
		{
			double spacing = style.get( Controls.menuItemSubmenuArrowSpacing, Double.class );
			double arrowSize = style.get( Controls.menuItemSubmenuArrowSize, Double.class );
			Painter arrowPainter = style.get( Controls.menuItemSubmenuArrowPainter, Painter.class );
			Arrow.Direction arrowDirection;
			if ( direction == SubmenuPopupDirection.RIGHT )
			{
				arrowDirection = Arrow.Direction.RIGHT;
			}
			else if ( direction == SubmenuPopupDirection.DOWN )
			{
				arrowDirection = Arrow.Direction.DOWN;
			}
			else
			{
				throw new RuntimeException( "Invalid submenu popup direction" );
			}
			Pres childWithArrow = StyleSheet.instance.withAttr( Primitive.rowSpacing, spacing ).applyTo( 
					new Row( new Pres[] { child.alignHExpand().alignVRefYExpand(),
					StyleSheet.instance.withAttr( Primitive.shapePainter, arrowPainter ).applyTo( new Arrow( arrowDirection, arrowSize ).alignVCentre() ) } ) );
			childElem = presentAsCombinator( ctx, usedStyle, childWithArrow );
		}
		else
		{
			childElem = presentAsCombinator( ctx, usedStyle, child );
		}
		
		Painter hoverBackground = style.get( Controls.menuItemHoverBackground, Painter.class );
		double padX = style.get( Controls.menuItemXPadding, Double.class );
		double padY = style.get( Controls.menuItemYPadding, Double.class );
		boolean bClosePopupOnActivate = style.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		StyleValues menuItemStyle = style.withAttr( Primitive.hoverBackground, hoverBackground );
		Pres menuItem = new Bin( childElem.alignHExpand().pad( padX, padY ) );

		DPBin element = (DPBin)menuItem.present( ctx, menuItemStyle );
		
		
		if ( subMenu != null )
		{
			return new MenuItemControl( ctx, style, element, subMenu, direction, false );
		}
		else
		{
			return new MenuItemControl( ctx, style, element, listener, bClosePopupOnActivate );
		}
	}
}
