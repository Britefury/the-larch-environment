//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPFragment;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class DVNode extends IncrementalTreeNode
{
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	

	
	
	private DPFragment fragmentElement;
	private DPWidget element;
	
	
	
	public DVNode(DocView view, DMNode docNode, NodeResultChangeListener resultChangeListener)
	{
		super( view, docNode, resultChangeListener );
		
		// Fragment element, with null context, initially; later set in @setContext method
		fragmentElement = new DPFragment( null );
		element = null;
	}
	
	
	
	//
	//
	// Result acquisition methods
	//
	//
	
	public DPWidget getElementNoRefresh()
	{
		return fragmentElement;
	}
	
	public DPWidget getElement()
	{
		refresh();
		return fragmentElement;
	}
	
	
	public DPWidget getInnerElementNoRefresh()
	{
		return element;
	}
	
	
	public Object getResultNoRefresh()
	{
		return fragmentElement;
	}
	
	public Object getResult()
	{
		refresh();
		return fragmentElement;
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
	
	public void setElementContext(ElementContext elementContext)
	{
		fragmentElement.setContext( elementContext );
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
				fragmentElement.setChild( element );
			}
			else
			{
				element = null;
				fragmentElement.setChild( null );
			}
		}
		getDocView().profile_stopUpdateNodeElement();
	}
}
