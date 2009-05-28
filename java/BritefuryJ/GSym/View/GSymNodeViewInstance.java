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

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.Cell.CellInterface;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.ElementTree.BorderElement;
import BritefuryJ.DocPresent.ElementTree.BranchElement;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementFactory;
import BritefuryJ.DocPresent.ElementTree.ElementKeyboardListener;
import BritefuryJ.DocPresent.ElementTree.ElementTextRepresentationListener;
import BritefuryJ.DocPresent.ElementTree.FractionElement;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.ElementTree.HiddenContentElement;
import BritefuryJ.DocPresent.ElementTree.LineBreakElement;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.PyElementFactory;
import BritefuryJ.DocPresent.ElementTree.ScriptElement;
import BritefuryJ.DocPresent.ElementTree.SegmentElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.ElementTree.WhitespaceElement;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.GSym.View.ListView.ListViewLayout;
import BritefuryJ.GSym.View.ListView.PySeparatorElementFactory;
import BritefuryJ.GSym.View.ListView.SeparatorElementFactory;

public class GSymNodeViewInstance implements Element.ElementContext, DVNode.NodeContext
{
	protected static class PyGSymNodeViewFunction implements GSymNodeViewFunction
	{
		private PyObject callable;
		
		
		public PyGSymNodeViewFunction(PyObject callable)
		{
			this.callable = callable;
		}

	
		public Element createElement(DocTreeNode x, GSymNodeViewInstance ctx, Object state)
		{
			return (Element)Py.tojava( callable.__call__( Py.java2py( x ), Py.java2py( ctx ), Py.java2py( state ) ), Element.class );
		}
	}

	
	
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
	
	
	
	public Element border(Border border, ContainerStyleSheet styleSheet, Element child)
	{
		viewInstance.getView().profile_startElement();
		BorderElement element = new BorderElement( border, styleSheet );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element indent(float indentation, Element child)
	{
		viewInstance.getView().profile_startElement();
		Border border = viewInstance.indentationBorder( indentation );
		BorderElement element = new BorderElement( border );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element text(TextStyleSheet styleSheet, String txt)
	{
		viewInstance.getView().profile_startElement();
		Element element = new TextElement( styleSheet, txt );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element textWithContent(TextStyleSheet styleSheet, String txt, String content)
	{
		viewInstance.getView().profile_startElement();
		Element element = new TextElement( styleSheet, txt, content );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element hiddenText(String txt)
	{
		viewInstance.getView().profile_startElement();
		Element element = new HiddenContentElement( txt );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element whitespace(String txt, float width)
	{
		viewInstance.getView().profile_startElement();
		Element element = new WhitespaceElement( txt, width );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}

	public Element whitespace(String txt)
	{
		viewInstance.getView().profile_startElement();
		Element element = new WhitespaceElement( txt, 0.0 );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	
	public Element hbox(HBoxStyleSheet styleSheet, List<Element> children)
	{
		viewInstance.getView().profile_startElement();
		HBoxElement element = new HBoxElement( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element ahbox(List<Element> children)
	{
		return hbox( ahboxStyleSheet, children );
	}
	
	public Element vbox(VBoxStyleSheet styleSheet, List<Element> children)
	{
		viewInstance.getView().profile_startElement();
		VBoxElement element = new VBoxElement( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element paragraph(ParagraphStyleSheet styleSheet, List<Element> children)
	{
		viewInstance.getView().profile_startElement();
		ParagraphElement element = new ParagraphElement( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element lineBreak(ContainerStyleSheet styleSheet, int lineBreakPriority, Element child)
	{
		viewInstance.getView().profile_startElement();
		LineBreakElement element = new LineBreakElement( styleSheet, lineBreakPriority );
		if ( child != null )
		{
			element.setChild( child );
		}
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element lineBreak(int lineBreakPriority, Element child)
	{
		return lineBreak( ContainerStyleSheet.defaultStyleSheet, lineBreakPriority, child );
	}
	
	public Element lineBreak(ContainerStyleSheet styleSheet, int lineBreakPriority)
	{
		return lineBreak( styleSheet, lineBreakPriority, null );
	}
	
	public Element lineBreak(int lineBreakPriority)
	{
		return lineBreak( ContainerStyleSheet.defaultStyleSheet, lineBreakPriority, null );
	}
	
	public Element segment(ParagraphStyleSheet styleSheet, TextStyleSheet textStyleSheet, boolean bGuardBegin, boolean bGuardEnd, Element child)
	{
		viewInstance.getView().profile_startElement();
		SegmentElement element = new SegmentElement( styleSheet, textStyleSheet, bGuardBegin, bGuardEnd );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element segment(boolean bGuardBegin, boolean bGuardEnd, Element child)
	{
		viewInstance.getView().profile_startElement();
		SegmentElement element = new SegmentElement( ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, bGuardBegin, bGuardEnd );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	public Element script(ScriptStyleSheet styleSheet, ParagraphStyleSheet segmentParagraphStyleSheet, TextStyleSheet segmentTextStyleSheet, Element mainChild, Element leftSuperChild, Element leftSubChild, Element rightSuperChild, Element rightSubChild)
	{
		viewInstance.getView().profile_startElement();
		ScriptElement element = new ScriptElement( styleSheet, segmentParagraphStyleSheet, segmentTextStyleSheet );
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
	
	public Element script(ScriptStyleSheet styleSheet, Element mainChild, Element leftSuperChild, Element leftSubChild, Element rightSuperChild, Element rightSubChild)
	{
		return script( styleSheet, ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild );
	}
	
	
	public Element scriptLSuper(ScriptStyleSheet styleSheet, ParagraphStyleSheet segmentParagraphStyleSheet, TextStyleSheet segmentTextStyleSheet, Element mainChild, Element scriptChild)
	{
		return script( styleSheet, segmentParagraphStyleSheet, segmentTextStyleSheet, mainChild, scriptChild, null, null, null );
	}
	
	public Element scriptLSub(ScriptStyleSheet styleSheet, ParagraphStyleSheet segmentParagraphStyleSheet, TextStyleSheet segmentTextStyleSheet, Element mainChild, Element scriptChild)
	{
		return script( styleSheet, segmentParagraphStyleSheet, segmentTextStyleSheet, mainChild, null, scriptChild, null, null );
	}
	
	public Element scriptRSuper(ScriptStyleSheet styleSheet, ParagraphStyleSheet segmentParagraphStyleSheet, TextStyleSheet segmentTextStyleSheet, Element mainChild, Element scriptChild)
	{
		return script( styleSheet, segmentParagraphStyleSheet, segmentTextStyleSheet, mainChild, null, null, scriptChild, null );
	}
	
	public Element scriptRSub(ScriptStyleSheet styleSheet, ParagraphStyleSheet segmentParagraphStyleSheet, TextStyleSheet segmentTextStyleSheet, Element mainChild, Element scriptChild)
	{
		return script( styleSheet, segmentParagraphStyleSheet, segmentTextStyleSheet, mainChild, null, null, null, scriptChild );
	}
	
	
	public Element scriptLSuper(ScriptStyleSheet styleSheet, Element mainChild, Element scriptChild)
	{
		return script( styleSheet, mainChild, scriptChild, null, null, null );
	}
	
	public Element scriptLSub(ScriptStyleSheet styleSheet, Element mainChild, Element scriptChild)
	{
		return script( styleSheet, mainChild, null, scriptChild, null, null );
	}
	
	public Element scriptRSuper(ScriptStyleSheet styleSheet, Element mainChild, Element scriptChild)
	{
		return script( styleSheet, mainChild, null, null, scriptChild, null );
	}
	
	public Element scriptRSub(ScriptStyleSheet styleSheet, Element mainChild, Element scriptChild)
	{
		return script( styleSheet, mainChild, null, null, null, scriptChild );
	}
	
	
	
	public Element fraction(FractionStyleSheet styleSheet, ParagraphStyleSheet segmentParagraphStyleSheet, TextStyleSheet segmentTextStyleSheet, Element numerator, Element denominator, String barContent)
	{
		viewInstance.getView().profile_startElement();
		FractionElement element = new FractionElement( styleSheet, segmentParagraphStyleSheet, segmentTextStyleSheet, barContent );
		element.setNumeratorChild( numerator );
		element.setDenominatorChild( denominator );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element fraction(FractionStyleSheet styleSheet, Element numerator, Element denominator, String barContent)
	{
		return fraction( styleSheet, ParagraphStyleSheet.defaultStyleSheet, TextStyleSheet.defaultStyleSheet, numerator, denominator, barContent );
	}
	

	public Element listView(ListViewLayout layout, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator, List<Element> children)
	{
		viewInstance.getView().profile_startElement();
		Element element = layout.createListElement( children, beginDelim, endDelim, separator );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public Element listView(ListViewLayout layout, PyObject beginDelim, PyObject endDelim, PyObject separator, List<Element> children)
	{
		viewInstance.getView().profile_startElement();
		Element element = layout.createListElement( children, PyElementFactory.pyToElementFactory( beginDelim ), PyElementFactory.pyToElementFactory( endDelim ), PySeparatorElementFactory.pyToSeparatorElementFactory( separator ) );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	
	public Element textRepresentationListener(Element child, ElementTextRepresentationListener listener)
	{
		viewInstance.getView().profile_startElement();
		child.setTextRepresentationListener( listener );
		viewInstance.getView().profile_stopElement();
		return child;
	}
	
	public List<Element> textRepresentationListener(List<Element> children, ElementTextRepresentationListener listener)
	{
		viewInstance.getView().profile_startElement();
		for (Element child: children)
		{
			child.setTextRepresentationListener( listener );
		}
		viewInstance.getView().profile_stopElement();
		return children;
	}
	
	
	public Element keyboardListener(Element child, ElementKeyboardListener listener)
	{
		viewInstance.getView().profile_startElement();
		child.setKeyboardListener( listener );
		viewInstance.getView().profile_stopElement();
		return child;
	}
	
	public List<Element> keyboardListener(List<Element> children, ElementKeyboardListener listener)
	{
		viewInstance.getView().profile_startElement();
		for (Element child: children)
		{
			child.setKeyboardListener( listener );
		}
		viewInstance.getView().profile_stopElement();
		return children;
	}
	
	
	public Element viewEval(DocTreeNode x)
	{
		return viewEvalFn( x, (GSymNodeViewFunction)null, null );
	}

	public Element viewEval(DocTreeNode x, Object state)
	{
		return viewEvalFn( x, (GSymNodeViewFunction)null, state );
	}

	public Element viewEvalFn(DocTreeNode x, GSymNodeViewFunction nodeViewFunction)
	{
		return viewEvalFn( x, nodeViewFunction, null );
	}

	public Element viewEvalFn(DocTreeNode x, GSymNodeViewFunction nodeViewFunction, Object state)
	{
		viewInstance.getView().profile_startJava();
		
		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		DVNode viewNode = viewInstance.getView().buildNodeView( x );
		viewNode.setNodeElementFactory( viewInstance.makeNodeElementFactory( nodeViewFunction, state ) );
		
		
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
	
	public Element viewEvalFn(DocTreeNode x, PyObject nodeViewFunction)
	{
		return viewEvalFn( x, new PyGSymNodeViewFunction( nodeViewFunction ), null );
	}

	public Element viewEvalFn(DocTreeNode x, PyObject nodeViewFunction, Object state)
	{
		return viewEvalFn( x, new PyGSymNodeViewFunction( nodeViewFunction ), state );
	}
	
	
	
	
	public List<Element> mapViewEval(List<DocTreeNode> xs)
	{
		return mapViewEvalFn( xs, (GSymNodeViewFunction)null, null );
	}

	public List<Element> mapViewEval(List<DocTreeNode> xs, Object state)
	{
		return mapViewEvalFn( xs, (GSymNodeViewFunction)null, state );
	}

	public List<Element> mapViewEvalFn(List<DocTreeNode> xs, GSymNodeViewFunction nodeViewFunction)
	{
		return mapViewEvalFn( xs, nodeViewFunction, null );
	}

	public List<Element> mapViewEvalFn(List<DocTreeNode> xs, GSymNodeViewFunction nodeViewFunction, Object state)
	{
		ArrayList<Element> children = new ArrayList<Element>();
		children.ensureCapacity( xs.size() );
		for (DocTreeNode x: xs)
		{
			children.add( viewEvalFn( x, nodeViewFunction, state ) );
		}
		return children;
	}
	
	public List<Element> mapViewEvalFn(List<DocTreeNode> xs, PyObject nodeViewFunction)
	{
		return mapViewEvalFn( xs, new PyGSymNodeViewFunction( nodeViewFunction ), null );
	}

	public List<Element> mapViewEvalFn(List<DocTreeNode> xs, PyObject nodeViewFunction, Object state)
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
	
	
	
	public Element getViewNodeElement()
	{
		return viewNode.getElementNoRefresh();
	}
	
	public Element getViewNodeContentElement()
	{
		return viewNode.getInnerElementNoRefresh();
	}
	
	
	
	private GSymNodeViewInstance getPreviousSiblingFromChildElement(GSymNodeViewInstance parent, Element fromChild)
	{
		if ( fromChild == null )
		{
			return null;
		}
		BranchElement parentElement = (BranchElement)fromChild.getParent();
		if ( parentElement == parent.getViewNodeElement() )
		{
			return null;
		}
		
		List<Element> children = parentElement.getChildren();
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
	
	private GSymNodeViewInstance getLastChildFromParentElement(GSymNodeViewInstance parent, Element element)
	{
		if ( element.getContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewInstance)element.getContext();
		}
		else if ( element instanceof BranchElement )
		{
			BranchElement branch = (BranchElement)element;
			List<Element> children = branch.getChildren();
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
	

	
	private GSymNodeViewInstance getNextSiblingFromChildElement(GSymNodeViewInstance parent, Element fromChild)
	{
		if ( fromChild == null )
		{
			return null;
		}
		BranchElement parentElement = (BranchElement)fromChild.getParent();
		if ( parentElement == parent.getViewNodeElement() )
		{
			return null;
		}
		
		List<Element> children = parentElement.getChildren();
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
	
	private GSymNodeViewInstance getFirstChildFromParentElement(GSymNodeViewInstance parent, Element element)
	{
		if ( element.getContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewInstance)element.getContext();
		}
		else if ( element instanceof BranchElement )
		{
			BranchElement branch = (BranchElement)element;
			for (Element child: branch.getChildren())
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
	

	
	private static HBoxStyleSheet ahboxStyleSheet = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 ); 
}
