//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.GSym.View.ListView;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementFactory;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.ElementTree.WhitespaceElement;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.GSym.View.ListView.HorizontalListViewLayout;
import BritefuryJ.GSym.View.ListView.ListViewLayout;
import BritefuryJ.GSym.View.ListView.ParagraphListViewLayout;
import BritefuryJ.GSym.View.ListView.SeparatorElementFactory;
import BritefuryJ.GSym.View.ListView.VerticalInlineListViewLayout;
import BritefuryJ.GSym.View.ListView.VerticalListViewLayout;

public class ListViewTest
{
	protected Element makeText(String text, TextStyleSheet styleSheet)
	{
		if ( text != null )
		{
			return new TextElement( styleSheet, text );
		}
		else
		{
			return null;
		}
	}
	
	static class TextElementFactory implements ElementFactory
	{
		String text;
		TextStyleSheet styleSheet;
		
		public TextElementFactory(String text, TextStyleSheet styleSheet)
		{
			this.text = text;
			this.styleSheet = styleSheet;
		}
		
		
		public Element createElement()
		{
			return new TextElement( styleSheet, text );
		}
	}
	
	static class TextSeparatorElementFactory implements SeparatorElementFactory
	{
		String text;
		TextStyleSheet styleSheet;
		
		public TextSeparatorElementFactory(String text, TextStyleSheet styleSheet)
		{
			this.text = text;
			this.styleSheet = styleSheet;
		}
		
		
		public Element createElement(int index, Element child)
		{
			return new TextElement( styleSheet, text );
		}
	}
	
	static class SpacingElementFactory implements ElementFactory
	{
		double spacing;
		
		public SpacingElementFactory(double spacing)
		{
			this.spacing = spacing;
		}
		
		
		public Element createElement()
		{
			return new WhitespaceElement( " ", spacing );
		}
	}
	
	
	protected Element makeListView(ListViewLayout layout, String[] txt, String title, String beginDelim, String endDelim, String separator)
	{
		TextStyleSheet s0 = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 16 ), Color.blue );
		TextStyleSheet s1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		TextStyleSheet s2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), new Color( 0.0f, 0.5f, 0.0f ) );

		Element children[] = new TextElement[txt.length];
		for (int i = 0; i < txt.length; i++)
		{
			children[i] = new TextElement( s1, txt[i] );
		}
		Element ls = layout.createListElement( Arrays.asList( children ), new TextElementFactory( beginDelim, s2 ), new TextElementFactory( endDelim, s2 ), new TextSeparatorElementFactory( separator, s2 ) );
		
		
		Element titleElem = new TextElement( s0, title );
		
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		VBoxElement vbox = new VBoxElement( boxs );
		vbox.setChildren( Arrays.asList( new Element[] { titleElem, ls } ) );
		return vbox;
	}

	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 15.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		

		
		ParagraphStyleSheet paraStyle = new ParagraphStyleSheet();
		HBoxStyleSheet hboxStyle = new HBoxStyleSheet();
		VBoxStyleSheet vboxStyle = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		
		String[] txt = new String[] { "abcdef", "123456", "hello", "world", "this", "is", "a", "test", "of", "the", "list", "layout", "system" };
		
		ParagraphListViewLayout paraLayout = new ParagraphListViewLayout( paraStyle, new SpacingElementFactory( 5.0 ), 1, ListViewLayout.TrailingSeparator.NEVER );
		HorizontalListViewLayout hLayout = new HorizontalListViewLayout( hboxStyle, new SpacingElementFactory( 5.0 ), ListViewLayout.TrailingSeparator.NEVER );
		VerticalListViewLayout vLayout = new VerticalListViewLayout( vboxStyle, paraStyle, 30.0f, ListViewLayout.TrailingSeparator.NEVER );
		VerticalInlineListViewLayout vInlineLayout = new VerticalInlineListViewLayout( vboxStyle, paraStyle, 30.0f, ListViewLayout.TrailingSeparator.NEVER );
		
		children.add( makeListView( paraLayout, txt, "PARAGRAPH", "[", "]", "," ) );
		children.add( makeListView( hLayout, txt, "HORIZONTAL", "[", "]", "," ) );
		children.add( makeListView( vLayout, txt, "VERTICAL", "[", "]", "," ) );
		children.add( makeListView( vInlineLayout, txt, "VERTICAL-INLINE", "[", "]", "," ) );
		
		box.setChildren( children );
		
		return box;
	}



	public ListViewTest()
	{
		JFrame frame = new JFrame( "Fraction element test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		ElementTree tree = new ElementTree();

		tree.getRoot().setChild( createContentNode() );
	     
	     
		DPPresentationArea area = tree.getPresentationArea();
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
	
	
	public static void main(String[] args)
	{
		new ListViewTest();
	}
}
