//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSLabel;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Label extends Pres
{
	private String text;
	
	
	public Label(String text)
	{
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}

		this.text = text;
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return new LSLabel( Primitive.labelTextParams.get( style ), text );
	}
}
