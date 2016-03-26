//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Graphics.FilledBorder;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.RichText.Body;

public class BorderTestPage extends TestPage
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

		Pres emptyBorder = new FilledBorder( 50.0, 50.0, 20.0, 20.0, 20.0, 20.0, new Color( 0.8f, 0.8f, 0.8f ) ).surround(
			    new Label( "Empty border: 50 pixel h-margins, 20 pixel v-margins, 20 pixel rounding, light-grey background" ) );
		
		Pres solidBorder = new SolidBorder( 3.0f, 10.0, 20.0, 20.0, new Color( 0.6f, 0.6f, 0.6f ), new Color( 0.8f, 0.8f, 0.8f ) ).surround(
			    new Label( "Solid border: 3 pixel thickness, 10 pixel inset (margin), 20 pixel rounding, grey border, light-grey background" ) );
		
		
		return new Body( new Pres[] { onePixelBorder, padded, emptyBorder, solidBorder } ); 
	}
}