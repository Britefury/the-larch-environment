//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ObjectPresentation;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyMethod;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.core.__builtin__;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Projection.TransientSubject;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.Projection.Subject;
import BritefuryJ.Util.PolymorphicMap;

public abstract class ObjectPresentationPerspective extends AbstractPerspective
{
	private class ObjectPresentationSubject extends TransientSubject
	{
		private Object focus;
		private String title, trailText;
		
		
		public ObjectPresentationSubject(Object focus, String title, String trailText)
		{
			super( null );
			this.focus = focus;
			this.title = title;
			this.trailText = trailText;
		}

		public ObjectPresentationSubject(Object focus, String title)
		{
			this(focus, title, title);
		}

		public ObjectPresentationSubject(Object focus)
		{
			this(focus, focus != null  ?  focus.getClass().getName()  :  "<null>");
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

		@Override
		public String getTrailLinkText()
		{
			return trailText;
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

	public Subject objectSubject(Object o, String title, String trailText)
	{
		return new ObjectPresentationSubject( o, title, trailText );
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
				// If @presentMethod is an unbound method, we cannot invoke it, since we do not have
				// an instance
				if ( presentMethod instanceof PyMethod )
				{
					PyMethod m = (PyMethod)presentMethod;
					if ( m.__self__ == Py.None  ||  m.__self__ == null )
					{
						presentMethod = null;
					}
				}
				
				if ( presentMethod != null )
				{
					result = Py.tojava( presentMethod.__call__( Py.java2py( fragment ), Py.java2py( inheritedState ) ),  Pres.class );
					if ( result != null )
					{
						typeName = pyX.getType().getName();
					}
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
