//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class StaticText extends Pres
{
	private String text, textRepresentation;
	
	
	public StaticText(String text)
	{
		this.text = text;
		this.textRepresentation = text;
	}
	
	public StaticText(String text, String textRepresentation)
	{
		this.text = text;
		this.textRepresentation = textRepresentation;
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return new DPText( Primitive.staticTextParams.get( style ), text, textRepresentation );
	}
}
