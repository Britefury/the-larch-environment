//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Fraction;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.MathRoot;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class MathRootTestPage extends SystemPage
{
	protected MathRootTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Math-root test";
	}
	
	protected String getDescription()
	{
		return "The math-root element places its child within a mathematical square-root symbol."; 
	}


	private static StyleSheet styleSheet = StyleSheet.style( Primitive.editable.as( false ) );
	private static StyleSheet rootStyleSheet = styleSheet.withValues( Primitive.foreground.as( Color.black ), Primitive.hoverForeground.as( new Color( 0.0f, 0.5f, 0.5f ) ) );
	private static StyleSheet textStyleSheet = styleSheet.withValues( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ), Primitive.hoverForeground.as( null ) );

	
	private Pres makeFraction(String numeratorText, String denominatorText)
	{
		return new Fraction( textStyleSheet.applyTo( new Label( numeratorText ) ), textStyleSheet.applyTo( new Label( denominatorText ) ), "/" );
	}

	
	protected Pres createContents()
	{
		ArrayList<Object> children = new ArrayList<Object>( );
		
		children.add( rootStyleSheet.applyTo( new MathRoot( textStyleSheet.applyTo( new Label( "a" ) ) ) ) );
		children.add( rootStyleSheet.applyTo( new MathRoot( textStyleSheet.applyTo( new Label( "a+p" ) ) ) ) );
		children.add( rootStyleSheet.applyTo( new MathRoot( makeFraction( "a", "p+q" ) ) ) );
		
		return new Body( children );
	}
}
