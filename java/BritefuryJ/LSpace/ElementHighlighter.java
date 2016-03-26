//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
