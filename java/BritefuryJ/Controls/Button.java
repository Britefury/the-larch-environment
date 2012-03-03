//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Input.PointerInputElement;
import BritefuryJ.LSpace.Interactor.HoverElementInteractor;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;
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
		public void onButtonClicked(ButtonControl button, PointerButtonEvent event);
	}
	
	
	public static class ButtonControl extends Control
	{
		private class ButtonInteractor implements PushElementInteractor, HoverElementInteractor
		{
			private ButtonInteractor()
			{
			}
			
			
			@Override
			public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
			{
				LSElement buttonElement = (LSElement)element;
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
			public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
			{
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
		
		
		
		private BritefuryJ.Graphics.AbstractBorder buttonBorder, highlightBorder;
		private LSBorder buttonElement;
		private ButtonListener listener;
		private boolean bClosePopupOnActivate;
	
	
		
		protected ButtonControl(PresentationContext ctx, StyleValues style, LSBorder buttonElement, BritefuryJ.Graphics.AbstractBorder buttonBorder,
				BritefuryJ.Graphics.AbstractBorder highlightBorder, ButtonListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.buttonElement = buttonElement;
			this.buttonBorder = buttonBorder;
			this.highlightBorder = highlightBorder;
			this.listener = listener;
			this.buttonElement.addElementInteractor( new ButtonInteractor() );
		}
		
		
		public LSElement getElement()
		{
			return buttonElement;
		}
		
		public void setContents(LSElement contents)
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
		BritefuryJ.Graphics.AbstractBorder border = style.get( Controls.buttonBorder, BritefuryJ.Graphics.AbstractBorder.class );
		BritefuryJ.Graphics.AbstractBorder highlightBorder = style.get( Controls.buttonHighlightBorder, BritefuryJ.Graphics.AbstractBorder.class );
		boolean bClosePopupOnActivate = style.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		Pres childElement = presentAsCombinator( ctx, Controls.useButtonAttrs( style ), child );
		LSBorder borderElement = (LSBorder)StyleSheet.style( Primitive.border.as( border ) ).applyTo( new Border( childElement.alignHCentre() ) ).present( ctx, style );
		
		return new ButtonControl( ctx, style, borderElement, border, highlightBorder, listener, bClosePopupOnActivate );
	}
}
