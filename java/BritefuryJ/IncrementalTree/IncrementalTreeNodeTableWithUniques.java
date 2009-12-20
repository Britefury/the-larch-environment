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

public class IncrementalTreeNodeTableWithUniques extends IncrementalTreeNodeTable
{
	private static class EntryForDocNode
	{
		private IncrementalTreeNodeTableWithUniques table;
		private Key key;
		private WeakReference<IncrementalTreeNode> refedNode;
		private IncrementalTreeNode unrefedNode;
		
		
		
		public EntryForDocNode(IncrementalTreeNodeTableWithUniques table, Key key)
		{
			this.table = table;
			this.key = key;
		}
		
		
		
		public IncrementalTreeNode takeUnusedNodeFor(DMNode node, IncrementalTreeNode.NodeResultFactory resultFactory)
		{
			removeDeadEntries();
			
			
			if ( refedNode == null  &&  unrefedNode != null )
			{
				// Find a view node; prefer one with the same result factory, otherwise pick one from the end of the list
				IncrementalTreeNode incrementalNode = unrefedNode;
				
				if ( incrementalNode != null )
				{
					// Found an unused incremental tree node; move it into the ref'ed table
					unrefedNode = null;
					refedNode = new WeakReference<IncrementalTreeNode>( incrementalNode );
				}
				
				return incrementalNode;
			}
			
			return null;
		}
		
		
		public void addRefedIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			refedNode = new WeakReference<IncrementalTreeNode>( incrementalNode );
		}
		
		public void removeIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			if ( refedNode != null  &&  refedNode.get() == incrementalNode )
			{
				refedNode = null;
				destroyIfEmpty();
			}
			else if ( unrefedNode == incrementalNode )
			{
				unrefedNode = null;
				destroyIfEmpty();
			}
		}
		
		
		public List<IncrementalTreeNode> getRefedNodes()
		{
			if ( refedNode != null )
			{
				IncrementalTreeNode node = refedNode.get();
				if ( node != null )
				{
					return Arrays.asList( new IncrementalTreeNode[] { node } );
				}
			}
			return new ArrayList<IncrementalTreeNode>();
		}
		
		public int size()
		{
			return refedNode != null  &&  refedNode.get() != null   ?   1  :  0;
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
			refedNode = new WeakReference<IncrementalTreeNode>( incrementalNode );
		}
		
		public void unrefIncrementalNode(IncrementalTreeNode incrementalNode)
		{
			if ( refedNode != null )
			{
				IncrementalTreeNode node = refedNode.get();
				if ( node != null  &&  node != incrementalNode )
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
		
		private void removeDeadEntries()
		{
			// Remove the refed node if it is dead
			if ( refedNode != null  &&  refedNode.get() == null )
			{
				refedNode = null;
			}
		}
		
		private void destroyIfEmpty()
		{
			removeDeadEntries();
			
			if ( refedNode == null  &&  unrefedNode == null )
			{
				table.removeViewTable( key );
			}
		}
	}
	
	
	
	private HashMap<Key, EntryForDocNode> table;
	
	
	
	
	
	public IncrementalTreeNodeTableWithUniques()
	{
		table = new HashMap<Key, EntryForDocNode>();
	}
	
	
	

	public IncrementalTreeNode takeUnusedIncrementalNodeFor(DMNode docNode, IncrementalTreeNode.NodeResultFactory resultFactory)
	{
		Key key = new Key( docNode );
		EntryForDocNode subTable = table.get( key );
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
		EntryForDocNode subTable = table.get( key );
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
		EntryForDocNode subTable = table.get( key );
		if ( subTable == null )
		{
			subTable = new EntryForDocNode( this, key );
			table.put( key, subTable );
		}
		subTable.addRefedIncrementalNode( viewNode );
	}
	
	public void remove(IncrementalTreeNode viewNode)
	{
		Key key = new Key( viewNode.getDocNode() );
		EntryForDocNode subTable = table.get( key );
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
	
	public int getNumIncrementalNodesForDocNode(DMNode docNode)
	{
		Key key = new Key( docNode );
		EntryForDocNode subTable = table.get( key );
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
		EntryForDocNode subTable = table.get( key );
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
		ArrayList<EntryForDocNode> subTables = new ArrayList<EntryForDocNode>();
		subTables.addAll( table.values() );
		for (EntryForDocNode subTable: subTables)
		{
			subTable.clean();
		}
	}
	
	
	protected void refIncrementalNode(IncrementalTreeNode node)
	{
		Key key = new Key( node.getDocNode() );
		EntryForDocNode subTable = table.get( key );
		subTable.refIncrementalNode( node );
	}

	protected void unrefIncrementalNode(IncrementalTreeNode node)
	{
		Key key = new Key( node.getDocNode() );
		EntryForDocNode subTable = table.get( key );
		subTable.unrefIncrementalNode( node );
	}


	

	private void removeViewTable(Key key)
	{
		table.remove( key );
	}
}
