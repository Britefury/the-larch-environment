//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectPresentation;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Utils.PolymorphicMap;

public abstract class GSymObjectPresentationPerspective extends GSymAbstractPerspective
{
	private String pythonPresentMethodName;
	private GSymObjectViewLocationTable locationTable = new GSymObjectViewLocationTable();
	private PolymorphicMap<Object> objectPresenters = new PolymorphicMap<Object>();
	
	
	public GSymObjectPresentationPerspective(String pythonMethodName, ObjectPresentationLocationResolver objPresLocationResolver)
	{
		this.pythonPresentMethodName = pythonMethodName.intern();
		objPresLocationResolver.registerPerspective( this );
	}

	

	
	protected abstract Pres presentWithJavaInterface(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState);
	protected abstract Pres presentJavaObjectFallback(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState);
	protected abstract Pres presentPyObjectFallback(PyObject x, GSymFragmentView fragment, SimpleAttributeTable inhritedState);
	protected abstract Pres invokeObjectPresenter(ObjectPresenter presenter, Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState);
	protected abstract Pres invokePyObjectPresenter(PyObjectPresenter presenter, PyObject x, GSymFragmentView fragment, SimpleAttributeTable inheritedState);
	

	@Override
	public Pres present(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres result = null;
		
		
		PyObject pyX = null;
		
		// First, see if is a subclass of Pres, if so, just return it:
		if ( x instanceof Pres )
		{
			return (Pres)x;
		}

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
				presentMethod = pyX.__getattr__( pythonPresentMethodName );
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
		
		// Java object presentation protocol - registered presenters
		if ( result == null )
		{
			ObjectPresenter presenter = (ObjectPresenter)objectPresenters.get( x.getClass() );
			if ( presenter != null )
			{
				result = invokeObjectPresenter( presenter, x, fragment, inheritedState );
			}
		}
		
		// Fallback - use Java or Python toString() / __str__() methods
		if ( result == null )
		{
			if ( pyX != null )
			{
				result = presentPyObjectFallback( pyX, fragment, inheritedState );
			}
			else
			{
				result = presentJavaObjectFallback( x, fragment, inheritedState );
			}
		}
		
		result.setDebugName( x.getClass().getName() );
		return result;
	}

	
	
	public GSymSubject resolveRelativeLocation(GSymSubject enclosingSubject, Location.TokenIterator relativeLocation)
	{
		Object x = locationTable.getObjectAtLocation( relativeLocation );
		if ( x != null )
		{
			String title = x != null  ?  x.getClass().getName()  :  "<null>";
			return new GSymSubject( x, this, title, SimpleAttributeTable.instance, null );
		}
		else
		{
			return null;
		}
	}



	protected String getRelativeLocationForObject(Object x)
	{
		return locationTable.getLocationForObject( x );
	}
	
	protected Object getObjectAtRelativeLocation(Location.TokenIterator relativeLocation)
	{
		return locationTable.getObjectAtLocation( relativeLocation );
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
