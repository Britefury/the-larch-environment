//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;

public class Button extends Control
{
	public static interface ButtonListener
	{
		public boolean onButtonClicked(Button button, PointerButtonEvent event);
	}
	
	
	private static class PyButtonListener implements ButtonListener
	{
		private PyObject callable;
		
		
		public PyButtonListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public boolean onButtonClicked(Button button, PointerButtonEvent buttonEvent)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( button ), Py.java2py( buttonEvent ) ) );
		}
	}
	

	private class ButtonInteractor extends ElementInteractor
	{
		private ButtonInteractor()
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
				return listener.onButtonClicked( Button.this, event );
			}
			
			return false;
		}
		
		
		public void onEnter(DPElement element, PointerMotionEvent event)
		{
			((DPBorder)element).setBorder( highlightBorder );
		}

		public void onLeave(DPElement element, PointerMotionEvent event)
		{
			((DPBorder)element).setBorder( buttonBorder );
		}
	}
	
	
	
	private Border buttonBorder, highlightBorder;
	private DPBorder buttonElement;
	private ButtonListener listener;


	
	protected Button(DPBorder buttonElement, Border buttonBorder, Border highlightBorder, ButtonListener listener)
	{
		this.buttonElement = buttonElement;
		this.buttonBorder = buttonBorder;
		this.highlightBorder = highlightBorder;
		this.listener = listener;
		this.buttonElement.addInteractor( new ButtonInteractor() );
	}
	
	protected Button(DPBorder buttonElement, Border buttonBorder, Border highlightBorder, PyObject listener)
	{
		this( buttonElement, buttonBorder, highlightBorder, new PyButtonListener( listener ) );
	}
	
	
	public DPElement getElement()
	{
		return buttonElement;
	}
	
	public void setContents(DPElement contents)
	{
		buttonElement.setChild( contents );
	}
}
