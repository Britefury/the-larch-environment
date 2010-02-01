//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class DPProxy extends DPContainer
{
	public DPProxy(ElementContext context)
	{
		this( context, ContainerStyleSheet.defaultStyleSheet );
	}

	public DPProxy(ElementContext context, ContainerStyleSheet styleSheet)
	{
		super( context, styleSheet );
	}

	



	public DPWidget getChild()
	{
		if ( registeredChildren.size() > 0 )
		{
			return registeredChildren.get( 0 );
		}
		else
		{
			return null;
		}
	}
	
	public void setChild(DPWidget child)
	{
		DPWidget prevChild = getChild();
		if ( child != prevChild )
		{
			if ( prevChild != null )
			{
				unregisterChild( prevChild );
				registeredChildren.remove( 0 );
			}
			
			if ( child != null )
			{
				registeredChildren.add( child );
				registerChild( child, null );				
			}
			
			onChildListModified();
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	

	public List<DPWidget> getChildren()
	{
		return registeredChildren;
	}

	
	
	
	private AABox2[] computeBoundingBoxes()
	{
		ArrangedSequenceLayoutNode arrangedLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
		if ( arrangedLayout != null )
		{
			return arrangedLayout.computeBranchBoundsBoxes( this );
		}
		else
		{
			return null;
		}
	}
	
	public boolean containsParentSpacePoint(Point2 p)
	{
		AABox2 boundsBoxes[] = computeBoundingBoxes();
		if ( boundsBoxes != null )
		{
			for (AABox2 box: boundsBoxes)
			{
				if ( box.containsPoint( p ) )
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean containsLocalSpacePoint(Point2 p)
	{
		AABox2 boundsBoxes[] = computeBoundingBoxes();
		if ( boundsBoxes != null )
		{
			for (AABox2 box: boundsBoxes)
			{
				if ( box.containsPoint( p ) )
				{
					return true;
				}
			}
		}
		
		return false;
	}



	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}
}
