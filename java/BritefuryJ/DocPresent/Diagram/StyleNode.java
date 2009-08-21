//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;

public class StyleNode extends UnaryBranchNode
{
	// The flags indicate whether or not a state is modified by this node.
	// No flag is necessary for colour, since if the colour field is null, the colour state is not changed.
	// Flags are necessary for stroke and paint, since you may either want to leave them as they are, (flag not set) or disable them (flags set, and value == null).

	protected static int BITMASK_STROKE = 0x1;
	protected static int BITMASK_PAINT = 0x2;
	protected static int BITMASK_FILLPAINT = 0x4;
	
	// Flags
	protected int flags;
	// Stroke
	protected Stroke stroke;
	// Paint
	protected Paint paint, fillPaint;

	
	
	protected StyleNode(DiagramNode child, Stroke stroke, Paint paint, Paint fillPaint, int flags)
	{
		super( child );
		this.stroke = stroke;
		this.paint = paint;
		this.fillPaint = fillPaint;
		this.flags = flags;
	}

	public StyleNode(DiagramNode child, Stroke stroke)
	{
		super( child );
		this.stroke = stroke;
		flags = BITMASK_STROKE;
	}

	private StyleNode(StyleNode s)
	{
		super( s.child );
		stroke = s.stroke;
		paint = s.paint;
		fillPaint = s.fillPaint;
		flags = s.flags;
	}
	
	
	// User API
	public DiagramNode stroke(Stroke stroke)
	{
		StyleNode s = new StyleNode( this );
		s.stroke = stroke;
		s.flags |= BITMASK_STROKE;
		return s;
	}
	
	public DiagramNode paint(Paint paint)
	{
		StyleNode s = new StyleNode( this );
		s.paint = paint;
		s.flags |= BITMASK_PAINT;
		return s;
	}

	public DiagramNode fillPaint(Paint fillPaint)
	{
		StyleNode s = new StyleNode( this );
		s.fillPaint = fillPaint;
		s.flags |= BITMASK_FILLPAINT;
		return s;
	}

	
	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		Stroke s = null;
		Paint p = null, fp = null;
		
		if ( ( flags & BITMASK_STROKE )  !=  0 )
		{
			if ( stroke != null )
			{
				s = graphics.getStroke();
				graphics.setStroke( stroke );
			}
		}
		
		if ( ( flags & BITMASK_PAINT )  !=  0 )
		{
			p = context.getStrokePaint();
			context.setStrokePaint( paint );
		}

		if ( ( flags & BITMASK_FILLPAINT )  !=  0 )
		{
			fp = context.getFillPaint();
			context.setFillPaint( fillPaint );
		}

		
		child.draw( graphics, context );


		if ( ( flags & BITMASK_FILLPAINT )  !=  0 )
		{
			context.setFillPaint( fp );
		}

		if ( ( flags & BITMASK_PAINT )  !=  0 )
		{
			context.setStrokePaint( p );
		}

		if ( ( flags & BITMASK_STROKE )  !=  0  &&  stroke != null )
		{
			graphics.setStroke( s );
		}
	}
	
	
	public static StyleNode paintNode(DiagramNode child, Paint paint)
	{
		return new StyleNode( child, null, paint, null, BITMASK_PAINT );
	}

	public static StyleNode fillPaintNode(DiagramNode child, Paint fillPaint)
	{
		return new StyleNode( child, null, null, fillPaint, BITMASK_FILLPAINT );
	}
}
