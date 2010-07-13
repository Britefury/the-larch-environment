//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StreamValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamValueAccessor
{
	private StreamValue stream;
	private StreamValue.Item currentItem;
	
	
	protected StreamValueAccessor(StreamValue stream)
	{
		this.stream = stream;
		
		currentItem = stream.items.length == 1  ?  stream.items[0]  :  null;
	}
	
	
	public StreamValue getStream()
	{
		return stream;
	}
	
	
	public int length()
	{
		return stream.length();
	}
	
	
	public int consumeString(int start, String x)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof StreamValue.TextItem )
		{
			if ( start + x.length() <= currentItem.stop )
			{
				int offset = start - currentItem.start;
				StreamValue.TextItem t = (StreamValue.TextItem)currentItem;
				if ( t.textValue.substring( offset, offset + x.length() ).equals( x ) )
				{
					return start + x.length();
				}
			}
		}
		
		return -1;
	}
	
	public int consumeRegEx(int start, Pattern pattern)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof StreamValue.TextItem )
		{
			StreamValue.TextItem t = (StreamValue.TextItem)currentItem;
			int offset = start - t.start;
			Matcher m = pattern.matcher( t.textValue.substring( offset, t.stop - t.start ) );
			
			boolean bFound = m.find();
			if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
			{
				return start + m.group().length();
			}
		}
		
		return -1;
	}
	
	public int skipRegEx(int start,  Pattern pattern)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof StreamValue.TextItem )
		{
			StreamValue.TextItem t = (StreamValue.TextItem)currentItem;
			if ( start < t.stop )
			{
				int offset = start - t.start;
				Matcher m = pattern.matcher( t.textValue.substring( offset, t.stop - t.start ) );
				
				boolean bFound = m.find();
				if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
				{
					return start + m.group().length();
				}
			}
		}
		
		return start;
	}


	public String matchRegEx(int start, Pattern pattern)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof StreamValue.TextItem )
		{
			StreamValue.TextItem t = (StreamValue.TextItem)currentItem;
			int offset = start - t.start;
			Matcher m = pattern.matcher( t.textValue.substring( offset, t.stop - t.start ) );
			
			boolean bFound = m.find();
			if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
			{
				return m.group();
			}
		}
		
		return null;
	}
	
	public boolean matchesRegEx(int start, int stop, Pattern pattern)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof StreamValue.TextItem )
		{
			StreamValue.TextItem t = (StreamValue.TextItem)currentItem;
			int offset = start - t.start;
			stop = Math.min( stop, t.stop );
			Matcher m = pattern.matcher( t.textValue.substring( offset, stop - t.start ) );
			return m.matches();
		}
		
		return false;
	}
	
	
	
	//
	// matchStructuralNode():
	//
	// On match success:
	//	returns value as a 1 element array
	// On match failure:
	//	returns null
	//
	// This allows a structural node with a null value to be returned (a 1 element array containing a null)
	//
	public Object[] matchStructuralNode(int start)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof StreamValue.StructuralItem )
		{
			StreamValue.StructuralItem s = (StreamValue.StructuralItem)currentItem;
			return new Object[] { s.structuralValue };
		}
		
		return null;
	}
	
	
	
	public boolean canMatchTextAt(int pos)
	{
		updateCurrentItem( pos );
		return currentItem instanceof StreamValue.TextItem;
	}
	
	public boolean canMatchStructuralNodeAt(int pos)
	{
		updateCurrentItem( pos );
		return currentItem instanceof StreamValue.StructuralItem;
	}
	
	public boolean isAtEnd(int pos)
	{
		return pos == stream.length;
	}
	

	
	public CharSequence getItemTextFrom(int start)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof StreamValue.TextItem )
		{
			StreamValue.TextItem t = (StreamValue.TextItem)currentItem;
			return t.textValue.subSequence( start - currentItem.start, currentItem.stop - currentItem.start );
		}
		
		return null;
	}
	
	
	
	public StreamValueAccessor subStream(int start, int stop)
	{
		return stream.subStream( start, stop ).accessor();
	}
	
	

	private void updateCurrentItem(int pos)
	{
		if ( currentItem != null )
		{
			if ( pos >= currentItem.start  &&  pos < currentItem.stop )
			{
				// Within range of the current item; nothing to do
				return;
			}
		}
		currentItem = stream.itemAt( pos );
		
		// TODO: remove this:
		if ( currentItem != null  &&  ( pos < currentItem.start  ||  pos > currentItem.stop ) )
		{
			throw new RuntimeException( "outside range: pos=" + pos + ", range=" + currentItem.start + ":" + currentItem.stop );
		}
	}


	public String toString()
	{
		return stream.toString();
	}
}
