//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.HashMap;

import org.python.core.PyObject;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocPresent.DPFrame;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocTree.DocTree;
import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.Utils.Profile.ProfileTimer;

public class GSymViewInstance implements DocView.RefreshListener
{
	//
	//
	// PROFILING
	//
	//
	
	static boolean ENABLE_PROFILING = true;
	
	
	public static class CannotViewTerminalDocNode extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	
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


		public DPWidget createNodeElement(DVNode viewNode, DocTreeNode treeNode)
		{
			// Create the node view instance
			GSymNodeViewInstance nodeViewInstance = new GSymNodeViewInstance( viewInstance, viewNode );
			
			// Build the contents
			//return nodeViewFunction.createElement( treeNode, nodeViewInstance, state );
			
			viewInstance.getView().profile_startPython();
			DPWidget e = nodeViewFunction.createElement( treeNode, nodeViewInstance, state );
			viewInstance.getView().profile_stopPython();
			return e;
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
	
	
	
	private Object docRootNode;
	private DocTree tree;
	private DocTreeNode treeRootNode;
	
	private GSymNodeViewFunction generalNodeViewFunction;
	
	private DocView view;
	
	private DPFrame frame;
	
	private HashMap<Float, Border> indentationBorders;
	private HashMap<NodeContentsFactoryKey, NodeContentsFactory> nodeContentsFactories;
	
	private Object owner;
	
	
	public GSymViewInstance(Object docRootNode, DPFrame frame, GSymNodeViewFunction generalNodeViewFunction, GSymNodeViewFunction rootNodeViewFunction,
			CommandHistory commandHistory, Object owner) throws CannotViewTerminalDocNode
	{
		this.docRootNode = docRootNode;
		tree = new DocTree();
		Object docTreeRoot = tree.treeNode( docRootNode );
		this.owner = owner;
		
		if ( docTreeRoot instanceof DocTreeNode )
		{
			treeRootNode = (DocTreeNode)docTreeRoot;
			this.generalNodeViewFunction = generalNodeViewFunction; 
			
			this.frame = frame;
			
			indentationBorders = new HashMap<Float, Border>();
			nodeContentsFactories = new HashMap<NodeContentsFactoryKey, NodeContentsFactory>();
	
			view = new DocView( tree, treeRootNode, makeNodeElementFactory( rootNodeViewFunction, null ) );
			view.setElementChangeListener( new NodeElementChangeListenerDiff() );
			view.setRefreshListener( this );
			this.frame.setChild( view.getRootViewElement() );
		}
		else
		{
			throw new CannotViewTerminalDocNode();
		}
	}
	
	
	public GSymViewInstance(Object docRootNode, DPFrame frame, PyObject generalNodeViewFunction, PyObject rootNodeViewFunction,
			CommandHistory commandHistory, Object owner) throws CannotViewTerminalDocNode
	{
		this( docRootNode, frame, new PyGSymNodeViewFunction( generalNodeViewFunction ), new PyGSymNodeViewFunction( generalNodeViewFunction ), commandHistory, owner );
	}

	
	
	
	public Object getOwner()
	{
		return owner;
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
	
	
	
	public Caret getCaret()
	{
		DPPresentationArea elementTree = frame.getPresentationArea();
		return elementTree != null  ?  elementTree.getCaret()  :  null;
	}
	
	public Selection getSelection()
	{
		DPPresentationArea elementTree = frame.getPresentationArea();
		return elementTree != null  ?  elementTree.getSelection()  :  null;
	}
	
	
	
	
	public DocView getView()
	{
		return view;
	}
	
	public DPFrame getFrame()
	{
		return frame;
	}
	
	public DPPresentationArea getElementTree()
	{
		return frame.getPresentationArea();
	}
	
	
	
	public Object getDocRootNode()
	{
		return docRootNode;
	}
	
	public DocTree getTree()
	{
		return tree;
	}
	
	public DocTreeNode getTreeRootNode()
	{
		return treeRootNode;
	}




	public void onViewRequestRefresh(DocView view)
	{
		Runnable r = new Runnable()
		{
			public void run()
			{
				refreshView();
			}
		};
		frame.queueImmediateEvent( r );
	}
	
	
	private void refreshView()
	{
		long t1 = 0;
		if ( ENABLE_PROFILING )
		{
			t1 = System.nanoTime();
			ProfileTimer.initProfiling();
			view.beginProfiling();
		}
		view.refresh();
		if ( ENABLE_PROFILING )
		{
			long t2 = System.nanoTime();
			view.endProfiling();
			ProfileTimer.shutdownProfiling();
			double deltaT = ( t2 - t1 )  *  1.0e-9;
			System.out.println( "MainApp: REFRESH VIEW TIME = " + deltaT );
			System.out.println( "MainApp: REFRESH VIEW PROFILE: JAVA TIME = " + view.getJavaTime() + ", ELEMENT CREATE TIME = " + view.getElementTime() +
					", PYTHON TIME = " + view.getPythonTime() + ", CONTENT CHANGE TIME = " + view.getContentChangeTime() +
					", UPDATE NODE ELEMENT TIME = " + view.getUpdateNodeElementTime() );
		}
	}
}
