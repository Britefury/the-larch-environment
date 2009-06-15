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
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPFractionTest
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
		Font f0 = new Font( "Sans serif", Font.PLAIN, 16 );
		TextStyleSheet s0 = new TextStyleSheet( f0, new Color( 0.0f, 0.5f, 0.0f ) );
		TextStyleSheet s1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 10 ), Color.blue );
		TextStyleSheet s2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		DPText num = new DPText( s0, numeratorText );
		DPText denom = new DPText( s0, denominatorText );
		
		DPFraction frac = new DPFraction();
		
		frac.setNumeratorChild( num );
		frac.setDenominatorChild( denom );

		DPText labelA = new DPText( s1, "Label A yYgGjJpPqQ" );
		DPText labelB = new DPText( s2, "Label B yYgGjJpPqQ" );
		
		HBoxStyleSheet boxs = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 0.0 );
		DPHBox box = new DPHBox( boxs );
		box.append( labelA );
		box.append( frac );
		box.append( labelB );
		
		return box;
	}

	
	
	protected DPWidget createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		
		box.append( makeFraction( "a", "p" ) );
		box.append( makeFraction( "a", "p+q" ) );
		box.append( makeFraction( "a+b", "p" ) );
		box.append( makeFraction( "a+b", "p+q" ) );
		
		
		return box;
	}



	public DPFractionTest()
	{
		JFrame frame = new JFrame( "Fraction test" );

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
		new DPFractionTest();
	}
}
