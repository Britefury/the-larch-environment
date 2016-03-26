//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.RichString;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.python.core.PySlice;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.ClipboardFilter.ClipboardCopyable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.ObjectPres.ObjectPresStyle;
import BritefuryJ.Pres.ObjectPres.UnescapedStringAsSpan;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.StyleSheet.StyleSheet;

public class RichString implements Presentable, ClipboardCopyable
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
			String text = textValue.substring( offset );
			return new TextItem( text, atPos, atPos + text.length() );
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
			String text = textValue.substring( offset, end );
			return new TextItem( text, atPos, atPos + text.length() );
		}
		
		public Item copyAt(int atPos)
		{
			return new TextItem( textValue, atPos, atPos + stop - start );
		}
		
		
		public String getValue()
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
		
		
		public Object getValue()
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
			return itemBorder.surround( new InnerFragment( structuralValue ) );
		}


		private static AbstractBorder itemBorder = new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.15f, 0.25f, 0.75f ), null );
	}
	
	

	protected Item items[];
	protected int length;
	
	
	public RichString(String text)
	{
		items = new Item[] { new TextItem( text, 0, text.length() ) };
		length = text.length();
	}
	
	protected RichString(Item items[])
	{
		this.items = items;
		length = items.length > 0  ?  items[items.length-1].stop  :  0;
	}
	
	
	
	public boolean startsWithText()
	{
		return items.length > 0  &&  items[0] instanceof TextItem;
	}
	
	
	public boolean startsWith(String s)
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
	
	public boolean startsWith(Object x)
	{
		if ( items.length > 0  &&  items[0] instanceof StructuralItem )
		{
			StructuralItem i = (StructuralItem)items[0];
			return x.equals( i.structuralValue );
		}
		else
		{
			return false;
		}
	}
	
	public boolean endsWith(String s)
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
	
	public boolean endsWith(Object x)
	{
		if ( items.length > 0  &&  items[items.length-1] instanceof StructuralItem )
		{
			StructuralItem i = (StructuralItem)items[items.length-1];
			return x.equals( i.structuralValue );
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
			throw new RuntimeException( "Item sequence is not textual" );
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
	
	
	public int indexOf(String sub, int start, int end)
	{
		int s = start == -1  ?  0  :  itemIndexAt( start );
		int e = end == -1  ?  items.length - 1  :  itemIndexAt( end );
		int n = start == -1  ?  0  :  items[s].start;
		for (int i = s; i <= e; i++)
		{
			Item item = items[i];
			if ( item instanceof TextItem )
			{
				int j;
				// Check if we are in the item that contains @start
				if ( i == s )
				{
					// We must ensure that we only look for occurrences after @start - not for occurrences anywhere in the item
					j = ((TextItem)item).textValue.indexOf( sub, start - item.start );
				}
				else
				{
					j = ((TextItem)item).textValue.indexOf( sub );
				}
				
				if ( j != -1 )
				{
					// Found something
					// Ensure that we have not yet reached @end
					if ( end == -1  ||  (j+n+sub.length()) <= end )
					{
						return j + n;
					}
				}
			}
			
			n += item.getLength();
		}
		return -1;
	}
	
	public int indexOf(String sub, int start)
	{
		return indexOf( sub, start, -1 );
	}
	
	public int indexOf(String sub)
	{
		return indexOf( sub, -1, -1 );
	}
	
	public int indexOf(Object sub, int start, int end)
	{
		int s = start == -1  ?  0  :  itemIndexAt( start );
		int e = end == -1  ?  items.length - 1  :  itemIndexAt( end );
		int n = start == -1  ?  0  :  items[s].start;
		for (int i = s; i <= e; i++)
		{
			Item item = items[i];
			if ( item instanceof StructuralItem )
			{
				if ( sub.equals( ((StructuralItem)item).structuralValue ) )
				{
					// Found something
					// Ensure that we have not yet reached @end
					if ( end == -1  ||  (n+1) <= end )
					{
						return n;
					}
				}
			}
			
			n += item.getLength();
		}
		return -1;
	}
	
	public int indexOf(Object sub, int start)
	{
		return indexOf( sub, start, -1 );
	}
	
	public int indexOf(Object sub)
	{
		return indexOf( sub, -1, -1 );
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
			throw new IndexOutOfBoundsException( "RichString index out of range: pos=" + pos + "/" + length );
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
	
	public RichString __getitem__(PySlice i)
	{
		int indices[] = i.indicesEx( length );
		int start = indices[0];
		int stop = indices[1];
		int step = indices[2];
		if ( step != 1 )
		{
			throw new RuntimeException( "RichString.__getItem__(PySlice) does not support slice step != 1" );
		}

		return substring( start, stop );
	}
	
	
	
	public RichString substring(int start, int stop)
	{
		if ( items.length == 0 )
		{
			return this;
		}
		else if ( start == stop )
		{
			return new RichString( new Item[] {} );
		}
		else
		{
			int startIndex = itemIndexAt( start );
			int stopIndex = itemIndexAt( stop );
			
			Item subItems[];
			
			if ( startIndex == stopIndex )
			{
				subItems = new Item[] { items[startIndex].subItem( start, stop, 0 ) };
			}
			else
			{
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
			}
			
			return new RichString( subItems );
		}
	}
	
	
	public RichString[] split(String sub)
	{
		ArrayList<RichString> richStrings = new ArrayList<RichString>();
		
		int subLength = sub.length();
		int pos = 0;
		int index = indexOf( sub );
		
		while ( index != -1 )
		{
			richStrings.add( substring( pos, index ) );
			pos = index + subLength;
			index = indexOf( sub, pos );
		}
		
		richStrings.add( substring( pos, length ) );
		
		return richStrings.toArray( new RichString[richStrings.size()] );
	}
	
	
	public RichString[] split(Object sub)
	{
		ArrayList<RichString> richStrings = new ArrayList<RichString>();
		
		int pos = 0;
		int index = indexOf( sub );
		
		while ( index != -1 )
		{
			richStrings.add( substring( pos, index ) );
			pos = index + 1;
			index = indexOf( sub, pos );
		}
		
		richStrings.add( substring( pos, length ) );
		
		return richStrings.toArray( new RichString[richStrings.size()] );
	}
	
	
	public RichStringAccessor accessor()
	{
		return new RichStringAccessor( this );
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
	
	
	
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		RichStringBuilder builder = new RichStringBuilder();
		for (RichString.Item item: getItems())
		{
			if ( item instanceof RichString.StructuralItem )
			{
				RichString.StructuralItem structuralItem = (RichString.StructuralItem)item;
				builder.appendStructuralValue( memo.copy( structuralItem.getValue() ) );
			}
			else if ( item instanceof RichString.TextItem )
			{
				RichString.TextItem textItem = (RichString.TextItem)item;
				builder.appendTextValue( textItem.getValue() );
			}
		}
		return builder.richString();
	}
	
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof RichString )
		{
			RichString sx = (RichString)x;
			
			if ( length == sx.length  &&  items.length == sx.items.length )
			{
				return Arrays.equals( items, sx.items );
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
		
		return richStringStyle.applyTo( new ObjectBox( "BritefuryJ.Util.RichString.RichString", contents ) );
	}


	private static StyleSheet richStringStyle = StyleSheet.style( ObjectPresStyle.objectBorderPaint.as( new Color( 0.65f, 0.0f, 0.55f ) ), ObjectPresStyle.objectTitlePaint.as( new Color( 0.65f, 0.0f, 0.55f ) ) );
}
