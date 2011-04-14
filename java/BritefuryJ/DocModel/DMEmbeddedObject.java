//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.Isolation.IsolationBarrier;

public class DMEmbeddedObject extends DMNode
{
	protected static class ChildrenIterator implements Iterator<Object>
	{
		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Object next()
		{
			throw new NoSuchElementException();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	protected static class ChildrenIterable implements Iterable<Object>
	{
		private static ChildrenIterator iter = new ChildrenIterator();
		
		@Override
		public Iterator<Object> iterator()
		{
			return iter;
		}
	}
	
	protected static ChildrenIterable childrenIterable = new ChildrenIterable();

	
	
	private static final long serialVersionUID = 1L;
	
	protected static DMNodeClass embeddedObjectNodeClass = new DMNodeClass( "DMEmbeddedObject" );


	private IsolationBarrier<PyObject> iso = null;
	
	
	public DMEmbeddedObject()
	{
		super();
	}
	
	public DMEmbeddedObject(PyObject value)
	{
		super();
		this.iso = new IsolationBarrier<PyObject>( value );
	}
	
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		else
		{
			if ( x instanceof DMEmbeddedObject )
			{
				DMEmbeddedObject e = (DMEmbeddedObject)x;
				
				if ( iso != null  &&  e.iso != null )
				{
					return iso.equals( e.iso );
				}
				else
				{
					return ( iso != null ) == ( e.iso != null );
				}
			}
			
			return false;
		}
	}
	
	public int hashCode()
	{
		return iso != null  ?  iso.hashCode()  :  0;
	}
	
	
	@Override
	public void become(Object x)
	{
		if ( x instanceof DMEmbeddedObject )
		{
			DMEmbeddedObject em = (DMEmbeddedObject)x;
			this.iso = em.iso;
			
			// We could remove this exception - just include an incremental monitor (as with DMList), as notify it when the value is accessed and modified
			throw new RuntimeException( "DMEmbeddedObject does not support become()" );
		}
		else
		{
			throw new CannotChangeNodeClassException( x.getClass(), getClass() );
		}
	}

	@Override
	protected Object createDeepCopy(IdentityHashMap<Object, Object> memo)
	{
		if ( iso != null )
		{
			return new DMEmbeddedObject( iso.getValue() );
		}
		else
		{
			return new DMEmbeddedObject();
		}
	}

	@Override
	public DMNodeClass getDMNodeClass()
	{
		return embeddedObjectNodeClass;
	}

	@Override
	public Iterable<Object> getChildren()
	{
		return childrenIterable;
	}


	public PyObject __getstate__()
	{
		return new PyTuple( Py.java2py( iso ) );
	}
	
	@SuppressWarnings("unchecked")
	public void __setstate__(PyObject state)
	{
		if ( state instanceof PyTuple )
		{
			PyObject x = ((PyTuple)state).pyget( 0 );
			iso = Py.tojava( x, IsolationBarrier.class );
		}
		else
		{
			throw Py.TypeError( "Pickle state should be a Python tuple" );
		}
	}



	public PyObject getValue()
	{
		return iso.getValue();
	}
	
	
	
	protected IsolationBarrier<PyObject> getIsolationBarrier()
	{
		return iso;
	}
	
	protected void setIsolationBarrier(IsolationBarrier<PyObject> iso)
	{
		this.iso = iso;
	}
}
