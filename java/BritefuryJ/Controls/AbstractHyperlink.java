//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.net.URI;

import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSElement.PropertyValue;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.LSpace.Input.ObjectDndHandler.SourceDataFn;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.Interactor.ContextMenuElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.UI.BubblePopup;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class AbstractHyperlink extends ControlPres
{
	private static class TargetKey
	{
		private static final TargetKey instance = new TargetKey();
	}
	
	
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

	
	
	protected static class LinkTargetContextMenuFactory implements ContextMenuElementInteractor
	{
		private Subject targetSubject;
		
		
		public LinkTargetContextMenuFactory(Subject targetSubject)
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

	
	protected static class LinkURIContextMenuFactory implements ContextMenuElementInteractor, ClipboardOwner
	{
		private URI uri;
		
		
		public LinkURIContextMenuFactory(URI uri)
		{
			this.uri = uri;
		}
		
		
		@Override
		public boolean contextMenu(LSElement element, PopupMenu menu)
		{
			MenuItem.MenuItemListener open = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
				{
					openURIInExternalBrowser( menuItem, uri );
				}
			};

			MenuItem.MenuItemListener copyURI = new MenuItem.MenuItemListener()
			{
				@Override
				public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
				{
					StringSelection contents = new StringSelection( uri.toASCIIString() );
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents( contents, LinkURIContextMenuFactory.this );
				}
			};
			
			
			menu.add( MenuItem.menuItemWithLabel( "Open", open ) );
			menu.add( MenuItem.menuItemWithLabel( "Copy link location", copyURI ) );
			
			return true;
		}


		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents)
		{
		}
	}

	
	private static final SourceDataFn sourceDataFn = new ObjectDndHandler.SourceDataFn()
	{
		@Override
		public Object createSourceData(LSElement sourceElement, int aspect)
		{
			PropertyValue value = sourceElement.getProperty( TargetKey.instance );
			return value.getValue();
		}
	};
	
	private static final ObjectDndHandler.DragSource dndSource = new ObjectDndHandler.DragSource( Subject.class, sourceDataFn );

	
	
	protected static class LinkURIListener implements LinkListener
	{
		private URI uri;
		
		
		public LinkURIListener(URI uri)
		{
			this.uri = uri;
		}
		
		public void onLinkClicked(AbstractHyperlinkControl link, PointerButtonClickedEvent buttonEvent)
		{
			openURIInExternalBrowser( link, uri );
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
	protected ContextMenuElementInteractor contextMenuInteractor;
	private Subject targetSubject;
	private Pres contents;
	
	
	
	public AbstractHyperlink(Object contents, LinkListener listener)
	{
		this.listener = listener;
		this.contents = Pres.coerce( contents );
	}

	public AbstractHyperlink(Object contents, Subject targetSubject)
	{
		this.listener = new LinkTargetListener( targetSubject );
		this.contextMenuInteractor = new LinkTargetContextMenuFactory( targetSubject );
		this.targetSubject = targetSubject;
		this.contents = Pres.coerce( contents );
	}

	public AbstractHyperlink(Object contents, URI uri)
	{
		this.listener = new LinkURIListener( uri );
		this.contextMenuInteractor = new LinkURIContextMenuFactory( uri );
		this.contents = Pres.coerce( contents );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleSheet hyperlinkStyle = style.get( Controls.hyperlinkAttrs, StyleSheet.class );
		Pres contentsPres = hyperlinkStyle.applyTo( contents );
		boolean bClosePopupOnActivate = hyperlinkStyle.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		LSElement contentsElement = contentsPres.present( ctx, style );
		Control ctl = createHyperlinkControl( ctx, style, contentsElement, bClosePopupOnActivate, listener, contextMenuInteractor );
		LSElement controlElement = ctl.getElement();

		if ( contextMenuInteractor != null )
		{
			controlElement.addContextMenuInteractor( contextMenuInteractor );
		}
		if ( targetSubject != null )
		{
			//controlElement.addDragSource( dndSource );
			controlElement.setProperty( TargetKey.instance, targetSubject );
		}

		return ctl;
	}


	protected abstract Control createHyperlinkControl(PresentationContext ctx, StyleValues style, LSElement contentsElement, boolean bClosePopupOnActivate, LinkListener listener,
			ContextMenuElementInteractor contextMenuInteractor);

	

	private static void openURIInExternalBrowser(Control link, URI uri)
	{
		try
		{
			Desktop.getDesktop().browse( uri );
		}
		catch (IOException e)
		{
			Pres warning = new Label( "Unable to launch browser" );
			BubblePopup.popupInBubbleAdjacentTo( warning, link.getElement(), Anchor.TOP, true, false );
		}
	}
}