//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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

public class IncrementalView
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
	
	
	
	
	protected static class FragmentFactory
	{
		protected AbstractPerspective perspective;
		protected SimpleAttributeTable subjectContext;
		protected StyleValues style;
		protected SimpleAttributeTable inheritedState;
		protected int hash;
		
		public FragmentFactory(IncrementalView view, AbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
		{
			if ( style == null )
			{
				throw new RuntimeException( "style == null" );
			}
			this.perspective = perspective;
			this.subjectContext = subjectContext;
			this.style = style;
			this.inheritedState = inheritedState;
			hash = HashUtils.nHash( new int[] { System.identityHashCode( perspective ), style.hashCode(), inheritedState.hashCode(), subjectContext.hashCode() } );
		}


		@Override
		public int hashCode()
		{
			return hash;
		}
		
		@Override
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof FragmentFactory )
			{
				FragmentFactory fx = (FragmentFactory)x;
				return perspective == fx.perspective  &&  style.equals( fx.style )  &&  inheritedState == fx.inheritedState  &&  subjectContext == fx.subjectContext;
			}
			else
			{
				return false;
			}
		}

		
		
		public DPElement createFragmentElement(IncrementalView view, FragmentView incrementalNode, Object model)
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
					view.profile_startPresToElements();
					DPElement element = fragment.present( new PresentationContext( fragmentView, DefaultPerspective.instance, inheritedState ), style );
					view.profile_stopPresToElements();
					return element;
				}
				catch (Exception e2)
				{
					fragment = new ErrorBox( "DOUBLE EXCEPTION!", new Column( new Pres[] {
							labelStyle.applyTo( new Label( "Got exception:" ) ),
							exceptionStyle.applyTo( new StaticText( e2.toString() ) ).padX( 15.0, 0.0 ),
							labelStyle.applyTo( new Label( "While trying to display exception:" ) ),
							exceptionStyle.applyTo( new StaticText( t.toString() ) ).padX( 15.0, 0.0 )   } ) );
					view.profile_stopPresBuild();
					view.profile_startPresToElements();
					DPElement element = fragment.present( new PresentationContext( fragmentView, DefaultPerspective.instance, inheritedState ), style );
					view.profile_stopPresToElements();
					return element;
				}
			}
			
			view.profile_stopPresBuild();
			view.profile_startPresToElements();
			DPElement element;
			
			try
			{
				element = presToElements( fragment, fragmentView );
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
			
			view.profile_stopPresToElements();
			return element;
		}


		private DPElement presToElements(Pres fragment, FragmentView fragmentView)
		{
			return fragment.present( new PresentationContext( fragmentView, perspective, inheritedState ), style );
		}

	
		private static final StyleSheet labelStyle = StyleSheet.instance;
		private static final StyleSheet exceptionStyle = StyleSheet.style( Primitive.foreground.as( new Color( 1.0f, 0.2f, 0.0f ) ) );
	}
	
	
	
	private class RootPres extends Pres
	{
		@Override
		public DPElement present(PresentationContext ctx, StyleValues style)
		{
			FragmentFactory fragmentFactory = getUniqueFragmentFactory( rootPerspective, subjectContext, style, SimpleAttributeTable.instance );
			setRootFragmentFactory( fragmentFactory );
			
			refresh();
			
			FragmentView rootView = (FragmentView)getRootFragment();
			rootElement = rootView.getRefreshedFragmentElement();
			
			return rootElement;
		}
	}
	
	
	
	
	
	private Subject subject;
	private ProjectiveBrowserContext browserContext;
	private SimpleAttributeTable subjectContext;
	private AbstractPerspective rootPerspective;
	protected Object modelRootNode;

	protected IncrementalViewTable nodeTable;
	private FragmentView rootFragment;
	private boolean bRefreshRequired;
	private FragmentFactory rootFragmentFactory;
	
	
	private NodeElementChangeListener elementChangeListener = null;
	//private DPColumn rootBox = null;
	
	private StateStore stateStoreToLoad;
	
	private ChangeHistory changeHistory;

	private Pres viewPres;
	private DPElement rootElement;
	
	protected Log log;
	
	
	
	
	private boolean takePerformanceMeasurements = false;
	private ProfileTimer modelViewMappingTimer = new ProfileTimer();
	private ProfileTimer presBuildTimer = new ProfileTimer();
	private ProfileTimer presToElementsTimer = new ProfileTimer();
	private ProfileTimer handleContentChangeTimer = new ProfileTimer();
	private ProfileTimer modifyPresTreeTimer = new ProfileTimer();
	private ViewProfile profile = null;
	
	
	
	
	private HashMap<FragmentFactory, FragmentFactory> uniqueFragmentFactories = new HashMap<FragmentFactory, FragmentFactory>();
	
	
	
	
	public IncrementalView(Subject subject, ProjectiveBrowserContext browserContext, PersistentStateStore persistentState)
	{
		this.subject = subject;
		this.browserContext = browserContext;

		this.modelRootNode = subject.getFocus();
		
		this.rootPerspective = subject.getPerspective();
		if ( this.rootPerspective == null )
		{
			this.rootPerspective = DefaultPerspective.instance;
		}

		this.subjectContext = subject.getSubjectContext();
		this.changeHistory = subject.getChangeHistory();
		
	
		nodeTable = new IncrementalViewTable();
		bRefreshRequired = false;
	
		
		log = new Log( "View log" );
		
		if ( persistentState != null  &&  persistentState instanceof StateStore )
		{
			stateStoreToLoad = (StateStore)persistentState;
		}
		
		
		elementChangeListener = new NodeElementChangeListenerDiff( this );

		RootPres rootPres = new RootPres();
		viewPres = new Column( new Pres[] { new Region( rootPres, rootPerspective.getClipboardHandler() ) } );
	}
	
	
	public IncrementalView(Subject subject, ProjectiveBrowserContext browserContext)
	{
		this( subject, browserContext, null );
	}

	
	
	//
	// View
	//
	
	public Pres getViewPres()
	{
		return viewPres;
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
	
	
	
	//
	// Model root
	//
	
	public Object getModelRoot()
	{
		return modelRootNode;
	}
	
	
	//
	// Contexts, logs, etc
	//
	
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
	
	
	
	
	
	
	//
	// Refreshing
	//
	
	public void refresh()
	{
		if ( bRefreshRequired )
		{
			bRefreshRequired = false;
			performRefresh();
		}
	}
	

	public void queueRefresh()
	{
		if ( !bRefreshRequired )
		{
			bRefreshRequired = true;
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
	}
	
	
	
	protected void onNodeRequestRefresh(FragmentView node)
	{
		if ( node == rootFragment )
		{
			queueRefresh();
		}
	}
	
	
	private void performRefresh()
	{
		// >>> PROFILING
		long t1 = 0;
		if ( profile != null )
		{
			t1 = System.nanoTime();
			ProfileTimer.initProfiling();
			beginProfiling();
		}

		
		profile_startModelViewMapping();
		// <<< PROFILING
		
		onViewRefreshBegin();
		FragmentView node = getRootFragment();
		if ( node != null )
		{
			node.refresh();
		}
		onViewRefreshEnd();
		
		// Clear unused entries from the node table
		nodeTable.clean();

		stateStoreToLoad = null;
		
		// >>> PROFILING
		profile_stopModelViewMapping();
	
	
		if ( profile != null )
		{
			long t2 = System.nanoTime();
			endProfiling();
			ProfileTimer.shutdownProfiling();
			double deltaT = ( t2 - t1 )  *  1.0e-9;
			ProfileMeasurement m = new ProfileMeasurement( deltaT, getModelViewMappingTime(), getPresBuildTime(), getPresToElementsTime(), getHandleContentChangeTime(), getModifyPresTreeTime() );
			profile.addMeasurement( m );
			System.out.println( "IncrementalView: REFRESH TIME = " + deltaT );
			System.out.println( "IncrementalView: PROFILE -- view: " + getModelViewMappingTime() +
					",  pres build: " + getPresBuildTime() +
					",  pres realise: " + getPresToElementsTime() +
					",  handle content change: " + getHandleContentChangeTime() +
					",  commit fragment element: " + getModifyPresTreeTime() );
		}
		
		if ( ENABLE_DISPLAY_TREESIZES )
		{
			FragmentView rootView = (FragmentView)getRootFragment();
			int presTreeSize = rootView.getRefreshedFragmentElement().computeSubtreeSize();
			int numFragments = rootView.computeSubtreeSize();
			System.out.println( "IncrementalView.performRefresh(): presentation tree size=" + presTreeSize + ", # fragments=" + numFragments );
		}
		// <<< PROFILING
	}
	

	
	
	//
	// Result change handling
	//
	
	protected void onViewRefreshBegin()
	{
		if ( elementChangeListener != null )
		{
			profile_startHandleContentChange();
			elementChangeListener.begin( this );
			profile_stopHandleContentChange();
		}
	}

	protected void onViewRefreshEnd()
	{
		if ( elementChangeListener != null )
		{
			profile_startHandleContentChange();
			elementChangeListener.end( this );
			profile_stopHandleContentChange();
		}
	}

	protected void onElementChangeFrom(FragmentView node, DPElement result)
	{
		if ( elementChangeListener != null )
		{
			profile_startHandleContentChange();
			elementChangeListener.elementChangeFrom( node, result );
			profile_stopHandleContentChange();
		}
	}

	protected void onElementChangeTo(FragmentView node, DPElement result)
	{
		if ( elementChangeListener != null )
		{
			profile_startHandleContentChange();
			elementChangeListener.elementChangeTo( node, result );
			profile_stopHandleContentChange();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	//
	// Fragment building and acquisition
	//
	
	private FragmentView getRootFragment()
	{
		if ( rootFragmentFactory == null )
		{
			throw new RuntimeException( "No root node result factory set" );
		}
		
		
		if ( rootFragment != null )
		{
			nodeTable.unrefFragment( rootFragment );
		}
		if ( rootFragment == null )
		{
			rootFragment = buildFragment( modelRootNode, rootFragmentFactory );
		}
		if ( rootFragment != null )
		{
			nodeTable.refFragment( rootFragment );
		}
		return rootFragment;
	}
	
	
	protected FragmentView buildFragment(Object model, FragmentFactory fragmentFactory)
	{
		if ( model == null )
		{
			return null;
		}
		else
		{
			// Try asking the table for an unused incremental tree node for the document node
			FragmentView fragment = nodeTable.getUnrefedFragmentForModel( model, fragmentFactory );
			
			if ( fragment == null )
			{
				// No existing incremental tree node could be acquired.
				// Create a new one and add it to the table
				fragment = new FragmentView( model, this, persistentStateForNode( model ) );
			}
			
			fragment.setFragmentFactory( fragmentFactory );
			
			return fragment;
		}
	}
	
	
	
	
	//
	// Fragment factories
	//
	
	private void setRootFragmentFactory(FragmentFactory fragmentFactory)
	{
		if ( fragmentFactory != rootFragmentFactory )
		{
			rootFragmentFactory = fragmentFactory;
			queueRefresh();
		}
	}
	
	protected FragmentFactory getUniqueFragmentFactory(AbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
	{
		FragmentFactory factory = new FragmentFactory( this, perspective, subjectContext, style, inheritedState );
		
		FragmentFactory uniqueFactory = uniqueFragmentFactories.get( factory );
		
		if ( uniqueFactory == null )
		{
			uniqueFragmentFactories.put( factory, factory );
			uniqueFactory = factory;
		}
		
		return uniqueFactory;
	}

	
	
	
	
	//
	// Persistent state
	//
	
	
	public PersistentStateStore storePersistentState()
	{
		StateStore store = new StateStore();
		
		LinkedList<FragmentView> nodeQueue = new LinkedList<FragmentView>();
		nodeQueue.push( getRootFragment() );
		
		while ( !nodeQueue.isEmpty() )
		{
			FragmentView node = nodeQueue.removeFirst();
			
			// Get the persistent state, if any, and store it
			FragmentView viewNode = (FragmentView)node;
			PersistentStateTable stateTable = viewNode.getPersistentStateTable();
			if ( stateTable != null )
			{
				store.addPersistentState( node.getModel(), stateTable );
			}
			
			// Add the children using an interator; that means that they will be inserted at the beginning
			// of the queue so that they appear *in order*, hence they will be removed in order.
			ListIterator<FragmentView> iterator = nodeQueue.listIterator();
			for (FragmentView child: ((FragmentView)node).getChildren())
			{
				iterator.add( child );
			}
		}
		
		
		return store;
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
	
	
	
	//
	// Profiling methods
	//
	
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

	
	public void profile_startPresToElements()
	{
		if ( takePerformanceMeasurements )
		{
			presToElementsTimer.start();
		}
	}
	
	public void profile_stopPresToElements()
	{
		if ( takePerformanceMeasurements )
		{
			presToElementsTimer.stop();
		}
	}

	
	public void profile_startModelViewMapping()
	{
		if ( takePerformanceMeasurements )
		{
			modelViewMappingTimer.start();
		}
	}
	
	public void profile_stopModelViewMapping()
	{
		if ( takePerformanceMeasurements )
		{
			modelViewMappingTimer.stop();
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
	
	
	
	public void profile_startModifyPresTree()
	{
		if ( takePerformanceMeasurements )
		{
			modifyPresTreeTimer.start();
		}
	}
	
	public void profile_stopModifyPresTree()
	{
		if ( takePerformanceMeasurements )
		{
			modifyPresTreeTimer.stop();
		}
	}
	
	
	
	
	private void beginProfiling()
	{
		takePerformanceMeasurements = true;
		presBuildTimer.reset();
		presToElementsTimer.reset();
		modelViewMappingTimer.reset();
		handleContentChangeTimer.reset();
		modifyPresTreeTimer.reset();
	}

	private void endProfiling()
	{
		takePerformanceMeasurements = false;
	}
	
	private double getPresBuildTime()
	{
		return presBuildTimer.getTime();
	}

	private double getPresToElementsTime()
	{
		return presToElementsTimer.getTime();
	}

	private double getModelViewMappingTime()
	{
		return modelViewMappingTimer.getTime();
	}

	private double getHandleContentChangeTime()
	{
		return handleContentChangeTimer.getTime();
	}
	
	private double getModifyPresTreeTime()
	{
		return modifyPresTreeTimer.getTime();
	}
	
	
	
	public void enableProfiling()
	{
		profile = new ViewProfile();
	}
	
	public void disableProfiling()
	{
		profile = null;
	}
	
	public ViewProfile getProfile()
	{
		return profile;
	}

	
	

	//
	//
	// Presentation
	//
	//
	
	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres title = titleStyle.applyTo( new Label( "View" ) );
		
		FragmentView rootFragment = (FragmentView)getRootFragment();
		
		Pres boxContents = contentsStyle.applyTo( new Column( new Object[] { title, rootFragment } ) );

		return new ObjectBorder( boxContents );
	}


	private static final StyleSheet titleStyle = StyleSheet.style( Primitive.fontSize.as( 14 ), Primitive.fontBold.as( true ) );
	private static final StyleSheet contentsStyle = StyleSheet.style( Primitive.columnSpacing.as( 5.0 ) );



	//
	//
	// PROFILING
	//
	//




	public static class ProfileMeasurement
	{
		private double refreshTime, modelViewMapping, presBuildTime, presToElementsTime, handleContentChangeTime, modifyPresTreeTime;
		
		public ProfileMeasurement()
		{
		}
		
		public ProfileMeasurement(double refreshTime, double modelViewMapping, double presBuildTime, double presToElementsTime,
							double handleContentChangeTime, double modifyPresTreeTime)
		{
			this.refreshTime = refreshTime;
			this.modelViewMapping = modelViewMapping;
			this.presBuildTime = presBuildTime;
			this.presToElementsTime = presToElementsTime;
			this.handleContentChangeTime = handleContentChangeTime;
			this.modifyPresTreeTime = modifyPresTreeTime;
		}
		
		
		public double getRefreshTime()
		{
			return refreshTime;
		}
		
		public double getModelViewMappingTime()
		{
			return modelViewMapping;
		}
		
		public double getPresBuildTime()
		{
			return presBuildTime;
		}
		
		public double getPresToElementsTime()
		{
			return presToElementsTime;
		}
		
		public double getHandleContentChangeTime()
		{
			return handleContentChangeTime;
		}
		
		public double getModifyPresTreeTime()
		{
			return modifyPresTreeTime;
		}
	}
	
	
	private static AttributeColumn refreshTimeColumn;
	private static AttributeColumn modelViewMappingTimeColumn;
	private static AttributeColumn presBuildTimeColumn;
	private static AttributeColumn presRealiseTimeColumn;
	private static AttributeColumn handleContentChangeTimeColumn;
	private static AttributeColumn commitFragmentElementTimeColumn;
	
	private static ObjectListTableEditor profileTableEditor = null;
	
	
	
	private static ObjectListTableEditor getProfileTableEditor()
	{
		if ( profileTableEditor == null )
		{
			refreshTimeColumn = new AttributeColumn( "Complete refresh", Py.newString( "refreshTime" ) );
			modelViewMappingTimeColumn = new AttributeColumn( "Model-view mapping", Py.newString( "modelViewMappingTime" ) );
			presBuildTimeColumn = new AttributeColumn( "Pres construction", Py.newString( "presBuildTime" ) );
			presRealiseTimeColumn = new AttributeColumn( "Pres to elems", Py.newString( "presToElementsTime" ) );
			handleContentChangeTimeColumn = new AttributeColumn( "Handle content change", Py.newString( "handleContentChangeTime" ) );
			commitFragmentElementTimeColumn = new AttributeColumn( "Modify pres. tree", Py.newString( "modifyPresTreeTime" ) );
			
			profileTableEditor = new ObjectListTableEditor(
					Arrays.asList( new Object[] { refreshTimeColumn, modelViewMappingTimeColumn, presBuildTimeColumn, presRealiseTimeColumn,
							handleContentChangeTimeColumn, commitFragmentElementTimeColumn } ),
					ProfileMeasurement.class, true, true, false, false );
		}
		return profileTableEditor;
	}

	
	
	
	public static class ViewProfile implements ObjectListInterface, Presentable
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
}
