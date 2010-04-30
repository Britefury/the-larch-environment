//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.ListView.HorizontalListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.ListView.ListViewStyleSheet;
import BritefuryJ.DocPresent.ListView.ParagraphListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.ListView.SeparatorElementFactory;
import BritefuryJ.DocPresent.ListView.TrailingSeparator;
import BritefuryJ.DocPresent.ListView.VerticalInlineListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.ListView.VerticalListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class ListViewTestPage extends SystemPage
{
	protected ListViewTestPage()
	{
		register( "tests.listview" );
	}
	
	
	public String getTitle()
	{
		return "List view test";
	}
	
	protected String getDescription()
	{
		return "The list view style sheet separates its children with optional separators, and bounds the list with optional delimiters. There are a variety of layout styles."; 
	}

	
	protected DPElement makeText(String text, PrimitiveStyleSheet basicStyle)
	{
		if ( text != null )
		{
			return basicStyle.staticText( text );
		}
		else
		{
			return null;
		}
	}
	
	static class TextElementFactory implements ElementFactory
	{
		String text;

		public TextElementFactory(String text)
		{
			this.text = text;
		}
		
		
		public DPElement createElement(StyleSheet basicStyle)
		{
			return ((PrimitiveStyleSheet)basicStyle).staticText( text );
		}
	}
	
	static class TextSeparatorElementFactory implements SeparatorElementFactory
	{
		String text;

		public TextSeparatorElementFactory(String text)
		{
			this.text = text;
		}
		
		
		public DPElement createElement(StyleSheet basicStyle, int index, DPElement child)
		{
			return ((PrimitiveStyleSheet)basicStyle).staticText( text );
		}
	}
	
	static class SpacingElementFactory implements ElementFactory
	{
		double spacing;
		
		public SpacingElementFactory(double spacing)
		{
			this.spacing = spacing;
		}
		
		
		public DPElement createElement(StyleSheet basicStyle)
		{
			return ((PrimitiveStyleSheet)basicStyle).whitespace( " ", spacing );
		}
	}
	
	
	protected DPElement makeListView(PrimitiveStyleSheet basicStyle, ListViewStyleSheet listView, String[] txt, String title, String beginDelim, String endDelim, String separator)
	{
		PrimitiveStyleSheet titleStyle = basicStyle.withFontFace( "Serif" ).withFontBold( true ).withFontSize( 16 ).withForeground( Color.blue );
		PrimitiveStyleSheet elemStyle = basicStyle.withFontSize( 12 ).withForeground( Color.black );
		PrimitiveStyleSheet puncStyle = basicStyle.withFontSize( 12 ).withForeground( new Color( 0.0f, 0.5f, 0.0f ) );

		DPElement children[] = new DPText[txt.length];
		for (int i = 0; i < txt.length; i++)
		{
			children[i] = elemStyle.staticText( txt[i] ); 
		}
		
		listView = listView.withPrimitiveStyle( puncStyle ).withBeginDelimFactory( new TextElementFactory( beginDelim ) ).withEndDelimFactory( new TextElementFactory( endDelim ) ).withSeparatorFactory( new TextSeparatorElementFactory( separator ) );
		DPElement ls = listView.createListElement( Arrays.asList( children ), TrailingSeparator.NEVER );
		
		
		DPElement titleElem = titleStyle.staticText( title );
		
		return basicStyle.vbox( new DPElement[] { titleElem, ls } );
	}

	
	protected DPElement createContents()
	{
		String[] lessTexts = new String[] { "abcdef", "hello", "world" };
		String[] texts = new String[] { "abcdef", "123456", "hello", "world", "this", "is", "a", "test", "of", "the", "list", "layout", "system" };
		
		PrimitiveStyleSheet basicStyle = PrimitiveStyleSheet.instance;
		
		ParagraphListViewLayoutStyleSheet paraLayout = ParagraphListViewLayoutStyleSheet.instance.withAddParagraphIndentMarkers( true ); 
		HorizontalListViewLayoutStyleSheet hLayout = HorizontalListViewLayoutStyleSheet.instance; 
		VerticalListViewLayoutStyleSheet vLayout = VerticalListViewLayoutStyleSheet.instance.withIndentation( 30.0 ); 
		VerticalInlineListViewLayoutStyleSheet vInlineLayout = VerticalInlineListViewLayoutStyleSheet.instance.withIndentation( 30.0 ); 
		
		ListViewStyleSheet listView = ListViewStyleSheet.instance.withSpacingFactory( new SpacingElementFactory( 5.0 ) );
		
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		children.add( makeListView( basicStyle, listView.withListLayout( paraLayout ), texts, "PARAGRAPH", "[", "]", "," ) );
		children.add( makeListView( basicStyle, listView.withListLayout( hLayout ), lessTexts, "HORIZONTAL", "[", "]", "," ) );
		children.add( makeListView( basicStyle, listView.withListLayout( vLayout ), texts, "VERTICAL", "[", "]", "," ) );
		children.add( makeListView( basicStyle, listView.withListLayout( vInlineLayout ), texts, "VERTICAL-INLINE", "[", "]", "," ) );
		
		return basicStyle.withVBoxSpacing( 20.0 ).vbox( children.toArray( new DPElement[0] ) );
	}
}
