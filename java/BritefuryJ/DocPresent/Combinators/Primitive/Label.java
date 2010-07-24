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

public class Label extends Pres
{
	private String text;
	
	
	public Label(String text)
	{
		this.text = text;
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		return new DPText( Primitive.labelTextParams.get( ctx.getStyle() ), text, "" );
	}
}
