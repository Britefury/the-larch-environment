//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Combinators.PresentationCombinator;

public class StaticText extends PresentationCombinator
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
	public DPElement present(PresentationContext ctx)
	{
		return new DPText( ctx.getStyle().getStaticTextParams(), text, textRepresentation );
	}
}
