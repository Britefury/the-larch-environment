//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package visualtests.DocPresent.ElementTree;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.LineBreakElement;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.SpanElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class ParagraphElementTest extends ElementTreeTestBase
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
		
		ParagraphStyleSheet p0 = new ParagraphStyleSheet( VAlignment.BASELINES, 0.0, 0.0, 0.0, 30.0 );
		ParagraphElement para = new ParagraphElement( p0 ); 
		
		para.setChildren( ch );
		return para;
	}

	
	protected Element makeNestedSpanElement(String prefix, double indentation, int numChildren, int level)
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
				ch.add( makeNestedSpanElement( prefix + String.valueOf( i ) + "_", indentation, numChildren, level - 1 ) );
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
		
		SpanElement span = new SpanElement();
		span.setChildren( ch );
		return span;
	}

	
	protected Element makeNestedParagraphElement(String prefix, double indentation, int numChildren, int level)
	{
		ParagraphStyleSheet p0 = new ParagraphStyleSheet( VAlignment.BASELINES, 0.0, 0.0, 0.0, indentation );
		ParagraphElement para = new ParagraphElement( p0 ); 
		
		para.setChildren( Arrays.asList( new Element[] { makeNestedSpanElement( prefix, indentation, numChildren, level ) } ) );
		return para;
	}
	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 50.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		
		children.add( makeParagraphElement( "word_", 15 ) );
		children.add( makeNestedParagraphElement( "word_", 0.0, 3, 4 ) );
		
		box.setChildren( children );
		
		return box;
	}



	public ParagraphElementTest()
	{
		JFrame frame = new JFrame( "Paragraph element test" );
		initFrame( frame );
	}
	
	
	public static void main(String[] args)
	{
		new ParagraphElementTest();
	}
}
