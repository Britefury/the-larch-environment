//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.DPSpan;
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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPSpan element = new DPSpan( Primitive.containerParams.get( style ) );
		StyleValues childStyle =  Primitive.useContainerParams.get( style );
		
		DPElement childElements[];
		
		if ( children.length > 0 )
		{
			childElements = new DPElement[children.length+2];
			childElements[0] = new DPParagraphIndentMarker();
			for (int i = 0; i < children.length; i++)
			{
				childElements[i+1] = children[i].present( ctx, childStyle );
			}
			childElements[children.length+1] = new DPParagraphDedentMarker();
		}
		else
		{
			childElements = new DPElement[0];
		}

		element.setChildren( childElements );
		return element;
	}
}
