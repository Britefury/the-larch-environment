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
import BritefuryJ.DocPresent.ElementTree.BorderElement;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementContentListener;
import BritefuryJ.DocPresent.ElementTree.ElementFactory;
import BritefuryJ.DocPresent.ElementTree.FractionElement;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.ElementTree.HiddenContentElement;
import BritefuryJ.DocPresent.ElementTree.LineBreakElement;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.ScriptElement;
import BritefuryJ.DocPresent.ElementTree.SegmentElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.ElementTree.WhitespaceElement;
import BritefuryJ.DocPresent.ElementTree.SegmentElement.CaretStopElementFactory;
import BritefuryJ.DocPresent.StyleSheets.BorderStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.GSym.View.ListView.ListViewLayout;

public class GSymNodeViewInstance
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
	
	
	private static class PyElementFactory implements ElementFactory
	{
		private PyObject callable;
		
		public PyElementFactory(PyObject callable)
		{
			this.callable = callable;
		}
		
		
		public Element createElement()
		{
			return (Element)Py.tojava( callable.__call__(), Element.class );
		}
	}
	
	
	protected Object xs;
	protected DocView view;
	protected GSymViewInstance viewInstance;
	protected DVNode viewNode;
	
	
	public GSymNodeViewInstance(Object xs, DocView view, GSymViewInstance viewInstance, DVNode viewNode)
	{
		this.xs = xs;
		this.view = view;
		this.viewInstance = viewInstance;
		this.viewNode = viewNode;
	}
	
	
	
	
	
	
	private void registerViewNodeRelationship(DVNode childNode)
	{
		viewNode.registerChild( childNode );
	}
	
	
	
	public Element border(BorderStyleSheet styleSheet, Element child)
	{
		BorderElement element = new BorderElement( styleSheet );
		element.setChild( child );
		return element;
	}
	
	public Element indent(float indentation, Element child)
	{
		BorderStyleSheet styleSheet = viewInstance.indentationStyleSheet( indentation );
		BorderElement element = new BorderElement( styleSheet );
		element.setChild( child );
		return element;
	}
	
	public Element text(TextStyleSheet styleSheet, String txt)
	{
		return new TextElement( styleSheet, txt );
	}
	
	public Element hiddenText(String txt)
	{
		return new HiddenContentElement( txt );
	}
	
	public Element whitespace(String txt, float width)
	{
		return new WhitespaceElement( txt, width );
	}

	public Element whitespace(String txt)
	{
		return new WhitespaceElement( txt, 0.0 );
	}
	
	
	
	public Element hbox(HBoxStyleSheet styleSheet, List<Element> children)
	{
		HBoxElement element = new HBoxElement( styleSheet );
		element.setChildren( children );
		return element;
	}
	
	public Element ahbox(List<Element> children)
	{
		return hbox( ahboxStyleSheet, children );
	}
	
	public Element vbox(VBoxStyleSheet styleSheet, List<Element> children)
	{
		VBoxElement element = new VBoxElement( styleSheet );
		element.setChildren( children );
		return element;
	}
	
	public Element paragraph(ParagraphStyleSheet styleSheet, List<Element> children)
	{
		ParagraphElement element = new ParagraphElement( styleSheet );
		element.setChildren( children );
		return element;
	}
	
	public Element lineBreak(ContainerStyleSheet styleSheet, Element child)
	{
		LineBreakElement element = new LineBreakElement( styleSheet );
		element.setChild( child );
		return element;
	}
	
	public Element segment(ParagraphStyleSheet styleSheet, CaretStopElementFactory stopFactory, Element child)
	{
		SegmentElement element = new SegmentElement( styleSheet, stopFactory );
		element.setChild( child );
		return element;
	}
	
	public Element script(ScriptStyleSheet styleSheet, Element mainChild, Element leftSuperChild, Element leftSubChild, Element rightSuperChild, Element rightSubChild)
	{
		ScriptElement element = new ScriptElement( styleSheet );
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
		return element;
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
	
	
	
	public Element fraction(FractionStyleSheet styleSheet, Element numerator, Element denominator)
	{
		FractionElement element = new FractionElement( styleSheet );
		element.setNumeratorChild( numerator );
		element.setDenominatorChild( denominator );
		return element;
	}
	

	public Element listView(ListViewLayout layout, ElementFactory beginDelim, ElementFactory endDelim, ElementFactory separator, List<Element> children)
	{
		return layout.createListElement( children, beginDelim, endDelim, separator );
	}
	
	public Element listView(ListViewLayout layout, PyObject beginDelim, PyObject endDelim, PyObject separator, List<Element> children)
	{
		return layout.createListElement( children, new PyElementFactory( beginDelim ), new PyElementFactory( endDelim ), new PyElementFactory( separator ) );
	}
	
	
	
	public Element contentListener(Element child, ElementContentListener listener)
	{
		child.setContentListener( listener );
		return child;
	}
	
	public List<Element> contentListener(List<Element> children, ElementContentListener listener)
	{
		for (Element child: children)
		{
			child.setContentListener( listener );
		}
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
		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		DVNode viewNode = view.buildNodeView( x );
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
	
	
	
	
	
	
	private static HBoxStyleSheet ahboxStyleSheet = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 ); 
}
