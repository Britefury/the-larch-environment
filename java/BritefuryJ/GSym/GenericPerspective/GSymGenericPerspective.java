//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.GenericPerspective;

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
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.GSymLocationResolver;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GSymGenericPerspective extends GSymAbstractPerspective
{
	private static class GenericPerspectiveLocationResolver implements GSymLocationResolver
	{
		private GSymGenericPerspective perspective;
		
		
		public GenericPerspectiveLocationResolver(GSymGenericPerspective perspective)
		{
			this.perspective = perspective;
		}
		
		
		@Override
		public GSymSubject resolveLocationAsSubject(Location location)
		{
			return perspective.resolveRelativeLocation( null, location.iterator() );
		}
	}
	
	
	private GSymObjectViewLocationTable locationTable = new GSymObjectViewLocationTable();
	private GenericPerspectiveLocationResolver locationResolver = new GenericPerspectiveLocationResolver( this );
	private HashMap<Class<?>, ObjectPresenter> registeredJavaObjectPresenters = new HashMap<Class<?>, ObjectPresenter>();
	private HashMap<Class<?>, ObjectPresenter> javaObjectPresenters = new HashMap<Class<?>, ObjectPresenter>();
	private HashMap<PyType, PyObjectPresenter> registeredPythonObjectPresenters = new HashMap<PyType, PyObjectPresenter>();
	private HashMap<PyType, PyObjectPresenter> pythonObjectPresenters = new HashMap<PyType, PyObjectPresenter>();
	
	
	public GSymGenericPerspective()
	{
		SystemObjectPresenters.registerPresenters( this );
	}

	


	@Override
	public DPElement present(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		DPElement result = null;
		GenericPerspectiveStyleSheet genericStyleSheet = null;
		if ( styleSheet instanceof GenericPerspectiveStyleSheet )
		{
			genericStyleSheet = (GenericPerspectiveStyleSheet)styleSheet;
		}
		else
		{
			genericStyleSheet = GenericPerspectiveStyleSheet.instance;
		}
		
		
		PyObject pyX = null;

		// Java object presentation protocol - Presentable interface
		if ( x instanceof Presentable )
		{
			// @x is an instance of @Presentable; use Presentable#present()
			Presentable p = (Presentable)x;
			result = p.present( ctx, genericStyleSheet, state );
		}
		
		// Python object presentation protocol
		if ( result == null  &&  x instanceof PyObject )
		{
			// @x is a Python object - if it offers a __present__ method, use that
			pyX = (PyObject)x;
			PyObject __present__ = null;
			try
			{
				__present__ = pyX.__getattr__( "__present__" );
			}
			catch (PyException e)
			{
				__present__ = null;
			}
			
			if ( __present__ != null  &&  __present__.isCallable() )
			{
				result = Py.tojava( __present__.__call__( Py.java2py( ctx ), Py.java2py( styleSheet ), Py.java2py( state ) ),  DPElement.class );
			}
			
			
			// __present__ did not succeed. Try the registered presenters.
			if ( result == null )
			{
				// Now try Python object presenters
				PyType typeX = pyX.getType();
				
				PyObjectPresenter presenter = getPresenterForPythonType( typeX );
				if ( presenter != null )
				{
					result = presenter.presentObject( pyX, ctx, genericStyleSheet, state );
				}
			}
		}
		
		// Java object presentation protocol - registered presenters
		if ( result == null )
		{
			ObjectPresenter presenter = getPresenterForJavaObject( x );
			if ( presenter != null )
			{
				result = presenter.presentObject( x, ctx, genericStyleSheet, state );
			}
		}
		
		// Fallback - use Java or Python toString() / __str__() methods
		if ( result == null )
		{
			if ( pyX != null )
			{
				result = presentPythonObjectAsString( pyX, ctx, genericStyleSheet, state );
			}
			else
			{
				result = presentJavaObjectAsString( x, ctx, genericStyleSheet, state );
			}
		}
		
		result.setDebugName( x.getClass().getName() );
		return result;
	}

	
	
	@Override
	public StyleSheet getStyleSheet()
	{
		return GenericPerspectiveStyleSheet.instance;
	}
	
	@Override
	public AttributeTable getInitialInheritedState()
	{
		return AttributeTable.instance;
	}
	
	@Override
	public EditHandler getEditHandler()
	{
		return null;
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



	public Location getLocationForObject(Object x)
	{
		return locationTable.getLocationForObject( x );
	}
	
	public Object getObjectAtLocation(Location location)
	{
		return locationTable.getObjectAtLocation( location.iterator() );
	}
	
	
	public GSymLocationResolver getLocationResolver()
	{
		return locationResolver;
	}
	
	
	
	public void registerJavaObjectPresenter(Class<?> cls, ObjectPresenter presenter)
	{
		registeredJavaObjectPresenters.put( cls, presenter );
		javaObjectPresenters.clear();
	}
	
	public void registerPythonObjectPresenter(PyType type, PyObjectPresenter presenter)
	{
		registeredPythonObjectPresenters.put( type, presenter );
		pythonObjectPresenters.clear();
	}
	
	
	private ObjectPresenter getPresenterForJavaObject(Object x)
	{
		// If the list of java object presenters is empty, but the registered list is not, then copy
		if ( javaObjectPresenters.isEmpty()  &&  !registeredJavaObjectPresenters.isEmpty() )
		{
			javaObjectPresenters.putAll( registeredJavaObjectPresenters );
		}
		Class<?> xClass = x.getClass();
		
		// See if we have a presenter
		ObjectPresenter presenter = registeredJavaObjectPresenters.get( xClass );
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
	
	
	private PyObjectPresenter getPresenterForPythonType(PyType typeX)
	{
		// If the list of python object presenters is empty, but the registered list is not, then copy
		if ( pythonObjectPresenters.isEmpty()  &&  !registeredPythonObjectPresenters.isEmpty() )
		{
			pythonObjectPresenters.putAll( registeredPythonObjectPresenters );
		}

		// See if we have a presenter
		PyObjectPresenter presenter = registeredPythonObjectPresenters.get( typeX );
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
	
	
	private static DPElement presentJavaObjectAsString(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
	{
		return styleSheet.objectBox( x.getClass().getName(), asStringStyle.staticText( x.toString() ) );
	}
	
	private static DPElement presentPythonObjectAsString(PyObject pyX, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
	{
		PyType typeX = pyX.getType();
		return styleSheet.objectBox( typeX.getName(), asStringStyle.staticText( pyX.toString() ) );
	}
	
	
	
	
	
	private static final PrimitiveStyleSheet asStringStyle = PrimitiveStyleSheet.instance.withFontItalic( true ).withFontSize( 14 );
}
