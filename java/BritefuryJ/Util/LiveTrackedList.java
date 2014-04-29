//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2014.
//##************************
package BritefuryJ.Util;

import BritefuryJ.ChangeHistory.Change;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.ClipboardFilter.ClipboardCopyable;
import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.Util.Jython.JythonIndex;
import BritefuryJ.Util.Jython.JythonSlice;
import BritefuryJ.Util.Jython.Jython_copy;
import org.python.core.*;

import java.util.*;

public class LiveTrackedList <T> implements List<T>, Trackable, ClipboardCopyable {
	/*
	Change classes
	 */
	private static class AddCommand <T> extends Change
	{
		private LiveTrackedList<T> xls;
		private T x;

		public AddCommand(LiveTrackedList<T> ls, T x)
		{
			this.xls = ls;
			this.x = x;
		}


		protected void execute()
		{
			xls.add( x );
		}

		protected void unexecute()
		{
			xls.remove( xls.size() - 1 );
		}

		protected String getDescription()
		{
			return "List add";
		}
	}



	private static class InsertCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private int i;
		private T x;

		public InsertCommand(LiveTrackedList<T> ls, int i, T x)
		{
			this.ls = ls;
			this.i = i;
			this.x = x;
		}


		protected void execute()
		{
			ls.add( i, x );
		}

		protected void unexecute()
		{
			ls.remove( i );
		}

		protected String getDescription()
		{
			return "List insert (" + i + ")";
		}
	}



	private static class AddAllCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private List<T> x;

		public AddAllCommand(LiveTrackedList<T> ls, List<T> x)
		{
			this.ls = ls;
			this.x = x;
		}


		protected void execute()
		{
			ls.addAll( x );
		}

		protected void unexecute()
		{
			ls.removeLast( x.size() );
		}

		protected String getDescription()
		{
			return "List add all";
		}
	}



	private static class InsertAllCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private int i;
		private List<T> x;

		public InsertAllCommand(LiveTrackedList<T> ls, int i, List<T> x)
		{
			this.ls = ls;
			this.i = i;
			this.x = x;
		}


		protected void execute()
		{
			ls.addAll( i, x );
		}

		protected void unexecute()
		{
			ls.removeRange( i, x.size() );
		}

		protected String getDescription()
		{
			return "List insert all (" + i + ")";
		}
	}



	private static class ClearCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private ArrayList<T> contents;

		public ClearCommand(LiveTrackedList<T> ls, ArrayList<T> contents)
		{
			this.ls = ls;
			this.contents = contents;
		}


		protected void execute()
		{
			ls.clear();
		}

		protected void unexecute()
		{
			ls.addAll( contents );
		}

		protected String getDescription()
		{
			return "List clear";
		}
	}


	private static class RemoveCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private int i;
		private T x;

		public RemoveCommand(LiveTrackedList<T> ls, int i, T x)
		{
			this.ls = ls;
			this.i = i;
			this.x = x;
		}


		protected void execute()
		{
			ls.remove( i );
		}

		protected void unexecute()
		{
			ls.add( i, x );
		}

		protected String getDescription()
		{
			return "List remove (" + i + ")";
		}
	}



	private static class SetCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private int i;
		private T oldX, x;

		public SetCommand(LiveTrackedList<T> ls, int i, T oldX, T x)
		{
			this.ls = ls;
			this.i = i;
			this.oldX = oldX;
			this.x = x;
		}


		protected void execute()
		{
			ls.set( i, x );
		}

		protected void unexecute()
		{
			ls.set( i, oldX );
		}

		protected String getDescription()
		{
			return "List set (" + i + ")";
		}
	}



	private static class SetContentsCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private T[] oldContents;
		private T[] newContents;

		public SetContentsCommand(LiveTrackedList<T> ls, T[] oldContents, T[] newContents)
		{
			this.ls = ls;
			this.oldContents = oldContents;
			this.newContents = newContents;
		}


		protected void execute()
		{
			ls.commandTracker_setContents( newContents );
		}

		protected void unexecute()
		{
			ls.commandTracker_setContents( oldContents );
		}

		protected String getDescription()
		{
			return "List set contents";
		}
	}

	private static class RemoveLastCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private T removedValues[];

		public RemoveLastCommand(LiveTrackedList<T> ls, T removedValues[])
		{
			this.ls = ls;
			this.removedValues = removedValues;
		}


		protected void execute()
		{
			ls.removeLast( removedValues.length );
		}

		protected void unexecute()
		{
			ls.addAll( Arrays.asList( removedValues ) );
		}

		protected String getDescription()
		{
			return "List remove last";
		}
	}

	private static class RemoveRangeCommand <T>extends Change
	{
		private LiveTrackedList<T> ls;
		private int pos;
		private T removedValues[];

		public RemoveRangeCommand(LiveTrackedList<T> ls, int pos, T removedValues[])
		{
			this.ls = ls;
			this.pos = pos;
			this.removedValues = removedValues;
		}


		protected void execute()
		{
			ls.removeRange( pos, removedValues.length );
		}

		protected void unexecute()
		{
			ls.addAll( pos, Arrays.asList( removedValues ) );
		}

		protected String getDescription()
		{
			return "List remove range";
		}
	}





	protected static <T> void onAdd(ChangeHistory changeHistory, LiveTrackedList<T> ls, T x)
	{
		if ( changeHistory != null )
		{
			changeHistory.addChange( new AddCommand<T>( ls, x ) );
			changeHistory.track( x );
		}
	}

	protected static <T> void onInsert(ChangeHistory changeHistory, LiveTrackedList<T> ls, int i, T x)
	{
		if ( changeHistory != null )
		{
			changeHistory.addChange( new InsertCommand<T>( ls, i, x ) );
			changeHistory.track( x );
		}
	}

	protected static <T> void onAddAll(ChangeHistory changeHistory, LiveTrackedList<T> ls, List<T> xs)
	{
		if ( changeHistory != null )
		{
			changeHistory.addChange( new AddAllCommand<T>( ls, xs ) );
			for (Object x: xs)
			{
				changeHistory.track( x );
			}
		}
	}

	protected static <T> void onInsertAll(ChangeHistory changeHistory, LiveTrackedList<T> ls, int i, List<T> xs)
	{
		if ( changeHistory != null )
		{
			changeHistory.addChange( new InsertAllCommand<T>( ls, i, xs ) );
			for (Object x: xs)
			{
				changeHistory.track( x );
			}
		}
	}

	protected static <T> void onClear(ChangeHistory changeHistory, LiveTrackedList<T> ls, ArrayList<T> contents)
	{
		if ( changeHistory != null )
		{
			for (Object x: contents)
			{
				changeHistory.stopTracking( x );
			}
			changeHistory.addChange( new ClearCommand<T>( ls, contents ) );
		}
	}

	protected static <T> void onRemove(ChangeHistory changeHistory, LiveTrackedList<T> ls, int i, T x)
	{
		if ( changeHistory != null )
		{
			changeHistory.stopTracking( x );
			changeHistory.addChange( new RemoveCommand<T>( ls, i, x ) );
		}
	}

	protected static <T> void onSet(ChangeHistory changeHistory, LiveTrackedList<T> ls, int i, T oldX, T x)
	{
		if ( changeHistory != null )
		{
			changeHistory.stopTracking( oldX );
			changeHistory.addChange( new SetCommand<T>( ls, i, oldX, x ) );
			changeHistory.track( x );
		}
	}

	protected static <T> void onSetContents(ChangeHistory changeHistory, LiveTrackedList<T> ls, T[] oldContents, T[] newContents)
	{
		if ( changeHistory != null )
		{
			for (Object oldX: oldContents)
			{
				changeHistory.stopTracking( oldX );
			}
			changeHistory.addChange( new SetContentsCommand<T>( ls, oldContents, newContents ) );
			for (Object x: newContents)
			{
				changeHistory.track( x );
			}
		}
	}

	protected static <T> void onRemoveLast(ChangeHistory changeHistory, LiveTrackedList<T> ls, T[] removedValues)
	{
		if ( changeHistory != null )
		{
			for (Object x: removedValues)
			{
				changeHistory.stopTracking( x );
			}
			changeHistory.addChange( new RemoveLastCommand<T>( ls, removedValues ) );
		}
	}

	protected static <T> void onRemoveRange(ChangeHistory changeHistory, LiveTrackedList<T> ls, int pos, T[] removedValues)
	{
		if ( changeHistory != null )
		{
			for (Object x: removedValues)
			{
				changeHistory.stopTracking( x );
			}
			changeHistory.addChange( new RemoveRangeCommand<T>( ls, pos, removedValues ) );
		}
	}



	/*
	Iterator
	 */
	private static class LiveTrackedListIterator<T> implements Iterator<T>
	{
		protected List<T> src;
		protected int index;

		public LiveTrackedListIterator(List<T> src)
		{
			this.src = src;
			index = 0;
		}

		public boolean hasNext()
		{
			return index < src.size();
		}

		public T next()
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


	private static class LiveTrackedListListIterator<T> implements ListIterator<T>
	{
		private List<T> src;
		private int index, lastIndex;

		public LiveTrackedListListIterator(List<T> src)
		{
			this.src = src;
			lastIndex = index = 0;

		}

		public LiveTrackedListListIterator(List<T> src, int i)
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

		public T next()
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

		public T previous()
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

		public void add(T x)
		{
			src.add( lastIndex, x );
		}

		public void remove()
		{
			src.remove( lastIndex );
		}

		public void set(T x)
		{
			src.set( lastIndex, x );
		}
	}




	private static class ListView<T> implements List<T>
	{
		private LiveTrackedList<T> src;
		private int start, stop;


		private ListView(LiveTrackedList<T> src, int start, int stop)
		{
			this.src = src;
			this.start = start;
			this.stop = stop;
		}




		public boolean add(T x)
		{
			src.add( stop, x );
			stop++;
			return true;
		}

		public void add(int index, T x)
		{
			src.add( start + index, x );
			stop++;
		}

		public boolean addAll(Collection<? extends T> xs)
		{
			boolean bResult = src.addAll( stop, xs );
			stop += xs.size();
			return bResult;
		}

		public boolean addAll(int index, Collection<? extends T> xs)
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
			return this == xs  ||  src.getInternalContainer().subList( start, stop ).equals( xs );
		}


		public T get(int index)
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


		public Iterator<T> iterator()
		{
			return new LiveTrackedListIterator<T>( this );
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


		public ListIterator<T> listIterator()
		{
			return new LiveTrackedListListIterator<T>( this );
		}

		public ListIterator<T> listIterator(int i)
		{
			return new LiveTrackedListListIterator<T>( this, i );
		}

		public T remove(int i)
		{
			T x = src.remove( i - start );
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

		public T set(int index, T x)
		{
			return src.set( start + index, x );
		}

		public int size()
		{
			return stop - start;
		}



		public List<T> subList(int fromIndex, int toIndex)
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

		public <U> U[] toArray(U[] a)
		{
			return src.getInternalContainer().subList( start, stop ).toArray( a );
		}
	}



	private IncrementalValueMonitor incr;
	ArrayList<T> value;
	private ChangeHistory changeHistory;


	public LiveTrackedList()
	{
		this( null );
	}

	public LiveTrackedList(List<T> xs)
	{
		incr = new IncrementalValueMonitor( this );
		value = new ArrayList<T>();

		if ( xs != null )
		{
			value.addAll(xs);
		}
	}


	public PyObject __deepcopy__(PyDictionary memo)
	{
		long id = Py.java_obj_id( this );
		PyObject key = Py.newInteger( id );

		PyObject value = memo.get( key );
		if ( value != Py.None )
		{
			return value;
		}
		else
		{
			Object copy = createDeepCopy( memo );
			value = Py.java2py( copy );
			memo.__setitem__( key, value );
			return value;
		}
	}

	protected Object createDeepCopy(PyDictionary memo)
	{
		onAccess();

		ArrayList<Object> ys = new ArrayList<Object>();
		ys.ensureCapacity( value.size() );

		for (Object x: value)
		{
			Object copy = null;
			if (x instanceof PyObject) {
				copy = Jython_copy.deepcopy((PyObject)x, memo);
			}
			else {
				copy = x;
			}
			ys.add( copy );
		}

		return new LiveTrackedList( ys );
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

		return new LiveTrackedList( ys );
	}


	public boolean add(T x)
	{
		boolean bResult = value.add( x );
		incr.onChanged();
		onAdd(changeHistory, this, x);
		return bResult;
	}

	public void add(int index, T x)
	{
		value.add( index, x );
		incr.onChanged();
		onInsert(changeHistory, this, index, x);
	}

	public boolean addAll(Collection<? extends T> xs)
	{
		ArrayList<T> cxs = new ArrayList<T>();
		cxs.addAll(xs);

		value.addAll( cxs );
		incr.onChanged();
		onAddAll(changeHistory, this, cxs);
		return true;
	}

	public boolean addAll(int index, Collection<? extends T> xs)
	{
		ArrayList<T> cxs = new ArrayList<T>();
		cxs.addAll(xs);

		value.addAll( index, cxs );
		incr.onChanged();
		onInsertAll(changeHistory, this, index, cxs);
		return true;
	}



	@SuppressWarnings("unchecked")
	public void clear()
	{
		ArrayList<T> copy = (ArrayList<T>)value.clone();
		value.clear();
		incr.onChanged();
		onClear(changeHistory, this, copy);
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


	public T get(int index)
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


	public Iterator<T> iterator()
	{
		onAccess();
		return value.iterator();
	}


	public int lastIndexOf(Object x)
	{
		onAccess();
		return value.lastIndexOf(  x );
	}


	public ListIterator<T> listIterator()
	{
		onAccess();
		return value.listIterator();
	}

	public ListIterator<T> listIterator(int i)
	{
		onAccess();
		return value.listIterator( i );
	}

	public T remove(int i)
	{
		T x = value.remove( i );
		incr.onChanged();
		onRemove(changeHistory, this, i, x);
		return x;
	}

	public boolean remove(Object x)
	{
		int i = value.indexOf( x );
		if ( i != -1 )
		{
			value.remove(i);
			incr.onChanged();
			onRemove(changeHistory, this, i, (T)x);
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

	public T set(int index, T x)
	{
		T oldX = value.set( index, x );
		incr.onChanged();
		onSet( changeHistory, this, index, oldX, x );
		return oldX;
	}

	public int size()
	{
		onAccess();
		return value.size();
	}



	public List<T> subList(int fromIndex, int toIndex)
	{
		if ( fromIndex < 0  ||  toIndex > size()  ||  fromIndex > toIndex )
		{
			throw new IndexOutOfBoundsException();
		}
		return new ListView<T>( this, fromIndex, toIndex );
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
		add( (T)x );
	}

	public void extend(List<Object> xs)
	{
		addAll( (List<T>)xs );
	}

	public void insert(int i, Object x)
	{
		int s = size();
		// Clamp to -s:s
		i = Math.min( Math.max( i, -s ), s );
		// Handle negative indexing
		i = i < 0  ?  s + i  :  i;
		add( i, (T)x );
	}

	public Object __getitem__(int i)
	{
		i = JythonIndex.pyIndexToJava(i, size(), "DMList index out of range");
		return get( i );
	}

	public List<Object> __getitem__(PySlice i)
	{
		onAccess();
		return Arrays.asList( JythonSlice.arrayGetSlice(value.toArray(), i) );
	}

	public void __setitem__(int i, Object x)
	{
		i = JythonIndex.pyIndexToJava( i, size(), "DMList assignment index out of range" );
		set( i, (T)x );
	}

	public void __setitem__(PySlice i, List<Object> xs)
	{
		T oldContents[] = value.toArray((T[])new Object[value.size()]);

		T[] src = xs.toArray((T[])new Object[xs.size()]);
		T[] dest = value.toArray((T[])new Object[value.size()]);

		T[] result = (T[])JythonSlice.arraySetSlice( dest, i, src );

		value.clear();
		value.addAll( Arrays.asList( result ) );
		incr.onChanged();
		onSetContents( changeHistory, this, oldContents, result );
	}

	public void __delitem__(int i)
	{
		i = JythonIndex.pyIndexToJava( i, size(), "DMList assignment index out of range" );
		remove( i );
	}

	public void __delitem__(PySlice i)
	{
		T[] oldContents = (T[])value.toArray();

		T[] dest = (T[])value.toArray();

		T[] result = (T[])JythonSlice.arrayDelSlice( dest, i );

		value.clear();
		value.addAll( Arrays.asList( result ) );
		incr.onChanged();
		onSetContents( changeHistory, this, oldContents, result );
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
			throw Py.ValueError("DMList.index(x): x not in list");
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


	public LiveTrackedList __add__(List<Object> xs)
	{
		LiveTrackedList result = new LiveTrackedList( this );
		result.addAll( xs );
		return result;
	}



	public LiveTrackedList __mul__(int n)
	{
		LiveTrackedList result = new LiveTrackedList();
		for (int i = 0; i < n; i++)
		{
			result.addAll( this );
		}
		return result;
	}


	public LiveTrackedList __rmul__(int n)
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
		T removedValues[] = value.subList( value.size() - numElements, value.size() ).toArray((T[])new Object[numElements]);

		for (int i = 0, j = value.size() - 1; i < numElements; i++, j--)
		{
			value.remove( j );
		}
		incr.onChanged();

		onRemoveLast( changeHistory, this, removedValues );
	}


	protected void removeRange(int start, int num)
	{
		T removedValues[] = value.subList( start, start + num ).toArray((T[])new Object[num]);

		for (int i = 0; i < num; i++)
		{
			Object x = value.get( start );
			value.remove( start );
		}
		incr.onChanged();

		onRemoveRange( changeHistory, this, start, removedValues );
	}


	public void setContents(List<?> xs)
	{
		T newContents[] = xs.toArray((T[])new Object[xs.size()]);

		commandTracker_setContents( newContents );
	}


	/*
	 * Only call from DMListCommandTracker
	 */
	protected void commandTracker_setContents(T xs[])
	{
		T oldContents[] = value.toArray((T[])new Object[value.size()]);
		T newContents[] = (T[])new Object[xs.length];
		System.arraycopy( xs, 0, newContents, 0, xs.length );

		value.clear();
		value.addAll( Arrays.asList( xs ) );
		incr.onChanged();

		onSetContents( changeHistory, this, oldContents, newContents );
	}


	protected ArrayList<T> getInternalContainer()
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
		return (List<Object>)value;
	}



	//
	// Pickling
	//

	public PyObject __getstate__()
	{
		PyList contents = new PyList();
		contents.addAll( this );

		PyTuple state = new PyTuple( contents );

		return state;
	}

	public void __setstate__(PyObject state)
	{
		// DMList state is of the form of a list in a tuple
		if ( state instanceof PyTuple )
		{
			PyTuple tupleState = (PyTuple)state;

			if ( tupleState.size() == 1 )
			{
				PyList contents = (PyList)tupleState.pyget( 0 );

				addAll( contents );
			}
		}
		else
		{
			throw Py.TypeError("LiveTrackedList.__setstate__: state must be a Python tuple");
		}
	}
}
