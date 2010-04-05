//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.GSym.GSymPerspective;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.Incremental.IncrementalFunction;
import BritefuryJ.Incremental.IncrementalValue;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class GSymFragmentViewContext implements IncrementalTreeNode.NodeContext, FragmentContext
{
	private static final PrimitiveStyleSheet viewError_textStyle = PrimitiveStyleSheet.instance.withFont( new Font( "SansSerif", Font.BOLD, 12 ) ).withForeground( new Color( 0.8f, 0.0f, 0.0f ) );

	

	protected GSymViewContext.ViewFragmentContextAndResultFactory factory;
	protected DVNode viewNode;
	protected AttributeTable subjectContext;

	
	
	public GSymFragmentViewContext(GSymViewContext.ViewFragmentContextAndResultFactory factory, DVNode viewNode, AttributeTable subjectContext)
	{
		this.factory = factory;
		this.viewNode = viewNode;
		this.viewNode.setContext( this );
		this.viewNode.setFragmentContext( this );
		this.subjectContext = subjectContext;
	}
	
	
	
	public GSymViewContext getViewContext()
	{
		return factory.viewContext;
	}
	
	
	public AttributeTable getSubjectContext()
	{
		return subjectContext;
	}
	
	
	
	
	public Object getDocNode()
	{
		return viewNode.getDocNode();
	}
	
	
	

	public DPElement errorElement(String errorText)
	{
		return viewError_textStyle.staticText( errorText );
	}
	
	
	
	protected void registerIncrementalNodeRelationship(IncrementalTreeNode childNode)
	{
		viewNode.registerChild( childNode );
	}



	
	private DPElement presentFragment(Object x, GSymPerspective perspective, GSymViewFragmentFunction fragmentViewFunction, AttributeTable subjectContext, StyleSheet styleSheet, AttributeTable state)
	{
		if ( x == null )
		{
			throw new RuntimeException( "GSymFragmentViewContext.presentFragment(): cannot build view of null node" );
		}
		
		if ( state == null )
		{
			throw new RuntimeException( "GSymFragmentViewContext.presentFragment(): @state cannot be null" );
		}

		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		GSymViewContext viewContext = factory.viewContext;
		DVNode incrementalNode = (DVNode)viewContext.getView().buildIncrementalTreeNodeResult( x, viewContext.makeNodeResultFactory( perspective, fragmentViewFunction, subjectContext, styleSheet, state ) );
		
		
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
	

	
	
	public DPElement presentFragment(Object x, StyleSheet styleSheet)
	{
		return presentFragment( x, factory.perspective, factory.viewFragmentFunction, subjectContext, styleSheet, factory.state );
	}

	public DPElement presentFragment(Object x, StyleSheet styleSheet, AttributeTable state)
	{
		return presentFragment( x, factory.perspective, factory.viewFragmentFunction, subjectContext, styleSheet, state );
	}

	public DPElement presentFragmentFn(Object x, StyleSheet styleSheet, GSymViewFragmentFunction fragmentViewFunction)
	{
		return presentFragment( x, factory.perspective, fragmentViewFunction, subjectContext, styleSheet, factory.state );
	}

	public DPElement presentFragmentFn(Object x, StyleSheet styleSheet, GSymViewFragmentFunction fragmentViewFunction, AttributeTable state)
	{
		return presentFragment( x, factory.perspective, fragmentViewFunction, subjectContext, styleSheet, state );
	}

	public DPElement presentFragmentWithPerspective(Object x, GSymPerspective perspective)
	{
		return presentFragment( x, perspective, perspective.getFragmentViewFunction(), subjectContext, perspective.getStyleSheet(), perspective.getInitialState() );
	}

	public DPElement presentFragmentWithPerspective(Object x, GSymPerspective perspective, AttributeTable state)
	{
		return presentFragment( x, perspective, perspective.getFragmentViewFunction(), subjectContext, perspective.getStyleSheet(), state );
	}
	
	
	
	
	private List<DPElement> mapPresentFragment(List<Object> xs, GSymPerspective perspective, GSymViewFragmentFunction fragmentViewFunction, AttributeTable subjectContext,
			StyleSheet styleSheet, AttributeTable state)
	{
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		children.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			children.add( presentFragment( x, perspective, fragmentViewFunction, subjectContext, styleSheet, state ) );
		}
		return children;
	}
	

	public List<DPElement> mapPresentFragment(List<Object> xs, StyleSheet styleSheet)
	{
		return mapPresentFragment( xs, factory.perspective, factory.viewFragmentFunction, subjectContext, styleSheet, factory.state );
	}

	public List<DPElement> mapPresentFragment(List<Object> xs, StyleSheet styleSheet, AttributeTable state)
	{
		return mapPresentFragment( xs, factory.perspective, factory.viewFragmentFunction, subjectContext, styleSheet, state );
	}

	public List<DPElement> mapPresentFragmentFn(List<Object> xs, StyleSheet styleSheet, GSymViewFragmentFunction nodeViewFunction)
	{
		return mapPresentFragment( xs, factory.perspective, nodeViewFunction, subjectContext, styleSheet, factory.state );
	}

	public List<DPElement> mapPresentFragmentFn(List<Object> xs, StyleSheet styleSheet, GSymViewFragmentFunction nodeViewFunction, AttributeTable state)
	{
		return mapPresentFragment( xs, factory.perspective, nodeViewFunction, subjectContext, styleSheet, state );
	}
	
	public List<DPElement> mapPresentFragmentPerspective(List<Object> xs, GSymPerspective perspective)
	{
		return mapPresentFragment( xs, perspective, perspective.getFragmentViewFunction(), subjectContext, perspective.getStyleSheet(), perspective.getInitialState() );
	}

	public List<DPElement> mapPresentFragmentPerspective(List<Object> xs, GSymPerspective perspective, AttributeTable state)
	{
		return mapPresentFragment( xs, perspective, perspective.getFragmentViewFunction(), subjectContext, perspective.getStyleSheet(), state );
	}
	

	
	
	public DPElement presentLocationAsElement(Location location)
	{
		GSymSubject subject = getViewContext().getBrowserContext().resolveLocationAsSubject( location );
		GSymPerspective perspective = subject.getPerspective();
		return presentFragment( subject.getFocus(), perspective, perspective.getFragmentViewFunction(), subject.getSubjectContext(), perspective.getStyleSheet(), perspective.getInitialState() );
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
}
