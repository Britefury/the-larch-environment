//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.imp;
import org.python.modules.cPickle;
import org.python.modules.cStringIO;
import org.python.modules.cStringIO.StringIO;

public class IsolationPickle
{
	public static void dump(PyObject file, PyObject obj)
	{
		// Import cPickle to ensure that it is properly initialised before attempting to use it
		imp.importName( "cPickle", true );
		
		cPickle.Pickler rootPickler = cPickle.Pickler( file );
		
		IsolationPicklerState picklerState = new IsolationPicklerState();
		
		rootPickler.dump( Py.java2py( new IsolationPickleTag() ) );
		picklerState.dump( rootPickler, obj );
	}
	
	public static PyObject load(PyObject file)
	{
		// Import cPickle to ensure that it is properly initialised before attempting to use it
		imp.importName( "cPickle", true );
		
		cPickle.Unpickler unpickler = cPickle.Unpickler( file );
		
		PyObject x = unpickler.load();
		if ( Py.isInstance( x, PyType.fromClass( IsolationPickleTag.class ) ) )
		{
			IsolationUnpicklerState unpicklerState = new IsolationUnpicklerState();
			return unpicklerState.load( unpickler );
		}
		else
		{
			return x;
		}
	}
	
	public static String dumps(PyObject obj)
	{
		StringIO stream = cStringIO.StringIO();
		dump( stream, obj );
		return stream.getvalue().asString();
	}
	
	public static PyObject loads(String s)
	{
		StringIO stream = cStringIO.StringIO( s );
		return load( stream );		
	}
}
