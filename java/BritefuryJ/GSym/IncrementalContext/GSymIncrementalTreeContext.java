//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.IncrementalContext;

import java.util.HashMap;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public abstract class GSymIncrementalTreeContext
{
	protected static abstract class NodeContextAndResultFactory implements IncrementalTreeNode.NodeResultFactory
	{
		protected GSymIncrementalTreeContext treeContext;
		protected Object state;
		
		
		protected NodeContextAndResultFactory(GSymIncrementalTreeContext treeContext, Object state)
		{
			this.treeContext = treeContext;
			this.state = state;
		}

		
		public abstract Object createNodeResult(IncrementalTreeNode incrementalNode, Object docNode);
	}
	
	
	protected static class InheritedState
	{
	}
	
	
	protected static class NodeContextAndResultFactoryKey
	{
	}
	
	
	
	private Object docRootNode;
	
	private GSymIncrementalNodeFunction generalNodeFunction;
	
	protected IncrementalTree incrementalTree;
	
	private HashMap<NodeContextAndResultFactoryKey, NodeContextAndResultFactory> nodeContextAndResultFactories;
	
	
	public GSymIncrementalTreeContext(Object docRootNode, GSymIncrementalNodeFunction generalNodeFunction, GSymIncrementalNodeFunction rootNodeFunction, InheritedState rootInheritedState)
	{
		this.docRootNode = docRootNode;
		
		this.generalNodeFunction = generalNodeFunction; 
		
		nodeContextAndResultFactories = new HashMap<NodeContextAndResultFactoryKey, NodeContextAndResultFactory>();

		incrementalTree = createIncrementalTree( docRootNode, makeNodeResultFactory( rootNodeFunction, rootInheritedState ) );
	}
	
	
	public GSymIncrementalTreeContext(DMNode docRootNode, GSymIncrementalNodeFunction nodeFunction, InheritedState rootInheritedState)
	{
		this( docRootNode, nodeFunction, nodeFunction, rootInheritedState );
	}

	
	
	protected abstract IncrementalTree createIncrementalTree(Object docRootNode, IncrementalTreeNode.NodeResultFactory resultFactory);
	
	protected abstract NodeContextAndResultFactory createContextAndResultFactory(GSymIncrementalTreeContext treeContext, GSymIncrementalNodeFunction nodeFunction, InheritedState inheritedState);
	
	protected abstract NodeContextAndResultFactoryKey createFragmentKey(GSymIncrementalNodeFunction nodeFunction, InheritedState inheritedState);

	
	protected DVNode.NodeResultFactory makeNodeResultFactory(GSymIncrementalNodeFunction nodeFunction, InheritedState inheritedState)
	{
		// Memoise the contents factory, keyed by  @nodeViewFunction and @state
		if ( nodeFunction == null )
		{
			nodeFunction = generalNodeFunction;
		}

		NodeContextAndResultFactoryKey key = createFragmentKey( nodeFunction, inheritedState );
		
		NodeContextAndResultFactory factory = nodeContextAndResultFactories.get( key );
		
		if ( factory == null )
		{
			factory = createContextAndResultFactory( this, nodeFunction, inheritedState );
			nodeContextAndResultFactories.put( key, factory );
			return factory;
		}
		
		return factory;
	}
	
	
	
	public IncrementalTree getIncrementalTree()
	{
		return incrementalTree;
	}
	
	
	
	public Object getDocRootNode()
	{
		return docRootNode;
	}
	
	
	
	public void refreshTree()
	{
		incrementalTree.refresh();
	}
}
