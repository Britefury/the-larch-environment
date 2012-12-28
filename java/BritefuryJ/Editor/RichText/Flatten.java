//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.StringSplit;

class Flatten
{
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
	
	
	// Replace newline characters in strings with newline tokens
	// SHOULD IMPLEMENT AS ITERATOR, BUT I'M BUGGERED IF I AM GOING TO SPEND TIME CONVERTING A NICE PYTHON GENERATOR TO A JAVA ITERATOR......
	private static ArrayList<Object> newlineSplit(Iterable<Object> xs)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		for (Object x: xs)
		{
			if ( x instanceof String  &&  ((String)x).contains( "\n" ) )
			{
				List<String> lines = StringSplit.split( (String)x, "\n" );
				result.add( lines.get( 0 ) );
				for (int i = 1; i < lines.size(); i++)
				{
					result.add( Newline.instance );
					String line = lines.get( i );
					if ( line.length() > 0 )
					{
						result.add( lines.get( i ) );
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
	
	// Create a flattened version of the sequence of tags and strings in @xs
	// Paragraph start tags (TagPStart) remain
	// Newlines are converted to paragraph start tags (TagPStart)
	// Strings are wrapped in EdStyleSpan objects, with styles determined by the style span start and end tags (TagSStart and TagSEnd)
	// Paragraphs (editor model paragraphs) that have not been 'flattened out' but remain as structural items are left as is.
	// SHOULD IMPLEMENT AS ITERATOR, BUT I'M BUGGERED IF I AM GOING TO SPEND TIME CONVERTING A NICE PYTHON GENERATOR TO A JAVA ITERATOR......
	private static ArrayList<Object> flatten(Iterable<Object> xs)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		HashMap<Object, Object> currentStyleAttrs = new HashMap<Object, Object>();
		Stack<HashMap<Object, Object>> styleStack = new Stack<HashMap<Object, Object>>();
		styleStack.add( new HashMap<Object, Object>() );
		Object prevElement = null;
		
		for (Object x: xs)
		{
			if ( prevElement instanceof Newline )
			{
				// Previous element was a newline
				if ( x instanceof TagPStart  ||  isPara( x ) )
				{
					// Current element is a paragraph start tag, or a paragraph:
					// Emit nothing as it will be handled further down
				}
				else
				{
					// Emit a paragraph start tag
					result.add( new TagPStart( null ) );
				}
			}
			
			
			if ( x instanceof String  ||  x instanceof EdInlineEmbed )
			{
				result.add( new EdStyleSpan( Arrays.asList( new Object[] { x } ), currentStyleAttrs ) );
			}
			else if ( x instanceof TagSStart )
			{
				// Update the style stack
				HashMap<Object, Object> attrs = new HashMap<Object, Object>();
				attrs.putAll( currentStyleAttrs );
				attrs.putAll( ((TagSStart)x).getStyleAttrs() );
				currentStyleAttrs = attrs;
				styleStack.add( attrs );
			}
			else if ( x instanceof TagSEnd )
			{
				// Update the style stack
				styleStack.pop();
				currentStyleAttrs = styleStack.lastElement();
			}
			else if ( x instanceof TagPStart )
			{
				// Element is a paragraph start tag
				if ( prevElement instanceof Newline  ||  isPara( prevElement )  ||  prevElement == null )
				{
					// Previous element is a newline, a paragraph, or nothing - emit the tag as is
					result.add( x );
				}
			}
			else if ( x instanceof Newline )
			{
			}
			else if ( isPara( x ) )
			{
				result.add( x );
			}
			else
			{
				throw new RuntimeException( "Could not process element " + x.getClass().getName() );
			}
			
			prevElement = x;
		}
		
		return result;
	}
	
	
	protected static ArrayList<Object> flattenParagraphs(Iterable<Object> xs)
	{
		return flatten( textJoin( newlineSplit( xs ) ) );
	}
}
