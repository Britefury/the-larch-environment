//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import BritefuryJ.DocPresent.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class DPSpan extends DPContainerSequence
{
	public DPSpan(ContainerStyleParams styleParams)
	{
		super( styleParams );
	}
	
	protected DPSpan(DPSpan element)
	{
		super( element );
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPSpan clone = new DPSpan( this );
		clone.clonePostConstuct( this );
		return clone;
	}



	protected void drawBackground(Graphics2D graphics)
	{
		super.drawBackground( graphics );
	}

	
	protected Point2 getPopupPositionInLocalSpace()
	{
		AABox2 boxes[] = computeBoundingBoxes();
		AABox2 lastBox = boxes.length > 0  ?  boxes[boxes.length-1]  :  null;
		if ( lastBox != null )
		{
			return new Point2( lastBox.getLowerX(), lastBox.getUpperY() );
		}
		else
		{
			return new Point2( 0.0, getHeight() );
		}
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
