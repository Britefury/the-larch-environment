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
import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPFrame;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.EditHandler;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeContext;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeFunction;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalTreeContext;
import BritefuryJ.GSym.IncrementalContext.PyGSymIncrementalNodeFunction;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class GSymViewContext extends GSymIncrementalTreeContext implements DocView.RefreshListener
{
	protected static class NodeViewContextAndResultFactory extends GSymIncrementalTreeContext.NodeContextAndResultFactory
	{
		public NodeViewContextAndResultFactory(GSymIncrementalTreeContext treeContext, GSymIncrementalNodeFunction nodeFunction, Object state)
		{
			super( treeContext, nodeFunction, state );
		}


		protected GSymIncrementalNodeContext createContext(GSymIncrementalTreeContext treeContext, IncrementalTreeNode incrementalNode) 
		{
			return new GSymNodeViewContext( (GSymViewContext)treeContext, (DVNode)incrementalNode );
		}

		public Object createNodeResult(IncrementalTreeNode incrementalNode, DMNode docNode)
		{
			GSymViewContext viewContext = (GSymViewContext)treeContext;
			DocView docView = viewContext.getView();
			docView.profile_startPython();
			Object r = super.createNodeResult( incrementalNode, docNode );
			docView.profile_stopPython();
			return r;
		}
	}

	
	
	private DocView view;
	
	private DPFrame frame;
	
	private HashMap<Double, Border> indentationBorders;
	
	private CommandHistory commandHistory;

	
	public GSymViewContext(DMNode docRootNode, GSymIncrementalNodeFunction generalNodeViewFunction, GSymIncrementalNodeFunction rootNodeViewFunction,
			CommandHistory commandHistory)
	{
		super( docRootNode, generalNodeViewFunction, rootNodeViewFunction );
		this.commandHistory = commandHistory;
		
		frame = new DPFrame( );
		
		indentationBorders = new HashMap<Double, Border>();
		
		
		view = (DocView)incrementalTree;
		view.setElementChangeListener( new NodeElementChangeListenerDiff() );
		view.setRefreshListener( this );
		frame.setChild( view.getRootViewElement().alignHExpand() );
	}
	
	
	public GSymViewContext(DMNode docRootNode, PyObject generalNodeViewFunction, PyObject rootNodeViewFunction, CommandHistory commandHistory)
	{
		this( docRootNode, new PyGSymIncrementalNodeFunction( generalNodeViewFunction ), new PyGSymIncrementalNodeFunction( generalNodeViewFunction ), commandHistory );
	}

	
	public GSymViewContext(DMNode docRootNode, GSymIncrementalNodeFunction nodeViewFunction, CommandHistory commandHistory)
	{
		this( docRootNode, nodeViewFunction, nodeViewFunction, commandHistory );
	}

	public GSymViewContext(DMNode docRootNode, PyObject nodeViewFunction, CommandHistory commandHistory)
	{
		this( docRootNode, new PyGSymIncrementalNodeFunction( nodeViewFunction ), commandHistory );
	}

	

	protected IncrementalTree createIncrementalTree(DMNode docRootNode, IncrementalTreeNode.NodeResultFactory resultFactory)
	{
		return new DocView( docRootNode, resultFactory );
	}
	
	protected NodeContextAndResultFactory createContextAndResultFactory(GSymIncrementalTreeContext treeContext, GSymIncrementalNodeFunction nodeFunction, Object state)
	{
		return new NodeViewContextAndResultFactory( this, nodeFunction, state );
	}
	
	
	
	protected Border indentationBorder(double indentation)
	{
		Border border = indentationBorders.get( indentation );
		
		if ( border == null )
		{
			border = new EmptyBorder( indentation, 0.0, 0.0, 0.0 );
			indentationBorders.put( indentation, border );
		}
		
		return border;
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
	
	public EditHandler getEditHandler()
	{
		return frame.getEditHandler();
	}
	
	public DPPresentationArea getElementTree()
	{
		return frame.getPresentationArea();
	}
	
	
	
	public CommandHistory getCommandHistory()
	{
		return commandHistory;
	}



	public void onIncrementalTreeRequestRefresh(IncrementalTree tree)
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
		view.refresh();
	}
}
