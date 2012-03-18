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

public abstract class ClipboardExporter
{
	private PyString pythonExportMethodName;
	private PolymorphicMap<Object> objectExporters = new PolymorphicMap<Object>();
	
	
	public ClipboardExporter(String pythonMethodName)
	{
		this.pythonExportMethodName = Py.newString( pythonMethodName.intern() );
	}

	

	
	protected abstract Object exportWithJavaInterface(Object x);
	
	
	protected Object invokeObjectExporter(ObjectClipboardExporter exporter, Object x)
	{
		return exporter.exportObject( x );
	}
	
	protected PyObject invokePyObjectExporter(PyObjectClipboardExporter exporter, PyObject x)
	{
		return exporter.exportObject( x );
	}
	
	
	protected abstract Object defaultExport(Object x);
	

	public Object exportObject(Object x)
	{
		Object result = null;
		
		
		PyObject pyX = null;
		
		// Java object clipboard export protocol - Java interface
		result = exportWithJavaInterface( x );
		
		// Python object clipboard export protocol
		if ( result == null  &&  x instanceof PyObject )
		{
			// @x is a Python object - if it offers a export method, use that
			pyX = (PyObject)x;
			PyObject presentMethod = null;
			try
			{
				presentMethod = __builtin__.getattr( pyX, pythonExportMethodName );
			}
			catch (PyException e)
			{
				presentMethod = null;
			}
			
			if ( presentMethod != null  &&  presentMethod.isCallable() )
			{
				result = Py.tojava( presentMethod.__call__(),  Object.class );
			}
			
			
			// Presentation method did not succeed. Try the registered exporters.
			if ( result == null )
			{
				// Now try Python object exporters
				PyType typeX = pyX.getType();
				
				PyObjectClipboardExporter exporter = (PyObjectClipboardExporter)objectExporters.get( typeX );
				if ( exporter != null )
				{
					result = Py.tojava( invokePyObjectExporter( exporter, pyX ), Object.class );
				}
			}
		}
		
		// Java object clipboard export protocol - registered exporters
		if ( result == null )
		{
			ObjectClipboardExporter exporter = (ObjectClipboardExporter)objectExporters.get( x.getClass() );
			if ( exporter != null )
			{
				result = invokeObjectExporter( exporter, x );
			}
		}
		
		if ( result == null )
		{
			// Fallback
			return defaultExport( x );
		}
		else
		{
			return result;
		}
	}

	
	

	public void registerJavaObjectExporter(Class<?> cls, ObjectClipboardExporter exporter)
	{
		objectExporters.put( cls, exporter );
	}
	
	public void registerPythonObjectExporter(PyType type, PyObjectClipboardExporter exporter)
	{
		objectExporters.put( type, exporter );
	}


	public void unregisterJavaObjectExporter(Class<?> cls)
	{
		objectExporters.remove( cls );
	}
	
	public void unregisterPythonObjectExporter(PyType type)
	{
		objectExporters.remove( type );
	}
}
