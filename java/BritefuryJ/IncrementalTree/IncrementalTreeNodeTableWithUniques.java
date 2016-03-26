//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.IncrementalTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Stack;


public class IncrementalTreeNodeTableWithUniques extends IncrementalTreeNodeTable
{
	private static class EntryForDocNode
	{
		private IncrementalTreeNodeTableWithUniques table;
		private Object key;
		private IncrementalTreeNode refedNode;
		private IncrementalTreeNode unrefedNode;
		
		
		
		public EntryForDocNode(IncrementalTreeNodeTableWithUniques table, Object key)
		{
			this.table = table;
			this.key = key;
		}
		
		
		
		public IncrementalTreeNode getUnrefedNodeFor(Object node, IncrementalTreeNode.NodeResultFactory resultFactory)
		{
			if ( refedNode == null  &&  unrefedNode != null )
			{
				if ( unrefedNode.getNodeResultFactory() == resultFactory )
				{
					return unrefedNode;
				}
			}
			
			return null;
		}
		
		
		public Collection<IncrementalTreeNode> getRefedNodes()
		{
			if ( refedNode != null )
			{
				return Arrays.asList( refedNode );
			}
			return new ArrayList<IncrementalTreeNode>();
		}
		
		public int size()
		{
			return refedNode != null  ?  1  :  0;
		}
		
		public int getNumUnrefedNodes()
		{
			return unrefedNode != null  ?  1  :  0;
		}

		
		public void refIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			if ( unrefedNode != null  &&  unrefedNode != incrementalNode )
			{
				throw new RuntimeException( "Attempting to ref the wrong incremental tree node (unique nodes for each key)" );
			}
			unrefedNode = null;
			refedNode = incrementalNode;
		}
		
		public void unrefIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			if ( refedNode != null )
			{
				if ( refedNode != incrementalNode )
				{
					throw new RuntimeException( "Attempting to unref the wrong incremental tree node (unique nodes for each key)" );
				}
			}
			refedNode = null;
			unrefedNode = incrementalNode;
		}
		
		
		
		
		private void clean()
		{
			unrefedNode = null;
			destroyIfEmpty();
		}
		
		private void destroyIfEmpty()
		{
			if ( refedNode == null  &&  unrefedNode == null )
			{
				table.removeViewTable( key );
			}
		}
	}
	
	
	
	private IdentityHashMap<Object, EntryForDocNode> table = new IdentityHashMap<Object, EntryForDocNode>();
	private HashSet<IncrementalTreeNode> unrefedNodes = new HashSet<IncrementalTreeNode>();
	
	
	
	
	
	public IncrementalTreeNodeTableWithUniques()
	{
	}
	
	
	

	public IncrementalTreeNode getUnrefedIncrementalNodeFor(Object node, IncrementalTreeNode.NodeResultFactory resultFactory)
	{
		EntryForDocNode subTable = table.get( node );
		if ( subTable != null )
		{
			return subTable.getUnrefedNodeFor( node, resultFactory );
		}
		else
		{
			return null;
		}
	}
	
	
	public Collection<IncrementalTreeNode> get(Object node)
	{
		EntryForDocNode subTable = table.get( node );
		if ( subTable != null )
		{
			return subTable.getRefedNodes();
		}
		else
		{
			return Arrays.asList();
		}
	}
	
	
	public boolean containsKey(Object docNode)
	{
		return getNumIncrementalNodesForDocNode( docNode ) > 0;
	}
	
	
	public int size()
	{
		int s = 0;
		for (EntryForDocNode subTable: table.values())
		{
			s += subTable.size();
		}
		return s;
	}
	
	public int getNumDocNodes()
	{
		return table.size();
	}
	
	public int getNumIncrementalNodesForDocNode(Object node)
	{
		EntryForDocNode subTable = table.get( node );
		if ( subTable != null )
		{
			return subTable.size();
		}
		else
		{
			return 0;
		}
	}
	
	public int getNumUnrefedIncrementalNodesForDocNode(Object node)
	{
		EntryForDocNode subTable = table.get( node );
		if ( subTable != null )
		{
			return subTable.getNumUnrefedNodes();
		}
		else
		{
			return 0;
		}
	}
	
	
	public void clean()
	{
		// We need to remove all nodes within the sub-trees rooted at the unrefed nodes
		Stack<IncrementalTreeNode> unrefedStack = new Stack<IncrementalTreeNode>();
		unrefedStack.addAll( unrefedNodes );
		
		while ( !unrefedStack.isEmpty() )
		{
			IncrementalTreeNode node = unrefedStack.pop();
			
			EntryForDocNode subTable = table.get( node.getModel() );
			if ( subTable != null )
			{
				subTable.clean();
			}
			
			for (IncrementalTreeNode child: node.getChildren())
			{
				unrefedStack.push( child );
			}
			
			node.dispose();
		}
		
		unrefedNodes.clear();
	}
	
	
	protected void refIncrementalNode(IncrementalTreeNode node)
	{
		Object key = node.getModel();
		EntryForDocNode subTable = table.get( key );
		if ( subTable == null )
		{
			subTable = new EntryForDocNode( this, key );
			table.put( key, subTable );
		}
		subTable.refIncrementalNode( node );
		unrefedNodes.remove( node );
	}

	protected void unrefIncrementalNode(IncrementalTreeNode node)
	{
		Object key = node.getModel();
		EntryForDocNode subTable = table.get( key );
		if ( subTable == null )
		{
			subTable = new EntryForDocNode( this, key );
			table.put( key, subTable );
		}
		subTable.unrefIncrementalNode( node );
		unrefedNodes.add( node );
	}


	

	private void removeViewTable(Object key)
	{
		table.remove( key );
	}
}
