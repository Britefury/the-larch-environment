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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PySlice;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Util.HashUtils;
import BritefuryJ.Util.Jython.JythonIndex;
import BritefuryJ.Util.Jython.JythonSlice;

public class DMList extends DMNode implements List<Object>, Trackable, Presentable
{
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
	
	
	
	private IncrementalValueMonitor incr;
	ArrayList<Object> value;
	private ChangeHistory changeHistory;
	
	
	public DMList()
	{
		this( null );
	}
	
	public DMList(List<Object> xs)
	{
		incr = new IncrementalValueMonitor( this );
		value = new ArrayList<Object>();

		if ( xs != null )
		{
			value.ensureCapacity(  xs.size() );
			for (Object x: xs)
			{
				x = coerceForStorage( x );
				notifyAddChild( x );
				value.add( x );
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void become(Object x)
	{
		if ( x instanceof DMList )
		{
			DMList ls = (DMList)x;
			ls.onAccess();
			setContents( ls.value );
		}
		else if ( x instanceof List )
		{
			setContents( (List<Object>)x );
		}
		else
		{
			throw new CannotChangeNodeClassException( x.getClass(), getClass() );
		}
	}
	
	
	@Override
	protected Object createDeepCopy(PyDictionary memo)
	{
		onAccess();

		ArrayList<Object> ys = new ArrayList<Object>();
		ys.ensureCapacity( value.size() );
		
		for (Object x: value)
		{
			ys.add( deepCopyOf( x, memo ) );
		}
		
		return new DMList( ys );
	}
	
	@Override
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		onAccess();

		ArrayList<Object> ys = new ArrayList<Object>();
		ys.ensureCapacity( value.size() );
		
		for (Object x: value)
		{
			ys.add( memo.copy( x ) );
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
		x = coerceForStorage( x );
		notifyAddChild( x );
		boolean bResult = value.add( x );
		incr.onChanged();
		DMList_changes.onAdd( changeHistory, this, x );
		return bResult;
	}
	
	public void add(int index, Object x)
	{
		x = coerceForStorage( x );
		notifyAddChild( x );
		value.add( index, x );
		incr.onChanged();
		DMList_changes.onInsert( changeHistory, this, index, x );
	}
	
	public boolean addAll(Collection<?> xs)
	{
		ArrayList<Object> cxs = new ArrayList<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			x = coerceForStorage( x );
			cxs.add( x );
			notifyAddChild( x );
		}
		
		value.addAll( cxs );
		incr.onChanged();
		DMList_changes.onAddAll( changeHistory, this, cxs );
		return true;
	}
	
	public boolean addAll(int index, Collection<?> xs)
	{
		ArrayList<Object> cxs = new ArrayList<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			x = coerceForStorage( x );
			cxs.add( x );
			notifyAddChild( x );
		}
		
		value.addAll( index, cxs );
		incr.onChanged();
		DMList_changes.onInsertAll( changeHistory, this, index, cxs );
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
			notifyRemoveChild( x );
			updateChildParentage( x );
		}
		DMList_changes.onClear( changeHistory, this, copy );
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
	
	public int hashCode()
	{
		onAccess();
		int hashes[] = new int[value.size()];
		for (int i = 0; i < hashes.length; i++)
		{
			hashes[i] = value.get( i ).hashCode();
		}
		return HashUtils.nHash( hashes );
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
		notifyRemoveChild( x );
		updateChildParentage( x );
		incr.onChanged();
		DMList_changes.onRemove( changeHistory, this, i, x );
		return x;
	}
	
	public boolean remove(Object x)
	{
		int i = value.indexOf( x );
		if ( i != -1 )
		{
			value.remove( i );
			notifyRemoveChild( x );
			updateChildParentage( x );
			incr.onChanged();
			DMList_changes.onRemove( changeHistory, this, i, x );
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
		x = coerceForStorage( x );
		Object oldX = value.set( index, x );
		if ( oldX != x )
		{
			notifyRemoveChild( oldX );
			notifyAddChild( x );
			updateChildParentage( oldX );
		}
		incr.onChanged();
		DMList_changes.onSet( changeHistory, this, index, oldX, x );
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
	
	public void __setitem__(PySlice i, List<Object> xs)
	{
		Object oldContents[] = value.toArray();
		
		ArrayList<Object> cxs = new ArrayList<Object>();
		cxs.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			cxs.add( coerceForStorage( x ) );
		}
		
		Object[] src = cxs.toArray();
		Object[] dest = value.toArray();
		
		Object[] result = JythonSlice.arraySetSlice( dest, i, src );

		value.clear();
		value.addAll( Arrays.asList( result ) );
		for (Object x: oldContents)
		{
			notifyRemoveChild( x );
		}
		for (Object x: value)
		{
			notifyAddChild( x );
		}
		for (Object x: oldContents)
		{
			updateChildParentage( x );
		}
		incr.onChanged();
		DMList_changes.onSetContents( changeHistory, this, oldContents, result );
	}
	
	public void __delitem__(int i)
	{
		i = JythonIndex.pyIndexToJava( i, size(), "DMList assignment index out of range" );
		remove( i );
	}
	
	public void __delitem__(PySlice i)
	{
		Object oldContents[] = value.toArray();
		
		Object[] dest = value.toArray();
		
		Object[] result = JythonSlice.arrayDelSlice( dest, i );

		value.clear();
		value.addAll( Arrays.asList( result ) );
		for (Object x: oldContents)
		{
			notifyRemoveChild( x );
		}
		for (Object x: value)
		{
			notifyAddChild( x );
		}
		for (Object x: oldContents)
		{
			updateChildParentage( x );
		}
		incr.onChanged();
		DMList_changes.onSetContents( changeHistory, this, oldContents, result );
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
	
	
	public DMList __add__(List<Object> xs)
	{
		DMList result = new DMList( this );
		result.addAll( xs );
		return result;
	}

	
	
	public DMList __mul__(int n)
	{
		DMList result = new DMList();
		for (int i = 0; i < n; i++)
		{
			result.addAll( this );
		}
		return result;
	}

	
	public DMList __rmul__(int n)
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
	
	
	
	protected void removeLast(int numElements)
	{
		Object removedValues[] = value.subList( value.size() - numElements, value.size() ).toArray();
		
		for (int i = 0, j = value.size() - 1; i < numElements; i++, j--)
		{
			Object x = value.get( j );
			notifyRemoveChild( x );
			updateChildParentage( x );
			value.remove( j );
		}
		incr.onChanged();
	
		DMList_changes.onRemoveLast( changeHistory, this, removedValues );
	}


	protected void removeRange(int start, int num)
	{
		Object removedValues[] = value.subList( start, start + num ).toArray();
		
		for (int i = 0; i < num; i++)
		{
			Object x = value.get( start );
			notifyRemoveChild( x );
			updateChildParentage( x );
			value.remove( start );
		}
		incr.onChanged();
		
		DMList_changes.onRemoveRange( changeHistory, this, start, removedValues );
	}


	public void setContents(List<?> xs)
	{
		Object newContents[] = new Object[xs.size()];
		int i = 0;
		for (Object x: xs)
		{
			newContents[i] = coerceForStorage( x );
			i++;
		}
		
		commandTracker_setContents( newContents );
	}
	

	/*
	 * Only call from DMListCommandTracker
	 */
	protected void commandTracker_setContents(Object xs[])
	{
		Object oldContents[] = value.toArray();
		Object newContents[] = new Object[xs.length];
		System.arraycopy( xs, 0, newContents, 0, xs.length );

		value.clear();
		value.addAll( Arrays.asList( xs ) );
		for (Object x: oldContents)
		{
			notifyRemoveChild( x );
		}
		for (Object x: value)
		{
			notifyAddChild( x );
		}
		for (Object x: oldContents)
		{
			updateChildParentage( x );
		}
		incr.onChanged();
	
		DMList_changes.onSetContents( changeHistory, this, oldContents, newContents );
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
		incr.onAccess();
	}




	
	//
	// Trackable interface
	//

	public void setChangeHistory(ChangeHistory h)
	{
		changeHistory = h;
	}
	
	public ChangeHistory getChangeHistory()
	{
		return changeHistory;
	}
	
	
	public List<Object> getTrackableContents()
	{
		return value;
	}
	
	
	
	//
	// Serialisation
	//
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		incr = new IncrementalValueMonitor( this );
		value = (ArrayList<Object>)stream.readObject();
		for (Object x: value)
		{
			notifyAddChild( x );
		}
		incr.onChanged();
		
		changeHistory = null;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.writeObject( value );
	}


	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return DocModelPresenter.presentDMList( this, fragment, inheritedState );
	}
}
