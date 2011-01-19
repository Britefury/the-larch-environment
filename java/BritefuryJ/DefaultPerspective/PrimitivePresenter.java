//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DefaultPerspective;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.DefaultPerspective.Pres.UnescapedStringAsSpan;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Script;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.StyleSheet.StyleSheet;

public class PrimitivePresenter
{
	public static Pres presentChar(char c, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String str = Character.toString( c );
		return new Row( new Pres[] {
				punctuationStyle.applyTo( new StaticText(  "'" ) ),
				charStyle.applyTo( new StaticText( str ) ),
				punctuationStyle.applyTo( new StaticText(  "'" ) ) } );
	}
	
	public static Pres presentString(String text, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String textLines[] = text.split( "\n" );
		if ( textLines.length == 1 )
		{
			ArrayList<Object> lineContent = new ArrayList<Object>();
			lineContent.add( punctuationStyle.applyTo( new StaticText(  "\"" ) ) );
			lineContent.add( new UnescapedStringAsSpan( text ) );
			lineContent.add( punctuationStyle.applyTo( new StaticText(  "\"" ) ) );
			return new Row( lineContent );
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
				lines.add( new Row( lineContent ) );
				index++;
			}
			return multiLineStringStyle.applyTo( new Column( lines ) );
		}
	}

	public static Pres presentByte(byte b, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( Integer.toHexString( ((int)b) & 0xff ) ) );
	}
	
	
	public static Pres presentShort(short x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( Short.toString( x ) ) );
	}
	
	public static Pres presentInt(int x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( Integer.toString( x ) ) );
	}
	
	public static Pres presentLong(long x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( Long.toString( x ) ) );
	}
	
	public static Pres presentBigInteger(BigInteger x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( x.toString() + "L" ) );
	}
	
	public static Pres presentBigDecimal(BigDecimal x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( new StaticText( x.toString() + "LD" ) );
	}
	
	public static Pres presentDouble(double x, FragmentView fragment, SimpleAttributeTable inheritedState)
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
	
	public static Pres presentBoolean(boolean b, FragmentView fragment, SimpleAttributeTable inheritedState)
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
	
	
	
	public static Pres presentObjectAsString(Object x)
	{
		String lines[] = x.toString().split( "\n" );
		Pres linePres[] = new Pres[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			linePres[i] = new NormalText( lines[i] );
		}
		return staticStyle.applyTo( new Column( linePres ) );
	}
	
	
	public static Pres presentJavaObjectInspector(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres asString = presentObjectAsString( x );

		ArrayList<Object> contents = new ArrayList<Object>();
		
		// Type
		Pres type = new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Type" ) ), new InnerFragment( x.getClass() ) );
		contents.add( type );
		
		
		ArrayList<Object> fields = new ArrayList<Object>();
		Class<?> cls = x.getClass();
		for (Field field: cls.getFields())
		{
			Pres header = new Row( new Pres[] {
					presentJavaClassName( field.getType(), typeNameStyle ),
					space,
					getAccessNameStyle( field.getModifiers() ).applyTo( new Label( field.getName() ) ) } );
			Pres value;
			try
			{
				value = new InnerFragment( field.get( x ) );
			}
			catch (IllegalArgumentException e)
			{
				value = errorStyle.applyTo( new Label( "<Illegal argument>" ) );
			}
			catch (IllegalAccessException e)
			{
				value = errorStyle.applyTo( new Label( "<Cannot access>" ) );
			}
			fields.add( new Column( new Pres[] { header, value.padX( 45.0, 0.0 ) } ) );
		}
		if ( fields.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Fields" ) ), new Column( fields ) ) );
		}
		
		Pres inspector = new Column( contents );
		return new DropDownExpander( asString, inspector );
	}
	
	public static Pres presentPythonObjectInspector(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres asString = presentObjectAsString( x );

		ArrayList<Object> contents = new ArrayList<Object>();
		
		// Type
		Pres type = new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Type" ) ), new InnerFragment( x.getType() ) );
		contents.add( type );
		
		
		// Attributes
		ArrayList<Object> attributes = new ArrayList<Object>();
		PyObject dict = x.fastGetDict();
		if ( dict != null )
		{
			PyList dictItems;
			if ( dict instanceof PyDictionary )
			{
				dictItems = ((PyDictionary)dict).items();
			}
			else if ( dict instanceof PyStringMap )
			{
				dictItems = ((PyStringMap)dict).items();
			}
			else
			{
				throw new RuntimeException( "Expected a PyDictionary or a PyStringMap when acquiring __dict__ from a PyObject" );
			}
			
			
			for (Object dictItem: dictItems)
			{
				PyTuple pair = (PyTuple)dictItem;
				PyObject key = pair.getArray()[0];
				PyObject value = pair.getArray()[1];
				String name = key.toString();
				
				if ( name.equals( "__dict__" ) )
				{
					break;
				}
				
				Pres namePres = attributeNameStyle.applyTo( new Label( name ) );
				Pres valueView = new InnerFragment( value ).padX( 15.0, 0.0 );
				attributes.add( new Column( new Pres[] { namePres, valueView } ) );
			}
		}
		
		if ( attributes.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Attributes" ) ),   new Column( attributes ) ) );
		}
		
		
		Pres inspector = new Column( contents );
		return new DropDownExpander( asString, inspector );
	}
	
	
	
	
	public static StyleSheet getAccessNameStyle(int modifiers)
	{
		if ( Modifier.isPrivate( modifiers ) )
		{
			return privateNameStyle;
		}
		else if ( Modifier.isProtected( modifiers ) )
		{
			return protectedNameStyle;
		}
		else if ( Modifier.isPublic( modifiers ) )
		{
			return publicNameStyle;
		}
		else
		{
			return defaultNameStyle;
		}
	}
	
	

	public static Pres presentJavaClassName(Class<?> c, StyleSheet classNameStyle)
	{
		if ( c.isArray() )
		{
			c = c.getComponentType();
			return new Span( new Pres[] { classNameStyle.applyTo( new Label( c.getName() ) ), typePunctuationStyle.applyTo( new Label( "[]" ) ) } );
		}
		else
		{
			return classNameStyle.applyTo( new Label( c.getName() ) );
		}
	}
	
	
	private static final StyleSheet punctuationStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color.blue );
	private static final StyleSheet charStyle = StyleSheet.instance; 
	private static final StyleSheet multiLineStringStyle = StyleSheet.instance.withAttr( Primitive.background, new FillPainter( new Color( 1.0f, 1.0f, 0.75f ) ) );
	private static final StyleSheet integerStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.5f ) );
	private static final StyleSheet floatStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	private static final StyleSheet booleanStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) ).withAttr( Primitive.fontSmallCaps, true );
	private static final StyleSheet nullStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.75f, 0.0f, 0.5f ) ).withAttr( Primitive.fontSmallCaps, true );



	private static final StyleSheet staticStyle = StyleSheet.instance.withAttr( Primitive.editable, false );
	
	
	private static final StyleSheet sectionHeadingStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontFace, "Serif" );
	private static final StyleSheet attributeNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.25f ) );

	private static final StyleSheet typePunctuationStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	private static final StyleSheet typeNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.4f ) );

	private static final StyleSheet privateNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.0f ) );
	private static final StyleSheet protectedNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.35f, 0.35f, 0.0f ) );
	private static final StyleSheet publicNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static final StyleSheet defaultNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.35f, 0.35f, 0.15f ) );

	private static final StyleSheet errorStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.0f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontFace, "Serif" );

	private static final Pres space = staticStyle.applyTo( new StaticText( " " ) );
}
