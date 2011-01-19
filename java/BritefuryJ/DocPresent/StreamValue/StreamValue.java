//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StreamValue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.python.core.PySlice;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DefaultPerspective.Pres.GenericStyle;
import BritefuryJ.DefaultPerspective.Pres.ObjectBox;
import BritefuryJ.DefaultPerspective.Pres.UnescapedStringAsSpan;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class StreamValue implements Presentable
{
	public static abstract class Item
	{
		protected int start, stop;
		
		
		public Item(int start, int stop)
		{
			this.start = start;
			this.stop = stop;
		}

		
		public int getStart()
		{
			return start;
		}
		
		public int getStop()
		{
			return stop;
		}
		
		public int getLength()
		{
			return stop - start;
		}
		
		
		abstract public boolean isStructural();
		
		
		abstract public Item subItemFrom(int start, int atPos);
		abstract public Item subItemTo(int end, int atPos);
		abstract public Item subItem(int start, int end, int atPos);
		abstract public Item copyAt(int atPos);
	}
	
	
	
	public static class TextItem extends Item implements Presentable
	{
		protected String textValue;
		
		
		public TextItem(String textValue, int start, int stop)
		{
			super( start, stop );
			this.textValue = textValue;
		}

		
		public boolean isStructural()
		{
			return false;
		}

		
		public Item subItemFrom(int start, int atPos)
		{
			int offset = start - this.start;
			return new TextItem( textValue.substring( offset ), atPos, atPos + textValue.length() - offset );
		}
		
		public Item subItemTo(int end, int atPos)
		{
			end -= start;
			return new TextItem( textValue.substring( 0, end ), atPos, atPos + end );
		}
		
		public Item subItem(int start, int end, int atPos)
		{
			int offset = start - this.start;
			end -= this.start;
			return new TextItem( textValue.substring( offset, end ), atPos, atPos + end - offset );
		}
		
		public Item copyAt(int atPos)
		{
			return new TextItem( textValue, atPos, atPos + stop - start );
		}
		
		
		public String getTextValue()
		{
			return textValue;
		}
		
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof TextItem )
			{
				TextItem tx = (TextItem)x;
				
				return textValue.equals( tx.textValue );
			}
			
			return false;
		}
		
		
		public String toString()
		{
			return textValue;
		}


		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return new UnescapedStringAsSpan( textValue );
		}
	}
	
	
	
	public static class StructuralItem extends Item implements Presentable
	{
		protected Object structuralValue;
		
		
		public StructuralItem(Object structuralValue, int start, int stop)
		{
			super( start, stop );
			this.structuralValue = structuralValue;
		}
		
		
		public boolean isStructural()
		{
			return true;
		}

		
		public Item subItemFrom(int start, int atPos)
		{
			return new StructuralItem( structuralValue, atPos, atPos + 1 );
		}
		
		public Item subItemTo(int end, int atPos)
		{
			return new StructuralItem( structuralValue, atPos, atPos + 1 );
		}
		
		public Item subItem(int start, int end, int atPos)
		{
			return new StructuralItem( structuralValue, atPos, atPos + 1 );
		}
		
		public Item copyAt(int atPos)
		{
			return new StructuralItem( structuralValue, atPos, atPos + 1 );
		}
		
		
		public Object getStructuralValue()
		{
			return structuralValue;
		}
		
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof StructuralItem )
			{
				StructuralItem sx = (StructuralItem)x;
				
				return structuralValue.equals( sx.structuralValue );
			}
			
			return false;
		}
		
		
		public String toString()
		{
			return "<<--Structural: " + structuralValue.toString() + "-->>";
		}


		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return borderStyle.applyTo( new Border( new InnerFragment( structuralValue ) ) );
		}

	
		private static StyleSheet borderStyle = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.15f, 0.25f, 0.75f ), null ) ); 
	}
	
	

	protected Item items[];
	protected int length;
	
	
	public StreamValue(String text)
	{
		items = new Item[] { new TextItem( text, 0, text.length() ) };
		length = text.length();
	}
	
	protected StreamValue(Item items[])
	{
		this.items = items;
		length = items.length > 0  ?  items[items.length-1].stop  :  0;
	}
	
	
	
	public boolean startsWithText()
	{
		return items.length > 0  &&  items[0] instanceof TextItem;
	}
	
	
	public boolean startsWithString(String s)
	{
		if ( items.length > 0  &&  items[0] instanceof TextItem )
		{
			TextItem i = (TextItem)items[0];
			return i.textValue.startsWith( s );
		}
		else
		{
			return false;
		}
	}
	
	public boolean endsWithString(String s)
	{
		if ( items.length > 0  &&  items[items.length-1] instanceof TextItem )
		{
			TextItem i = (TextItem)items[items.length-1];
			return i.textValue.endsWith( s );
		}
		else
		{
			return false;
		}
	}
	
	
	
	public List<Item> getItems()
	{
		return Arrays.asList( items );
	}
	
	public ArrayList<Object> getItemValues()
	{
		ArrayList<Object> itemValues = new ArrayList<Object>();
		itemValues.ensureCapacity( items.length );
		for (Item item: items)
		{
			if ( item instanceof TextItem )
			{
				itemValues.add( ((TextItem)item).textValue );
			}
			else if ( item instanceof StructuralItem )
			{
				itemValues.add( ((StructuralItem)item).structuralValue );
			}
			else
			{
				throw new RuntimeException( "Invalid item type" );
			}
		}
		return itemValues;
	}
	
	public boolean isTextual()
	{
		return items.length == 1 &&  items[0] instanceof TextItem;
	}
	
	public boolean isEmpty()
	{
		return length == 0;
	}
	
	public int length()
	{
		return length;
	}
	
	public int __len__()
	{
		return length;
	}
	
	public int numItems()
	{
		return items.length;
	}
	
	public boolean hasNoItems()
	{
		return items.length == 0;
	}
	
	public String textualValue()
	{
		if ( items.length == 1 &&  items[0] instanceof TextItem )
		{
			return ((TextItem)items[0]).textValue;
		}
		else
		{
			throw new RuntimeException( "Item stream is not textual" );
		}
	}
	
	public boolean contains(String sub)
	{
		for (Item item: items)
		{
			if ( item instanceof TextItem )
			{
				if ( ((TextItem)item).textValue.contains( sub ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean contains(Object sub)
	{
		for (Item item: items)
		{
			if ( item instanceof StructuralItem )
			{
				if ( sub == ((StructuralItem)item).structuralValue )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public int indexOf(String sub)
	{
		int n = 0;
		for (Item item: items)
		{
			if ( item instanceof TextItem )
			{
				int i = ((TextItem)item).textValue.indexOf( sub );
				if ( i != -1 )
				{
					return i + n;
				}
			}
			
			n += item.getLength();
		}
		return -1;
	}
	
	public int indexOf(Object sub)
	{
		int n = 0;
		for (Item item: items)
		{
			if ( item instanceof StructuralItem )
			{
				if ( sub == ((StructuralItem)item).structuralValue )
				{
					return n;
				}
			}
			
			n += item.getLength();
		}
		return -1;
	}
	
	public boolean __contains__(String sub)
	{
		return contains( sub );
	}
	
	public boolean __contains__(Object sub)
	{
		return contains( sub );
	}
	
	
	public Object __getitem__(int pos)
	{
		if ( pos >= length )
		{
			throw new IndexOutOfBoundsException( "StreamValue index out of range: pos=" + pos + "/" + length );
		}
		Item x = itemAt( pos );
		if ( x instanceof StructuralItem )
		{
			return ((StructuralItem)x).structuralValue;
		}
		else if ( x instanceof TextItem )
		{
			pos -= x.start;
			return ((TextItem)x).textValue.substring( pos, pos + 1 );
		}
		else
		{
			throw new RuntimeException();
		}
	}
	
	public StreamValue __getitem__(PySlice i)
	{
		int indices[] = i.indicesEx( length );
		int start = indices[0];
		int stop = indices[1];
		int step = indices[2];
		if ( step != 1 )
		{
			throw new RuntimeException( "StreamValue.__getItem__(PySlice) does not support slice step != 1" );
		}

		return subStream( start, stop );
	}
	
	
	
	public StreamValue subStream(int start, int stop)
	{
		if ( items.length == 0 )
		{
			return this;
		}
		else if ( start == stop )
		{
			return new StreamValue( new Item[] {} );
		}
		else
		{
			int startIndex = itemIndexAt( start );
			int stopIndex = itemIndexAt( stop );
			
			Item subItems[];
			
			if ( stop > items[stopIndex].start )
			{
				subItems = new Item[stopIndex+1-startIndex];
			}
			else
			{
				subItems = new Item[stopIndex-startIndex];
			}
			
			int pos = 0;
			subItems[0] = items[startIndex].subItemFrom( start, 0 );
			pos = subItems[0].stop;
			
			for (int i = startIndex + 1; i < stopIndex; i++)
			{
				Item subItem = items[i].copyAt( pos );
				subItems[i-startIndex] = subItem;
				pos = subItem.stop;
			}
			
			if ( stop > items[stopIndex].start )
			{
				subItems[stopIndex-startIndex] = items[stopIndex].subItemTo( stop, pos );
			}
			
			return new StreamValue( subItems );
		}
	}
	
	
	public StreamValueAccessor accessor()
	{
		return new StreamValueAccessor( this );
	}
	
	
	protected Item itemAt(int pos)
	{
		if ( items.length > 0 )
		{
			int index = itemIndexAt( pos );
			return items[index];
		}
		else
		{
			return null;
		}
	}

	protected int itemIndexAt(int pos)
	{
		return binarySearchItem( 0, items.length, pos );
	}

	protected int binarySearchItem(int lo, int hi, int pos)
	{
		while ( lo < ( hi - 1 ) )
		{
			int mid = ( lo + hi ) / 2;
			if ( pos < items[mid].start )
			{
				hi = mid;
			}
			else if ( pos >= items[mid].stop )
			{
				lo = mid;
			}
			else
			{
				return mid;
			}
		}
		
		return lo;
	}
	
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof StreamValue )
		{
			StreamValue sx = (StreamValue)x;
			
			if ( length == sx.length  &&  items.length == sx.items.length )
			{
				for (int i = 0; i < items.length; i++)
				{
					if ( !items[i].equals( sx.items[i] ) )
					{
						return false;
					}
				}
				
				return true;
			}
		}
		else if ( x instanceof String )
		{
			String sx = (String)x;
			
			if ( items.length == 1  &&  items[0] instanceof TextItem )
			{
				return ((TextItem)items[0]).textValue.equals( sx );
			}
			
			return false;
		}
		
		return false;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (Item i: items)
		{
			builder.append( i.toString() );
		}
		return builder.toString();
	}



	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres contents = new Paragraph( InnerFragment.map( items ) );
		
		return streamValueStyle.applyTo( new ObjectBox( "BritefuryJ.DocPresent.StreamValue.StreamValue", contents ) );
	}


	private static StyleSheet streamValueStyle = StyleSheet.instance.withAttr( GenericStyle.objectBorderPaint,new Color( 0.65f, 0.0f, 0.55f ) ).withAttr( GenericStyle.objectTitlePaint,new Color( 0.65f, 0.0f, 0.55f ) );
}
