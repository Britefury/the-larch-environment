//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalTree;

import java.util.Iterator;

import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalOwner;
import BritefuryJ.Incremental.IncrementalMonitorListener;

public abstract class IncrementalTreeNode implements IncrementalMonitorListener, IncrementalOwner
{
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static interface NodeResultFactory
	{
		public Object createNodeResult(IncrementalTreeNode viewNode, Object docNode);
	}
	
	public static class ChildrenIterator implements Iterator<IncrementalTreeNode>
	{
		private IncrementalTreeNode current;
		
		
		
		private ChildrenIterator(IncrementalTreeNode childrenHead)
		{
			current = childrenHead;
		}

		
		@Override
		public boolean hasNext()
		{
			return current != null;
		}

		@Override
		public IncrementalTreeNode next()
		{
			IncrementalTreeNode res = current;
			current = current.nextSibling;
			return res;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	
	public static class ChildrenIterable implements Iterable<IncrementalTreeNode>
	{
		private IncrementalTreeNode node;
		
		private ChildrenIterable(IncrementalTreeNode node)
		{
			this.node = node;
		}
		
		
		@Override
		public Iterator<IncrementalTreeNode> iterator()
		{
			return new ChildrenIterator( node.childrenHead );
		}
	}
	
	

	
	
	private IncrementalTree incrementalTree;
	private Object model;
	
	private IncrementalFunctionMonitor incr;
	protected NodeResultFactory resultFactory;
	
	private IncrementalTreeNode parent, nextSibling;
	private IncrementalTreeNode childrenHead, childrenTail;
	
	private boolean bSubtreeRefreshRequired = true, bNodeRefreshRequired = true;
	
	
	
	
	public IncrementalTreeNode(IncrementalTree incrementalTree, Object model)
	{
		this.incrementalTree = incrementalTree;
		this.model = model;
		
		parent = null;
		nextSibling = null;
		childrenHead = childrenTail = null;
		
		
		resultFactory = null;

		incr = new IncrementalFunctionMonitor( this );
		incr.addListener( this );
	}
	
	protected void dispose()
	{
		incr.removeListener( this );
	}
	
	
	
	//
	// Set the node element factory
	//
	protected void setNodeResultFactory(NodeResultFactory factory)
	{
		if ( factory != resultFactory )
		{
			resultFactory = factory;
			incr.onChanged();
		}
	}
	
	protected NodeResultFactory getNodeResultFactory()
	{
		return resultFactory;
	}
	
	
	
	//
	//
	// Result acquisition methods
	//
	//
	
	protected abstract Object getResultNoRefresh();
	
	
	
	
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
	
	protected ChildrenIterable getChildren()
	{
		return new ChildrenIterable( this );
	}
	

	public Object getModel()
	{
		return model;
	}
	
	
	public int computeSubtreeSize()
	{
		int subtreeSize = 1;
		IncrementalTreeNode child = childrenHead;
		while ( child != null )
		{
			subtreeSize += child.computeSubtreeSize();
			child = child.nextSibling;
		}
		return subtreeSize;
	}
	

	
	//
	//
	// Refresh methods
	//
	//
	
	private void refreshSubtree()
	{
		Object result = getResultNoRefresh();
		incrementalTree.onResultChangeFrom( this, result );

		Object r = result;
		if ( bNodeRefreshRequired )
		{
			// Compute the result for this node, and refresh all children
			Object refreshState = incr.onRefreshBegin();
			if ( refreshState != null )
			{
				r = computeNodeResult();
			}
			incr.onRefreshEnd( refreshState );
		}
		
		// Refresh each child
		IncrementalTreeNode child = childrenHead;
		while ( child != null )
		{
			child.refresh();
			child = child.nextSibling;
		}
		
		if ( bNodeRefreshRequired )
		{
			incr.onAccess();
			// Set the node result
			updateNodeResult( r );
		}
		
		
		incrementalTree.onResultChangeTo( this, result );
		bNodeRefreshRequired = false;
	}
	
	
	public void refresh()
	{
		if ( bSubtreeRefreshRequired )
		{
			refreshSubtree();
			bSubtreeRefreshRequired = false;
		}
	}
	
	public void queueRefresh()
	{
		incr.onChanged();
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
		onComputeNodeResultBegin();
		
		if ( resultFactory != null )
		{
			Object r = resultFactory.createNodeResult( this, model );
			
			// Register new child relationships
			child = childrenHead;
			while ( child != null )
			{
				incrementalTree.nodeTable.refIncrementalNode( child );
				child = child.nextSibling;
			}
			onComputeNodeResultEnd();
			return r;
		}
		else
		{
			onComputeNodeResultEnd();
			return null;
		}
	}
	
	protected void onComputeNodeResultBegin()
	{
	}
	
	protected void onComputeNodeResultEnd()
	{
	}

	
	protected abstract void updateNodeResult(Object r);
	
	
	
	
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
	
	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		if ( !bNodeRefreshRequired )
		{
			bNodeRefreshRequired = true;
			requestSubtreeRefresh();
		}
	}

	
	
	//
	//
	// Child notifications
	//
	//
	
	protected void requestSubtreeRefresh()
	{
		if ( !bSubtreeRefreshRequired )
		{
			bSubtreeRefreshRequired = true;
			if ( parent != null )
			{
				parent.requestSubtreeRefresh();
			}
			
			incrementalTree.onNodeRequestRefresh( this );
		}
	}
}
