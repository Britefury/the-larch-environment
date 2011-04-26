//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalView;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPFragment;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.PersistentState.PersistentStateTable;
import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
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

public class FragmentView extends IncrementalTreeNode implements FragmentContext, PresentationStateListener, Presentable
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
			FragmentView ctx = (FragmentView)element.getFragmentContext();
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
	

	
	
	
	public FragmentView(Object modelNode, IncrementalView view, PersistentStateTable persistentState)
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
	

	
	
	public boolean isActive()
	{
		return getParent() != null;
	}
	
	protected ChildrenIterable getChildren()
	{
		return super.getChildren();
	}

	
	protected IncrementalView.ViewFragmentContextAndResultFactory getNodeResultFactory()
	{
		return (IncrementalView.ViewFragmentContextAndResultFactory)resultFactory;
	}

	
	
	//
	//
	// Document view and node / tree methods
	//
	//
	
	public IncrementalView getView()
	{
		return (IncrementalView)getIncrementalTree();
	}
	
	
	
	
	//
	//
	// Fragment context methods
	//
	//
	
	@Override
	public PresentationContext createPresentationContext()
	{
		IncrementalView.ViewFragmentContextAndResultFactory f = getNodeResultFactory();
		return new PresentationContext( this, f.perspective, f.inheritedState );
	}
	
	@Override
	public StyleValues getStyleValues()
	{
		IncrementalView.ViewFragmentContextAndResultFactory f = getNodeResultFactory();
		return f.style;
	}
	
	
	
	
	//
	//
	// Refresh methods
	//
	//
	
	protected Object computeNodeResult()
	{
		getView().profile_startView();
		Object result = super.computeNodeResult();
		getView().profile_stopView();
		return result;
	}

	protected void updateNodeResult(Object r)
	{
		getView().profile_startCommitFragmentElement();
		if ( r != element )
		{
			if ( r != null )
			{
				element = (DPElement)r;
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
		getView().profile_stopCommitFragmentElement();
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





	public ProjectiveBrowserContext getBrowserContext()
	{
		return getView().getBrowserContext();
	}
	
	
	public SimpleAttributeTable getSubjectContext()
	{
		return getNodeResultFactory().subjectContext;
	}
	
	
	public AbstractPerspective getPerspective()
	{
		return getNodeResultFactory().perspective;
	}
	
	
	
	
	protected void registerIncrementalNodeRelationship(IncrementalTreeNode childNode)
	{
		registerChild( childNode );
	}



	
	private DPElement presentInnerFragment(Object model, AbstractPerspective perspective, SimpleAttributeTable subjectContext, StyleValues style, SimpleAttributeTable inheritedState)
	{
		if ( model == null )
		{
			return PrimitivePresenter.presentNull().present( new PresentationContext( this, perspective, inheritedState ), style );
		}
		
		
		if ( inheritedState == null )
		{
			throw new RuntimeException( "FragmentView.presentInnerFragment(): @inheritedState cannot be null" );
		}

		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		IncrementalView view = getView();
		FragmentView incrementalNode = (FragmentView)view.buildIncrementalTreeNodeResult( model, view.makeNodeResultFactory( perspective, subjectContext, style, inheritedState ) );
		
		
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
	
	protected static DPElement perspectiveFragmentRegion(DPElement fragmentContents, AbstractPerspective perspective)
	{
		return new Region( fragmentContents, perspective.getClipboardHandler() ).present();
	}
	

	public DPElement presentInnerFragment(Object x, AbstractPerspective perspective, StyleValues style, SimpleAttributeTable inheritedState)
	{
		IncrementalView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		DPElement e = presentInnerFragment( x, perspective, factory.subjectContext, style, inheritedState );
		if ( perspective != factory.perspective )
		{
			e = perspectiveFragmentRegion( e, perspective );
		}
		return e;
	}
	
	
	
	public DPElement presentLocationAsElement(Location location, StyleValues style, SimpleAttributeTable inheritedState)
	{
		Subject subject = getBrowserContext().resolveLocationAsSubject( location );
		AbstractPerspective perspective = subject.getPerspective();
		if ( perspective == null )
		{
			perspective = DefaultPerspective.instance;
		}
		DPElement e = presentInnerFragment( subject.getFocus(), perspective, subject.getSubjectContext(), style, inheritedState );
		return perspectiveFragmentRegion( e, perspective );
	}
	
	public Location getLocationForObject(Object x)
	{
		return getBrowserContext().getLocationForObject( x );
	}
	
	
	
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

	
	
	public void onPresentationStateChanged(Object x)
	{
		queueRefresh();
	}
	
	
	
	public static FragmentView getEnclosingFragment(DPElement element, FragmentViewFilter filter)
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
			if ( filter.testFragmentView( f ) )
			{
				pathA.add( f );
			}
			f = (FragmentView)f.getParent();
		}

		f = b;
		while ( f != null )
		{
			if ( filter.testFragmentView( f ) )
			{
				pathB.add( f );
			}
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
			return pathA.get( pathA.size() - commonLength );
		}
	}



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
