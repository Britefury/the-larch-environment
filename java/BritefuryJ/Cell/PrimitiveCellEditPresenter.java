//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.UnaryFn;

public class PrimitiveCellEditPresenter
{
	public static Pres presentChar(char c, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String str = Character.toString( c );
		return charStyle.applyTo( EditableTextCell.textCellWithCachedListener( str, textToChar ) );
	}
	
	public static Pres presentString(String text, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return stringStyle.applyTo( EditableTextCell.textCellWithCachedListener( text, textToString ) );
	}

	public static Pres presentByte(byte b, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( EditableTextCell.textCellWithCachedListener( Integer.toHexString( ((int)b) & 0xff ), textToByte ) );
	}
	
	
	public static Pres presentShort(short x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( EditableTextCell.textCellWithCachedListener( Short.toString( x ), textToShort ) );
	}
	
	public static Pres presentInt(int x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( EditableTextCell.textCellWithCachedListener( Integer.toString( x ), textToInt ) );
	}
	
	public static Pres presentLong(long x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( EditableTextCell.textCellWithCachedListener( Long.toString( x ), textToLong ) );
	}
	
	public static Pres presentBigInteger(BigInteger x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( EditableTextCell.textCellWithCachedListener( x.toString(), textToBigInteger ) );
	}
	
	public static Pres presentBigDecimal(BigDecimal x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( EditableTextCell.textCellWithCachedListener( x.toString(), textToBigDecimal ) );
	}
	
	public static Pres presentDouble(double x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return floatStyle.applyTo( EditableTextCell.textCellWithCachedListener( Double.toString( x ), textToDouble ) );
	}
	
	public static Pres presentBoolean(boolean b, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( b )
		{
			return booleanStyle.applyTo( EditableTextCell.textCellWithCachedListener( "True", textToBoolean ) );
		}
		else
		{
			return booleanStyle.applyTo( EditableTextCell.textCellWithCachedListener( "False", textToBoolean ) );
		}
	}
	
	
	
	public static abstract class TextToValue implements UnaryFn
	{
		public Object invoke(Object x)
		{
			return textToValue( (String)x );
		}
		
		public abstract Object textToValue(String textValue);
	}
	
	
	private static final TextToValue textToChar = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			if ( textValue.length() == 1 )
			{
				return textValue.charAt( 0 );
			}
			return null;
		}
	};
	
	private static final TextToValue textToString = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			return textValue;
		}
	};
	
	private static final TextToValue textToByte = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			try
			{
				return Byte.parseByte( textValue, 16 );
			}
			catch (NumberFormatException e)
			{
			}
			
			return null;
		}
	};
	
	private static final TextToValue textToShort = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			try
			{
				return Short.parseShort( textValue );
			}
			catch (NumberFormatException e)
			{
			}
			
			return null;
		}
	};
	
	private static final TextToValue textToInt = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			try
			{
				return Integer.parseInt( textValue );
			}
			catch (NumberFormatException e)
			{
			}
			
			return null;
		}
	};
	
	private static final TextToValue textToLong = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			try
			{
				return Long.parseLong( textValue );
			}
			catch (NumberFormatException e)
			{
			}
			
			return null;
		}
	};
	
	private static final TextToValue textToBigInteger = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			try
			{
				return new BigInteger( textValue );
			}
			catch (NumberFormatException e)
			{
			}
			
			return null;
		}
	};
	
	private static final TextToValue textToBigDecimal = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			try
			{
				return new BigDecimal( textValue );
			}
			catch (NumberFormatException e)
			{
			}
			
			return null;
		}
	};
	
	private static final TextToValue textToDouble = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			try
			{
				return Double.parseDouble( textValue );
			}
			catch (NumberFormatException e)
			{
			}
			
			return null;
		}
	};
	
	private static final TextToValue textToBoolean = new TextToValue()
	{
		public Object textToValue(String textValue)
		{
			String t = textValue.toLowerCase();
			
			if ( t.equals( "true" ) )
			{
				return true;
			}
			else if ( t.equals( "false" ) )
			{
				return false;
			}
			
			return null;
		}
	};
	
	
	

	private static final StyleSheet charStyle = StyleSheet.instance; 
	private static final StyleSheet stringStyle = StyleSheet.instance;
	private static final StyleSheet integerStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.5f, 0.0f, 0.5f ) ) );
	private static final StyleSheet floatStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.25f, 0.0f, 0.5f ) ) );
	private static final StyleSheet booleanStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ), Primitive.fontSmallCaps.as( true ) );
}
