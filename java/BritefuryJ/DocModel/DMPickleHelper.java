//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;

public class DMPickleHelper
{
	private static PyDictionary locals = new PyDictionary();
	
	
	private static PyDictionary getLocals()
	{
		if ( locals == null )
		{
			locals = new PyDictionary();

			String code = 
			"import imp\n" +
			"import sys\n" +
			"\n" +
			"fullname = 'BritefuryJ.DocModel.PyDMPickleHelper'\n" +
			"\n" +
			"\n" +
			"moduleCode = '''\n" +
			"from BritefuryJ.DocModel import DMList, DMObject\n" +
			"from BritefuryJ.DocModel.Resource import DMJavaResource, DMPyResource\n" +
			"\n" +
			"def makeDMList():\n" +
			"	return DMList()\n" +
			"\n" +
			"def makeDMObject():\n" +
			"	return DMList()\n" +
			"\n" +
			"def makeDMJavaResource():\n" +
			"	return DMList()\n" +
			"\n" +
			"def makeDMPyResource():\n" +
			"	return DMList()'''\n" +
			"\n" +
			"\n" +
			"mod = sys.modules.setdefault( fullname, imp.new_module( fullname ) )\n" +
			"mod.__file__ = fullname\n" +
			"mod.__loader__ = None\n" +
			"mod.__path__ = fullname.split( '.' )\n" +
			"\n" +
			"exec moduleCode in mod.__dict__\n";
			
			Py.exec( new PyString( code ), locals, locals );
		}
		
		return locals;
	}
	
	
	protected static PyObject getDMListFactory()
	{
		return getLocals().__getitem__( new PyString( "makeDMList" ) );
	}

	protected static PyObject getDMObjectFactory()
	{
		return getLocals().__getitem__( new PyString( "makeDMObject" ) );
	}

	public static PyObject getDMJavaResourceFactory()
	{
		return getLocals().__getitem__( new PyString( "makeDMJavaResource" ) );
	}

	public static PyObject getDMPyResourceFactory()
	{
		return getLocals().__getitem__( new PyString( "makeDMPyResource" ) );
	}
}
