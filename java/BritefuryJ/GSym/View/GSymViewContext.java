//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import org.python.core.PyObject;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.GSym.GSymBrowserContext;
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


		protected GSymFragmentViewContext createContext(GSymIncrementalTreeContext treeContext, IncrementalTreeNode incrementalNode) 
		{
			return new GSymFragmentViewContext( (GSymViewContext)treeContext, (DVNode)incrementalNode );
		}

		public Object createNodeResult(IncrementalTreeNode incrementalNode, Object docNode)
		{
			GSymViewContext viewContext = (GSymViewContext)treeContext;
			DocView docView = viewContext.getView();
			docView.profile_startPython();

			// Create the node context
			GSymFragmentViewContext nodeContext = createContext( treeContext, incrementalNode );
			
			// Create the view fragment
			DPElement fragment = viewFragmentFunction.createViewFragment( docNode, nodeContext, styleSheet, state );
			
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
			if ( nodeFunction == null  ||  styleSheet == null )
			{
				throw new RuntimeException( "null?nodeFunction=" + ( nodeFunction == null ) + ", null?styleSheet=" + ( styleSheet == null ) );
			}
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
	
	private DPRegion region;
	
	private GSymBrowserContext browserContext;
	
	private CommandHistory commandHistory;

	
	public GSymViewContext(Object docRootNode, GSymIncrementalNodeFunction generalNodeViewFunction, GSymIncrementalNodeFunction rootNodeViewFunction, StyleSheet rootStyleSheet,
			Object rootState, GSymBrowserContext browserContext, CommandHistory commandHistory)
	{
		super( docRootNode, generalNodeViewFunction, rootNodeViewFunction, new ViewInheritedState( rootStyleSheet, rootState ) );
		this.browserContext = browserContext;
		this.commandHistory = commandHistory;
		
		region = new DPRegion( );
		
		
		view = (DocView)incrementalTree;
		view.setElementChangeListener( new NodeElementChangeListenerDiff() );
		view.setRefreshListener( this );
		region.setChild( view.getRootViewElement().alignHExpand() );
	}
	
	
	public GSymViewContext(Object docRootNode, PyObject generalNodeViewFunction, PyObject rootNodeViewFunction, StyleSheet rootStyleSheet, Object rootState,
			GSymBrowserContext browserContext, CommandHistory commandHistory)
	{
		this( docRootNode, new PyGSymViewFragmentFunction( generalNodeViewFunction ), new PyGSymViewFragmentFunction( generalNodeViewFunction ), rootStyleSheet, rootState,
				browserContext, commandHistory );
	}

	
	public GSymViewContext(Object docRootNode, GSymIncrementalNodeFunction nodeViewFunction, StyleSheet rootStyleSheet, Object rootState,
			GSymBrowserContext browserContext, CommandHistory commandHistory)
	{
		this( docRootNode, nodeViewFunction, nodeViewFunction, rootStyleSheet, rootState, browserContext, commandHistory );
	}

	public GSymViewContext(Object docRootNode, PyObject nodeViewFunction, StyleSheet rootStyleSheet, Object rootState, GSymBrowserContext browserContext, CommandHistory commandHistory)
	{
		this( docRootNode, new PyGSymViewFragmentFunction( nodeViewFunction ), rootStyleSheet, rootState, browserContext, commandHistory );
	}

	

	protected IncrementalTree createIncrementalTree(Object docRootNode, IncrementalTreeNode.NodeResultFactory resultFactory)
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
		DPPresentationArea elementTree = region.getPresentationArea();
		return elementTree != null  ?  elementTree.getCaret()  :  null;
	}
	
	public Selection getSelection()
	{
		DPPresentationArea elementTree = region.getPresentationArea();
		return elementTree != null  ?  elementTree.getSelection()  :  null;
	}
	
	
	
	
	public DocView getView()
	{
		return view;
	}
	
	public DPRegion getRegion()
	{
		return region;
	}
	
	public EditHandler getEditHandler()
	{
		return region.getEditHandler();
	}
	
	public DPPresentationArea getElementTree()
	{
		return region.getPresentationArea();
	}
	
	
	
	public GSymBrowserContext getBrowserContext()
	{
		return browserContext;
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
		region.queueImmediateEvent( r );
	}
	
	
	private void refreshView()
	{
		view.refresh();
	}
}
