//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.ContextMenuFactory;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Hyperlink extends ControlPres
{
	public interface LinkListener
	{
		public void onLinkClicked(HyperlinkControl link, PointerButtonEvent event);
	}
	
	private static class LinkTargetListener implements LinkListener
	{
		private Location targetLocation;
		
		
		public LinkTargetListener(Location targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public void onLinkClicked(HyperlinkControl link, PointerButtonEvent buttonEvent)
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
				public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
				{
					pageController.openLocation( targetListener.targetLocation, PageController.OpenOperation.OPEN_IN_NEW_TAB );
				}
			};

			MenuItem.MenuItemListener openInNewWindowListener = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
				{
					pageController.openLocation( targetListener.targetLocation, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
				}
			};
			
			
			menu.add( MenuItem.menuItemWithLabel( "Open in new tab", openInNewTabListener ) );
			menu.add( MenuItem.menuItemWithLabel( "Open in new window", openInNewWindowListener ) );
		}
	}

	
	public static class HyperlinkControl extends Control
	{
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
					listener.onLinkClicked( HyperlinkControl.this, event );
					if ( bClosePopupOnActivate )
					{
						element.closeContainingPopupChain();
					}
				}
				
				return false;
			}
		}
	
		
		
		private DPText element;
		private LinkListener listener;
		private boolean bClosePopupOnActivate;
		
		
		protected HyperlinkControl(PresentationContext ctx, StyleValues style, DPText element, LinkListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.element = element;
			this.listener = listener;
			this.element.addInteractor( new LinkInteractor() );
			this.bClosePopupOnActivate = bClosePopupOnActivate;
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
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleSheet hyperlinkStyle = style.get( Controls.hyperlinkAttrs, StyleSheet.class );
		Pres textElement = hyperlinkStyle.applyTo( new Text( text ) );
		boolean bClosePopupOnActivate = hyperlinkStyle.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		DPText element = (DPText)textElement.present( ctx, style );
		if ( menuFactory != null )
		{
			element.addContextMenuFactory( menuFactory );
		}
		
		return new HyperlinkControl( ctx, style, element, listener, bClosePopupOnActivate );
	}
}
