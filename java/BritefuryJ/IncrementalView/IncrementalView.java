//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalView;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import org.python.core.Py;

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
import BritefuryJ.Editor.Table.ObjectList.AttributeColumn;
import BritefuryJ.Editor.Table.ObjectList.ObjectListInterface;
import BritefuryJ.Editor.Table.ObjectList.ObjectListTableEditor;
import BritefuryJ.Incremental.IncrementalValueMonitor;
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
import BritefuryJ.Util.HashUtils;
import BritefuryJ.Util.WeakIdentityHashMap;
import BritefuryJ.Util.Profile.ProfileTimer;

public class IncrementalView extends IncrementalTree implements Presentable
{
	//
	//
	// PROFILING
	//
	//
	
	static boolean ENABLE_DISPLAY_TREESIZES = false;
	
	
	public interface NodeElementChangeListener
	{
		public void begin(IncrementalView view);
		public void end(IncrementalView view);
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
					view.profile_stopPresBuild();
					view.profile_startPresRealise();
					Object result = fragment.present( new PresentationContext( fragmentView, DefaultPerspective.instance, inheritedState ), style );
					view.profile_stopPresRealise();
					return result;
				}
				catch (Exception e2)
				{
					fragment = new ErrorBox( "DOUBLE EXCEPTION!", new Column( new Pres[] {
							labelStyle.applyTo( new Label( "Got exception:" ) ),
							exceptionStyle.applyTo( new StaticText( e2.toString() ) ).padX( 15.0, 0.0 ),
							labelStyle.applyTo( new Label( "While trying to display exception:" ) ),
							exceptionStyle.applyTo( new StaticText( t.toString() ) ).padX( 15.0, 0.0 )   } ) );
					view.profile_stopPresBuild();
					view.profile_startPresRealise();
					Object result = fragment.present( new PresentationContext( fragmentView, DefaultPerspective.instance, inheritedState ), style );
					view.profile_stopPresRealise();
					return result;
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
		private static final StyleSheet exceptionStyle = StyleSheet.style( Primitive.foreground.as( new Color( 1.0f, 0.2f, 0.0f ) ) );
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
	
	
	
	
	public static class ProfileMeasurement
	{
		private double refreshTime, viewTime, presBuildTime, presRealiseTime, handleContentChangeTime, commitFragmentElementTime;
		
		public ProfileMeasurement()
		{
		}
		
		public ProfileMeasurement(double refreshTime, double viewTime, double presBuildTime, double presRealiseTime,
							double handleContentChangeTime, double commitFragmentElementTime)
		{
			this.refreshTime = refreshTime;
			this.viewTime = viewTime;
			this.presBuildTime = presBuildTime;
			this.presRealiseTime = presRealiseTime;
			this.handleContentChangeTime = handleContentChangeTime;
			this.commitFragmentElementTime = commitFragmentElementTime;
		}
		
		
		public double getRefreshTime()
		{
			return refreshTime;
		}
		
		public double getViewTime()
		{
			return viewTime;
		}
		
		public double getPresBuildTime()
		{
			return presBuildTime;
		}
		
		public double getPresRealiseTime()
		{
			return presRealiseTime;
		}
		
		public double getHandleContentChangeTime()
		{
			return handleContentChangeTime;
		}
		
		public double getCommitFragmentElementTime()
		{
			return commitFragmentElementTime;
		}
	}
	
	
	private static AttributeColumn refreshTimeColumn = new AttributeColumn( "Complete refresh", Py.newString( "refreshTime" ) );
	private static AttributeColumn viewTimeColumn = new AttributeColumn( "View", Py.newString( "viewTime" ) );
	private static AttributeColumn presBuildTimeColumn = new AttributeColumn( "Pres construction", Py.newString( "presBuildTime" ) );
	private static AttributeColumn presRealiseTimeColumn = new AttributeColumn( "Pres realise", Py.newString( "presRealiseTime" ) );
	private static AttributeColumn handleContentChangeTimeColumn = new AttributeColumn( "Handle content change", Py.newString( "handleContentChangeTime" ) );
	private static AttributeColumn commitFragmentElementTimeColumn = new AttributeColumn( "Commit fragment element", Py.newString( "commitFragmentElementTime" ) );
	
	private static ObjectListTableEditor profileTableEditor = null;
	
	
	
	private static ObjectListTableEditor getProfileTableEditor()
	{
		if ( profileTableEditor == null )
		{
			profileTableEditor = new ObjectListTableEditor(
					Arrays.asList( new Object[] { refreshTimeColumn, viewTimeColumn, presBuildTimeColumn, presRealiseTimeColumn,
							handleContentChangeTimeColumn, commitFragmentElementTimeColumn } ),
					ProfileMeasurement.class, true, true, false, false );
		}
		return profileTableEditor;
	}

	
	private static class ViewProfile implements ObjectListInterface, Presentable
	{
		private ArrayList<ProfileMeasurement> measurements = new ArrayList<ProfileMeasurement>();
		private IncrementalValueMonitor incr = new IncrementalValueMonitor();
		
		
		public ViewProfile()
		{
		}
		
		
		public void addMeasurement(ProfileMeasurement m)
		{
			measurements.add( m );
			incr.onChanged();
		}
		

		@Override
		public int size()
		{
			incr.onAccess();
			return measurements.size();
		}

		@Override
		public Object get(int i)
		{
			incr.onAccess();
			return measurements.get( i );
		}

		@Override
		public void append(Object x)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeRange(int start, int end)
		{
			throw new UnsupportedOperationException();
		}


		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return getProfileTableEditor().editTable( this );
		}
	}
	
	


	
	
	
	private NodeElementChangeListener elementChangeListener = null;
	//private DPColumn rootBox = null;
	
	private StateStore stateStoreToLoad;
	
	
	private boolean takePerformanceMeasurements = false;
	private ProfileTimer viewTimer = new ProfileTimer();
	private ProfileTimer presBuildTimer = new ProfileTimer();
	private ProfileTimer presRealiseTimer = new ProfileTimer();
	private ProfileTimer handleContentChangeTimer = new ProfileTimer();
	private ProfileTimer commitFragmentElementTimer = new ProfileTimer();
	private ViewProfile profile = null;
	
	
	
	private ProjectiveBrowserContext browserContext;

	private AbstractPerspective rootPerspective;
	private SimpleAttributeTable subjectContext;
	private ChangeHistory changeHistory;
	private Subject subject;

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
		
		
		setElementChangeListener( new NodeElementChangeListenerDiff( this ) );

		RootPres rootPres = new RootPres();
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
	
	
	
	@Override
	protected void performRefresh()
	{
		// >>> PROFILING
		long t1 = 0;
		if ( profile != null )
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
	
	
		if ( profile != null )
		{
			long t2 = System.nanoTime();
			endProfiling();
			ProfileTimer.shutdownProfiling();
			double deltaT = ( t2 - t1 )  *  1.0e-9;
			ProfileMeasurement m = new ProfileMeasurement( deltaT, getViewTime(), getPresBuildTime(), getPresRealiseTime(), getHandleContentChangeTime(), getCommitFragmentElementTime() );
			profile.addMeasurement( m );
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
		if ( takePerformanceMeasurements )
		{
			presBuildTimer.start();
		}
	}
	
	public void profile_stopPresBuild()
	{
		if ( takePerformanceMeasurements )
		{
			presBuildTimer.stop();
		}
	}

	
	public void profile_startPresRealise()
	{
		if ( takePerformanceMeasurements )
		{
			presRealiseTimer.start();
		}
	}
	
	public void profile_stopPresRealise()
	{
		if ( takePerformanceMeasurements )
		{
			presRealiseTimer.stop();
		}
	}

	
	public void profile_startView()
	{
		if ( takePerformanceMeasurements )
		{
			viewTimer.start();
		}
	}
	
	public void profile_stopView()
	{
		if ( takePerformanceMeasurements )
		{
			viewTimer.stop();
		}
	}
	
	
	public void profile_startHandleContentChange()
	{
		if ( takePerformanceMeasurements )
		{
			handleContentChangeTimer.start();
		}
	}
	
	public void profile_stopHandleContentChange()
	{
		if ( takePerformanceMeasurements )
		{
			handleContentChangeTimer.stop();
		}
	}
	
	
	
	public void profile_startCommitFragmentElement()
	{
		if ( takePerformanceMeasurements )
		{
			commitFragmentElementTimer.start();
		}
	}
	
	public void profile_stopCommitFragmentElement()
	{
		if ( takePerformanceMeasurements )
		{
			commitFragmentElementTimer.stop();
		}
	}
	
	
	
	
	private void beginProfiling()
	{
		takePerformanceMeasurements = true;
		presBuildTimer.reset();
		presRealiseTimer.reset();
		viewTimer.reset();
		handleContentChangeTimer.reset();
		commitFragmentElementTimer.reset();
	}

	private void endProfiling()
	{
		takePerformanceMeasurements = false;
	}
	
	private double getPresBuildTime()
	{
		return presBuildTimer.getTime();
	}

	private double getPresRealiseTime()
	{
		return presRealiseTimer.getTime();
	}

	private double getViewTime()
	{
		return viewTimer.getTime();
	}

	private double getHandleContentChangeTime()
	{
		return handleContentChangeTimer.getTime();
	}
	
	private double getCommitFragmentElementTime()
	{
		return commitFragmentElementTimer.getTime();
	}
	
	
	
	public void enableProfiling()
	{
		System.out.println( "IncrementalView.enableProfiling" );
		profile = new ViewProfile();
	}
	
	public void disableProfiling()
	{
		System.out.println( "IncrementalView.disableProfiling" );
		profile = null;
	}
	
	public ViewProfile getProfile()
	{
		return profile;
	}

	
	

	@Override
	protected void onResultChangeTreeRefreshBegin()
	{
		if ( elementChangeListener != null )
		{
			elementChangeListener.begin( this );
		}
	}

	@Override
	protected void onResultChangeTreeRefreshEnd()
	{
		if ( elementChangeListener != null )
		{
			elementChangeListener.end( this );
		}
	}

	@Override
	protected void onResultChangeFrom(IncrementalTreeNode node, Object result)
	{
		if ( elementChangeListener != null )
		{
			profile_startHandleContentChange();
			elementChangeListener.elementChangeFrom( (FragmentView)node, (DPElement)result );
			profile_stopHandleContentChange();
		}
	}

	@Override
	protected void onResultChangeTo(IncrementalTreeNode node, Object result)
	{
		if ( elementChangeListener != null )
		{
			profile_startHandleContentChange();
			elementChangeListener.elementChangeTo( (FragmentView)node, (DPElement)result );
			profile_stopHandleContentChange();
		}
	}
	
	
	
	
	
	
	public PresentationComponent.RootElement getPresentationRootElement()
	{
		return rootElement != null  ?  rootElement.getRootElement()  :  null;
	}
	
	public Caret getCaret()
	{
		PresentationComponent.RootElement elementTree = getPresentationRootElement();
		return elementTree != null  ?  elementTree.getCaret()  :  null;
	}
	
	public Selection getSelection()
	{
		PresentationComponent.RootElement elementTree = getPresentationRootElement();
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
	
	
	@Override
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



	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres title = titleStyle.applyTo( new Label( "View" ) );
		
		FragmentView rootFragment = (FragmentView)getRootIncrementalTreeNode();
		
		Pres boxContents = contentsStyle.applyTo( new Column( new Object[] { title, rootFragment } ) );

		return new ObjectBorder( boxContents );
	}


	private static final StyleSheet titleStyle = StyleSheet.style( Primitive.fontSize.as( 14 ), Primitive.fontBold.as( true ) );
	private static final StyleSheet contentsStyle = StyleSheet.style( Primitive.columnSpacing.as( 5.0 ) );
}
