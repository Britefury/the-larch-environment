//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalTree;

import BritefuryJ.Cell.Cell;
import BritefuryJ.Cell.CellEvaluator;
import BritefuryJ.Cell.CellInterface;
import BritefuryJ.Cell.CellListener;
import BritefuryJ.DocModel.DMNode;

public class IncrementalTreeNode implements CellListener
{
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	public static interface NodeResultFactory
	{
		public Object createNodeResult(IncrementalTreeNode viewNode, DMNode docNode);
	}
	
	public static interface NodeResultChangeListener
	{
		public void reset(IncrementalTree view);
		public void resultChangeFrom(IncrementalTreeNode node, Object result);
		public void resultChangeTo(IncrementalTreeNode node, Object result);
	}
	
	public static interface NodeContext
	{
	};
	
	

	
	
	private IncrementalTree incrementalTree;
	private DMNode docNode;
	
	private Cell resultCell;
	private NodeResultFactory resultFactory;
	private Object result;
	
	private NodeResultChangeListener resultChangeListener;
	
	private boolean bRefreshRequired;
	
	private IncrementalTreeNode parent, nextSibling;
	private IncrementalTreeNode childrenHead, childrenTail;
	
	
	
	public IncrementalTreeNode(IncrementalTree incrementalTree, DMNode docNode, NodeResultChangeListener elementChangeListener)
	{
		this.incrementalTree = incrementalTree;
		this.docNode = docNode;
		
		parent = null;
		nextSibling = null;
		childrenHead = childrenTail = null;
		
		
		final IncrementalTreeNode self = this;
		bRefreshRequired = true;
		
		
		resultFactory = null;

		CellEvaluator elementEval = new CellEvaluator()
		{
			public Object evaluate()
			{
				return self.computeNodeResult();
			}
		};
		resultCell = new Cell();
		resultCell.setEvaluator( elementEval );
		resultCell.addListener( this );
		
		
		this.resultChangeListener = elementChangeListener;
	}
	
	
	
	//
	// Set the node element factory
	//
	protected void setNodeResultFactory(NodeResultFactory factory)
	{
		if ( factory != resultFactory )
		{
			resultFactory = factory;
			resultCell.setEvaluator( resultCell.getEvaluator() );
		}
	}
	
	protected NodeResultFactory getNodeResultFactory()
	{
		return resultFactory;
	}
	
	
	
	//
	//
	// Content acquisition methods
	//
	//
	
	public Object getResultNoRefresh()
	{
		return result;
	}
	
	public Object getResult()
	{
		refresh();
		return result;
	}
	
	
	
	
	//
	//
	// Structure / doc node methods
	//
	//
	
	public IncrementalTree getIncrementalTree()
	{
		return incrementalTree;
	}
	
	public IncrementalTreeNode getParent()
	{
		return parent;
	}
	

	public DMNode getDocNode()
	{
		return docNode;
	}
	
	
	
	//
	//
	// Refresh methods
	//
	//
	
	private void refreshNode()
	{
		if ( resultChangeListener != null )
		{
			resultChangeListener.resultChangeFrom( this, result );
		}

		// Compute the result for this node, and refresh all children
		Object r = resultCell.getValue();
		
		// Refresh each child
		IncrementalTreeNode child = childrenHead;
		while ( child != null )
		{
			child.refresh();
			child = child.nextSibling;
		}
		
		// Set the node result
		updateNodeResult( r );
		
		
		if ( resultChangeListener != null )
		{
			resultChangeListener.resultChangeTo( this, result );
		}
	}
	
	
	public void refresh()
	{
		if ( bRefreshRequired )
		{
			refreshNode();
			bRefreshRequired = false;
		}
	}
	
	
	protected Object computeNodeResult()
	{
		// Unregister existing child relationships
		IncrementalTreeNode child = childrenHead;
		while ( child != null )
		{
			IncrementalTreeNode next = child.nextSibling;

			incrementalTree.nodeTable.unrefIncrementalNode( child );
			child.parent = null;
			child.nextSibling = null;
			
			child = next;
		}
		childrenHead = childrenTail = null;
		
		if ( resultFactory != null )
		{
			Object r = resultFactory.createNodeResult( this, docNode );
			
			// Register new child relationships
			child = childrenHead;
			while ( child != null )
			{
				incrementalTree.nodeTable.refIncrementalNode( child );
				child = child.nextSibling;
			}
			return r;
		}
		else
		{
			return null;
		}
	}

	
	protected void updateNodeResult(Object r)
	{
		result = r;
	}
	
	
	
	
	//
	//
	// Child / parent relationship methods
	//
	//
	
	public void registerChild(IncrementalTreeNode child)
	{
		assert child.parent == null  ||  child.parent == this;

		// Append child to the list of children
		if ( childrenTail != null )
		{
			childrenTail.nextSibling = child;
		}

		if ( childrenHead == null )
		{
			childrenHead = child;
		}
		
		childrenTail = child;

		child.parent = this;
	}
	




	//
	//
	// Cell notifications
	//
	//
	
	public void onCellChanged(CellInterface cell)
	{
		assert cell == resultCell;
		requestRefresh();
	}

	public void onCellEvaluator(CellInterface cell, CellEvaluator oldEval, CellEvaluator newEval)
	{
	}
	
	public void onCellValidity(CellInterface cell)
	{
	}
	
	
	
	//
	//
	// Child notifications
	//
	//
	
	public void onChildRefreshRequired()
	{
		requestRefresh();
	}
	
	
	
	//
	//
	// Refresh request
	//
	//
	
	private void requestRefresh()
	{
		if ( !bRefreshRequired )
		{
			bRefreshRequired = true;
			if ( parent != null )
			{
				parent.onChildRefreshRequired();
			}
			
			incrementalTree.onNodeRequestRefresh( this );
		}
	}
}
