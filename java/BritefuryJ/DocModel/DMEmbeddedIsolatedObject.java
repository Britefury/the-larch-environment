//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Isolation.IsolationBarrier;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Util.Jython.Jython_copy;

public class DMEmbeddedIsolatedObject extends DMNode implements DMEmbeddedPyObjectInterface, Presentable, Trackable
{
	protected static class ChildrenIterator implements Iterator<Object>
	{
		public boolean hasNext()
		{
			return false;
		}

		public Object next()
		{
			throw new NoSuchElementException();
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	protected static class ChildrenIterable implements Iterable<Object>
	{
		private static ChildrenIterator iter = new ChildrenIterator();
		
		public Iterator<Object> iterator()
		{
			return iter;
		}
	}
	
	protected static ChildrenIterable childrenIterable = new ChildrenIterable();

	
	
	protected static DMNodeClass embeddedIsolatedObjectNodeClass = new DMNodeClass( "DMEmbeddedIsolatedObject" );


	private IsolationBarrier<PyObject> iso = null;
	private ChangeHistory changeHistory = null;
	
	
	public DMEmbeddedIsolatedObject()
	{
		super();
	}
	
	public DMEmbeddedIsolatedObject(PyObject value)
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
			if ( x instanceof DMEmbeddedIsolatedObject )
			{
				DMEmbeddedIsolatedObject e = (DMEmbeddedIsolatedObject)x;
				
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
		if ( x instanceof DMEmbeddedIsolatedObject )
		{
			DMEmbeddedIsolatedObject em = (DMEmbeddedIsolatedObject)x;
			this.iso = em.iso;

			// We could remove this exception - just include an incremental monitor (as with DMList), and notify it when the value is accessed and modified
			throw new RuntimeException( "DMEmbeddedObjectIsolated does not support become()" );
		}
		else
		{
			throw new CannotChangeNodeClassException( x.getClass(), getClass() );
		}
	}

	@Override
	protected Object createDeepCopy(PyDictionary memo)
	{
		if ( iso != null )
		{
			PyObject valueCopy = Jython_copy.deepcopy( iso.getValue(), memo );
			return new DMEmbeddedIsolatedObject( valueCopy );
		}
		else
		{
			return new DMEmbeddedIsolatedObject();
		}
	}
	
	@Override
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		if ( iso != null )
		{
			return new DMEmbeddedIsolatedObject( memo.copy( iso.getValue() ) );
		}
		else
		{
			return new DMEmbeddedIsolatedObject();
		}
	}

	@Override
	public DMNodeClass getDMNodeClass()
	{
		return embeddedIsolatedObjectNodeClass;
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
			PyTuple tupleState = (PyTuple)state;
			PyObject x = tupleState.pyget( 0 );
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

	
	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return DocModelPresenter.presentDMEmbeddedIsolatedObject( this, fragment, inheritedState );
	}

	
	
	public ChangeHistory getChangeHistory()
	{
		return changeHistory;
	}

	public void setChangeHistory(ChangeHistory h)
	{
		changeHistory = h;
	}

	public List<Object> getTrackableContents()
	{
		return Arrays.asList( new Object[] { iso } );
	}
}
