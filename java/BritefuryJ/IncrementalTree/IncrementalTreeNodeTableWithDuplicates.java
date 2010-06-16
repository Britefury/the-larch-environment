//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalTree;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


public class IncrementalTreeNodeTableWithDuplicates extends IncrementalTreeNodeTable
{
	private static class TableForDocNode
	{
		private IncrementalTreeNodeTableWithDuplicates table;
		private Key key;
		
		private HashSet<IncrementalTreeNode> refedNodes = new HashSet<IncrementalTreeNode>();
		private HashSet<IncrementalTreeNode> unrefedNodes;
		
		
		
		public TableForDocNode(IncrementalTreeNodeTableWithDuplicates table, Key key)
		{
			this.table = table;
			this.key = key;
		}
		
		
		private void addUnrefedNode(IncrementalTreeNode node)
		{
			if ( unrefedNodes == null )
			{
				unrefedNodes = new HashSet<IncrementalTreeNode>();
			}
			unrefedNodes.add( node );
		}
		
		private void removeUnrefedNode(IncrementalTreeNode node)
		{
			if ( unrefedNodes != null )
			{
				unrefedNodes.remove( node );
				if ( unrefedNodes.size() == 0 )
				{
					unrefedNodes = null;
				}
			}
		}
		
		private IncrementalTreeNode getUnrefedNode(IncrementalTreeNode.NodeResultFactory nodeResultFactory)
		{
			if ( unrefedNodes != null )
			{
				for (IncrementalTreeNode node: unrefedNodes)
				{
					if ( node.getNodeResultFactory() == nodeResultFactory )
					{
						return node;
					}
				}
			}
			return null;
		}
		
		
		
		public IncrementalTreeNode getUnrefedNodeFor(Object node, IncrementalTreeNode.NodeResultFactory resultFactory)
		{
			return getUnrefedNode( resultFactory );
		}
		
		
		public Collection<IncrementalTreeNode> getRefedNodes()
		{
			return refedNodes;
		}
		
		
		public int size()
		{
			return refedNodes.size();
		}
		
		public int getNumUnrefedNodes()
		{
			return unrefedNodes != null  ?  unrefedNodes.size()  :  0;
		}
		
		
		
		public void refIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			removeUnrefedNode( incrementalNode );
			refedNodes.add( incrementalNode );
		}
		
		public void unrefIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			refedNodes.remove( incrementalNode );
			addUnrefedNode( incrementalNode );
		}
		
		
		
		
		private void clean()
		{
			unrefedNodes = null;
			if ( refedNodes.size() == 0 )
			{
				table.removeViewTable( key );
			}
		}
	}
	
	
	
	private HashMap<Key, TableForDocNode> table = new HashMap<Key, TableForDocNode>();
	private HashSet<IncrementalTreeNode> unrefedNodes = new HashSet<IncrementalTreeNode>();
	
	
	
	
	
	public IncrementalTreeNodeTableWithDuplicates()
	{
	}
	
	
	

	public IncrementalTreeNode getUnrefedIncrementalNodeFor(Object docNode, IncrementalTreeNode.NodeResultFactory resultFactory)
	{
		Key key = new Key( docNode );
		TableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			return subTable.getUnrefedNodeFor( docNode, resultFactory );
		}
		else
		{
			return null;
		}
	}
	
	
	public Collection<IncrementalTreeNode> get(Object docNode)
	{
		Key key = new Key( docNode );
		TableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			return subTable.getRefedNodes();
		}
		else
		{
			return Arrays.asList( new IncrementalTreeNode[] {} );
		}
	}
	
	
	public boolean containsKey(Object docNode)
	{
		return getNumIncrementalNodesForDocNode( docNode ) > 0;
	}
	
	
	public int size()
	{
		int s = 0;
		for (TableForDocNode subTable: table.values())
		{
			s += subTable.size();
		}
		return s;
	}
	
	public int getNumDocNodes()
	{
		return table.size();
	}
	
	public int getNumIncrementalNodesForDocNode(Object docNode)
	{
		Key key = new Key( docNode );
		TableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			return subTable.size();
		}
		else
		{
			return 0;
		}
	}
	
	public int getNumUnrefedIncrementalNodesForDocNode(Object docNode)
	{
		Key key = new Key( docNode );
		TableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			return subTable.getNumUnrefedNodes();
		}
		else
		{
			return 0;
		}
	}
	
	public Set<Key> getKeys()
	{
		return table.keySet();
	}
	
	public void clean()
	{
		// We need to remove all nodes within the sub-trees rooted at the unrefed nodes
		Stack<IncrementalTreeNode> unrefedStack = new Stack<IncrementalTreeNode>();
		unrefedStack.addAll( unrefedNodes );
		
		while ( !unrefedStack.isEmpty() )
		{
			IncrementalTreeNode node = unrefedStack.pop();
			
			for (IncrementalTreeNode child: node.getChildren())
			{
				unrefedStack.push( child );
			}

			TableForDocNode subTable = table.get( new Key( node.getDocNode() ) );
			if ( subTable != null )
			{
				subTable.clean();
			}
			
			node.dispose();
		}
		
		unrefedNodes.clear();
	}
	
	
	protected void refIncrementalNode(IncrementalTreeNode node)
	{
		Key key = new Key( node.getDocNode() );
		TableForDocNode subTable = table.get( key );
		if ( subTable == null )
		{
			subTable = new TableForDocNode( this, key );
			table.put( key, subTable );
		}
		subTable.refIncrementalNode( node );
		unrefedNodes.remove( node );
	}

	protected void unrefIncrementalNode(IncrementalTreeNode node)
	{
		Key key = new Key( node.getDocNode() );
		TableForDocNode subTable = table.get( key );
		if ( subTable == null )
		{
			subTable = new TableForDocNode( this, key );
			table.put( key, subTable );
		}
		subTable.unrefIncrementalNode( node );
		unrefedNodes.add( node );
	}


	

	private void removeViewTable(Key key)
	{
		table.remove( key );
	}
}
