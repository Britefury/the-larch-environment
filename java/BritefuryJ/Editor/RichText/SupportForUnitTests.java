//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2013.
//##************************
package BritefuryJ.Editor.RichText;

import BritefuryJ.Editor.RichText.Attrs.RichTextAttributes;

import java.util.*;


public class SupportForUnitTests
{
	public static Object newline()
	{
		return new Flatten.Newline();
	}


	public static Object tagPStart(RichTextAttributes styleAttrs)
	{
		return new TagPStart( styleAttrs );
	}

	public static Object tagSStart(RichTextAttributes styleAttrs)
	{
		return new TagSStart( styleAttrs );
	}

	public static Object tagSEnd()
	{
		return new TagSEnd();
	}


	public static Object span(Object contents[], RichTextAttributes spanAttrs)
	{
		return new EdSpan( Arrays.asList( contents ), spanAttrs );
	}

	public static Object iembed(Object value)
	{
		return new EdInlineEmbed( value );
	}

	public static Object p(Object contents[], RichTextAttributes paraAttrs)
	{
		return new EdParagraph( null, Arrays.asList( contents ), paraAttrs );
	}

	public static Object pembed(Object value)
	{
		return new EdParagraphEmbed( null, value );
	}




	public static ArrayList<Object> flattenParagraphs(List<Object> xs)
	{
		return Flatten.flattenParagraphs( xs );
	}

}
