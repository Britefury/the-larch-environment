//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.python.core.Py;
import org.python.core.PySlice;

import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.CommandTrackerFactory;
import BritefuryJ.CommandHistory.Trackable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.ObjectView.Presentable;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.Incremental.IncrementalOwner;
import BritefuryJ.Incremental.IncrementalValue;
import BritefuryJ.JythonInterface.JythonIndex;
import BritefuryJ.JythonInterface.JythonSlice;

public class DMList extends DMNode implements DMListInterface, Trackable, Serializable, IncrementalOwner, Presentable
{
	private static final long serialVersionUID = 1L;
	
	protected static DMNodeClass listNodeClass = new DMNodeClass( "DMList" );
	
	
	private static class DMListIterator implements Iterator<Object>
	{
		protected List<Object> src;
		protected int index;
		
		public DMListIterator(List<Object> src)
		{
			this.src = src;
			index = 0;
		}
		
		public boolean hasNext()
		{
			return index < src.size();
		}

		public Object next()
		{
			if ( index < src.size() )
			{
				return src.get( index++ );
			}
			else
			{
				throw new NoSuchElementException();
			}
		}
		
		public void remove()
		{
			src.remove( index - 1 );
		}
	}
	

	private static class DMListListIterator implements ListIterator<Object>
	{
		private List<Object> src;
		private int index, lastIndex;
		
		public DMListListIterator(List<Object> src)
		{
			this.src = src;
			lastIndex = index = 0;
			
		}

		public DMListListIterator(List<Object> src, int i)
		{
			this.src = src;
			lastIndex = index = i;
			
		}

		
		public boolean hasNext()
		{
			return index < src.size();
		}

		public int nextIndex()
		{
			return index;
		}

		public Object next()
		{
			if ( index < src.size() )
			{
				lastIndex = index++;
				return src.get( lastIndex );
			}
			else
			{
				throw new NoSuchElementException();
			}
		}
		

		public boolean hasPrevious()
		{
			return index > 0;
		}

		public int previousIndex()
		{
			return index - 1;
		}

		public Object previous()
		{
			if ( index > 0 )
			{
				lastIndex = --index;
				return src.get( lastIndex );
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		public void add(Object x)
		{
			src.add( lastIndex, x );
		}

		public void remove()
		{
			src.remove( lastIndex );
		}

		public void set(Object x)
		{
			src.set( lastIndex, x );
		}
	}
	

	
	
	private static class ListView implements List<Object>
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
			stop++;
			return true;
		}
		
		public void add(int index, Object x)
		{
			src.add( start + index, x );
			stop++;
		}
		
		public boolean addAll(Collection<?> xs)
		{
			boolean bResult = src.addAll( stop, xs );
			stop += xs.size();
			return bResult;
		}
		
		public boolean addAll(int index, Collection<?> xs)
		{
			boolean bResult = src.addAll( start + index, xs );
			stop += xs.size();
			return bResult;
		}
		

		
		public void clear()
		{
			src.removeRange( start, stop - start );
			stop = start;
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
			if ( this == xs )
			{
				return true;
			}
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
			return new DMListIterator( this );
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
			return new DMListListIterator( this );
		}
		
		public ListIterator<Object> listIterator(int i)
		{
			return new DMListListIterator( this, i );
		}
		
		public Object remove(int i)
		{
			Object x = src.remove( i - start );
			stop--;
			return x;
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
				stop--;
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
			if ( fromIndex < 0  ||  toIndex > size()  ||  fromIndex > toIndex )
			{
				throw new IndexOutOfBoundsException();
			}
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
	
	
	
	private IncrementalValue incr;
	ArrayList<Object> value;
	private DMListCommandTracker commandTracker;
	
	
	public DMList()
	{
		this( null );
	}
	
	public DMList(List<Object> xs)
	{
		incr = new IncrementalValue( this );
		value = new ArrayList<Object>();

		if ( xs != null )
		{
			value.ensureCapacity(  xs.size() );
			for (Object x: xs)
			{
				x = coerce( x );
				if ( x instanceof DMNode )
				{
					((DMNode)x).addParent( this );
				}
				value.add( x );
			}
		}
		
		commandTracker = null;
	}
	
	
	public Object clone()
	{
		return new DMList( this );
	}
	
	protected Object createDeepCopy(Map<Object, Object> memo)
	{
		onAccess();

		ArrayList<Object> ys = new ArrayList<Object>();
		ys.ensureCapacity( value.size() );
		
		for (Object x: value)
		{
			if ( x instanceof DMNode )
			{
				ys.add( ((DMNode)x).deepCopy( memo ) );
			}
			else
			{
				ys.add( x );
			}
		}
		
		return new DMList( ys );
	}
	
	
	public static DMNodeClass getListDMNodeClass()
	{
		return listNodeClass;
	}
	
	public DMNodeClass getDMNodeClass()
	{
		return listNodeClass;
	}
	
	
	
	
	public boolean add(Object x)
	{
		x = coerce( x );
		if ( x instanceof DMNode )
		{
			((DMNode)x).addParent( this );
		}
		boolean bResult = value.add( x );
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onAdd( this, x );
		}
		return bResult;
	}
	
	public void add(int index, Object x)
	{
		x = coerce( x );
		if ( x instanceof DMNode )
		{
			((DMNode)x).addParent( this );
		}
		value.add( index, x );
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onInsert( this, index, x );
		}
	}
	
	public boolean addAll(Collection<?> xs)
	{
		ArrayList<Object> cxs = new ArrayList<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			x = coerce( x );
			cxs.add( x );
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		
		value.addAll( cxs );
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onAddAll( this, cxs );
		}
		return true;
	}
	
	public boolean addAll(int index, Collection<?> xs)
	{
		ArrayList<Object> cxs = new ArrayList<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			x = coerce( x );
			cxs.add( x );
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		
		value.addAll( index, cxs );
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onInsertAll( this, index, cxs );
		}
		return true;
	}
	

	
	@SuppressWarnings("unchecked")
	public void clear()
	{
		ArrayList<Object> copy = (ArrayList<Object>)value.clone();
		value.clear();
		incr.onChanged();
		for (Object x: copy)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).removeParent( this );
			}
		}
		if ( commandTracker != null )
		{
			commandTracker.onClear( this, copy );
		}
	}
	
	
	public boolean contains(Object x)
	{
		onAccess();
		return value.contains( x );
	}
	

	public boolean containsAll(Collection<?> x)
	{
		onAccess();
		for (Object a: x)
		{
			if ( !value.contains( a ) )
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	public boolean equals(Object x)
	{
		if ( this == x )
		{
			return true;
		}
		
		onAccess();
		return value.equals( x );
	}
	
	
	public Object get(int index)
	{
		onAccess();
		return value.get( index );
	}
	
	
	public int indexOf(Object x)
	{
		onAccess();
		return value.indexOf(  x );
	}

	
	public int indexOfById(Object x)
	{
		onAccess();
		for (int i = 0; i < value.size(); i++)
		{
			if ( value.get( i ) == x )
			{
				return i;
			}
		}
		return -1;
	}

	
	public boolean isEmpty()
	{
		onAccess();
		return value.isEmpty();
	}
	
	
	public Iterator<Object> iterator()
	{
		onAccess();
		return value.iterator();
	}
	
	
	public int lastIndexOf(Object x)
	{
		onAccess();
		return value.lastIndexOf(  x );
	}

	
	public ListIterator<Object> listIterator()
	{
		onAccess();
		return value.listIterator();
	}
	
	public ListIterator<Object> listIterator(int i)
	{
		onAccess();
		return value.listIterator( i );
	}
	
	public Object remove(int i)
	{
		Object x = value.remove( i );
		if ( x instanceof DMNode )
		{
			((DMNode)x).removeParent( this );
		}
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onRemove( this, i, x );
		}
		return x;
	}
	
	public boolean remove(Object x)
	{
		int i = value.indexOf( x );
		if ( i != -1 )
		{
			value.remove( i );
			if ( x instanceof DMNode )
			{
				((DMNode)x).removeParent( this );
			}
			incr.onChanged();
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
	
	public Object set(int index, Object x)
	{
		x = coerce( x );
		Object oldX = value.set( index, x );
		if ( oldX != x )
		{
			if ( oldX instanceof DMNode )
			{
				((DMNode)oldX).removeParent( this );
			}
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onSet( this, index, oldX, x );
		}
		return oldX;
	}
	
	public int size()
	{
		onAccess();
		return value.size();
	}
	
	
	
	public List<Object> subList(int fromIndex, int toIndex)
	{
		if ( fromIndex < 0  ||  toIndex > size()  ||  fromIndex > toIndex )
		{
			throw new IndexOutOfBoundsException();
		}
		return new ListView( this, fromIndex, toIndex );
	}

	public Object[] toArray()
	{
		onAccess();
		return value.toArray();
	}

	public <T> T[] toArray(T[] a)
	{
		onAccess();
		return value.toArray( a );
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
		int s = size();
		// Clamp to -s:s
		i = Math.min( Math.max( i, -s ), s );
		// Handle negative indexing
		i = i < 0  ?  s + i  :  i;
		add( i, x );
	}
	
	public Object __getitem__(int i)
	{
		i = JythonIndex.pyIndexToJava( i, size(), "DMList index out of range" );
		return get( i );
	}
	
	public List<Object> __getitem__(PySlice i)
	{
		onAccess();
		return Arrays.asList( JythonSlice.arrayGetSlice( value.toArray(), i ) );
	}
	
	public void __setitem__(int i, Object x)
	{
		i = JythonIndex.pyIndexToJava( i, size(), "DMList assignment index out of range" );
		set( i, x );
	}
	
	@SuppressWarnings("unchecked")
	public void __setitem__(PySlice i, List<Object> xs)
	{
		ArrayList<Object> oldContents = (ArrayList<Object>)value.clone();
		
		ArrayList<Object> cxs = new ArrayList<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			cxs.add( coerce( x ) );
		}
		
		Object[] src = cxs.toArray();
		Object[] dest = value.toArray();
		
		Object[] result = JythonSlice.arraySetSlice( dest, i, src );

		for (Object x: value)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).removeParent( this );
			}
		}
		value.clear();
		value.addAll( Arrays.asList( result ) );
		for (Object x: value)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onSetContents( this, oldContents, result );
		}
	}
	
	public void __delitem__(int i)
	{
		i = JythonIndex.pyIndexToJava( i, size(), "DMList assignment index out of range" );
		remove( i );
	}
	
	@SuppressWarnings("unchecked")
	public void __delitem__(PySlice i)
	{
		ArrayList<Object> oldContents = (ArrayList<Object>)value.clone();
		
		Object[] dest = value.toArray();
		
		Object[] result = JythonSlice.arrayDelSlice( dest, i );

		for (Object x: value)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).removeParent( this );
			}
		}
		value.clear();
		value.addAll( Arrays.asList( result ) );
		for (Object x: value)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onSetContents( this, oldContents, result );
		}
	}
	
	public Object pop()
	{
		return remove( size() - 1 );
	}
	
	public Object pop(int i)
	{
		i = JythonIndex.pyIndexToJava( i, size(), "pop index out of range" );
		return remove( i );
	}
	
	public int __len__()
	{
		return size();
	}

	public int index(Object x)
	{
		int i = indexOf( x );
		if ( i == -1 )
		{
			throw Py.ValueError( "DMList.index(x): x not in list" );
		}
		return i;
	}
	
	public int index(Object x, int j)
	{
		onAccess();
	
		int s = value.size();
		// Clamp to -s:s
		j = Math.min( Math.max( j, -s ), s );
		// Handle negative indexing
		j = j < 0  ?  s + j  :  j;

		int i = value.subList( j, value.size() ).indexOf( x );
		if ( i == -1 )
		{
			throw Py.ValueError( "DMList.index(x,j): x not in list[j:]" );
		}
		return i + j;
	}
	
	public int index(Object x, int j, int k)
	{
		onAccess();
		
		int s = value.size();
		// Clamp to -s:s
		j = Math.min( Math.max( j, -s ), s );
		k = Math.min( Math.max( k, -s ), s );
		// Handle negative indexing
		j = j < 0  ?  s + j  :  j;
		k = k < 0  ?  s + k  :  k;

		int i = value.subList( j, k ).indexOf( x );
		if ( i == -1 )
		{
			throw Py.ValueError( "DMList.index(x,j,k): x not in list[j:k]" );
		}
		return i + j;
	}
	
	public int count(Object x)
	{
		int n = 0;
		
		onAccess();
		for (Object a: value)
		{
			if ( a.equals( x ) )
			{
				n++;
			}
		}
		
		return n;
	}
	
	
	public DMListInterface __add__(List<Object> xs)
	{
		DMList result = (DMList)clone();
		result.addAll( xs );
		return result;
	}

	
	
	public DMListInterface __mul__(int n)
	{
		DMList result = new DMList();
		for (int i = 0; i < n; i++)
		{
			result.addAll( this );
		}
		return result;
	}

	
	public DMListInterface __rmul__(int n)
	{
		return __mul__( n );
	}
	
	
	public Iterable<Object> getChildren()
	{
		Iterable<Object> iterable = new Iterable<Object>()
		{
			public Iterator<Object> iterator()
			{
				Iterator<Object> iter = new Iterator<Object>()
				{
					int index = 0;
					
					public boolean hasNext()
					{
						onAccess();
						return index < value.size();
					}

					public Object next()
					{
						onAccess();
						if ( index < value.size() )
						{
							return value.get( index++ );
						}
						else
						{
							throw new NoSuchElementException();
						}
					}

					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
				
				return iter;
			}
		};
		return iterable;
	}
	
	
	
	public String toString()
	{
		if ( size() > 0 )
		{
			StringBuilder builder = new StringBuilder();
			builder.append( "[" );
			
			for (int i = 0; i < size() - 1; i++)
			{
				Object x = get( i );
				if ( x == null )
				{
					builder.append( "<null>" );
				}
				else
				{
					builder.append( x.toString() );
				}
				builder.append( ", " );
			}
			
			Object x = get( size() - 1 );
			if ( x == null )
			{
				builder.append( "<null>" );
			}
			else
			{
				builder.append( x.toString() );
			}
			builder.append( "]" );
			
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
	protected void removeLast(int numElements)
	{
		for (int i = 0, j = value.size() - 1; i < numElements; i++, j--)
		{
			Object x = value.get( j );
			if ( x instanceof DMNode )
			{
				((DMNode)x).removeParent( this );
			}
			value.remove( j );
		}
		incr.onChanged();
	}


	/*
	 * Only call from DMListCommandTracker
	 */
	protected void removeRange(int start, int num)
	{
		for (int i = 0; i < num; i++)
		{
			Object x = value.get( start );
			if ( x instanceof DMNode )
			{
				((DMNode)x).removeParent( this );
			}
			value.remove( start );
		}
		incr.onChanged();
	}


	/*
	 * Only call from DMListCommandTracker
	 */
	protected void setContents(List<Object> xs)
	{
		for (Object x: value)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).removeParent( this );
			}
		}
		value.clear();
		value.addAll( xs );
		for (Object x: value)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		incr.onChanged();
	}


	protected ArrayList<Object> getInternalContainer()
	{
		onAccess();
		return value;
	}
	
	
	
	
	//
	// Incremental computation
	//
	
	private void onAccess()
	{
		Object refreshState = incr.onRefreshBegin();
		incr.onRefreshEnd( refreshState );
		incr.onAccess();
	}




	
	//
	// Trackable interface
	//

	public CommandTrackerFactory getTrackerFactory()
	{
		return DMListTrackerFactory.factory;
	}

	public void setTracker(CommandTracker tracker)
	{
		commandTracker = (DMListCommandTracker)tracker;
	}
	
	
	
	
	//
	// Serialisation
	//
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		incr = new IncrementalValue( this );
		value = (ArrayList<Object>)stream.readObject();
		for (Object x: value)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		incr.onChanged();
		
		commandTracker = null;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.writeObject( value );
	}


	
	public DPElement present(GSymFragmentViewContext ctx, StyleSheet styleSheet, Object state)
	{
		return DocModelPresenter.presentDMList( this, ctx, PrimitiveStyleSheet.instance, state );
	}
}
