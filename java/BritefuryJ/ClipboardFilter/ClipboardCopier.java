//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.ClipboardFilter;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.core.__builtin__;

import BritefuryJ.Util.PolymorphicMap;

public class ClipboardCopier
{
	private PyString pythonCopyMethodName;
	private PolymorphicMap<Object> objectCopiers = new PolymorphicMap<Object>();
	
	
	protected ClipboardCopier()
	{
		this.pythonCopyMethodName = Py.newString( "__clipboard_copy__".intern() );
	}

	

	
	protected Object copyWithJavaInterface(Object x, ClipboardCopierMemo memo)
	{
		if ( x instanceof ClipboardCopyable )
		{
			ClipboardCopyable c = (ClipboardCopyable)x;
			return c.clipboardCopy( memo );
		}
		else
		{
			return null;
		}
	}
	
	
	protected Object invokeObjectCopier(ObjectClipboardCopier copier, Object x, ClipboardCopierMemo memo)
	{
		return copier.copyObject( x, memo );
	}
	
	protected PyObject invokePyObjectCopier(PyObjectClipboardCopier copier, PyObject x, ClipboardCopierMemo memo)
	{
		return copier.copyObject( x, memo );
	}
	
	
	protected Object defaultCopy(Object x)
	{
		return x;
	}
	
	
	public ClipboardCopierMemo memo()
	{
		return new ClipboardCopierMemo( this );
	}

	public Object copy(Object x)
	{
		return memo().copy( x );
	}
	
	public Object copy(Object x, ClipboardCopierMemo memo)
	{
		return memo.copy( x );
	}
	
	protected Object createCopy(Object x, ClipboardCopierMemo memo)
	{
		if ( x == null )
		{
			return null;
		}
		
		Object result = null;
		
		
		PyObject pyX = null;
		
		// Java object clipboard copy protocol - Java interface
		result = copyWithJavaInterface( x, memo );
		
		// Python object clipboard copy protocol
		if ( result == null  &&  x instanceof PyObject )
		{
			// @x is a Python object - if it offers a copying method, use that
			pyX = (PyObject)x;
			PyObject copyMethod = null;
			try
			{
				copyMethod = __builtin__.getattr( pyX, pythonCopyMethodName );
			}
			catch (PyException e)
			{
				copyMethod = null;
			}
			
			if ( copyMethod != null  &&  copyMethod.isCallable() )
			{
				result = Py.tojava( copyMethod.__call__( Py.java2py( memo ) ),  Object.class );
			}
			
			
			// Presentation method did not succeed. Try the registered copiers.
			if ( result == null )
			{
				// Now try Python object copiers
				PyType typeX = pyX.getType();
				
				PyObjectClipboardCopier copier = (PyObjectClipboardCopier)objectCopiers.get( typeX );
				if ( copier != null )
				{
					result = Py.tojava( invokePyObjectCopier( copier, pyX, memo ),  Object.class );
				}
			}
		}
		
		// Java object clipboard copy protocol - registered copiersw
		if ( result == null )
		{
			ObjectClipboardCopier copier = (ObjectClipboardCopier)objectCopiers.get( x.getClass() );
			if ( copier != null )
			{
				result = invokeObjectCopier( copier, x, memo );
			}
		}
		
		if ( result == null )
		{
			// Fallback
			return defaultCopy( x );
		}
		else
		{
			return result;
		}
	}
	

	protected PyObject py_createCopy(PyObject x, ClipboardCopierMemo memo)
	{
		PyObject result = null;
		
		
		// Python object clipboard copy protocol
		// @x is a Python object - if it offers a copying method, use that
		PyObject copyMethod = null;
		try
		{
			copyMethod = __builtin__.getattr( x, pythonCopyMethodName );
		}
		catch (PyException e)
		{
			copyMethod = null;
		}
		
		if ( copyMethod != null  &&  copyMethod.isCallable() )
		{
			result = copyMethod.__call__( Py.java2py( memo ) );
		}
		
		
		// Copy method did not succeed. Try the registered copiers.
		if ( result == null )
		{
			PyType typeX = x.getType();
			
			PyObjectClipboardCopier copier = (PyObjectClipboardCopier)objectCopiers.get( typeX );
			if ( copier != null )
			{
				result = invokePyObjectCopier( copier, x, memo );
			}
		}
		
		if ( result == null )
		{
			// Fallback
			return (PyObject)defaultCopy( x );
		}
		else
		{
			return result;
		}
	}
	

	public void registerJavaObjectCopier(Class<?> cls, ObjectClipboardCopier copier)
	{
		objectCopiers.put( cls, copier );
	}
	
	public void registerPythonObjectCopier(PyType type, PyObjectClipboardCopier copier)
	{
		objectCopiers.put( type, copier );
	}


	public void registerPythonObjectCopier_copy(PyType type)
	{
		throw new RuntimeException( "Not implemented - need to implement Python copy for ClipboardCopier" );
	}

	public void registerPythonObjectCopier_deepcopy(PyType type)
	{
		throw new RuntimeException( "Not implemented - need to implement Python deepcopy for ClipboardCopier" );
	}


	public void unregisterJavaObjectExporter(Class<?> cls)
	{
		objectCopiers.remove( cls );
	}
	
	public void unregisterPythonObjectExporter(PyType type)
	{
		objectCopiers.remove( type );
	}
	
	
	
	public static final ClipboardCopier instance = new ClipboardCopier();
}
