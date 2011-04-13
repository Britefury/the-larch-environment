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

class IsolationUnpicklerState
{
	private PyDictionary rootNameToObj = null;
	private int partitionMembers[][] = null;
	private String partitionStreams[] = null;
	private int objectIndexToPartitionPos[][] = null;
	private PyObject partitionObjects[][] = null;
	private PyObject isolatedObjects[] = null;
	
	
	protected PyObject load(cPickle.Unpickler unpickler)
	{
		// Setup isolation unpickler state, and load root object
		IsolationUnpicklerState prev = IsolationBarrier.isolationUnpicklerState; 
		IsolationBarrier.isolationUnpicklerState = this;
		PyObject root = unpickler.load();
		IsolationBarrier.isolationUnpicklerState = prev;
		
		PyList isolatedRootRefsPy = (PyList)unpickler.load();
		PyObject isolatedRootRefs[] = isolatedRootRefsPy.getArray();
		rootNameToObj = (PyDictionary)unpickler.load();
		PyList partitionsAndStreamsPy = (PyList)unpickler.load();
		PyObject partitionsAndStreams[] = partitionsAndStreamsPy.getArray();
		PyInteger numIsolatedObjectsPy = (PyInteger)unpickler.load();
		int numIsolatedObjects = numIsolatedObjectsPy.asInt();
		
		
		objectIndexToPartitionPos = new int[numIsolatedObjects][];
		partitionObjects = new PyObject[partitionsAndStreams.length][];
		
		
		partitionMembers = new int[partitionsAndStreams.length][];
		partitionStreams = new String[partitionsAndStreams.length];
		for (int i = 0; i < partitionsAndStreams.length; i++)
		{
			PyTuple tup = (PyTuple)partitionsAndStreams[i];
			PyList partitionPy = (PyList)tup.pyget( 0 ); 
			
			PyObject partition[] = partitionPy.getArray();
			int indices[] = new int[partition.length];
			for (int j = 0; j < partition.length; j++)
			{
				int x = partition[j].asInt();
				indices[j] = x;
				if ( x < numIsolatedObjects )
				{
					objectIndexToPartitionPos[x] = new int[] { i, j };
				}
			}
			
			partitionMembers[i] = indices;
			partitionStreams[i] = tup.pyget( 1 ).asString();
		}
		
		isolatedObjects = new PyObject[numIsolatedObjects];
		
		for (PyObject r: isolatedRootRefs)
		{
			PyTuple tup = (PyTuple)r;
			
			int index = tup.pyget( 0 ).asInt();
			PyObject value = tup.pyget( 1 );
			
			isolatedObjects[index] = value;
		}
		
		return root;
	}
	
	protected Object getIsolatedValue(int index)
	{
		int partitionPos[] = objectIndexToPartitionPos[index];
		if ( partitionPos != null )
		{
			loadPartition( partitionPos[0] );
		}
		return isolatedObjects[index];
	}
	
	
	private void loadPartition(int partitionIndex)
	{
		PyObject rootPersistentLoad = new PyObject()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PyObject __call__(PyObject args[], String keywords[])
			{
				String name = args[0].asString();
				
				if ( name.startsWith( "r" ) )
				{
					return rootNameToObj.__getitem__( args[0] );
				}
				else
				{
					int colonPos = name.indexOf( ":" );
					int p = Integer.parseInt( name.substring( 0, colonPos ), 16 );
					int m = Integer.parseInt( name.substring( colonPos + 1 ), 16 );
					if ( partitionObjects[p] == null )
					{
						loadPartition( p );
					}
					return partitionObjects[p][m];
				}
			}
		};
		
		
		cPickle.Unpickler unpickler = cPickle.Unpickler( cStringIO.StringIO( partitionStreams[partitionIndex] ) );
		unpickler.persistent_load = rootPersistentLoad;
		PyList valuesPy = (PyList)unpickler.load();
		PyObject values[] = valuesPy.getArray();
		
		int partition[] = partitionMembers[partitionIndex];
		
		if ( values.length != partition.length )
		{
			throw Py.ValueError( "Island %d: size of values list does not match size of indices list" );
		}
		
		for (int i = 0; i < partition.length; i++)
		{
			int x = partition[i];
			if ( x < isolatedObjects.length )
			{
				isolatedObjects[x] = values[i];
				objectIndexToPartitionPos[x] = null;
			}
		}
		
		partitionObjects[partitionIndex] = values;
	}
}
