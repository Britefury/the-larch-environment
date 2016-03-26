//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.UI;

import java.util.List;

import BritefuryJ.Pres.RichText.RichParagraph;


abstract public class TextParagraph extends RichParagraph
{
	public TextParagraph(Object contents[])
	{
		super( contents );
	}
	
	public TextParagraph(List<Object> contents)
	{
		super( contents );
	}
	
	public TextParagraph(String text)
	{
		super( text );
	}
}
