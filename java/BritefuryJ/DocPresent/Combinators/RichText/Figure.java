//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Figure extends Pres
{
	private Pres content;
	private String captionText;
	
	
	public Figure(Object content, String captionText)
	{
		this.content = coerce( content );
		this.captionText = captionText;
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues contentStyleValues = RichText.useFigureAttrs( style );
		StyleSheet figureStyle = RichText.figureStyle( style );
		DPElement contentElement = content.present( ctx, contentStyleValues );
		return new Column( new Pres[] { figureStyle.applyTo( new Border( contentElement ).alignHExpand() ), new Caption( captionText ) } ).present( ctx, style );
	}
}
