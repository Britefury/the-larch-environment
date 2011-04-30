//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalView;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.PersistentState.PersistentStateTable;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.Logging.Log;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.ObjectPres.ErrorBox;
import BritefuryJ.Pres.ObjectPres.ObjectBorder;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;
import BritefuryJ.Utils.HashUtils;
import BritefuryJ.Utils.WeakIdentityHashMap;
import BritefuryJ.Utils.Profile.ProfileTimer;

public class IncrementalView extends IncrementalTree implements Presentable
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
		public void reset(IncrementalView view);
		public void elementChangeFrom(FragmentView node, DPElement e);
		public void elementChangeTo(FragmentView node, DPElement e);
	}
	
	
	public static class StateStore extends PersistentStateStore
	{
		private WeakIdentityHashMap<Object, LinkedList<PersistentStateTable>> table = new WeakIdentityHashMap<Object, LinkedList<PersistentStateTable>>();
		
		
		public StateStore()
		{
		}
		
		private void addPersistentState(Object model, PersistentStateTable persistentStateTable)
		{
			LinkedList<PersistentStateTable> entryList = table.get( model );
			if ( entryList == null )
			{
				entryList = new LinkedList<PersistentStateTable>();
				table.put( model, entryList );
			}
			entryList.push( persistentStateTable );
		}
		
		private PersistentStateTable usePersistentState(Object model)
		{
			LinkedList<PersistentStateTable> entryList = table.get( model );
			if ( entryList != null  &&  !entryList.isEmpty() )
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
		protected IncrementalView view;
		protected AbstractPerspective perspective;
		protected SimpleAttributeTable subjectContext;
		protected StyleValues style;
		protected SimpleAttributeTable inheritedState;
		
		public ViewFragmentContextAndResultFactory(IncrementalView view, AbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
		{
			this.view = view;
			this.perspective = perspective;
			this.subjectContext = subjectContext;
			this.style = style;
			this.inheritedState = inheritedState;
		}


		public Object createNodeResult(IncrementalTreeNode incrementalNode, Object model)
		{
			view.profile_startPresBuild();

			// Create the node context
			FragmentView fragmentView = (FragmentView)incrementalNode;
			
			// Create the view fragment
			Pres fragment;
			try
			{
				fragment = perspective.presentObject( model, fragmentView, inheritedState );
			}
			catch (Throwable t)
			{
				try
				{
					Pres exceptionView = DefaultPerspective.instance.presentObject( t, fragmentView, inheritedState );
					fragment = new ErrorBox( "Presentation error - exception during presentation", exceptionView );
					return fragment.present( new PresentationContext( fragmentView, DefaultPerspective.instance, inheritedState ), style );
				}
				catch (Exception e2)
				{
					fragment = new ErrorBox( "DOUBLE EXCEPTION!", new Column( new Pres[] {
							labelStyle.applyTo( new Label( "Got exception:" ) ),
							exceptionStyle.applyTo( new StaticText( e2.toString() ) ).padX( 15.0, 0.0 ),
							labelStyle.applyTo( new Label( "While trying to display exception:" ) ),
							exceptionStyle.applyTo( new StaticText( t.toString() ) ).padX( 15.0, 0.0 )   } ) );
					return fragment.present( new PresentationContext( fragmentView, DefaultPerspective.instance, inheritedState ), style );
				}
			}
			
			view.profile_stopPresBuild();
			view.profile_startPresRealise();
			Object nodeResult;
			
			try
			{
				nodeResult = fragment.present( new PresentationContext( fragmentView, perspective, inheritedState ), style );
			}
			catch (Throwable t)
			{
				try
				{
					Pres exceptionView = DefaultPerspective.instance.presentObject( t, fragmentView, inheritedState );
					fragment = new ErrorBox( "Presentation realisation error - exception during presentation realisation", exceptionView );
					return fragment.present( new PresentationContext( fragmentView, DefaultPerspective.instance, inheritedState ), style );
				}
				catch (Exception e2)
				{
					fragment = new ErrorBox( "DOUBLE EXCEPTION!", new Column( new Pres[] {
							labelStyle.applyTo( new Label( "Got exception:" ) ),
							exceptionStyle.applyTo( new StaticText( e2.toString() ) ).padX( 15.0, 0.0 ),
							labelStyle.applyTo( new Label( "While trying to display exception:" ) ),
							exceptionStyle.applyTo( new StaticText( t.toString() ) ).padX( 15.0, 0.0 )   } ) );
					return fragment.present( new PresentationContext( fragmentView, DefaultPerspective.instance, inheritedState ), style );
				}
			}
			
			view.profile_stopPresRealise();
			return nodeResult;
		}

	
		private static final StyleSheet labelStyle = StyleSheet.instance;
		private static final StyleSheet exceptionStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 1.0f, 0.2f, 0.0f ) );
	}
	
	
	protected static class ViewFragmentContextAndResultFactoryKey
	{
		private AbstractPerspective perspective;
		private SimpleAttributeTable subjectContext;
		private StyleValues style;
		private SimpleAttributeTable inheritedState;
		
		
		public ViewFragmentContextAndResultFactoryKey(AbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
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
				throw new RuntimeException( "style == null" );
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
	
	
	
	private class RootPres extends Pres
	{
		@Override
		public DPElement present(PresentationContext ctx, StyleValues style)
		{
			FragmentView.NodeResultFactory resultFactory = makeNodeResultFactory( rootPerspective, subjectContext, style, SimpleAttributeTable.instance );
			setRootNodeResultFactory( resultFactory );
			
			refresh();
			
			FragmentView rootView = (FragmentView)getRootIncrementalTreeNode();
			rootElement = rootView.getRefreshedFragmentElement();
			
			return rootElement;
		}
	}


	
	
	
	private NodeElementChangeListener elementChangeListener = null;
	//private DPColumn rootBox = null;
	
	private StateStore stateStoreToLoad;
	
	
	private boolean bProfilingEnabled = false;
	private ProfileTimer viewTimer = new ProfileTimer();
	private ProfileTimer presBuildTimer = new ProfileTimer();
	private ProfileTimer presRealiseTimer = new ProfileTimer();
	private ProfileTimer handleContentChangeTimer = new ProfileTimer();
	private ProfileTimer commitFragmentElementTimer = new ProfileTimer();
	
	
	
	private ProjectiveBrowserContext browserContext;

	private AbstractPerspective rootPerspective;
	private SimpleAttributeTable subjectContext;
	private ChangeHistory changeHistory;
	private Subject subject;
	
	private RootPres rootPres;
	private Pres viewPres;
	
	protected Log log;
	
	private DPElement rootElement;
	
	

	private HashMap<ViewFragmentContextAndResultFactoryKey, ViewFragmentContextAndResultFactory> viewFragmentContextAndResultFactories =
		new HashMap<ViewFragmentContextAndResultFactoryKey, ViewFragmentContextAndResultFactory>();
	
	
	
	
	public IncrementalView(Subject subject, ProjectiveBrowserContext browserContext, PersistentStateStore persistentState)
	{
		super( subject.getFocus(), DuplicatePolicy.ALLOW_DUPLICATES );
		
		this.subject = subject;
		
		this.rootPerspective = subject.getPerspective();
		if ( this.rootPerspective == null )
		{
			this.rootPerspective = DefaultPerspective.instance;
		}
		this.subjectContext = subject.getSubjectContext();
		this.changeHistory = subject.getChangeHistory();
		
		this.browserContext = browserContext;
	
		log = new Log( "View log" );
		
		if ( persistentState != null  &&  persistentState instanceof StateStore )
		{
			stateStoreToLoad = (StateStore)persistentState;
		}
		
		
		setElementChangeListener( new NodeElementChangeListenerDiff() );
		
		rootPres = new RootPres();
		viewPres = new Column( new Pres[] { new Region( rootPres, getRootPerspective().getClipboardHandler() ) } );
	}
	
	public IncrementalView(Subject subject, ProjectiveBrowserContext browserContext)
	{
		this( subject, browserContext, null );
	}
	
	
	public void setElementChangeListener(NodeElementChangeListener elementChangeListener)
	{
		this.elementChangeListener = elementChangeListener;
	}
	
	
	
	
	protected FragmentView.NodeResultFactory makeNodeResultFactory(AbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
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

	
	
	
	
	public Pres getViewPres()
	{
		return viewPres;
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
			FragmentView viewNode = (FragmentView)node;
			PersistentStateTable stateTable = viewNode.getPersistentStateTable();
			if ( stateTable != null )
			{
				store.addPersistentState( node.getModel(), stateTable );
			}
			
			// Add the children using an interator; that means that they will be inserted at the beginning
			// of the queue so that they appear *in order*, hence they will be removed in order.
			ListIterator<IncrementalTreeNode> iterator = nodeQueue.listIterator();
			for (IncrementalTreeNode child: ((FragmentView)node).getChildren())
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

		
		profile_startView();
		// <<< PROFILING
		
		super.performRefresh();
		stateStoreToLoad = null;
		
		// >>> PROFILING
		profile_stopView();
	
	
		if ( ENABLE_PROFILING )
		{
			long t2 = System.nanoTime();
			endProfiling();
			ProfileTimer.shutdownProfiling();
			double deltaT = ( t2 - t1 )  *  1.0e-9;
			System.out.println( "IncrementalView: REFRESH TIME = " + deltaT );
			System.out.println( "IncrementalView: PROFILE -- view: " + getViewTime() +
					",  pres build: " + getPresBuildTime() +
					",  pres realise: " + getPresRealiseTime() +
					",  handle content change: " + getHandleContentChangeTime() +
					",  commit fragment element: " + getCommitFragmentElementTime() );
		}
		
		if ( ENABLE_DISPLAY_TREESIZES )
		{
			FragmentView rootView = (FragmentView)getRootIncrementalTreeNode();
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
		return new FragmentView( node, this, persistentStateForNode( node ) );
	}

	
	
	
	public void profile_startPresBuild()
	{
		if ( bProfilingEnabled )
		{
			presBuildTimer.start();
		}
	}
	
	public void profile_stopPresBuild()
	{
		if ( bProfilingEnabled )
		{
			presBuildTimer.stop();
		}
	}

	
	public void profile_startPresRealise()
	{
		if ( bProfilingEnabled )
		{
			presRealiseTimer.start();
		}
	}
	
	public void profile_stopPresRealise()
	{
		if ( bProfilingEnabled )
		{
			presRealiseTimer.stop();
		}
	}

	
	public void profile_startView()
	{
		if ( bProfilingEnabled )
		{
			viewTimer.start();
		}
	}
	
	public void profile_stopView()
	{
		if ( bProfilingEnabled )
		{
			viewTimer.stop();
		}
	}
	
	
	public void profile_startHandleContentChange()
	{
		if ( bProfilingEnabled )
		{
			handleContentChangeTimer.start();
		}
	}
	
	public void profile_stopHandleContentChange()
	{
		if ( bProfilingEnabled )
		{
			handleContentChangeTimer.stop();
		}
	}
	
	
	
	public void profile_startCommitFragmentElement()
	{
		if ( bProfilingEnabled )
		{
			commitFragmentElementTimer.start();
		}
	}
	
	public void profile_stopCommitFragmentElement()
	{
		if ( bProfilingEnabled )
		{
			commitFragmentElementTimer.stop();
		}
	}
	
	
	
	
	public void beginProfiling()
	{
		bProfilingEnabled = true;
		presBuildTimer.reset();
		presRealiseTimer.reset();
		viewTimer.reset();
		handleContentChangeTimer.reset();
		commitFragmentElementTimer.reset();
	}

	public void endProfiling()
	{
		bProfilingEnabled = false;
	}
	
	public double getPresBuildTime()
	{
		return presBuildTimer.getTime();
	}

	public double getPresRealiseTime()
	{
		return presRealiseTimer.getTime();
	}

	public double getViewTime()
	{
		return viewTimer.getTime();
	}

	public double getHandleContentChangeTime()
	{
		return handleContentChangeTimer.getTime();
	}
	
	public double getCommitFragmentElementTime()
	{
		return commitFragmentElementTimer.getTime();
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
			profile_startHandleContentChange();
			elementChangeListener.elementChangeFrom( (FragmentView)node, (DPElement)result );
			profile_stopHandleContentChange();
		}
	}

	protected void onResultChangeTo(IncrementalTreeNode node, Object result)
	{
		if ( elementChangeListener != null )
		{
			profile_startHandleContentChange();
			elementChangeListener.elementChangeTo( (FragmentView)node, (DPElement)result );
			profile_stopHandleContentChange();
		}
	}
	
	
	
	
	
	
	
	
	public Caret getCaret()
	{
		PresentationComponent.RootElement elementTree = rootElement.getRootElement();
		return elementTree != null  ?  elementTree.getCaret()  :  null;
	}
	
	public Selection getSelection()
	{
		PresentationComponent.RootElement elementTree = rootElement.getRootElement();
		return elementTree != null  ?  elementTree.getSelection()  :  null;
	}
	
	
	
	
	public Object getDocRootNode()
	{
		return modelRootNode;
	}
	
	
	
	public ProjectiveBrowserContext getBrowserContext()
	{
		return browserContext;
	}
	
	public ChangeHistory getChangeHistory()
	{
		return changeHistory;
	}
	
	public Log getLog()
	{
		return log;
	}
	
	public Subject getSubject()
	{
		return subject;
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
		if ( rootElement != null )
		{
			rootElement.queueImmediateEvent( r );
		}
	}
	
	
	
	private AbstractPerspective getRootPerspective()
	{
		return rootPerspective;
	}



	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres title = titleStyle.applyTo( new Label( "View" ) );
		
		FragmentView rootFragment = (FragmentView)getRootIncrementalTreeNode();
		
		Pres boxContents = contentsStyle.applyTo( new Column( new Object[] { title, rootFragment } ) );

		return new ObjectBorder( boxContents );
	}
	
	
	private static final StyleSheet titleStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.fontBold, true );
	private static final StyleSheet contentsStyle = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 );
}
