//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import java.util.ArrayList;
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

import BritefuryJ.Utils.DependencyGraph;

class IslandPicklerState
{
	private HashMap<Long, Integer> isolatedObjectIdToIndex = new HashMap<Long, Integer>();
	private ArrayList<PyObject> isolatedObjects = new ArrayList<PyObject>();
	
	
	
	
	protected HashSet<Long> dumpRoots(cPickle.Pickler rootPickler, PyObject obj)
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
		IslandPicklerState prevIslandPicklerState = IsolationBarrier.islandPicklerState;
		IsolationBarrier.islandPicklerState = this;
		rootPickler.persistent_id = rootPersistentId;
		rootPickler.dump( obj );
		rootPickler.persistent_id = null;
		IsolationBarrier.islandPicklerState = prevIslandPicklerState;
		
		return rootIds;
	}
	
	
	
	protected int[][] computeIslands(cPickle.Pickler rootPickler, final HashSet<Long> rootIds)
	{
		// A dependency graph that tracks dependencies between isolated objects
		final DependencyGraph<Long> deps = new DependencyGraph<Long>();
		
		final long currentIsolatedObjectId[] = { 0L };
		
		PyObject gatherRefsPersistentId = new PyObject()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PyObject __call__(PyObject args[], String keywords[])
			{
				PyObject x = args[0];
				if ( x != null  &&  x != Py.None  &&  !isSimplePrimitiveType( x.getType() ) )
				{
					long key = Py.id( x );
					
					if ( !rootIds.contains( key ) )
					{
						// the current isolated objects depends upon @x
						deps.addSymmetricDependency( currentIsolatedObjectId[0], key );
					}
				}
				
				return Py.None;
			}
		};
		
		
		// Create stream for detecting references
		cStringIO.StringIO stream = new cStringIO.StringIO();
		cPickle.Pickler pickler = new cPickle.Pickler( stream, 0 );
		pickler.persistent_id = gatherRefsPersistentId;
		
		// Pickle each isolated object in turn, generating dependencies as we go
		for (int i = 0; i < isolatedObjects.size(); i++)
		{
			PyObject x = isolatedObjects.get( i );
			long key = Py.id( x );
			currentIsolatedObjectId[0] = key;
			pickler.dump( x );
		}
		
		// Get the dependency islands
		ArrayList<ArrayList<Long>> islandsAsKeys = deps.findIslands();
		
		// Translate the object IDs to object indices
		int islandsAsIndices[][] = new int[islandsAsKeys.size()][];
		int j = 0;
		for (ArrayList<Long> islandAsKeys: islandsAsKeys)
		{
			ArrayList<Integer> islandAsIndices = new ArrayList<Integer>();
			for (Long key: islandAsKeys)
			{
				Integer index = isolatedObjectIdToIndex.get( key );
				if ( index != null )
				{
					islandAsIndices.add( index );
				}
			}
			islandsAsIndices[j] = new int[islandAsIndices.size()];
			for (int i = 0; i < islandAsIndices.size(); i++)
			{
				islandsAsIndices[j][i] = islandAsIndices.get( i );
			}
			j++;
		}
		
		return islandsAsIndices;
	}
	
	
	
	
	protected void dumpIslands(cPickle.Pickler rootPickler, final HashSet<Long> rootIds, int islandsAsIndices[][])
	{
		// Maintain a map of root object key to name
		final HashMap<Long, PyString> rootKeyToName = new HashMap<Long, PyString>();
		final PyDictionary rootNameToObj = new PyDictionary();
		
		PyObject pickleIslandsPersistentId = new PyObject()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public PyObject __call__(PyObject args[], String keywords[])
			{
				PyObject x = args[0];
				if ( x != null  &&  x != Py.None  &&  !isSimplePrimitiveType( x.getType() ) )
				{
					long key = Py.id( x );
					PyString name;
					
					if ( rootIds.contains( key ) )
					{
						name = rootKeyToName.get( key );
					}
					else
					{
						int index = rootKeyToName.size();
						name = Py.newString( Integer.toHexString( index ) );
						rootKeyToName.put( key, name );
						rootNameToObj.__setitem__( name, x );
					}
					
					return name;
				}
				
				return Py.None;
			}
		};
		
		
		// Serialise each island
		ArrayList<PyString> islandStreams = new ArrayList<PyString>();
		for (int island[]: islandsAsIndices)
		{
			// Create a pickler for each island
			cStringIO.StringIO stream = cStringIO.StringIO();
			cPickle.Pickler pickler = cPickle.Pickler( stream );
			pickler.persistent_id = pickleIslandsPersistentId;
			
			PyList islandObjects = new PyList();
			for (int i: island)
			{
				islandObjects.append( isolatedObjects.get( i ) );
			}
			
			pickler.dump( islandObjects );
			
			islandStreams.add( stream.getvalue() );
		}
		
		
		// Dump rootNameToObj
		// this can be used to reconstruct references to the root objects
		rootPickler.dump( rootNameToObj );
		
		// Build a list of tuples, containing the islands, and streams
		PyList islandsAndStreams = new PyList();
		for (int i = 0; i < islandsAsIndices.length; i++)
		{
			int islandAsIndices[] = islandsAsIndices[i];
			
			PyList island = new PyList();
			for (int index: islandAsIndices)
			{
				island.append( Py.newInteger( index ) );
			}
			islandsAndStreams.append( new PyTuple( island, islandStreams.get( i ) ) );
		}
		
		// Dump
		rootPickler.dump( islandsAndStreams );
		
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
