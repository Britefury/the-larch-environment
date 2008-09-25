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
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.FractionElement;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.ScriptElement;
import BritefuryJ.DocPresent.ElementTree.SegmentElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.ElementTree.WhitespaceElement;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class SegmentElementTest
{
	protected Element text(String text)
	{
		TextStyleSheet s0 = new TextStyleSheet();
		return new TextElement( s0, text );
	}
	
	
	protected Element segment(Element x)
	{
		SegmentElement e = new SegmentElement( SegmentElement.defaultStopFactory );
		e.setChild( x );
		return e;
	}

	
	
	protected Element line(Element x)
	{
		ParagraphElement para = new ParagraphElement();
		Element[] children = { segment( x ), new WhitespaceElement( "\n" ) };
		para.setChildren( Arrays.asList( children ) );
		return para;
	}
	
	
	protected Element pow(Element x, Element y)
	{
		ScriptElement script = new ScriptElement();
		
		script.setMainChild( segment( x ) );
		script.setRightSuperscriptChild( segment( y ) );
		return script;
	}
	
	
	protected Element div(Element x, Element y)
	{
		FractionElement frac = new FractionElement();
		
		frac.setNumeratorChild( segment( x ) );
		frac.setDenominatorChild( segment( y ) );
		return frac;
	}
	
	
	
	protected Element bin(Element x, String op, Element y)
	{
		TextStyleSheet opss = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 12 ), new Color( 0.0f, 0.5f, 0.0f ) );
		ParagraphElement para = new ParagraphElement();
		Element[] children = { x, new TextElement( opss, " " + op + " " ), y };
		para.setChildren( Arrays.asList( children ) );
		return para;
	}
	
	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		Vector<Element> children = new Vector<Element>();
		
		children.add( line( text( "seg" ) ) );
		children.add( line( bin( text( "a" ), "+", text( "b" ) ) ) );
		children.add( line( pow( text( "a" ), text( "b" ) ) ) );
		children.add( line( div( text( "a" ), text( "b" ) ) ) );
		children.add( line( div( text( "a" ), bin( text( "x" ), "+", pow( text( "p" ), text( "q" ) ) ) ) ) );
		
		box.setChildren( children );
		
		return box;
	}



	public SegmentElementTest()
	{
		JFrame frame = new JFrame( "Segment test" );

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
		new SegmentElementTest();
	}
}
