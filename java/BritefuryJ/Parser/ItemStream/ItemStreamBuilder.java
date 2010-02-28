//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.ItemStream;

import java.util.ArrayList;
import java.util.Arrays;


public class ItemStreamBuilder
{
	public abstract static class Item
	{
		abstract public int length();
		
		abstract public ItemStream.Item streamItem(int start);
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

	
		public ItemStream.Item streamItem(int start)
		{
			return new ItemStream.TextItem( textValue.toString(), start, start + textValue.length() );
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

	
		public ItemStream.Item streamItem(int start)
		{
			return new ItemStream.StructuralItem( structuralValue, start, start + 1 );
		}
	}

	
	
	private ArrayList<Item> items;
	
	
	public ItemStreamBuilder()
	{
		items = new ArrayList<Item>();
	}
	
	public ItemStreamBuilder(String text)
	{
		items = new ArrayList<Item>();
		appendTextValue( text );
	}
	
	public ItemStreamBuilder(Item items[])
	{
		this.items = new ArrayList<Item>();

                this.items.addAll( Arrays.asList( items ) );
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
	
	public void appendItemStreamItem(ItemStream.Item item)
	{
		if ( item instanceof ItemStream.TextItem )
		{
			appendTextValue( ((ItemStream.TextItem)item).textValue );
		}
		else if ( item instanceof ItemStream.StructuralItem )
		{
			appendStructuralValue( ((ItemStream.StructuralItem)item).structuralValue );
		}
	}
	
	public void extend(ItemStream items)
	{
		for (ItemStream.Item item: items.getItems())
		{
			if ( item instanceof ItemStream.TextItem )
			{
				appendTextValue( ((ItemStream.TextItem)item).textValue );
			}
			else if ( item instanceof ItemStream.StructuralItem )
			{
				appendStructuralValue( ((ItemStream.StructuralItem)item).structuralValue );
			}
		}
	}
	
	
	public ItemStream stream()
	{
		ItemStream.Item streamItems[] = new ItemStream.Item[items.size()];
		
		int pos = 0;
		int i = 0;
		for (Item item: items)
		{
			streamItems[i] = item.streamItem( pos );
			pos += item.length();
			i++;
		}
		
		return new ItemStream( streamItems );		
	}
}
