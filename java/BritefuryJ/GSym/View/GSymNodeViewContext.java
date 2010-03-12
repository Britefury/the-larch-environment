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

import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeContext;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeFunction;

public class GSymNodeViewContext extends GSymIncrementalNodeContext implements FragmentContext
{
	TextStyleParams viewError_textStyle = new TextStyleParams( null, null, null, false, new Font( "SansSerif", Font.BOLD, 12 ),  new Color( 0.8f, 0.0f, 0.0f ),  null,  null,  false );

	
	public GSymNodeViewContext(GSymViewContext viewContext, DVNode viewNode)
	{
		super( viewContext, viewNode );
		viewNode.setElementContext( this );
	}
	
	
	
	public GSymViewContext getViewContext()
	{
		return (GSymViewContext)treeContext;
	}
	
	
	
	
	public DPElement errorElement(String errorText)
	{
		return new DPText( viewError_textStyle, errorText );
	}
	
	
	
	public DPElement viewEval(DMNode x, StyleSheet styleSheet)
	{
		return viewEvalFn( x, styleSheet, (GSymIncrementalNodeFunction)null, null );
	}

	public DPElement viewEval(DMNode x, StyleSheet styleSheet, Object state)
	{
		return viewEvalFn( x, styleSheet, (GSymIncrementalNodeFunction)null, state );
	}

	public DPElement viewEvalFn(DMNode x, StyleSheet styleSheet, GSymIncrementalNodeFunction nodeViewFunction)
	{
		return viewEvalFn( x, styleSheet, nodeViewFunction, null );
	}

	public DPElement viewEvalFn(DMNode x, StyleSheet styleSheet, GSymIncrementalNodeFunction nodeViewFunction, Object state)
	{
		return (DPElement)evalFn( x, nodeViewFunction, new GSymViewContext.ViewInheritedState( styleSheet, state ) );
	}
	
	public DPElement viewEvalFn(DMNode x, StyleSheet styleSheet, PyObject nodeViewFunction)
	{
		return viewEvalFn( x, styleSheet, new PyGSymViewFragmentFunction( nodeViewFunction ), null );
	}

	public DPElement viewEvalFn(DMNode x, StyleSheet styleSheet, PyObject nodeViewFunction, Object state)
	{
		return viewEvalFn( x, styleSheet, new PyGSymViewFragmentFunction( nodeViewFunction ), state );
	}
	
	
	
	
	public List<DPElement> mapViewEval(List<DMNode> xs, StyleSheet styleSheet)
	{
		return mapViewEvalFn( xs, styleSheet, (GSymIncrementalNodeFunction)null, null );
	}

	public List<DPElement> mapViewEval(List<DMNode> xs, StyleSheet styleSheet, Object state)
	{
		return mapViewEvalFn( xs, styleSheet, (GSymIncrementalNodeFunction)null, state );
	}

	public List<DPElement> mapViewEvalFn(List<DMNode> xs, StyleSheet styleSheet, GSymIncrementalNodeFunction nodeViewFunction)
	{
		return mapViewEvalFn( xs, styleSheet, nodeViewFunction, null );
	}

	public List<DPElement> mapViewEvalFn(List<DMNode> xs, StyleSheet styleSheet, GSymIncrementalNodeFunction nodeViewFunction, Object state)
	{
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		children.ensureCapacity( xs.size() );
		for (DMNode x: xs)
		{
			children.add( viewEvalFn( x, styleSheet, nodeViewFunction, state ) );
		}
		return children;
	}
	
	public List<DPElement> mapViewEvalFn(List<DMNode> xs, StyleSheet styleSheet, PyObject nodeViewFunction)
	{
		return mapViewEvalFn( xs, styleSheet, new PyGSymViewFragmentFunction( nodeViewFunction ), null );
	}

	public List<DPElement> mapViewEvalFn(List<DMNode> xs, StyleSheet styleSheet, PyObject nodeViewFunction, Object state)
	{
		return mapViewEvalFn( xs, styleSheet, new PyGSymViewFragmentFunction( nodeViewFunction ), state );
	}
	
	
	
	public DPElement getViewNodeElement()
	{
		DVNode viewNode = (DVNode)treeNode;
		return viewNode.getElementNoRefresh();
	}
	
	public DPElement getViewNodeContentElement()
	{
		DVNode viewNode = (DVNode)treeNode;
		return viewNode.getInnerElementNoRefresh();
	}
	
	
	
	private GSymNodeViewContext getPreviousSiblingFromChildElement(GSymNodeViewContext parent, DPElement fromChild)
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
			GSymNodeViewContext sibling = getLastChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymNodeViewContext getLastChildFromParentElement(GSymNodeViewContext parent, DPElement element)
	{
		if ( element.getFragmentContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewContext)element.getFragmentContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			List<DPElement> children = branch.getChildren();
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
	

	
	private GSymNodeViewContext getNextSiblingFromChildElement(GSymNodeViewContext parent, DPElement fromChild)
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
			GSymNodeViewContext sibling = getFirstChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymNodeViewContext getFirstChildFromParentElement(GSymNodeViewContext parent, DPElement element)
	{
		if ( element.getFragmentContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewContext)element.getFragmentContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			for (DPElement child: branch.getChildren())
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
}
