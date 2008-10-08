//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.python.core.PySlice;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.CommandTrackerFactory;
import BritefuryJ.CommandHistory.Trackable;
import BritefuryJ.JythonInterface.JythonSlice;

public class DMList implements DMListInterface, Trackable
{
	public static class ListView implements List<Object>
	{
		private DMList src;
		private int start, stop;
		
		
		private ListView(DMList src, int start, int stop)
		{
			this.src = src;
			this.start = start;
			this.stop = stop;
		}

	
	
	
		public boolean add(Object x)
		{
			src.add( stop, x );
			return true;
		}
		
		public void add(int index, Object x)
		{
			src.add( start + index, x );
		}
		
		public boolean addAll(Collection<? extends Object> xs)
		{
			return src.addAll( stop, xs );
		}
		
		public boolean addAll(int index, Collection<? extends Object> xs)
		{
			return src.addAll( start + index, xs );
		}
		

		
		public void clear()
		{
			src.removeRange( start, stop - start );
		}
		
		
		public boolean contains(Object x)
		{
			return src.getInternalContainer().subList( start, stop ).contains( x );
		}
		

		public boolean containsAll(Collection<?> x)
		{
			return src.getInternalContainer().subList( start, stop ).containsAll( x );
		}
		
		
		public boolean equals(Object xs)
		{
			return src.getInternalContainer().subList( start, stop ).equals( xs );
		}
		
		
		public Object get(int index)
		{
			return src.get( start + index );
		}
		
		
		public int indexOf(Object x)
		{
			int index = src.indexOf( x );
			if ( index < start  ||  index >= stop  ||  index == -1 )
			{
				return -1;
			}
			else
			{
				return index - start;
			}
		}

		
		public boolean isEmpty()
		{
			return src.isEmpty()  ||  start >= stop;
		}
		
		
		public Iterator<Object> iterator()
		{
			return src.getInternalContainer().subList( start, stop ).iterator();
		}
		
		
		public int lastIndexOf(Object x)
		{
			int index = src.lastIndexOf( x );
			if ( index < start  ||  index >= stop  ||  index == -1 )
			{
				return -1;
			}
			else
			{
				return index - start;
			}
		}

		
		public ListIterator<Object> listIterator()
		{
			return src.getInternalContainer().subList( start, stop ).listIterator();
		}
		
		public ListIterator<Object> listIterator(int i)
		{
			return src.getInternalContainer().subList( start, stop ).listIterator( i );
		}
		
		public Object remove(int i)
		{
			return src.remove( i - start );
		}
		
		public boolean remove(Object x)
		{
			int index = indexOf( x );
			if ( index == -1 )
			{
				return false;
			}
			else
			{
				src.remove( start + index );
				return true;
			}
		}
		
		public boolean removeAll(Collection<?> x)
		{
			throw new UnsupportedOperationException();
		}
		
		public boolean retainAll(Collection<?> x)
		{
			throw new UnsupportedOperationException();
		}
		
		public Object set(int index, Object x)
		{
			return src.set( start + index, x );
		}
		
		public int size()
		{
			return stop - start;
		}
		
		
		
		public List<Object> subList(int fromIndex, int toIndex)
		{
			return new ListView( src, start + fromIndex, start + toIndex );
		}

		public Object[] toArray()
		{
			return src.getInternalContainer().subList( start, stop ).toArray();
		}

		public <T> T[] toArray(T[] a)
		{
			return src.getInternalContainer().subList( start, stop ).toArray( a );
		}
	}
	
	
	
	
	private LiteralCell cell;
	private DMListCommandTracker commandTracker;
	
	
	public DMList()
	{
		this( null );
	}
	
	public DMList(List<Object> xs)
	{
		cell = new LiteralCell();
		Vector<Object> value = new Vector<Object>();

		if ( xs != null )
		{
			value.setSize(  xs.size() );
			for (int i = 0; i < xs.size(); i++)
			{
				value.set( i, coerce( xs.get( i ) ) );
			}
		}
		cell.setLiteralValue( value );
		
		commandTracker = null;
	}
	
	
	public Object coerce(String x)
	{
		return x;
	}
	
	public Object coerce(List<Object> x)
	{
		return new DMList( x );
	}
	
	@SuppressWarnings("unchecked")
	public Object coerce(Object x)
	{
		if ( x instanceof String )
		{
			return coerce( (String)x );
		}
		else if ( x instanceof List )
		{
			return coerce( (List<Object>)x );
		}
		else
		{
			return x;
		}
	}
	
	
	
	public Object clone()
	{
		return new DMList( this );
	}
	
	
	
	@SuppressWarnings("unchecked")
	public boolean add(Object x)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		x = coerce( x );
		boolean bResult = v.add( x );
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onAdd( this, x );
		}
		return bResult;
	}
	
	@SuppressWarnings("unchecked")
	public void add(int index, Object x)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		x = coerce( x );
		v.add( index, x );
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onInsert( this, index, x );
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean addAll(Collection<? extends Object> xs)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		
		Vector<Object> cxs = new Vector<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			cxs.add( coerce( x ) );
		}
		
		v.addAll( cxs );
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onAddAll( this, cxs );
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addAll(int index, Collection<? extends Object> xs)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		
		Vector<Object> cxs = new Vector<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			cxs.add( coerce( x ) );
		}
		
		v.addAll( index, cxs );
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onInsertAll( this, index, cxs );
		}
		return true;
	}
	

	
	@SuppressWarnings("unchecked")
	public void clear()
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		Vector<Object> copy = (Vector<Object>)v.clone();
		v.clear();
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onClear( this, copy );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean contains(Object x)
	{
		return ((Vector<Object>)cell.getValue()).contains( x );
	}
	

	@SuppressWarnings("unchecked")
	public boolean containsAll(Collection<?> x)
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		for (Object a: x)
		{
			if ( !v.contains( a ) )
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object xs)
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.equals( xs );
	}
	
	
	@SuppressWarnings("unchecked")
	public Object get(int index)
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.get( index );
	}
	
	
	@SuppressWarnings("unchecked")
	public int indexOf(Object x)
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.indexOf(  x );
	}

	
	@SuppressWarnings("unchecked")
	public boolean isEmpty()
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.isEmpty();
	}
	
	
	@SuppressWarnings("unchecked")
	public Iterator<Object> iterator()
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.iterator();
	}
	
	
	@SuppressWarnings("unchecked")
	public int lastIndexOf(Object x)
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.lastIndexOf(  x );
	}

	
	@SuppressWarnings("unchecked")
	public ListIterator<Object> listIterator()
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.listIterator();
	}
	
	@SuppressWarnings("unchecked")
	public ListIterator<Object> listIterator(int i)
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.listIterator( i );
	}
	
	@SuppressWarnings("unchecked")
	public Object remove(int i)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		Object x = v.remove( i );
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onRemove( this, i, x );
		}
		return x;
	}
	
	@SuppressWarnings("unchecked")
	public boolean remove(Object x)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		int i = v.indexOf( x );
		if ( i != -1 )
		{
			v.remove( i );
			cell.setLiteralValue( v );
			if ( commandTracker != null )
			{
				commandTracker.onRemove( this, i, x );
			}
		}
		return i != -1;
	}
	
	public boolean removeAll(Collection<?> x)
	{
		throw new UnsupportedOperationException();
	}
	
	public boolean retainAll(Collection<?> x)
	{
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unchecked")
	public Object set(int index, Object x)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		x = coerce( x );
		Object oldX = v.set( index, x );
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onSet( this, index, oldX, x );
		}
		return oldX;
	}
	
	@SuppressWarnings("unchecked")
	public int size()
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.size();
	}
	
	
	
	public List<Object> subList(int fromIndex, int toIndex)
	{
		return new ListView( this, fromIndex, toIndex );
	}

	@SuppressWarnings("unchecked")
	public Object[] toArray()
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.toArray();
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a)
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return v.toArray( a );
	}

	
	
	
	
	
	//
	//
	// Jython methods
	//
	//
	
	public void append(Object x)
	{
		add( x );
	}
	
	public void extend(List<Object> xs)
	{
		addAll( xs );
	}
	
	public void insert(int i, Object x)
	{
		add( i, x );
	}
	
	public Object __getitem__(int i)
	{
		return get( i );
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> __getitem__(PySlice i)
	{
		Vector<Object> v = (Vector<Object>)cell.getValue();
		return Arrays.asList( JythonSlice.arrayGetSlice( v.toArray(), i ) );
	}
	
	public void __setitem__(int i, Object x)
	{
		set( i, x );
	}
	
	@SuppressWarnings("unchecked")
	public void __setitem__(PySlice i, List<Object> xs)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		
		Vector<Object> oldContents = (Vector<Object>)v.clone();
		
		Vector<Object> cxs = new Vector<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			cxs.add( coerce( x ) );
		}
		
		Object[] src = cxs.toArray();
		Object[] dest = v.toArray();
		
		Object[] result = JythonSlice.arraySetSlice( dest, i, src );

		v.clear();
		v.addAll( Arrays.asList( result ) );
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onSetContents( this, oldContents, result );
		}
	}
	
	public void __delitem__(int i)
	{
		remove( i );
	}
	
	@SuppressWarnings("unchecked")
	public void __delitem__(PySlice i)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		Vector<Object> oldContents = (Vector<Object>)v.clone();
		
		Object[] dest = v.toArray();
		
		Object[] result = JythonSlice.arrayDelSlice( dest, i );

		v.clear();
		v.addAll( Arrays.asList( result ) );
		cell.setLiteralValue( v );
		if ( commandTracker != null )
		{
			commandTracker.onSetContents( this, oldContents, result );
		}
	}
	
	public int __len__()
	{
		return size();
	}

	public int index(Object x)
	{
		return indexOf( x );
	}
	
	
	public DMListInterface __add__(List<Object> xs)
	{
		DMList result = (DMList)clone();
		result.addAll( xs );
		return result;
	}

	
	
	public String toString()
	{
		if ( size() > 0 )
		{
			StringBuilder builder = new StringBuilder();
			builder.append( "[ " );
			
			for (int i = 0; i < size() - 1; i++)
			{
				builder.append( get( i ).toString() );
				builder.append( ", " );
			}
			
			builder.append( get( size() - 1 ).toString() );
			builder.append( " ]" );
			
			return builder.toString();
		}
		else
		{
			return "[]";
		}
	}
	
	
	
	/*
	 * Only call from DMListCommandTracker
	 */
	@SuppressWarnings("unchecked")
	protected void removeLast(int numElements)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		v.setSize( v.size() - numElements );
		cell.setLiteralValue( v );
	}


	/*
	 * Only call from DMListCommandTracker
	 */
	@SuppressWarnings("unchecked")
	protected void removeRange(int start, int num)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		for (int i = 0; i < num; i++)
		{
			v.remove( start );
		}
		cell.setLiteralValue( v );
	}


	/*
	 * Only call from DMListCommandTracker
	 */
	@SuppressWarnings("unchecked")
	protected void setContents(List<Object> xs)
	{
		Vector<Object> v = (Vector<Object>)cell.getLiteralValue();
		v.clear();
		v.addAll( xs );
		cell.setLiteralValue( v );
	}


	@SuppressWarnings("unchecked")
	protected Vector<Object> getInternalContainer()
	{
		return (Vector<Object>)cell.getLiteralValue();
	}





	public CommandTrackerFactory getTrackerFactory()
	{
		return DMListTrackerFactory.factory;
	}

	public void setTracker(CommandTracker tracker)
	{
		commandTracker = (DMListCommandTracker)tracker;
	}
}
