//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.RichText;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Heading2 extends RichParagraph
{
	public Heading2(Object contents[])
	{
		super( contents );
	}
	
	public Heading2(List<Object> contents)
	{
		super( contents );
	}
	
	public Heading2(String text)
	{
		super( text );
	}

	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return presentParagraph( ctx, style.withAttrs( RichText.h2TextStyle( style ) ) );
	}
}
