//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.IncrementalView;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSFragment;
import BritefuryJ.LSpace.FragmentContext;
import BritefuryJ.LSpace.Browser.Location;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.LSpace.Input.PointerInputElement;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.PersistentState.PersistentStateTable;
import BritefuryJ.ObjectPresentation.PresentationStateListener;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class FragmentView implements IncrementalMonitorListener, FragmentContext, PresentationStateListener, Presentable
{
	public static class CannotChangeModelException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static class ChildrenIterator implements Iterator<FragmentView>
	{
		private FragmentView current;
		
		
		
		private ChildrenIterator(FragmentView childrenHead)
		{
			current = childrenHead;
		}

		
		@Override
		public boolean hasNext()
		{
			return current != null;
		}

		@Override
		public FragmentView next()
		{
			FragmentView res = current;
			current = current.nextSibling;
			return res;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	
	public static class ChildrenIterable implements Iterable<FragmentView>
	{
		private FragmentView node;
		
		private ChildrenIterable(FragmentView node)
		{
			this.node = node;
		}
		
		
		@Override
		public Iterator<FragmentView> iterator()
		{
			return new ChildrenIterator( node.childrenHead );
		}
	}
	
	
	
	private static final ObjectDndHandler.SourceDataFn fragmentDragSourceFn = new ObjectDndHandler.SourceDataFn()
	{
		@Override
		public Object createSourceData(PointerInputElement sourceElement, int aspect)
		{
			LSElement element = (LSElement)sourceElement;
			FragmentView fragment = (FragmentView)element.getFragmentContext();
			return new FragmentData( fragment.getModel(), fragment.getFragmentContentElement() );
		}
	};
	

	private static final ObjectDndHandler.DragSource fragmentDragSource = new ObjectDndHandler.DragSource( FragmentData.class, ObjectDndHandler.ASPECT_DOC_NODE, fragmentDragSourceFn );
	
	
	
	private static final PushElementInteractor fragmentInspectorInteractor = new PushElementInteractor()
	{
		@Override
		public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
		{
			int keyMods = Modifier.getKeyModifiers( event.getModifiers() );
			if ( keyMods == ( Modifier.CTRL | Modifier.ALT )  ||  keyMods == ( Modifier.SHIFT | Modifier.ALT ) )
			{
				if ( event.getButton() == 3 )
				{
					LSElement e = (LSElement)element;
					FragmentView fragment = (FragmentView)e.getFragmentContext();
					return fragment.incView.inspectFragment( fragment, e, event );
				}
			}
			return false;
		}

		@Override
		public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
		{
		}
	};
	
	
	
	
	

	
	protected final static int FLAG_SUBTREE_REFRESH_REQUIRED = 0x1;
	protected final static int FLAG_NODE_REFRESH_REQUIRED = 0x2;
	protected final static int FLAG_NODE_REFRESH_IN_PROGRESS = 0x4;
	
	protected final static int FLAG_REFSTATE_NONE = 0x00;
	protected final static int FLAG_REFSTATE_REFED = 0x08;
	protected final static int FLAG_REFSTATE_UNREFED = 0x10;
	protected final static int _FLAG_REFSTATEMASK = 0x18;
	
	protected final static int FLAGS_INCREMENTALTREENODE_END = 0x20;

	
	
	private IncrementalView incView;
	private Object model;
	
	private IncrementalFunctionMonitor incr;
	protected IncrementalView.FragmentFactory fragmentFactory;
	
	private FragmentView parent, nextSibling;
	private FragmentView childrenHead, childrenTail;
	
	private int flags = 0;
	
	private LSFragment fragmentElement;
	private LSElement element;
	private PersistentStateTable persistentState;
	private PresentationStateListenerList stateListeners;

	
	
	
	
	public FragmentView(Object model, IncrementalView view, PersistentStateTable persistentState)
	{
		setFlag( FLAG_SUBTREE_REFRESH_REQUIRED );
		setFlag( FLAG_NODE_REFRESH_REQUIRED );

		this.incView = view;
		this.model = model;
		
		parent = null;
		nextSibling = null;
		childrenHead = childrenTail = null;
		
		
		fragmentFactory = null;

		incr = new IncrementalFunctionMonitor( this );
		incr.addListener( this );
		
		
		// Fragment element, with null context, initially; later set in @setContext method
		fragmentElement = new LSFragment( this );
		fragmentElement.addDragSource( fragmentDragSource );
		fragmentElement.addElementInteractor( fragmentInspectorInteractor );
		element = null;
		this.persistentState = persistentState;
	}
	
	protected void dispose()
	{
		incr.removeListener( this );
	}
	
	
	
	
	
	//
	//
	// Element acquisition methods
	//
	//
	
	public LSElement getFragmentElement()
	{
		return fragmentElement;
	}
	
	public LSElement getRefreshedFragmentElement()
	{
		refresh();
		return fragmentElement;
	}
	
	
	public LSElement getFragmentContentElement()
	{
		return element;
	}
	
	
	
	
	public boolean isActive()
	{
		return getParent() != null;
	}
	
	
	
	
	
	//
	//
	// Structure / model methods
	//
	//
	
	public IncrementalView getView()
	{
		return incView;
	}
	
	public FragmentView getParent()
	{
		return parent;
	}
	
	protected ChildrenIterable getChildren()
	{
		return new ChildrenIterable( this );
	}
	

	public Object getModel()
	{
		return model;
	}
	
	
	public int computeSubtreeSize()
	{
		int subtreeSize = 1;
		FragmentView child = childrenHead;
		while ( child != null )
		{
			subtreeSize += child.computeSubtreeSize();
			child = child.nextSibling;
		}
		return subtreeSize;
	}
	
	
	

	
	//
	//
	// Context methods
	//
	//
	
	public StyleValues getStyleValues()
	{
		IncrementalView.FragmentFactory f = getFragmentFactory();
		return f.style;
	}
	
	public ProjectiveBrowserContext getBrowserContext()
	{
		return incView.getBrowserContext();
	}
	
	
	public SimpleAttributeTable getSubjectContext()
	{
		return getFragmentFactory().subjectContext;
	}
	
	public AbstractPerspective getPerspective()
	{
		return getFragmentFactory().perspective;
	}
	
	public PresentationContext createPresentationContext()
	{
		IncrementalView.FragmentFactory f = getFragmentFactory();
		return new PresentationContext( this, f.perspective, f.inheritedState );
	}
	
	
	
	
	//
	// Set the fragment factory
	//
	protected void setFragmentFactory(IncrementalView.FragmentFactory factory)
	{
		if ( factory != fragmentFactory )
		{
			fragmentFactory = factory;
			incr.onChanged();
		}
	}
	
	protected IncrementalView.FragmentFactory getFragmentFactory()
	{
		return fragmentFactory;
	}
	
	
	
	//
	//
	// Refresh methods
	//
	//
	
	public void refresh()
	{
		if ( testFlag( FLAG_SUBTREE_REFRESH_REQUIRED ) )
		{
			refreshSubtree();
			clearFlag( FLAG_SUBTREE_REFRESH_REQUIRED );
		}
	}
	
	public void queueRefresh()
	{
		incr.onChanged();
	}
	
	

	
	private void refreshSubtree()
	{
		setFlag( FLAG_NODE_REFRESH_IN_PROGRESS );
		
		incView.onElementChangeFrom( this, element );

		LSElement newElement = element;
		if ( testFlag( FLAG_NODE_REFRESH_REQUIRED ) )
		{
			// Compute the result for this node, and refresh all children
			Object refreshState = incr.onRefreshBegin();
			if ( refreshState != null )
			{
				newElement = computeFragmentContentElement();
			}
			incr.onRefreshEnd( refreshState );
		}
		
		// Refresh each child
		FragmentView child = childrenHead;
		while ( child != null )
		{
			child.refresh();
			child = child.nextSibling;
		}
		
		if ( testFlag( FLAG_NODE_REFRESH_REQUIRED ) )
		{
			incr.onAccess();
			// Set the node result
			updateNodeResult( newElement );
		}
		
		
		incView.onElementChangeTo( this, newElement );
		clearFlag( FLAG_NODE_REFRESH_REQUIRED );

		clearFlag( FLAG_NODE_REFRESH_IN_PROGRESS );
	}
	
	
	private static void unrefSubtree(IncrementalView incView, FragmentView fragment)
	{
		ArrayDeque<FragmentView> q = new ArrayDeque<FragmentView>();
		
		q.addLast( fragment );
		
		while ( !q.isEmpty() )
		{
			FragmentView f = q.removeFirst();
			
			if ( f.getRefState() != FLAG_REFSTATE_UNREFED )
			{
				incView.nodeTable.unrefFragment( f );
				
				FragmentView child = f.childrenHead;
				while ( child != null )
				{
					q.addLast( child );
					
					child = child.nextSibling;
				}
			}
		}
	}
	
	private static void refSubtree(IncrementalView incView, FragmentView fragment)
	{
		ArrayDeque<FragmentView> q = new ArrayDeque<FragmentView>();
		
		q.addLast( fragment );
		
		while ( !q.isEmpty() )
		{
			FragmentView f = q.removeFirst();
			
			if ( f.getRefState() != FLAG_REFSTATE_REFED )
			{
				incView.nodeTable.refFragment( f );
				
				FragmentView child = f.childrenHead;
				while ( child != null )
				{
					q.addLast( child );
					
					child = child.nextSibling;
				}
			}
		}
	}
	
	protected void childDisconnected()
	{
		// ONLY INVOKE AGAINST A NOTE WHICH HAS BEEN UNREFED, AND DURING A REFRESH
		
		FragmentView child = childrenHead;
		while ( child != null )
		{
			FragmentView next = child.nextSibling;

			child.parent = null;
			child.nextSibling = null;
			
			child = next;
		}
		childrenHead = childrenTail = null;
		
		if ( !testFlag( FLAG_NODE_REFRESH_REQUIRED ) )
		{
			setFlag( FLAG_NODE_REFRESH_REQUIRED );
			requestSubtreeRefresh();
		}
	}
	
	
	private LSElement computeFragmentContentElement()
	{
		incView.profile_startModelViewMapping();

		// Unregister existing child relationships
		FragmentView child = childrenHead;
		while ( child != null )
		{
			FragmentView next = child.nextSibling;

			unrefSubtree( incView, child );
			child.parent = null;
			child.nextSibling = null;
			
			child = next;
		}
		childrenHead = childrenTail = null;
		onComputeNodeResultBegin();
		
		if ( fragmentFactory != null )
		{
			LSElement r = fragmentFactory.createFragmentContentElement( incView, this, model );
			
			onComputeNodeResultEnd();
			incView.profile_stopModelViewMapping();
			return r;
		}
		else
		{
			onComputeNodeResultEnd();
			incView.profile_stopModelViewMapping();
			return null;
		}
	}
	
	
	
	//
	//
	// Child / parent relationship methods
	//
	//
	
	private void registerChild(FragmentView child)
	{
		if ( child.parent != null  &&  child.parent != this )
		{
			child.parent.childDisconnected();
		}
		//assert child.parent == null  ||  child.parent == this;

		// Append child to the list of children
		if ( childrenTail != null )
		{
			childrenTail.nextSibling = child;
		}

		if ( childrenHead == null )
		{
			childrenHead = child;
		}
		
		childrenTail = child;

		child.parent = this;

		// Ref the node, so that it is kept around
		
		// We need to disconnect it from any parent node
		//incView.nodeTable.refFragment( child );
		refSubtree( incView, child );
	}


	




	//
	//
	// Child notifications
	//
	//
	
	private void requestSubtreeRefresh()
	{
		if ( !testFlag( FLAG_SUBTREE_REFRESH_REQUIRED ) )
		{
			setFlag( FLAG_SUBTREE_REFRESH_REQUIRED );
			if ( parent != null )
			{
				parent.requestSubtreeRefresh();
			}
			
			incView.onNodeRequestRefresh( this );
		}
	}




	//
	//
	// Refresh methods
	//
	//
	
	protected void updateNodeResult(LSElement r)
	{
		incView.profile_startModifyPresTree();
		if ( r != element )
		{
			if ( r != null )
			{
				element = r;
				fragmentElement.setChild( element );
				StyleValues style = getStyleValues();
				fragmentElement.setAlignment( style.get( Primitive.hAlign, HAlignment.class ), style.get( Primitive.vAlign, VAlignment.class ) );
			}
			else
			{
				element = null;
				fragmentElement.setChild( null );
			}
			stateListeners = PresentationStateListenerList.onPresentationStateChanged( stateListeners, this );
		}
		incView.profile_stopModifyPresTree();
	}
	
	
	
	protected void onComputeNodeResultBegin()
	{
		if ( persistentState != null )
		{
			persistentState.onRefreshBegin();
		}
	}
	
	protected void onComputeNodeResultEnd()
	{
		if ( persistentState != null )
		{
			persistentState.onRefreshEnd();
			if ( persistentState.isEmpty() )
			{
				persistentState = null;
			}
		}
	}

	
	public PersistentStateTable getValidPersistentStateTable()
	{
		if ( persistentState == null )
		{
			persistentState = new PersistentStateTable();
		}
		return persistentState;
	}
	
	public PersistentStateTable getPersistentStateTable()
	{
		return persistentState;
	}
	
	public PersistentState persistentState(Object key)
	{
		return getValidPersistentStateTable().persistentState( key );
	}





	//
	// Inner fragment presentation
	//
	
	public LSElement presentInnerFragment(Object x, AbstractPerspective perspective, StyleValues style, SimpleAttributeTable inheritedState)
	{
		IncrementalView.FragmentFactory factory = getFragmentFactory();
		LSElement e = presentInnerFragment( x, perspective, factory.subjectContext, style, inheritedState );
		if ( perspective != factory.perspective )
		{
			e = perspectiveFragmentRegion( e, perspective );
		}
		return e;
	}
	
	
	
	public LSElement presentLocationAsElement(Location location, StyleValues style, SimpleAttributeTable inheritedState)
	{
		Subject subject = getBrowserContext().resolveLocationAsSubject( location );
		AbstractPerspective perspective = subject.getPerspective();
		if ( perspective == null )
		{
			perspective = DefaultPerspective.instance;
		}
		LSElement e = presentInnerFragment( subject.getFocus(), perspective, subject.getSubjectContext(), style, inheritedState );
		return perspectiveFragmentRegion( e, perspective );
	}
	
	public Location getLocationForObject(Object x)
	{
		return getBrowserContext().getLocationForObject( x );
	}
	
	
	protected static LSElement perspectiveFragmentRegion(LSElement fragmentContents, AbstractPerspective perspective)
	{
		return new Region( fragmentContents, perspective.getClipboardHandler() ).present();
	}
	


	private LSElement presentInnerFragment(Object model, AbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
	{
		if ( model == null )
		{
			return PrimitivePresenter.presentNull().present( new PresentationContext( this, perspective, inheritedState ), style );
		}
		
		
		if ( inheritedState == null )
		{
			throw new RuntimeException( "FragmentView2.presentInnerFragment(): @inheritedState cannot be null" );
		}

		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		FragmentView incrementalNode = incView.buildFragment( model, incView.getUniqueFragmentFactory( perspective, subjectContext, style, inheritedState ) );
		
		
		// Register the parent <-> child relationship before refreshing the node, so that the relationship is 'available' during (re-computation)
		registerChild( incrementalNode );

		// We don't need to refresh the child node - this is done by incremental view after the fragments contents have been computed

		// If a refresh is in progress, we do not need to refresh the child node, as all child nodes will be refreshed by FragmentView.refreshSubtree()
		// Otherwise, we are constructing a presentation of a child node, outside the normal process, in which case, a refresh is required.
		if ( !testFlag( FLAG_NODE_REFRESH_IN_PROGRESS ) )
		{
			// Block access tracking to prevent the contents of this node being dependent upon the child node being refreshed,
			// and refresh the view node
			// Refreshing the child node will ensure that when its contents are inserted into outer elements, its full element tree
			// is up to date and available.
			// Blocking the access tracking prevents an inner node from causing all parent/grandparent/etc nodes from requiring a
			// refresh.
			IncrementalFunctionMonitor currentComputation = IncrementalMonitor.blockAccessTracking();
			incrementalNode.refresh();
			IncrementalMonitor.unblockAccessTracking( currentComputation );
		}
		
		return incrementalNode.getFragmentElement();
	}
	
	
	
	//
	// Complex structure methods
	//
	
	public ArrayList<FragmentView> getNodeViewInstancePathFromRoot()
	{
		ArrayList<FragmentView> path = new ArrayList<FragmentView>();
		
		FragmentView n = this;
		while ( n != null )
		{
			path.add( 0, n );
			n = (FragmentView)n.getParent();
		}
		
		return path;
	}
	
	public ArrayList<FragmentView> getNodeViewInstancePathFromSubtreeRoot(FragmentView root)
	{
		ArrayList<FragmentView> path = new ArrayList<FragmentView>();
		
		FragmentView n = this;
		while ( n != null )
		{
			path.add( 0, n );
			if ( n == root )
			{
				return path;
			}
			n = (FragmentView)n.getParent();
		}

		return null;
	}

	
	public static FragmentView getEnclosingFragment(LSElement element, FragmentViewFilter filter)
	{
		if ( filter == null )
		{
			return (FragmentView)element.getFragmentContext();
		}
		else
		{
			FragmentView fragment = (FragmentView)element.getFragmentContext();
			
			while ( !filter.testFragmentView( fragment ) )
			{
				fragment = (FragmentView)fragment.getParent();
				if ( fragment == null )
				{
					return null;
				}
			}
			
			return fragment;
		}
	}
	
	
	public static FragmentView getCommonRootFragment(FragmentView a, FragmentView b, FragmentViewFilter filter)
	{
		ArrayList<FragmentView> pathA = new ArrayList<FragmentView>();
		ArrayList<FragmentView> pathB = new ArrayList<FragmentView>();
		FragmentView f = null;
		
		f = a;
		while ( f != null )
		{
			pathA.add( f );
			f = (FragmentView)f.getParent();
		}

		f = b;
		while ( f != null )
		{
			pathB.add( f );
			f = (FragmentView)f.getParent();
		}
		
		int top = Math.min( pathA.size(), pathB.size() );
		int commonLength = top;
		for (int i = 0; i < top; i++)
		{
			FragmentView x = pathA.get( pathA.size() - 1 - i );
			FragmentView y = pathB.get( pathB.size() - 1 - i );
			if ( x != y )
			{
				commonLength = i;
				break;
			}
		}
		
		if ( commonLength == 0 )
		{
			return null;
		}
		else
		{
			for (int i = pathA.size() - commonLength ; i < pathA.size(); i++)
			{
				f = pathA.get( i );
				if ( filter.testFragmentView( f ) )
				{
					return f;
				}
			}
			return null;
		}
	}


	
	//
	//
	// Incremental monitor notifications
	//
	//
	
	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		if ( !testFlag( FLAG_NODE_REFRESH_REQUIRED ) )
		{
			setFlag( FLAG_NODE_REFRESH_REQUIRED );
			requestSubtreeRefresh();
		}
	}

	
	
	//
	// Presentation state listener list notification
	//
	
	@Override
	public void onPresentationStateChanged(Object x)
	{
		queueRefresh();
	}
	
	
	

	//
	//
	// Flag methods
	//
	//
	
	protected void clearFlag(int flag)
	{
		flags &= ~flag;
	}
	
	protected void setFlag(int flag)
	{
		flags |= flag;
	}
	
	protected void setFlagValue(int flag, boolean value)
	{
		if ( value )
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
	}
	
	protected boolean testFlag(int flag)
	{
		return ( flags & flag )  !=  0;
	}
	
	protected int getRefState()
	{
		return flags & _FLAG_REFSTATEMASK;
	}
	
	protected void setRefState(int state)
	{
		flags = ( flags & ~_FLAG_REFSTATEMASK ) | state;
	}
	
	protected void setRefStateRefed()
	{
		setRefState( FLAG_REFSTATE_REFED );
	}
	
	protected void setRefStateUnrefed()
	{
		setRefState( FLAG_REFSTATE_UNREFED );
	}
	
	
	
	
	//
	// Presentation
	//
	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		stateListeners = PresentationStateListenerList.addListener( stateListeners, fragment );
		
		
		String debugName = element != null  ?  element.getDebugName()  :  null;
		Pres name;
		if ( debugName != null )
		{
			name = nameStyle.applyTo( new Label( debugName ) );
		}
		else
		{
			name = noNameStyle.applyTo( new Label( "<fragment>" ) );
		}
		
		
		ArrayList<Object> childNodes = new ArrayList<Object>();
		for (FragmentView childTreeNode: getChildren())
		{
			childNodes.add( childTreeNode );
		}
		
		
		Pres childrenPres = new Column( childNodes ).padX( 20.0, 0.0 );
		
		return new Column( new Pres[] { name, childrenPres } );
	}


	private static final StyleSheet nameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );
	private static final StyleSheet noNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.5f ) ) );
	
}
