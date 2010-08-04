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
import BritefuryJ.DocPresent.Combinators.Primitive.MathRoot;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

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

	
	private static StyleSheet styleSheet = StyleSheet.instance.withAttr( Primitive.editable, false );
	private static StyleSheet rootStyleSheet = styleSheet.withAttr( Primitive.foreground, Color.black ).withAttr( Primitive.hoverForeground, new Color( 0.0f, 0.5f, 0.5f ) );
	private static StyleSheet textStyleSheet = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) ).withAttr( Primitive.hoverForeground, null );

	
	private Pres makeFraction(String numeratorText, String denominatorText)
	{
		return new Fraction( textStyleSheet.applyTo( new StaticText( numeratorText ) ), textStyleSheet.applyTo( new StaticText( denominatorText ) ), "/" );
	}

	
	protected Pres createContents()
	{
		ArrayList<Object> children = new ArrayList<Object>( );
		
		children.add( rootStyleSheet.applyTo( new MathRoot( textStyleSheet.applyTo( new StaticText( "a" ) ) ) ) );
		children.add( rootStyleSheet.applyTo( new MathRoot( textStyleSheet.applyTo( new StaticText( "a+p" ) ) ) ) );
		children.add( rootStyleSheet.applyTo( new MathRoot( makeFraction( "a", "p+q" ) ) ) );
		
		return new Body( children );
	}
}
