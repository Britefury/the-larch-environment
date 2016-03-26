//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
	
	protected PyObject py_copyWithJavaInterface(Object jx, ClipboardCopierMemo memo)
	{
		if ( jx instanceof ClipboardCopyable )
		{
			ClipboardCopyable c = (ClipboardCopyable)jx;
			return Py.java2py( c.clipboardCopy( memo ) );
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
	
	protected PyObject py_defaultCopy(PyObject x)
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
	
	public Object __call__(Object x)
	{
		return copy( x );
	}
	
	public Object __call__(Object x, ClipboardCopierMemo memo)
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
	

	protected PyObject py_createCopy(PyObject px, ClipboardCopierMemo memo)
	{
		if ( px == null  ||  px == Py.None )
		{
			return px;
		}
		
		PyObject result = null;
		
		// Convert to a Java object  -  needed so that we can try the Java object protocols, for instances
		// where x is a wrapped Java object
		Object jx = Py.tojava( px, Object.class );
		
		// Java object clipboard copy protocol - Java interface
		result = py_copyWithJavaInterface( jx, memo );
		
		// Python object clipboard copy protocol
		if ( result == null )
		{
			// @x is a Python object - if it offers a copying method, use that
			PyObject copyMethod = null;
			try
			{
				copyMethod = __builtin__.getattr( px, pythonCopyMethodName );
			}
			catch (PyException e)
			{
				copyMethod = null;
			}
			
			if ( copyMethod != null  &&  copyMethod.isCallable() )
			{
				result = copyMethod.__call__( Py.java2py( memo ) );
			}
			
			
			// Presentation method did not succeed. Try the registered copiers.
			if ( result == null )
			{
				// Now try Python object copiers
				PyType typeX = px.getType();
				
				PyObjectClipboardCopier copier = (PyObjectClipboardCopier)objectCopiers.get( typeX );
				if ( copier != null )
				{
					result = invokePyObjectCopier( copier, px, memo );
				}
			}
		}
		
		// Java object clipboard copy protocol - registered copiers
		if ( result == null )
		{
			ObjectClipboardCopier copier = (ObjectClipboardCopier)objectCopiers.get( jx.getClass() );
			if ( copier != null )
			{
				result = Py.java2py( invokeObjectCopier( copier, jx, memo ) );
			}
		}
		
		if ( result == null )
		{
			// Fallback
			return py_defaultCopy( px );
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
