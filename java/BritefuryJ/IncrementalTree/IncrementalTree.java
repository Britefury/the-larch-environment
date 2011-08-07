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
	IncrementalTreeNode.NodeResultFactory rootNodeResultFactory;
	
	
	
	
	
	
	
	
	
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
	
	
	protected void setRootNodeResultFactory(IncrementalTreeNode.NodeResultFactory rootNodeResultFactory)
	{
		if ( rootNodeResultFactory != this.rootNodeResultFactory )
		{
			this.rootNodeResultFactory = rootNodeResultFactory;
			
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
	
	
	protected IncrementalTreeNode getRootIncrementalTreeNode()
	{
		if ( rootNodeResultFactory == null )
		{
			throw new RuntimeException( "No root node result factory set" );
		}
		
		
		if ( rootIncrementalTreeNode != null )
		{
			nodeTable.unrefIncrementalNode( rootIncrementalTreeNode );
		}
		if ( rootIncrementalTreeNode == null )
		{
			rootIncrementalTreeNode = buildIncrementalTreeNodeResult( modelRootNode, rootNodeResultFactory );
		}
		if ( rootIncrementalTreeNode != null )
		{
			nodeTable.refIncrementalNode( rootIncrementalTreeNode );
		}
		return rootIncrementalTreeNode;
	}
	
	
	public IncrementalTreeNode buildIncrementalTreeNodeResult(Object model, IncrementalTreeNode.NodeResultFactory resultFactory)
	{
		if ( model == null )
		{
			return null;
		}
		else
		{
			// Try asking the table for an unused incremental tree node for the document node
			IncrementalTreeNode resultNode = nodeTable.getUnrefedIncrementalNodeFor( model, resultFactory );
			
			if ( resultNode == null )
			{
				// No existing incremental tree node could be acquired.
				// Create a new one and add it to the table
				resultNode = createIncrementalTreeNode( model );
			}
			
			resultNode.setNodeResultFactory( resultFactory );
			
			return resultNode;
		}
	}
	
	
	
	
	protected void performRefresh()
	{
		onResultChangeTreeRefreshBegin();
		IncrementalTreeNode node = getRootIncrementalTreeNode();
		if ( node != null )
		{
			node.refresh();
		}
		onResultChangeTreeRefreshEnd();
		
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
	
	
	protected void onResultChangeTreeRefreshBegin()
	{
	}
	
	protected void onResultChangeTreeRefreshEnd()
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
