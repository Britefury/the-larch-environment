//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import java.util.HashSet;

import BritefuryJ.Cell.Cell;
import BritefuryJ.Cell.CellEvaluator;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ProxyElement;
import BritefuryJ.DocTree.DocTreeNode;

public class DVNode
{
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
	}

	public static interface NodeElementFactory
	{
		public Element createNodeElement(DVNode viewNode, DocTreeNode treeNode);
	}
	
	public static interface NodeElementChangeListener
	{
		public void elementChangeFrom(DVNode node, Element e);
		public void elementChangeTo(DVNode node, Element e);
	}
	
	
	
	private DocView view;
	private Object docNode;
	private DocTreeNode treeNode;
	
	private DVNode parent;
	
	private Cell refreshCell, elementCell;
	private ProxyElement proxyElement;
	private Element element;
	private NodeElementFactory elementFactory;
	
	private HashSet<DVNode> children;
	
	private NodeElementChangeListener elementChangeListener;
	
	
	
	public DVNode(DocView view, DocTreeNode treeNode, NodeElementChangeListener elementChangeListener)
	{
		this.view = view;
		this.treeNode = treeNode;
		this.docNode = treeNode.getNode();
		
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
		
		
		this.elementChangeListener = elementChangeListener;
	}
	
	
	
	/*
	 * Change the doc tree node that is views by this
	 * 
	 * The underlying document node MUST BE THE SAME NODE, or this function will fail
	 */
	protected void changeTreeNode(DocTreeNode treeNode) throws CannotChangeDocNodeException
	{
		// Ensure that the doc node is the same
		if ( treeNode.getNode()  !=  docNode )
		{
			throw new CannotChangeDocNodeException();
		}
		
		this.treeNode = treeNode;
	}
	
	
	
	/*
	 * Set the node element factory
	 */
	public void setNodeElementFactory(NodeElementFactory factory)
	{
		if ( factory != elementFactory )
		{
			elementFactory = factory;
			elementCell.setEvaluator( elementCell.getEvaluator() );
		}
	}
	
	
	
	//
	//
	// Content acquisition methods
	//
	//
	
	public Element getElementNoRefresh()
	{
		return proxyElement;
	}
	
	public Element getElement()
	{
		refresh();
		return proxyElement;
	}
	
	
	public Element getInnerElementNoRefresh()
	{
		return element;
	}
	
	
	
	
	//
	//
	// Document view and node treemethods
	//
	//
	
	public DocView getDocView()
	{
		return view;
	}
	
	public DVNode getParentNodeView()
	{
		return parent;
	}
	
	public boolean isDescendantOf(DVNode node)
	{
		DVNode n = this;
		
		while ( n != null )
		{
			if ( n == node )
			{
				return true;
			}
			
			n = n.parent;
		}
		
		return false;
	}
	
	
	public DocTreeNode getTreeNode()
	{
		return treeNode;
	}
	
	public Object getDocNode()
	{
		return docNode;
	}
	
	
	
	
	//
	//
	// Refresh methods
	//
	//
	
	private void refreshNode()
	{
		if ( elementChangeListener != null )
		{
			elementChangeListener.elementChangeFrom( this, element );
		}

		// Compute the element for this node, and refresh all children
		Element e = (Element)elementCell.getValue();
		for (DVNode child: children)
		{
			child.refresh();
		}
		
		// Set the node element
		updateNodeElement( e );
		
		
		if ( elementChangeListener != null )
		{
			elementChangeListener.elementChangeTo( this, element );
		}
	}
	
	
	public void refresh()
	{
		refreshCell.getValue();
	}
	
	
	private Element computeNodeElement()
	{
		// Unregister existing child relationships
		for (DVNode child: children)
		{
			view.nodeTable.unrefViewNode( child );
		}
		children.clear();
		
		if ( elementFactory != null )
		{
			Element e = elementFactory.createNodeElement( this, treeNode );
			
			// Register new child relationships
			for (DVNode child: children)
			{
				view.nodeTable.refViewNode( child );
			}
			
			return e;
		}
		else
		{
			return null;
		}
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
	
	
	public void registerChild(DVNode child)
	{
		children.add( child );
	}
}
