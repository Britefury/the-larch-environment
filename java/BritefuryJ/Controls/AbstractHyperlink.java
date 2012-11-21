//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.Interactor.ContextMenuElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class AbstractHyperlink extends ControlPres
{
	public interface LinkListener
	{
		public void onLinkClicked(AbstractHyperlinkControl link, PointerButtonClickedEvent event);
	}

	
	
	protected static class LinkTargetListener implements LinkListener
	{
		private Subject targetSubject;
		
		
		public LinkTargetListener(Subject targetSubject)
		{
			this.targetSubject = targetSubject;
		}
		
		public void onLinkClicked(AbstractHyperlinkControl link, PointerButtonClickedEvent buttonEvent)
		{
			PageController pageController = link.getElement().getRootElement().getPageController();
			if ( ( buttonEvent.getPointer().getModifiers() & Modifier.CTRL ) != 0 )
			{
				if ( buttonEvent.getButton() == 1  ||  buttonEvent.getButton() == 2 )
				{
					pageController.openSubject( targetSubject, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
				}
			}
			else
			{
				if ( buttonEvent.getButton() == 1 )
				{
					pageController.openSubject( targetSubject, PageController.OpenOperation.OPEN_IN_CURRENT_TAB );
				}
				else if ( buttonEvent.getButton() == 2 )
				{
					pageController.openSubject( targetSubject, PageController.OpenOperation.OPEN_IN_NEW_TAB );
				}
			}
		}
	}

	
	
	protected static class LinkContextMenuFactory implements ContextMenuElementInteractor
	{
		private Subject targetSubject;
		
		
		public LinkContextMenuFactory(Subject targetSubject)
		{
			this.targetSubject = targetSubject;
		}
		
		
		@Override
		public boolean contextMenu(LSElement element, PopupMenu menu)
		{
			final PageController pageController = element.getRootElement().getPageController();

			MenuItem.MenuItemListener openInNewTabListener = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
				{
					pageController.openSubject( targetSubject, PageController.OpenOperation.OPEN_IN_NEW_TAB );
				}
			};

			MenuItem.MenuItemListener openInNewWindowListener = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
				{
					pageController.openSubject( targetSubject, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
				}
			};
			
			
			menu.add( MenuItem.menuItemWithLabel( "Open in new tab", openInNewTabListener ) );
			menu.add( MenuItem.menuItemWithLabel( "Open in new window", openInNewWindowListener ) );
			
			return true;
		}
	}

	
	public static abstract class AbstractHyperlinkControl extends Control
	{
		private class LinkInteractor implements ClickElementInteractor
		{
			public LinkInteractor()
			{	
			}
			
			@Override
			public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
			{
				return true;
			}

			@Override
			public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
			{
				if ( element.isRealised() )
				{
					listener.onLinkClicked( AbstractHyperlinkControl.this, event );
					if ( bClosePopupOnActivate )
					{
						element.closeContainingPopupChain();
					}
					return true;
				}
				
				return false;
			}
		}
	
		
		
		private LSElement element;
		private LinkListener listener;
		private boolean bClosePopupOnActivate;
		
		
		protected AbstractHyperlinkControl(PresentationContext ctx, StyleValues style, LSElement element, LinkListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.element = element;
			this.listener = listener;
			this.element.addElementInteractor( new LinkInteractor() );
			this.bClosePopupOnActivate = bClosePopupOnActivate;
		}
		
		
		public LSElement getElement()
		{
			return element;
		}
	}
	
	
	
	protected LinkListener listener;
	protected LinkContextMenuFactory contextMenuInteractor;
	private Pres contents;
	
	
	
	public AbstractHyperlink(Object contents, LinkListener listener)
	{
		this.listener = listener;
		this.contents = Pres.coerce( contents );
	}

	public AbstractHyperlink(Object contents, Subject targetSubject)
	{
		this.listener = new LinkTargetListener( targetSubject );
		this.contextMenuInteractor = new LinkContextMenuFactory( targetSubject );
		this.contents = Pres.coerce( contents );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleSheet hyperlinkStyle = style.get( Controls.hyperlinkAttrs, StyleSheet.class );
		Pres contentsPres = hyperlinkStyle.applyTo( contents );
		boolean bClosePopupOnActivate = hyperlinkStyle.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		LSElement contentsElement = contentsPres.present( ctx, style );
		if ( contextMenuInteractor != null )
		{
			contentsElement.addContextMenuInteractor( contextMenuInteractor );
		}

		return createHyperlinkControl( ctx, style, contentsElement, bClosePopupOnActivate, listener, contextMenuInteractor );
	}


	protected abstract Control createHyperlinkControl(PresentationContext ctx, StyleValues style, LSElement contentsElement, boolean bClosePopupOnActivate, LinkListener listener,
			ContextMenuElementInteractor contextMenuInteractor);
}