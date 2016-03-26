//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
