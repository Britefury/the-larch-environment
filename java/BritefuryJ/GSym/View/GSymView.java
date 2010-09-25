//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPColumn;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Browser.BrowserPage;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.PersistentState.PersistentStateTable;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.ErrorBox;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBorder;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.IncrementalTree.IncrementalTreeNodeTable;
import BritefuryJ.Logging.Log;
import BritefuryJ.Utils.HashUtils;
import BritefuryJ.Utils.Profile.ProfileTimer;

public class GSymView extends IncrementalTree implements Presentable
{
	//
	//
	// PROFILING
	//
	//
	
	static boolean ENABLE_PROFILING = false;
	static boolean ENABLE_DISPLAY_TREESIZES = false;
	
	
	public interface NodeElementChangeListener
	{
		public void reset(GSymView view);
		public void elementChangeFrom(GSymFragmentView node, DPElement e);
		public void elementChangeTo(GSymFragmentView node, DPElement e);
	}
	
	
	public static class StateStore extends PersistentStateStore
	{
		private HashMap<IncrementalTreeNodeTable.Key, LinkedList<PersistentStateTable>> table = new HashMap<IncrementalTreeNodeTable.Key, LinkedList<PersistentStateTable>>();
		
		
		public StateStore()
		{
		}
		
		private void addPersistentState(Object node, PersistentStateTable persistentStateTable)
		{
			IncrementalTreeNodeTable.Key key = new IncrementalTreeNodeTable.Key( node );
			LinkedList<PersistentStateTable> entryList = table.get( key );
			if ( entryList == null )
			{
				entryList = new LinkedList<PersistentStateTable>();
				table.put( key, entryList );
			}
			entryList.push( persistentStateTable );
		}
		
		private PersistentStateTable usePersistentState(Object node)
		{
			LinkedList<PersistentStateTable> entryList = table.get( new IncrementalTreeNodeTable.Key( node ) );
			if ( entryList != null )
			{
				return entryList.removeFirst();
			}
			else
			{
				return null;
			}
		}
		
		
		public boolean isEmpty()
		{
			return table.isEmpty();
		}
	}
	
	
	
	
	protected static class ViewFragmentContextAndResultFactory implements IncrementalTreeNode.NodeResultFactory
	{
		protected GSymView view;
		protected GSymAbstractPerspective perspective;
		protected SimpleAttributeTable subjectContext;
		protected StyleValues style;
		protected SimpleAttributeTable inheritedState;
		
		public ViewFragmentContextAndResultFactory(GSymView view, GSymAbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
		{
			this.view = view;
			this.perspective = perspective;
			this.subjectContext = subjectContext;
			this.style = style;
			this.inheritedState = inheritedState;
		}


		public Object createNodeResult(IncrementalTreeNode incrementalNode, Object model)
		{
			view.profile_startPython();

			// Create the node context
			GSymFragmentView fragmentView = (GSymFragmentView)incrementalNode;
			
			// Create the view fragment
			Pres fragment;
			try
			{
				fragment = perspective.presentObject( model, fragmentView, inheritedState );
			}
			catch (Throwable t)
			{
				GSymAbstractPerspective genericPerspective = view.browserContext.getGenericPerspective();
				try
				{
					Pres exceptionView = genericPerspective.presentObject( t, fragmentView, inheritedState );
					fragment = new ErrorBox( "Presentation error - exception during presentation", exceptionView );
					return fragment.present( new PresentationContext( fragmentView, genericPerspective, inheritedState ), style );
				}
				catch (Exception e2)
				{
					fragment = new ErrorBox( "DOUBLE EXCEPTION!", new Column( new Pres[] {
							labelStyle.applyTo( new StaticText( "Got exception:" ) ),
							exceptionStyle.applyTo( new StaticText( e2.toString() ) ).padX( 15.0, 0.0 ),
							labelStyle.applyTo( new StaticText( "While trying to display exception:" ) ),
							exceptionStyle.applyTo( new StaticText( t.toString() ) ).padX( 15.0, 0.0 )   } ) );
					return fragment.present( new PresentationContext( fragmentView, genericPerspective, inheritedState ), style );
				}
			}
			
			view.profile_stopPython();
			
			return fragment.present( new PresentationContext( fragmentView, perspective, inheritedState ), style );
		}

	
		private static final StyleSheet labelStyle = StyleSheet.instance;
		private static final StyleSheet exceptionStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 1.0f, 0.2f, 0.0f ) );
	}
	
	
	protected static class ViewFragmentContextAndResultFactoryKey
	{
		private GSymAbstractPerspective perspective;
		private SimpleAttributeTable subjectContext;
		private StyleValues style;
		private SimpleAttributeTable inheritedState;
		
		
		public ViewFragmentContextAndResultFactoryKey(GSymAbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
		{
			this.perspective = perspective;
			this.style = style;
			this.inheritedState = inheritedState;
			this.subjectContext = subjectContext;
		}
		
		
		public int hashCode()
		{
			if ( style == null )
			{
				throw new RuntimeException( "null?styleSheet=" + ( style == null ) );
			}
			return HashUtils.nHash( new int[] { System.identityHashCode( perspective ), style.hashCode(), inheritedState.hashCode(), subjectContext.hashCode() } );
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
				return perspective == kx.perspective  &&  style.equals( kx.style )  &&  inheritedState == kx.inheritedState  &&  subjectContext == kx.subjectContext;
			}
			else
			{
				return false;
			}
		}
	}


	
	
	
	
	
	private NodeElementChangeListener elementChangeListener = null;
	private DPColumn rootBox = null;
	
	private StateStore stateStoreToLoad;
	
	
	private boolean bProfilingEnabled = false;
	private ProfileTimer pythonTimer = new ProfileTimer();
	private ProfileTimer javaTimer = new ProfileTimer();
	private ProfileTimer contentChangeTimer = new ProfileTimer();
	private ProfileTimer updateNodeElementTimer = new ProfileTimer();
	
	
	
	private GSymFragmentView.NodeResultFactory rootNodeResultFactory;
	
	
	private DPRegion region;
	
	private GSymBrowserContext browserContext;
	private GSymViewPage page;
	
	private CommandHistory commandHistory;

	private HashMap<ViewFragmentContextAndResultFactoryKey, ViewFragmentContextAndResultFactory> viewFragmentContextAndResultFactories =
		new HashMap<ViewFragmentContextAndResultFactoryKey, ViewFragmentContextAndResultFactory>();
	
	
	
	
	public GSymView(GSymSubject subject, GSymBrowserContext browserContext, PersistentStateStore persistentState)
	{
		super( subject.getFocus(), DuplicatePolicy.ALLOW_DUPLICATES );
		GSymAbstractPerspective perspective = subject.getPerspective();
		if ( perspective == null )
		{
			perspective = browserContext.getGenericPerspective();
		}
		rootNodeResultFactory = makeNodeResultFactory( perspective, subject.getSubjectContext(), StyleValues.instance, SimpleAttributeTable.instance );
		
		rootBox = null;
		
		if ( persistentState != null  &&  persistentState instanceof StateStore )
		{
			stateStoreToLoad = (StateStore)persistentState;
		}
	
	
		
		this.browserContext = browserContext;
		this.commandHistory = subject.getCommandHistory();
		
		region = new DPRegion();
		DPElement column = new Column( new Object[] { region } ).present();

		page = new GSymViewPage( column.alignHExpand().alignVExpand(), subject.getTitle(), browserContext, commandHistory, this );
		
		setElementChangeListener( new NodeElementChangeListenerDiff() );
		
		// We need to do this last
		region.setChild( getRootViewElement().alignHExpand().alignVExpand() );
		region.setEditHandler( perspective.getEditHandler() );
	}
	
	
	public void setElementChangeListener(NodeElementChangeListener elementChangeListener)
	{
		this.elementChangeListener = elementChangeListener;
	}
	
	
	
	
	protected IncrementalTreeNode.NodeResultFactory getRootNodeResultFactory()
	{
		return rootNodeResultFactory;
	}

	protected GSymFragmentView.NodeResultFactory makeNodeResultFactory(GSymAbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
	{
		// Memoise the contents factory, keyed by  @nodeViewFunction and @state
		ViewFragmentContextAndResultFactoryKey key = new ViewFragmentContextAndResultFactoryKey( perspective, subjectContext, style, inheritedState );
		
		ViewFragmentContextAndResultFactory factory = viewFragmentContextAndResultFactories.get( key );
		
		if ( factory == null )
		{
			factory = new ViewFragmentContextAndResultFactory( this, perspective, subjectContext, style, inheritedState );
			viewFragmentContextAndResultFactories.put( key, factory );
		}
		
		return factory;
	}

	
	
	
	
	public DPElement getRootViewElement()
	{
		if ( rootBox == null )
		{
			performRefresh();
			GSymFragmentView rootView = (GSymFragmentView)getRootIncrementalTreeNode();
			DPElement fragmentElement = rootView.getRefreshedFragmentElement();
			rootBox = (DPColumn)new Column( new Object[] { fragmentElement.alignHExpand().alignVExpand() } ).present();
		}
		return rootBox;
	}
	
	
	
	public PersistentStateStore storePersistentState()
	{
		StateStore store = new StateStore();
		
		LinkedList<IncrementalTreeNode> nodeQueue = new LinkedList<IncrementalTreeNode>();
		nodeQueue.push( getRootIncrementalTreeNode() );
		
		while ( !nodeQueue.isEmpty() )
		{
			IncrementalTreeNode node = nodeQueue.removeFirst();
			
			// Get the persistent state, if any, and store it
			GSymFragmentView viewNode = (GSymFragmentView)node;
			PersistentStateTable stateTable = viewNode.getValidPersistentStateTable();
			if ( stateTable != null )
			{
				store.addPersistentState( node.getModel(), stateTable );
			}
			
			// Add the children using an interator; that means that they will be inserted at the beginning
			// of the queue so that they appear *in order*, hence they will be removed in order.
			ListIterator<IncrementalTreeNode> iterator = nodeQueue.listIterator();
			for (IncrementalTreeNode child: ((GSymFragmentView)node).getChildren())
			{
				iterator.add( child );
			}
		}
		
		
		return store;
	}
	
	
	
	protected void performRefresh()
	{
		// >>> PROFILING
		long t1 = 0;
		if ( ENABLE_PROFILING )
		{
			t1 = System.nanoTime();
			ProfileTimer.initProfiling();
			beginProfiling();
		}

		
		profile_startJava();
		// <<< PROFILING
		
		super.performRefresh();
		stateStoreToLoad = null;
		
		// >>> PROFILING
		profile_stopJava();
	
	
		if ( ENABLE_PROFILING )
		{
			long t2 = System.nanoTime();
			endProfiling();
			ProfileTimer.shutdownProfiling();
			double deltaT = ( t2 - t1 )  *  1.0e-9;
			System.out.println( "DocView: REFRESH VIEW TIME = " + deltaT );
			System.out.println( "DocView: REFRESH VIEW PROFILE: JAVA TIME = " + getJavaTime() +
					", PYTHON TIME = " + getPythonTime() + ", CONTENT CHANGE TIME = " + getContentChangeTime() +
					", UPDATE NODE ELEMENT TIME = " + getUpdateNodeElementTime() );
		}
		
		if ( ENABLE_DISPLAY_TREESIZES )
		{
			GSymFragmentView rootView = (GSymFragmentView)getRootIncrementalTreeNode();
			int presTreeSize = rootView.getRefreshedFragmentElement().computeSubtreeSize();
			int numFragments = rootView.computeSubtreeSize();
			System.out.println( "DocView.performRefresh(): presentation tree size=" + presTreeSize + ", # fragments=" + numFragments );
		}
		// <<< PROFILING
	}
	

	private PersistentStateTable persistentStateForNode(Object node)
	{
		PersistentStateTable persistentState = null;
		if ( stateStoreToLoad != null )
		{
			persistentState = stateStoreToLoad.usePersistentState( node );
		}
		return persistentState;
	}
	
	protected IncrementalTreeNode createIncrementalTreeNode(Object node)
	{
		return new GSymFragmentView( node, this, persistentStateForNode( node ) );
	}

	
	
	
	public void profile_startPython()
	{
		if ( bProfilingEnabled )
		{
			pythonTimer.start();
		}
	}
	
	public void profile_stopPython()
	{
		if ( bProfilingEnabled )
		{
			pythonTimer.stop();
		}
	}

	
	public void profile_startJava()
	{
		if ( bProfilingEnabled )
		{
			javaTimer.start();
		}
	}
	
	public void profile_stopJava()
	{
		if ( bProfilingEnabled )
		{
			javaTimer.stop();
		}
	}
	
	
	public void profile_startContentChange()
	{
		if ( bProfilingEnabled )
		{
			contentChangeTimer.start();
		}
	}
	
	public void profile_stopContentChange()
	{
		if ( bProfilingEnabled )
		{
			contentChangeTimer.stop();
		}
	}
	
	
	
	public void profile_startUpdateNodeElement()
	{
		if ( bProfilingEnabled )
		{
			updateNodeElementTimer.start();
		}
	}
	
	public void profile_stopUpdateNodeElement()
	{
		if ( bProfilingEnabled )
		{
			updateNodeElementTimer.stop();
		}
	}
	
	
	
	
	public void beginProfiling()
	{
		bProfilingEnabled = true;
		pythonTimer.reset();
		javaTimer.reset();
		contentChangeTimer.reset();
		updateNodeElementTimer.reset();
	}

	public void endProfiling()
	{
		bProfilingEnabled = false;
	}
	
	public double getPythonTime()
	{
		return pythonTimer.getTime();
	}

	public double getJavaTime()
	{
		return javaTimer.getTime();
	}

	public double getContentChangeTime()
	{
		return contentChangeTimer.getTime();
	}
	
	public double getUpdateNodeElementTime()
	{
		return updateNodeElementTimer.getTime();
	}

	
	

	protected void onResultChangeTreeRefresh()
	{
		if ( elementChangeListener != null )
		{
			elementChangeListener.reset( this );
		}
	}

	protected void onResultChangeFrom(IncrementalTreeNode node, Object result)
	{
		if ( elementChangeListener != null )
		{
			profile_startContentChange();
			elementChangeListener.elementChangeFrom( (GSymFragmentView)node, (DPElement)result );
			profile_stopContentChange();
		}
	}

	protected void onResultChangeTo(IncrementalTreeNode node, Object result)
	{
		if ( elementChangeListener != null )
		{
			profile_startContentChange();
			elementChangeListener.elementChangeTo( (GSymFragmentView)node, (DPElement)result );
			profile_stopContentChange();
		}
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
		return modelRootNode;
	}
	
	
	
	public GSymBrowserContext getBrowserContext()
	{
		return browserContext;
	}
	
	public BrowserPage getPage()
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
	
	
	
	public void onRequestRefresh()
	{
		Runnable r = new Runnable()
		{
			public void run()
			{
				refresh();
			}
		};
		region.queueImmediateEvent( r );
	}



	@Override
	public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres title = titleStyle.applyTo( new Label( "View" ) );
		
		GSymFragmentView rootFragment = (GSymFragmentView)getRootIncrementalTreeNode();
		
		Pres boxContents = contentsStyle.applyTo( new Column( new Object[] { title, rootFragment } ) );

		return new ObjectBorder( boxContents );
	}
	
	
	private static final StyleSheet titleStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.fontBold, true );
	private static final StyleSheet contentsStyle = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 );
}
