//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.UI;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class SectionHeading2 extends TextParagraph
{
	public SectionHeading2(Object contents[])
	{
		super( contents );
	}
	
	public SectionHeading2(List<Object> contents)
	{
		super( contents );
	}
	
	public SectionHeading2(String text)
	{
		super( text );
	}

	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return presentParagraph( ctx, style.withAttrs( UI.h2TextStyle( style ) ) );
	}
}
