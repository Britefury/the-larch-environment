//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSWhitespace;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Whitespace extends Pres
{
	private String text;
	private double width;
	
	
	public Whitespace(String text)
	{
		this.text = text;
		this.width = 0.0;
	}
	
	public Whitespace(String text, double width)
	{
		this.text = text;
		this.width = width;
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return new LSWhitespace( Primitive.contentLeafParams.get( style ), text, width );
	}
}
