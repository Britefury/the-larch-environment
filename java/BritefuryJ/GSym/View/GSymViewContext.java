//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.HashMap;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.GSymPerspective;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.Logging.Log;
import BritefuryJ.Utils.HashUtils;

public class GSymViewContext implements DocView.RefreshListener
{
	protected static class ViewFragmentContextAndResultFactory implements IncrementalTreeNode.NodeResultFactory
	{
		protected GSymViewContext viewContext;
		protected GSymPerspective perspective;
		protected GSymViewFragmentFunction viewFragmentFunction;
		protected AttributeTable subjectContext;
		protected StyleSheet styleSheet;
		protected AttributeTable inheritedState;
		
		public ViewFragmentContextAndResultFactory(GSymViewContext viewContext, GSymPerspective perspective, GSymViewFragmentFunction nodeFunction, AttributeTable subjectContext,
				StyleSheet styleSheet, AttributeTable inheritedState)
		{
			this.viewContext = viewContext;
			this.perspective = perspective;
			this.viewFragmentFunction = nodeFunction;
			this.subjectContext = subjectContext;
			this.styleSheet = styleSheet;
			this.inheritedState = inheritedState;
		}


		public Object createNodeResult(IncrementalTreeNode incrementalNode, Object docNode)
		{
			DocView docView = viewContext.getView();
			docView.profile_startPython();

			// Create the node context
			GSymFragmentViewContext nodeContext = new GSymFragmentViewContext( this, (DVNode)incrementalNode );
			
			// Create the view fragment
			DPElement fragment = viewFragmentFunction.createViewFragment( docNode, nodeContext, styleSheet, inheritedState );
			
			docView.profile_stopPython();
			
			return fragment;
		}
	}
	
	
	protected static class ViewFragmentContextAndResultFactoryKey
	{
		private GSymPerspective perspective;
		private GSymViewFragmentFunction viewFragmentFunction;
		private AttributeTable subjectContext;
		private StyleSheet styleSheet;
		private AttributeTable inheritedState;
		
		
		public ViewFragmentContextAndResultFactoryKey(GSymPerspective perspective, GSymViewFragmentFunction viewFragmentFunction, AttributeTable subjectContext,
				StyleSheet styleSheet, AttributeTable inheritedState)
		{
			this.perspective = perspective;
			this.viewFragmentFunction = viewFragmentFunction;
			this.styleSheet = styleSheet;
			this.inheritedState = inheritedState;
			this.subjectContext = subjectContext;
		}
		
		
		public int hashCode()
		{
			if ( viewFragmentFunction == null  ||  styleSheet == null )
			{
				throw new RuntimeException( "null?nodeFunction=" + ( viewFragmentFunction == null ) + ", null?styleSheet=" + ( styleSheet == null ) );
			}
			return HashUtils.nHash( new int[] { System.identityHashCode( perspective ), System.identityHashCode( viewFragmentFunction ), styleSheet.hashCode(), inheritedState.hashCode(), subjectContext.hashCode() } );
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
				return perspective == kx.perspective  &&  viewFragmentFunction == kx.viewFragmentFunction  &&  styleSheet.equals( kx.styleSheet )  &&  inheritedState == kx.inheritedState  &&
					subjectContext == kx.subjectContext;
			}
			else
			{
				return false;
			}
		}
	}


	
	
	private DocView view;
	
	private DPVBox vbox;
	private DPRegion region;
	
	private GSymBrowserContext browserContext;
	private GSymViewPage page;
	
	private CommandHistory commandHistory;

	private Object docRootNode;

	private HashMap<ViewFragmentContextAndResultFactoryKey, ViewFragmentContextAndResultFactory> viewFragmentContextAndResultFactories =
		new HashMap<ViewFragmentContextAndResultFactoryKey, ViewFragmentContextAndResultFactory>();

	
	public GSymViewContext(GSymSubject subject, GSymBrowserContext browserContext, CommandHistory commandHistory, PersistentStateStore persistentState)
	{
		this.docRootNode = subject.getFocus();
		GSymPerspective perspective = subject.getPerspective();
		
		view = new DocView( docRootNode,
				makeNodeResultFactory( perspective, perspective.getFragmentViewFunction(), subject.getSubjectContext(), perspective.getStyleSheet(), perspective.getInitialInheritedState() ),
				persistentState );

		this.browserContext = browserContext;
		this.commandHistory = commandHistory;
		
		region = new DPRegion();
		vbox = PrimitiveStyleSheet.instance.vbox( new DPElement[] { region } );

		page = new GSymViewPage( vbox.alignHExpand().alignVExpand(), subject.getTitle(), browserContext, commandHistory, this );
		
		view.setElementChangeListener( new NodeElementChangeListenerDiff() );
		view.setRefreshListener( this );
		
		// We need to do this last
		region.setChild( view.getRootViewElement().alignHExpand().alignVExpand() );
		region.setEditHandler( perspective.getEditHandler() );
	}
	
	
	
	

	protected DVNode.NodeResultFactory makeNodeResultFactory(GSymPerspective perspective, GSymViewFragmentFunction nodeFunction, AttributeTable subjectContext,
			StyleSheet styleSheet, AttributeTable inheritedState)
	{
		// Memoise the contents factory, keyed by  @nodeViewFunction and @state
		ViewFragmentContextAndResultFactoryKey key = new ViewFragmentContextAndResultFactoryKey( perspective, nodeFunction, subjectContext, styleSheet, inheritedState );
		
		ViewFragmentContextAndResultFactory factory = viewFragmentContextAndResultFactories.get( key );
		
		if ( factory == null )
		{
			factory = new ViewFragmentContextAndResultFactory( this, perspective, nodeFunction, subjectContext, styleSheet, inheritedState );
			viewFragmentContextAndResultFactories.put( key, factory );
			return factory;
		}
		
		return factory;
	}

	
	public Caret getCaret()
	{
		PresentationComponent.RootElement elementTree = region.getRootElement();
		return elementTree != null  ?  elementTree.getCaret()  :  null;
	}
	
	public Selection getSelection()
	{
		PresentationComponent.RootElement elementTree = region.getRootElement();
		return elementTree != null  ?  elementTree.getSelection()  :  null;
	}
	
	
	
	
	public Object getDocRootNode()
	{
		return docRootNode;
	}
	
	
	
	public void refreshTree()
	{
		view.refresh();
	}

	
	
	public DocView getView()
	{
		return view;
	}
	
	public PresentationComponent.RootElement getElementTree()
	{
		return vbox.getRootElement();
	}
	
	
	
	public GSymBrowserContext getBrowserContext()
	{
		return browserContext;
	}
	
	public Page getPage()
	{
		return page;
	}
	
	public Log getPageLog()
	{
		return page.getLog();
	}
	
	public CommandHistory getCommandHistory()
	{
		return commandHistory;
	}
	
	
	
	public PersistentStateStore storePersistentState()
	{
		return view.storePersistentState();
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
