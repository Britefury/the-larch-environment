//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.GenericPerspective;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.View.GSymFragmentViewContext;

public class GSymPrimitivePresenter
{
	public static DPElement presentChar(char c, GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		String str = Character.toString( c );
		return PrimitiveStyleSheet.instance.hbox( new DPElement[] {
				punctuationStyle.staticText(  "'" ),
				charStyle.staticText( str ),
				punctuationStyle.staticText(  "'" ) } );
	}
	
	public static DPElement presentString(String str, GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		String lines[] = str.split( "\n" );
		if ( lines.length == 1 )
		{
			ArrayList<DPElement> lineContent = new ArrayList<DPElement>();
			lineContent.add( punctuationStyle.staticText(  "\"" ) );
			lineContent.addAll( styleSheet.unescapedStringAsElementList( str ) );
			lineContent.add( punctuationStyle.staticText(  "\"" ) );
			return PrimitiveStyleSheet.instance.hbox( lineContent.toArray( new DPElement[0] ) );
		}
		else
		{
			ArrayList<DPElement> lineElements = new ArrayList<DPElement>();
			int index = 0;
			for (String line: lines)
			{
				ArrayList<DPElement> lineContent = new ArrayList<DPElement>();
				if ( index == 0 )
				{
					lineContent.add( punctuationStyle.staticText(  "\"" ) );
				}
				lineContent.addAll( styleSheet.unescapedStringAsElementList( line ) );
				if ( index == lines.length - 1 )
				{
					lineContent.add( punctuationStyle.staticText(  "\"" ) );
				}
				lineElements.add( PrimitiveStyleSheet.instance.hbox( lineContent.toArray( new DPElement[0]) ) );
				index++;
			}
			return multiLineStringStyle.vbox( lineElements.toArray( new DPElement[0]) );
		}
	}

	public static DPElement presentByte(byte b, GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return integerStyle.staticText( Integer.toHexString( (int)b ) );
	}
	
	
	public static DPElement presentShort(short x, GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return integerStyle.staticText( Short.toString( x ) );
	}
	
	public static DPElement presentInt(int x, GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return integerStyle.staticText( Integer.toString( x ) );
	}
	
	public static DPElement presentLong(long x, GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return integerStyle.staticText( Long.toString( x ) );
	}
	
	public static DPElement presentDouble(double x, GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		String asText = Double.toString( x );
		
		if ( asText.contains( "e" ) )
		{
			return presentSIDouble( asText, asText.indexOf( "e" ) );
		}
		else if ( asText.contains( "E" ) )
		{
			return presentSIDouble( asText, asText.indexOf( "E" ) );
		}
		else
		{
			return floatStyle.staticText( asText );
		}
	}
	
	public static DPElement presentSIDouble(String textValue, int expIndex)
	{
		DPElement mantissa = floatStyle.staticText( textValue.substring( 0, expIndex ) + "*10" );
		DPElement exponent = floatStyle.staticText( textValue.substring( expIndex + 1, textValue.length() ) );
		return PrimitiveStyleSheet.instance.scriptRSuper( mantissa, exponent );
	}
	
	public static DPElement presentBoolean(boolean b, GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		if ( b )
		{
			return booleanStyle.staticText( "True" );
		}
		else
		{
			return booleanStyle.staticText( "False" );
		}
	}
	private static final PrimitiveStyleSheet punctuationStyle = PrimitiveStyleSheet.instance.withForeground( Color.blue );
	private static final PrimitiveStyleSheet charStyle = PrimitiveStyleSheet.instance; 
	private static final PrimitiveStyleSheet multiLineStringStyle = PrimitiveStyleSheet.instance.withBackground( new FillPainter( new Color( 0.8f, 0.8f, 1.0f ) ) );
	private static final PrimitiveStyleSheet integerStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.5f, 0.0f, 0.5f ) );
	private static final PrimitiveStyleSheet floatStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.25f, 0.0f, 0.5f ) );
	private static final PrimitiveStyleSheet booleanStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.5f, 0.0f ) ).withTextSmallCaps( true );
}
