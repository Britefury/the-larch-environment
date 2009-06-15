//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPMathRoot;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPMathRootTest
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

	
	protected DPWidget makeFraction(String numeratorText, String denominatorText)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 14 );
		TextStyleSheet s0 = new TextStyleSheet( f0, new Color( 0.0f, 0.5f, 0.0f ) );
		DPText num = new DPText( s0, numeratorText );
		DPText denom = new DPText( s0, denominatorText );
		
		DPFraction frac = new DPFraction();
		
		frac.setNumeratorChild( num );
		frac.setDenominatorChild( denom );

		return frac;
	}

	
	protected DPWidget makeRoot(DPWidget child)
	{
		DPMathRoot root = new DPMathRoot();
		root.setChild( child );
		return root;
	}

	
	protected DPWidget makeRoot(String text)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 14 );
		TextStyleSheet s0 = new TextStyleSheet( f0, new Color( 0.0f, 0.5f, 0.0f ) );
		DPText t = new DPText( s0, text );
		return makeRoot( t );
	}

	
	
	protected DPWidget createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		
		box.append( makeRoot( "a" ) );
		box.append( makeRoot( "a+p" ) );
		box.append( makeRoot( makeFraction( "a", "p+q" ) ) );
		
		
		return box;
	}



	public DPMathRootTest()
	{
		JFrame frame = new JFrame( "Math root test" );

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
		new DPMathRootTest();
	}
}
