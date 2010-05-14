//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalTree;


public abstract class IncrementalTree
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
	
	
	protected Object modelRootNode;
	protected IncrementalTreeNodeTable nodeTable;
	private IncrementalTreeNode rootIncrementalTreeNode;
	private boolean bRefreshRequired;
	private RefreshListener refreshListener;
	
	
	
	
	
	
	
	
	
	public IncrementalTree(Object root, DuplicatePolicy duplicatePolicy)
	{
		this.modelRootNode = root;
		
		if ( duplicatePolicy == DuplicatePolicy.ALLOW_DUPLICATES )
		{
			nodeTable = new IncrementalTreeNodeTableWithDuplicates();
		}
		else if ( duplicatePolicy == DuplicatePolicy.REQUIRE_UNIQUES )
		{
			nodeTable = new IncrementalTreeNodeTableWithUniques();
		}
		else
		{
			throw new RuntimeException( "Invalid duplicate policy" );
		}
		
		bRefreshRequired = false;
	}
	
	
	public void setRefreshListener(RefreshListener listener)
	{
		refreshListener = listener;
	}
	
	
	protected abstract IncrementalTreeNode.NodeResultFactory getRootNodeResultFactory();
	
	
	protected IncrementalTreeNode getRootIncrementalTreeNode()
	{
		if ( rootIncrementalTreeNode != null )
		{
			nodeTable.unrefIncrementalNode( rootIncrementalTreeNode );
		}
		if ( rootIncrementalTreeNode == null )
		{
			rootIncrementalTreeNode = buildIncrementalTreeNodeResult( modelRootNode, getRootNodeResultFactory() );
		}
		if ( rootIncrementalTreeNode != null )
		{
			nodeTable.refIncrementalNode( rootIncrementalTreeNode );
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
			IncrementalTreeNode viewNode = nodeTable.getUnrefedIncrementalNodeFor( node, elementFactory );
			
			if ( viewNode == null )
			{
				// No existing incremental tree node could be acquired.
				// Create a new one and add it to the table
				viewNode = createIncrementalTreeNode( node );
			}
			
			viewNode.setNodeResultFactory( elementFactory );
			
			return viewNode;
		}
	}
	
	
	
	
	protected void performRefresh()
	{
		onResultChangeTreeRefresh();
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
				onRequestRefresh();
				
				if ( refreshListener != null )
				{
					refreshListener.onIncrementalTreeRequestRefresh( this );
				}
			}
		}
	}
	
	
	
	protected abstract IncrementalTreeNode createIncrementalTreeNode(Object node);
	
	
	protected void onResultChangeTreeRefresh()
	{
	}
	
	protected void onResultChangeFrom(IncrementalTreeNode node, Object result)
	{
	}
	
	protected void onResultChangeTo(IncrementalTreeNode node, Object result)
	{
	}
	
	
	protected void onRequestRefresh()
	{
	}
}
