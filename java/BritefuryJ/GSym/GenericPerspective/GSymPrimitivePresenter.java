//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.GenericPerspective;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Script;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.PresCom.UnescapedStringAsSpan;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GSymPrimitivePresenter
{
	public static Pres presentChar(char c, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String str = Character.toString( c );
		return new HBox( new Pres[] {
				punctuationStyle.applyTo( new StaticText(  "'" ) ),
				charStyle.applyTo( new StaticText( str ) ),
				punctuationStyle.applyTo( new StaticText(  "'" ) ) } );
	}
	
	public static Pres presentString(String text, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String textLines[] = text.split( "\n" );
		if ( textLines.length == 1 )
		{
			ArrayList<Object> lineContent = new ArrayList<Object>();
			lineContent.add( punctuationStyle.applyTo( new StaticText(  "\"" ) ) );
			lineContent.add( new UnescapedStringAsSpan( text ) );
			lineContent.add( punctuationStyle.applyTo( new StaticText(  "\"" ) ) );
			return new HBox( lineContent );
		}
		else
		{
			ArrayList<Object> lines = new ArrayList<Object>();
			int index = 0;
			for (String line: textLines)
			{
				ArrayList<Object> lineContent = new ArrayList<Object>();
				if ( index == 0 )
				{
					lineContent.add( punctuationStyle.applyTo( new StaticText(  "\"" ) ) );
				}
				lineContent.add( new UnescapedStringAsSpan( line ) );
				if ( index == textLines.length - 1 )
				{
					lineContent.add( punctuationStyle.applyTo( new StaticText(  "\"" ) ) );
				}
				lines.add( new HBox( lineContent ) );
				index++;
			}
			return multiLineStringStyle.applyTo( new VBox( lines ) );
		}
	}

	public static Pres presentByte(byte b, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( Integer.toHexString( (int)b ) ) );
	}
	
	
	public static Pres presentShort(short x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( Short.toString( x ) ) );
	}
	
	public static Pres presentInt(int x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( Integer.toString( x ) ) );
	}
	
	public static Pres presentLong(long x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( Long.toString( x ) ) );
	}
	
	public static Pres presentDouble(double x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
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
			return floatStyle.applyTo( new StaticText( asText ) );
		}
	}
	
	public static Pres presentSIDouble(String textValue, int expIndex)
	{
		Pres mantissa = floatStyle.applyTo( new StaticText( textValue.substring( 0, expIndex ) + "*10" ) );
		Pres exponent = floatStyle.applyTo( new StaticText( textValue.substring( expIndex + 1, textValue.length() ) ) );
		return Script.scriptRSuper( mantissa, exponent );
	}
	
	public static Pres presentNull()
	{
		return nullStyle.applyTo( new StaticText( "Null" ) );
	}
	
	public static Pres presentNone()
	{
		return nullStyle.applyTo( new StaticText( "None" ) );
	}
	
	public static Pres presentBoolean(boolean b, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( b )
		{
			return booleanStyle.applyTo( new StaticText( "True" ) );
		}
		else
		{
			return booleanStyle.applyTo( new StaticText( "False" ) );
		}
	}
	
	private static final StyleSheet punctuationStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color.blue );
	private static final StyleSheet charStyle = StyleSheet.instance; 
	private static final StyleSheet multiLineStringStyle = StyleSheet.instance.withAttr( Primitive.background, new FillPainter( new Color( 1.0f, 1.0f, 0.75f ) ) );
	private static final StyleSheet integerStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.5f ) );
	private static final StyleSheet floatStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	private static final StyleSheet booleanStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) ).withAttr( Primitive.textSmallCaps, true );
	private static final StyleSheet nullStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.75f, 0.0f, 0.5f ) ).withAttr( Primitive.textSmallCaps, true );
}
