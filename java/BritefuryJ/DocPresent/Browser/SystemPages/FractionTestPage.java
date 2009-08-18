//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;

public class FractionTestPage extends SystemPage
{
	protected FractionTestPage()
	{
		register( "tests.fraction" );
	}
	
	
	protected String getTitle()
	{
		return "Fraction test";
	}

	protected DPWidget makeText(String text, ElementStyleSheet styleSheet)
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

	
	protected DPWidget makeFraction(DPWidget num, DPWidget denom)
	{
		DPFraction frac = new DPFraction();
		
		frac.setNumeratorChild( num );
		frac.setDenominatorChild( denom );

		return frac;
	}

	protected DPWidget makeFractionLine(DPWidget num, DPWidget denom)
	{
		ElementStyleSheet s1 = DPText.styleSheet( new Font( "Sans serif", Font.PLAIN, 10 ), Color.blue );
		ElementStyleSheet s2 = DPText.styleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		
		DPWidget frac = makeFraction( num, denom );
		
		DPText labelA = new DPText( s1, "Label A yYgGjJpPqQ" );
		DPText labelB = new DPText( s2, "Label B yYgGjJpPqQ" );
		
		ElementStyleSheet boxs = DPHBox.styleSheet( VAlignment.BASELINES, 0.0, false, 0.0 );
		DPHBox box = new DPHBox( boxs );
		box.append( labelA );
		box.append( frac );
		box.append( labelB );
		
		return box;
	}

	protected DPWidget span(DPWidget... x)
	{
		DPSpan s = new DPSpan();
		s.setChildren( Arrays.asList( x ) );
		return s;
	}
	
	
	protected DPWidget createContents()
	{
		ElementStyleSheet s0 = DPText.styleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), new Color( 0.0f, 0.5f, 0.0f ) );
		ElementStyleSheet blackStyle = DPText.styleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.black );

		ElementStyleSheet boxs = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.LEFT, 10.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		
		box.append( makeFractionLine( makeText( "a", s0 ), makeText( "p", s0 ) ) );
		box.append( makeFractionLine( makeText( "a", s0 ), makeText( "p+q", s0 ) ) );
		box.append( makeFractionLine( makeText( "a+b", s0 ), makeText( "p", s0 ) ) );
		box.append( makeFractionLine( makeText( "a+b", s0 ), makeText( "p+q", s0 ) ) );

		box.append( makeText( "---", blackStyle ) );
		
		box.append( makeFractionLine( span( makeText( "a+", s0 ), makeFraction( makeText( "x", s0 ), makeText( "y", s0 ) ) ),
				makeText( "p+q", s0 ) ) );
		box.append( makeFractionLine( span( makeFraction( makeText( "x", s0 ), makeText( "y", s0 ) ),  makeText( "+b", s0 ) ),
				makeText( "p+q", s0 ) ) );
		box.append( makeFractionLine( makeText( "a+b", s0 ),
				span( makeText( "p+", s0 ), makeFraction( makeText( "x", s0 ), makeText( "y", s0 ) ) ) ) );
		box.append( makeFractionLine( makeText( "a+b", s0 ),
				span( makeFraction( makeText( "x", s0 ), makeText( "y", s0 ) ),  makeText( "+q", s0 ) ) ) );
		
		return box;
	}
}
