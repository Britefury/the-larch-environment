//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.ContextMenuFactory;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class Hyperlink extends Pres
{
	public interface LinkListener
	{
		public void onLinkClicked(Hyperlink link, DPElement element, PointerButtonEvent event);
	}
	
	private static class LinkTargetListener implements LinkListener
	{
		private Location targetLocation;
		
		
		public LinkTargetListener(Location targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public void onLinkClicked(Hyperlink link, DPElement element, PointerButtonEvent buttonEvent)
		{
			PageController pageController = element.getRootElement().getPageController();
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
		private boolean bClosePopupOnActivate;
		
		
		public LinkInteractor(boolean bClosePopupOnActivate)
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
				listener.onLinkClicked( Hyperlink.this, element, event );
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

			MenuItem.MenuItemListener openInNewTabListener = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem menuItem, DPElement element)
				{
					pageController.openLocation( targetListener.targetLocation, PageController.OpenOperation.OPEN_IN_NEW_TAB );
				}
			};

			MenuItem.MenuItemListener openInNewWindowListener = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem menuItem, DPElement element)
				{
					pageController.openLocation( targetListener.targetLocation, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
				}
			};
			
			menu.add( new MenuItem( "Open in new tab", openInNewTabListener ) );
			menu.add( new MenuItem( "Open in new window", openInNewWindowListener ) );
		}
	}

	
	
	private String text;
	private LinkListener listener;
	private ContextMenuFactory menuFactory;
	
	
	public Hyperlink(String text, LinkListener listener)
	{
		this.text = text;
		this.listener = listener;
	}
	
	public Hyperlink(String text, Location targetLocation)
	{
		this( text, new LinkTargetListener( targetLocation ) );
		this.menuFactory = new LinkContextMenuFactory();
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		StyleSheet2 style = ctx.getStyle().get( Controls.hyperlinkAttrs, StyleSheet2.class );
		Pres textElement = style.applyTo( new Text( text ) );
		
		DPElement element = textElement.present( ctx );
		element.addInteractor( new LinkInteractor( ctx.getStyle().get( Controls.bClosePopupOnActivate, Boolean.class ) ) );
		if ( menuFactory != null )
		{
			element.addContextMenuFactory( menuFactory );
		}
		
		return element;
	}
}
