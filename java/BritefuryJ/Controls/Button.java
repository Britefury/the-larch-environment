//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Event.AbstractPointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.ClickElementInteractor;
import BritefuryJ.DocPresent.Interactor.HoverElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Button extends ControlPres
{
	public static interface ButtonListener
	{
		public void onButtonClicked(ButtonControl button, PointerButtonClickedEvent event);
	}
	
	
	public static class ButtonControl extends Control
	{
		private class ButtonInteractor implements ClickElementInteractor, HoverElementInteractor
		{
			private ButtonInteractor()
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
				DPElement buttonElement = (DPElement)element;
				if ( buttonElement.isRealised() )
				{
					if ( listener != null )
					{
						listener.onButtonClicked( ButtonControl.this, event );
					}
					if ( bClosePopupOnActivate )
					{
						buttonElement.closeContainingPopupChain();
					}
					return true;
				}
				
				return false;
			}


			@Override
			public void pointerEnter(PointerInputElement element, PointerMotionEvent event)
			{
				buttonElement.setBorder( highlightBorder );
			}

			@Override
			public void pointerLeave(PointerInputElement element, PointerMotionEvent event)
			{
				buttonElement.setBorder( buttonBorder );
			}
		}
		
		
		
		private BritefuryJ.DocPresent.Border.AbstractBorder buttonBorder, highlightBorder;
		private DPBorder buttonElement;
		private ButtonListener listener;
		private boolean bClosePopupOnActivate;
	
	
		
		protected ButtonControl(PresentationContext ctx, StyleValues style, DPBorder buttonElement, BritefuryJ.DocPresent.Border.AbstractBorder buttonBorder,
				BritefuryJ.DocPresent.Border.AbstractBorder highlightBorder, ButtonListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.buttonElement = buttonElement;
			this.buttonBorder = buttonBorder;
			this.highlightBorder = highlightBorder;
			this.listener = listener;
			this.buttonElement.addElementInteractor( new ButtonInteractor() );
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



	private Pres child;
	private ButtonListener listener;


	
	public Button(Pres child, ButtonListener listener)
	{
		this.child = child;
		this.listener = listener;
	}
	
	
	public static Button buttonWithLabel(String labelText, ButtonListener listener)
	{
		return new Button( new Label( labelText ), listener );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		BritefuryJ.DocPresent.Border.AbstractBorder border = style.get( Controls.buttonBorder, BritefuryJ.DocPresent.Border.AbstractBorder.class );
		BritefuryJ.DocPresent.Border.AbstractBorder highlightBorder = style.get( Controls.buttonHighlightBorder, BritefuryJ.DocPresent.Border.AbstractBorder.class );
		boolean bClosePopupOnActivate = style.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		Pres childElement = presentAsCombinator( ctx, Controls.useButtonAttrs( style ), child );
		DPBorder borderElement = (DPBorder)StyleSheet.style( Primitive.border.as( border ) ).applyTo( new Border( childElement.alignHCentre() ) ).present( ctx, style );
		
		return new ButtonControl( ctx, style, borderElement, border, highlightBorder, listener, bClosePopupOnActivate );
	}
}
