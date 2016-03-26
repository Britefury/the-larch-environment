//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Visual;

import java.awt.Graphics2D;
import java.awt.Shape;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpacer;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Drawing extends Pres
{
	public static interface Painter
	{
		public void draw(LSElement element, Graphics2D graphics);
	}
	
	
	protected static class DrawingPainter implements ElementPainter
	{
		private Painter painter;
		
		protected DrawingPainter(Painter painter)
		{
			this.painter = painter;
		}
		
		
		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
		}

		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
			Shape prevClip = graphics.getClip();
			element.clipToAllocBox( graphics );
			painter.draw( element, graphics );
			graphics.setClip( prevClip );
		}
	}
	
	
	protected double minWidth, minHeight;
	protected Painter painter;
	
	
	public Drawing(double minWidth, double minHeight, Painter painter)
	{
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		this.painter = painter;
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSSpacer element = new LSSpacer( minWidth, minHeight );
		element.addPainter( new DrawingPainter( painter ) );
		return element;
	}
}
