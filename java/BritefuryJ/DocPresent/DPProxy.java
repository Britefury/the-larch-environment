//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

import BritefuryJ.DocPresent.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class DPProxy extends DPContainer
{
	public DPProxy()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public DPProxy(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}

	



	public DPElement getChild()
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
	
	public void setChild(DPElement child)
	{
		DPElement prevChild = getChild();
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
				registerChild( child );				
			}
			
			onChildListModified();
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPElement child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	

	public List<DPElement> getChildren()
	{
		return registeredChildren;
	}

	
	
	
	protected Shape[] getShapes()
	{
		AABox2 bounds[] = computeBoundingBoxes();
		Shape shapes[] = new Shape[bounds.length];
		for (int i = 0; i < bounds.length; i++)
		{
			AABox2 box = bounds[i];
			shapes[i] = new Rectangle2D.Double( box.getLowerX(), box.getLowerY(), box.getWidth(), box.getHeight() );
		}
		return shapes;
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
}
