//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class MathRootTestPage extends SystemPage
{
	protected MathRootTestPage()
	{
		register( "tests.mathroot" );
	}
	
	
	public String getTitle()
	{
		return "Math-root test";
	}
	
	protected String getDescription()
	{
		return "The math-root element places its child within a mathematical square-root symbol."; 
	}

	
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet rootStyleSheet = styleSheet.withForeground( Color.black ).withHoverForeground( new Color( 0.0f, 0.5f, 0.5f ) );
	private static PrimitiveStyleSheet textStyleSheet = styleSheet.withForeground( new Color( 0.0f, 0.5f, 0.0f ) );

	
	private DPElement makeFraction(String numeratorText, String denominatorText)
	{
		return styleSheet.fraction( textStyleSheet.staticText( numeratorText ), textStyleSheet.staticText( denominatorText ), "/" );
	}

	
	protected DPElement createContents()
	{
		ArrayList<DPElement> children = new ArrayList<DPElement>( );
		
		children.add( rootStyleSheet.mathRoot( textStyleSheet.staticText( "a" ) ) );
		children.add( rootStyleSheet.mathRoot( textStyleSheet.staticText( "a+p" ) ) );
		children.add( rootStyleSheet.mathRoot( makeFraction( "a", "p+q" ) ) );
		
		return styleSheet.vbox( children );
	}
}
