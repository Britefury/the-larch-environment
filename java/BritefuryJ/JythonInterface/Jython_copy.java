//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.JythonInterface;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.__builtin__;
import org.python.core.imp;

public class Jython_copy
{
	private static PyObject copy_mod, copy_fn, deepcopy_fn;
	
	
	private static PyObject get_copy_mod()
	{
		if ( copy_mod == null )
		{
			copy_mod = imp.importName( "copy", true );
		}
		
		return copy_mod;
	}
	
	private static PyObject get_copy_fn()
	{
		if ( copy_fn == null )
		{
			copy_fn = __builtin__.getattr( get_copy_mod(), Py.newString( "copy" ) );
		}
		
		return copy_fn;
	}
	
	private static PyObject get_deepcopy_fn()
	{
		if ( deepcopy_fn == null )
		{
			deepcopy_fn = __builtin__.getattr( get_copy_mod(), Py.newString( "deepcopy" ) );
		}
		
		return deepcopy_fn;
	}
	
	
	public static PyObject copy(PyObject x)
	{
		return get_copy_fn().__call__( x );
	}
	
	public static PyObject deepcopy(PyObject x)
	{
		return get_deepcopy_fn().__call__( x, Py.None );
	}
	
	public static PyObject deepcopy(PyObject x, PyObject memo)
	{
		return get_deepcopy_fn().__call__( x, memo );
	}
}
