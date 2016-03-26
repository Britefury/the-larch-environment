//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Controls.Button.ButtonControl;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class PresentationButton extends ControlPres
{
	public static interface PresFactory
	{
		public Object createPres();
	}
	
	
	
	private static class ButtonListener implements Button.ButtonListener
	{
		private PresentationButtonControl control;
		
		@Override
		public void onButtonClicked(ButtonControl button, AbstractPointerButtonEvent event)
		{
			control.displayPres();
		}
	}

	
	public static class PresentationButtonControl extends Control
	{
		private LSElement element;
		private LSBin presContainer;
		private PresFactory presFactory;
		
		
		protected PresentationButtonControl(PresentationContext ctx, StyleValues style, LSElement element, LSBin presContainer, PresFactory presFactory)
		{
			super( ctx, style );
			this.element = element;
			this.presContainer = presContainer;
			this.presFactory = presFactory;
		}
		
		
		
		
		@Override
		public LSElement getElement()
		{
			return element;
		}
		
		
		
		private void displayPres()
		{
			LSElement p = Pres.coerce( presFactory.createPres() ).present( ctx, style );
			presContainer.setChild( p.layoutWrap( style.get( Primitive.hAlign, HAlignment.class ), style.get( Primitive.vAlign, VAlignment.class ) ) );
		}
	}

	
	
	

	private Pres buttonContent;
	private PresFactory presFactory;
	
	
	
	
	public PresentationButton(Object buttonContent, PresFactory presFactory)
	{
		this.buttonContent = Pres.coerce( buttonContent );
		this.presFactory = presFactory;
	}
	
	
	public static PresentationButton buttonWithLabel(String labelText, PresFactory presFactory)
	{
		return new PresentationButton( new Label( labelText ), presFactory );
	}
	
	
	
	
	
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.usePresentationButtonAttrs( style );
		
		double spacing = style.get( Controls.presentationButtonSpacing, Double.class );
		
		ButtonListener buttonListener = new ButtonListener();
		
		Button button = new Button( buttonContent, buttonListener );
		
		Bin presBin = new Bin( null );
		LSBin binElement = (LSBin)presBin.present( ctx, usedStyle );
		
		Pres presButton = StyleSheet.style( Primitive.columnSpacing.as( spacing ) ).applyTo( new Column( new Object[] { button, binElement } ) );
		LSElement element = presButton.present( ctx, usedStyle );
		
		PresentationButtonControl control = new PresentationButtonControl( ctx, usedStyle, element, binElement, presFactory );
		
		buttonListener.control = control;
		
		return control;
	}
}
