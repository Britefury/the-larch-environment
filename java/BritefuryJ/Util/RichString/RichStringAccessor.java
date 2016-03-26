//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.RichString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichStringAccessor
{
	private RichString richString;
	private RichString.Item currentItem;
	
	
	protected RichStringAccessor(RichString richString)
	{
		this.richString = richString;
		
		currentItem = richString.items.length == 1  ?  richString.items[0]  :  null;
	}
	
	
	public RichString getRichString()
	{
		return richString;
	}
	
	
	public int length()
	{
		return richString.length();
	}
	
	
	public int consumeString(int start, String x)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof RichString.TextItem )
		{
			if ( start + x.length() <= currentItem.stop )
			{
				int offset = start - currentItem.start;
				RichString.TextItem t = (RichString.TextItem)currentItem;
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
		if ( currentItem instanceof RichString.TextItem )
		{
			RichString.TextItem t = (RichString.TextItem)currentItem;
			int offset = start - t.start;
			Matcher m = pattern.matcher( t.textValue.substring( offset, t.stop - t.start ) );
			
			boolean bFound = m.lookingAt();
			if ( bFound  &&  m.end() > 0 )
			{
				return start + m.group().length();
			}
		}
		
		return -1;
	}
	
	public int skipRegEx(int start,  Pattern pattern)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof RichString.TextItem )
		{
			RichString.TextItem t = (RichString.TextItem)currentItem;
			if ( start < t.stop )
			{
				int offset = start - t.start;
				Matcher m = pattern.matcher( t.textValue.substring( offset, t.stop - t.start ) );
				
				boolean bFound = m.lookingAt();
				if ( bFound  &&  m.end() > 0 )
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
		if ( currentItem instanceof RichString.TextItem )
		{
			RichString.TextItem t = (RichString.TextItem)currentItem;
			int offset = start - t.start;
			Matcher m = pattern.matcher( t.textValue.substring( offset, t.stop - t.start ) );
			
			boolean bFound = m.lookingAt();
			if ( bFound  &&  m.end() > 0 )
			{
				return m.group();
			}
		}
		
		return null;
	}
	
	public boolean matchesRegEx(int start, int stop, Pattern pattern)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof RichString.TextItem )
		{
			RichString.TextItem t = (RichString.TextItem)currentItem;
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
		if ( currentItem instanceof RichString.StructuralItem )
		{
			RichString.StructuralItem s = (RichString.StructuralItem)currentItem;
			return new Object[] { s.structuralValue };
		}
		
		return null;
	}
	
	
	
	public boolean canMatchTextAt(int pos)
	{
		updateCurrentItem( pos );
		return currentItem instanceof RichString.TextItem;
	}
	
	public boolean canMatchStructuralNodeAt(int pos)
	{
		updateCurrentItem( pos );
		return currentItem instanceof RichString.StructuralItem;
	}
	
	public boolean isAtEnd(int pos)
	{
		return pos == richString.length;
	}
	

	
	public CharSequence getItemTextFrom(int start)
	{
		updateCurrentItem( start );
		if ( currentItem instanceof RichString.TextItem )
		{
			RichString.TextItem t = (RichString.TextItem)currentItem;
			return t.textValue.subSequence( start - currentItem.start, currentItem.stop - currentItem.start );
		}
		
		return null;
	}
	
	
	
	public RichStringAccessor substring(int start, int stop)
	{
		return richString.substring( start, stop ).accessor();
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
		currentItem = richString.itemAt( pos );
		
		// TODO: remove this:
		if ( currentItem != null  &&  ( pos < currentItem.start  ||  pos > currentItem.stop ) )
		{
			throw new RuntimeException( "outside range: pos=" + pos + ", range=" + currentItem.start + ":" + currentItem.stop );
		}
	}


	public String toString()
	{
		return richString.toString();
	}
}
