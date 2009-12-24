//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.IncrementalContext;

import java.util.HashMap;

import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public abstract class GSymIncrementalTreeContext
{
	public static abstract class NodeContextAndResultFactory implements IncrementalTreeNode.NodeResultFactory
	{
		protected GSymIncrementalTreeContext treeContext;
		protected GSymIncrementalNodeFunction nodeFunction;
		protected Object state;
		
		
		protected NodeContextAndResultFactory(GSymIncrementalTreeContext treeContext, GSymIncrementalNodeFunction nodeFunction, Object state)
		{
			assert nodeFunction != null;
			
			this.treeContext = treeContext;
			this.nodeFunction = nodeFunction;
			this.state = state;
		}

		
		protected GSymIncrementalNodeContext createContext(GSymIncrementalTreeContext treeContext, IncrementalTreeNode incrementalNode) 
		{
			return new GSymIncrementalNodeContext( treeContext, incrementalNode );
		}

		public Object createNodeResult(IncrementalTreeNode incrementalNode, DMNode docNode)
		{
			// Create the node context
			GSymIncrementalNodeContext nodeContext = createContext( treeContext, incrementalNode );
			
			// Compute and return the result
			return nodeFunction.computeNodeResult( docNode, nodeContext, state );
		}
	}
	
	
	private static class NodeContextAndResultFactoryKey
	{
		private GSymIncrementalNodeFunction nodeFunction;
		private Object state;
		
		
		public NodeContextAndResultFactoryKey(GSymIncrementalNodeFunction nodeFunction, Object state)
		{
			this.nodeFunction = nodeFunction;
			this.state = state;
		}
		
		
		public int hashCode()
		{
			int stateHash = state != null  ?  state.hashCode()  :  0;
			int mult = 1000003;
			int x = 0x345678;
			x = ( x ^ nodeFunction.hashCode() ) * mult;
			mult += 82520 + 2;
			x = ( x ^ stateHash ) * mult;
			return x + 97351;
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof NodeContextAndResultFactoryKey )
			{
				NodeContextAndResultFactoryKey kx = (NodeContextAndResultFactoryKey)x;
				if ( state == null  ||  kx.state == null )
				{
					return nodeFunction == kx.nodeFunction  &&  ( state != null ) == ( kx.state != null );
				}
				else
				{
					return nodeFunction == kx.nodeFunction  &&  state.equals( kx.state );
				}
			}
			else
			{
				return false;
			}
		}
	}
	
	
	
	private DMNode docRootNode;
	
	private GSymIncrementalNodeFunction generalNodeFunction;
	
	protected IncrementalTree incrementalTree;
	
	private HashMap<NodeContextAndResultFactoryKey, NodeContextAndResultFactory> nodeContextAndResultFactories;
	
	
	public GSymIncrementalTreeContext(DMNode docRootNode, GSymIncrementalNodeFunction generalNodeFunction, GSymIncrementalNodeFunction rootNodeFunction)
	{
		this.docRootNode = docRootNode;
		
		this.generalNodeFunction = generalNodeFunction; 
		
		nodeContextAndResultFactories = new HashMap<NodeContextAndResultFactoryKey, NodeContextAndResultFactory>();

		incrementalTree = createIncrementalTree( docRootNode, makeNodeResultFactory( rootNodeFunction, null ) );
	}
	
	
	public GSymIncrementalTreeContext(DMNode docRootNode, PyObject generalNodeFunction, PyObject rootNodeFunction)
	{
		this( docRootNode, new PyGSymIncrementalNodeFunction( generalNodeFunction ), new PyGSymIncrementalNodeFunction( generalNodeFunction ) );
	}

	
	public GSymIncrementalTreeContext(DMNode docRootNode, GSymIncrementalNodeFunction nodeFunction)
	{
		this( docRootNode, nodeFunction, nodeFunction );
	}

	public GSymIncrementalTreeContext(DMNode docRootNode, PyObject nodeFunction)
	{
		this( docRootNode, new PyGSymIncrementalNodeFunction( nodeFunction ) );
	}

	
	
	protected abstract IncrementalTree createIncrementalTree(DMNode docRootNode, IncrementalTreeNode.NodeResultFactory resultFactory);
	
	protected abstract NodeContextAndResultFactory createContextAndResultFactory(GSymIncrementalTreeContext treeContext, GSymIncrementalNodeFunction nodeFunction, Object state);

	
	protected DVNode.NodeResultFactory makeNodeResultFactory(GSymIncrementalNodeFunction nodeFunction, Object state)
	{
		// Memoise the contents factory, keyed by  @nodeViewFunction and @state
		if ( nodeFunction == null )
		{
			nodeFunction = generalNodeFunction;
		}

		NodeContextAndResultFactoryKey key = new NodeContextAndResultFactoryKey( nodeFunction, state );
		
		NodeContextAndResultFactory factory = nodeContextAndResultFactories.get( key );
		
		if ( factory == null )
		{
			factory = createContextAndResultFactory( this, nodeFunction, state );
			nodeContextAndResultFactories.put( key, factory );
			return factory;
		}
		
		return factory;
	}
	
	
	
	public IncrementalTree getIncrementalTree()
	{
		return incrementalTree;
	}
	
	
	
	public DMNode getDocRootNode()
	{
		return docRootNode;
	}
	
	
	
	public void refreshTree()
	{
		incrementalTree.refresh();
	}
}
