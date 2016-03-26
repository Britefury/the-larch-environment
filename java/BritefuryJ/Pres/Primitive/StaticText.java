//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSText;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class StaticText extends Pres
{
	private String text, textRepresentation;
	
	
	public StaticText(String text)
	{
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}

		this.text = text;
		this.textRepresentation = text;
	}
	
	public StaticText(String text, String textRepresentation)
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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return new LSText( Primitive.staticTextParams.get( style ), text, textRepresentation );
	}
}
