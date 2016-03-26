//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
		LSElement[] childElems = mapPresent( ctx, Primitive.useParagraphParams( style ).withAttr( Primitive.hAlign, HAlignment.PACK ), children );
		return new LSParagraph( Primitive.paragraphParams.get( style ), childElems );
	}
}
