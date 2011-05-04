//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ObjectPresentation;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.core.__builtin__;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.Utils.PolymorphicMap;

public abstract class ObjectPresentationPerspective extends AbstractPerspective
{
	private PyString pythonPresentMethodName;
	private PolymorphicMap<Object> objectPresenters = new PolymorphicMap<Object>();
	protected AbstractPerspective fallbackPerspective;
	
	
	public ObjectPresentationPerspective(String pythonMethodName, AbstractPerspective fallbackPerspective)
	{
		this.pythonPresentMethodName = Py.newString( pythonMethodName.intern() );
		this.fallbackPerspective = fallbackPerspective;
	}

	public ObjectPresentationPerspective(String pythonMethodName)
	{
		this( pythonMethodName, null );
	}

	

	
	protected abstract Pres presentWithJavaInterface(Object x, FragmentView fragment, SimpleAttributeTable inheritedState);
	protected abstract Pres presentJavaArray(Object x, FragmentView fragment, SimpleAttributeTable inheritedState);
	
	
	protected Pres invokeObjectPresenter(ObjectPresenter presenter, Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presenter.presentObject( x, fragment, inheritedState );
	}
	
	protected Pres invokePyObjectPresenter(PyObjectPresenter presenter, PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presenter.presentObject( x, fragment, inheritedState );
	}
	

	@Override
	protected Pres presentModel(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres result = null;
		
		
		PyObject pyX = null;
		
		// Java object presentation protocol - Java interface
		result = presentWithJavaInterface( x, fragment, inheritedState );
		
		// Python object presentation protocol
		if ( result == null  &&  x instanceof PyObject )
		{
			// @x is a Python object - if it offers a __present__ method, use that
			pyX = (PyObject)x;
			PyObject presentMethod = null;
			try
			{
				presentMethod = __builtin__.getattr( pyX, pythonPresentMethodName );
			}
			catch (PyException e)
			{
				presentMethod = null;
			}
			
			if ( presentMethod != null  &&  presentMethod.isCallable() )
			{
				result = Py.tojava( presentMethod.__call__( Py.java2py( fragment ), Py.java2py( inheritedState ) ),  Pres.class );
			}
			
			
			// __present__ did not succeed. Try the registered presenters.
			if ( result == null )
			{
				// Now try Python object presenters
				PyType typeX = pyX.getType();
				
				PyObjectPresenter presenter = (PyObjectPresenter)objectPresenters.get( typeX );
				if ( presenter != null )
				{
					result = invokePyObjectPresenter( presenter, pyX, fragment, inheritedState );
				}
			}
		}
		
		// Java object presentation protocol - array
		if ( result == null  &&  x.getClass().isArray() )
		{
			result = presentJavaArray( x, fragment, inheritedState );
		}
		
		// Java object presentation protocol - registered presenters
		if ( result == null )
		{
			ObjectPresenter presenter = (ObjectPresenter)objectPresenters.get( x.getClass() );
			if ( presenter != null )
			{
				result = invokeObjectPresenter( presenter, x, fragment, inheritedState );
			}
		}
		
		if ( result == null )
		{
			// Fallback
			if ( fallbackPerspective != null )
			{
				return fallbackPerspective.presentObject( x, fragment, inheritedState );
			}
			else
			{
				return null;
			}
		}
		else
		{
			result.setDebugName( x.getClass().getName() );
			return result;
		}
	}

	
	

	public void registerJavaObjectPresenter(Class<?> cls, ObjectPresenter presenter)
	{
		objectPresenters.put( cls, presenter );
	}
	
	public void registerPythonObjectPresenter(PyType type, PyObjectPresenter presenter)
	{
		objectPresenters.put( type, presenter );
	}


	public void unregisterJavaObjectPresenter(Class<?> cls)
	{
		objectPresenters.remove( cls );
	}
	
	public void unregisterPythonObjectPresenter(PyType type)
	{
		objectPresenters.remove( type );
	}
}
