//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package visualtests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;


public class DPParagraphTest
{
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected ArrayList<DPWidget> makeTextNodes(String text, TextStyleSheet style)
	{
		String[] words = text.split( " " );
		ArrayList<DPWidget> nodes = new ArrayList<DPWidget>();
		for (int i = 0; i < words.length; i++)
		{
			String word = words[i];
			nodes.add( new DPText( style, word ) );
		}
		return nodes;
	}
	
	protected ArrayList<DPWidget> addLineBreaks(ArrayList<DPWidget> nodesIn, int step)
	{
		ArrayList<DPWidget> nodesOut = new ArrayList<DPWidget>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				DPText space = new DPText( " " );
				DPLineBreak b = new DPLineBreak();
				b.setChild( space );
				nodesOut.add( b );
			}
			else
			{
				nodesOut.add( new DPText( " " ) );
			}
		}
		return nodesOut;
	}
	
	
	protected DPVBox makeTextVBox(VTypesetting typesetting, HAlignment alignment, double spacing, double padding)
	{
		TextStyleSheet style = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		DPWidget t1 = new DPText( style, "Vert1" );
		DPWidget t2 = new DPText( style, "Vert2" );
		DPWidget t3 = new DPText( style, "Vert3" );
		DPWidget[] children = { t1, t2, t3 };
		VBoxStyleSheet boxs = new VBoxStyleSheet( typesetting, alignment, spacing, false, padding );
		DPVBox box = new DPVBox( boxs );
		box.extend( children );
		return box;
	}
	
	protected DPParagraph makeParagraph(String title, VAlignment alignment, double spacing, double vSpacing, double padding, double indentation, int lineBreakStep, TextStyleSheet textStyle)
	{
		ArrayList<DPWidget> children = makeTextNodes( title + ": " + textBlock, textStyle );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep );
		}
		ParagraphStyleSheet boxs = new ParagraphStyleSheet( alignment, spacing, vSpacing, padding, indentation );
		DPParagraph box = new DPParagraph( boxs );
		box.extend( children );
		return box;
	}
	
	protected DPParagraph makeParagraphWithNestedPara(String title, VAlignment alignment, double spacing, double vSpacing, double padding, double indentation, int lineBreakStep, TextStyleSheet textStyle, TextStyleSheet nestedTextStyle)
	{
		ArrayList<DPWidget> children = makeTextNodes( title + ": " + textBlock, textStyle );
		children = addLineBreaks( children, lineBreakStep );
		children.add( children.size()/2, makeParagraph( title + " (inner)", alignment, spacing, vSpacing, padding, indentation, lineBreakStep, nestedTextStyle ) );
		ParagraphStyleSheet boxs = new ParagraphStyleSheet( alignment, spacing, vSpacing, padding, indentation );
		DPParagraph box = new DPParagraph( boxs );
		box.extend( children );
		return box;
	}
	
	
	protected DPWidget createContentNode()
	{
		TextStyleSheet blackText = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		TextStyleSheet redText = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.red );
		
		DPWidget b1 = makeParagraph( "ONE-LINE", VAlignment.BASELINES, 0.0, 0.0, 0.0, 0.0, 0, blackText );
		DPWidget b2 = makeParagraph( "PER-WORD", VAlignment.BASELINES, 0.0, 0.0, 0.0, 0.0, 1, blackText );
		DPWidget b3 = makeParagraph( "EVERY-4-WORDS", VAlignment.BASELINES, 0.0, 0.0, 0.0, 0.0, 4, blackText);
		DPWidget b4 = makeParagraphWithNestedPara( "NESTED-1", VAlignment.BASELINES, 0.0, 0.0, 0.0, 0.0, 1, blackText, redText );
		DPWidget b5 = makeParagraphWithNestedPara( "NESTED-2", VAlignment.BASELINES, 0.0, 0.0, 0.0, 0.0, 2, blackText, redText );
		DPWidget b6 = makeParagraphWithNestedPara( "NESTED-4", VAlignment.BASELINES, 0.0, 0.0, 0.0, 0.0, 4, blackText, redText );
		DPWidget b7 = makeParagraph( "PER-WORD INDENTED", VAlignment.BASELINES, 0.0, 0.0, 0.0, 50.0, 1, blackText );
		DPWidget b8 = makeParagraphWithNestedPara( "NESTED-2-INDENTED", VAlignment.BASELINES, 0.0, 0.0, 0.0, 50.0, 2, blackText, redText );
		DPWidget[] children = { b1, b2, b3, b4, b5, b6, b7, b8 };
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 30.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		box.extend( children );
		return box;
	}



	public DPParagraphTest()
	{
		JFrame frame = new JFrame( "Paragraph test" );

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
		new DPParagraphTest();
	}
}
