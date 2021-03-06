//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.IncrementalTree;

import java.util.Iterator;

import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;

public abstract class IncrementalTreeNode implements IncrementalMonitorListener
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
	
	

	
	protected final static int FLAG_SUBTREE_REFRESH_REQUIRED = 0x1;
	protected final static int FLAG_NODE_REFRESH_REQUIRED = 0x2;
	protected final static int FLAG_NODE_REFRESH_IN_PROGRESS = 0x4;
	
	protected final static int FLAGS_INCREMENTALTREENODE_END = 0x8;

	
	
	private IncrementalTree incrementalTree;
	private Object model;
	
	private IncrementalFunctionMonitor incr;
	protected NodeResultFactory resultFactory;
	
	private IncrementalTreeNode parent, nextSibling;
	private IncrementalTreeNode childrenHead, childrenTail;
	
	private int flags;
	
	
	
	
	public IncrementalTreeNode(IncrementalTree incrementalTree, Object model)
	{
		setFlag( FLAG_SUBTREE_REFRESH_REQUIRED );
		setFlag( FLAG_NODE_REFRESH_REQUIRED );

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
		setFlag( FLAG_NODE_REFRESH_IN_PROGRESS );
		
		Object result = getResultNoRefresh();
		incrementalTree.onResultChangeFrom( this, result );

		Object r = result;
		if ( testFlag( FLAG_NODE_REFRESH_REQUIRED ) )
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
		
		if ( testFlag( FLAG_NODE_REFRESH_REQUIRED ) )
		{
			incr.onAccess();
			// Set the node result
			updateNodeResult( r );
		}
		
		
		incrementalTree.onResultChangeTo( this, result );
		clearFlag( FLAG_NODE_REFRESH_REQUIRED );

		clearFlag( FLAG_NODE_REFRESH_IN_PROGRESS );
	}
	
	
	public void refresh()
	{
		if ( testFlag( FLAG_SUBTREE_REFRESH_REQUIRED ) )
		{
			refreshSubtree();
			clearFlag( FLAG_SUBTREE_REFRESH_REQUIRED );
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

		// Ref the node, so that it is kept around
		incrementalTree.nodeTable.refIncrementalNode( child );
	}
	




	//
	//
	// Cell notifications
	//
	//
	
	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		if ( !testFlag( FLAG_NODE_REFRESH_REQUIRED ) )
		{
			setFlag( FLAG_NODE_REFRESH_REQUIRED );
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
		if ( !testFlag( FLAG_SUBTREE_REFRESH_REQUIRED ) )
		{
			setFlag( FLAG_SUBTREE_REFRESH_REQUIRED );
			if ( parent != null )
			{
				parent.requestSubtreeRefresh();
			}
			
			incrementalTree.onNodeRequestRefresh( this );
		}
	}




	//
	//
	// Flag methods
	//
	//
	
	protected void clearFlag(int flag)
	{
		flags &= ~flag;
	}
	
	protected void setFlag(int flag)
	{
		flags |= flag;
	}
	
	protected void setFlagValue(int flag, boolean value)
	{
		if ( value )
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
	}
	
	protected boolean testFlag(int flag)
	{
		return ( flags & flag )  !=  0;
	}
}
