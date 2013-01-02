//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;

import BritefuryJ.Graphics.Painter;


/*
 * Used for highlighting elements
 */
public class ElementHighlighter implements ElementPainter
{
	private Painter elementPainter;
	
	
	public ElementHighlighter(Painter elementPainter)
	{
		this.elementPainter = elementPainter;
	}
	
	
	/*
	 * Add highlight to an element
	 */
	public void highlight(LSElement element)
	{
		if ( element.isRealised() )
		{
			element.addPainter( this );
			element.queueFullRedraw();
		}
	}
	
	/*
	 * Remove highlight from an element
	 */
	public void unhighlight(LSElement element)
	{
		if ( element.isRealised() )
		{
			element.removePainter( this );
			element.queueFullRedraw();
		}
	}
	

	@Override
	public void drawBackground(LSElement element, Graphics2D graphics)
	{
	}

	@Override
	public void draw(LSElement element, Graphics2D graphics)
	{
		elementPainter.drawShapes( graphics, element.getShapes() );
	}
}
