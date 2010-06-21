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

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
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

	

	
	protected abstract DPElement presentWithJavaInterface(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	protected abstract DPElement presentJavaObjectFallback(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	protected abstract DPElement presentPyObjectFallback(PyObject x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	protected abstract DPElement invokeObjectPresenter(ObjectPresenter<? extends StyleSheet> presenter, Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	protected abstract DPElement invokePyObjectPresenter(PyObjectPresenter<? extends StyleSheet> presenter, PyObject x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state);
	

	@SuppressWarnings("unchecked")
	@Override
	public DPElement present(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		DPElement result = null;
		
		
		PyObject pyX = null;

		// Java object presentation protocol - Java interface
		result = presentWithJavaInterface( x, ctx, styleSheet, inheritedState );
		
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
				result = Py.tojava( presentMethod.__call__( Py.java2py( ctx ), Py.java2py( styleSheet ), Py.java2py( inheritedState ) ),  DPElement.class );
			}
			
			
			// __present__ did not succeed. Try the registered presenters.
			if ( result == null )
			{
				// Now try Python object presenters
				PyType typeX = pyX.getType();
				
				PyObjectPresenter<? extends StyleSheet> presenter = (PyObjectPresenter<? extends StyleSheet>)objectPresenters.get( typeX );
				if ( presenter != null )
				{
					result = invokePyObjectPresenter( presenter, pyX, ctx, styleSheet, inheritedState );
				}
			}
		}
		
		// Java object presentation protocol - registered presenters
		if ( result == null )
		{
			ObjectPresenter<? extends StyleSheet> presenter = (ObjectPresenter<? extends StyleSheet>)objectPresenters.get( x.getClass() );
			if ( presenter != null )
			{
				result = invokeObjectPresenter( presenter, x, ctx, styleSheet, inheritedState );
			}
		}
		
		// Fallback - use Java or Python toString() / __str__() methods
		if ( result == null )
		{
			if ( pyX != null )
			{
				result = presentPyObjectFallback( pyX, ctx, styleSheet, inheritedState );
			}
			else
			{
				result = presentJavaObjectFallback( x, ctx, styleSheet, inheritedState );
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
		objectPresenters.put( cls, presenter );
	}
	
	public void registerPythonObjectPresenter(PyType type, PyObjectPresenter<? extends StyleSheet> presenter)
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
