//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPWhitespace;
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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return new DPWhitespace( Primitive.contentLeafParams.get( style ), text, width );
	}
}
