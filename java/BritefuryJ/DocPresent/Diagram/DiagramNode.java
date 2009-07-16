//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import BritefuryJ.Math.Vector2;

public abstract class DiagramNode
{
	protected static class DrawContext
	{
		protected static int BITMASK_STROKE = 0x1;
		protected static int BITMASK_FILL = 0x2;
		
		protected int flags;
		
		
		public DrawContext()
		{
			flags = 0;
		}
		
		
		public void setStrokeEnabled(boolean bEnabled)
		{
			if ( bEnabled )
			{
				flags |= BITMASK_STROKE;
			}
			else
			{
				flags &= ~BITMASK_STROKE;
			}
		}

		public void setFillEnabled(boolean bEnabled)
		{
			if ( bEnabled )
			{
				flags |= BITMASK_FILL;
			}
			else
			{
				flags &= ~BITMASK_FILL;
			}
		}
		
		
		public boolean isStrokeEnabled()
		{
			return ( flags & BITMASK_STROKE )  !=  0;
		}
		
		public boolean isFillEnabled()
		{
			return ( flags & BITMASK_FILL )  !=  0;
		}
	}
	
	
	// Hover
	protected HoverMonitor hoverMonitor;
	protected DiagramNode hoverHighlight;
	
	// Interaction
	protected InteractionListener interaction;
	
	
	
	
	protected DiagramNode()
	{
	}
	
	protected DiagramNode(DiagramNode n)
	{
	}
	
	
	
	// User API
	public DiagramNode stroke(Stroke stroke)
	{
		return new StyleNode( this, stroke );
	}
	
	public DiagramNode colour(Color colour)
	{
		return new StyleNode( this, colour );
	}
	
	public DiagramNode paint(Paint paint)
	{
		return new StyleNode( this, paint );
	}
	
	public DiagramNode hoverMonitor(HoverMonitor monitor)
	{
		return new InteractionNode( this, monitor );
	}
	
	public DiagramNode hoverHighlight(DiagramNode highlight)
	{
		return new InteractionNode( this, highlight );
	}
	
	public DiagramNode onInteraction(InteractionListener listener)
	{
		return new InteractionNode( this, listener );
	}

	public DiagramNode transform(AffineTransform t)
	{
		return new TransformationNode( this, t );
	}
	
	
	public DiagramNode translate(double x, double y)
	{
		return transform( AffineTransform.getTranslateInstance( x, y ) );
	}

	public DiagramNode translate(Vector2 x)
	{
		return transform( AffineTransform.getTranslateInstance( x.x, x.y ) );
	}


	public DiagramNode scale(double x, double y)
	{
		return transform( AffineTransform.getScaleInstance( x, y ) );
	}

	public DiagramNode scale(Vector2 s)
	{
		return transform( AffineTransform.getScaleInstance( s.x, s.y ) );
	}

	public DiagramNode scale(double s)
	{
		return transform( AffineTransform.getScaleInstance( s, s ) );
	}

	
	public DiagramNode rotate(double r)
	{
		return transform( AffineTransform.getRotateInstance( r ) );
	}

	
	
	// Interface API
	public abstract void draw(Graphics2D graphics, DrawContext context);
}
