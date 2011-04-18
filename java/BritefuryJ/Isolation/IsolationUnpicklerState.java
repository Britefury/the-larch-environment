//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import java.util.HashMap;
import java.util.Stack;

import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.modules.cPickle;
import org.python.modules.cStringIO;

class IsolationUnpicklerState
{
	private PyDictionary rootNameToObj = null;
	private int partitionMemberIndices[][] = null;
	private long partitionMemberTags[][] = null;
	private String partitionStreams[] = null;
	private int partitionDeps[][] = null;
	private HashMap<Long,int[]> objTagToPartitionPos;
	private PyObject partitionObjects[][] = null;
	private HashMap<Long, PyObject> isolatedObjTagToObj;
	
	
	private IsolationUnpicklerState pushUnpicklerState()
	{
		IsolationUnpicklerState prev = IsolationBarrier.isolationUnpicklerState; 
		IsolationBarrier.isolationUnpicklerState = this;
		return prev;
	}
	
	private void popUnpicklerState(IsolationUnpicklerState prev)
	{
		IsolationBarrier.isolationUnpicklerState = prev;
	}


	protected Object getIsolatedValue(long tag)
	{
		int partitionPos[] = objTagToPartitionPos.get( tag );
		if ( partitionPos != null )
		{
			loadPartition( partitionPos[0] );
		}
		return isolatedObjTagToObj.get( tag );
	}
	
	
	protected PyObject load(cPickle.Unpickler unpickler)
	{
		// Setup isolation unpickler state, and load root object
		IsolationUnpicklerState prev = pushUnpicklerState();
		PyObject root = unpickler.load();
		popUnpicklerState( prev );
		
		PyList isolatedRootRefsPy = (PyList)unpickler.load();
		PyObject isolatedRootRefs[] = isolatedRootRefsPy.getArray();
		rootNameToObj = (PyDictionary)unpickler.load();
		PyList partitionsStreamsDepsPy = (PyList)unpickler.load();
		PyObject partitionsStreamsDeps[] = partitionsStreamsDepsPy.getArray();
		//PyList isolatedObjTags = (PyList)unpickler.load();
		
		
		objTagToPartitionPos = new HashMap<Long,int[]>();
		partitionObjects = new PyObject[partitionsStreamsDeps.length][];
		
		
		partitionMemberIndices = new int[partitionsStreamsDeps.length][];
		partitionMemberTags = new long[partitionsStreamsDeps.length][];
		partitionStreams = new String[partitionsStreamsDeps.length];
		partitionDeps = new int[partitionsStreamsDeps.length][];
		for (int i = 0; i < partitionsStreamsDeps.length; i++)
		{
			PyTuple tup = (PyTuple)partitionsStreamsDeps[i];
			
			PyList membersPy = (PyList)tup.pyget( 0 ); 
			PyObject members[] = membersPy.getArray();
			int memberIndices[] = new int[members.length];
			long memberTags[] = new long[members.length];
			for (int j = 0; j < members.length; j++)
			{
				PyTuple pair = (PyTuple)members[j];
				memberIndices[j] = pair.pyget( 0 ).asInt();
				long tag = pair.pyget( 1 ).asLong();
				memberTags[j] = tag;
				objTagToPartitionPos.put( tag, new int[] { i, j } );
			}
			
			PyList depsPy = (PyList)tup.pyget( 2 ); 
			PyObject deps[] = depsPy.getArray();
			int depIndices[] = new int[deps.length];
			for (int j = 0; j < deps.length; j++)
			{
				depIndices[j] = deps[j].asInt();
			}
			
			partitionMemberIndices[i] = memberIndices;
			partitionMemberTags[i] = memberTags;
			partitionStreams[i] = tup.pyget( 1 ).asString();
			partitionDeps[i] = depIndices;
		}
		
		isolatedObjTagToObj = new HashMap<Long, PyObject>();
		for (PyObject r: isolatedRootRefs)
		{
			PyTuple tup = (PyTuple)r;
			
			long tag = tup.pyget( 0 ).asInt();
			PyObject value = tup.pyget( 1 );
			
			isolatedObjTagToObj.put( tag, value );
		}
		
		return root;
	}

	private void loadPartition(int index)
	{
		Stack<Integer> partitionStack = new Stack<Integer>();
		partitionStack.push( index );
		
		while ( !partitionStack.isEmpty() )
		{
			int p = partitionStack.lastElement();
			
			int deps[] = partitionDeps[p];
			boolean finished = false;
			for (int d: deps)
			{
				if ( partitionObjects[d] == null )
				{
					partitionStack.add( d );
					finished = true;
				}
			}
			
			if ( !finished )
			{
				int x = partitionStack.pop();
				loadSinglePartition( x );
			}
		}
	}
	
	
	private void loadSinglePartition(int partitionIndex)
	{
		PyObject partitionPersistentLoad = new PyObject()
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
						throw new RuntimeException( "Partition " + p + " is not loaded" );
					}
					return partitionObjects[p][m];
				}
			}
		};
		
		
		cPickle.Unpickler unpickler = cPickle.Unpickler( cStringIO.StringIO( partitionStreams[partitionIndex] ) );
		unpickler.persistent_load = partitionPersistentLoad;
		
		IsolationUnpicklerState prev = pushUnpicklerState();
		PyList valuesPy = (PyList)unpickler.load();
		popUnpicklerState( prev );

		PyObject values[] = valuesPy.getArray();
		
		int memberIndices[] = partitionMemberIndices[partitionIndex];
		long memberTags[] = partitionMemberTags[partitionIndex];
		
		for (int i = 0; i < memberIndices.length; i++)
		{
			int n = memberIndices[i];
			long tag = memberTags[i];
			isolatedObjTagToObj.put( tag, values[n] );
			objTagToPartitionPos.remove( tag );
		}
		
		partitionObjects[partitionIndex] = values;
	}
}
