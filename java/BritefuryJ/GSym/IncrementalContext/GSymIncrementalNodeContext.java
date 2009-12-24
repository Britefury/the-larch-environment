//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.IncrementalContext;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.python.core.PyObject;

import BritefuryJ.Cell.CellInterface;
import BritefuryJ.DocModel.DMNode;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class GSymIncrementalNodeContext implements IncrementalTreeNode.NodeContext
{
	protected GSymIncrementalTreeContext treeContext;
	protected IncrementalTreeNode treeNode;
	
	
	public GSymIncrementalNodeContext(GSymIncrementalTreeContext treeContext, IncrementalTreeNode treeNode)
	{
		this.treeContext = treeContext;
		this.treeNode = treeNode;
		this.treeNode.setContext( this );
	}
	
	
	
	
	
	
	protected void registerIncrementalNodeRelationship(IncrementalTreeNode childNode)
	{
		treeNode.registerChild( childNode );
	}



	public Object eval(DMNode x)
	{
		return evalFn( x, (GSymIncrementalNodeFunction)null, null );
	}

	public Object eval(DMNode x, Object state)
	{
		return evalFn( x, (GSymIncrementalNodeFunction)null, state );
	}

	public Object evalFn(DMNode x, GSymIncrementalNodeFunction nodeViewFunction)
	{
		return evalFn( x, nodeViewFunction, null );
	}

	public Object evalFn(DMNode x, GSymIncrementalNodeFunction nodeViewFunction, Object state)
	{
		if ( x == null )
		{
			throw new RuntimeException( "GSymNodeViewInstance.viewEvanFn(): cannot build view of null node" );
		}
		
		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		IncrementalTreeNode incrementalNode = treeContext.getIncrementalTree().buildIncrementalTreeNodeResult( x, treeContext.makeNodeResultFactory( nodeViewFunction, state ) );
		
		
		// Block access tracking to prevent the contents of this node being dependent upon the child node being refreshed,
		// and refresh the view node
		// Refreshing the child node will ensure that when its contents are inserted into outer elements, its full element tree
		// is up to date and available.
		// Blocking the access tracking prevents an inner node from causing all parent/grandparent/etc nodes from requiring a
		// refresh.
		WeakHashMap<CellInterface, Object> accessList = CellInterface.blockAccessTracking();
		incrementalNode.refresh();
		CellInterface.unblockAccessTracking( accessList );
		
		registerIncrementalNodeRelationship( incrementalNode );
		
		return incrementalNode.getResultNoRefresh();
	}
	
	public Object evalFn(DMNode x, PyObject nodeViewFunction)
	{
		return evalFn( x, new PyGSymIncrementalNodeFunction( nodeViewFunction ), null );
	}

	public Object evalFn(DMNode x, PyObject nodeViewFunction, Object state)
	{
		return evalFn( x, new PyGSymIncrementalNodeFunction( nodeViewFunction ), state );
	}
	
	
	
	
	public List<Object> mapEval(List<DMNode> xs)
	{
		return mapEvalFn( xs, (GSymIncrementalNodeFunction)null, null );
	}

	public List<Object> mapEval(List<DMNode> xs, Object state)
	{
		return mapEvalFn( xs, (GSymIncrementalNodeFunction)null, state );
	}

	public List<Object> mapEvalFn(List<DMNode> xs, GSymIncrementalNodeFunction nodeViewFunction)
	{
		return mapEvalFn( xs, nodeViewFunction, null );
	}

	public List<Object> mapEvalFn(List<DMNode> xs, GSymIncrementalNodeFunction nodeViewFunction, Object state)
	{
		ArrayList<Object> children = new ArrayList<Object>();
		children.ensureCapacity( xs.size() );
		for (DMNode x: xs)
		{
			children.add( evalFn( x, nodeViewFunction, state ) );
		}
		return children;
	}
	
	public List<Object> mapEvalFn(List<DMNode> xs, PyObject nodeViewFunction)
	{
		return mapEvalFn( xs, new PyGSymIncrementalNodeFunction( nodeViewFunction ), null );
	}

	public List<Object> mapEvalFn(List<DMNode> xs, PyObject nodeViewFunction, Object state)
	{
		return mapEvalFn( xs, new PyGSymIncrementalNodeFunction( nodeViewFunction ), state );
	}
	
	
	
	public Object getDocNode()
	{
		return treeNode.getDocNode();
	}
	
	
	
	public Object getTreeNodeResult()
	{
		return treeNode.getResultNoRefresh();
	}
	
	
	public GSymIncrementalNodeContext getParent()
	{
		IncrementalTreeNode parentViewNode = (IncrementalTreeNode)treeNode.getParent();
		return parentViewNode != null  ?  (GSymIncrementalNodeContext)parentViewNode.getContext()  :  null;
	}
	

	public ArrayList<GSymIncrementalNodeContext> getNodeViewInstancePathFromRoot()
	{
		ArrayList<GSymIncrementalNodeContext> path = new ArrayList<GSymIncrementalNodeContext>();
		
		GSymIncrementalNodeContext n = this;
		while ( n != null )
		{
			path.add( 0, n );
			n = n.getParent();
		}
		
		return path;
	}
	
	public ArrayList<GSymIncrementalNodeContext> getNodeViewInstancePathFromSubtreeRoot(GSymIncrementalNodeContext root)
	{
		ArrayList<GSymIncrementalNodeContext> path = new ArrayList<GSymIncrementalNodeContext>();
		
		GSymIncrementalNodeContext n = this;
		while ( n != null )
		{
			path.add( 0, n );
			if ( n == root )
			{
				return path;
			}
			n = n.getParent();
		}

		return null;
	}
	
	
	public GSymIncrementalTreeContext getTreeContext()
	{
		return treeContext;
	}
}
