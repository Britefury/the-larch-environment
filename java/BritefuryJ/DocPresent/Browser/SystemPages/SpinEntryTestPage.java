//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.RealSpinEntry;
import BritefuryJ.DocPresent.Controls.IntSpinEntry;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class SpinEntryTestPage extends SystemPage
{
	protected SpinEntryTestPage()
	{
		register( "tests.controls.spinentry" );
	}
	
	
	public String getTitle()
	{
		return "Spin entry test";
	}
	
	protected String getDescription()
	{
		return "Spin entry control: edit a numeric value";
	}
	
	
	private class RealSpinEntryTextChanger implements RealSpinEntry.RealSpinEntryListener
	{
		private DPText textElement;
		
		
		public RealSpinEntryTextChanger(DPText textElement)
		{
			this.textElement = textElement;
		}


		public void onSpinEntryValueChanged(RealSpinEntry spinEntry, double value)
		{
			textElement.setText( String.valueOf( value ) );
		}
	}

	

	private class IntSpinEntryTextChanger implements IntSpinEntry.IntSpinEntryListener
	{
		private DPText textElement;
		
		
		public IntSpinEntryTextChanger(DPText textElement)
		{
			this.textElement = textElement;
		}


		public void onSpinEntryValueChanged(IntSpinEntry spinEntry, long value)
		{
			textElement.setText( String.valueOf( value ) );
		}
	}

	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFontSize( 18 );

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
		DPText realValueText = styleSheet.staticText( "0.0" );
		DPText intValueText = styleSheet.staticText( "0" );
		RealSpinEntryTextChanger realListener = new RealSpinEntryTextChanger( realValueText );
		IntSpinEntryTextChanger intListener = new IntSpinEntryTextChanger( intValueText );
		RealSpinEntry realSpinEntry = controlsStyleSheet.realSpinEntry( 0.0, -100.0, 100.0, 1.0, 10.0, realListener );
		IntSpinEntry intSpinEntry = controlsStyleSheet.intSpinEntry( 0, -100, 100, 1, 10, intListener );
		DPElement realLine = styleSheet.withHBoxSpacing( 20.0 ).hbox( new DPElement[] { styleSheet.staticText( "Real: " ),
				styleSheet.spaceBin( realSpinEntry.getElement().alignHExpand(), 100.0, -1.0 ), realValueText } ).padX( 5.0 );
		DPElement intLine = styleSheet.withHBoxSpacing( 20.0 ).hbox( new DPElement[] { styleSheet.staticText( "Integer: " ),
				styleSheet.spaceBin( intSpinEntry.getElement().alignHExpand(), 100.0, -1.0 ), intValueText } ).padX( 5.0 );
		DPElement spinEntrySectionContents = styleSheet.vbox( new DPElement[] { realLine, intLine } );
		DPElement spinEntrySection = section( "Spin entry", spinEntrySectionContents );
		
		return spinEntrySection;
	}
}
