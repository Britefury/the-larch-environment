//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.awt.Color;
import java.awt.Font;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.Modifier;

public class ControlsStyleSheet extends StyleSheet
{
	private static final Font linkDefaultFont = new Font( "Sans serif", Font.PLAIN, 14 );
	
	
	private static abstract class LinkListener extends ElementInteractor
	{
		public void onEnter(DPWidget element, PointerMotionEvent event)
		{
			if ( element.isRealised() )
			{
				element.getPresentationArea().setCursorHand( event.getPointer() );
			}
		}

		public void onLeave(DPWidget element, PointerMotionEvent event)
		{
			if ( element.isRealised() )
			{
				element.getPresentationArea().setCursorArrow( event.getPointer() );
			}
		}



		public boolean onButtonDown(DPWidget element, PointerButtonEvent event)
		{
			return true;
		}

		public boolean onButtonUp(DPWidget element, PointerButtonEvent event)
		{
			if ( element.isRealised() )
			{
				return onLinkClicked( element, event );
			}
			
			return false;
		}
		
		
		
		protected abstract boolean onLinkClicked(DPWidget element, PointerButtonEvent event);
	}
	
	
	protected static class LinkTargetListener extends LinkListener
	{
		private String targetLocation;
		
		
		public LinkTargetListener(String targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public boolean onLinkClicked(DPWidget link, PointerButtonEvent buttonEvent)
		{
			PageController pageController = link.getPresentationArea().getPageController();
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
	
	
	protected static class PyLinkListener extends LinkListener
	{
		private PyObject callable;
		
		
		public PyLinkListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public boolean onLinkClicked(DPWidget link, PointerButtonEvent buttonEvent)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( link ), Py.java2py( buttonEvent ) ) );
		}
	}
	

	
	
	
	public ControlsStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyleSheet", PrimitiveStyleSheet.instance );

		initAttr( "linkAttrs", new AttributeSet( new String[] { "font", "foreground" }, new Object[] { linkDefaultFont, Color.blue } ) );
		
	}

	
	
	
	private PrimitiveStyleSheet linkStyleSheet = null;

	private PrimitiveStyleSheet getLinkStyleSheet()
	{
		if ( linkStyleSheet == null )
		{
			linkStyleSheet = (PrimitiveStyleSheet)get( "primitveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance ).withAttrSet( get( "linkAttrs", AttributeSet.class, AttributeSet.identity ) );
		}
		return linkStyleSheet;
	}
	
	
	
	public DPWidget link(String txt, String targetLocation)
	{
		return link( txt, new LinkTargetListener( targetLocation ) );
	}
	
	public DPWidget link(String txt, LinkListener listener)
	{
		DPWidget element = getLinkStyleSheet().staticText( txt );
		element.setInteractor( listener );
		return element;
	}
	
	public DPWidget link(String txt, PyObject listener)
	{
		return link( txt, new PyLinkListener( listener ) );
	}
}
