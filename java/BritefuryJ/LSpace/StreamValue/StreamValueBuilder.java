//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.StreamValue;

import java.util.ArrayList;
import java.util.Arrays;


public class StreamValueBuilder
{
	public abstract static class Item
	{
		abstract public int length();
		
		abstract public StreamValue.Item streamItem(int start);
	}

	
	
	public static class TextItem extends Item
	{
		private StringBuilder textValue;
		
		
		public TextItem(String textValue)
		{
			this.textValue = new StringBuilder();
			this.textValue.append( textValue );
		}
		
		
		public int length()
		{
			return textValue.length();
		}

	
		public StreamValue.Item streamItem(int start)
		{
			return new StreamValue.TextItem( textValue.toString(), start, start + textValue.length() );
		}
	}

	
	
	public static class StructuralItem extends Item
	{
		private Object structuralValue;
		
		
		public StructuralItem(Object structuralValue)
		{
			this.structuralValue = structuralValue;
		}
		
		
		public int length()
		{
			return 1;
		}

	
		public StreamValue.Item streamItem(int start)
		{
			return new StreamValue.StructuralItem( structuralValue, start, start + 1 );
		}
	}

	
	
	private ArrayList<Item> items;
	
	
	public StreamValueBuilder()
	{
		items = new ArrayList<Item>();
	}
	
	public StreamValueBuilder(String text)
	{
		items = new ArrayList<Item>();
		appendTextValue( text );
	}
	
	public StreamValueBuilder(Item items[])
	{
		this.items = new ArrayList<Item>();

                this.items.addAll( Arrays.asList( items ) );
	}
	
	public StreamValueBuilder(Iterable<? extends Object> values)
	{
		items = new ArrayList<Item>();
		extend( values );
	}
	
	
	public void appendTextValue(String text)
	{
		Item last = items.size() == 0  ?  null  :  items.get( items.size() - 1 );
		if ( last != null  &&  last instanceof TextItem )
		{
			TextItem t = (TextItem)last;
			t.textValue.append( text );
		}
		else
		{
			items.add( new TextItem( text ) );
		}
	}
	
	public void appendStructuralValue(Object structuralValue)
	{
		items.add( new StructuralItem( structuralValue ) );
	}
	
	public void appendStreamValueItem(StreamValue.Item item)
	{
		if ( item instanceof StreamValue.TextItem )
		{
			appendTextValue( ((StreamValue.TextItem)item).textValue );
		}
		else if ( item instanceof StreamValue.StructuralItem )
		{
			appendStructuralValue( ((StreamValue.StructuralItem)item).structuralValue );
		}
	}
	
	public void append(Object value)
	{
		if ( value instanceof String )
		{
			appendTextValue( (String)value );
		}
		else if ( value instanceof StreamValue )
		{
			extend( (StreamValue)value );
		}
		else if ( value instanceof StreamValue.Item )
		{
			appendStreamValueItem( (StreamValue.Item)value );
		}
		else
		{
			appendStructuralValue( value );
		}
	}
	
	public void extend(StreamValue items)
	{
		for (StreamValue.Item item: items.getItems())
		{
			if ( item instanceof StreamValue.TextItem )
			{
				appendTextValue( ((StreamValue.TextItem)item).textValue );
			}
			else if ( item instanceof StreamValue.StructuralItem )
			{
				appendStructuralValue( ((StreamValue.StructuralItem)item).structuralValue );
			}
		}
	}
	
	public void extend(Iterable<? extends Object> values)
	{
		for (Object value: values)
		{
			append( value );
		}
	}
	
	
	public StreamValue stream()
	{
		StreamValue.Item streamItems[] = new StreamValue.Item[items.size()];
		
		int pos = 0;
		int i = 0;
		for (Item item: items)
		{
			streamItems[i] = item.streamItem( pos );
			pos += item.length();
			i++;
		}
		
		return new StreamValue( streamItems );		
	}
}
