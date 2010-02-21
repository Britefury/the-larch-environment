//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Paint;

public class LineStyleParams extends WidgetStyleParams
{
	public enum Direction
	{
		HORIZONTAL,
		VERTICAL
	}
	
	
	public static final LineStyleParams defaultStyleParams = new LineStyleParams();
	
	
	protected final Direction direction;
	protected final Paint linePaint;
	protected final double thickness, inset, padding;
	
	
	public LineStyleParams()
	{
		this( Direction.HORIZONTAL, Color.black, 1.0, 0.0, 0.0 );
	}
	
	public LineStyleParams(Direction direction, Paint linePaint)
	{
		this( direction, linePaint, 1.0, 0.0, 0.0 );
	}
	
	public LineStyleParams(Direction direction, Paint linePaint, double thickness, double inset, double padding)
	{
		super();

		this.direction = direction;
		this.linePaint = linePaint;
		this.thickness = thickness;
		this.inset = inset;
		this.padding = padding;
	}
	
	
	
	public Direction getDirection()
	{
		return direction;
	}
	
	public Paint getLinePaint()
	{
		return linePaint;
	}
	
	public double getThickness()
	{
		return thickness;
	}
	
	public double getInset()
	{
		return inset;
	}
	
	public double getPadding()
	{
		return padding;
	}
}