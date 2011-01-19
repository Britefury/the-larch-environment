//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ObjectPresentation;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.python.core.PyType;

public class ObjectPresenterRegistry
{
	public static class PresenterNotRegisteredException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public PresenterNotRegisteredException()
		{
			super();
		}
		
		public PresenterNotRegisteredException(String message)
		{
			super( message );
		}
	}
	
	
	private WeakHashMap<ObjectPresentationPerspective, Object> perspectives = new WeakHashMap<ObjectPresentationPerspective, Object>();
	private HashMap<Class<?>, ObjectPresenter> registeredJavaPresenters = new HashMap<Class<?>, ObjectPresenter>();
	private HashMap<PyType, PyObjectPresenter> registeredPythonPresenters = new HashMap<PyType, PyObjectPresenter>();
	private IdentityHashMap<ObjectPresenter, Class<?>> javaPresenterToClass = new IdentityHashMap<ObjectPresenter, Class<?>>();
	private IdentityHashMap<PyObjectPresenter, PyType> pythonPresenterToType = new IdentityHashMap<PyObjectPresenter, PyType>();
	
	
	public ObjectPresenterRegistry()
	{
	}
	
	
	public void registerPerspective(ObjectPresentationPerspective perspective)
	{
		perspectives.put( perspective, null );
		
		for (Map.Entry<Class<?>, ObjectPresenter> entry: registeredJavaPresenters.entrySet())
		{
			perspective.registerJavaObjectPresenter( entry.getKey(), entry.getValue() );
		}
		
		for (Map.Entry<PyType, PyObjectPresenter> entry: registeredPythonPresenters.entrySet())
		{
			perspective.registerPythonObjectPresenter( entry.getKey(), entry.getValue() );
		}
	}
	
	
	
	public ObjectPresenter getPresenter(Class<?> cls)
	{
		return registeredJavaPresenters.get( cls );
	}
	
	public PyObjectPresenter getPresenter(PyType type)
	{
		return registeredPythonPresenters.get( type );
	}
	
	
	public void registerJavaObjectPresenter(Class<?> cls, ObjectPresenter presenter)
	{
		registeredJavaPresenters.put( cls, presenter );
		javaPresenterToClass.put( presenter, cls );
		
		for (ObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.registerJavaObjectPresenter( cls, presenter );
		}
	}
	
	public void registerPythonObjectPresenter(PyType type, PyObjectPresenter presenter)
	{
		registeredPythonPresenters.put( type, presenter );
		pythonPresenterToType.put( presenter, type );
		
		for (ObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.registerPythonObjectPresenter( type, presenter );
		}
	}


	public void unregisterJavaObjectPresenter(Class<?> cls)
	{
		ObjectPresenter presenter = registeredJavaPresenters.get( cls );
		if ( presenter == null )
		{
			throw new PresenterNotRegisteredException( "Presenter not registered for Java class " + cls );
		}
		registeredJavaPresenters.remove( cls );
		javaPresenterToClass.remove( presenter );
		
		for (ObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.unregisterJavaObjectPresenter( cls );
		}
	}
	
	public void unregisterPythonObjectPresenter(PyType type)
	{
		PyObjectPresenter presenter = registeredPythonPresenters.get( type );
		if ( presenter == null )
		{
			throw new PresenterNotRegisteredException( "Presenter not registered for Python type " + type );
		}
		registeredPythonPresenters.remove( type );
		pythonPresenterToType.remove( presenter );

		for (ObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.unregisterPythonObjectPresenter( type );
		}
	}


	public void unregisterJavaObjectPresenter(ObjectPresenter presenter)
	{
		Class<?> cls = javaPresenterToClass.get( presenter );
		if ( cls == null )
		{
			throw new PresenterNotRegisteredException( "Java object presenter not registered" );
		}
		registeredJavaPresenters.remove( cls );
		javaPresenterToClass.remove( presenter );
		
		for (ObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.unregisterJavaObjectPresenter( cls );
		}
	}
	
	public void unregisterPythonObjectPresenter(PyObjectPresenter presenter)
	{
		PyType type = pythonPresenterToType.get( presenter );
		if ( type == null )
		{
			throw new PresenterNotRegisteredException( "Java object presenter not registered" );
		}
		registeredPythonPresenters.remove( type );
		pythonPresenterToType.remove( presenter );
		
		for (ObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.unregisterPythonObjectPresenter( type );
		}
	}
}
