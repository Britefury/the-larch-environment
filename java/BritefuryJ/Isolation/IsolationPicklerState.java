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
	private HashMap<Long, PyObject> objTagToObj = new HashMap<Long, PyObject>();
	private HashMap<Long, PyObject> isolatedObjTagToObj = new HashMap<Long, PyObject>();
	private ArrayList<PyObject> isolatedObjects = new ArrayList<PyObject>();
	
	
	private final static PyString rootName = Py.newString( "root" );
	
	
	
	private void insertObjKey(PyObject obj, long key)
	{
		objTagToObj.put( key, obj );
	}
	
	
	private IsolationPicklerState pushPicklerState()
	{
		IsolationPicklerState prev = IsolationBarrier.isolationPicklerState;
		IsolationBarrier.isolationPicklerState = this;
		return prev;
	}
	
	private void popPicklerState(IsolationPicklerState prev)
	{
		IsolationBarrier.isolationPicklerState = prev;
	}
	
	
	
	protected long isolatedValue(Object value)
	{
		PyObject x = Py.java2py( value );
		long key = Py.id( x );
		
		if ( !isolatedObjTagToObj.containsKey( key ) )
		{
			isolatedObjTagToObj.put( key, x );
			isolatedObjects.add( x );
		}
		
		return key;
	}



	protected void dump(cPickle.Pickler rootPickler, PyObject obj)
	{
		// We need to generate a set of IDs of the objects that are part of the root sub-graph
		final HashSet<Long> rootTags = new HashSet<Long>();
		
		PyObject rootPersistentId = new PyObject()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PyObject __call__(PyObject args[], String keywords[])
			{
				PyObject x = args[0];
				if ( x != null  &&  x != Py.None  &&  !isSimplePrimitiveType( x.getType() ) )
				{
					rootTags.add( Py.id( x ) );
				}
				
				return Py.None;
			}
		};
		
		
		// Setup the pickler, and dump @obj
		IsolationPicklerState prev = pushPicklerState();
		rootPickler.persistent_id = rootPersistentId;
		rootPickler.dump( obj );
		rootPickler.persistent_id = null;
		popPicklerState( prev );
		
		
		
		if ( isolatedObjects.isEmpty() )
		{
			// No isolated objects - we are done
			rootPickler.dump( Py.False );
		}
		else
		{
			// Isolated objects - more to come
			rootPickler.dump( Py.True );

			//
			//
			// COMPUTE PARTITIONS
			//
			//
			
			// A dependency table that tracks dependencies between isolated objects
			final DependencyTable<Long> deps = new DependencyTable<Long>();
			
			final long currentIsolatedObjTag[] = { -1 };
			
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
						
						if ( rootTags.contains( key ) )
						{
							return rootName;
						}
						else
						{
							insertObjKey( obj, key );
							
							// Add a dependency
							deps.addDependency( currentIsolatedObjTag[0], key );
							
							return Py.None;
						}
					}
					
					return Py.None;
				}
			};
			
			
			
			// Pickle each isolated object in turn, generating dependencies as we go
			// If an isolated value is in the list of root objects, create an entry in the roof refs table
			prev = pushPicklerState();
			PyList isolatedRootRefs = new PyList();
			// NOTE: we MUST iterate by index here  -  the pickling process will want to add new elements to @isolatedObjects
			// WHILE we are iterating over it, in the case of nested isolated objects. If we iterate using an iterator, we get a
			// concurrent modification exception.
			for (int i = 0; i < isolatedObjects.size(); i++)
			{
				PyObject isolatedObject = isolatedObjects.get( i );
				
				// Create stream for detecting references
				cStringIO.StringIO stream = new cStringIO.StringIO();
				cPickle.Pickler pickler = new cPickle.Pickler( stream, 0 );
				pickler.persistent_id = gatherRefsPersistentId;

				long key = Py.id( isolatedObject );

				if ( rootTags.contains( key ) )
				{
					isolatedRootRefs.append( new PyTuple( Py.newInteger( key ), isolatedObject ) );
				}
				else
				{
					currentIsolatedObjTag[0] = key;
					pickler.dump( isolatedObject );
				}
			}
			popPicklerState( prev );
			
			// Dump the roof refs table
			rootPickler.dump( isolatedRootRefs );
		
			// Get the partitions
			Collection<DependencyTable.Partition<Long>> partitions = deps.computePartitions();
			
			final HashMap<Long,int[]> objTagToPartitionPos = new HashMap<Long,int[]>();
			int partitionIndex = 0;
			for (DependencyTable.Partition<Long> p: partitions)
			{
				for (int j = 0; j < p.members.size(); j++)
				{
					long m = p.members.get( j );
					objTagToPartitionPos.put( m, new int[] { partitionIndex, j } );
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
			
			final int currentPartitionIndex[] = { -1 };
			final HashSet<Integer> currentPartitionDependencies = new HashSet<Integer>();
			
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
						
						if ( rootTags.contains( key ) )
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
							int partitionPos[] = objTagToPartitionPos.get( key );
							if ( partitionPos != null )
							{
								if ( partitionPos[0] == currentPartitionIndex[0] )
								{
									return Py.None;
								}
								else
								{
									currentPartitionDependencies.add( partitionPos[0] );
									return Py.newString( Integer.toHexString( partitionPos[0] ) + ":" + Integer.toHexString( partitionPos[1] ) );
								}
							}
							else
							{
								// Could not get a partition pos for this object - its a temporary, created during pickling
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
			// Build a list of tuples, containing the partitions, streams and partition dependencies
			prev = pushPicklerState();
			PyList partitionsStreamsDeps = new PyList();
			partitionIndex = 0;
			for (DependencyTable.Partition<Long> partition: partitions)
			{
				// Create a pickler for each partition
				cStringIO.StringIO stream = cStringIO.StringIO();
				cPickle.Pickler pickler = cPickle.Pickler( stream );
				pickler.persistent_id = picklePartitionsPersistentId;
				
				currentPartitionIndex[0] = partitionIndex;
				currentPartitionDependencies.clear();
				
				PyList partitionObjects = new PyList();
				for (Long m: partition.members)
				{
					partitionObjects.append( objTagToObj.get( m ) );
				}
				
				pickler.dump( partitionObjects );
				
				// Convert the member list to a PyList
				// Retain only members in the member list that are isolated objects
				PyList partitionAsList = new PyList();
				int memberIndex = 0;
				for (Long tag: partition.members)
				{
					if ( isolatedObjTagToObj.containsKey( tag ) )
					{
						partitionAsList.append( new PyTuple( Py.newInteger( memberIndex ), Py.newInteger( tag ) ) );
					}
					memberIndex++;
				}
	
				// Convert the dependencies to a PyList
				PyList depsAsList = new PyList();
				for (int d: currentPartitionDependencies)
				{
					depsAsList.append( Py.newInteger( d ) );
				}
	
				partitionsStreamsDeps.append( new PyTuple( partitionAsList, stream.getvalue(), depsAsList ) );
				
				partitionIndex++;
			}
			popPicklerState( prev );
			
			
			// Dump rootNameToObj
			// this can be used to reconstruct references to the root objects
			rootPickler.dump( rootNameToObj );
			
			// Dump
			rootPickler.dump( partitionsStreamsDeps );
		}
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
