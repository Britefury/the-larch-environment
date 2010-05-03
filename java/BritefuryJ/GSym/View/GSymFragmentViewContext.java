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
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.PersistentState.PersistentStateTable;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.GSymPerspective;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.GenericPerspective.PresentationStateListener;
import BritefuryJ.Incremental.IncrementalFunction;
import BritefuryJ.Incremental.IncrementalValue;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class GSymFragmentViewContext implements IncrementalTreeNode.NodeContext, FragmentContext, PresentationStateListener
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
	
	
	private ObjectDndHandler.SourceDataFn fragmentDragSourceFn = new ObjectDndHandler.SourceDataFn()
	{
		@Override
		public Object createSourceData(PointerInputElement sourceElement, int aspect)
		{
			DPElement element = (DPElement)sourceElement;
			GSymFragmentViewContext ctx = (GSymFragmentViewContext)element.getFragmentContext();
			return new FragmentDocNode( ctx.viewNode.getDocNode() );
		}
	};
	

	private ObjectDndHandler.DragSource fragmentDragSource = new ObjectDndHandler.DragSource( FragmentDocNode.class, ObjectDndHandler.ASPECT_DOC_NODE, fragmentDragSourceFn );
	
	
	private static final PrimitiveStyleSheet viewError_textStyle = PrimitiveStyleSheet.instance.withFontBold( true ).withFontSize( 12 ).withForeground( new Color( 0.8f, 0.0f, 0.0f ) );
	private static final PrimitiveStyleSheet viewNull_textStyle = PrimitiveStyleSheet.instance.withFontItalic( true ).withFontSize( 12 ).withForeground( new Color( 0.8f, 0.0f, 0.4f ) );

	

	protected GSymViewContext.ViewFragmentContextAndResultFactory factory;
	protected DVNode viewNode;

	
	
	public GSymFragmentViewContext(GSymViewContext.ViewFragmentContextAndResultFactory factory, DVNode viewNode)
	{
		this.factory = factory;
		this.viewNode = viewNode;
		this.viewNode.setContext( this );
		this.viewNode.setFragmentContext( this );
		this.viewNode.getElementNoRefresh().addDragSource( fragmentDragSource );
	}
	
	
	
	public GSymViewContext getViewContext()
	{
		return factory.viewContext;
	}
	
	public GSymBrowserContext getBrowserContext()
	{
		return factory.viewContext.getBrowserContext();
	}
	
	
	public AttributeTable getSubjectContext()
	{
		return factory.subjectContext;
	}
	
	
	public GSymPerspective getPerspective()
	{
		return factory.perspective;
	}
	
	
	
	
	public Object getDocNode()
	{
		return viewNode.getDocNode();
	}
	
	
	
	public PersistentState persistentState(Object key)
	{
		return viewNode.persistentState( key );
	}
	
	public PersistentStateTable getPersistentStateTable()
	{
		return viewNode.getPersistentStateTable();
	}
	
	
	

	public DPElement errorElement(String errorText)
	{
		return viewError_textStyle.staticText( errorText );
	}
	
	
	
	protected void registerIncrementalNodeRelationship(IncrementalTreeNode childNode)
	{
		viewNode.registerChild( childNode );
	}



	
	private DPElement presentFragment(Object x, GSymPerspective perspective, AttributeTable subjectContext, StyleSheet styleSheet, AttributeTable inheritedState)
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
		GSymViewContext viewContext = factory.viewContext;
		DVNode incrementalNode = (DVNode)viewContext.getView().buildIncrementalTreeNodeResult( x,
				viewContext.makeNodeResultFactory( perspective, subjectContext, styleSheet, inheritedState ) );
		
		
		// Block access tracking to prevent the contents of this node being dependent upon the child node being refreshed,
		// and refresh the view node
		// Refreshing the child node will ensure that when its contents are inserted into outer elements, its full element tree
		// is up to date and available.
		// Blocking the access tracking prevents an inner node from causing all parent/grandparent/etc nodes from requiring a
		// refresh.
		IncrementalFunction currentComputation = IncrementalValue.blockAccessTracking();
		incrementalNode.refresh();
		IncrementalValue.unblockAccessTracking( currentComputation );
		
		registerIncrementalNodeRelationship( incrementalNode );
		
		return incrementalNode.getElementNoRefresh();
	}
	
	protected static DPElement perspectiveFragmentRegion(DPElement fragmentContents, GSymPerspective perspective)
	{
		return PrimitiveStyleSheet.instance.region( fragmentContents, perspective.getEditHandler() );
	}
	

	
	
	public DPElement presentFragment(Object x, StyleSheet styleSheet)
	{
		return presentFragment( x, factory.perspective, factory.subjectContext, styleSheet, factory.inheritedState );
	}

	public DPElement presentFragment(Object x, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		return presentFragment( x, factory.perspective, factory.subjectContext, styleSheet, inheritedState );
	}

	public DPElement presentFragmentWithPerspective(Object x, GSymPerspective perspective)
	{
		DPElement e = presentFragment( x, perspective, factory.subjectContext, perspective.getStyleSheet(), perspective.getInitialInheritedState() );
		return perspectiveFragmentRegion( e, perspective );
	}

	public DPElement presentFragmentWithPerspective(Object x, GSymPerspective perspective, AttributeTable inheritedState)
	{
		DPElement e = presentFragment( x, perspective, factory.subjectContext, perspective.getStyleSheet(), inheritedState );
		return perspectiveFragmentRegion( e, perspective );
	}

	public DPElement presentFragmentWithPerspectiveAndStyleSheet(Object x, GSymPerspective perspective, StyleSheet styleSheet)
	{
		DPElement e = presentFragment( x, perspective, factory.subjectContext, styleSheet, perspective.getInitialInheritedState() );
		return perspectiveFragmentRegion( e, perspective );
	}

	public DPElement presentFragmentWithPerspectiveAndStyleSheet(Object x, GSymPerspective perspective, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		DPElement e = presentFragment( x, perspective, factory.subjectContext, styleSheet, inheritedState );
		return perspectiveFragmentRegion( e, perspective );
	}
	
	public DPElement presentFragmentWithGenericPerspective(Object x)
	{
		GSymPerspective genericPerspective = getViewContext().getBrowserContext().getGenericPerspective();
		return presentFragmentWithPerspective( x, genericPerspective );
	}
	
	public DPElement presentFragmentWithGenerixcPerspective(Object x, AttributeTable inheritedState)
	{
		GSymPerspective genericPerspective = getViewContext().getBrowserContext().getGenericPerspective();
		return presentFragmentWithPerspective( x, genericPerspective, inheritedState );
	}
	
	
	
	
	private List<DPElement> mapPresentFragment(List<Object> xs, GSymPerspective perspective, AttributeTable subjectContext,
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
		return mapPresentFragment( xs, factory.perspective, factory.subjectContext, styleSheet, factory.inheritedState );
	}

	public List<DPElement> mapPresentFragment(List<Object> xs, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		return mapPresentFragment( xs, factory.perspective, factory.subjectContext, styleSheet, inheritedState );
	}

	
	
	public DPElement presentLocationAsElement(Location location)
	{
		GSymSubject subject = getViewContext().getBrowserContext().resolveLocationAsSubject( location );
		GSymPerspective perspective = subject.getPerspective();
		DPElement e = presentFragment( subject.getFocus(), perspective, subject.getSubjectContext(), perspective.getStyleSheet(), perspective.getInitialInheritedState() );
		return perspectiveFragmentRegion( e, perspective );
	}
	
	public Location getLocationForObject(Object x)
	{
		return getViewContext().getBrowserContext().getLocationForObject( x );
	}
	
	
	
	public void queueRefresh()
	{
		viewNode.queueRefresh();
	}
	
	
	
	public DPElement getViewNodeElement()
	{
		return viewNode.getElementNoRefresh();
	}
	
	public DPElement getViewNodeContentElement()
	{
		return viewNode.getInnerElementNoRefresh();
	}
	
	
	
	public GSymFragmentViewContext getParent()
	{
		DVNode parentViewNode = (DVNode)viewNode.getParent();
		return parentViewNode != null  ?  (GSymFragmentViewContext)parentViewNode.getContext()  :  null;
	}
	

	public ArrayList<GSymFragmentViewContext> getNodeViewInstancePathFromRoot()
	{
		ArrayList<GSymFragmentViewContext> path = new ArrayList<GSymFragmentViewContext>();
		
		GSymFragmentViewContext n = this;
		while ( n != null )
		{
			path.add( 0, n );
			n = n.getParent();
		}
		
		return path;
	}
	
	public ArrayList<GSymFragmentViewContext> getNodeViewInstancePathFromSubtreeRoot(GSymFragmentViewContext root)
	{
		ArrayList<GSymFragmentViewContext> path = new ArrayList<GSymFragmentViewContext>();
		
		GSymFragmentViewContext n = this;
		while ( n != null )
		{
			path.add( 0, n );
			if ( n == root )
			{
				return path;
			}
			n = n.getParent();
		}

		return null;
	}

	
	
	private GSymFragmentViewContext getPreviousSiblingFromChildElement(GSymFragmentViewContext parent, DPElement fromChild)
	{
		if ( fromChild == null )
		{
			return null;
		}
		DPContainer parentElement = fromChild.getParent();
		if ( parentElement == parent.getViewNodeElement() )
		{
			return null;
		}
		
		List<DPElement> children = parentElement.getChildren();
		int index = children.indexOf( fromChild );
		for (int i = index - 1; i >= 0; i--)
		{
			GSymFragmentViewContext sibling = getLastChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymFragmentViewContext getLastChildFromParentElement(GSymFragmentViewContext parent, DPElement element)
	{
		if ( element.getFragmentContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymFragmentViewContext)element.getFragmentContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			List<DPElement> children = branch.getChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				GSymFragmentViewContext sibling = getLastChildFromParentElement( parent, children.get( i ) );
				if ( sibling != null )
				{
					return sibling;
				}
			}
		}
		return null;
	}
	

	
	private GSymFragmentViewContext getNextSiblingFromChildElement(GSymFragmentViewContext parent, DPElement fromChild)
	{
		if ( fromChild == null )
		{
			return null;
		}
		DPContainer parentElement = fromChild.getParent();
		if ( parentElement == parent.getViewNodeElement() )
		{
			return null;
		}
		
		List<DPElement> children = parentElement.getChildren();
		int index = children.indexOf( fromChild );
		for (int i = index + 1; i < children.size(); i++)
		{
			GSymFragmentViewContext sibling = getFirstChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymFragmentViewContext getFirstChildFromParentElement(GSymFragmentViewContext parent, DPElement element)
	{
		if ( element.getFragmentContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymFragmentViewContext)element.getFragmentContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			for (DPElement child: branch.getChildren())
			{
				GSymFragmentViewContext sibling = getFirstChildFromParentElement( parent, child );
				if ( sibling != null )
				{
					return sibling;
				}
			}
		}
		return null;
	}
	

	
	public GSymFragmentViewContext getPrevSibling()
	{
		return getPreviousSiblingFromChildElement( (GSymFragmentViewContext)getParent(), getViewNodeElement() );
	}
	
	public GSymFragmentViewContext getNextSibling()
	{
		return getNextSiblingFromChildElement( this, getViewNodeElement() );
	}
	
	
	
	public GSymFragmentViewContext getFirstChild()
	{
		return getFirstChildFromParentElement( (GSymFragmentViewContext)getParent(), getViewNodeElement() );
	}
	
	public GSymFragmentViewContext getLastChild()
	{
		return getNextSiblingFromChildElement( this, getViewNodeElement() );
	}
	
	
	public void onPresentationStateChanged(Object x)
	{
		queueRefresh();
	}
}
