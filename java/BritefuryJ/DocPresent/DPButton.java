//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.util.ArrayList;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeButton;
import BritefuryJ.DocPresent.StyleSheets.ButtonStyleSheet;

public class DPButton extends DPBin
{
	public interface ButtonListener
	{
		public void onButtonClicked(DPButton button);
	}
	
	protected static class PyButtonListener implements ButtonListener
	{
		private PyObject callable;
		
		
		public PyButtonListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public void onButtonClicked(DPButton button)
		{
			callable.__call__( Py.java2py( button ) );
		}
	}
	
	
	
	protected ButtonListener listener;
	
	
	
	public DPButton(ElementContext context)
	{
		this( context, ButtonStyleSheet.defaultStyleSheet, (ButtonListener)null );
	}

	public DPButton(ElementContext context, ButtonListener listener)
	{
		this( context, ButtonStyleSheet.defaultStyleSheet, listener );
	}

	public DPButton(ElementContext context, PyObject listener)
	{
		this( context, ButtonStyleSheet.defaultStyleSheet, new PyButtonListener( listener ) );
	}

	public DPButton(ElementContext context, ButtonStyleSheet styleSheet)
	{
		this( context, styleSheet, (ButtonListener)null );
	}

	public DPButton(ElementContext context, ButtonStyleSheet styleSheet, ButtonListener listener)
	{
		super( context, styleSheet );
		
		layoutNode = new LayoutNodeButton( this );
		this.listener = listener;
	}
	
	public DPButton(ElementContext context, ButtonStyleSheet styleSheet, PyObject listener)
	{
		this( context, styleSheet, new PyButtonListener( listener ) );
	}
	
	
	
	public void setButtonListener(ButtonListener listener)
	{
		this.listener = listener;
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		super.drawBackground( graphics );
		ButtonStyleSheet buttonStyle = (ButtonStyleSheet)styleSheet;
		
		ArrayList<PointerInterface> pointersWithinBounds = getPointersWithinBounds();
		if ( pointersWithinBounds != null  &&  pointersWithinBounds.size() > 0 )
		{
			buttonStyle.getHighlightBorder().draw( graphics, 0.0, 0.0, getAllocationX(), getAllocationY() );
		}
		else
		{
			buttonStyle.getBorder().draw( graphics, 0.0, 0.0, getAllocationX(), getAllocationY() );
		}
	}
	
	
	protected void onEnter(PointerMotionEvent event)
	{
		super.onEnter( event );
		queueFullRedraw();
	}

	protected void onLeave(PointerMotionEvent event)
	{
		queueFullRedraw();
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
				if ( listener != null )
				{
					listener.onButtonClicked( this );
				}
				return true;
			}
		}
		return false;
	}
}
