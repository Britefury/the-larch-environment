//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.StyleParams.*;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPButton;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPGridRow;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPLine;
import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPTable;
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
import BritefuryJ.DocPresent.StyleParams.ButtonStyleParams;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeContext;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeFunction;
import BritefuryJ.GSym.IncrementalContext.PyGSymIncrementalNodeFunction;
import BritefuryJ.GSym.View.ListView.ListViewLayout;
import BritefuryJ.GSym.View.ListView.PySeparatorElementFactory;
import BritefuryJ.GSym.View.ListView.SeparatorElementFactory;
import BritefuryJ.Parser.ItemStream.ItemStream;

public class GSymNodeViewContext extends GSymIncrementalNodeContext implements ElementContext
{
	public GSymNodeViewContext(GSymViewContext viewContext, DVNode viewNode)
	{
		super( viewContext, viewNode );
		viewNode.setElementContext( this );
	}
	
	
	
	public GSymViewContext getViewContext()
	{
		return (GSymViewContext)treeContext;
	}
	
	
	
	
	
	public DPWidget border(Border border, ContainerStyleParams styleParams, DPWidget child)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPBorder element = new DPBorder( border, styleParams);
		element.setChild( child );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget border(Border border, DPWidget child)
	{
		return border( border, ContainerStyleParams.defaultStyleParams, child );
	}
	
	public DPWidget indent(double indentation, DPWidget child)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		Border border = viewContext.indentationBorder( indentation );
		DPBorder element = new DPBorder( border );
		element.setChild( child );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget text(TextStyleParams styleParams, String txt)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPText(styleParams, txt );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget textWithContent(TextStyleParams styleParams, String txt, String content)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPText(styleParams, txt, content );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenStructuralObject(Object structuralRepresentation)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPEmpty( );
		element.setStructuralValueObject( structuralRepresentation );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenStructuralSequence(List<Object> structuralRepresentation)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPEmpty( );
		element.setStructuralValueSequence( structuralRepresentation );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget hiddenStructuralStream(ItemStream structuralRepresentation)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPEmpty( );
		element.setStructuralValueStream( structuralRepresentation );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenText(String txt)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPEmpty( txt );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget whitespace(String txt, float width)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPWhitespace( txt, width );
		viewContext.getView().profile_stopElement();
		return element;
	}

	public DPWidget whitespace(String txt)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPWhitespace( txt, 0.0 );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	
	
	public DPWidget staticText(StaticTextStyleParams styleParams, String txt)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPStaticText(styleParams, txt );
		viewContext.getView().profile_stopElement();
		return element;
	}
	

	public DPWidget link(LinkStyleParams styleParams, String txt, String targetLocation)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPLink(styleParams, txt, targetLocation );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget link(LinkStyleParams styleParams, String txt, DPLink.LinkListener listener)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPLink(styleParams, txt, listener );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget link(LinkStyleParams styleParams, String txt, PyObject listener)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPLink(styleParams, txt, listener );
		viewContext.getView().profile_stopElement();
		return element;
	}
	

	
	public DPWidget line(LineStyleParams styleParams)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = new DPLine(styleParams);
		viewContext.getView().profile_stopElement();
		return element;
	}
	

	
	public DPWidget button(ButtonStyleParams styleParams, DPButton.ButtonListener listener, DPWidget child)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPButton element = new DPButton(styleParams, listener );
		element.setChild( child );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget button(ButtonStyleParams styleParams, PyObject listener, DPWidget child)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPButton element = new DPButton(styleParams, listener );
		element.setChild( child );
		viewContext.getView().profile_stopElement();
		return element;
	}
	


	public DPWidget span(List<DPWidget> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPSpan element = new DPSpan( );
		element.setChildren( children );
		viewContext.getView().profile_stopElement();
		return element;
	}
	

	
	public DPWidget hbox(HBoxStyleParams styleParams, List<DPWidget> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPHBox element = new DPHBox(styleParams);
		element.setChildren( children );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget ahbox(List<DPWidget> children)
	{
		return hbox(ahboxStyleParams, children );
	}
	
	public DPWidget vbox(VBoxStyleParams styleParams, List<DPWidget> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPVBox element = new DPVBox(styleParams);
		element.setChildren( children );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget paragraph(ParagraphStyleParams styleParams, List<DPWidget> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPParagraph element = new DPParagraph(styleParams);
		element.setChildren( children );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget lineBreak(ContainerStyleParams styleParams, DPWidget child)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPLineBreak element = new DPLineBreak(styleParams);
		if ( child != null )
		{
			element.setChild( child );
		}
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget lineBreak(DPWidget child)
	{
		return lineBreak( ContainerStyleParams.defaultStyleParams, child );
	}
	
	public DPWidget lineBreak(ContainerStyleParams styleParams)
	{
		return lineBreak(styleParams, null );
	}
	
	public DPWidget lineBreak()
	{
		return lineBreak( ContainerStyleParams.defaultStyleParams );
	}
	
	public DPWidget paragraphIndentMarker()
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPParagraphIndentMarker element = new DPParagraphIndentMarker( );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget paragraphDedentMarker()
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPParagraphDedentMarker element = new DPParagraphDedentMarker( );
		viewContext.getView().profile_stopElement();
		return element;
	}
	

	public DPWidget gridRow(GridRowStyleParams styleParams, List<DPWidget> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPGridRow element = new DPGridRow(styleParams);
		element.setChildren( children );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget rgrid(TableStyleParams styleParams, List<DPWidget> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPRGrid element = new DPRGrid(styleParams);
		element.setChildren( children );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget table(TableStyleParams styleParams, List<List<DPWidget>> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPTable element = new DPTable(styleParams);
		element.setChildren( children );
		viewContext.getView().profile_stopElement();
		return element;
	}
	

	public DPWidget segment(TextStyleParams textStyleParams, boolean bGuardBegin, boolean bGuardEnd, DPWidget child)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPSegment element = new DPSegment(textStyleParams, bGuardBegin, bGuardEnd );
		element.setChild( child );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget segment(boolean bGuardBegin, boolean bGuardEnd, DPWidget child)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPSegment element = new DPSegment( TextStyleParams.defaultStyleParams, bGuardBegin, bGuardEnd );
		element.setChild( child );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget script(ScriptStyleParams styleSheet, TextStyleParams segmentTextStyleParams, DPWidget mainChild, DPWidget leftSuperChild, DPWidget leftSubChild, DPWidget rightSuperChild, DPWidget rightSubChild)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPScript element = new DPScript( styleSheet, segmentTextStyleParams);
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
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget script(ScriptStyleParams styleSheet, DPWidget mainChild, DPWidget leftSuperChild, DPWidget leftSubChild, DPWidget rightSuperChild, DPWidget rightSubChild)
	{
		return script( styleSheet, TextStyleParams.defaultStyleParams, mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild );
	}
	
	
	public DPWidget scriptLSuper(ScriptStyleParams styleSheet, TextStyleParams segmentTextStyleParams, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, segmentTextStyleParams, mainChild, scriptChild, null, null, null );
	}
	
	public DPWidget scriptLSub(ScriptStyleParams styleSheet, TextStyleParams segmentTextStyleParams, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, segmentTextStyleParams, mainChild, null, scriptChild, null, null );
	}
	
	public DPWidget scriptRSuper(ScriptStyleParams styleSheet, TextStyleParams segmentTextStyleParams, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, segmentTextStyleParams, mainChild, null, null, scriptChild, null );
	}
	
	public DPWidget scriptRSub(ScriptStyleParams styleSheet, TextStyleParams segmentTextStyleParams, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, segmentTextStyleParams, mainChild, null, null, null, scriptChild );
	}
	
	
	public DPWidget scriptLSuper(ScriptStyleParams styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, scriptChild, null, null, null );
	}
	
	public DPWidget scriptLSub(ScriptStyleParams styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, scriptChild, null, null );
	}
	
	public DPWidget scriptRSuper(ScriptStyleParams styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, null, scriptChild, null );
	}
	
	public DPWidget scriptRSub(ScriptStyleParams styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, null, null, scriptChild );
	}
	
	
	
	public DPWidget fraction(FractionStyleParams styleParams, TextStyleParams segmentTextStyleParams, DPWidget numerator, DPWidget denominator, String barContent)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPFraction element = new DPFraction(styleParams, segmentTextStyleParams, barContent );
		element.setNumeratorChild( numerator );
		element.setDenominatorChild( denominator );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget fraction(FractionStyleParams styleParams, DPWidget numerator, DPWidget denominator, String barContent)
	{
		return fraction(styleParams, TextStyleParams.defaultStyleParams, numerator, denominator, barContent );
	}
	

	public DPWidget listView(ListViewLayout layout, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator, List<DPWidget> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = layout.createListElement( this, children, beginDelim, endDelim, separator );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget listView(ListViewLayout layout, PyObject beginDelim, PyObject endDelim, PyObject separator, List<DPWidget> children)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		DPWidget element = layout.createListElement( this, children, PyElementFactory.pyToElementFactory( beginDelim ), PyElementFactory.pyToElementFactory( endDelim ), PySeparatorElementFactory.pyToSeparatorElementFactory( separator ) );
		viewContext.getView().profile_stopElement();
		return element;
	}
	
	
	
	public DPWidget linearRepresentationListener(DPWidget child, ElementLinearRepresentationListener listener)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		child.setLinearRepresentationListener( listener );
		viewContext.getView().profile_stopElement();
		return child;
	}
	
	public List<DPWidget> linearRepresentationListener(List<DPWidget> children, ElementLinearRepresentationListener listener)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		for (DPWidget child: children)
		{
			child.setLinearRepresentationListener( listener );
		}
		viewContext.getView().profile_stopElement();
		return children;
	}
	
	
	public DPWidget keyboardListener(DPWidget child, ElementKeyboardListener listener)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		child.setKeyboardListener( listener );
		viewContext.getView().profile_stopElement();
		return child;
	}
	
	public List<DPWidget> keyboardListener(List<DPWidget> children, ElementKeyboardListener listener)
	{
		GSymViewContext viewContext = getViewContext();
		viewContext.getView().profile_startElement();
		for (DPWidget child: children)
		{
			child.setKeyboardListener( listener );
		}
		viewContext.getView().profile_stopElement();
		return children;
	}
	
	
	public DPWidget viewEval(DMNode x)
	{
		return viewEvalFn( x, (GSymIncrementalNodeFunction)null, null );
	}

	public DPWidget viewEval(DMNode x, Object state)
	{
		return viewEvalFn( x, (GSymIncrementalNodeFunction)null, state );
	}

	public DPWidget viewEvalFn(DMNode x, GSymIncrementalNodeFunction nodeViewFunction)
	{
		return viewEvalFn( x, nodeViewFunction, null );
	}

	public DPWidget viewEvalFn(DMNode x, GSymIncrementalNodeFunction nodeViewFunction, Object state)
	{
		return (DPWidget)evalFn( x, nodeViewFunction, state );
	}
	
	public DPWidget viewEvalFn(DMNode x, PyObject nodeViewFunction)
	{
		return viewEvalFn( x, new PyGSymIncrementalNodeFunction( nodeViewFunction ), null );
	}

	public DPWidget viewEvalFn(DMNode x, PyObject nodeViewFunction, Object state)
	{
		return viewEvalFn( x, new PyGSymIncrementalNodeFunction( nodeViewFunction ), state );
	}
	
	
	
	
	public List<DPWidget> mapViewEval(List<DMNode> xs)
	{
		return mapViewEvalFn( xs, (GSymIncrementalNodeFunction)null, null );
	}

	public List<DPWidget> mapViewEval(List<DMNode> xs, Object state)
	{
		return mapViewEvalFn( xs, (GSymIncrementalNodeFunction)null, state );
	}

	public List<DPWidget> mapViewEvalFn(List<DMNode> xs, GSymIncrementalNodeFunction nodeViewFunction)
	{
		return mapViewEvalFn( xs, nodeViewFunction, null );
	}

	public List<DPWidget> mapViewEvalFn(List<DMNode> xs, GSymIncrementalNodeFunction nodeViewFunction, Object state)
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		children.ensureCapacity( xs.size() );
		for (DMNode x: xs)
		{
			children.add( viewEvalFn( x, nodeViewFunction, state ) );
		}
		return children;
	}
	
	public List<DPWidget> mapViewEvalFn(List<DMNode> xs, PyObject nodeViewFunction)
	{
		return mapViewEvalFn( xs, new PyGSymIncrementalNodeFunction( nodeViewFunction ), null );
	}

	public List<DPWidget> mapViewEvalFn(List<DMNode> xs, PyObject nodeViewFunction, Object state)
	{
		return mapViewEvalFn( xs, new PyGSymIncrementalNodeFunction( nodeViewFunction ), state );
	}
	
	
	
	public DPWidget getViewNodeElement()
	{
		DVNode viewNode = (DVNode)treeNode;
		return viewNode.getElementNoRefresh();
	}
	
	public DPWidget getViewNodeContentElement()
	{
		DVNode viewNode = (DVNode)treeNode;
		return viewNode.getInnerElementNoRefresh();
	}
	
	
	
	private GSymNodeViewContext getPreviousSiblingFromChildElement(GSymNodeViewContext parent, DPWidget fromChild)
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
			GSymNodeViewContext sibling = getLastChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymNodeViewContext getLastChildFromParentElement(GSymNodeViewContext parent, DPWidget element)
	{
		if ( element.getContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewContext)element.getContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			List<DPWidget> children = branch.getChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				GSymNodeViewContext sibling = getLastChildFromParentElement( parent, children.get( i ) );
				if ( sibling != null )
				{
					return sibling;
				}
			}
		}
		return null;
	}
	

	
	private GSymNodeViewContext getNextSiblingFromChildElement(GSymNodeViewContext parent, DPWidget fromChild)
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
			GSymNodeViewContext sibling = getFirstChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymNodeViewContext getFirstChildFromParentElement(GSymNodeViewContext parent, DPWidget element)
	{
		if ( element.getContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewContext)element.getContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			for (DPWidget child: branch.getChildren())
			{
				GSymNodeViewContext sibling = getFirstChildFromParentElement( parent, child );
				if ( sibling != null )
				{
					return sibling;
				}
			}
		}
		return null;
	}
	

	
	public GSymNodeViewContext getPrevSibling()
	{
		return getPreviousSiblingFromChildElement( (GSymNodeViewContext)getParent(), getViewNodeElement() );
	}
	
	public GSymNodeViewContext getNextSibling()
	{
		return getNextSiblingFromChildElement( this, getViewNodeElement() );
	}
	
	
	
	public GSymNodeViewContext getFirstChild()
	{
		return getFirstChildFromParentElement( (GSymNodeViewContext)getParent(), getViewNodeElement() );
	}
	
	public GSymNodeViewContext getLastChild()
	{
		return getNextSiblingFromChildElement( this, getViewNodeElement() );
	}
	
	
	

	
	private static HBoxStyleParams ahboxStyleParams = new HBoxStyleParams( 0.0 );
}
