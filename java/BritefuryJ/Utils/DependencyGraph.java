//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class DependencyGraph <KeyType>
{
	private static class Node <KeyType>
	{
		private KeyType key;
		private HashSet<Node<KeyType>> deps = new HashSet<Node<KeyType>>();
		private boolean visited = false;
		
		private Node(KeyType key)
		{
			this.key = key;
		}
		
		
		public int hashCode()
		{
			return key.hashCode();
		}
		
		@SuppressWarnings("unchecked")
		public boolean equals(Object x)
		{
			if ( x instanceof Node )
			{
				return key.equals( ((Node<KeyType>)x).key );
			}
			else
			{
				return false;
			}
		}
	}
	
	
	private HashMap<KeyType, Node<KeyType>> nodeTable = new HashMap<KeyType, Node<KeyType>>();
	
	
	public DependencyGraph()
	{
	}
	
	
	public void addDependency(KeyType srcKey, KeyType destKey)
	{
		Node<KeyType> srcNode = nodeTable.get( srcKey );
		if ( srcNode == null )
		{
			srcNode = new Node<KeyType>( srcKey );
			nodeTable.put( srcKey, srcNode );
		}

		Node<KeyType> destNode = nodeTable.get( destKey );
		if ( destNode == null )
		{
			destNode = new Node<KeyType>( destKey );
			nodeTable.put( destKey, destNode );
		}
		
		srcNode.deps.add( destNode );
	}

	public void addSymmetricDependency(KeyType srcKey, KeyType destKey)
	{
		Node<KeyType> srcNode = nodeTable.get( srcKey );
		if ( srcNode == null )
		{
			srcNode = new Node<KeyType>( srcKey );
			nodeTable.put( srcKey, srcNode );
		}

		Node<KeyType> destNode = nodeTable.get( destKey );
		if ( destNode == null )
		{
			destNode = new Node<KeyType>( destKey );
			nodeTable.put( destKey, destNode );
		}
		
		srcNode.deps.add( destNode );
		destNode.deps.add( srcNode );
	}
	
	
	public ArrayList<ArrayList<KeyType>> findIslands()
	{
		for (Node<KeyType> node: nodeTable.values())
		{
			node.visited = false;
		}
		
		ArrayList<ArrayList<KeyType>> islands = new ArrayList<ArrayList<KeyType>>();
		
		for (Node<KeyType> node: nodeTable.values())
		{
			if ( !node.visited )
			{
				Stack<Node<KeyType>> stack = new Stack<Node<KeyType>>();
				stack.push( node );
					
				ArrayList<KeyType> island = new ArrayList<KeyType>();
				while ( !stack.isEmpty() )
				{
					Node<KeyType> n = stack.pop();
					if ( !n.visited )
					{
						n.visited = true;
						island.add( n.key );
						for (Node<KeyType> d: n.deps)
						{
							if ( !d.visited )
							{
								stack.push( d );
							}
						}
					}
				}
				
				islands.add( island );
			}
		}
		
		return islands;
	}
}
