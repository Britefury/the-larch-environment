//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSParagraph;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.StyleSheet.StyleValues;

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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSParagraph element = new LSParagraph( Primitive.paragraphParams.get( style ) );
		element.setChildren( mapPresent( ctx, Primitive.useParagraphParams( style ).withAttr( Primitive.hAlign, HAlignment.PACK ), children ) );
		return element;
	}
}
