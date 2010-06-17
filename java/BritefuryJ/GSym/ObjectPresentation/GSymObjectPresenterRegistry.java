//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectPresentation;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.python.core.PyType;

import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class GSymObjectPresenterRegistry
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
	
	
	private WeakHashMap<GSymObjectPresentationPerspective, Object> perspectives = new WeakHashMap<GSymObjectPresentationPerspective, Object>();
	private HashMap<Class<?>, ObjectPresenter<? extends StyleSheet>> registeredJavaPresenters = new HashMap<Class<?>, ObjectPresenter<? extends StyleSheet>>();
	private HashMap<PyType, PyObjectPresenter<? extends StyleSheet>> registeredPythonPresenters = new HashMap<PyType, PyObjectPresenter<? extends StyleSheet>>();
	private IdentityHashMap<ObjectPresenter<? extends StyleSheet>, Class<?>> javaPresenterToClass = new IdentityHashMap<ObjectPresenter<? extends StyleSheet>, Class<?>>();
	private IdentityHashMap<PyObjectPresenter<? extends StyleSheet>, PyType> pythonPresenterToType = new IdentityHashMap<PyObjectPresenter<? extends StyleSheet>, PyType>();
	
	
	public GSymObjectPresenterRegistry()
	{
	}
	
	
	public void registerPerspective(GSymObjectPresentationPerspective perspective)
	{
		perspectives.put( perspective, null );
		
		for (Map.Entry<Class<?>, ObjectPresenter<? extends StyleSheet>> entry: registeredJavaPresenters.entrySet())
		{
			perspective.registerJavaObjectPresenter( entry.getKey(), entry.getValue() );
		}
		
		for (Map.Entry<PyType, PyObjectPresenter<? extends StyleSheet>> entry: registeredPythonPresenters.entrySet())
		{
			perspective.registerPythonObjectPresenter( entry.getKey(), entry.getValue() );
		}
	}
	
	
	
	public ObjectPresenter<? extends StyleSheet> getPresenter(Class<?> cls)
	{
		return registeredJavaPresenters.get( cls );
	}
	
	public PyObjectPresenter<? extends StyleSheet> getPresenter(PyType type)
	{
		return registeredPythonPresenters.get( type );
	}
	
	
	public void registerJavaObjectPresenter(Class<?> cls, ObjectPresenter<? extends StyleSheet> presenter)
	{
		registeredJavaPresenters.put( cls, presenter );
		javaPresenterToClass.put( presenter, cls );
		
		for (GSymObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.registerJavaObjectPresenter( cls, presenter );
		}
	}
	
	public void registerPythonObjectPresenter(PyType type, PyObjectPresenter<? extends StyleSheet> presenter)
	{
		registeredPythonPresenters.put( type, presenter );
		pythonPresenterToType.put( presenter, type );
		
		for (GSymObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.registerPythonObjectPresenter( type, presenter );
		}
	}


	public void unregisterJavaObjectPresenter(Class<?> cls)
	{
		ObjectPresenter<? extends StyleSheet> presenter = registeredJavaPresenters.get( cls );
		if ( presenter == null )
		{
			throw new PresenterNotRegisteredException( "Presenter not registered for Java class " + cls );
		}
		registeredJavaPresenters.remove( cls );
		javaPresenterToClass.remove( presenter );
		
		for (GSymObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.unregisterJavaObjectPresenter( cls );
		}
	}
	
	public void unregisterPythonObjectPresenter(PyType type)
	{
		PyObjectPresenter<? extends StyleSheet> presenter = registeredPythonPresenters.get( type );
		if ( presenter == null )
		{
			throw new PresenterNotRegisteredException( "Presenter not registered for Python type " + type );
		}
		registeredPythonPresenters.remove( type );
		pythonPresenterToType.remove( presenter );

		for (GSymObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.unregisterPythonObjectPresenter( type );
		}
	}


	public void unregisterJavaObjectPresenter(ObjectPresenter<? extends StyleSheet> presenter)
	{
		Class<?> cls = javaPresenterToClass.get( presenter );
		if ( cls == null )
		{
			throw new PresenterNotRegisteredException( "Java object presenter not registered" );
		}
		registeredJavaPresenters.remove( cls );
		javaPresenterToClass.remove( presenter );
		
		for (GSymObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.unregisterJavaObjectPresenter( cls );
		}
	}
	
	public void unregisterPythonObjectPresenter(PyObjectPresenter<? extends StyleSheet> presenter)
	{
		PyType type = pythonPresenterToType.get( presenter );
		if ( type == null )
		{
			throw new PresenterNotRegisteredException( "Java object presenter not registered" );
		}
		registeredPythonPresenters.remove( type );
		pythonPresenterToType.remove( presenter );
		
		for (GSymObjectPresentationPerspective perspective: perspectives.keySet())
		{
			perspective.unregisterPythonObjectPresenter( type );
		}
	}
}
