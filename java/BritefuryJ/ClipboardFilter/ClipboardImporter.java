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

public abstract class ClipboardImporter <ImportDataType>
{
	private PyString pythonImportMethodName;
	private PolymorphicMap<Object> objectImporters = new PolymorphicMap<Object>();
	
	
	public ClipboardImporter(String pythonMethodName)
	{
		this.pythonImportMethodName = Py.newString( pythonMethodName.intern() );
	}

	

	
	protected Object invokeObjectImporter(ObjectClipboardImporter<ImportDataType> importer, ImportDataType importData)
	{
		return importer.importObject( importData );
	}
	
	protected PyObject invokePyObjectImporter(PyObjectClipboardImporter importer, PyObject importData)
	{
		return importer.importObject( importData );
	}
	
	
	protected abstract Object defaultImportJava(Class<?> cls, ImportDataType importData);
	protected abstract PyObject defaultImportPython(PyType type, PyObject importData);
	

	public Object importObject(Class<?> cls, ImportDataType importData)
	{
		Object result = null;
		
		@SuppressWarnings("unchecked")
		ObjectClipboardImporter<ImportDataType> importer = (ObjectClipboardImporter<ImportDataType>)objectImporters.get( cls );
		if ( importer != null )
		{
			result = invokeObjectImporter( importer, importData );
		}
		
		if ( result == null )
		{
			// Fallback
			return defaultImportJava( cls, importData );
		}
		else
		{
			return result;
		}
	}

	public PyObject importObject(PyType type, ImportDataType importData)
	{
		PyObject result = null;
		
		PyObject pyImportData = Py.java2py( importData );
		
		
		// Python object clipboard import protocol
		// If @type offers a import method, use that
		PyObject importMethod = null;
		try
		{
			importMethod = __builtin__.getattr( type, pythonImportMethodName );
		}
		catch (PyException e)
		{
			importMethod = null;
		}
		
		if ( importMethod != null  &&  importMethod.isCallable() )
		{
			result = importMethod.__call__( pyImportData );
		}
		
		
		// Import method did not succeed. Try the registered importers.
		if ( result == null )
		{
			PyObjectClipboardImporter importer = (PyObjectClipboardImporter)objectImporters.get( type );
			if ( importer != null )
			{
				result = invokePyObjectImporter( importer, pyImportData );
			}
		}
		
		if ( result == null )
		{
			// Fallback
			return defaultImportPython( type, pyImportData );
		}
		else
		{
			return result;
		}
	}

	
	

	public void registerJavaObjectImporter(Class<?> cls, ObjectClipboardImporter<ImportDataType> importer)
	{
		objectImporters.put( cls, importer );
	}
	
	public void registerPythonObjectImporter(PyType type, PyObjectClipboardImporter importer)
	{
		objectImporters.put( type, importer );
	}


	public void unregisterJavaObjectImporter(Class<?> cls)
	{
		objectImporters.remove( cls );
	}
	
	public void unregisterPythonObjectImporter(PyType type)
	{
		objectImporters.remove( type );
	}
}
