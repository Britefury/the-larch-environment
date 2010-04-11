//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.ListView.ListViewStyleSheet;
import BritefuryJ.DocPresent.ListView.ParagraphListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.ListView.SeparatorElementFactory;
import BritefuryJ.DocPresent.ListView.TrailingSeparator;
import BritefuryJ.DocPresent.ListView.VerticalInlineListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.ListView.VerticalListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.View.GSymFragmentViewContext;

public class DocModelPresenter
{
	private static enum ObjectPresentMode
	{
		HORIZONTAL,
		VERTICALINLINE
	}


	private static final PrimitiveStyleSheet defaultStyle = PrimitiveStyleSheet.instance.withFont( new Font( "SansSerif", Font.PLAIN, 14 ) ).withForeground( Color.black ).withParagraphIndentation( 60.0 );

	private static final PrimitiveStyleSheet nullStyle = defaultStyle.withFont( new Font( "SansSerif", Font.ITALIC, 14 ) ).withForeground( new Color( 0.5f, 0.0f, 0.25f ) );

	private static final PrimitiveStyleSheet stringStyle = defaultStyle.withFont( new Font( "SansSerif", Font.PLAIN, 14 ) ).withForeground( new Color( 0.0f, 0.5f, 0.5f ) );

	private static final PrimitiveStyleSheet punctuationStyle = defaultStyle.withForeground( new Color( 0.0f, 0.0f, 1.0f ) );

	private static final PrimitiveStyleSheet classNameStyle = defaultStyle.withFont( new Font( "SansSerif", Font.PLAIN, 14 ) ).withForeground( new Color( 0.0f, 0.5f, 0.0f ) );

	private static final PrimitiveStyleSheet fieldNameStyle = defaultStyle.withFont( new Font( "SansSerif", Font.PLAIN, 14 ) ).withForeground( new Color( 0.5f, 0.0f, 0.5f ) );

	
	
	private static final SeparatorElementFactory spaceFactory = new SeparatorElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet, int index, DPElement child)
		{
			return ((PrimitiveStyleSheet)styleSheet).text( " " );
		}
	};
	
	private static final ElementFactory openBracketFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return punctuationStyle.text( "[" );
		}
	};

	private static final ElementFactory closeBracketFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return punctuationStyle.text( "]" );
		}
	};

	private static final ElementFactory openParenFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return punctuationStyle.text( "(" );
		}
	};

	private static final ElementFactory closeParenFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return punctuationStyle.text( ")" );
		}
	};



	private static final ParagraphListViewLayoutStyleSheet paragraph_listViewLayout = ParagraphListViewLayoutStyleSheet.instance.withAddParagraphIndentMarkers( true );
	private static final VerticalInlineListViewLayoutStyleSheet verticalInline_listViewLayout = VerticalInlineListViewLayoutStyleSheet.instance.withIndentation( 30.0 );
	private static final VerticalListViewLayoutStyleSheet vertical_listViewLayout = VerticalListViewLayoutStyleSheet.instance.withIndentation( 30.0 );

	private static final ListViewStyleSheet _listviewStyle = ListViewStyleSheet.instance.withSeparatorFactory( spaceFactory ).withBeginDelimFactory( openBracketFactory ).withEndDelimFactory( closeBracketFactory );

	private static final ListViewStyleSheet _objectviewStyle = ListViewStyleSheet.instance.withSeparatorFactory( spaceFactory ).withBeginDelimFactory( openParenFactory ).withEndDelimFactory( closeParenFactory );;

	private static final ListViewStyleSheet paragraph_listViewStyle = _listviewStyle.withListLayout( paragraph_listViewLayout );
	private static final ListViewStyleSheet vertical_listViewStyle = _listviewStyle.withListLayout( vertical_listViewLayout );

	private static final ListViewStyleSheet paragraph_objectViewStyle = _objectviewStyle.withListLayout( paragraph_listViewLayout );
	private static final ListViewStyleSheet verticalInline_objectViewStyle = _objectviewStyle.withListLayout( verticalInline_listViewLayout );


	
	private static DPElement present(Object x, GSymFragmentViewContext ctx, PrimitiveStyleSheet styleSheet, AttributeTable state)
	{
		if ( x == null )
		{
			return nullStyle.staticText( "<null>" );
		}
		else if ( x instanceof String )
		{
			return stringStyle.staticText( (String)x );
		}
		else
		{
			return ctx.presentFragment( x, styleSheet, state );
		}
	}
	
	protected static DPElement presentDMList(DMList node, GSymFragmentViewContext ctx, PrimitiveStyleSheet styleSheet, AttributeTable state)
	{
		List<DPElement> xViews = new ArrayList<DPElement>();
		for (Object x: node)
		{
			xViews.add( present( x, ctx, styleSheet, state ) );
		}
		
		// Check the contents, to determine the layout
		// Default: horizontal (paragraph layout)
		ListViewStyleSheet listViewStyleSheet = paragraph_listViewStyle;
		if ( node.size() > 0 )
		{
			for (Object x: node )
			{
				if ( !(x instanceof String) )
				{
					listViewStyleSheet = vertical_listViewStyle;
					break;
				}
			}
		}
		
		// Create a list view
		return listViewStyleSheet.createListElement( xViews, TrailingSeparator.NEVER );
	}
	
	
	protected static DPElement presentDMObject(DMObject node, GSymFragmentViewContext ctx, PrimitiveStyleSheet styleSheet, AttributeTable state)
	{
		DMObjectClass cls = node.getDMObjectClass();
		
		// Check the contents, to determine the layout
		// Default: horizontal (paragraph layout)
		ListViewStyleSheet listViewStyleSheet = paragraph_listViewStyle;
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
		DPElement className;
		if ( mode == ObjectPresentMode.HORIZONTAL )
		{
			className = defaultStyle.span( Arrays.asList( new DPElement[] { classNameStyle.text( cls.getName() ), stringStyle.text( " " ), punctuationStyle.text( ":" ) } ) );
		}
		else if ( mode == ObjectPresentMode.VERTICALINLINE )
		{
			className = defaultStyle.paragraph( Arrays.asList( new DPElement[] { classNameStyle.text( cls.getName() ), stringStyle.text( " " ), punctuationStyle.text( ":" ) } ) );
		}
		else
		{
			throw new RuntimeException( "Invalid mode" );
		}
		
		ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
		itemViews.add( className );
		// Create views of each item
		for (int i = 0; i < cls.getNumFields(); i++)
		{
			Object value = node.get( i );
			String fieldName = cls.getField( i ).getName();
			if ( value != null )
			{
				DPElement line;
				if ( mode == ObjectPresentMode.HORIZONTAL )
				{
					line = defaultStyle.span( Arrays.asList( new DPElement[] { fieldNameStyle.text( fieldName ), punctuationStyle.text( "=" ), present( value, ctx, styleSheet, state ) } ) );
				}
				else if ( mode == ObjectPresentMode.VERTICALINLINE )
				{
					line = defaultStyle.paragraph( Arrays.asList( new DPElement[] { fieldNameStyle.text( fieldName ), punctuationStyle.text( "=" ), present( value, ctx, styleSheet, state ) } ) );
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
			listViewStyleSheet = paragraph_objectViewStyle;
		}
		else if ( mode == ObjectPresentMode.VERTICALINLINE )
		{
			listViewStyleSheet = verticalInline_objectViewStyle;
		}
		else
		{
			throw new RuntimeException( "Invalid mode" );
		}
		
		// Create a list view
		return listViewStyleSheet.createListElement( itemViews, TrailingSeparator.NEVER );
	}

}
