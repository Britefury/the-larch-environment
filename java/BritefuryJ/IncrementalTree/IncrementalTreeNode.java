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

public class IncrementalTreeNode implements IncrementalMonitorListener, IncrementalOwner
{
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static interface NodeResultFactory
	{
		public Object createNodeResult(IncrementalTreeNode viewNode, Object docNode);
	}
	
	public static interface NodeResultChangeListener
	{
		public void reset(IncrementalTree view);
		public void resultChangeFrom(IncrementalTreeNode node, Object result);
		public void resultChangeTo(IncrementalTreeNode node, Object result);
	}
	
	public static interface NodeContext
	{
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
	private Object docNode;
	
	private IncrementalFunctionMonitor incr;
	private NodeResultFactory resultFactory;
	private Object result;
	
	private NodeResultChangeListener resultChangeListener;
	
	private IncrementalTreeNode parent, nextSibling;
	private IncrementalTreeNode childrenHead, childrenTail;
	
	private NodeContext nodeContext;
	
	private boolean bRefreshRequired;
	
	
	
	
	public IncrementalTreeNode(IncrementalTree incrementalTree, Object docNode, NodeResultChangeListener elementChangeListener)
	{
		this.incrementalTree = incrementalTree;
		this.docNode = docNode;
		nodeContext = null;
		
		parent = null;
		nextSibling = null;
		childrenHead = childrenTail = null;
		
		
		bRefreshRequired = true;
		
		
		resultFactory = null;

		incr = new IncrementalFunctionMonitor( this );
		incr.addListener( this );
		
		
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
			incr.onChanged();
		}
	}
	
	public NodeResultFactory getNodeResultFactory()
	{
		return resultFactory;
	}
	
	
	
	//
	//
	// Result acquisition methods
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
	
	public ChildrenIterable getChildren()
	{
		return new ChildrenIterable( this );
	}
	

	public Object getDocNode()
	{
		return docNode;
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
	// Context methods
	//
	//
	
	public NodeContext getContext()
	{
		return nodeContext;
	}
	
	public void setContext(NodeContext context)
	{
		nodeContext = context;
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
		Object refreshState = incr.onRefreshBegin();
		Object r = result;
		if ( refreshState != null )
		{
			r = computeNodeResult();
		}
		incr.onRefreshEnd( refreshState );
		incr.onAccess();
		
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
			Object r = resultFactory.createNodeResult( this, docNode );
			
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
	
	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		//assert cell == resultCell;
		requestRefresh();
	}

	
	
	//
	//
	// Child notifications
	//
	//
	
	protected void onChildRefreshRequired()
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
