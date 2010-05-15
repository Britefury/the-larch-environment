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
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class FractionTestPage extends SystemPage
{
	private static final PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static final PrimitiveStyleSheet fractionStyle = styleSheet.withForeground( Color.black ).withHoverForeground( new Color( 0.0f, 0.5f, 0.5f ) );
	private static final PrimitiveStyleSheet smallStyle = styleSheet.withFontSize( 10 ).withForeground( new Color( 0.0f, 0.5f, 0.0f) );
	private static final PrimitiveStyleSheet largeStyle = styleSheet.withFontSize( 24 ).withForeground( new Color( 0.0f, 0.5f, 0.0f) );
	
	
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
	
	
	private ElementFactory textFactory(final String text)
	{
		return new ElementFactory()
		{
			@Override
			public DPElement createElement(StyleSheet styleSheet)
			{
				return ((PrimitiveStyleSheet)styleSheet).text( text );
			}
		};
	}
	
	private ElementFactory fractionFactory(final ElementFactory num, final ElementFactory denom, final String barContent)
	{
		return new ElementFactory()
		{
			@Override
			public DPElement createElement(StyleSheet styleSheet)
			{
				PrimitiveStyleSheet numStyle = ((PrimitiveStyleSheet)styleSheet).fractionNumeratorStyle();
				PrimitiveStyleSheet denomStyle = ((PrimitiveStyleSheet)styleSheet).fractionDenominatorStyle();
				return ((PrimitiveStyleSheet)styleSheet).fraction( num.createElement( numStyle ), denom.createElement( denomStyle ) , barContent );
			}
		};
	}

	private ElementFactory spanFactory(final ElementFactory... children)
	{
		return new ElementFactory()
		{
			@Override
			public DPElement createElement(StyleSheet styleSheet)
			{
				DPElement childElems[] = new DPElement[children.length];
				for (int i = 0; i < children.length; i++)
				{
					childElems[i] = children[i].createElement( styleSheet );
				}
				return ((PrimitiveStyleSheet)styleSheet).span( childElems );
			}
		};
	}


	private DPElement makeFractionLine(ElementFactory num, ElementFactory denom)
	{
		ElementFactory fractionFac = fractionFactory( num, denom, "/" );
		return styleSheet.hbox( new DPElement[] { smallStyle.text( "<<Left<<" ), fractionFac.createElement( fractionStyle ), largeStyle.text( ">>Right>>" ) } );
	}
	
	protected DPElement createContents()
	{
		ArrayList<DPElement> lines = new ArrayList<DPElement>();
		
		lines.add( makeFractionLine( textFactory( "a" ), textFactory( "p" ) ) );
		lines.add( makeFractionLine( textFactory( "a" ), textFactory( "p+q" ) ) );
		lines.add( makeFractionLine( textFactory( "a+b" ), textFactory( "p" ) ) );
		lines.add( makeFractionLine( textFactory( "a+b" ), textFactory( "p+q" ) ) );

		lines.add( styleSheet.withFontSize( 24 ).text( "---" ) );
		
		lines.add( makeFractionLine( spanFactory( textFactory( "a+" ), fractionFactory( textFactory( "x" ), textFactory( "y" ), "/" ) ),
				textFactory( "p+q" ) ) );
		lines.add( makeFractionLine( spanFactory( fractionFactory( textFactory( "x" ), textFactory( "y" ), "/" ),  textFactory( "+b" ) ),
				textFactory( "p+q" ) ) );
		lines.add( makeFractionLine( textFactory( "a+b" ),
				spanFactory( textFactory( "p+" ), fractionFactory( textFactory( "x" ), textFactory( "y" ), "/" ) ) ) );
		lines.add( makeFractionLine( textFactory( "a+b" ),
				spanFactory( fractionFactory( textFactory( "x" ), textFactory( "y" ), "/" ),  textFactory( "+q" ) ) ) );
		
		return styleSheet.withVBoxSpacing( 10.0 ).vbox( lines.toArray( new DPElement[0] ) );
	}
}
