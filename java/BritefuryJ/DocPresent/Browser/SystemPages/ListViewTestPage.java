//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWidget;
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

	
	protected DPWidget makeText(String text, PrimitiveStyleSheet basicStyle)
	{
		if ( text != null )
		{
			return basicStyle.text( text );
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
		
		
		public DPWidget createElement(StyleSheet basicStyle)
		{
			return ((PrimitiveStyleSheet)basicStyle).text( text );
		}
	}
	
	static class TextSeparatorElementFactory implements SeparatorElementFactory
	{
		String text;

		public TextSeparatorElementFactory(String text)
		{
			this.text = text;
		}
		
		
		public DPWidget createElement(StyleSheet basicStyle, int index, DPWidget child)
		{
			return ((PrimitiveStyleSheet)basicStyle).text( text );
		}
	}
	
	static class SpacingElementFactory implements ElementFactory
	{
		double spacing;
		
		public SpacingElementFactory(double spacing)
		{
			this.spacing = spacing;
		}
		
		
		public DPWidget createElement(StyleSheet basicStyle)
		{
			return ((PrimitiveStyleSheet)basicStyle).whitespace( " ", spacing );
		}
	}
	
	
	protected DPWidget makeListView(PrimitiveStyleSheet basicStyle, ListViewStyleSheet listView, String[] txt, String title, String beginDelim, String endDelim, String separator)
	{
		PrimitiveStyleSheet titleStyle = basicStyle.withFont( new Font( "Serif", Font.BOLD, 16 ) ).withForeground( Color.blue );
		PrimitiveStyleSheet elemStyle = basicStyle.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) ).withForeground( Color.black );
		PrimitiveStyleSheet puncStyle = basicStyle.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) ).withForeground( new Color( 0.0f, 0.5f, 0.0f ) );

		DPWidget children[] = new DPText[txt.length];
		for (int i = 0; i < txt.length; i++)
		{
			children[i] = elemStyle.text( txt[i] ); 
		}
		
		listView = listView.withPrimitiveStyle( puncStyle ).withBeginDelimFactory( new TextElementFactory( beginDelim ) ).withEndDelimFactory( new TextElementFactory( endDelim ) ).withSeparatorFactory( new TextSeparatorElementFactory( separator ) );
		DPWidget ls = listView.createListElement( Arrays.asList( children ), TrailingSeparator.NEVER );
		
		
		DPWidget titleElem = titleStyle.text( title );
		
		return basicStyle.vbox( Arrays.asList( new DPWidget[] { titleElem, ls } ) );
	}

	
	protected DPWidget createContents()
	{
		String[] lessTexts = new String[] { "abcdef", "hello", "world" };
		String[] texts = new String[] { "abcdef", "123456", "hello", "world", "this", "is", "a", "test", "of", "the", "list", "layout", "system" };
		
		PrimitiveStyleSheet basicStyle = PrimitiveStyleSheet.instance;
		
		ParagraphListViewLayoutStyleSheet paraLayout = ParagraphListViewLayoutStyleSheet.instance.withAddParagraphIndentMarkers( true ); 
		HorizontalListViewLayoutStyleSheet hLayout = HorizontalListViewLayoutStyleSheet.instance; 
		VerticalListViewLayoutStyleSheet vLayout = VerticalListViewLayoutStyleSheet.instance.withIndentation( 30.0 ); 
		VerticalInlineListViewLayoutStyleSheet vInlineLayout = VerticalInlineListViewLayoutStyleSheet.instance.withIndentation( 30.0 ); 
		
		ListViewStyleSheet listView = ListViewStyleSheet.instance.withSpacingFactory( new SpacingElementFactory( 5.0 ) );
		
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		children.add( makeListView( basicStyle, listView.withListLayout( paraLayout ), texts, "PARAGRAPH", "[", "]", "," ) );
		children.add( makeListView( basicStyle, listView.withListLayout( hLayout ), lessTexts, "HORIZONTAL", "[", "]", "," ) );
		children.add( makeListView( basicStyle, listView.withListLayout( vLayout ), texts, "VERTICAL", "[", "]", "," ) );
		children.add( makeListView( basicStyle, listView.withListLayout( vInlineLayout ), texts, "VERTICAL-INLINE", "[", "]", "," ) );
		
		return basicStyle.withVBoxSpacing( 20.0 ).vbox( children );
	}
}
