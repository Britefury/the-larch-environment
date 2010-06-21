//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Logging;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.python.core.Py;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;

public class LogEntry implements Presentable
{
	public static class NameInUseException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public NameInUseException(String message)
		{
			super( message );
		}
	};
	
	
	public enum Layout
	{
		HORIZONTAL,
		VERTICAL
	};
	
	
	public static class Item
	{
		private String name;
		private Object value;
		private Layout layout;
		
		public Item(String name, Object value, Layout layout)
		{
			this.name = name;
			this.value = value;
			this.layout = layout;
		}
		
		
		public String getName()
		{
			return name;
		}
		
		public Object getValue()
		{
			return value;
		}
		
		public Layout getLayout()
		{
			return layout;
		}
	}
	
	
	private static final HashSet<String> emptyTags = new HashSet<String>();
	
	
	private HashSet<String> tags;
	private ArrayList<Item> items = new ArrayList<Item>();
	private HashMap<String, Item> itemTable = new HashMap<String, Item>();
	private String entryClass;
	
	
	
	public LogEntry(String entryClass)
	{
		this.entryClass = entryClass;
		tags = emptyTags;
	}
	
	
	public LogEntry tag(String tag)
	{
		if ( tags == emptyTags )
		{
			tags = new HashSet<String>();
		}
		tags.add( tag );
		return this;
	}
	
	public LogEntry item(String name, Object value, Layout layout)
	{
		if ( itemTable.containsKey( name ) )
		{
			throw new NameInUseException( "Item name '" + name + "' already in use" );
		}
		Item item = new Item( name, value, layout );
		items.add( item );
		itemTable.put( name, item );
		return this;
	}
	
	public LogEntry hItem(String name, Object value)
	{
		return item( name, value, Layout.HORIZONTAL );
	}
	
	public LogEntry vItem(String name, Object value)
	{
		return item( name, value, Layout.VERTICAL );
	}
	
	
	
	public String getEntryClass()
	{
		return entryClass;
	}
	
	
	
	public HashSet<String> getTags()
	{
		return tags;
	}
	
	
	
	public ArrayList<Item> getItems()
	{
		return items;
	}
	
	public int size()
	{
		return items.size();
	}
	
	public Item get(int index)
	{
		return items.get( index );
	}
	
	public Object get(String name)
	{
		Item item = itemTable.get( name );
		return item != null  ?  item.getValue()  :  null;
	}
	
	
	public Item __getitem__(int index)
	{
		return items.get( index );
	}
	
	public Object __getitem__(String name)
	{
		Item item = itemTable.get( name );
		if ( item == null )
		{
			throw Py.KeyError( "No log entry item named '" + name + "'" );
		}
		return item.getValue();
	}
	
	
	public boolean __contains__(String name)
	{
		return itemTable.containsKey( name ); 
	}
	


	public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		DPElement fields[] = new DPElement[items.size()];
		for (int i = 0; i < items.size(); i++)
		{
			Item item = items.get( i );
			DPElement valueView = fragment.presentFragment( item.getValue(), styleSheet, inheritedState );
			if ( item.getLayout() == Layout.HORIZONTAL )
			{
				fields[i] = logEntryStyle.horizontalObjectField( item.getName(), valueView );
			}
			else if ( item.getLayout() == Layout.VERTICAL )
			{
				fields[i] = logEntryStyle.verticalObjectField( item.getName(), valueView );
			}
			else
			{
				throw new RuntimeException( "Invalid layout" );
			}
		}
		
		return logEntryStyle.objectBoxWithFields( "Log entry - " + entryClass, fields );
	}


	private static GenericPerspectiveStyleSheet logEntryStyle = GenericPerspectiveStyleSheet.instance.withObjectBorderPaint( new Color( 0.45f, 0.65f, 0.0f ) ).withObjectTitlePaint(
			new Color( 0.45f, 0.65f, 0.0f ) );
}
