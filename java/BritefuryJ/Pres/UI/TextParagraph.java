//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
