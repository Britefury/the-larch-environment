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

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.FractionElement;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.ElementTree.MathRootElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class MathRootElementTest
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

	
	protected Element makeFraction(String numeratorText, String denominatorText)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 14 );
		TextStyleSheet s0 = new TextStyleSheet( f0, new Color( 0.0f, 0.5f, 0.0f ) );
		TextElement num = new TextElement( s0, numeratorText );
		TextElement denom = new TextElement( s0, denominatorText );
		
		FractionElement frac = new FractionElement();
		
		frac.setNumeratorChild( num );
		frac.setDenominatorChild( denom );

		return frac;
	}

	
	
	protected Element makeRoot(Element child)
	{
		MathRootElement root = new MathRootElement();
		root.setChild( child );
		return root;
	}

	
	protected Element makeRoot(String text)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 14 );
		TextStyleSheet s0 = new TextStyleSheet( f0, new Color( 0.0f, 0.5f, 0.0f ) );
		TextElement t = new TextElement( s0, text );
		return makeRoot( t );
	}

	
	
	protected Element wrapLeftRight(Element child)
	{
		TextStyleSheet s1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 10 ), Color.blue );
		TextStyleSheet s2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );

		TextElement labelA = new TextElement( s1, "Label A yYgGjJpPqQ" );
		TextElement labelB = new TextElement( s2, "Label B yYgGjJpPqQ" );
		
		HBoxStyleSheet boxs = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 );
		HBoxElement box = new HBoxElement( boxs );
		ArrayList<Element> boxChildren = new ArrayList<Element>();
		boxChildren.add( labelA );
		boxChildren.add( child );
		boxChildren.add( labelB );
		box.setChildren( boxChildren );
		
		return box;
	}

	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		
		children.add( wrapLeftRight( makeRoot( "a" ) ) );
		children.add( wrapLeftRight( makeRoot( "a+p" ) ) );
		children.add( wrapLeftRight( makeRoot( makeFraction( "a", "p+q" ) ) ) );

		box.setChildren( children );
		
		return box;
	}



	public MathRootElementTest()
	{
		JFrame frame = new JFrame( "Math root element test" );

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
		new MathRootElementTest();
	}
}
