//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
		LSSpan element = new LSSpan( Primitive.containerParams.get( style ) );
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

		element.setChildren( childElements );
		return element;
	}
}
