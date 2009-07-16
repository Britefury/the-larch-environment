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

public class StyleNode extends DiagramNode
{
	// The flags indicate whether or not a state is modified by this node.
	// No flag is necessary for colour, since if the colour field is null, the colour state is not changed.
	// Flags are necessary for stroke and paint, since you may either want to leave them as they are, (flag not set) or disable them (flags set, and value == null).

	protected static int BITMASK_STROKE = 0x1;
	protected static int BITMASK_PAINT = 0x2;
	
	// Flags
	protected int flags;
	// Shape
	protected Stroke stroke;
	protected Color colour;
	
	// Filling
	protected Paint paint;
	
	// Child
	protected DiagramNode child;

	
	
	public StyleNode(DiagramNode child, Stroke stroke)
	{
		this.child = child;
		this.stroke = stroke;
		flags = BITMASK_STROKE;
	}

	public StyleNode(DiagramNode child, Color colour)
	{
		this.child = child;
		this.colour = colour;
	}

	public StyleNode(DiagramNode child, Paint paint)
	{
		this.child = child;
		this.paint = paint;
		flags = BITMASK_PAINT;
	}
	
	private StyleNode(StyleNode s)
	{
		flags = s.flags;
		stroke = s.stroke;
		colour = s.colour;
		paint = s.paint;
	}
	
	
	// User API
	public DiagramNode stroke(Stroke stroke)
	{
		StyleNode s = new StyleNode( this );
		s.stroke = stroke;
		s.flags |= BITMASK_STROKE;
		return s;
	}
	
	public DiagramNode colour(Color colour)
	{
		StyleNode s = new StyleNode( this );
		s.colour = colour;
		return s;
	}
	
	public DiagramNode paint(Paint paint)
	{
		StyleNode s = new StyleNode( this );
		s.paint = paint;
		s.flags |= BITMASK_PAINT;
		return s;
	}

	
	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		Stroke s = null;
		Color c = null;
		Paint p = null;
		int contextFlags = context.flags;
		
		if ( ( flags & BITMASK_STROKE )  !=  0 )
		{
			context.setStrokeEnabled( stroke != null );
			if ( stroke != null )
			{
				s = graphics.getStroke();
				graphics.setStroke( stroke );
			}
		}

		if ( colour != null )
		{
			c = graphics.getColor();
			graphics.setColor( colour );
		}

		if ( ( flags & BITMASK_PAINT )  !=  0 )
		{
			context.setFillEnabled( paint != null );
			if ( paint != null )
			{
				p = graphics.getPaint();
				graphics.setPaint( paint );
			}
		}

		
		child.draw( graphics, context );


		if ( ( flags & BITMASK_PAINT )  !=  0  &&  paint != null )
		{
			graphics.setPaint( p );
		}

		if ( colour != null )
		{
			graphics.setColor( c );
		}

		if ( ( flags & BITMASK_STROKE )  !=  0  &&  stroke != null )
		{
			graphics.setStroke( s );
		}
		
		context.flags = contextFlags;
	}
}
