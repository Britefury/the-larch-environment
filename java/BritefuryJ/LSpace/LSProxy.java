//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

import BritefuryJ.LSpace.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LSProxy extends LSContainerNonOverlayed
{
	protected final static int FLAGS_PROXY_END = FLAGS_CONTAINERNONOVERLAYED_END * 0x1;

	
	
	public LSProxy(LSElement child)
	{
		this( ContainerStyleParams.defaultStyleParams, child );
	}

	public LSProxy(ContainerStyleParams styleParams, LSElement child)
	{
		super(styleParams);

		if ( child != null )
		{
			registeredChildren.add( child );
			registerChild( child );				
		}
	}
	
	

	//
	// Child access / modification
	//
	
	public LSElement getChild()
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
	
	public void setChild(LSElement child)
	{
		if ( child == this )
		{
			throw new RuntimeException( "Attempt to make LSProxy recursive" );
		}
		LSElement prevChild = getChild();
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
	
	
	protected void replaceChildWithEmpty(LSElement child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	protected void replaceChild(LSElement child, LSElement replacement)
	{
		assert child == this.getChild();
		setChild( replacement );
	}
	
	

	public List<LSElement> getChildren()
	{
		return registeredChildren;
	}

	
	public boolean isSingleElementContainer()
	{
		return true;
	}

	
	
		
	protected AABox2 getVisibleBoxInLocalSpace()
	{
		AABox2 boxes[] = computeBoundingBoxes();
		if ( boxes.length > 0 )
		{
			AABox2 visibleBox = new AABox2();
			for (AABox2 box: boxes)
			{
				visibleBox.addBox( box );
			}
			return visibleBox;
		}
		else
		{
			return getLocalAABox();
		}
	}
	
	public Shape[] getShapes()
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

	// For most elements, this is the bounding box. For layout-less elements, it is their bounds within the closest
	// non-layout-less parent
	public AABox2 getVisibleSpaceBox()
	{
		AABox2 bounds[] = computeBoundingBoxes();
		AABox2 box = new AABox2();
		for (AABox2 b: bounds)
		{
			box.addBox( b );
		}
		return box;
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
