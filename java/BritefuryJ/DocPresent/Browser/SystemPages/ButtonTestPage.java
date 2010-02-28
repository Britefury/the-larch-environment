//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.ControlsStyleSheet;
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
	
	
	private class ButtonColourChanger implements ControlsStyleSheet.ButtonListener
	{
		private DPProxy parentElement;
		private PrimitiveStyleSheet style;
		
		
		public ButtonColourChanger(DPProxy parentElement, PrimitiveStyleSheet style)
		{
			this.parentElement = parentElement;
			this.style = style;
		}


		public boolean onButtonClicked(DPWidget element, PointerButtonEvent event)
		{
			parentElement.setChild( colouredText( style ) );
			return true;
		}
	}

	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 18) );
	private static PrimitiveStyleSheet blackText = styleSheet.withForeground( Color.black );
	private static PrimitiveStyleSheet redText = styleSheet.withForeground( Color.red );
	private static PrimitiveStyleSheet greenText = styleSheet.withForeground( new Color( 0.0f, 0.5f, 0.0f ) );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	protected DPWidget section(String title, DPWidget contents)
	{
		DPWidget heading = headingStyleSheet.staticText( title );
		
		return styleSheet.vbox( Arrays.asList( new DPWidget[] { heading.padY( 10.0 ), contents } ) );
	}
	
	protected DPWidget colouredText(PrimitiveStyleSheet style)
	{
		return style.staticText( "Change the colour of this text using the buttons below." );
	}
	
	protected DPWidget createContents()
	{
		DPProxy colouredTextProxy = styleSheet.proxy( colouredText( blackText ) );
		DPWidget blackLink = controlsStyleSheet.button( styleSheet.staticText( "Black" ), new ButtonColourChanger( colouredTextProxy, blackText ) );
		DPWidget redLink = controlsStyleSheet.button( styleSheet.staticText( "Red" ), new ButtonColourChanger( colouredTextProxy, redText ) );
		DPWidget greenLink = controlsStyleSheet.button( styleSheet.staticText( "Green" ), new ButtonColourChanger( colouredTextProxy, greenText ) );
		DPWidget colourLinks = styleSheet.withHBoxSpacing( 20.0 ).hbox( Arrays.asList( new DPWidget[] { blackLink, redLink, greenLink } ) ).padX( 5.0 );
		DPWidget colourBox = styleSheet.vbox( Arrays.asList( new DPWidget[] { colouredTextProxy, colourLinks } ) );
		DPWidget colourSection = section( "Action buttons", colourBox );
		
		return colourSection;
	}
}
