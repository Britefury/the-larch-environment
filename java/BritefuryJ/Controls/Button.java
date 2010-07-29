//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class Button extends ControlPres
{
	public static interface ButtonListener
	{
		public void onButtonClicked(ButtonControl button, PointerButtonEvent event);
	}
	
	
	public static class ButtonControl extends Control
	{
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
					listener.onButtonClicked( ButtonControl.this, event );
					if ( bClosePopupOnActivate )
					{
						element.closeContainingPopupChain();
					}
					return true;
				}
				
				return false;
			}
			
			
			public void onEnter(DPElement element, PointerMotionEvent event)
			{
				buttonElement.setBorder( highlightBorder );
			}
	
			public void onLeave(DPElement element, PointerMotionEvent event)
			{
				buttonElement.setBorder( buttonBorder );
			}
		}
		
		
		
		private BritefuryJ.DocPresent.Border.Border buttonBorder, highlightBorder;
		private DPBorder buttonElement;
		private ButtonListener listener;
		private boolean bClosePopupOnActivate;
	
	
		
		protected ButtonControl(PresentationContext ctx, DPBorder buttonElement, BritefuryJ.DocPresent.Border.Border buttonBorder,
				BritefuryJ.DocPresent.Border.Border highlightBorder, ButtonListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx );
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
	public Control createControl(PresentationContext ctx)
	{
		StyleSheetValues style = ctx.getStyle();
		BritefuryJ.DocPresent.Border.Border border = style.get( Controls.buttonBorder, BritefuryJ.DocPresent.Border.Border.class );
		BritefuryJ.DocPresent.Border.Border highlightBorder = style.get( Controls.buttonHighlightBorder, BritefuryJ.DocPresent.Border.Border.class );
		boolean bClosePopupOnActivate = style.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		Pres childElement = presentAsCombinator( Controls.useButtonAttrs( ctx ), child );
		DPBorder borderElement = (DPBorder)StyleSheet2.instance.withAttr( Primitive.border, border ).applyTo( new Border( childElement ) ).present( ctx );
		
		return new ButtonControl( ctx, borderElement, border, highlightBorder, listener, bClosePopupOnActivate );
	}
}
