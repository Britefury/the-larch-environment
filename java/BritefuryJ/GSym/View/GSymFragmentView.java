//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPFragment;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.PersistentState.PersistentStateTable;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.ObjectPresentation.PresentationStateListener;
import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class GSymFragmentView extends IncrementalTreeNode implements FragmentContext, PresentationStateListener
{
	public static class FragmentDocNode
	{
		private Object docNode;
		
		
		public FragmentDocNode(Object docNode)
		{
			this.docNode = docNode;
		}
		
		
		public Object getDocNode()
		{
			return docNode;
		}
	}
	
	
	private static final ObjectDndHandler.SourceDataFn fragmentDragSourceFn = new ObjectDndHandler.SourceDataFn()
	{
		@Override
		public Object createSourceData(PointerInputElement sourceElement, int aspect)
		{
			DPElement element = (DPElement)sourceElement;
			GSymFragmentView ctx = (GSymFragmentView)element.getFragmentContext();
			return new FragmentDocNode( ctx.getDocNode() );
		}
	};
	

	private static final ObjectDndHandler.DragSource fragmentDragSource = new ObjectDndHandler.DragSource( FragmentDocNode.class, ObjectDndHandler.ASPECT_DOC_NODE, fragmentDragSourceFn );
	
	
	private static final PrimitiveStyleSheet viewError_textStyle = PrimitiveStyleSheet.instance.withFontBold( true ).withFontSize( 12 ).withForeground( new Color( 0.8f, 0.0f, 0.0f ) );
	private static final PrimitiveStyleSheet viewNull_textStyle = PrimitiveStyleSheet.instance.withFontItalic( true ).withFontSize( 12 ).withForeground( new Color( 0.8f, 0.0f, 0.4f ) );

	
	
	
	
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	
	private DPFragment fragmentElement;
	private DPElement element;
	private PersistentStateTable persistentState;
	

	
	
	
	public GSymFragmentView(Object modelNode, GSymView view, PersistentStateTable persistentState)
	{
		super( view, modelNode );
		
		// Fragment element, with null context, initially; later set in @setContext method
		fragmentElement = new DPFragment( this );
		fragmentElement.addDragSource( fragmentDragSource );
		element = null;
		this.persistentState = persistentState;
		if ( this.persistentState == null )
		{
			this.persistentState = new PersistentStateTable();
		}
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

	
	public PersistentStateTable getPersistentStateTable()
	{
		if ( persistentState == null )
		{
			persistentState = new PersistentStateTable();
		}
		return persistentState;
	}
	
	public PersistentState persistentState(Object key)
	{
		return getPersistentStateTable().persistentState( key );
	}





	public GSymBrowserContext getBrowserContext()
	{
		return getView().getBrowserContext();
	}
	
	
	public AttributeTable getSubjectContext()
	{
		return getNodeResultFactory().subjectContext;
	}
	
	
	public GSymAbstractPerspective getPerspective()
	{
		return getNodeResultFactory().perspective;
	}
	
	
	
	
	public DPElement errorElement(String errorText)
	{
		return viewError_textStyle.staticText( errorText );
	}
	
	
	
	protected void registerIncrementalNodeRelationship(IncrementalTreeNode childNode)
	{
		registerChild( childNode );
	}



	
	private DPElement presentFragment(Object x, GSymAbstractPerspective perspective, AttributeTable subjectContext, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		if ( x == null )
		{
			return viewNull_textStyle.staticText( "<null>" );
		}
		
		
		if ( inheritedState == null )
		{
			throw new RuntimeException( "GSymFragmentViewContext.presentFragment(): @state cannot be null" );
		}

		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		GSymView view = getView();
		GSymFragmentView incrementalNode = (GSymFragmentView)view.buildIncrementalTreeNodeResult( x, view.makeNodeResultFactory( perspective, subjectContext, styleSheet, inheritedState ) );
		
		
		// Block access tracking to prevent the contents of this node being dependent upon the child node being refreshed,
		// and refresh the view node
		// Refreshing the child node will ensure that when its contents are inserted into outer elements, its full element tree
		// is up to date and available.
		// Blocking the access tracking prevents an inner node from causing all parent/grandparent/etc nodes from requiring a
		// refresh.
		IncrementalFunctionMonitor currentComputation = IncrementalMonitor.blockAccessTracking();
		incrementalNode.refresh();
		IncrementalMonitor.unblockAccessTracking( currentComputation );
		
		registerIncrementalNodeRelationship( incrementalNode );
		
		return incrementalNode.getFragmentElement();
	}
	
	protected static DPElement perspectiveFragmentRegion(DPElement fragmentContents, GSymAbstractPerspective perspective)
	{
		return PrimitiveStyleSheet.instance.region( fragmentContents, perspective.getEditHandler() );
	}
	

	
	
	public DPElement presentFragment(Object x, StyleSheet styleSheet)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		return presentFragment( x, factory.perspective, factory.subjectContext, styleSheet, factory.inheritedState );
	}

	public DPElement presentFragment(Object x, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		return presentFragment( x, factory.perspective, factory.subjectContext, styleSheet, inheritedState );
	}

	public DPElement presentFragmentWithPerspective(Object x, GSymAbstractPerspective perspective)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		DPElement e = presentFragment( x, perspective, factory.subjectContext, perspective.getStyleSheet(), perspective.getInitialInheritedState() );
		return perspectiveFragmentRegion( e, perspective );
	}

	public DPElement presentFragmentWithPerspective(Object x, GSymAbstractPerspective perspective, AttributeTable inheritedState)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		DPElement e = presentFragment( x, perspective, factory.subjectContext, perspective.getStyleSheet(), inheritedState );
		return perspectiveFragmentRegion( e, perspective );
	}

	public DPElement presentFragmentWithPerspectiveAndStyleSheet(Object x, GSymAbstractPerspective perspective, StyleSheet styleSheet)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		DPElement e = presentFragment( x, perspective, factory.subjectContext, styleSheet, perspective.getInitialInheritedState() );
		return perspectiveFragmentRegion( e, perspective );
	}

	public DPElement presentFragmentWithPerspectiveAndStyleSheet(Object x, GSymAbstractPerspective perspective, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		DPElement e = presentFragment( x, perspective, factory.subjectContext, styleSheet, inheritedState );
		return perspectiveFragmentRegion( e, perspective );
	}
	
	public DPElement presentFragmentWithGenericPerspective(Object x)
	{
		GSymAbstractPerspective genericPerspective = getBrowserContext().getGenericPerspective();
		return presentFragmentWithPerspective( x, genericPerspective );
	}
	
	public DPElement presentFragmentWithGenericPerspective(Object x, AttributeTable inheritedState)
	{
		GSymAbstractPerspective genericPerspective = getBrowserContext().getGenericPerspective();
		return presentFragmentWithPerspective( x, genericPerspective, inheritedState );
	}
	
	
	
	
	private List<DPElement> mapPresentFragment(List<Object> xs, GSymAbstractPerspective perspective, AttributeTable subjectContext,
			StyleSheet styleSheet, AttributeTable inheritedState)
	{
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		children.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			children.add( presentFragment( x, perspective, subjectContext, styleSheet, inheritedState ) );
		}
		return children;
	}
	

	public List<DPElement> mapPresentFragment(List<Object> xs, StyleSheet styleSheet)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		return mapPresentFragment( xs, factory.perspective, factory.subjectContext, styleSheet, factory.inheritedState );
	}

	public List<DPElement> mapPresentFragment(List<Object> xs, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		GSymView.ViewFragmentContextAndResultFactory factory = getNodeResultFactory();
		return mapPresentFragment( xs, factory.perspective, factory.subjectContext, styleSheet, inheritedState );
	}

	
	
	public DPElement presentLocationAsElement(Location location)
	{
		GSymSubject subject = getBrowserContext().resolveLocationAsSubject( location );
		GSymAbstractPerspective perspective = subject.getPerspective();
		DPElement e = presentFragment( subject.getFocus(), perspective, subject.getSubjectContext(), perspective.getStyleSheet(), perspective.getInitialInheritedState() );
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
}
