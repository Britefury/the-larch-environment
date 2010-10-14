//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Visual;

import java.awt.Graphics2D;
import java.awt.Shape;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSpacer;
import BritefuryJ.DocPresent.ElementPainter;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Drawing extends Pres
{
	public static interface Painter
	{
		public void draw(DPElement element, Graphics2D graphics);
	}
	
	
	protected static class DrawingPainter implements ElementPainter
	{
		private Painter painter;
		
		protected DrawingPainter(Painter painter)
		{
			this.painter = painter;
		}
		
		
		@Override
		public void drawBackground(DPElement element, Graphics2D graphics)
		{
		}

		@Override
		public void draw(DPElement element, Graphics2D graphics)
		{
			Shape prevClip = graphics.getClip();
			element.clip( graphics );
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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPSpacer element = new DPSpacer( minWidth, minHeight );
		element.addPainter( new DrawingPainter( painter ) );
		return element;
	}
}
