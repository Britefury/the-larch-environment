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
	private static class Item
	{
		private StringBuilder textValue;
		private Object structuralValue;
		
		
		public Item(String textValue)
		{
			this.textValue = new StringBuilder();
			this.textValue.append( textValue );
			structuralValue = null;
		}

		public Item(Object structuralValue, String textValue)
		{
			this.textValue = new StringBuilder();
			this.textValue.append( textValue );
			this.structuralValue = structuralValue;
		}
		
		
		public boolean isTextual()
		{
			return textValue != null  &&  structuralValue == null;
		}
		
		public boolean isStructural()
		{
			return structuralValue != null;
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
		if ( items.size() == 0  ||  items.get( items.size() - 1 ).isStructural() )
		{
			items.add( new Item( text ) );
		}
		else
		{
			items.get( items.size() - 1 ).textValue.append( text );
		}
	}
	
	public void appendStructuralValue(Object structuralValue)
	{
		items.add( new Item( structuralValue, null ) );
	}
	
	
	public ItemStream stream()
	{
		ItemStream.Item streamItems[] = new ItemStream.Item[items.size()];
		
		int pos = 0;
		for (int i = 0; i < items.size(); i++)
		{
			Item item = items.get( i );
			
			if ( item.isStructural() )
			{
				streamItems[i] = new ItemStream.Item( item.structuralValue, pos, pos + 1 );
				pos += 1;
			}
			else
			{
				String text = item.textValue.toString();
				int end = pos + text.length();
				streamItems[i] = new ItemStream.Item( item.textValue.toString(), pos, end );
				pos = end;
			}
		}
		
		return new ItemStream( streamItems );		
	}
}
