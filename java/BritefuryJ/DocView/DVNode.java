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
import BritefuryJ.Cell.CellInterface;
import BritefuryJ.Cell.CellListener;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ProxyElement;
import BritefuryJ.DocTree.DocTreeNode;

public class DVNode implements CellListener
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
	
	public static interface NodeRefreshListener
	{
		public void onNodeRequestRefresh(DVNode node);
	}
	
	
	
	private DocView view;
	private Object docNode;
	private DocTreeNode treeNode;
	
	private DVNode parent;
	
	private Cell elementCell;
	private ProxyElement proxyElement;
	private Element element;
	private NodeElementFactory elementFactory;
	
	private HashSet<DVNode> children;
	
	private NodeElementChangeListener elementChangeListener;
	private NodeRefreshListener refreshListener;
	
	private boolean bRefreshRequired;
	
	
	
	public DVNode(DocView view, DocTreeNode treeNode, NodeElementChangeListener elementChangeListener)
	{
		this.view = view;
		this.treeNode = treeNode;
		this.docNode = treeNode.getNode();
		
		parent = null;
		
		
		final DVNode self = this;
		bRefreshRequired = true;
		
		
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
		elementCell.addListener( this );
		
		
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
	// Document view and node / tree methods
	//
	//
	
	public DocView getDocView()
	{
		return view;
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
	
	public void setRefreshListener(NodeRefreshListener listener)
	{
		refreshListener = listener;
	}
	
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
			assert child.parent == null  ||  child.parent == this;
			child.parent = this;
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
		if ( bRefreshRequired )
		{
			refreshNode();
			bRefreshRequired = false;
		}
	}
	
	
	private Element computeNodeElement()
	{
		// Unregister existing child relationships
		for (DVNode child: children)
		{
			view.nodeTable.unrefViewNode( child );
			child.parent = null;
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





	//
	//
	// Cell notifications
	//
	//
	
	public void onCellChanged(CellInterface cell)
	{
		assert cell == elementCell;
		requestRefresh();
	}

	public void onCellEvaluator(CellInterface cell, CellEvaluator oldEval, CellEvaluator newEval)
	{
	}
	
	public void onCellValidity(CellInterface cell)
	{
	}
	
	
	
	//
	//
	// Child notifications
	//
	//
	
	public void onChildRefreshRequired()
	{
		requestRefresh();
	}
	
	
	
	//
	//
	// Refresh request
	//
	//
	
	private void requestRefresh()
	{
		if ( !bRefreshRequired )
		{
			bRefreshRequired = true;
			if ( parent != null )
			{
				parent.onChildRefreshRequired();
			}
			
			if ( refreshListener != null )
			{
				refreshListener.onNodeRequestRefresh( this );
			}
		}
	}
}
