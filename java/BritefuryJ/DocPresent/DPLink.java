//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheetField;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldCascading;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldSet;
import BritefuryJ.DocPresent.Util.TextVisual;

public class DPLink extends DPStaticText
{
	protected static ElementStyleSheetField link_fontField = ElementStyleSheetField.newField( "link_font", Font.class );
	protected static ElementStyleSheetField link_paintField = ElementStyleSheetField.newField( "link_paint", Paint.class );
	protected static ElementStyleSheetField link_bMixedSizeCapsField = ElementStyleSheetField.newField( "link_bMixedSizeCaps", Boolean.class );
	
	protected static StyleSheetValueFieldCascading link_fontValueField = StyleSheetValueFieldCascading.newField( "link_font", Font.class, new Font( "Sans serif", Font.PLAIN, 14 ), link_fontField );
	protected static StyleSheetValueFieldCascading link_paintValueField = StyleSheetValueFieldCascading.newField( "link_paint", Paint.class, Color.blue, link_paintField );
	protected static StyleSheetValueFieldCascading link_bMixedSizeCapsValueField = StyleSheetValueFieldCascading.newField( "link_bMixedSizeCaps", Boolean.class, false, link_bMixedSizeCapsField );
	
	
	protected static StyleSheetValueFieldSet useStyleSheetFields_Link = useStyleSheetFields_Element.join( link_fontValueField, link_paintValueField, link_bMixedSizeCapsValueField );
	
	
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
		this( null, text, new LinkTargetListener( targetLocation ) );
	}
	
	public DPLink(ElementStyleSheet styleSheet, String text, String targetLocation)
	{
		this( styleSheet, text, new LinkTargetListener( targetLocation ) );
	}

	public DPLink(String text, LinkListener listener)
	{
		this( null, text, listener );
	}
	
	public DPLink(ElementStyleSheet styleSheet, String text, LinkListener listener)
	{
		super( styleSheet, text );
		this.listener = listener;
	}

	public DPLink(String text, PyObject listener)
	{
		this( null, text, new PyLinkListener( listener ) );
	}
	
	public DPLink(ElementStyleSheet styleSheet, String text, PyObject listener)
	{
		this( styleSheet, text, new PyLinkListener( listener ) );
	}


	
	
	
	protected TextVisual createTextVisual()
	{
		return TextVisual.getTextVisual( getPresentationArea(), text, (Font)styleSheetValues.get( link_fontValueField ), (Boolean)styleSheetValues.get( link_bMixedSizeCapsValueField ) );
	}
	
	protected Paint getTextPaint()
	{
		return (Paint)styleSheetValues.get( link_paintValueField );
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
		return event.button == 1;
	}

	protected boolean onButtonUp(PointerButtonEvent event)
	{
		super.onButtonUp( event );
		
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



	protected StyleSheetValueFieldSet getUsedStyleSheetValueFields()
	{
		return useStyleSheetFields_Link;
	}
}
