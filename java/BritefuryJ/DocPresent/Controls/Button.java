//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;

public class Button extends Control
{
	public static interface ButtonListener
	{
		public void onButtonClicked(Button button, PointerButtonEvent event);
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
				if ( bClosePopupOnActivate )
				{
					element.closeContainingPopupChain();
				}
				listener.onButtonClicked( Button.this, event );
				return true;
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
	
	
	
	private AbstractBorder buttonBorder, highlightBorder;
	private DPBorder buttonElement;
	private ButtonListener listener;
	private boolean bClosePopupOnActivate;


	
	protected Button(DPBorder buttonElement, AbstractBorder buttonBorder, AbstractBorder highlightBorder, ButtonListener listener, boolean bClosePopupOnActivate)
	{
		this.buttonElement = buttonElement;
		this.buttonBorder = buttonBorder;
		this.highlightBorder = highlightBorder;
		this.listener = listener;
		this.buttonElement.addInteractor( new ButtonInteractor() );
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
