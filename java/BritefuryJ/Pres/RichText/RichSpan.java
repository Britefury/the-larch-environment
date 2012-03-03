//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.RichText;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleValues;

public class RichSpan extends AbstractRichText
{
	public RichSpan(Object contents[])
	{
		super( contents );
	}
	
	public RichSpan(List<Object> contents)
	{
		super( contents );
	}
	
	public RichSpan(String contents)
	{
		super( contents );
	}
	
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		List<Object> paragraphContents = null;
		
		if ( isEmpty() )
		{
			paragraphContents = Arrays.asList( new Object[] { new Text( "" ).alignHPack().alignVRefY() } );
		}
		else
		{
			paragraphContents = splitContents();
		}
		
		return new Span( paragraphContents ).present( ctx, style );
	}
}
