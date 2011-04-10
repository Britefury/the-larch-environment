//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import java.util.HashSet;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.modules.cPickle;

public class IsolationPickle
{
	public static void dump(PyObject file, PyObject obj)
	{
		cPickle.Pickler rootPickler = cPickle.Pickler( file );
		
		IslandPicklerState picklerState = new IslandPicklerState();
		
		rootPickler.dump( Py.java2py( new IslandPickleTag() ) );
		HashSet<Long> rootIds = picklerState.dumpRoots( rootPickler, obj );
		int islandsAsIndices[][] = picklerState.computeIslands( rootPickler, rootIds );
		picklerState.dumpIslands( rootPickler, rootIds, islandsAsIndices );
	}
	
	public static PyObject load(PyObject file)
	{
		cPickle.Unpickler unpickler = cPickle.Unpickler( file );
		
		PyObject x = unpickler.load();
		if ( Py.isInstance( x, PyType.fromClass( IslandPickleTag.class ) ) )
		{
			IslandUnpicklerState unpicklerState = new IslandUnpicklerState();
			return unpicklerState.load( unpickler );
		}
		else
		{
			return x;
		}
	}
}
