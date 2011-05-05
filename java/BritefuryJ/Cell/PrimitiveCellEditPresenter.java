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
import java.util.IdentityHashMap;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.TextClipboardHandler;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleSheet;

public class PrimitiveCellEditPresenter
{
	public static Pres presentChar(char c, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String str = Character.toString( c );
		return charStyle.applyTo( presentEditableText( str, textToChar ) );
	}
	
	public static Pres presentString(String text, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return stringStyle.applyTo( presentEditableText( text, textToString ) );
	}

	public static Pres presentByte(byte b, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( presentEditableText( Integer.toHexString( ((int)b) & 0xff ), textToByte ) );
	}
	
	
	public static Pres presentShort(short x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( presentEditableText( Short.toString( x ), textToShort ) );
	}
	
	public static Pres presentInt(int x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( presentEditableText( Integer.toString( x ), textToInt ) );
	}
	
	public static Pres presentLong(long x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( presentEditableText( Long.toString( x ), textToLong ) );
	}
	
	public static Pres presentBigInteger(BigInteger x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( presentEditableText( x.toString(), textToBigInteger ) );
	}
	
	public static Pres presentBigDecimal(BigDecimal x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return integerStyle.applyTo( presentEditableText( x.toString(), textToBigDecimal ) );
	}
	
	public static Pres presentDouble(double x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return floatStyle.applyTo( presentEditableText( Double.toString( x ), textToDouble ) );
	}
	
	public static Pres presentBoolean(boolean b, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( b )
		{
			return booleanStyle.applyTo( presentEditableText( "True", textToBoolean ) );
		}
		else
		{
			return booleanStyle.applyTo( presentEditableText( "False", textToBoolean ) );
		}
	}
	
	
	
	
	
	private static final TextToValue textToChar = new TextToValue()
	{
		@Override
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
		@Override
		public Object textToValue(String textValue)
		{
			return textValue;
		}
	};
	
	private static final TextToValue textToByte = new TextToValue()
	{
		@Override
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
		@Override
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
		@Override
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
		@Override
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
		@Override
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
		@Override
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
		@Override
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
		@Override
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
	
	
	
	private static Pres presentEditableText(String text, TextToValue textToValue)
	{
		TreeEventListener listener = treeEventListenerFor( textToValue );
		Pres textPres = new Text( text );
		textPres = textPres.withTreeEventListener( listener );
		Region r = new Region( textPres, clipboardHandler );
		return r;
	}


	
	private static final TextClipboardHandler clipboardHandler = new TextClipboardHandler()
	{
		@Override
		protected void deleteText(TextSelection selection, Caret caret)
		{
			DPText textElement = (DPText)selection.getStartMarker().getElement();
			textElement.removeText( selection.getStartMarker(), selection.getEndMarker() );
		}

		@Override
		protected void insertText(Marker marker, String text)
		{
			DPText textElement = (DPText)marker.getElement();
			textElement.insertText( marker, text );
		}
		
		@Override
		protected void replaceText(TextSelection selection, Caret caret, String replacement)
		{
			DPText textElement = (DPText)selection.getStartMarker().getElement();
			textElement.replaceText( selection.getStartMarker(), selection.getEndMarker(), replacement );
		}
		
		@Override
		protected String getText(TextSelection selection)
		{
			DPText textElement = (DPText)selection.getStartMarker().getElement();
			return textElement.getTextRepresentationBetweenMarkers( selection.getStartMarker(), selection.getEndMarker() );
		}
	};
	
	
	
	private static final IdentityHashMap<TextToValue, TreeEventListener> textCellTreeEventListeners = new IdentityHashMap<TextToValue, TreeEventListener>();
	
	private static TreeEventListener treeEventListenerFor(final TextToValue textToValue)
	{
		TreeEventListener listener = textCellTreeEventListeners.get( textToValue );
		
		if ( listener == null )
		{
			listener = new TreeEventListener()
			{
				@Override
				public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
				{
					if ( event instanceof TextEditEvent )
					{
						String textValue = element.getTextRepresentation();
						Object value = textToValue.textToValue( textValue );
						if ( value != null )
						{
							CellSetValueEvent cellEvent = new CellSetValueEvent( value );
							return element.postTreeEvent( cellEvent );
						}
					}
					return false;
				}
			};
			
			
			textCellTreeEventListeners.put( textToValue, listener );
		}
		
		return listener;
	}
	
	
	

	private static final StyleSheet charStyle = StyleSheet.instance; 
	private static final StyleSheet stringStyle = StyleSheet.instance; 
	private static final StyleSheet integerStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.5f ) );
	private static final StyleSheet floatStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	private static final StyleSheet booleanStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) ).withAttr( Primitive.fontSmallCaps, true );
}
