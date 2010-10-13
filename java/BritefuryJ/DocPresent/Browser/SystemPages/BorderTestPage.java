//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class BorderTestPage extends SystemPage
{
	protected BorderTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Border test";
	}
	
	protected String getDescription()
	{
		return "The border element is used to provide additional space around elements. Different border styles are available.";
	}


	protected Pres createContents()
	{
		Pres onePixelBorder = new Border( new Label( "Normal 1-pixel border" ) ); 
		
		Pres padded = new Border( new Label( "Padding: 30 pixels of padding all round, via the pad() method" ).pad( 30.0, 30.0 ) ); 

		Pres emptyBorder = StyleSheet.instance.withAttr( Primitive.border, new FilledBorder( 50.0, 50.0, 20.0, 20.0, 20.0, 20.0, new Color( 0.8f, 0.8f, 0.8f ) ) ).applyTo(
				new Border( new Label( "Empty border: 50 pixel h-margins, 20 pixel v-margins, 20 pixel rounding, light-grey background"  ) ) );
		
		Pres solidBorder = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 3.0f, 10.0, 20.0, 20.0, new Color( 0.6f, 0.6f, 0.6f ), new Color( 0.8f, 0.8f, 0.8f ) ) ).applyTo(
				new Border( new Label( "Solid border: 3 pixel thickness, 10 pixel inset (margin), 20 pixel rounding, grey border, light-grey background" ) ) );
		
		
		return new Body( new Pres[] { onePixelBorder, padded, emptyBorder, solidBorder } ); 
	}
}