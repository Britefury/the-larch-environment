//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.ContextMenuFactory;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Controls.MenuItem.MenuItemListener;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.Modifier;

public class Hyperlink extends Control
{
	public interface LinkListener
	{
		public void onLinkClicked(Hyperlink link, PointerButtonEvent event);
	}
	
	private static class LinkTargetListener implements LinkListener
	{
		private Location targetLocation;
		
		
		public LinkTargetListener(Location targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public void onLinkClicked(Hyperlink link, PointerButtonEvent buttonEvent)
		{
			PageController pageController = link.getElement().getRootElement().getPageController();
			if ( ( buttonEvent.getPointer().getModifiers() & Modifier.CTRL ) != 0 )
			{
				if ( buttonEvent.getButton() == 1  ||  buttonEvent.getButton() == 2 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
				}
			}
			else
			{
				if ( buttonEvent.getButton() == 1 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_CURRENT_TAB );
				}
				else if ( buttonEvent.getButton() == 2 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_NEW_TAB );
				}
			}
		}
	}
	
	
	private class LinkInteractor extends ElementInteractor
	{
		public LinkInteractor()
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
				listener.onLinkClicked( Hyperlink.this, event );
			}
			
			return false;
		}
	}
	
	private class LinkContextMenuFactory implements ContextMenuFactory
	{
		@Override
		public void buildContextMenu(final DPElement element, PopupMenu menu)
		{
			final PageController pageController = element.getRootElement().getPageController();
			final LinkTargetListener targetListener = (LinkTargetListener)listener;

			MenuItemListener openInNewTabListener = new MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem menuItem)
				{
					pageController.openLocation( targetListener.targetLocation, PageController.OpenOperation.OPEN_IN_NEW_TAB );
				}
			};

			MenuItemListener openInNewWindowListener = new MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem menuItem)
				{
					pageController.openLocation( targetListener.targetLocation, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
				}
			};
			
			
			menu.add( styleSheet.withClosePopupOnActivate().menuItemWithLabel( "Open in new tab", openInNewTabListener ).getElement() );
			menu.add( styleSheet.withClosePopupOnActivate().menuItemWithLabel( "Open in new window", openInNewWindowListener ).getElement() );
		}
	}

	
	
	private DPText element;
	private LinkListener listener;
	private ControlsStyleSheet styleSheet;
	private boolean bClosePopupOnActivate;
	
	
	protected Hyperlink(DPText element, LinkListener listener, boolean bClosePopupOnActivate, ControlsStyleSheet styleSheet)
	{
		this.element = element;
		this.listener = listener;
		this.element.addInteractor( new LinkInteractor() );
		this.bClosePopupOnActivate = bClosePopupOnActivate;
		this.styleSheet = styleSheet;
	}
	
	protected Hyperlink(DPText element, Location targetLocation, boolean bClosePopupOnActivate, ControlsStyleSheet styleSheet)
	{
		this( element, new LinkTargetListener( targetLocation ), bClosePopupOnActivate, styleSheet );
		this.element.addContextMenuFactory( new LinkContextMenuFactory() );
	}
	
	
	public DPElement getElement()
	{
		return element;
	}
	
	
	public String getText()
	{
		return element.getText();
	}
	
	public void setText(String text)
	{
		element.setText( text );
	}
}
