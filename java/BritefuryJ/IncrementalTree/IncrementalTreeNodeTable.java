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

public class IncrementalTreeNodeTable
{
	private static class Key
	{
		private WeakReference<DMNode> node;
		private int hash;
		
		
		public Key(DMNode node)
		{
			this.node = new WeakReference<DMNode>( node );
			hash = System.identityHashCode( node );
		}
		
		
		public int hashCode()
		{
			return hash;
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof Key )
			{
				Key kx = (Key)x;
				return node.get() == kx.node.get();
			}
			
			return false;
		}
	}
	
	
	private static class ViewTableForDocNode
	{
		private IncrementalTreeNodeTable table;
		private Key key;
		
		private ArrayList<WeakReference<IncrementalTreeNode>> refedNodes;
		private ArrayList<IncrementalTreeNode> unrefedNodes;
		
		
		
		public ViewTableForDocNode(IncrementalTreeNodeTable table, Key key)
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
				// Find a view node; prefer one with the same element factory, otherwise pick one from the end of the list
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
					// Found an unused view node; move it into the ref'ed table
					unrefedNodes.remove( viewNode );
					refedNodes.add( new WeakReference<IncrementalTreeNode>( viewNode ) );
				}
				
				return viewNode;
			}
			
			return null;
		}
		
		
		public void addRefedViewNode(IncrementalTreeNode viewNode)
		{
			refedNodes.add( new WeakReference<IncrementalTreeNode>( viewNode ) );
		}
		
		public void removeViewNode(IncrementalTreeNode viewNode)
		{
			int i = 0;
			for (WeakReference<IncrementalTreeNode> ref: refedNodes)
			{
				if ( ref.get() == viewNode )
				{
					refedNodes.remove( i );
					destroyIfEmpty();
					return;
				}
				i++;
			}
			
			// If we fail to find @viewNode in @refedNodes, look in @unrefedNodes
			unrefedNodes.remove( viewNode );
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
		
		
		
		public void refViewNode(IncrementalTreeNode viewNode)
		{
			unrefedNodes.remove( viewNode );
			refedNodes.add( new WeakReference<IncrementalTreeNode>( viewNode ) );
		}
		
		public void unrefViewNode(IncrementalTreeNode viewNode)
		{
			for (int i = 0; i < refedNodes.size(); i++)
			{
				if ( refedNodes.get( i ).get() == viewNode )
				{
					refedNodes.remove( i );
				}
			}
			unrefedNodes.add( viewNode );
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
	
	
	
	private HashMap<Key, ViewTableForDocNode> table;
	
	
	
	
	
	public IncrementalTreeNodeTable()
	{
		table = new HashMap<Key, ViewTableForDocNode>();
	}
	
	
	

	public IncrementalTreeNode takeUnusedViewNodeFor(DMNode docNode, IncrementalTreeNode.NodeResultFactory resultFactory)
	{
		Key key = new Key( docNode );
		ViewTableForDocNode subTable = table.get( key );
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
		ViewTableForDocNode subTable = table.get( key );
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
		ViewTableForDocNode subTable = table.get( key );
		if ( subTable == null )
		{
			subTable = new ViewTableForDocNode( this, key );
			table.put( key, subTable );
		}
		subTable.addRefedViewNode( viewNode );
	}
	
	public void remove(IncrementalTreeNode viewNode)
	{
		Key key = new Key( viewNode.getDocNode() );
		ViewTableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			subTable.removeViewNode( viewNode );
		}
		else
		{
			throw new RuntimeException( "Could not get sub-table for doc-node" );
		}
	}
	
	
	public boolean containsKey(DMNode docNode)
	{
		return getNumViewNodesForDocNode( docNode ) > 0;
	}
	
	
	public int size()
	{
		int s = 0;
		for (ViewTableForDocNode subTable: table.values())
		{
			s += subTable.size();
		}
		return s;
	}
	
	public int getNumDocNodes()
	{
		return table.size();
	}
	
	public int getNumViewNodesForDocNode(DMNode docNode)
	{
		Key key = new Key( docNode );
		ViewTableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			return subTable.size();
		}
		else
		{
			return 0;
		}
	}
	
	public int getNumUnrefedViewNodesForDocNode(DMNode docNode)
	{
		Key key = new Key( docNode );
		ViewTableForDocNode subTable = table.get( key );
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
		ArrayList<ViewTableForDocNode> subTables = new ArrayList<ViewTableForDocNode>();
		subTables.addAll( table.values() );
		for (ViewTableForDocNode subTable: subTables)
		{
			subTable.clean();
		}
	}
	
	
	protected void refViewNode(IncrementalTreeNode node)
	{
		Key key = new Key( node.getDocNode() );
		ViewTableForDocNode subTable = table.get( key );
		subTable.refViewNode( node );
	}

	protected void unrefViewNode(IncrementalTreeNode node)
	{
		Key key = new Key( node.getDocNode() );
		ViewTableForDocNode subTable = table.get( key );
		subTable.unrefViewNode( node );
	}


	

	private void removeViewTable(Key key)
	{
		table.remove( key );
	}
}
