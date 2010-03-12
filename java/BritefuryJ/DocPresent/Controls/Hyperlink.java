//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PageController;
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
		private String targetLocation;
		
		
		public LinkTargetListener(String targetLocation)
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
	
	
	private static class PyLinkListener implements LinkListener
	{
		private PyObject callable;
		
		
		public PyLinkListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public boolean onLinkClicked(Hyperlink link, PointerButtonEvent buttonEvent)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( link ), Py.java2py( buttonEvent ) ) );
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
	
	protected Hyperlink(DPText element, String targetLocation)
	{
		this( element, new LinkTargetListener( targetLocation ) );
	}
	
	protected Hyperlink(DPText element, PyObject listener)
	{
		this( element, new PyLinkListener( listener ) );
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
