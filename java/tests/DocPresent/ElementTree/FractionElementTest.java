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
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class FractionElementTest
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
		Font f0 = new Font( "Sans serif", Font.PLAIN, 16 );
		TextStyleSheet s0 = new TextStyleSheet( f0, new Color( 0.0f, 0.5f, 0.0f ) );
		TextStyleSheet s1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 10 ), Color.blue );
		TextStyleSheet s2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		TextElement num = new TextElement( s0, numeratorText );
		TextElement denom = new TextElement( s0, denominatorText );
		
		FractionElement frac = new FractionElement();
		
		frac.setNumeratorChild( num );
		frac.setDenominatorChild( denom );

		TextElement labelA = new TextElement( s1, "Label A yYgGjJpPqQ" );
		TextElement labelB = new TextElement( s2, "Label B yYgGjJpPqQ" );
		
		HBoxStyleSheet boxs = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 );
		HBoxElement box = new HBoxElement( boxs );
		ArrayList<Element> boxChildren = new ArrayList<Element>();
		boxChildren.add( labelA );
		boxChildren.add( frac );
		boxChildren.add( labelB );
		box.setChildren( boxChildren );
		
		return box;
	}

	
	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		
		children.add( makeFraction( "a", "p" ) );
		children.add( makeFraction( "a", "p+q" ) );
		children.add( makeFraction( "a+b", "p" ) );
		children.add( makeFraction( "a+b", "p+q" ) );
		
		box.setChildren( children );
		
		return box;
	}



	public FractionElementTest()
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
		new FractionElementTest();
	}
}
