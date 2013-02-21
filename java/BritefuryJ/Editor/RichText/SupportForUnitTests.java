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

	public static Object tagSStart(Map<Object, Object> styleAttrs)
	{
		return new TagSStart( styleAttrs );
	}

	public static Object tagSEnd()
	{
		return new TagSEnd();
	}


	public static Object span(Object contents[], Map<Object, Object> styleAttrs)
	{
		return new EdStyleSpan( Arrays.asList( contents ), new HashMap<Object, Object>() );
	}

	public static Object iembed(Object value)
	{
		return new EdInlineEmbed( value );
	}

	public static Object p(Object contents[], Map<Object, Object> styleAttrs)
	{
		return new EdParagraph( Arrays.asList( contents ), new HashMap<Object, Object>() );
	}

	public static Object pembed(Object value)
	{
		return new EdParagraphEmbed( value );
	}




	public static ArrayList<Object> flattenParagraphs(List<Object> xs)
	{
		return Flatten.flattenParagraphs( xs );
	}

}
