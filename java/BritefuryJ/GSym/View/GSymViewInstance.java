//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.HashMap;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocPresent.EditHandler;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
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
			assert viewFunction != null;
			
			this.viewInstance = viewInstance;
			this.nodeViewFunction = viewFunction;
			this.state = state;
		}


		public Element createNodeElement(DVNode viewNode, DocTreeNode treeNode)
		{
			// Create the node view instance
			GSymNodeViewInstance nodeViewInstance = new GSymNodeViewInstance( viewInstance, viewNode );
			viewNode.setContext( nodeViewInstance );
			
			// Build the contents
			//return nodeViewFunction.createElement( treeNode, nodeViewInstance, state );
			
			viewInstance.getView().profile_startPython();
			Element e = nodeViewFunction.createElement( treeNode, nodeViewInstance, state );
			viewInstance.getView().profile_stopPython();
			return e;
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
			int stateHash = state != null  ?  state.hashCode()  :  0;
			int mult = 1000003;
			int x = 0x345678;
			x = ( x ^ nodeViewFunction.hashCode() ) * mult;
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
			
			if ( x instanceof NodeContentsFactoryKey )
			{
				NodeContentsFactoryKey kx = (NodeContentsFactoryKey)x;
				if ( state == null  ||  kx.state == null )
				{
					return nodeViewFunction == kx.nodeViewFunction  &&  ( state != null ) == ( kx.state != null );
				}
				else
				{
					return nodeViewFunction == kx.nodeViewFunction  &&  state.equals( kx.state );
				}
			}
			else
			{
				return false;
			}
		}
	}
	
	
	
	private DocTree tree;
	private DocTreeNode treeRootNode;
	private GSymNodeViewFunction generalNodeViewFunction;
	private DocView view;
	private ElementTree elementTree;
	private HashMap<Float, Border> indentationBorders;
	private HashMap<NodeContentsFactoryKey, NodeContentsFactory> nodeContentsFactories;
	
	
	public GSymViewInstance(DocTree tree, DocTreeNode treeRootNode, ElementTree elementTree, GSymViewFactory viewFactory, CommandHistory commandHistory)
	{
		this.tree = tree;
		this.treeRootNode = treeRootNode;
		generalNodeViewFunction = viewFactory.createViewFunction();
		if ( generalNodeViewFunction == null )
		{
			throw new RuntimeException();
		}
		view = new DocView( tree, treeRootNode, new RootInitialiser() );
		this.elementTree = elementTree;
		
		indentationBorders = new HashMap<Float, Border>();
		nodeContentsFactories = new HashMap<NodeContentsFactoryKey, NodeContentsFactory>();
	}
	
	
	protected Border indentationBorder(float indentation)
	{
		Border border = indentationBorders.get( indentation );
		
		if ( border == null )
		{
			border = new EmptyBorder( indentation, 0.0, 0.0, 0.0 );
			indentationBorders.put( indentation, border );
		}
		
		return border;
	}
	
	
	protected DVNode.NodeElementFactory makeNodeElementFactory(GSymNodeViewFunction nodeViewFunction, Object state)
	{
		// Memoise the contents factory, keyed by  @nodeViewFunction and @state
		if ( nodeViewFunction == null )
		{
			nodeViewFunction = generalNodeViewFunction;
		}

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
	
	
	
	public void setElementChangeListener(DVNode.NodeElementChangeListener elementChangeListener)
	{
		view.setElementChangeListener( elementChangeListener );
	}
	
	public void setEditHandler(EditHandler handler)
	{
		elementTree.setEditHandler( handler );
	}
	
	
	
	public DocView getView()
	{
		return view;
	}
	
	public ElementTree getElementTree()
	{
		return elementTree;
	}
	
	
	public DocTree getTree()
	{
		return tree;
	}
	
	public DocTreeNode getTreeRootNode()
	{
		return treeRootNode;
	}
}
