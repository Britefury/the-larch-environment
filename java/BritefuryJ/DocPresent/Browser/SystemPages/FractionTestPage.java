//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Fraction;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Span;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class FractionTestPage extends SystemPage
{
	private static final StyleSheet styleSheet = StyleSheet.instance;
	private static final StyleSheet fractionStyle = styleSheet.withAttr( Primitive.foreground, Color.black ).withAttr( Primitive.hoverForeground, new Color( 0.0f, 0.5f, 0.5f ) );
	private static final StyleSheet smallStyle = styleSheet.withAttr( Primitive.fontSize, 10 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f) );
	private static final StyleSheet largeStyle = styleSheet.withAttr( Primitive.fontSize, 24 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f) );
	
	
	protected FractionTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Fraction test";
	}
	
	protected String getDescription()
	{
		return "The fraction element places its two child elements into a mathematical fraction arrangement."; 
	}
	
	
	private Pres fractionOf(Pres num, Pres denom, String barContent)
	{
		return fractionStyle.applyTo( new Fraction( num, denom, barContent ) );
	}

	private Pres spanOf(Pres... children)
	{
		return new Span( children );
	}


	private Pres makeFractionLine(Pres num, Pres denom)
	{
		Pres fractionFac = fractionOf( num, denom, "/" );
		return styleSheet.applyTo( new HBox( new Pres[] { smallStyle.applyTo( new Text( "<<Left<<" ) ), fractionFac, largeStyle.applyTo( new Text( ">>Right>>" ) ) } ) );
	}
	
	protected Pres createContents()
	{
		ArrayList<Object> lines = new ArrayList<Object>();
		
		lines.add( makeFractionLine( new Text( "a" ), new Text( "p" ) ) );
		lines.add( makeFractionLine( new Text( "a" ), new Text( "p+q" ) ) );
		lines.add( makeFractionLine( new Text( "a+b" ), new Text( "p" ) ) );
		lines.add( makeFractionLine( new Text( "a+b" ), new Text( "p+q" ) ) );

		lines.add( styleSheet.withAttr( Primitive.fontSize, 24 ).applyTo( new Text( "---" ) ) );
		
		lines.add( makeFractionLine( spanOf( new Text( "a+" ), fractionOf( new Text( "x" ), new Text( "y" ), "/" ) ),
				new Text( "p+q" ) ) );
		lines.add( makeFractionLine( spanOf( fractionOf( new Text( "x" ), new Text( "y" ), "/" ),  new Text( "+b" ) ),
				new Text( "p+q" ) ) );
		lines.add( makeFractionLine( new Text( "a+b" ),
				spanOf( new Text( "p+" ), fractionOf( new Text( "x" ), new Text( "y" ), "/" ) ) ) );
		lines.add( makeFractionLine( new Text( "a+b" ),
				spanOf( fractionOf( new Text( "x" ), new Text( "y" ), "/" ),  new Text( "+q" ) ) ) );
		
		return new Body( lines );
	}
}
