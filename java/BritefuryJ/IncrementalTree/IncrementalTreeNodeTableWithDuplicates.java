//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalTree;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import BritefuryJ.DocModel.DMNode;

public class IncrementalTreeNodeTableWithDuplicates extends IncrementalTreeNodeTable
{
	private static class TableForDocNode
	{
		private IncrementalTreeNodeTableWithDuplicates table;
		private Key key;
		
		private ArrayList<WeakReference<IncrementalTreeNode>> refedNodes;
		private ArrayList<IncrementalTreeNode> unrefedNodes;
		
		
		
		public TableForDocNode(IncrementalTreeNodeTableWithDuplicates table, Key key)
		{
			this.table = table;
			this.key = key;
			
			refedNodes = new ArrayList<WeakReference<IncrementalTreeNode>>();
			unrefedNodes = new ArrayList<IncrementalTreeNode>();
		}
		
		
		
		public IncrementalTreeNode takeUnusedNodeFor(DMNode node, IncrementalTreeNode.NodeResultFactory resultFactory)
		{
			removeDeadEntriesFromWeakList( refedNodes );
			
			
			if ( !unrefedNodes.isEmpty() )
			{
				// Find a view node; prefer one with the same result factory, otherwise pick one from the end of the list
				IncrementalTreeNode viewNode = null;
				for (IncrementalTreeNode vn: unrefedNodes)
				{
					if ( vn.getNodeResultFactory() == resultFactory )
					{
						viewNode = vn;
						break;
					}
				}
				if ( viewNode == null )
				{
					viewNode = unrefedNodes.get( unrefedNodes.size() - 1 );
				}
				
				
				
				if ( viewNode != null )
				{
					// Found an unused incremental tree node; move it into the ref'ed table
					unrefedNodes.remove( viewNode );
					refedNodes.add( new WeakReference<IncrementalTreeNode>( viewNode ) );
				}
				
				return viewNode;
			}
			
			return null;
		}
		
		
		public void addRefedIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			refedNodes.add( new WeakReference<IncrementalTreeNode>( incrementalNode ) );
		}
		
		public void removeIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			int i = 0;
			for (WeakReference<IncrementalTreeNode> ref: refedNodes)
			{
				if ( ref.get() == incrementalNode )
				{
					refedNodes.remove( i );
					destroyIfEmpty();
					return;
				}
				i++;
			}
			
			// If we fail to find @incrementalNode in @refedNodes, look in @unrefedNodes
			unrefedNodes.remove( incrementalNode );
			destroyIfEmpty();
		}
		
		
		public List<IncrementalTreeNode> getRefedNodes()
		{
			ArrayList<IncrementalTreeNode> nodes = new ArrayList<IncrementalTreeNode>();
			for (WeakReference<IncrementalTreeNode> ref: refedNodes)
			{
				IncrementalTreeNode node = ref.get();
				if ( node != null )
				{
					nodes.add( node );
				}
			}
			return nodes;
		}
		
		
		public int size()
		{
			return refedNodes.size();
		}
		
		public int getNumUnrefedNodes()
		{
			return unrefedNodes.size();
		}
		
		
		
		public void refIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			unrefedNodes.remove( incrementalNode );
			refedNodes.add( new WeakReference<IncrementalTreeNode>( incrementalNode ) );
		}
		
		public void unrefIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			for (int i = 0; i < refedNodes.size(); i++)
			{
				if ( refedNodes.get( i ).get() == incrementalNode )
				{
					refedNodes.remove( i );
				}
			}
			unrefedNodes.add( incrementalNode );
		}
		
		
		
		
		private static void removeDeadEntriesFromWeakList(ArrayList<WeakReference<IncrementalTreeNode>> weakList)
		{
			if ( !weakList.isEmpty() )
			{
				for (int i = weakList.size() - 1; i >= 0; i--)
				{
					if ( weakList.get( i ).get() == null )
					{
						weakList.remove( i );
					}
				}
			}
		}
		
		private void clean()
		{
			unrefedNodes.clear();
			destroyIfEmpty();
		}
		
		private void destroyIfEmpty()
		{
			removeDeadEntriesFromWeakList( refedNodes );
			
			if ( refedNodes.size() == 0  &&  unrefedNodes.size() == 0 )
			{
				table.removeViewTable( key );
			}
		}
	}
	
	
	
	private HashMap<Key, TableForDocNode> table;
	
	
	
	
	
	public IncrementalTreeNodeTableWithDuplicates()
	{
		table = new HashMap<Key, TableForDocNode>();
	}
	
	
	

	public IncrementalTreeNode takeUnusedIncrementalNodeFor(DMNode docNode, IncrementalTreeNode.NodeResultFactory resultFactory)
	{
		Key key = new Key( docNode );
		TableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			return subTable.takeUnusedNodeFor( docNode, resultFactory );
		}
		else
		{
			return null;
		}
	}
	
	
	public List<IncrementalTreeNode> get(DMNode docNode)
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
	
	public void put(DMNode docNode, IncrementalTreeNode viewNode)
	{
		Key key = new Key( docNode );
		TableForDocNode subTable = table.get( key );
		if ( subTable == null )
		{
			subTable = new TableForDocNode( this, key );
			table.put( key, subTable );
		}
		subTable.addRefedIncrementalNode( viewNode );
	}
	
	public void remove(IncrementalTreeNode viewNode)
	{
		Key key = new Key( viewNode.getDocNode() );
		TableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			subTable.removeIncrementalNode( viewNode );
		}
		else
		{
			throw new RuntimeException( "Could not get sub-table for doc-node" );
		}
	}
	
	
	public boolean containsKey(DMNode docNode)
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
	
	public int getNumIncrementalNodesForDocNode(DMNode docNode)
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
	
	public int getNumUnrefedIncrementalNodesForDocNode(DMNode docNode)
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
	
	
	public void clean()
	{
		ArrayList<TableForDocNode> subTables = new ArrayList<TableForDocNode>();
		subTables.addAll( table.values() );
		for (TableForDocNode subTable: subTables)
		{
			subTable.clean();
		}
	}
	
	
	protected void refIncrementalNode(IncrementalTreeNode node)
	{
		Key key = new Key( node.getDocNode() );
		TableForDocNode subTable = table.get( key );
		subTable.refIncrementalNode( node );
	}

	protected void unrefIncrementalNode(IncrementalTreeNode node)
	{
		Key key = new Key( node.getDocNode() );
		TableForDocNode subTable = table.get( key );
		subTable.unrefIncrementalNode( node );
	}


	

	private void removeViewTable(Key key)
	{
		table.remove( key );
	}
}
