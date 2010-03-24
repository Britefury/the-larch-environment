//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalTree;


public class IncrementalTree
{
	public enum DuplicatePolicy
	{
		ALLOW_DUPLICATES,
		REQUIRE_UNIQUES
	}
	
	
	
	public interface RefreshListener
	{
		public void onIncrementalTreeRequestRefresh(IncrementalTree view);
	}
	
	
	private Object root;
	private IncrementalTreeNode.NodeResultFactory rootElementFactory;
	protected IncrementalTreeNodeTable nodeTable;
	private IncrementalTreeNode rootIncrementalTreeNode;
	private IncrementalTreeNode.NodeResultChangeListener resultChangeListener;
	private boolean bRefreshRequired;
	private RefreshListener refreshListener;
	
	
	
	
	
	
	
	
	
	public IncrementalTree(Object root, IncrementalTreeNode.NodeResultFactory rootElementFactory, DuplicatePolicy duplicatePolicy)
	{
		this.root = root;
		this.rootElementFactory = rootElementFactory;
		
		if ( duplicatePolicy == DuplicatePolicy.ALLOW_DUPLICATES )
		{
			nodeTable = new IncrementalTreeNodeTableWithDuplicates();
		}
		else if ( duplicatePolicy == DuplicatePolicy.ALLOW_DUPLICATES )
		{
			nodeTable = new IncrementalTreeNodeTableWithUniques();
		}
		else
		{
			throw new RuntimeException( "Invalid duplicate policy" );
		}
		
		resultChangeListener = null;
		
		bRefreshRequired = false;
	}
	
	
	public void setRefreshListener(RefreshListener listener)
	{
		refreshListener = listener;
	}
	
	public void setNodeResultChangeListener(IncrementalTreeNode.NodeResultChangeListener elementChangeListener)
	{
		this.resultChangeListener = elementChangeListener;
	}
	
	
	protected IncrementalTreeNode getRootIncrementalTreeNode()
	{
		if ( rootIncrementalTreeNode == null )
		{
			rootIncrementalTreeNode = buildIncrementalTreeNodeResult( root, rootElementFactory );
		}
		return rootIncrementalTreeNode;
	}
	
	
	public IncrementalTreeNode buildIncrementalTreeNodeResult(Object node, IncrementalTreeNode.NodeResultFactory elementFactory)
	{
		if ( node == null )
		{
			return null;
		}
		else
		{
			// Try asking the table for an unused incremental tree node for the document node
			IncrementalTreeNode viewNode = nodeTable.takeUnusedIncrementalNodeFor( node, elementFactory );
			
			if ( viewNode == null )
			{
				// No existing incremental tree node could be acquired.
				// Create a new one and add it to the table
				viewNode = createIncrementalTreeNode( node, resultChangeListener );
				nodeTable.put( node, viewNode );
			}
			
			viewNode.setNodeResultFactory( elementFactory );
			
			return viewNode;
		}
	}
	
	
	
	
	protected void performRefresh()
	{
		resultChangeListener.reset( this );
		getRootIncrementalTreeNode().refresh();
		
		// Clear unused entries from the node table
		nodeTable.clean();
	}
	
	
	public void refresh()
	{
		if ( bRefreshRequired )
		{
			bRefreshRequired = false;
			performRefresh();
		}
	}
	

	
	protected void onNodeRequestRefresh(IncrementalTreeNode node)
	{
		if ( node == rootIncrementalTreeNode )
		{
			if ( !bRefreshRequired )
			{
				bRefreshRequired = true;
				
				if ( refreshListener != null )
				{
					refreshListener.onIncrementalTreeRequestRefresh( this );
				}
			}
		}
	}
	
	
	
	protected IncrementalTreeNode createIncrementalTreeNode(Object node, IncrementalTreeNode.NodeResultChangeListener changeListener)
	{
		return new IncrementalTreeNode( this, node, resultChangeListener );
	}
}
