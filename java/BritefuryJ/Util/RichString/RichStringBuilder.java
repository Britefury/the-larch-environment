//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.RichString;

import java.util.ArrayList;
import java.util.Arrays;


public class RichStringBuilder
{
	public abstract static class Item
	{
		abstract public int length();
		
		abstract public RichString.Item richStringItem(int start);
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

	
		public RichString.Item richStringItem(int start)
		{
			return new RichString.TextItem( textValue.toString(), start, start + textValue.length() );
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

	
		public RichString.Item richStringItem(int start)
		{
			return new RichString.StructuralItem( structuralValue, start, start + 1 );
		}
	}

	
	
	private ArrayList<Item> items;
	
	
	public RichStringBuilder()
	{
		items = new ArrayList<Item>();
	}
	
	public RichStringBuilder(String text)
	{
		items = new ArrayList<Item>();
		appendTextValue( text );
	}
	
	public RichStringBuilder(Item items[])
	{
		this.items = new ArrayList<Item>();

                this.items.addAll( Arrays.asList( items ) );
	}
	
	public RichStringBuilder(Iterable<? extends Object> values)
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
	
	public void appendRichStringItem(RichString.Item item)
	{
		if ( item instanceof RichString.TextItem )
		{
			appendTextValue( ((RichString.TextItem)item).textValue );
		}
		else if ( item instanceof RichString.StructuralItem )
		{
			appendStructuralValue( ((RichString.StructuralItem)item).structuralValue );
		}
	}
	
	public void append(Object value)
	{
		if ( value instanceof String )
		{
			appendTextValue( (String)value );
		}
		else if ( value instanceof RichString )
		{
			extend( (RichString)value );
		}
		else if ( value instanceof RichString.Item )
		{
			appendRichStringItem( (RichString.Item)value );
		}
		else
		{
			appendStructuralValue( value );
		}
	}
	
	public void extend(RichString items)
	{
		for (RichString.Item item: items.getItems())
		{
			if ( item instanceof RichString.TextItem )
			{
				appendTextValue( ((RichString.TextItem)item).textValue );
			}
			else if ( item instanceof RichString.StructuralItem )
			{
				appendStructuralValue( ((RichString.StructuralItem)item).structuralValue );
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
	
	
	public RichString richString()
	{
		RichString.Item richStringItems[] = new RichString.Item[items.size()];
		
		int pos = 0;
		int i = 0;
		for (Item item: items)
		{
			richStringItems[i] = item.richStringItem( pos );
			pos += item.length();
			i++;
		}
		
		return new RichString( richStringItems );		
	}
}
