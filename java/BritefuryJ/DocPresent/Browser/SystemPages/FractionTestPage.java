//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class FractionTestPage extends SystemPage
{
	private static final PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static final PrimitiveStyleSheet fractionStyle = styleSheet.withForeground( Color.black ).withHoverForeground( new Color( 0.0f, 0.5f, 0.5f ) );
	private static final PrimitiveStyleSheet smallStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 10 ) ).withForeground( new Color( 0.0f, 0.5f, 0.0f) );
	private static final PrimitiveStyleSheet largeStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 24 ) ).withForeground( new Color( 0.0f, 0.5f, 0.0f) );
	
	
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


	private DPWidget makeFractionLine(DPWidget num, DPWidget denom)
	{
		return styleSheet.hbox( Arrays.asList( new DPWidget[] { smallStyle.text( "<<Left<<" ), fractionStyle.fraction( num, denom, "/" ), largeStyle.text( ">>Right>>" ) } ) );
	}
	
	private DPWidget span(DPWidget... children)
	{
		return styleSheet.span( Arrays.asList( children ) );
	}

	protected DPWidget createContents()
	{
		ArrayList<DPWidget> lines = new ArrayList<DPWidget>();
		
		lines.add( makeFractionLine( styleSheet.text( "a" ), styleSheet.text( "p" ) ) );
		lines.add( makeFractionLine( styleSheet.text( "a" ), styleSheet.text( "p+q" ) ) );
		lines.add( makeFractionLine( styleSheet.text( "a+b" ), styleSheet.text( "p" ) ) );
		lines.add( makeFractionLine( styleSheet.text( "a+b" ), styleSheet.text( "p+q" ) ) );

		lines.add( styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 24 ) ).text( "---" ) );
		
		lines.add( makeFractionLine( span( styleSheet.text( "a+" ), fractionStyle.fraction( styleSheet.text( "x" ), styleSheet.text( "y" ), "/" ) ),
				styleSheet.text( "p+q" ) ) );
		lines.add( makeFractionLine( span( fractionStyle.fraction( styleSheet.text( "x" ), styleSheet.text( "y" ), "/" ),  styleSheet.text( "+b" ) ),
				styleSheet.text( "p+q" ) ) );
		lines.add( makeFractionLine( styleSheet.text( "a+b" ),
				span( styleSheet.text( "p+" ), fractionStyle.fraction( styleSheet.text( "x" ), styleSheet.text( "y" ), "/" ) ) ) );
		lines.add( makeFractionLine( styleSheet.text( "a+b" ),
				span( fractionStyle.fraction( styleSheet.text( "x" ), styleSheet.text( "y" ), "/" ),  styleSheet.text( "+q" ) ) ) );
		
		return styleSheet.withVBoxSpacing( 10.0 ).vbox( lines  );
	}
}
