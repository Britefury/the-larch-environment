//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Controls.Button;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class ButtonTestPage extends SystemPage
{
	protected ButtonTestPage()
	{
		register( "tests.controls.button" );
	}
	
	
	public String getTitle()
	{
		return "Button test";
	}
	
	protected String getDescription()
	{
		return "Button element: performs an action when clicked";
	}
	
	
	private class ButtonColourChanger implements Button.ButtonListener
	{
		private DPProxy parentElement;
		private PrimitiveStyleSheet style;
		
		
		public ButtonColourChanger(DPProxy parentElement, PrimitiveStyleSheet style)
		{
			this.parentElement = parentElement;
			this.style = style;
		}


		public void onButtonClicked(Button button, PointerButtonEvent event)
		{
			parentElement.setChild( colouredText( style ) );
		}
	}

	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFontSize( 18 );
	private static PrimitiveStyleSheet blackText = styleSheet.withForeground( Color.black );
	private static PrimitiveStyleSheet redText = styleSheet.withForeground( Color.red );
	private static PrimitiveStyleSheet greenText = styleSheet.withForeground( new Color( 0.0f, 0.5f, 0.0f ) );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	protected DPElement section(String title, DPElement contents)
	{
		DPElement heading = headingStyleSheet.staticText( title );
		
		return styleSheet.vbox( new DPElement[] { heading.padY( 10.0 ), contents } );
	}
	
	protected DPElement colouredText(PrimitiveStyleSheet style)
	{
		return style.staticText( "Change the colour of this text using the buttons below." );
	}
	
	protected DPElement createContents()
	{
		DPProxy colouredTextProxy = styleSheet.proxy( colouredText( blackText ) );
		Button blackButton = controlsStyleSheet.button( styleSheet.staticText( "Black" ), new ButtonColourChanger( colouredTextProxy, blackText ) );
		Button redButton = controlsStyleSheet.button( styleSheet.staticText( "Red" ), new ButtonColourChanger( colouredTextProxy, redText ) );
		Button greenButton = controlsStyleSheet.button( styleSheet.staticText( "Green" ), new ButtonColourChanger( colouredTextProxy, greenText ) );
		DPElement colourLinks = styleSheet.withHBoxSpacing( 20.0 ).hbox( new DPElement[] { blackButton.getElement(), redButton.getElement(), greenButton.getElement() } ).padX( 5.0 );
		DPElement colourBox = styleSheet.vbox( new DPElement[] { colouredTextProxy, colourLinks } );
		DPElement colourSection = section( "Action buttons", colourBox );
		
		return colourSection;
	}
}
