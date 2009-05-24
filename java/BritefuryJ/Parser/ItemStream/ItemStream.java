//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.ItemStream;

import java.util.Arrays;
import java.util.List;

public class ItemStream
{
	public static class Item
	{
		protected String textValue;
		protected Object structuralValue;
		protected int start, stop;
		
		
		public Item(String textValue, int start, int stop)
		{
			this.textValue = textValue;
			this.start = start;
			this.stop = stop;
			structuralValue = null;
		}

		public Item(Object structuralValue, int start, int stop)
		{
			this.structuralValue = structuralValue;
			this.start = start;
			this.stop = stop;
			textValue = null;
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
		
		
		public boolean isTextual()
		{
			return textValue != null;
		}
		
		public boolean isStructural()
		{
			return textValue == null;
		}
		
		public Item subItemFrom(int start, int atPos)
		{
			if ( isTextual() )
			{
				int offset = start - this.start;
				return new Item( textValue.substring( offset ), atPos, atPos + textValue.length() - offset );
			}
			else
			{
				return new Item( structuralValue, atPos, atPos + 1 );
			}
		}
		
		public Item subItemTo(int end, int atPos)
		{
			if ( isTextual() )
			{
				end -= start;
				return new Item( textValue.substring( 0, end ), atPos, atPos + end );
			}
			else
			{
				return new Item( structuralValue, atPos, atPos + 1 );
			}
		}
		
		public Item subItem(int start, int end, int atPos)
		{
			if ( isTextual() )
			{
				int offset = start - this.start;
				end -= this.start;
				return new Item( textValue.substring( offset, end ), atPos, atPos + end - offset );
			}
			else
			{
				return new Item( structuralValue, atPos, atPos + 1 );
			}
		}
		
		public Item copyAt(int atPos)
		{
			if ( isTextual() )
			{
				return new Item( textValue, atPos, atPos + stop - start );
			}
			else
			{
				return new Item( structuralValue, atPos, atPos + 1 );
			}
		}
		
		
		public String toString()
		{
			if ( isTextual() )
			{
				return textValue;
			}
			else
			{
				return "<<--Structural: " + structuralValue.toString() + "-->>";
			}
		}
	}
	
	
	
	protected Item items[];
	protected int length;
	
	
	public ItemStream(String text)
	{
		items = new Item[] { new Item( text, 0, text.length() ) };
		length = text.length();
	}
	
	protected ItemStream(Item items[])
	{
		this.items = items;
		length = items.length > 0  ?  items[items.length-1].stop  :  0;
	}
	
	
	
	public List<Item> getItems()
	{
		return Arrays.asList( items );
	}
	
	public int length()
	{
		return length;
	}
	
	
	
	public ItemStream subStream(int start, int stop)
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
		
		return new ItemStream( subItems );
	}
	
	
	public ItemStreamAccessor accessor()
	{
		return new ItemStreamAccessor( this );
	}
	
	
	protected Item itemAt(int pos)
	{
		int index = itemIndexAt( pos );
		return items[index];
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
	
	
	

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (Item i: items)
		{
			builder.append( i.toString() );
		}
		return builder.toString();
	}
}
