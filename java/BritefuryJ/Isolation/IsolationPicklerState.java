//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyBuiltinCallable;
import org.python.core.PyClass;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyReflectedFunction;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.modules.cPickle;
import org.python.modules.cStringIO;

class IsolationPicklerState
{
	private HashMap<Long, Integer> isolatedObjectIdToIndex = new HashMap<Long, Integer>();
	private ArrayList<PyObject> isolatedObjects = new ArrayList<PyObject>();
	
	
	private final static PyString rootName = Py.newString( "root" );
	
	
	
	protected void dump(cPickle.Pickler rootPickler, PyObject obj)
	{
		// We need to generate a set of IDs of the objects that are part of the root sub-graph
		final HashSet<Long> rootIds = new HashSet<Long>();
		
		PyObject rootPersistentId = new PyObject()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PyObject __call__(PyObject args[], String keywords[])
			{
				PyObject x = args[0];
				if ( x != null  &&  x != Py.None  &&  !isSimplePrimitiveType( x.getType() ) )
				{
					rootIds.add( Py.id( x ) );
				}
				
				return Py.None;
			}
		};
		
		
		// Setup the pickler, and dump @obj
		IsolationPicklerState prevIslandPicklerState = IsolationBarrier.isolationPicklerState;
		IsolationBarrier.isolationPicklerState = this;
		rootPickler.persistent_id = rootPersistentId;
		rootPickler.dump( obj );
		rootPickler.persistent_id = null;
		IsolationBarrier.isolationPicklerState = prevIslandPicklerState;
		
		
		// If there are any isolated values which are root objects, dump them:
		PyList isolatedRootRefs = new PyList();
		for (int i = 0; i < isolatedObjects.size(); i++)
		{
			PyObject x = isolatedObjects.get( i );
			long key = Py.id( x );
			if ( rootIds.contains( key ) )
			{
				isolatedRootRefs.append( new PyTuple( Py.newInteger( i ), x ) );
			}
		}
		rootPickler.dump( isolatedRootRefs );
		
		
		
		//
		//
		// COMPUTE PARTITIONS
		//
		//
		
		// A dependency table that tracks dependencies between isolated objects
		final DependencyTable<Integer> deps = new DependencyTable<Integer>();
		
		final int currentIsolatedObjectIndex[] = { -1 };
		
		final HashMap<Long, Integer> objIdToIndex = new HashMap<Long, Integer>();
		objIdToIndex.putAll( isolatedObjectIdToIndex );
		final ArrayList<PyObject> allObjs = new ArrayList<PyObject>();
		allObjs.addAll( isolatedObjects );
		
		PyObject gatherRefsPersistentId = new PyObject()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PyObject __call__(PyObject args[], String keywords[])
			{
				PyObject obj = args[0];
				if ( obj != null  &&  obj != Py.None  &&  !isSimplePrimitiveType( obj.getType() ) )
				{
					long key = Py.id( obj );
					
					if ( rootIds.contains( key ) )
					{
						return rootName;
					}
					else
					{
						Integer index = objIdToIndex.get( key );
						if ( index == null )
						{
							index = allObjs.size();
							objIdToIndex.put( key, index );
							allObjs.add( obj );
						}
						
						// Add a dependency
						deps.addDependency( currentIsolatedObjectIndex[0], index );
						
						return Py.None;
					}
				}
				
				return Py.None;
			}
		};
		
		
		// Pickle each isolated object in turn, generating dependencies as we go
		for (int i = 0; i < isolatedObjects.size(); i++)
		{
			// Create stream for detecting references
			cStringIO.StringIO stream = new cStringIO.StringIO();
			cPickle.Pickler pickler = new cPickle.Pickler( stream, 0 );
			pickler.persistent_id = gatherRefsPersistentId;
			
			currentIsolatedObjectIndex[0] = i;
			PyObject x = isolatedObjects.get( i );
			pickler.dump( x );
		}
		
		// Get the partitions
		Collection<DependencyTable.Partition<Integer>> partitions = deps.computePartitions();
		
		final int objIndexToPartitionPos[][] = new int[objIdToIndex.size()][];
		int partitionIndex = 0;
		for (DependencyTable.Partition<Integer> p: partitions)
		{
			for (int j = 0; j < p.members.size(); j++)
			{
				int m = p.members.get( j );
				objIndexToPartitionPos[m] = new int[] { partitionIndex, j };
			}
			partitionIndex++;
		}
		
		
		
		
		//
		//
		// DUMP THE PARTITIONS
		//
		//

		// Maintain a map of root object key to name
		final HashMap<Long, PyString> rootKeyToName = new HashMap<Long, PyString>();
		final PyDictionary rootNameToObj = new PyDictionary();
		final int currentPartition[] = { -1 };
		
		PyObject picklePartitionsPersistentId = new PyObject()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PyObject __call__(PyObject args[], String keywords[])
			{
				PyObject obj = args[0];
				if ( obj != null  &&  obj != Py.None  &&  !isSimplePrimitiveType( obj.getType() ) )
				{
					long key = Py.id( obj );
					
					if ( rootIds.contains( key ) )
					{
						PyString name = rootKeyToName.get( key );
						
						if ( name == null )
						{
							int index = rootKeyToName.size();
							name = Py.newString( "r" + Integer.toHexString( index ) );
							rootKeyToName.put( key, name );
							rootNameToObj.put( name, obj );
						}
						
						return name;
					}
					else
					{
						Integer indexObj = objIdToIndex.get( key );
						if ( indexObj != null )
						{
							int index = indexObj;
							int p[] = objIndexToPartitionPos[index];
							if ( p[0] == currentPartition[0] )
							{
								return Py.None;
							}
							else
							{
								return Py.newString( Integer.toHexString( p[0] ) + ":" + Integer.toHexString( p[1] ) );
							}
						}
						else
						{
							// Could not get an index for this object - its a temporary, created during pickling
							return Py.None;
						}
					}
				}
				else
				{
					return Py.None;
				}
			}
		};
		
		
		// Serialise each partition
		// Build a list of tuples, containing the partitions, and streams
		PyList partitionsAndStreams = new PyList();
		partitionIndex = 0;
		for (DependencyTable.Partition<Integer> partition: partitions)
		{
			// Create a pickler for each partition
			cStringIO.StringIO stream = cStringIO.StringIO();
			cPickle.Pickler pickler = cPickle.Pickler( stream );
			pickler.persistent_id = picklePartitionsPersistentId;
			
			currentPartition[0] = partitionIndex;
			
			PyList partitionObjects = new PyList();
			for (int m: partition.members)
			{
				partitionObjects.append( allObjs.get( m ) );
			}
			
			pickler.dump( partitionObjects );
			
			// Convert the member list to a PyList
			PyList partitionAsList = new PyList();
			for (int index: partition.members)
			{
				partitionAsList.append( Py.newInteger( index ) );
			}

			partitionsAndStreams.append( new PyTuple( partitionAsList, stream.getvalue() ) );
			
			partitionIndex++;
		}
		
		
		// Dump rootNameToObj
		// this can be used to reconstruct references to the root objects
		rootPickler.dump( rootNameToObj );
		
		// Dump
		rootPickler.dump( partitionsAndStreams );
		
		// Finally, dump the number of isolated objects
		rootPickler.dump( Py.newInteger( isolatedObjects.size() ) );
	}
	
	
	
	protected int isolatedValue(Object value)
	{
		PyObject x = Py.java2py( value );
		long key = Py.id( x );
		Integer index = isolatedObjectIdToIndex.get( key );
		
		if ( index == null )
		{
			int i = isolatedObjects.size();
			isolatedObjectIdToIndex.put( key, i );
			isolatedObjects.add( x );
			index = i;
		}
		
		return index;
	}



	private static boolean isSimplePrimitiveType(PyType type)
	{
		return type == PyNone.TYPE  ||
			type == PyBoolean.TYPE  ||
			type == PyString.TYPE  ||
			type == PyUnicode.TYPE  ||
			type == PyInteger.TYPE  ||
			type == PyLong.TYPE  ||
			type == PyFloat.TYPE  ||
			type == PyClass.TYPE  ||
			type == PyType.TYPE  ||
			type == PyFunction.TYPE  ||
			type == PyBuiltinCallable.TYPE  ||
			type == PyReflectedFunction.TYPE; 
	}
}
