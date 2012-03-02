//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.PushElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Label;
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
		private class ButtonInteractor implements PushElementInteractor
		{
			private ButtonInteractor()
			{
			}
			
			
			@Override
			public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
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
			public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
			{
			}
		}
		
		
		
		private DPElement buttonElement;
		private ButtonListener listener;
		private boolean bClosePopupOnActivate;
	
	
		
		protected ButtonControl(PresentationContext ctx, StyleValues style, DPElement buttonElement, ButtonListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.buttonElement = buttonElement;
			this.listener = listener;
			this.buttonElement.addElementInteractor( new ButtonInteractor() );
		}
		
		
		public DPElement getElement()
		{
			return buttonElement;
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
		boolean bClosePopupOnActivate = style.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		Pres childElement = presentAsCombinator( ctx, Controls.useButtonAttrs( style.withAttrs( style.get( Controls.buttonAttrs, StyleSheet.class ) ) ), child );
		DPElement borderElement = border.surround( childElement.alignHCentre() ).present( ctx, style );
		
		return new ButtonControl( ctx, style, borderElement, listener, bClosePopupOnActivate );
	}
}
