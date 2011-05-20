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
import java.util.Iterator;
import java.util.WeakHashMap;

class DependencyTable <KeyType>
{
	public static class KeySet <KeyType> implements Collection<KeyType>
	{
		private static WeakHashMap<KeySet<?>, KeySet<?>> globalKeysets = new WeakHashMap<KeySet<?>, KeySet<?>>();

		private HashSet<KeyType> value = new HashSet<KeyType>();
		private HashMap<KeyType, KeySet<KeyType>> derivedKeysets = null;
		private int hashCode;
		
		
		private KeySet()
		{
			this.hashCode = value.hashCode();
		}
		
		private KeySet(HashSet<KeyType> value, KeyType key)
		{
			this.value = new HashSet<KeyType>();
			this.value.addAll( value );
			this.value.add( key );
			this.hashCode = this.value.hashCode();
		}
		
		
		@SuppressWarnings("unchecked")
		public KeySet<KeyType> withKey(KeyType key)
		{
			KeySet<KeyType> derived = null;
			if ( derivedKeysets != null )
			{
				derived = derivedKeysets.get( key );
			}
			
			if ( derived == null )
			{
				if ( derivedKeysets == null )
				{
					derivedKeysets = new HashMap<KeyType, KeySet<KeyType>>();
				}
				
				KeySet<KeyType> x = new KeySet<KeyType>( value, key );
				derived = (KeySet<KeyType>)globalKeysets.get( x );
				if ( derived == null )
				{
					globalKeysets.put( x, x );
					derived = x;
				}
			}
			
			return derived;
		}
		
		
		public int hashCode()
		{
			return hashCode;
		}
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			else if ( x instanceof KeySet )
			{
				@SuppressWarnings("unchecked")
				KeySet<KeyType> kx = (KeySet<KeyType>)x;
				
				return value.equals( kx.value );
			}
			else
			{
				return false;
			}
		}

		
		
		public boolean add(KeyType arg0)
		{
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends KeyType> arg0)
		{
			throw new UnsupportedOperationException();
		}

		public void clear()
		{
			throw new UnsupportedOperationException();
		}

		public boolean contains(Object x)
		{
			return value.contains( x );
		}

		public boolean containsAll(Collection<?> xs)
		{
			return value.containsAll( xs );
		}

		public boolean isEmpty()
		{
			return value.isEmpty();
		}

		public Iterator<KeyType> iterator()
		{
			return value.iterator();
		}

		public boolean remove(Object arg0)
		{
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> arg0)
		{
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection<?> arg0)
		{
			throw new UnsupportedOperationException();
		}

		public int size()
		{
			return value.size();
		}

		public Object[] toArray()
		{
			return value.toArray();
		}

		public <T> T[] toArray(T[] a)
		{
			return value.toArray( a );
		}
	}
	
	
	public class Node
	{
		private KeyType key;
		private KeySet<KeyType> dependents;
		
		public Node(KeyType key)
		{
			this.key = key;
			dependents = identity;
		}
		
		
		public KeyType getKey()
		{
			return key;
		}
		
		public KeySet<KeyType> getDependents()
		{
			return dependents;
		}
		
		
		public void addDependent(KeyType key)
		{
			dependents = dependents.withKey( key );
		}
	}
	
	
	public static class Partition <KeyType>
	{
		public KeySet<KeyType> srcKeys;
		public ArrayList<KeyType> members = new ArrayList<KeyType>();
		
		public Partition(KeySet<KeyType> srcKeys)
		{
			this.srcKeys = srcKeys;
		}
	}
	
	
	
	private HashMap<KeyType, Node> nodeTable = new HashMap<KeyType, Node>();
	private KeySet<KeyType> identity = new KeySet<KeyType>();
	
	
	public DependencyTable()
	{
	}
	
	
	public void addDependency(KeyType srcKey, KeyType destKey)
	{
		Node srcNode = nodeTable.get( srcKey );
		if ( srcNode == null )
		{
			srcNode = new Node( srcKey );
			nodeTable.put( srcKey, srcNode );
		}

		Node destNode = nodeTable.get( destKey );
		if ( destNode == null )
		{
			destNode = new Node( destKey );
			nodeTable.put( destKey, destNode );
		}
		
		destNode.addDependent( srcKey );
	}
	
	
	public Collection<Partition<KeyType>> computePartitions()
	{
		HashMap<KeySet<KeyType>, Partition<KeyType>> partitions = new HashMap<KeySet<KeyType>, Partition<KeyType>>();
		
		for (Node node: nodeTable.values())
		{
			KeySet<KeyType> key = node.getDependents();
			Partition<KeyType> partition = partitions.get( key );
			if ( partition == null )
			{
				partition = new Partition<KeyType>( key );
				partitions.put( key, partition );
			}
			partition.members.add( node.getKey() );
		}
		
		return partitions.values();
	}
}
