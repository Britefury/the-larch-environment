//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class BorderTestPage extends SystemPage
{
	protected BorderTestPage()
	{
		register( "tests.border" );
	}
	
	
	public String getTitle()
	{
		return "Border test";
	}
	
	protected String getDescription()
	{
		return "The border element is used to provide additional space around elements. Different border styles are available.";
	}


	protected DPElement createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		
		DPElement onePixelBorder = styleSheet.border( styleSheet.staticText( "Normal 1-pixel border" ) );
		
		DPElement padded = styleSheet.border( styleSheet.staticText( "Padding: 30 pixels of padding all round, via the pad() method" ).pad( 30.0, 30.0 ) );
		
		DPElement emptyBorder = styleSheet.withBorder( new FilledBorder( 50.0, 50.0, 20.0, 20.0, 20.0, 20.0, new Color( 0.8f, 0.8f, 0.8f ) ) ).border(
				styleSheet.staticText( "Empty border: 50 pixel h-margins, 20 pixel v-margins, 20 pixel rounding, light-grey background"  ) );
		
		DPElement solidBorder = styleSheet.withBorder( new SolidBorder( 3.0f, 10.0, 20.0, 20.0, new Color( 0.6f, 0.6f, 0.6f ), new Color( 0.8f, 0.8f, 0.8f ) ) ).border(
				styleSheet.staticText( "Solid border: 3 pixel thickness, 10 pixel inset (margin), 20 pixel rounding, grey border, light-grey background" ) );
		
		
		return styleSheet.withVBoxSpacing( 10.0 ).vbox( new DPElement[] { onePixelBorder, padded, emptyBorder, solidBorder } );
	}
}