//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.SequentialPres;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Paragraph extends SequentialPres
{
	public Paragraph(Object children[])
	{
		super( children );
	}
	
	public Paragraph(List<Object> children)
	{
		super( children );
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPParagraph element = new DPParagraph( Primitive.paragraphParams.get( style ) );
		element.setChildren( mapPresent( ctx, Primitive.useParagraphParams( style ), children ) );
		return element;
	}
}
