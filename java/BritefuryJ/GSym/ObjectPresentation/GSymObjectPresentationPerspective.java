//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectPresentation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class GSymObjectPresentationPerspective extends GSymAbstractPerspective
{
	private String pythonPresentMethodName;
	private GSymObjectViewLocationTable locationTable = new GSymObjectViewLocationTable();
	private HashMap<Class<?>, ObjectPresenter<?>> registeredJavaObjectPresenters = new HashMap<Class<?>, ObjectPresenter<?>>();
	private HashMap<Class<?>, ObjectPresenter<?>> javaObjectPresenters = new HashMap<Class<?>, ObjectPresenter<?>>();
	private HashMap<PyType, PyObjectPresenter<?>> registeredPythonObjectPresenters = new HashMap<PyType, PyObjectPresenter<?>>();
	private HashMap<PyType, PyObjectPresenter<?>> pythonObjectPresenters = new HashMap<PyType, PyObjectPresenter<?>>();
	
	
	public GSymObjectPresentationPerspective(String pythonMethodName, ObjectPresentationLocationResolver objPresLocationResolver)
	{
		this.pythonPresentMethodName = pythonMethodName.intern();
		objPresLocationResolver.registerPerspective( this );
	}

	

	
	protected abstract DPElement presentWithJavaInterface(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	protected abstract DPElement presentJavaObjectFallback(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	protected abstract DPElement presentPyObjectFallback(PyObject x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	protected abstract DPElement invokeObjectPresenter(ObjectPresenter<? extends StyleSheet> presenter, Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	protected abstract DPElement invokePyObjectPresenter(PyObjectPresenter<? extends StyleSheet> presenter, PyObject x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	

	@Override
	public DPElement present(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		DPElement result = null;
		
		
		PyObject pyX = null;

		// Java object presentation protocol - Java interface
		result = presentWithJavaInterface( x, ctx, styleSheet, state );
		
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
				result = Py.tojava( presentMethod.__call__( Py.java2py( ctx ), Py.java2py( styleSheet ), Py.java2py( state ) ),  DPElement.class );
			}
			
			
			// __present__ did not succeed. Try the registered presenters.
			if ( result == null )
			{
				// Now try Python object presenters
				PyType typeX = pyX.getType();
				
				PyObjectPresenter<? extends StyleSheet> presenter = getPresenterForPythonType( typeX );
				if ( presenter != null )
				{
					result = invokePyObjectPresenter( presenter, pyX, ctx, styleSheet, state );
				}
			}
		}
		
		// Java object presentation protocol - registered presenters
		if ( result == null )
		{
			ObjectPresenter<? extends StyleSheet> presenter = getPresenterForJavaObject( x );
			if ( presenter != null )
			{
				result = invokeObjectPresenter( presenter, x, ctx, styleSheet, state );
			}
		}
		
		// Fallback - use Java or Python toString() / __str__() methods
		if ( result == null )
		{
			if ( pyX != null )
			{
				result = presentPyObjectFallback( pyX, ctx, styleSheet, state );
			}
			else
			{
				result = presentJavaObjectFallback( x, ctx, styleSheet, state );
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
			return new GSymSubject( x, this, title, AttributeTable.instance, null );
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
	
	
	
	public void registerJavaObjectPresenter(Class<?> cls, ObjectPresenter<? extends StyleSheet> presenter)
	{
		registeredJavaObjectPresenters.put( cls, presenter );
		javaObjectPresenters.clear();
	}
	
	public void registerPythonObjectPresenter(PyType type, PyObjectPresenter<? extends StyleSheet> presenter)
	{
		registeredPythonObjectPresenters.put( type, presenter );
		pythonObjectPresenters.clear();
	}
	
	
	private ObjectPresenter<? extends StyleSheet> getPresenterForJavaObject(Object x)
	{
		// If the list of java object presenters is empty, but the registered list is not, then copy
		if ( javaObjectPresenters.isEmpty()  &&  !registeredJavaObjectPresenters.isEmpty() )
		{
			javaObjectPresenters.putAll( registeredJavaObjectPresenters );
		}
		Class<?> xClass = x.getClass();
		
		// See if we have a presenter
		ObjectPresenter<? extends StyleSheet> presenter = registeredJavaObjectPresenters.get( xClass );
		if ( presenter != null )
		{
			return presenter;
		}
		
		// No, we don't
		if ( xClass != Object.class )
		{
			// The class of x is a subclass of Object
			Class<?> superClass = xClass.getSuperclass();
			
			while ( superClass != Object.class )
			{
				// See if we can get a presenter for this superclass
				presenter = javaObjectPresenters.get( superClass );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					javaObjectPresenters.put( xClass, presenter );
					return presenter;
				}
				
				// Try the next class up the hierarchy
				superClass = superClass.getSuperclass();
			}
		}
		
		// Now check the interfaces
		
		// First, build a list of all interfaces implemented by x, and its superclasses.
		Class<?> c = xClass;
		HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
		Stack<Class<?>> interfaceStack = new Stack<Class<?>>();
		while ( c != Object.class )
		{
			for (Class<?> iface: c.getInterfaces())
			{
				presenter = javaObjectPresenters.get( iface );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					javaObjectPresenters.put( xClass, presenter );
					return presenter;
				}
				if ( !interfaces.contains( iface ) )
				{
					interfaces.add( iface );
					interfaceStack.add( iface );
				}
			}
			c = c.getSuperclass();
		}
		
		// Now check the super interfaces
		while ( !interfaceStack.empty() )
		{
			Class<?> interfaceFromStack = interfaceStack.pop();
			for (Class<?> superInterface: interfaceFromStack.getInterfaces())
			{
				presenter = javaObjectPresenters.get( superInterface );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					javaObjectPresenters.put( xClass, presenter );
					return presenter;
				}
				if ( !interfaces.contains( superInterface ) )
				{
					interfaces.add( superInterface );
					interfaceStack.add( superInterface );
				}
			}
		}
		
		return null;
	}
	
	
	private PyObjectPresenter<? extends StyleSheet> getPresenterForPythonType(PyType typeX)
	{
		// If the list of python object presenters is empty, but the registered list is not, then copy
		if ( pythonObjectPresenters.isEmpty()  &&  !registeredPythonObjectPresenters.isEmpty() )
		{
			pythonObjectPresenters.putAll( registeredPythonObjectPresenters );
		}

		// See if we have a presenter
		PyObjectPresenter<? extends StyleSheet> presenter = registeredPythonObjectPresenters.get( typeX );
		if ( presenter != null )
		{
			return presenter;
		}
		
		// No, we don't
		PyTuple mro = typeX.getMro();
		
		for (PyObject t: mro.getArray())
		{
			PyType superType = (PyType)t;
			
			// See if we can get a presenter for this superclass
			presenter = pythonObjectPresenters.get( superType );
			if ( presenter != null )
			{
				// Yes - cache it for future tests
				pythonObjectPresenters.put( typeX, presenter );
				return presenter;
			}
		}

		return null;
	}
}
