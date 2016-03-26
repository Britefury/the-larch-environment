//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Button extends ControlPres
{
	public static interface ButtonListener
	{
		public void onButtonClicked(ButtonControl button, AbstractPointerButtonEvent event);
	}
	
	
	public static class ButtonControl extends Control
	{
		private class ButtonInteractor implements ClickElementInteractor
		{
			private ButtonInteractor()
			{
			}
			
			
			@Override
			public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
			{
				return element.isRealised();
			}

			@Override
			public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
			{
				if ( element.isRealised() )
				{
					if ( listener != null )
					{
						listener.onButtonClicked( ButtonControl.this, event );
					}
					return true;
				}

				return false;
			}
		}
		
		
		
		private LSElement buttonElement;
		private ButtonListener listener;

	
		
		protected ButtonControl(PresentationContext ctx, StyleValues style, LSElement buttonElement, ButtonListener listener)
		{
			super( ctx, style );
			this.buttonElement = buttonElement;
			this.listener = listener;
			this.buttonElement.addElementInteractor( new ButtonInteractor() );
		}
		
		
		public LSElement getElement()
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
		LSElement borderElement = border.surround( childElement.alignHCentre() ).present( ctx, style );
		
		return new ButtonControl( ctx, style, borderElement, listener );
	}
}
