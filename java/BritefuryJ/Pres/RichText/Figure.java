//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.RichText;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Figure extends Pres
{
	private Pres content;
	private String captionText;
	
	
	public Figure(Object content, String captionText)
	{
		this.content = coercePresentingNull(content);
		this.captionText = captionText;
	}


	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues contentStyleValues = RichText.useFigureAttrs( style );
		StyleSheet figureStyle = RichText.figureStyle( style );
		LSElement contentElement = content.present( ctx, contentStyleValues );
		return new Column( new Pres[] { figureStyle.applyTo( new Border( contentElement ) ), new Caption( captionText ).alignHPack() } ).present( ctx, style );
	}
}
