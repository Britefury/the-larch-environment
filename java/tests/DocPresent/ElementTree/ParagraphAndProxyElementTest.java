//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.ElementTree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.LineBreakElement;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.ProxyElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class ParagraphAndProxyElementTest
{
	protected Element makeParagraphElement(String prefix, int numChildren)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 12 );
		TextStyleSheet s0 = new TextStyleSheet( f0, Color.black );
		
		ArrayList<Element> ch = new ArrayList<Element>();
		
		for (int i = 0; i < numChildren; i++)
		{
			ch.add( new TextElement( s0, prefix + String.valueOf( i ) ) );
			
			if ( i != numChildren - 1 )
			{
				TextElement space = new TextElement( s0, " " );
				ContainerStyleSheet lbs = new ContainerStyleSheet();
				LineBreakElement lb = new LineBreakElement( lbs );
				lb.setChild( space );
				ch.add( lb );
			}
		}
		
		ParagraphStyleSheet p0 = new ParagraphStyleSheet( DPParagraph.Alignment.BASELINES, 0.0, 0.0, 30.0 );
		ParagraphElement para = new ParagraphElement( p0 ); 
		
		para.setChildren( ch );
		return para;
	}

	
	protected Element makeNestedParagraphElement(String prefix, double indentation, int numChildren, int level)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 12 );
		TextStyleSheet s0 = new TextStyleSheet( f0, Color.black );
		
		ArrayList<Element> ch = new ArrayList<Element>();
		
		for (int i = 0; i < numChildren; i++)
		{
			if ( level == 0 )
			{
				ch.add( new TextElement( s0, prefix + String.valueOf( i ) ) );
			}
			else
			{
				ch.add( makeNestedParagraphElement( prefix + String.valueOf( i ) + "_", indentation, numChildren, level - 1 ) );
			}
			
			if ( i != numChildren - 1 )
			{
				TextElement space = new TextElement( s0, " " );
				ContainerStyleSheet lbs = new ContainerStyleSheet();
				LineBreakElement lb = new LineBreakElement( lbs );
				lb.setChild( space );
				ch.add( lb );
			}
		}
		
		ParagraphStyleSheet s1 = new ParagraphStyleSheet( DPParagraph.Alignment.BASELINES, 0.0, 0.0, indentation );
		ParagraphElement para = new ParagraphElement( s1 ); 
		para.setChildren( ch );
		
		ContainerStyleSheet s2 = new ContainerStyleSheet();
		ProxyElement proxy = new ProxyElement( s2 );
		proxy.setChild( para );

		
		return proxy;
	}

	
	
	protected Element makeProxy(String text)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 12 );
		TextStyleSheet s0 = new TextStyleSheet( f0, Color.black );
		TextElement t0 = new TextElement( s0, text );
		
		ContainerStyleSheet s2 = new ContainerStyleSheet();
		ProxyElement proxy = new ProxyElement( s2 );
		proxy.setChild( t0 );

		
		return proxy;
	}

	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 50.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		
		children.add( makeParagraphElement( "word_", 15 ) );
		children.add( makeProxy( "In a proxy" ) );
		children.add( makeNestedParagraphElement( "word_", 0.0, 3, 4 ) );
		
		box.setChildren( children );
		
		return box;
	}



	public ParagraphAndProxyElementTest()
	{
		JFrame frame = new JFrame( "Paragraph and proxy element test" );

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
		new ParagraphAndProxyElementTest();
	}

}
