//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import BritefuryJ.LSpace.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LSSpan extends LSContainerSequence
{
	public LSSpan(ContainerStyleParams styleParams, LSElement[] items)
	{
		super( styleParams, items );
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		super.drawBackground( graphics );
	}


	// For most elements, this is the bounding box. For layout-less elements, it is their bounds within the closest
	// non-layout-less parent
	@Override
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
	
	@Override
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
