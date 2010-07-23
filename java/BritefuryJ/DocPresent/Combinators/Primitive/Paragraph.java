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
import BritefuryJ.DocPresent.Combinators.PresentationCombinator;

public class Paragraph extends PresentationCombinator
{
	private PresentationCombinator children[];
	
	
	public Paragraph(Object children[])
	{
		this.children = mapCoerce( children );
	}
	
	public Paragraph(List<Object> children)
	{
		this.children = mapCoerce( children );
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPParagraph element = new DPParagraph( ctx.getStyle().getParagraphParams() );
		element.setChildren( mapPresent( ctx, children ) );
		return element;
	}
}
