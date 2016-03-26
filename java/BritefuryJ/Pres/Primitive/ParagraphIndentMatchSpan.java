//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSParagraphDedentMarker;
import BritefuryJ.LSpace.LSParagraphIndentMarker;
import BritefuryJ.LSpace.LSSpan;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.StyleSheet.StyleValues;

public class ParagraphIndentMatchSpan extends SequentialPres
{
	public ParagraphIndentMatchSpan(Object children[])
	{
		super( children );
	}
	
	public ParagraphIndentMatchSpan(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues childStyle =  Primitive.useContainerParams.get( style );
		
		LSElement childElements[];
		
		if ( children.length > 0 )
		{
			childElements = new LSElement[children.length+2];
			childElements[0] = new LSParagraphIndentMarker();
			for (int i = 0; i < children.length; i++)
			{
				childElements[i+1] = children[i].present( ctx, childStyle );
			}
			childElements[children.length+1] = new LSParagraphDedentMarker();
		}
		else
		{
			childElements = new LSElement[0];
		}

		return new LSSpan( Primitive.containerParams.get( style ), childElements );
	}
}
