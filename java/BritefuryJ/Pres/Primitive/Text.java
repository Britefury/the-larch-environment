//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Text extends Pres
{
	private String text, textRepresentation;
	
	
	public Text(String text)
	{
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}

		this.text = text;
		this.textRepresentation = text;
	}
	
	public Text(String text, String textRepresentation)
	{
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}

		if ( textRepresentation == null )
		{
			throw new RuntimeException( "Text representation cannot be null" );
		}

		this.text = text;
		this.textRepresentation = textRepresentation;
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return new DPText( Primitive.textParams.get( style ), text, textRepresentation );
	}
}
