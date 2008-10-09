//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

/*import java.util.HashSet;

import org.python.core.PyObject;

import BritefuryJ.Cell.Cell;
import BritefuryJ.Cell.CellEvaluator;
import BritefuryJ.DocPresent.DPWidget.IsNotInSubtreeException;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.ProxyElement;
import BritefuryJ.DocPresent.ElementTree.Caret.ElementCaret;
import BritefuryJ.DocPresent.Marker.Marker;*/

public class DVNode
{
/*	public static interface NodeElementFactory
	{
		public Element createNodeElement();
	}
	
	
	
	private static class PositionBiasContent
	{
		public int position;
		public Marker.Bias bias;
		public String content;
		
		
		public PositionBiasContent(int position, Marker.Bias bias, String content)
		{
			this.position = position;
			this.bias = bias;
			this.content = content;
		}
	}
	
	
	private DocView view;
	private PyObject treeNode, docNode;
	
	private DVNode parent;
	
	private Cell refreshCell, elementCell;
	private ProxyElement proxyElement;
	private Element element;
	private NodeElementFactory elementFactory;
	
	private HashSet<DVNode> children;
	
	
	public DVNode(DocView view, PyObject treeNode, PyObject docNode)
	{
		this.view = view;
		this.treeNode = treeNode;
		this.docNode = docNode;
		
		parent = null;
		
		
		final DVNode self = this;
		CellEvaluator refreshEval = new CellEvaluator()
		{
			public Object evaluate()
			{
				self.refreshNode();
				return null;
			}
		};
		refreshCell = new Cell();
		refreshCell.setEvaluator( refreshEval );
		
		
		proxyElement = new ProxyElement();
		element = null;

		elementFactory = null;

		CellEvaluator elementEval = new CellEvaluator()
		{
			public Object evaluate()
			{
				return self.computeNodeElement();
			}
		};
		elementCell = new Cell();
		elementCell.setEvaluator( elementEval );
		
		
		children = new HashSet<DVNode>();
	}
	
	
	
	public void changeTreeNode(PyObject treeNode)
	{
		// Ensure that the doc node is the same
		PyObject d = treeNode.__getattr__( "docNode" );
		assert d == docNode;
		this.treeNode = treeNode;
	}
	
	
	
	//
	//
	// Document view methods
	//
	//
	
	public DocView getDocView()
	{
		return view;
	}
	
	
	
	//
	//
	// Refresh methods
	//
	//
	
	private void refreshNode()
	{
		String startContent = element != null  ?  element.getContent()  :  "";
		
		// If the caret is within the bounds of @element; set the view's caret node to @this
		PositionBiasContent startState = getCaretPositionBiasAndContentString( element );
		if ( startState != null )
		{
			view.setCaretNode( this );
		}
		
		// Compute the element for this node, and refresh all children
		Element e = (Element)elementCell.getValue();
		for (DVNode child: children)
		{
			child.refresh();
		}
		
		// Set the node element
		updateNodeElement( e );
		
		// If the caret node is still @this (has not been grabbed by an inner node)
		if ( view.getCaretNode() == this )
		{
			if ( startState != null  &&  element != null )
			{
				String newContent = element.getContent();
				
				int newPosition = startState.position;
				Marker.Bias newBias = startState.bias;
				
				int oldIndex = startState.position  +  ( startState.bias == Marker.Bias.END  ?  1  :  0 );
				
				// Compute the difference
				
				
			}
		}
	}
	
	
	private void resetRefreshCell()
	{
		refreshCell.setEvaluator( refreshCell.getEvaluator() );
	}
	
	
	public void refresh()
	{
		refreshCell.getValue();
	}
	
	
	private PositionBiasContent getCaretPositionBiasAndContentString(Element e)
	{
		if ( e != null )
		{
			ElementTree tree = e.getElementTree();
			ElementCaret caret = tree.getCaret();
			int position;
			try
			{
				position = caret.getMarker().getPositionInSubtree( element );
			}
			catch (IsNotInSubtreeException e1)
			{
				return null;
			}
			return new PositionBiasContent( position, caret.getMarker().getBias(), e.getContent() );
		}
		else
		{
			return null;
		}
	}
	
	
	
	private Element computeNodeElement()
	{
		return null;
	}
	
	
	private void updateNodeElement(Element e)
	{
		if ( e != null )
		{
			element = e;
			proxyElement.setChild( element );
		}
		else
		{
			element = null;
			proxyElement.setChild( null );
		}
	}
	
	*/
}
