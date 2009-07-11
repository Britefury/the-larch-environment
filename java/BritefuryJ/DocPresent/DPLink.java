//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StyleSheets.LinkStyleSheet;

public class DPLink extends DPStaticText
{
	public interface LinkListener
	{
		public void onLinkClicked(DPLink link);
	}
	
	
	protected static class LinkTargetListener implements LinkListener
	{
		private String targetLocation;
		
		
		public LinkTargetListener(String targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public void onLinkClicked(DPLink link)
		{
			PageController pageController = link.presentationArea.getPageController();
			pageController.goToLocation( targetLocation );
		}
	}
	
	
	protected static class PyLinkListener implements LinkListener
	{
		private PyObject callable;
		
		
		public PyLinkListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public void onLinkClicked(DPLink link)
		{
			callable.__call__( Py.java2py( link ) );
		}
	}
	
	
	
	
	protected LinkListener listener;
	




	public DPLink(String text, String targetLocation)
	{
		this( LinkStyleSheet.defaultStyleSheet, text, new LinkTargetListener( targetLocation ) );
	}
	
	public DPLink(LinkStyleSheet styleSheet, String text, String targetLocation)
	{
		this( styleSheet, text, new LinkTargetListener( targetLocation ) );
	}

	public DPLink(String text, LinkListener listener)
	{
		this( LinkStyleSheet.defaultStyleSheet, text, listener );
	}
	
	public DPLink(LinkStyleSheet styleSheet, String text, LinkListener listener)
	{
		super( styleSheet, text );
		this.listener = listener;
	}

	public DPLink(String text, PyObject listener)
	{
		this( LinkStyleSheet.defaultStyleSheet, text, new PyLinkListener( listener ) );
	}
	
	public DPLink(LinkStyleSheet styleSheet, String text, PyObject listener)
	{
		this( styleSheet, text, new PyLinkListener( listener ) );
	}


	
	
	
	protected void onEnter(PointerMotionEvent event)
	{
		super.onEnter( event );
		if ( isRealised() )
		{
			presentationArea.setCursorHand( event.getPointer() );
		}
	}

	protected void onLeave(PointerMotionEvent event)
	{
		if ( isRealised() )
		{
			presentationArea.setCursorArrow( event.getPointer() );
		}
		super.onLeave( event );
	}



	protected boolean onButtonDown(PointerButtonEvent event)
	{
		super.onButtonDown( event );
		
		if ( isRealised() )
		{
			if ( event.button == 1 )
			{
				listener.onLinkClicked( this );
				return true;
			}
		}
		
		return false;
	}
}
