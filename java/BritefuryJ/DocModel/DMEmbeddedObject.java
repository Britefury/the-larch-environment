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
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.JythonInterface.Jython_copy;
import BritefuryJ.Pres.Pres;

public class DMEmbeddedObject extends DMNode implements DMEmbeddedPyObjectInterface, Presentable, Trackable
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

	
	
	private static final long serialVersionUID = 1L;
	
	protected static DMNodeClass embeddedObjectNodeClass = new DMNodeClass( "DMEmbeddedObject" );


	private PyObject value = null;
	private ChangeHistory changeHistory = null;
	
	
	public DMEmbeddedObject()
	{
		super();
	}
	
	public DMEmbeddedObject(PyObject value)
	{
		super();
		this.value = value;
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
				
				if ( value != null  &&  e.value != null )
				{
					return value.equals( e.value );
				}
				else
				{
					return ( value != null ) == ( e.value != null );
				}
			}
			
			return false;
		}
	}
	
	public int hashCode()
	{
		return value != null  ?  value.hashCode()  :  0;
	}
	
	
	@Override
	public void become(Object x)
	{
		if ( x instanceof DMEmbeddedObject )
		{
			DMEmbeddedObject em = (DMEmbeddedObject)x;
			this.value = em.value;
			
			// We could remove this exception - just include an incremental monitor (as with DMList), and notify it when the value is accessed and modified
			throw new RuntimeException( "DMEmbeddedObject does not support become()" );
		}
		else
		{
			throw new CannotChangeNodeClassException( x.getClass(), getClass() );
		}
	}

	@Override
	protected Object createDeepCopy(PyDictionary memo)
	{
		if ( value != null )
		{
			return new DMEmbeddedObject( Jython_copy.deepcopy( value ) );
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
		return new PyTuple( value );
	}
	
	public void __setstate__(PyObject state)
	{
		if ( state instanceof PyTuple )
		{
			value = ((PyTuple)state).pyget( 0 );
		}
		else
		{
			throw Py.TypeError( "Pickle state should be a Python tuple" );
		}
	}



	public PyObject getValue()
	{
		return value;
	}

	
	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return DocModelPresenter.presentDMEmbeddedObject( this, fragment, inheritedState );
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
		return Arrays.asList( new Object[] { value } );
	}
}
