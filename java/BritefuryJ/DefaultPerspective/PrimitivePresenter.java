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

import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.UnescapedStringAsSpan;
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
	public static Pres presentBoolean(boolean b)
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
	
	public static Pres presentChar(char c)
	{
		String str = Character.toString( c );
		return new Row( new Pres[] {
				punctuationStyle.applyTo( new StaticText(  "'" ) ),
				charStyle.applyTo( new StaticText( str ) ),
				punctuationStyle.applyTo( new StaticText(  "'" ) ) } );
	}
	
	public static Pres presentString(String text)
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

	public static Pres presentByte(byte b)
	{
		return integerStyle.applyTo( new StaticText( Integer.toHexString( ((int)b) & 0xff ) ) );
	}
	
	
	public static Pres presentShort(short x)
	{
		return integerStyle.applyTo( new StaticText( Short.toString( x ) ) );
	}
	
	public static Pres presentInt(int x)
	{
		return integerStyle.applyTo( new StaticText( Integer.toString( x ) ) );
	}
	
	public static Pres presentLong(long x)
	{
		return integerStyle.applyTo( new StaticText( Long.toString( x ) ) );
	}
	
	public static Pres presentBigInteger(BigInteger x)
	{
		return integerStyle.applyTo( new StaticText( x.toString() + "L" ) );
	}
	
	public static Pres presentBigDecimal(BigDecimal x)
	{
		return integerStyle.applyTo( new StaticText( x.toString() + "LD" ) );
	}
	
	public static Pres presentFloat(float x)
	{
		String asText = Float.toString( x );
		
		if ( asText.contains( "e" ) )
		{
			return presentSIReal( asText, asText.indexOf( "e" ) );
		}
		else if ( asText.contains( "E" ) )
		{
			return presentSIReal( asText, asText.indexOf( "E" ) );
		}
		else
		{
			return floatStyle.applyTo( new StaticText( asText ) );
		}
	}
	
	public static Pres presentDouble(double x)
	{
		String asText = Double.toString( x );
		
		if ( asText.contains( "e" ) )
		{
			return presentSIReal( asText, asText.indexOf( "e" ) );
		}
		else if ( asText.contains( "E" ) )
		{
			return presentSIReal( asText, asText.indexOf( "E" ) );
		}
		else
		{
			return floatStyle.applyTo( new StaticText( asText ) );
		}
	}
	
	public static Pres presentSIReal(String textValue, int expIndex)
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
	
	
	public static boolean isSmallString(String str)
	{
		return !str.contains( "\n" )  ||  str.length() < 128;
	}
	
	
	public static boolean isSmallPrimitive(Object x)
	{
		if ( x == null  ||  x instanceof Boolean  ||  x instanceof Character  ||
			x instanceof Byte  ||  x instanceof Short  ||  x instanceof Integer  ||  x instanceof Long  ||  x instanceof Float  ||  x instanceof Double )
		{
			return true;
		}
		else if ( x instanceof String )
		{
			return isSmallString( (String)x );
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isPrimitive(Object x)
	{
		return x == null  ||  x instanceof Boolean  ||  x instanceof Character  ||  x instanceof String  ||
			x instanceof Byte  ||  x instanceof Short  ||  x instanceof Integer  ||  x instanceof Long  ||  x instanceof Float  ||  x instanceof Double  ||
			x instanceof BigInteger  ||  x instanceof BigDecimal;
	}
	
	
	public static Pres presentPrimitive(Object x)
	{
		if ( x == null )
		{
			return presentNull();
		}
		else if ( x instanceof Boolean )
		{
			return presentBoolean( (Boolean)x );
		}
		else if ( x instanceof Character )
		{
			return presentChar( (Character)x );
		}
		else if ( x instanceof String )
		{
			return presentString( (String)x );
		}
		else if ( x instanceof String )
		{
			return presentString( (String)x );
		}
		else if ( x instanceof Byte )
		{
			return presentByte( (Byte)x );
		}
		else if ( x instanceof Short )
		{
			return presentShort( (Short)x );
		}
		else if ( x instanceof Integer )
		{
			return presentInt( (Integer)x );
		}
		else if ( x instanceof Long )
		{
			return presentLong( (Long)x );
		}
		else if ( x instanceof Float )
		{
			return presentFloat( (Float)x );
		}
		else if ( x instanceof Double )
		{
			return presentDouble( (Double)x );
		}
		else if ( x instanceof BigInteger )
		{
			return presentBigInteger( (BigInteger)x );
		}
		else if ( x instanceof BigDecimal )
		{
			return presentBigDecimal( (BigDecimal)x );
		}
		else
		{
			throw new RuntimeException( "Object is not a primitive" );
		}
	}
	
	
	
	public static boolean isSmallPrimitivePy(PyObject x)
	{
		if ( x == null  ||  x instanceof PyBoolean  ||  x instanceof PyInteger  ||  x instanceof PyFloat )
		{
			return true;
		}
		else if ( x instanceof PyString  ||  x instanceof PyUnicode )
		{
			return isSmallString( x.asString() );
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isPrimitivePy(PyObject x)
	{
		return x == null  ||  x instanceof PyBoolean  ||  x instanceof PyString  ||  x instanceof PyUnicode  ||
			x instanceof PyInteger  ||  x instanceof PyLong  ||  x instanceof PyFloat;
	}
	
	
	public static Pres presentPrimitivePy(PyObject x)
	{
		if ( x == Py.None )
		{
			return presentNone();
		}
		else if ( x instanceof PyBoolean )
		{
			return presentBoolean( ((PyBoolean)x).getBooleanValue() );
		}
		else if ( x instanceof PyString  ||  x instanceof PyUnicode )
		{
			return presentString( x.asString() );
		}
		else if ( x instanceof PyInteger )
		{
			return presentInt( x.asInt() );
		}
		else if ( x instanceof PyLong )
		{
			return presentBigInteger( ((PyLong)x).getValue() );
		}
		else if ( x instanceof PyFloat )
		{
			return presentDouble( x.asDouble() );
		}
		else
		{
			throw new RuntimeException( "Python object is not a primitive" );
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
		return labelStyle.applyTo( new Column( linePres ) );
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
			boolean isSmall = false;
			Object value = null;
			Pres valuePres = null;
			try
			{
				value = field.get( x );
			}
			catch (IllegalArgumentException e1)
			{
				valuePres = errorStyle.applyTo( new Label( "<Illegal argument>" ) );
				isSmall = true;
			}
			catch (IllegalAccessException e1)
			{
				valuePres = errorStyle.applyTo( new Label( "<Cannot access>" ) );
				isSmall = true;
			}
			
			if ( valuePres == null )
			{
				// No exception thrown while getting field value
				if ( isPrimitive( value ) )
				{
					isSmall = isSmallPrimitive( value );
					valuePres = presentPrimitive( value );
				}
				else
				{
					valuePres = new InnerFragment( value );
				}
			}
			if ( isSmall )
			{
				fields.add( new Row( new Pres[] {
						presentJavaClassName( field.getType(), typeNameStyle ),
						space,
						getAccessNameStyle( field.getModifiers() ).applyTo( new Label( field.getName() ) ),
						space,
						valuePres } ) );
			}
			else
			{
				Pres header = new Row( new Pres[] {
						presentJavaClassName( field.getType(), typeNameStyle ),
						space,
						getAccessNameStyle( field.getModifiers() ).applyTo( new Label( field.getName() ) ) } );
				fields.add( new Column( new Pres[] { header, valuePres.padX( 45.0, 0.0 ) } ) );
			}
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
				Pres valuePres = null;
				boolean isSmall = false;
				
				if ( isPrimitivePy( value ) )
				{
					isSmall = isSmallPrimitivePy( value );
					valuePres = presentPrimitivePy( value );
				}
				else
				{
					valuePres = new InnerFragment( value );
					isSmall = false;
				}
				
				if ( isSmall )
				{
					attributes.add( new Row( new Pres[] { namePres, space, valuePres } ) );
				}
				else
				{
					attributes.add( new Column( new Pres[] { namePres, valuePres.padX( 15.0, 0.0 ) } ) );
				}
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
	private static final StyleSheet labelStyle = StyleSheet.instance.withAttr( Primitive.editable, false ).withAttr( Primitive.selectable, false );
	
	
	private static final StyleSheet sectionHeadingStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontFace, "Serif" );
	private static final StyleSheet attributeNameStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.25f ) );

	private static final StyleSheet typePunctuationStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	private static final StyleSheet typeNameStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.4f ) );

	private static final StyleSheet privateNameStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.0f ) );
	private static final StyleSheet protectedNameStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.35f, 0.35f, 0.0f ) );
	private static final StyleSheet publicNameStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static final StyleSheet defaultNameStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.35f, 0.35f, 0.15f ) );

	private static final StyleSheet errorStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.0f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontFace, "Serif" );

	private static final Pres space = staticStyle.applyTo( new StaticText( " " ) );
}
