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
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class FractionTestPage extends SystemPage
{
	protected FractionTestPage()
	{
		register( "tests.fraction" );
	}
	
	
	public String getTitle()
	{
		return "Fraction test";
	}
	
	protected String getDescription()
	{
		return "The fraction element places its two child elements into a mathematical fraction arrangement."; 
	}

	protected DPWidget makeText(String text, TextStyleSheet styleSheet)
	{
		if ( text != null )
		{
			return new DPText( getContext(), styleSheet, text );
		}
		else
		{
			return null;
		}
	}

	
	protected DPWidget makeFraction(DPWidget num, DPWidget denom)
	{
		DPFraction frac = new DPFraction( getContext() );
		
		frac.setNumeratorChild( num );
		frac.setDenominatorChild( denom );

		return frac;
	}

	protected DPWidget makeFractionLine(DPWidget num, DPWidget denom)
	{
		TextStyleSheet s1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 10 ), Color.blue );
		TextStyleSheet s2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		
		DPWidget frac = makeFraction( num, denom );
		
		DPText labelA = new DPText( getContext(), s1, "<<Left<<" );
		DPText labelB = new DPText( getContext(), s2, ">>Right>>" );
		
		DPHBox box = new DPHBox( getContext() );
		box.append( labelA );
		box.append( frac );
		box.append( labelB );
		
		return box;
	}

	protected DPWidget span(DPWidget... x)
	{
		DPSpan s = new DPSpan( getContext() );
		s.setChildren( Arrays.asList( x ) );
		return s;
	}
	
	
	protected DPWidget createContents()
	{
		TextStyleSheet s0 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), new Color( 0.0f, 0.5f, 0.0f ) );
		TextStyleSheet blackStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.black );

		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, 10.0 );
		DPVBox box = new DPVBox( getContext(), boxs );
		
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
