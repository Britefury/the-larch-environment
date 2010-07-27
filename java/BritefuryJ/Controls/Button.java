//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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

public class Button extends Pres
{
	public static interface ButtonListener
	{
		public void onButtonClicked(Button button, DPElement element, PointerButtonEvent event);
	}
	
	
	private class ButtonInteractor extends ElementInteractor
	{
		private BritefuryJ.DocPresent.Border.Border border, highlightBorder;
		private boolean bClosePopupOnActivate;
		
		private ButtonInteractor(BritefuryJ.DocPresent.Border.Border border, BritefuryJ.DocPresent.Border.Border highlightBorder, boolean bClosePopupOnActivate)
		{
			this.border = border;
			this.highlightBorder = highlightBorder;
			this.bClosePopupOnActivate = bClosePopupOnActivate;
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
				listener.onButtonClicked( Button.this, element, event );
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
			((DPBorder)element).setBorder( border );
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
	public DPElement present(PresentationContext ctx)
	{
		StyleSheetValues style = ctx.getStyle();
		BritefuryJ.DocPresent.Border.Border border = style.get( Controls.buttonBorder, BritefuryJ.DocPresent.Border.Border.class );
		BritefuryJ.DocPresent.Border.Border highlightBorder = style.get( Controls.buttonHighlightBorder, BritefuryJ.DocPresent.Border.Border.class );
		boolean bClosePopupOnActivate = style.get( Controls.bClosePopupOnActivate, Boolean.class );
		
		Pres childElement = presentAsCombinator( Controls.useButtonAttrs( ctx ), child );
		DPElement borderElement = StyleSheet2.instance.withAttr( Primitive.border, border ).applyTo( new Border( childElement ) ).present( ctx );
		borderElement.addInteractor( new ButtonInteractor( border, highlightBorder, bClosePopupOnActivate ) );
		
		return borderElement;
	}
}
