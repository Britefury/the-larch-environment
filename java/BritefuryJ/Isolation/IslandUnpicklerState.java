//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.modules.cPickle;
import org.python.modules.cStringIO;

class IslandUnpicklerState
{
	private PyDictionary rootNameToObj = null;
	private int islandIndices[][] = null;
	private String islandStreams[] = null;
	private int objectIndexToIslandIndex[] = null;
	private PyObject isolatedObjects[] = null;
	
	
	protected PyObject load(cPickle.Unpickler unpickler)
	{
		// Setup isolation unpickler state, and load root object
		IslandUnpicklerState prev = IsolationBarrier.islandUnpicklerState; 
		IsolationBarrier.islandUnpicklerState = this;
		PyObject root = unpickler.load();
		IsolationBarrier.islandUnpicklerState = prev;
		
		rootNameToObj = (PyDictionary)unpickler.load();
		PyList islandsAndStreamsPy = (PyList)unpickler.load();
		PyInteger numIsolatedObjectsPy = (PyInteger)unpickler.load();
		int numIsolatedObjects = numIsolatedObjectsPy.asInt();
		objectIndexToIslandIndex = new int[numIsolatedObjects];
		for (int i = 0; i < numIsolatedObjects; i++)
		{
			objectIndexToIslandIndex[i] = -1;
		}
		
		PyObject islandsAndStreams[] = islandsAndStreamsPy.getArray();
		islandIndices = new int[islandsAndStreams.length][];
		islandStreams = new String[islandsAndStreams.length];
		for (int i = 0; i < islandsAndStreams.length; i++)
		{
			PyTuple tup = (PyTuple)islandsAndStreams[i];
			PyList islandPy = (PyList)tup.pyget( 0 ); 
			
			PyObject island[] = islandPy.getArray();
			int indices[] = new int[island.length];
			for (int j = 0; j < island.length; j++)
			{
				int x = island[j].asInt();
				indices[j] = x;
				objectIndexToIslandIndex[x] = i;
			}
			
			islandIndices[i] = indices;
			islandStreams[i] = tup.pyget( 1 ).asString();
		}
		
		isolatedObjects = new PyObject[numIsolatedObjects];
		
		return root;
	}
	
	protected Object getIsolatedValue(int index)
	{
		int islandIndex = objectIndexToIslandIndex[index];
		if ( islandIndex != -1 )
		{
			loadIsland( islandIndex );
		}
		return isolatedObjects[index];
	}
	
	
	private void loadIsland(int islandIndex)
	{
		PyObject rootPersistentLoad = new PyObject()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PyObject __call__(PyObject args[], String keywords[])
			{
				return rootNameToObj.__getitem__( args[0] );
			}
		};
		
		
		cPickle.Unpickler unpickler = cPickle.Unpickler( cStringIO.StringIO( islandStreams[islandIndex] ) );
		unpickler.persistent_load = rootPersistentLoad;
		PyList valuesPy = (PyList)unpickler.load();
		PyObject values[] = valuesPy.getArray();
		
		int island[] = islandIndices[islandIndex];
		
		if ( values.length != island.length )
		{
			throw Py.ValueError( "Island %d: size of values list does not match size of indices list" );
		}
		
		for (int i = 0; i < island.length; i++)
		{
			int x = island[i];
			isolatedObjects[x] = values[i];
			objectIndexToIslandIndex[x] = -1;
		}
	}
}
