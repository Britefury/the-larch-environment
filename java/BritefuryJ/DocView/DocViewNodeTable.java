//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.DocView.DVNode.CannotChangeDocNodeException;

public class DocViewNodeTable
{
	private static class ViewTableForDocNode
	{
		private DocViewNodeTable table;
		private Object docNode;
		
		private WeakHashMap<DocTreeNode, WeakReference<DVNode>> refedNodes;
		private WeakHashMap<DocTreeNode, DVNode> unrefedNodes;
		
		
		
		public ViewTableForDocNode(DocViewNodeTable table, Object docNode)
		{
			this.table = table;
			this.docNode = docNode;
			
			refedNodes = new WeakHashMap<DocTreeNode, WeakReference<DVNode>>();
			unrefedNodes = new WeakHashMap<DocTreeNode, DVNode>();
		}
		
		
		
		public DVNode takeUnusedNodeFor(DocTreeNode treeNode)
		{
			if ( refedNodes.containsKey( treeNode ) )
			{
				return null;
			}
			
			removeDeadEntriesFromTable( refedNodes );
			
			
			if ( !unrefedNodes.isEmpty() )
			{
				// Look in unrefed nodes to see if there is an existing node for @treeNode
				DVNode viewNode = unrefedNodes.get( treeNode );
				if ( viewNode != null )
				{
					// Found an unused view node; move it into the ref'ed table
					unrefedNodes.remove( treeNode );
					refedNodes.put( treeNode, new WeakReference<DVNode>( viewNode ) );
					return viewNode;
				}
			
			
				// Look for an unref'd node to re-use
				// Take an entry
				DocTreeNode key = unrefedNodes.keySet().iterator().next();
				viewNode = unrefedNodes.get( key );
				
				assert viewNode != null;
				
				unrefedNodes.remove( key );
				
				refedNodes.put( treeNode, new WeakReference<DVNode>( viewNode ) );
				try
				{
					viewNode.changeTreeNode( treeNode );
				}
				catch (CannotChangeDocNodeException e)
				{
					throw new RuntimeException();
				}
				
				if ( unrefedNodes.size() == 0 )
				{
					table.removeViewTableFromUnrefedList( this );
				}
				
				return viewNode;
			}
			
			return null;
		}
		
		
		public DVNode get(DocTreeNode treeNode)
		{
			WeakReference<DVNode> r = refedNodes.get( treeNode );
			return r != null  ?  r.get()  :  null;
		}
		
		public void put(DocTreeNode treeNode, DVNode viewNode)
		{
			refedNodes.put( treeNode, new WeakReference<DVNode>( viewNode ) );
		}
		
		public void remove(DocTreeNode treeNode)
		{
			refedNodes.remove( treeNode );
			destroyIfEmpty();
		}
		
		public boolean containsKey(DocTreeNode treeNode)
		{
			return refedNodes.containsKey( treeNode );
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
			DocTreeNode treeNode = viewNode.getTreeNode();
			unrefedNodes.remove( treeNode );
			if ( unrefedNodes.size() == 0 )
			{
				table.removeViewTableFromUnrefedList( this );
			}
			refedNodes.put( treeNode, new WeakReference<DVNode>( viewNode ) );
		}
		
		public void unrefViewNode(DVNode viewNode)
		{
			DocTreeNode treeNode = viewNode.getTreeNode();
			refedNodes.remove( treeNode );
			if ( unrefedNodes.size() == 0 )
			{
				table.addViewTableFromUnrefedList( this );
			}
			unrefedNodes.put( treeNode, viewNode );
		}
		
		
		
		
		private static void removeDeadEntriesFromTable(WeakHashMap<DocTreeNode, WeakReference<DVNode>> table)
		{
			if ( !table.isEmpty() )
			{
				HashSet<DocTreeNode> deadKeys = null;
				for (Map.Entry<DocTreeNode, WeakReference<DVNode>> entry: table.entrySet())
				{
					if ( entry.getValue().get() == null )
					{
						if ( deadKeys == null )
						{
							deadKeys = new HashSet<DocTreeNode>();
						}
						deadKeys.add( entry.getKey() );
					}
				}
				
				if ( deadKeys != null )
				{
					for (DocTreeNode key: deadKeys)
					{
						table.remove( key );
					}
				}
			}
		}
		
		private void clearUnrefedViewNodes()
		{
			unrefedNodes.clear();
			destroyIfEmpty();
		}
		
		private void destroyIfEmpty()
		{
			removeDeadEntriesFromTable( refedNodes );
			
			if ( refedNodes.size() == 0  &&  unrefedNodes.size() == 0 )
			{
				table.removeViewTable( docNode );
			}
		}
	}
	
	
	
	private HashMap<Object, ViewTableForDocNode> table;
	private HashSet<ViewTableForDocNode> unrefedTables;
	
	
	
	
	
	public DocViewNodeTable()
	{
		table = new HashMap<Object, ViewTableForDocNode>();
		unrefedTables = new HashSet<ViewTableForDocNode>();
	}
	
	
	

	public DVNode takeUnusedViewNodeFor(DocTreeNode treeNode)
	{
		ViewTableForDocNode subTable = table.get( treeNode.getNode() );
		if ( subTable != null )
		{
			return subTable.takeUnusedNodeFor( treeNode );
		}
		else
		{
			return null;
		}
	}
	
	
	
	public DVNode get(DocTreeNode treeNode)
	{
		ViewTableForDocNode subTable = table.get( treeNode.getNode() );
		if ( subTable != null )
		{
			return subTable.get( treeNode );
		}
		else
		{
			return null;
		}
	}
	
	public void put(DocTreeNode treeNode, DVNode viewNode)
	{
		ViewTableForDocNode subTable = table.get( treeNode.getNode() );
		if ( subTable == null )
		{
			subTable = new ViewTableForDocNode( this, treeNode.getNode() );
			table.put( treeNode.getNode(), subTable );
		}
		subTable.put( treeNode, viewNode );
	}
	
	public void remove(DocTreeNode treeNode)
	{
		ViewTableForDocNode subTable = table.get( treeNode.getNode() );
		if ( subTable != null )
		{
			subTable.remove( treeNode );
		}
	}
	
	
	public boolean containsKey(DocTreeNode treeNode)
	{
		ViewTableForDocNode subTable = table.get( treeNode.getNode() );
		if ( subTable != null )
		{
			return subTable.containsKey( treeNode );
		}
		else
		{
			return false;
		}
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
	
	public int getNumViewNodesForDocNode(Object docNode)
	{
		ViewTableForDocNode subTable = table.get( docNode );
		if ( subTable != null )
		{
			return subTable.size();
		}
		else
		{
			return 0;
		}
	}
	
	public int getNumUnrefedViewNodesForDocNode(Object docNode)
	{
		ViewTableForDocNode subTable = table.get( docNode );
		if ( subTable != null )
		{
			return subTable.getNumUnrefedNodes();
		}
		else
		{
			return 0;
		}
	}
	
	
	public void clearUnused()
	{
		for (ViewTableForDocNode subTable: unrefedTables)
		{
			subTable.clearUnrefedViewNodes();
		}
		unrefedTables.clear();
	}
	
	
	public void clean()
	{
		ArrayList<ViewTableForDocNode> subTables = new ArrayList<ViewTableForDocNode>();
		subTables.addAll( table.values() );
		for (ViewTableForDocNode subTable: subTables)
		{
			subTable.destroyIfEmpty();
		}
	}

	

	public void refViewNode(DVNode node)
	{
		ViewTableForDocNode subTable = table.get( node.getDocNode() );
		subTable.refViewNode( node );
	}

	public void unrefViewNode(DVNode node)
	{
		ViewTableForDocNode subTable = table.get( node.getDocNode() );
		subTable.unrefViewNode( node );
	}


	

	private void removeViewTable(Object docNode)
	{
		table.remove( docNode );
	}
	
	
	private void addViewTableFromUnrefedList(ViewTableForDocNode viewTable)
	{
		unrefedTables.add( viewTable );
	}

	public void removeViewTableFromUnrefedList(ViewTableForDocNode viewTable)
	{
		unrefedTables.remove( viewTable );
	}
}
