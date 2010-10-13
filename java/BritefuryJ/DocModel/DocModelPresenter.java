//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Span;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Sequence.ParagraphSequenceView;
import BritefuryJ.DocPresent.Combinators.Sequence.TrailingSeparator;
import BritefuryJ.DocPresent.Combinators.Sequence.VerticalInlineSequenceView;
import BritefuryJ.DocPresent.Combinators.Sequence.VerticalSequenceView;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.PresCom.GenericStyle;
import BritefuryJ.GSym.GenericPerspective.PresCom.UnescapedStringAsRow;
import BritefuryJ.GSym.PresCom.InnerFragment;
import BritefuryJ.GSym.View.GSymFragmentView;

public class DocModelPresenter
{
	private static enum ObjectPresentMode
	{
		HORIZONTAL,
		VERTICALINLINE
	}


	private static final StyleSheet defaultStyle = StyleSheet.instance.withAttr( Primitive.fontFace, "Sans serif" ).withAttr( Primitive.fontSize, 14 )
		.withAttr( Primitive.foreground, Color.black ).withAttr( Primitive.paragraphIndentation, 60.0 );

	private static final StyleSheet nullStyle = defaultStyle.withAttr( Primitive.fontItalic, true ).withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.25f ) );

	private static final StyleSheet stringStyle = defaultStyle.withAttr( GenericStyle.stringContentStyle, StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.5f ) ) );
	
	private static final StyleSheet punctuationStyle = defaultStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 1.0f ) );

	private static final StyleSheet classNameStyle = defaultStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static final StyleSheet schemaNameStyle = defaultStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) );

	private static final StyleSheet fieldNameStyle = defaultStyle.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.25f ) );

	
	
	private static final Pres space = new Label( " " );
	private static final Pres openBracket = punctuationStyle.applyTo( new Label( "[" ) );
	private static final Pres closeBracket = punctuationStyle.applyTo( new Label( "]" ) );
	private static final Pres openParen = punctuationStyle.applyTo( new Label( "(" ) );
	private static final Pres closeParen = punctuationStyle.applyTo( new Label( ")" ) );


	
	private static Pres present(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( x == null )
		{
			return nullStyle.applyTo( new Label( "<null>" ) );
		}
		else if ( x instanceof String )
		{
			return stringStyle.applyTo( new UnescapedStringAsRow( (String )x ) );
		}
		else
		{
			return new InnerFragment( x ); 
		}
	}
	
	protected static Pres presentDMList(DMList node, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		List<Object> xViews = new ArrayList<Object>();
		for (Object x: node)
		{
			xViews.add( present( x, fragment, inheritedState ) );
		}
		
		// Check the contents, to determine the layout
		// Default: horizontal (paragraph layout)
		if ( node.size() > 0 )
		{
			for (Object x: node )
			{
				if ( !(x instanceof String) )
				{
					return new VerticalSequenceView( xViews, openBracket, closeBracket, null, space, TrailingSeparator.NEVER );
				}
			}
		}
		
		// Create a list view
		return new ParagraphSequenceView( xViews, openBracket, closeBracket, null, space, TrailingSeparator.NEVER );
	}
	
	
	protected static Pres presentDMObject(DMObject node, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		DMObjectClass cls = node.getDMObjectClass();
		
		// Check the contents, to determine the layout
		// Default: horizontal (paragraph layout)
		ObjectPresentMode mode = ObjectPresentMode.HORIZONTAL;
		for (int i = 0; i < cls.getNumFields(); i++)
		{
			Object value = node.get( i );
			if ( value != null )
			{
				// If we encounter a non-string value, then this object cannot be displayed in a single line
				if ( !( value instanceof String ) )
				{
					mode = ObjectPresentMode.VERTICALINLINE;
					break;
				}
			}
		}
		
		// Header
		Pres header;
		Pres schemaName = schemaNameStyle.applyTo( new Label( cls.getSchema().getShortName() ) );
		Pres className = classNameStyle.applyTo( new Label( cls.getName() ) );
		if ( mode == ObjectPresentMode.HORIZONTAL )
		{
			header = defaultStyle.applyTo( new Span( new Object[] { schemaName, punctuationStyle.applyTo( new Label( "." ) ), className,
					new Label( " " ), punctuationStyle.applyTo( new Label( ":" ) ) } ) );
		}
		else if ( mode == ObjectPresentMode.VERTICALINLINE )
		{
			header = defaultStyle.applyTo( new Paragraph( new Object[] { schemaName, punctuationStyle.applyTo( new Label( "." ) ), className,
					new Label( " " ), punctuationStyle.applyTo( new Label( ":" ) ) } ) );
		}
		else
		{
			throw new RuntimeException( "Invalid mode" );
		}
		
		ArrayList<Object> itemViews = new ArrayList<Object>();
		itemViews.add( header );
		// Create views of each item
		for (int i = 0; i < cls.getNumFields(); i++)
		{
			Object value = node.get( i );
			String fieldName = cls.getField( i ).getName();
			if ( value != null )
			{
				Pres line;
				if ( mode == ObjectPresentMode.HORIZONTAL )
				{
					line = defaultStyle.applyTo( new Span( new Object[] { fieldNameStyle.applyTo( new StaticText( fieldName ) ), punctuationStyle.applyTo( new StaticText( "=" ) ),
							 present( value, fragment, inheritedState ) } ) );
				}
				else if ( mode == ObjectPresentMode.VERTICALINLINE )
				{
					line = defaultStyle.applyTo( new Paragraph( new Object[] { fieldNameStyle.applyTo( new StaticText( fieldName ) ), punctuationStyle.applyTo( new StaticText( "=" ) ),
							 present( value, fragment, inheritedState ) } ) );
				}
				else
				{
					throw new RuntimeException( "Invalid mode" );
				}
				itemViews.add( line );
			}
		}
				
		// Create the layout
		if ( mode == ObjectPresentMode.HORIZONTAL )
		{
			return new ParagraphSequenceView( itemViews, openParen, closeParen, null, space, TrailingSeparator.NEVER );
		}
		else if ( mode == ObjectPresentMode.VERTICALINLINE )
		{
			return new VerticalInlineSequenceView( itemViews, openParen, closeParen, null, space, TrailingSeparator.NEVER );
		}
		else
		{
			throw new RuntimeException( "Invalid mode" );
		}
	}

}
