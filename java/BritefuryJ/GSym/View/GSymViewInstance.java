//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.HashMap;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.StyleSheets.BorderStyleSheet;
import BritefuryJ.DocTree.DocTree;
import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;

public class GSymViewInstance
{
	protected static class NodeContentsFactory implements DVNode.NodeElementFactory
	{
		private GSymViewInstance viewInstance;
		private GSymNodeViewFunction nodeViewFunction;
		private Object state;
		
		
		public NodeContentsFactory(GSymViewInstance viewInstance, GSymNodeViewFunction viewFunction, Object state)
		{
			this.viewInstance = viewInstance;
			this.nodeViewFunction = viewFunction;
			this.state = state;
		}


		public Element createNodeElement(DVNode viewNode, DocTreeNode treeNode)
		{
			// Create the node view instance
			GSymNodeViewInstance nodeViewInstance = new GSymNodeViewInstance( treeNode.getNode(), viewInstance.getView(), viewInstance, viewNode );
			
			// Build the contents
			return viewInstance.buildNodeViewContents( nodeViewInstance, treeNode, nodeViewFunction, state );
		}
	}
	
	
	private class RootInitialiser implements DocView.RootNodeInitialiser
	{
		public void initRootNode(DVNode rootView, Object rootDocNode)
		{
			rootView.setNodeElementFactory( makeNodeElementFactory( null, null ) );
		}
	}
	
	private static class NodeContentsFactoryKey
	{
		private GSymNodeViewFunction nodeViewFunction;
		private Object state;
		
		
		public NodeContentsFactoryKey(GSymNodeViewFunction nodeViewFunction, Object state)
		{
			this.nodeViewFunction = nodeViewFunction;
			this.state = state;
		}
		
		
		public int hashCode()
		{
			int mult = 1000003;
			int x = 0x345678;
			x = ( x ^ nodeViewFunction.hashCode() ) * mult;
			mult += 82520 + 2;
			x = ( x ^ state.hashCode() ) * mult;
			return x + 97351;
		}
		
		public boolean equals(Object x)
		{
			if ( x instanceof NodeContentsFactoryKey )
			{
				NodeContentsFactoryKey kx = (NodeContentsFactoryKey)x;
				return nodeViewFunction == kx.nodeViewFunction  &&  state.equals( kx.state ); 
			}
			else
			{
				return false;
			}
		}
	}
	
	
	
	private DocTree tree;
	private DocTreeNode txs;
	private GSymNodeViewFunction generalNodeViewFunction;
	private DocView view;
	private HashMap<Float, BorderStyleSheet> indentationStyleSheets;
	private HashMap<NodeContentsFactoryKey, NodeContentsFactory> nodeContentsFactories;
	
	
	public GSymViewInstance(DocTree tree, DocTreeNode txs, GSymViewFactory viewFactory, CommandHistory commandHistory, DVNode.NodeElementChangeListener changeListener)
	{
		this.tree = tree;
		this.txs = txs;
		this.generalNodeViewFunction = viewFactory.createViewFunction();
		view = new DocView( tree, txs, new RootInitialiser(), changeListener );
		
		indentationStyleSheets = new HashMap<Float, BorderStyleSheet>();
		nodeContentsFactories = new HashMap<NodeContentsFactoryKey, NodeContentsFactory>();
	}
	
	
	protected BorderStyleSheet indentationStyleSheet(float indentation)
	{
		BorderStyleSheet styleSheet = indentationStyleSheets.get( indentation );
		
		if ( styleSheet == null )
		{
			styleSheet = new BorderStyleSheet( indentation, 0.0, 0.0, 0.0 );
			indentationStyleSheets.put( indentation, styleSheet );
		}
		
		return styleSheet;
	}
	
	
	protected DVNode.NodeElementFactory makeNodeElementFactory(GSymNodeViewFunction nodeViewFunction, Object state)
	{
		// Memoise the contents factory, keyed by  @nodeViewFunction and @state
		NodeContentsFactoryKey key = new NodeContentsFactoryKey( nodeViewFunction, state );
		
		NodeContentsFactory factory = nodeContentsFactories.get( key );
		
		if ( factory == null )
		{
			factory = new NodeContentsFactory( this, nodeViewFunction, state );
			nodeContentsFactories.put( key, factory );
			return factory;
		}
		
		return factory;
	}
	
	
	protected Element buildNodeViewContents(GSymNodeViewInstance nodeViewInstance, DocTreeNode txs, GSymNodeViewFunction nodeViewFunction, Object state)
	{
		// Build the contents of a view node
		if ( nodeViewFunction == null )
		{
			nodeViewFunction = generalNodeViewFunction;
		}
		
		return nodeViewFunction.createElement( txs, nodeViewInstance, state );
	}
	
	
	protected DocView getView()
	{
		return view;
	}
	
	
	public DocTree getTree()
	{
		return tree;
	}
	
	public DocTreeNode getTXS()
	{
		return txs;
	}
}
