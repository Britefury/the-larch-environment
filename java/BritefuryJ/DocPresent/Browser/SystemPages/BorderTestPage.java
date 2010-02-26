//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.EmptyBorder;
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


	protected DPWidget createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		
		DPWidget onePixelBorder = styleSheet.border( styleSheet.text( "Normal 1-pixel border" ) );
		
		DPWidget padded = styleSheet.border( styleSheet.text( "Padding: 30 pixels of padding all round, via the pad() method" ).pad( 30.0, 30.0 ) );
		
		DPWidget emptyBorder = styleSheet.withBorder( new EmptyBorder( 50.0, 50.0, 20.0, 20.0, 20.0, 20.0, new Color( 0.8f, 0.8f, 0.8f ) ) ).border(
				styleSheet.text( "Empty border: 50 pixel h-margins, 20 pixel v-margins, 20 pixel rounding, light-grey background"  ) );
		
		DPWidget solidBorder = styleSheet.withBorder( new SolidBorder( 3.0f, 10.0, 20.0, 20.0, new Color( 0.6f, 0.6f, 0.6f ), new Color( 0.8f, 0.8f, 0.8f ) ) ).border(
				styleSheet.text( "Solid border: 3 pixel thickness, 10 pixel inset (margin), 20 pixel rounding, grey border, light-grey background" ) );
		
		
		return styleSheet.withVBoxSpacing( 10.0 ).vbox( Arrays.asList( new DPWidget[] { onePixelBorder, padded, emptyBorder, solidBorder } ) );
	}
}