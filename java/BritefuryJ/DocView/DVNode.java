//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class DVNode extends IncrementalTreeNode
{
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	public static interface NodeContext
	{
	};
	
	

	
	
	private DPProxy proxyElement;
	private DPWidget element;
	
	private NodeContext nodeContext;
	
	
	
	public DVNode(DocView view, DMNode docNode, NodeResultChangeListener resultChangeListener)
	{
		super( view, docNode, resultChangeListener );
		
		// Proxy element, with null context, initially; later set in @setContext method
		proxyElement = new DPProxy( null );
		element = null;

		nodeContext = null;
	}
	
	
	
	//
	//
	// Content acquisition methods
	//
	//
	
	public DPWidget getElementNoRefresh()
	{
		return proxyElement;
	}
	
	public DPWidget getElement()
	{
		refresh();
		return proxyElement;
	}
	
	
	public DPWidget getInnerElementNoRefresh()
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
		return (DocView)getIncrementalTree();
	}
	
	
	
	
	//
	//
	// Context methods
	//
	//
	
	public NodeContext getContext()
	{
		return nodeContext;
	}
	
	public void setContext(NodeContext context, ElementContext elementContext)
	{
		nodeContext = context;
		proxyElement.setContext( elementContext );
	}
	
	
	
	
	//
	//
	// Refresh methods
	//
	//
	
	protected Object computeNodeResult()
	{
		getDocView().profile_startJava();
		Object result = super.computeNodeResult();
		getDocView().profile_stopJava();
		return result;
	}

	protected void updateNodeResult(Object r)
	{
		getDocView().profile_startUpdateNodeElement();
		super.updateNodeResult( r );
		if ( r != element )
		{
			if ( r != null )
			{
				element = (DPWidget)r;
				proxyElement.setChild( element );
			}
			else
			{
				element = null;
				proxyElement.setChild( null );
			}
		}
		getDocView().profile_stopUpdateNodeElement();
	}
}
