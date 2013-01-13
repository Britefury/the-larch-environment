//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;
import java.util.*;
import java.util.regex.Pattern;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

class Flatten
{
	private static class FlattenInput
	{
		private List<Object> xs;
		private int pos;


		private FlattenInput(List<Object> xs)
		{
			this.xs = xs;
			this.pos = 0;
		}


		private boolean matches(Class<?> cls)
		{
			return pos < xs.size()  &&  cls.isInstance( xs.get( pos ) );
		}

		private boolean matches(Class<?> ...cls)
		{
			if ( pos <= xs.size() - cls.length )
			{
				int p = pos;
				for (Class<?> c: cls)
				{
					if ( !c.isInstance( xs.get( p  ) ) )
					{
						return false;
					}
					p++;
				}
				return true;
			}
			else
			{
				return false;
			}
		}

		private List<Object> consume(int n)
		{
			List<Object> result = xs.subList( pos, pos + n );
			pos += n;
			return result;
		}

		private Object consume()
		{
			Object result = xs.get( pos );
			pos++;
			return result;
		}


		private boolean isAtStart()
		{
			return pos == 0;
		}

		private boolean hasMoreContent()
		{
			return pos < xs.size();
		}
	}




	protected static class Newline implements Presentable
	{
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return style.applyTo( new Border( new Label( "<newline>" ) ) );
		}

		private static final StyleSheet style = StyleSheet.style( Primitive.border.as( new SolidBorder( 1.0, 1.0, 5.0, 5.0, Color.BLACK, new Color( 0.85f, 0.85f, 0.95f ) ) ), Primitive.fontBold.as( true ), Primitive.fontItalic.as( true ) );
		
		
		protected static final Newline instance = new Newline();
	}
	
	
	private static Pattern newlinePattern = Pattern.compile( "\n" );


	// Replace newline characters in strings with newline tokens
	// SHOULD IMPLEMENT AS ITERATOR, BUT I'M BUGGERED IF I AM GOING TO SPEND TIME CONVERTING A NICE PYTHON GENERATOR TO A JAVA ITERATOR......
	private static ArrayList<Object> newlineSplit(Iterable<Object> xs)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		for (Object x: xs)
		{
			if ( x instanceof String  &&  ((String)x).contains( "\n" ) )
			{
				String lines[] = newlinePattern.split( (String)x, -1 );
				result.add( lines[0] );
				for (int i = 1; i < lines.length; i++)
				{
					result.add( Newline.instance );
					String line = lines[i];
					if ( line.length() > 0 )
					{
						result.add( line );
					}
				}
			}
			else
			{
				result.add( x );
			}
		}
		return result;
	}

	// Join adjacent string with one another
	// SHOULD IMPLEMENT AS ITERATOR, BUT I'M BUGGERED IF I AM GOING TO SPEND TIME CONVERTING A NICE PYTHON GENERATOR TO A JAVA ITERATOR......
	private static ArrayList<Object> textJoin(Iterable<Object> xs)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		StringBuilder text = new StringBuilder();

		for (Object x: xs)
		{
			if ( x instanceof String )
			{
				text.append( (String)x );
			}
			else
			{
				if ( text.length() > 0 )
				{
					result.add( text.toString() );
					text = new StringBuilder();
				}
				result.add( x );
			}
		}

		if ( text.length() > 0 )
		{
			result.add( text.toString() );
		}
		return result;
	}


	private static boolean isPara(Object x)
	{
		return x instanceof EdNode  &&  ((EdNode)x).isParagraph();
	}

	private static boolean isTextPara(Object x)
	{
		return x instanceof EdParagraph;
	}

	private static boolean isParaEmbed(Object x)
	{
		return x instanceof EdParagraphEmbed;
	}

	private static boolean isStyleSpan(Object x)
	{
		return x instanceof EdStyleSpan;
	}
	
	// Create a flattened version of the sequence of tags and strings in @xs
	// Paragraph start tags (TagPStart) remain
	// Newlines are converted to paragraph start tags (TagPStart)
	// Strings are wrapped in EdStyleSpan objects, with styles determined by the style span start and end tags (TagSStart and TagSEnd)
	// Paragraphs (editor model paragraphs) that have not been 'flattened out' but remain as structural items are left as is.
	// SHOULD IMPLEMENT AS ITERATOR, BUT I'M BUGGERED IF I AM GOING TO SPEND TIME CONVERTING A NICE PYTHON GENERATOR TO A JAVA ITERATOR......
	private static void flatten(ArrayList<Object> result, FlattenInput xs, HashMap<Object, Object> currentStyleAttrs)
	{
		Stack<HashMap<Object, Object>> styleStack = new Stack<HashMap<Object, Object>>();
		styleStack.add( new HashMap<Object, Object>() );
		Object prevElement = null;
		
		while ( xs.hasMoreContent() )
		{
			if ( xs.matches( Newline.class, EdParagraphEmbed.class ) )
			{
				// Newline followed by a paragraph embed
				// If the newline is the first element, then the user has attempted to insert a newline before the paragraph embed, so emit
				// a paragraph start tag
				// otherwise, it is the end of one paragraph, just before the embed, so suppress it
				if ( xs.isAtStart() )
				{
					result.add( new TagPStart( null ) );
				}
				xs.consume();
				result.add( xs.consume() );
			}
			else if ( xs.matches( Newline.class, TagPStart.class )  ||
					xs.matches( Newline.class, EdParagraph.class ) )
			{
				// End of one paragraph and the beginning of another; emit the paragraph start tag
				// Newline followed by:
				// - paragraph start tag
				// - paragraph embed
				// - paragraph
				// Discard the newline
				xs.consume();
				result.add( xs.consume() );
			}
			else if ( xs.matches( Newline.class, String.class )  ||
					xs.matches( Newline.class, EdInlineEmbed.class )  ||
					xs.matches( Newline.class, TagSStart.class )  ||
					xs.matches( Newline.class, EdStyleSpan.class )  ||
					xs.matches( Newline.class, Newline.class ) )
			{
				// Newline followed by paragraph content
				// The user has inserted a newline to split the paragraph
				// Consume ONLY the newline and emit a paragraph start tag
				xs.consume();
				result.add( new TagPStart( null ) );
			}
			else if ( xs.matches( EdParagraphEmbed.class, Newline.class ) )
			{
				// Emit the paragraph embed, and a paragraph start tag
				result.add( xs.consume() );
				xs.consume();
				result.add( new TagPStart( null ) );
			}
			else if ( xs.matches( Newline.class ) )
			{
				// Suppress
				xs.consume();
				// Should be at the end
				if ( xs.hasMoreContent() )
				{
					throw new RuntimeException( "Should have reached the end" );
				}
			}
			else if ( xs.matches( TagPStart.class, EdParagraphEmbed.class )  ||
					xs.matches( TagPStart.class, EdParagraph.class ) )
			{
				// Eliminate the paragraph start tag
				xs.consume();
				result.add( xs.consume() );
			}
			else if ( xs.matches( TagPStart.class, String.class )  ||
					xs.matches( TagPStart.class, EdInlineEmbed.class )  ||
					xs.matches( TagPStart.class, TagSStart.class )  ||
					xs.matches( TagPStart.class, EdStyleSpan.class )  ||
					xs.matches( TagPStart.class, Newline.class ) )
			{
				// Paragraph start tag followed by text content
				// Consume and emit only the paragraph start tag
				result.add( xs.consume() );
			}
			else if ( xs.matches( String.class )  ||  xs.matches( EdInlineEmbed.class ) )
			{
				// Textual content; wrap in a style span
				Object x = xs.consume();
				result.add( new EdStyleSpan( Arrays.asList( new Object[] { x } ), currentStyleAttrs ) );
			}
			else if ( xs.matches( TagSStart.class ) )
			{
				// Span start tag; put attributes onto stack
				TagSStart tag = (TagSStart)xs.consume();
				// Update the style stack
				HashMap<Object, Object> attrs = new HashMap<Object, Object>();
				attrs.putAll( currentStyleAttrs );
				attrs.putAll( tag.getStyleAttrs() );
				currentStyleAttrs = attrs;
				styleStack.add( attrs );
			}
			else if ( xs.matches( TagSEnd.class ) )
			{
				// Span end tag; pop attributes from stack
				xs.consume();
				// Update the style stack
				styleStack.pop();
				currentStyleAttrs = styleStack.lastElement();
			}
			else if ( xs.matches( TagPStart.class ) )
			{
				// Lone paragraph start tag; suppress
				xs.consume();
			}
			else if ( xs.matches( EdParagraph.class )  ||  xs.matches( EdParagraphEmbed.class ) )
			{
				// Paragraph start
				result.add( xs.consume() );
			}
			else if ( xs.matches( EdStyleSpan.class ) )
			{
				// Style span; process recursively
				EdStyleSpan span = (EdStyleSpan)xs.consume();
				HashMap<Object, Object> attrs = new HashMap<Object, Object>();
				attrs.putAll( currentStyleAttrs );
				attrs.putAll( span.getStyleAttrs() );
				flatten( result, new FlattenInput( span.getContents() ), attrs );
			}
			else
			{
				throw new RuntimeException( "Could not process element " + xs.consume().getClass().getName() );
			}
		}
	}

	private static ArrayList<Object> flatten(List<Object> xs)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		HashMap<Object, Object> currentStyleAttrs = new HashMap<Object, Object>();
		flatten( result, new FlattenInput( xs ), currentStyleAttrs );
		return result;
	}
	
	
	protected static ArrayList<Object> flattenParagraphs(List<Object> xs)
	{
		return flatten( textJoin( newlineSplit( xs ) ) );
	}
}
