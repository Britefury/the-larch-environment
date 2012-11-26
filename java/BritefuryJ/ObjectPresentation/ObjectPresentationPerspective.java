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
import BritefuryJ.Projection.Subject;
import BritefuryJ.Util.PolymorphicMap;

public abstract class ObjectPresentationPerspective extends AbstractPerspective
{
	private class ObjectPresentationSubject extends Subject
	{
		private Object focus;
		private String title;
		
		
		public ObjectPresentationSubject(Object focus, String title)
		{
			super( null );
			this.focus = focus;
			this.title = title;
		}
		
		public ObjectPresentationSubject(Object focus)
		{
			super( null );
			this.focus = focus;
			this.title = focus != null  ?  focus.getClass().getName()  :  "<null>";
		}
		

		@Override
		public Object getFocus()
		{
			return focus;
		}
		
		@Override
		public AbstractPerspective getPerspective()
		{
			return ObjectPresentationPerspective.this;
		}

		@Override
		public String getTitle()
		{
			return title;
		}
	}
	
	
	
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
	
	
	
	public Subject objectSubject(Object o)
	{
		return new ObjectPresentationSubject( o );
	}

	public Subject objectSubject(Object o, String title)
	{
		return new ObjectPresentationSubject( o, title );
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
		String typeName = null;
		
		
		PyObject pyX = null;
		
		// Java object presentation protocol - Java interface
		result = presentWithJavaInterface( x, fragment, inheritedState );
		if ( result != null )
		{
			typeName = x.getClass().getName();
		}
		
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
				if ( result != null )
				{
					typeName = pyX.getType().getName();
				}
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
				if ( result != null )
				{
					typeName = pyX.getType().getName();
				}
			}
		}
		
		// Java object presentation protocol - array
		if ( result == null  &&  x.getClass().isArray() )
		{
			result = presentJavaArray( x, fragment, inheritedState );
			typeName = "[" + x.getClass().getComponentType().getName() + "]";
		}
		
		// Java object presentation protocol - registered presenters
		if ( result == null )
		{
			ObjectPresenter presenter = (ObjectPresenter)objectPresenters.get( x.getClass() );
			if ( presenter != null )
			{
				result = invokeObjectPresenter( presenter, x, fragment, inheritedState );
			}
			if ( result != null )
			{
				typeName = x.getClass().getName();
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
			if ( typeName != null )
			{
				return result.setDebugName( typeName );
			}
			else
			{
				return result;
			}
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
