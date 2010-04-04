//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.Modifier;

public class Hyperlink extends Control
{
	public interface LinkListener
	{
		public boolean onLinkClicked(Hyperlink link, PointerButtonEvent event);
	}
	
	private static class LinkTargetListener implements LinkListener
	{
		private Location targetLocation;
		
		
		public LinkTargetListener(Location targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public boolean onLinkClicked(Hyperlink link, PointerButtonEvent buttonEvent)
		{
			PageController pageController = link.getElement().getPresentationArea().getPageController();
			if ( ( buttonEvent.getPointer().getModifiers() & Modifier.CTRL ) != 0 )
			{
				if ( buttonEvent.getButton() == 1  ||  buttonEvent.getButton() == 2 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
					return true;
				}
			}
			else
			{
				if ( buttonEvent.getButton() == 1 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_CURRENT_TAB );
					return true;
				}
				else if ( buttonEvent.getButton() == 2 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_NEW_TAB );
					return true;
				}
			}

			return false;
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
				return listener.onLinkClicked( Hyperlink.this, event );
			}
			
			return false;
		}
	}

	
	
	private DPText element;
	private LinkListener listener;
	
	
	protected Hyperlink(DPText element, LinkListener listener)
	{
		this.element = element;
		this.listener = listener;
		this.element.addInteractor( new LinkInteractor() );
	}
	
	protected Hyperlink(DPText element, Location targetLocation)
	{
		this( element, new LinkTargetListener( targetLocation ) );
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
