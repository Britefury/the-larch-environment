//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.GSym.View.ListView;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
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
	protected DPWidget makeText(String text, TextStyleSheet styleSheet)
	{
		if ( text != null )
		{
			return new DPText( styleSheet, text );
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
		
		
		public DPWidget createElement()
		{
			return new DPText( styleSheet, text );
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
		
		
		public DPWidget createElement(int index, DPWidget child)
		{
			return new DPText( styleSheet, text );
		}
	}
	
	static class SpacingElementFactory implements ElementFactory
	{
		double spacing;
		
		public SpacingElementFactory(double spacing)
		{
			this.spacing = spacing;
		}
		
		
		public DPWidget createElement()
		{
			return new DPWhitespace( " ", spacing );
		}
	}
	
	
	protected DPWidget makeListView(ListViewLayout layout, String[] txt, String title, String beginDelim, String endDelim, String separator)
	{
		TextStyleSheet s0 = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 16 ), Color.blue );
		TextStyleSheet s1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		TextStyleSheet s2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), new Color( 0.0f, 0.5f, 0.0f ) );

		DPWidget children[] = new DPText[txt.length];
		for (int i = 0; i < txt.length; i++)
		{
			children[i] = new DPText( s1, txt[i] );
		}
		DPWidget ls = layout.createListElement( Arrays.asList( children ), new TextElementFactory( beginDelim, s2 ), new TextElementFactory( endDelim, s2 ), new TextSeparatorElementFactory( separator, s2 ) );
		
		
		DPWidget titleElem = new DPText( s0, title );
		
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox vbox = new DPVBox( boxs );
		vbox.setChildren( Arrays.asList( new DPWidget[] { titleElem, ls } ) );
		return vbox;
	}

	
	protected DPWidget createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 15.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		

		
		ParagraphStyleSheet paraStyle = new ParagraphStyleSheet();
		HBoxStyleSheet hboxStyle = new HBoxStyleSheet();
		VBoxStyleSheet vboxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		
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
		
		DPPresentationArea area = new DPPresentationArea();

		area.setChild( createContentNode() );
	     
	     
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
