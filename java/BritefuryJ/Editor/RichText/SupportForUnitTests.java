//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2013.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.*;


public class SupportForUnitTests
{
	public static Object newline()
	{
		return new Flatten.Newline();
	}


	public static Object tagPStart(Map<Object, Object> styleAttrs)
	{
		return new TagPStart( styleAttrs );
	}

	public static Object tagSStart(SpanAttributes styleAttrs)
	{
		return new TagSStart( styleAttrs );
	}

	public static Object tagSEnd()
	{
		return new TagSEnd();
	}


	public static Object span(Object contents[], SpanAttributes styleAttrs)
	{
		return new EdStyleSpan( Arrays.asList( contents ), styleAttrs );
	}

	public static Object iembed(Object value)
	{
		return new EdInlineEmbed( value );
	}

	public static Object p(Object contents[], Map<Object, Object> styleAttrs)
	{
		return new EdParagraph( null, Arrays.asList( contents ), new HashMap<Object, Object>() );
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
