//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import BritefuryJ.DocModel.DMNode;

public class DocViewNodeTable
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
		private DocViewNodeTable table;
		private Key key;
		
		private ArrayList<WeakReference<DVNode>> refedNodes;
		private ArrayList<DVNode> unrefedNodes;
		
		
		
		public ViewTableForDocNode(DocViewNodeTable table, Key key)
		{
			this.table = table;
			this.key = key;
			
			refedNodes = new ArrayList<WeakReference<DVNode>>();
			unrefedNodes = new ArrayList<DVNode>();
		}
		
		
		
		public DVNode takeUnusedNodeFor(DMNode node, DVNode.NodeElementFactory elementFactory)
		{
			removeDeadEntriesFromWeakList( refedNodes );
			
			
			if ( !unrefedNodes.isEmpty() )
			{
				// Find a view node; prefer one with the same element factory, otherwise pick one from the end of the list
				DVNode viewNode = null;
				for (DVNode vn: unrefedNodes)
				{
					if ( vn.getNodeElementFactory() == elementFactory )
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
					refedNodes.add( new WeakReference<DVNode>( viewNode ) );
				}
				
				return viewNode;
			}
			
			return null;
		}
		
		
		public void addRefedViewNode(DVNode viewNode)
		{
			refedNodes.add( new WeakReference<DVNode>( viewNode ) );
		}
		
		public void removeViewNode(DVNode viewNode)
		{
			int i = 0;
			for (WeakReference<DVNode> ref: refedNodes)
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
		
		
		public List<DVNode> getRefedNodes()
		{
			ArrayList<DVNode> nodes = new ArrayList<DVNode>();
			for (WeakReference<DVNode> ref: refedNodes)
			{
				DVNode node = ref.get();
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
		
		
		
		public void refViewNode(DVNode viewNode)
		{
			unrefedNodes.remove( viewNode );
			refedNodes.add( new WeakReference<DVNode>( viewNode ) );
		}
		
		public void unrefViewNode(DVNode viewNode)
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
		
		
		
		
		private static void removeDeadEntriesFromWeakList(ArrayList<WeakReference<DVNode>> weakList)
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
	
	
	
	
	
	public DocViewNodeTable()
	{
		table = new HashMap<Key, ViewTableForDocNode>();
	}
	
	
	

	public DVNode takeUnusedViewNodeFor(DMNode docNode, DVNode.NodeElementFactory elementFactory)
	{
		Key key = new Key( docNode );
		ViewTableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			return subTable.takeUnusedNodeFor( docNode, elementFactory );
		}
		else
		{
			return null;
		}
	}
	
	
	public List<DVNode> get(DMNode docNode)
	{
		Key key = new Key( docNode );
		ViewTableForDocNode subTable = table.get( key );
		if ( subTable != null )
		{
			return subTable.getRefedNodes();
		}
		else
		{
			return Arrays.asList( new DVNode[] {} );
		}
	}
	
	public void put(DMNode docNode, DVNode viewNode)
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
	
	public void remove(DVNode viewNode)
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
	
	
	protected void refViewNode(DVNode node)
	{
		Key key = new Key( node.getDocNode() );
		ViewTableForDocNode subTable = table.get( key );
		subTable.refViewNode( node );
	}

	protected void unrefViewNode(DVNode node)
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
