//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import BritefuryJ.Cell.Cell;
import BritefuryJ.Cell.CellEvaluator;
import BritefuryJ.DocTree.DocTree;
import BritefuryJ.DocTree.DocTreeNode;

public class DocView
{
	public interface RootNodeInitialiser
	{
		public void initRootNode(DVNode rootView, Object rootDocNode);
	}
	
	
	private DocTreeNode root;
	private RootNodeInitialiser rootNodeInitialiser;
	private Cell refreshCell;
	protected DocViewNodeTable nodeTable;
	private DVNode rootView;
	private DVNode.NodeElementChangeListener elementChangeListener;
	
	
	public DocView(DocTree tree, DocTreeNode root, RootNodeInitialiser rootNodeInitialiser, DVNode.NodeElementChangeListener elementChangeListener)
	{
		this.root = root;
		this.rootNodeInitialiser = rootNodeInitialiser;
		
		final DocView view = this;
		CellEvaluator refreshEval = new CellEvaluator()
		{
			public Object evaluate()
			{
				view.performRefresh();
				return null;
			}
		};
		refreshCell = new Cell();
		refreshCell.setEvaluator( refreshEval );
		
		nodeTable = new DocViewNodeTable();
		
		this.elementChangeListener = elementChangeListener;
	}
	
	
	public DVNode getRootView()
	{
		if ( rootView == null )
		{
			rootView = buildNodeView( root );
			rootNodeInitialiser.initRootNode( rootView, root );
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
		refreshCell.getValue();
	}
	
	public Cell getRefreshCell()
	{
		return refreshCell;
	}
}
