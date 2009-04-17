//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import BritefuryJ.DocTree.DocTree;
import BritefuryJ.DocTree.DocTreeNode;

public class DocView implements DVNode.NodeRefreshListener
{
	public interface RootNodeInitialiser
	{
		public void initRootNode(DVNode rootView, Object rootDocNode);
	}
	
	public interface RefreshListener
	{
		public void onViewRequestRefresh(DocView view);
	}
	
	
	private DocTreeNode root;
	private RootNodeInitialiser rootNodeInitialiser;
	protected DocViewNodeTable nodeTable;
	private DVNode rootView;
	private DVNode.NodeElementChangeListener elementChangeListener;
	private boolean bRefreshRequired;
	private RefreshListener refreshListener;
	
	
	public DocView(DocTree tree, DocTreeNode root, RootNodeInitialiser rootNodeInitialiser, DVNode.NodeElementChangeListener elementChangeListener)
	{
		this.root = root;
		this.rootNodeInitialiser = rootNodeInitialiser;
		
		nodeTable = new DocViewNodeTable();
		
		this.elementChangeListener = elementChangeListener;
		
		bRefreshRequired = true;
	}
	
	
	public void setRefreshListener(RefreshListener listener)
	{
		refreshListener = listener;
	}
	
	
	public DVNode getRootView()
	{
		if ( rootView == null )
		{
			rootView = buildNodeView( root );
			rootNodeInitialiser.initRootNode( rootView, root );
			rootView.setRefreshListener( this );
		}
		return rootView;
	}
	
	
	
	public DVNode buildNodeView(DocTreeNode treeNode)
	{
		if ( treeNode == null )
		{
			return null;
		}
		else
		{
			// Try to get a view node for @treeNode from the node table; one will be returned if a view node exists, else null.
			DVNode viewNode = nodeTable.get( treeNode );
			
			if ( viewNode == null )
			{
				// No view node in the table.
				// Try asking the table for an unused one
				viewNode = nodeTable.takeUnusedViewNodeFor( treeNode );
				
				if ( viewNode == null )
				{
					// No existing view node could be acquired.
					// Create a new one
					viewNode = new DVNode( this, treeNode, elementChangeListener );
					nodeTable.put( treeNode, viewNode );
				}
			}
			
			return viewNode;
		}
	}
	
	
	public DVNode getViewNodeForDocTreeNode(DocTreeNode treeNode)
	{
		return nodeTable.get( treeNode );
	}
	
	
	public DVNode refreshAndGetViewNodeForDocTreeNode(DocTreeNode treeNode)
	{
		refresh();
		return nodeTable.get( treeNode );
	}
	
	
	
	private void performRefresh()
	{
		getRootView().refresh();
		
		// Clear unused entries from the node table
		nodeTable.clearUnused();
	}
	
	
	public void refresh()
	{
		if ( bRefreshRequired )
		{
			bRefreshRequired = false;
			performRefresh();
		}
	}
	

	
	public void onNodeRequestRefresh(DVNode node)
	{
		if ( !bRefreshRequired )
		{
			bRefreshRequired = true;
			
			if ( refreshListener != null )
			{
				refreshListener.onViewRequestRefresh( this );
			}
		}
	}
}
