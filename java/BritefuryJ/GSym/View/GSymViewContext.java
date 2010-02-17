//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import org.python.core.PyObject;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPFrame;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.EditHandler;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeFunction;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalTreeContext;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.Utils.HashUtils;

public class GSymViewContext extends GSymIncrementalTreeContext implements DocView.RefreshListener
{
	protected static class ViewFragmentContextAndResultFactory extends GSymIncrementalTreeContext.NodeContextAndResultFactory
	{
		protected GSymViewFragmentFunction viewFragmentFunction;
		protected StyleSheet styleSheet;
		
		public ViewFragmentContextAndResultFactory(GSymIncrementalTreeContext treeContext, GSymViewFragmentFunction nodeFunction, StyleSheet styleSheet, Object state)
		{
			super( treeContext, state );
			
			this.viewFragmentFunction = nodeFunction;
			this.styleSheet = styleSheet;
		}


		protected GSymNodeViewContext createContext(GSymIncrementalTreeContext treeContext, IncrementalTreeNode incrementalNode) 
		{
			return new GSymNodeViewContext( (GSymViewContext)treeContext, (DVNode)incrementalNode );
		}

		public Object createNodeResult(IncrementalTreeNode incrementalNode, DMNode docNode)
		{
			GSymViewContext viewContext = (GSymViewContext)treeContext;
			DocView docView = viewContext.getView();
			docView.profile_startPython();

			// Create the node context
			GSymNodeViewContext nodeContext = createContext( treeContext, incrementalNode );
			
			// Create the view fragment
			DPWidget fragment = viewFragmentFunction.createViewFragment( docNode, nodeContext, styleSheet, state );
			
			docView.profile_stopPython();
			
			return fragment;
		}
	}
	
	
	protected static class ViewInheritedState extends InheritedState
	{
		public StyleSheet styleSheet;
		public Object state;
		
		
		public ViewInheritedState(StyleSheet styleSheet, Object state)
		{
			this.styleSheet = styleSheet;
			this.state = state;
		}
	}
	
	
	protected static class ViewFragmentContextAndResultFactoryKey extends NodeContextAndResultFactoryKey
	{
		private GSymIncrementalNodeFunction nodeFunction;
		private StyleSheet styleSheet;
		private Object state;
		
		
		public ViewFragmentContextAndResultFactoryKey(GSymIncrementalNodeFunction nodeFunction, StyleSheet styleSheet, Object state)
		{
			this.nodeFunction = nodeFunction;
			this.styleSheet = styleSheet;
			this.state = state;
		}
		
		
		public int hashCode()
		{
			int stateHash = state != null  ?  state.hashCode()  :  0;
			return HashUtils.tripleHash( nodeFunction.hashCode(), styleSheet.hashCode(), stateHash );
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof ViewFragmentContextAndResultFactoryKey )
			{
				ViewFragmentContextAndResultFactoryKey kx = (ViewFragmentContextAndResultFactoryKey)x;
				boolean bStateEqual = ( state == null  ||  kx.state == null )  ?  ( state != null ) == ( kx.state != null )  :  state.equals( kx.state );
				return nodeFunction == kx.nodeFunction  &&  styleSheet.equals( kx.styleSheet )  &&  bStateEqual;
			}
			else
			{
				return false;
			}
		}
	}


	
	
	private DocView view;
	
	private DPFrame frame;
	
	private CommandHistory commandHistory;

	
	public GSymViewContext(DMNode docRootNode, GSymIncrementalNodeFunction generalNodeViewFunction, GSymIncrementalNodeFunction rootNodeViewFunction, StyleSheet rootStyleSheet,
			CommandHistory commandHistory)
	{
		super( docRootNode, generalNodeViewFunction, rootNodeViewFunction, new ViewInheritedState( rootStyleSheet, null ) );
		this.commandHistory = commandHistory;
		
		frame = new DPFrame( );
		
		
		view = (DocView)incrementalTree;
		view.setElementChangeListener( new NodeElementChangeListenerDiff() );
		view.setRefreshListener( this );
		frame.setChild( view.getRootViewElement().alignHExpand() );
	}
	
	
	public GSymViewContext(DMNode docRootNode, PyObject generalNodeViewFunction, PyObject rootNodeViewFunction, StyleSheet rootStyleSheet, CommandHistory commandHistory)
	{
		this( docRootNode, new PyGSymViewFragmentFunction( generalNodeViewFunction ), new PyGSymViewFragmentFunction( generalNodeViewFunction ), rootStyleSheet, commandHistory );
	}

	
	public GSymViewContext(DMNode docRootNode, GSymIncrementalNodeFunction nodeViewFunction, StyleSheet rootStyleSheet, CommandHistory commandHistory)
	{
		this( docRootNode, nodeViewFunction, nodeViewFunction, rootStyleSheet, commandHistory );
	}

	public GSymViewContext(DMNode docRootNode, PyObject nodeViewFunction, StyleSheet rootStyleSheet, CommandHistory commandHistory)
	{
		this( docRootNode, new PyGSymViewFragmentFunction( nodeViewFunction ), rootStyleSheet, commandHistory );
	}

	

	protected IncrementalTree createIncrementalTree(DMNode docRootNode, IncrementalTreeNode.NodeResultFactory resultFactory)
	{
		return new DocView( docRootNode, resultFactory );
	}
	
	protected NodeContextAndResultFactory createContextAndResultFactory(GSymIncrementalTreeContext treeContext, GSymIncrementalNodeFunction nodeFunction, InheritedState inheritedState)
	{
		ViewInheritedState viewState = (ViewInheritedState)inheritedState;
		return new ViewFragmentContextAndResultFactory( this, (GSymViewFragmentFunction)nodeFunction, viewState.styleSheet, viewState.state );
	}
	
	protected NodeContextAndResultFactoryKey createFragmentKey(GSymIncrementalNodeFunction nodeFunction, InheritedState inheritedState)
	{
		ViewInheritedState viewState = (ViewInheritedState)inheritedState;
		return new ViewFragmentContextAndResultFactoryKey( nodeFunction, viewState.styleSheet, viewState.state );
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
