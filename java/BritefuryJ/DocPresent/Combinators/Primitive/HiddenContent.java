//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPHiddenContent;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class HiddenContent extends Pres
{
	private String textRepresentation;
	
	
	public HiddenContent(String textRepresentation)
	{
		this.textRepresentation = textRepresentation;
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return new DPHiddenContent( textRepresentation );
	}
}
