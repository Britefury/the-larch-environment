//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.ItemStream;

import java.util.ArrayList;


public class ItemStreamBuilder
{
	private abstract static class Item
	{
		abstract public int length();
		
		abstract public ItemStream.Item streamItem(int start);
	}

	
	
	private static class TextItem extends Item
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

	
	
	private static class StructuralItem extends Item
	{
		private Object structuralValue;
		
		
		public StructuralItem(Object structuralValue, String textValue)
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
	
	public ItemStreamBuilder(Object values[])
	{
		this();
		
		for (Object v: values)
		{
			if ( v instanceof String )
			{
				appendTextValue( (String)v );
			}
			else
			{
				appendStructuralValue( v );
			}
		}
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
		items.add( new StructuralItem( structuralValue, null ) );
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
