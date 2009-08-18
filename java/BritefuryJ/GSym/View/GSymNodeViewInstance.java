//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.python.core.PyObject;

import BritefuryJ.Cell.CellInterface;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPButton;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.ElementKeyboardListener;
import BritefuryJ.DocPresent.ElementLinearRepresentationListener;
import BritefuryJ.DocPresent.PyElementFactory;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.GSym.View.ListView.ListViewLayout;
import BritefuryJ.GSym.View.ListView.PySeparatorElementFactory;
import BritefuryJ.GSym.View.ListView.SeparatorElementFactory;
import BritefuryJ.Parser.ItemStream.ItemStream;

public class GSymNodeViewInstance implements ElementContext, DVNode.NodeContext
{
	protected GSymViewInstance viewInstance;
	protected DVNode viewNode;
	
	
	public GSymNodeViewInstance(GSymViewInstance viewInstance, DVNode viewNode)
	{
		this.viewInstance = viewInstance;
		this.viewNode = viewNode;
		this.viewNode.setContext( this, this );
	}
	
	
	
	
	
	
	private void registerViewNodeRelationship(DVNode childNode)
	{
		viewNode.registerChild( childNode );
	}
	
	
	
	public DPWidget border(Border border, ElementStyleSheet styleSheet, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPBorder element = new DPBorder( styleSheet, border );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget indent(float indentation, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		Border border = viewInstance.indentationBorder( indentation );
		DPBorder element = new DPBorder( border );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget text(ElementStyleSheet styleSheet, String txt)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPText( styleSheet, txt );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget textWithContent(ElementStyleSheet styleSheet, String txt, String content)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPText( styleSheet, txt, content );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenStructuralObject(Object structuralRepresentation)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPEmpty();
		element.setStructuralValueObject( structuralRepresentation );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenStructuralSequence(List<Object> structuralRepresentation)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPEmpty();
		element.setStructuralValueSequence( structuralRepresentation );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget hiddenStructuralStream(ItemStream structuralRepresentation)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPEmpty();
		element.setStructuralValueStream( structuralRepresentation );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenText(String txt)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPEmpty( txt );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget whitespace(String txt, float width)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPWhitespace( txt, width );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}

	public DPWidget whitespace(String txt)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPWhitespace( txt, 0.0 );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	
	public DPWidget staticText(ElementStyleSheet styleSheet, String txt)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPStaticText( styleSheet, txt );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	

	public DPWidget link(ElementStyleSheet styleSheet, String txt, String targetLocation)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPLink( styleSheet, txt, targetLocation );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget link(ElementStyleSheet styleSheet, String txt, DPLink.LinkListener listener)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPLink( styleSheet, txt, listener );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget link(ElementStyleSheet styleSheet, String txt, PyObject listener)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPLink( styleSheet, txt, listener );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	

	
	public DPWidget button(ElementStyleSheet styleSheet, DPButton.ButtonListener listener, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPButton element = new DPButton( styleSheet, listener );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget button(ElementStyleSheet styleSheet, PyObject listener, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPButton element = new DPButton( styleSheet, listener );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	


	public DPWidget hbox(ElementStyleSheet styleSheet, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPHBox element = new DPHBox( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget ahbox(List<DPWidget> children)
	{
		return hbox( ahboxStyleSheet, children );
	}
	
	public DPWidget vbox(ElementStyleSheet styleSheet, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPVBox element = new DPVBox( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget paragraph(ElementStyleSheet styleSheet, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPParagraph element = new DPParagraph( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget span(List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPSpan element = new DPSpan();
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget lineBreak(ElementStyleSheet styleSheet, int lineBreakPriority, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPLineBreak element = new DPLineBreak( styleSheet, lineBreakPriority );
		if ( child != null )
		{
			element.setChild( child );
		}
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget lineBreak(int lineBreakPriority, DPWidget child)
	{
		return lineBreak( null, lineBreakPriority, child );
	}
	
	public DPWidget lineBreak(ElementStyleSheet styleSheet, int lineBreakPriority)
	{
		return lineBreak( styleSheet, lineBreakPriority, null );
	}
	
	public DPWidget lineBreak(int lineBreakPriority)
	{
		return lineBreak( null, lineBreakPriority, null );
	}
	
	public DPWidget segment(ElementStyleSheet textStyleSheet, boolean bGuardBegin, boolean bGuardEnd, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPSegment element = new DPSegment( textStyleSheet, bGuardBegin, bGuardEnd );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget segment(boolean bGuardBegin, boolean bGuardEnd, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPSegment element = new DPSegment( bGuardBegin, bGuardEnd );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget script(ElementStyleSheet styleSheet, DPWidget mainChild, DPWidget leftSuperChild, DPWidget leftSubChild, DPWidget rightSuperChild, DPWidget rightSubChild)
	{
		viewInstance.getView().profile_startElement();
		DPScript element = new DPScript( styleSheet );
		element.setMainChild( mainChild );
		if ( leftSuperChild != null )
		{
			element.setLeftSuperscriptChild( leftSuperChild );
		}
		if ( leftSubChild != null )
		{
			element.setLeftSubscriptChild( leftSubChild );
		}
		if ( rightSuperChild != null )
		{
			element.setRightSuperscriptChild( rightSuperChild );
		}
		if ( rightSubChild != null )
		{
			element.setRightSubscriptChild( rightSubChild );
		}
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget scriptLSuper(ElementStyleSheet styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, scriptChild, null, null, null );
	}
	
	public DPWidget scriptLSub(ElementStyleSheet styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, scriptChild, null, null );
	}
	
	public DPWidget scriptRSuper(ElementStyleSheet styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, null, scriptChild, null );
	}
	
	public DPWidget scriptRSub(ElementStyleSheet styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, null, null, scriptChild );
	}
	
	
	
	public DPWidget fraction(ElementStyleSheet styleSheet, DPWidget numerator, DPWidget denominator, String barContent)
	{
		viewInstance.getView().profile_startElement();
		DPFraction element = new DPFraction( styleSheet, barContent );
		element.setNumeratorChild( numerator );
		element.setDenominatorChild( denominator );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	

	public DPWidget listView(ListViewLayout layout, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = layout.createListElement( children, beginDelim, endDelim, separator );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget listView(ListViewLayout layout, PyObject beginDelim, PyObject endDelim, PyObject separator, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = layout.createListElement( children, PyElementFactory.pyToElementFactory( beginDelim ), PyElementFactory.pyToElementFactory( endDelim ), PySeparatorElementFactory.pyToSeparatorElementFactory( separator ) );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	
	public DPWidget linearRepresentationListener(DPWidget child, ElementLinearRepresentationListener listener)
	{
		viewInstance.getView().profile_startElement();
		child.setLinearRepresentationListener( listener );
		viewInstance.getView().profile_stopElement();
		return child;
	}
	
	public List<DPWidget> linearRepresentationListener(List<DPWidget> children, ElementLinearRepresentationListener listener)
	{
		viewInstance.getView().profile_startElement();
		for (DPWidget child: children)
		{
			child.setLinearRepresentationListener( listener );
		}
		viewInstance.getView().profile_stopElement();
		return children;
	}
	
	
	public DPWidget keyboardListener(DPWidget child, ElementKeyboardListener listener)
	{
		viewInstance.getView().profile_startElement();
		child.setKeyboardListener( listener );
		viewInstance.getView().profile_stopElement();
		return child;
	}
	
	public List<DPWidget> keyboardListener(List<DPWidget> children, ElementKeyboardListener listener)
	{
		viewInstance.getView().profile_startElement();
		for (DPWidget child: children)
		{
			child.setKeyboardListener( listener );
		}
		viewInstance.getView().profile_stopElement();
		return children;
	}
	
	
	public DPWidget viewEval(DocTreeNode x)
	{
		return viewEvalFn( x, (GSymNodeViewFunction)null, null );
	}

	public DPWidget viewEval(DocTreeNode x, Object state)
	{
		return viewEvalFn( x, (GSymNodeViewFunction)null, state );
	}

	public DPWidget viewEvalFn(DocTreeNode x, GSymNodeViewFunction nodeViewFunction)
	{
		return viewEvalFn( x, nodeViewFunction, null );
	}

	public DPWidget viewEvalFn(DocTreeNode x, GSymNodeViewFunction nodeViewFunction, Object state)
	{
		viewInstance.getView().profile_startJava();
		
		if ( x == null )
		{
			throw new RuntimeException( "GSymNodeViewInstance.viewEvanFn(): cannot build view of null node" );
		}
		
		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		DVNode viewNode = viewInstance.getView().buildNodeView( x, viewInstance.makeNodeElementFactory( nodeViewFunction, state ) );
		
		
		// Block access tracking to prevent the contents of this node being dependent upon the child node being refreshed,
		// and refresh the view node
		// Refreshing the child node will ensure that when its contents are inserted into outer elements, its full element tree
		// is up to date and available.
		// Blocking the access tracking prevents an inner node from causing all parent/grandparent/etc nodes from requiring a
		// refresh.
		WeakHashMap<CellInterface, Object> accessList = CellInterface.blockAccessTracking();
		viewNode.refresh();
		CellInterface.unblockAccessTracking( accessList );
		
		registerViewNodeRelationship( viewNode );
		
		viewInstance.getView().profile_stopJava();
		return viewNode.getElementNoRefresh();
	}
	
	public DPWidget viewEvalFn(DocTreeNode x, PyObject nodeViewFunction)
	{
		return viewEvalFn( x, new PyGSymNodeViewFunction( nodeViewFunction ), null );
	}

	public DPWidget viewEvalFn(DocTreeNode x, PyObject nodeViewFunction, Object state)
	{
		return viewEvalFn( x, new PyGSymNodeViewFunction( nodeViewFunction ), state );
	}
	
	
	
	
	public List<DPWidget> mapViewEval(List<DocTreeNode> xs)
	{
		return mapViewEvalFn( xs, (GSymNodeViewFunction)null, null );
	}

	public List<DPWidget> mapViewEval(List<DocTreeNode> xs, Object state)
	{
		return mapViewEvalFn( xs, (GSymNodeViewFunction)null, state );
	}

	public List<DPWidget> mapViewEvalFn(List<DocTreeNode> xs, GSymNodeViewFunction nodeViewFunction)
	{
		return mapViewEvalFn( xs, nodeViewFunction, null );
	}

	public List<DPWidget> mapViewEvalFn(List<DocTreeNode> xs, GSymNodeViewFunction nodeViewFunction, Object state)
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		children.ensureCapacity( xs.size() );
		for (DocTreeNode x: xs)
		{
			children.add( viewEvalFn( x, nodeViewFunction, state ) );
		}
		return children;
	}
	
	public List<DPWidget> mapViewEvalFn(List<DocTreeNode> xs, PyObject nodeViewFunction)
	{
		return mapViewEvalFn( xs, new PyGSymNodeViewFunction( nodeViewFunction ), null );
	}

	public List<DPWidget> mapViewEvalFn(List<DocTreeNode> xs, PyObject nodeViewFunction, Object state)
	{
		return mapViewEvalFn( xs, new PyGSymNodeViewFunction( nodeViewFunction ), state );
	}
	
	
	
	public DocTreeNode getTreeNode()
	{
		return viewNode.getTreeNode();
	}
	
	public Object getDocNode()
	{
		return viewNode.getDocNode();
	}
	
	
	
	public DPWidget getViewNodeElement()
	{
		return viewNode.getElementNoRefresh();
	}
	
	public DPWidget getViewNodeContentElement()
	{
		return viewNode.getInnerElementNoRefresh();
	}
	
	
	
	private GSymNodeViewInstance getPreviousSiblingFromChildElement(GSymNodeViewInstance parent, DPWidget fromChild)
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
		
		List<DPWidget> children = parentElement.getChildren();
		int index = children.indexOf( fromChild );
		for (int i = index - 1; i >= 0; i--)
		{
			GSymNodeViewInstance sibling = getLastChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymNodeViewInstance getLastChildFromParentElement(GSymNodeViewInstance parent, DPWidget element)
	{
		if ( element.getContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewInstance)element.getContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			List<DPWidget> children = branch.getChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				GSymNodeViewInstance sibling = getLastChildFromParentElement( parent, children.get( i ) );
				if ( sibling != null )
				{
					return sibling;
				}
			}
		}
		return null;
	}
	

	
	private GSymNodeViewInstance getNextSiblingFromChildElement(GSymNodeViewInstance parent, DPWidget fromChild)
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
		
		List<DPWidget> children = parentElement.getChildren();
		int index = children.indexOf( fromChild );
		for (int i = index + 1; i < children.size(); i++)
		{
			GSymNodeViewInstance sibling = getFirstChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymNodeViewInstance getFirstChildFromParentElement(GSymNodeViewInstance parent, DPWidget element)
	{
		if ( element.getContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewInstance)element.getContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			for (DPWidget child: branch.getChildren())
			{
				GSymNodeViewInstance sibling = getFirstChildFromParentElement( parent, child );
				if ( sibling != null )
				{
					return sibling;
				}
			}
		}
		return null;
	}
	

	
	public GSymNodeViewInstance getParent()
	{
		DVNode parentViewNode = viewNode.getParent();
		return parentViewNode != null  ?  (GSymNodeViewInstance)parentViewNode.getContext()  :  null;
	}
	

	public GSymNodeViewInstance getPrevSibling()
	{
		return getPreviousSiblingFromChildElement( getParent(), getViewNodeElement() );
	}
	
	public GSymNodeViewInstance getNextSibling()
	{
		return getNextSiblingFromChildElement( this, getViewNodeElement() );
	}
	
	
	
	public GSymNodeViewInstance getFirstChild()
	{
		return getFirstChildFromParentElement( getParent(), getViewNodeElement() );
	}
	
	public GSymNodeViewInstance getLastChild()
	{
		return getNextSiblingFromChildElement( this, getViewNodeElement() );
	}
	
	
	
	public ArrayList<GSymNodeViewInstance> getNodeViewInstancePathFromRoot()
	{
		ArrayList<GSymNodeViewInstance> path = new ArrayList<GSymNodeViewInstance>();
		
		GSymNodeViewInstance n = this;
		while ( n != null )
		{
			path.add( 0, n );
			n = n.getParent();
		}
		
		return path;
	}
	
	public ArrayList<GSymNodeViewInstance> getNodeViewInstancePathFromSubtreeRoot(GSymNodeViewInstance root)
	{
		ArrayList<GSymNodeViewInstance> path = new ArrayList<GSymNodeViewInstance>();
		
		GSymNodeViewInstance n = this;
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
	
	
	public GSymViewInstance getViewContext()
	{
		return viewInstance;
	}
	

	
	private static ElementStyleSheet ahboxStyleSheet = DPHBox.styleSheet( VAlignment.BASELINES, 0.0, false, 0.0 ); 
}
