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

public class DependencyGraph
{
	private static class Node
	{
		private Object key;
		private HashSet<Node> deps = new HashSet<Node>();
		private boolean visited = false;
		
		private Node(Object key)
		{
			this.key = key;
		}
		
		
		public int hashCode()
		{
			return key.hashCode();
		}
		
		public boolean equals(Object x)
		{
			if ( x instanceof Node )
			{
				return key.equals( ((Node)x).key );
			}
			else
			{
				return false;
			}
		}
	}
	
	
	private HashMap<Object, Node> nodeTable = new HashMap<Object, Node>();
	
	
	public DependencyGraph()
	{
	}
	
	
	public void addDependency(Object srcKey, Object destKey)
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
		
		srcNode.deps.add( destNode );
	}

	public void addSymmetricDependency(Object srcKey, Object destKey)
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
		
		srcNode.deps.add( destNode );
		destNode.deps.add( srcNode );
	}
	
	
	public ArrayList<ArrayList<Object>> findIslands()
	{
		for (Node node: nodeTable.values())
		{
			node.visited = false;
		}
		
		ArrayList<ArrayList<Object>> islands = new ArrayList<ArrayList<Object>>();
		
		for (Node node: nodeTable.values())
		{
			if ( !node.visited )
			{
				Stack<Node> stack = new Stack<Node>();
				stack.push( node );
					
				ArrayList<Object> island = new ArrayList<Object>();
				while ( !stack.isEmpty() )
				{
					Node n = stack.pop();
					if ( !n.visited )
					{
						n.visited = true;
						island.add( n.key );
						for (Node d: n.deps)
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
