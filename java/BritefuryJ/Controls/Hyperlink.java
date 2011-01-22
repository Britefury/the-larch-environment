//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Matcher;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Event.AbstractPointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.ClickElementInteractor;
import BritefuryJ.DocPresent.Interactor.ContextMenuElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Hyperlink extends ControlPres
{
	public interface LinkListener
	{
		public void onLinkClicked(HyperlinkControl link, PointerButtonClickedEvent event);
	}
	
	private static class LinkTargetListener implements LinkListener
	{
		private Location targetLocation;
		
		
		public LinkTargetListener(Location targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public void onLinkClicked(HyperlinkControl link, PointerButtonClickedEvent buttonEvent)
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
	
	
	private static class LinkContextMenuFactory implements ContextMenuElementInteractor
	{
		private LinkTargetListener listener;
		
		
		public LinkContextMenuFactory(LinkTargetListener listener)
		{
			this.listener = listener;
		}
		
		
		@Override
		public boolean contextMenu(PointerInputElement element, PopupMenu menu)
		{
			final DPElement linkElement = (DPElement)element;
			final PageController pageController = linkElement.getRootElement().getPageController();

			MenuItem.MenuItemListener openInNewTabListener = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
				{
					pageController.openLocation( listener.targetLocation, PageController.OpenOperation.OPEN_IN_NEW_TAB );
				}
			};

			MenuItem.MenuItemListener openInNewWindowListener = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
				{
					pageController.openLocation( listener.targetLocation, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
				}
			};
			
			
			menu.add( MenuItem.menuItemWithLabel( "Open in new tab", openInNewTabListener ) );
			menu.add( MenuItem.menuItemWithLabel( "Open in new window", openInNewWindowListener ) );
			
			return true;
		}
	}

	

	private static interface LocationFn
	{
		public Location apply(Location location);
	}
	
	private static interface LinkListenerFactory
	{
		public LinkListener createLinkListener(LocationFn locFn);
		public ContextMenuElementInteractor createContextMenuInteractor(LinkListener listener);
	}
	
	private static class FixedLinkListenerFactory implements LinkListenerFactory
	{
		private LinkListener linkListener;
		
		
		public FixedLinkListenerFactory(LinkListener linkListener)
		{
			this.linkListener = linkListener; 
		}
		
		
		public LinkListener createLinkListener(LocationFn locFn)
		{
			return linkListener;
		}
		
		public ContextMenuElementInteractor createContextMenuInteractor(LinkListener listener)
		{
			return null;
		}
	}
	
	
	private static class LocationLinkListenerFactory implements LinkListenerFactory
	{
		private Location location;
		
		
		public LocationLinkListenerFactory(Location location)
		{
			this.location = location; 
		}
		
		
		public LinkListener createLinkListener(LocationFn locFn)
		{
			return new LinkTargetListener( locFn.apply( location ) );
		}
		
		public ContextMenuElementInteractor createContextMenuInteractor(LinkListener listener)
		{
			return new LinkContextMenuFactory( (LinkTargetListener)listener );
		}
	}
	
	
	
	
	public static class HyperlinkControl extends Control
	{
		private class LinkInteractor implements ClickElementInteractor
		{
			public LinkInteractor()
			{	
			}
			
			@Override
			public boolean testClickEvent(PointerInputElement element, AbstractPointerButtonEvent event)
			{
				return true;
			}

			@Override
			public boolean buttonClicked(PointerInputElement element, PointerButtonClickedEvent event)
			{
				DPElement hyperlinkElement = (DPElement)element;
				if ( hyperlinkElement.isRealised() )
				{
					listener.onLinkClicked( HyperlinkControl.this, event );
					if ( bClosePopupOnActivate )
					{
						hyperlinkElement.closeContainingPopupChain();
					}
					return true;
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
			this.element.addElementInteractor( new LinkInteractor() );
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
	private LinkListenerFactory listenerFactory;
	
	
	public Hyperlink(String text, LinkListener listener)
	{
		this( text, new FixedLinkListenerFactory( listener ) );
	}
	
	public Hyperlink(String text, Location targetLocation)
	{
		this( text, new LocationLinkListenerFactory( targetLocation ) );
	}
	
	private Hyperlink(String text, LinkListenerFactory listenerFactory)
	{
		this.text = text;
		this.listenerFactory = listenerFactory;
	}
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleSheet hyperlinkStyle = style.get( Controls.hyperlinkAttrs, StyleSheet.class );
		Pres textElement = hyperlinkStyle.applyTo( new Text( text ) );
		boolean bClosePopupOnActivate = hyperlinkStyle.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		final SimpleAttributeTable subjectContext = ctx.getSubjectContext();
		LocationFn locFn = new LocationFn()
		{
			@Override
			public Location apply(Location location)
			{
				if ( subjectContext != null )
				{
					String loc = location.getLocationString();
					Matcher m;
					
					m = Location.locationVarPattern.matcher( loc );
					while ( m.find( 0 ) )
					{
						String varName = loc.substring( m.start()+1, m.end() );
						
						Object value = subjectContext.getOptional( varName );
						String valueLoc = "None";
						
						if ( value instanceof String )
						{
							valueLoc = (String)value;
						}
						else if ( value instanceof Location )
						{
							valueLoc = ((Location)value).getLocationString();
						}
						
						loc = loc.replace( "$" + varName, valueLoc );
						m = Location.locationVarPattern.matcher( loc );
					}
					
					return new Location( loc );
				}
				else
				{
					return location;
				}
			}
		};
		
		LinkListener listener = listenerFactory.createLinkListener( locFn );
		ContextMenuElementInteractor contextMenuInteractor = listenerFactory.createContextMenuInteractor( listener );
		
		DPText element = (DPText)textElement.present( ctx, style );
		if ( contextMenuInteractor != null )
		{
			element.addContextMenuInteractor( contextMenuInteractor );
		}
		
		return new HyperlinkControl( ctx, style, element, listener, bClosePopupOnActivate );
	}
}
