//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPFragment;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Region;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.PersistentState.PersistentStateTable;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.GenericPerspective.GSymPrimitivePresenter;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.ObjectPresentation.PresentationStateListener;
import BritefuryJ.GSym.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class GSymFragmentView extends IncrementalTreeNode implements FragmentContext, PresentationStateListener, Presentable
{
	public static class FragmentModel
	{
		private Object model;
		
		
		public FragmentModel(Object model)
		{
			this.model = model;
		}
		
		
		public Object getModel()
		{
			return model;
		}
	}
	
	
	private static final ObjectDndHandler.SourceDataFn fragmentDragSourceFn = new ObjectDndHandler.SourceDataFn()
	{
		@Override
		public Object createSourceData(PointerInputElement sourceElement, int aspect)
		{
			DPElement element = (DPElement)sourceElement;
			GSymFragmentView ctx = (GSymFragmentView)element.getFragmentContext();
			return new FragmentModel( ctx.getModel() );
		}
	};
	

	private static final ObjectDndHandler.DragSource fragmentDragSource = new ObjectDndHandler.DragSource( FragmentModel.class, ObjectDndHandler.ASPECT_DOC_NODE, fragmentDragSourceFn );
	
	
	
	
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	
	private DPFragment fragmentElement;
	private DPElement element;
	private PersistentStateTable persistentState;
	private PresentationStateListenerList stateListeners;
	

	
	
	
	public GSymFragmentView(Object modelNode, GSymView view, PersistentStateTable persistentState)
	{
		super( view, modelNode );
		
		// Fragment element, with null context, initially; later set in @setContext method
		fragmentElement = new DPFragment( this );
		fragmentElement.addDragSource( fragmentDragSource );
		element = null;
		this.persistentState = persistentState;
	}
	
	
	
	//
	//
	// Result acquisition methods
	//
	//
	
	public DPElement getFragmentElement()
	{
		return fragmentElement;
	}
	
	public DPElement getRefreshedFragmentElement()
	{
		refresh();
		return fragmentElement;
	}
	
	
	public DPElement getFragmentContentElement()
	{
		return element;
	}
	
	
	protected Object getResultNoRefresh()
	{
		return fragmentElement;
	}
	

	
	
	protected ChildrenIterable getChildren()
	{
		return super.getChildren();
	}

	
	protected GSymView.ViewFragmentContextAndResultFactory getNodeResultFactory()
	{
		return (GSymView.ViewFragmentContextAndResultFactory)resultFactory;
	}

	
	
	//
	//
	// Document view and node / tree methods
	//
	//
	
	public GSymView getView()
	{
		return (GSymView)getIncrementalTree();
	}
	
	
	
	
	//
	//
	// Fragment context methods
	//
	//
	
	@Override
	public PresentationContext createPresentationContext()
	{
		GSymView.ViewFragmentContextAndResultFactory f = getNodeResultFactory();
		return new PresentationContext( this, f.perspective, f.inheritedState );
	}
	
	@Override
	public StyleValues getStyleValues()
	{
		GSymView.ViewFragmentContextAndResultFactory f = getNodeResultFactory();
		return f.style;
	}
	
	
	
	
	//
	//
	// Refresh methods
	//
	//
	
	protected Object computeNodeResult()
	{
		getView().profile_startJava();
		Object result = super.computeNodeResult();
		getView().profile_stopJava();
		return result;
	}

	protected void updateNodeResult(Object r)
	{
		getView().profile_startUpdateNodeElement();
		if ( r != element )
		{
			if ( r != null )
			{
				element = (DPElement)r;
				fragmentElement.setChild( element );
				fragmentElement.copyAlignmentFlagsFrom( element );
			}
			else
			{
				element = null;
				fragmentElement.setChild( null );
				fragmentElement.alignHLeft().alignVRefY();
			}
			stateListeners = PresentationStateListenerList.onPresentationStateChanged( stateListeners, this );
		}
		getView().profile_stopUpdateNodeElement();
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





	public GSymBrowserContext getBrowserContext()
	{
		return getView().getBrowserContext();
	}
	
	
	public SimpleAttributeTable getSubjectContext()
	{
		return getNodeResultFactory().subjectContext;
	}
	
	
	public GSymAbstractPerspective getPerspective()
	{
		return getNodeResultFactory().perspective;
	}
	
	
	
	
	protected void registerIncrementalNodeRelationship(IncrementalTreeNode childNode)
	{
		registerChild( childNode );
	}



	
	private DPElement presentInnerFragment(Object model, GSymAbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
	{
		if ( model == null )
		{
			return GSymPrimitivePresenter.presentNull().present( new PresentationContext( this, perspective, inheritedState ), style );
		}
		
		
		if ( inheritedState == null )
		{
			throw new RuntimeException( "GSymFragmentViewContext.presentFragment(): @state cannot be null" );
		}

		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		GSymView view = getView();
		GSymFragmentView incrementalNode = (GSymFragmentView)view.buildIncrementalTreeNodeResult( model, view.makeNodeResultFactory( perspective, subjectContext, style, inheritedState ) );
		
		
		// Register the parent <-> child relationship before refreshing the node, so that the relationship is 'available' during (re-computation)
		registerIncrementalNodeRelationship( incrementalNode );
		
		// Block access tracking to prevent the contents of this node being dependent upon the child node being refreshed,
		// and refresh the view node
		// Refreshing the child node will ensure that when its contents are inserted into outer elements, its full element tree
		// is up to date and available.
		// Blocking the access tracking prevents an inner node from causing all parent/grandparent/etc nodes from requiring a
		// refresh.
		IncrementalFunctionMonitor currentComputation = IncrementalMonitor.blockAccessTracking();
		incrementalNode.refresh();
		IncrementalMonitor.unblockAccessTracking( currentComputation );
		
		return incrementalNode.getFragmentElement();
	}
	
	protected static DPElement perspectiveFragmentRegion(DPElement fragmentContents, GSymAbstractPerspective perspective)
	{
		return new Region( fragmentContents, perspective.getEditHandler() ).present();
	}
	

	public DPElement presentInnerFragment(Object x, GSymAbstractPerspective perspective, StyleValues style, SimpleAttributeTable inheritedState)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		DPElement e = presentInnerFragment( x, perspective, factory.subjectContext, style, inheritedState );
		if ( perspective != factory.perspective )
		{
			e = perspectiveFragmentRegion( e, perspective );
		}
		return e;
	}
	
	
	
	public DPElement presentLocationAsElement(Location location, StyleValues style, SimpleAttributeTable inheritedState)
	{
		GSymSubject subject = getBrowserContext().resolveLocationAsSubject( location );
		GSymAbstractPerspective perspective = subject.getPerspective();
		if ( perspective == null )
		{
			perspective = getBrowserContext().getGenericPerspective();
		}
		DPElement e = presentInnerFragment( subject.getFocus(), perspective, subject.getSubjectContext(), style, inheritedState );
		return perspectiveFragmentRegion( e, perspective );
	}
	
	public Location getLocationForObject(Object x)
	{
		return getBrowserContext().getLocationForObject( x );
	}
	
	
	
	public ArrayList<GSymFragmentView> getNodeViewInstancePathFromRoot()
	{
		ArrayList<GSymFragmentView> path = new ArrayList<GSymFragmentView>();
		
		GSymFragmentView n = this;
		while ( n != null )
		{
			path.add( 0, n );
			n = (GSymFragmentView)n.getParent();
		}
		
		return path;
	}
	
	public ArrayList<GSymFragmentView> getNodeViewInstancePathFromSubtreeRoot(GSymFragmentView root)
	{
		ArrayList<GSymFragmentView> path = new ArrayList<GSymFragmentView>();
		
		GSymFragmentView n = this;
		while ( n != null )
		{
			path.add( 0, n );
			if ( n == root )
			{
				return path;
			}
			n = (GSymFragmentView)n.getParent();
		}

		return null;
	}

	
	
	public void onPresentationStateChanged(Object x)
	{
		queueRefresh();
	}
	
	
	
	public static GSymFragmentView getEnclosingFragment(DPElement element, FragmentViewFilter filter)
	{
		if ( filter == null )
		{
			return (GSymFragmentView)element.getFragmentContext();
		}
		else
		{
			GSymFragmentView fragment = (GSymFragmentView)element.getFragmentContext();
			
			while ( !filter.testFragmentView( fragment ) )
			{
				fragment = (GSymFragmentView)fragment.getParent();
				if ( fragment == null )
				{
					return null;
				}
			}
			
			return fragment;
		}
	}
	
	
	public static GSymFragmentView getCommonRootFragment(GSymFragmentView a, GSymFragmentView b, FragmentViewFilter filter)
	{
		ArrayList<GSymFragmentView> pathA = new ArrayList<GSymFragmentView>();
		ArrayList<GSymFragmentView> pathB = new ArrayList<GSymFragmentView>();
		GSymFragmentView f = null;
		
		f = a;
		while ( f != null )
		{
			if ( filter.testFragmentView( f ) )
			{
				pathA.add( f );
			}
			f = (GSymFragmentView)f.getParent();
		}

		f = b;
		while ( f != null )
		{
			if ( filter.testFragmentView( f ) )
			{
				pathB.add( f );
			}
			f = (GSymFragmentView)f.getParent();
		}
		
		int top = Math.min( pathA.size(), pathB.size() );
		int commonLength = top;
		for (int i = 0; i < top; i++)
		{
			GSymFragmentView x = pathA.get( pathA.size() - 1 - i );
			GSymFragmentView y = pathB.get( pathB.size() - 1 - i );
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
			return pathA.get( pathA.size() - commonLength );
		}
	}



	@Override
	public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
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
		for (IncrementalTreeNode childTreeNode: getChildren())
		{
			childNodes.add( childTreeNode );
		}
		
		
		Pres childrenPres = new Column( childNodes ).padX( 20.0, 0.0 );
		
		return new Column( new Pres[] { name, childrenPres } );
	}
	
	
	private static final StyleSheet nameStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static final StyleSheet noNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) );
}
