//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.LSElement;

class CheckboxHelper
{
	protected static class CheckboxCheckPainter implements ElementPainter
	{
		private Paint paint;
		private Checkbox.CheckboxControl checkbox;
		private static final Stroke stroke = new BasicStroke( 2.0f );
		
		
		public CheckboxCheckPainter(Paint paint, Checkbox.CheckboxControl checkbox)
		{
			this.paint = paint;
			this.checkbox = checkbox;
		}
		
		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
		}
		
		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
			if ( checkbox.getState() )
			{
				double w = element.getActualWidth();
				double h = element.getActualHeight();
				Line2D.Double a = new Line2D.Double( 0.0, 0.0, w, h );
				Line2D.Double b = new Line2D.Double( w, 0.0, 0.0, h );
				
				Paint savedPaint = graphics.getPaint();
				Stroke savedStroke = graphics.getStroke();
				graphics.setPaint( paint );
				graphics.setStroke( stroke );
				graphics.draw( a );
				graphics.draw( b );
				graphics.setStroke( savedStroke );
				graphics.setPaint( savedPaint );
			}
		}
	}


	protected static class CheckboxCheckInteractor implements ClickElementInteractor
	{
		private Checkbox.CheckboxControl checkbox;
		
		
		public CheckboxCheckInteractor(Checkbox.CheckboxControl checkbox)
		{
			this.checkbox = checkbox;
		}
		
		
		
		@Override
		public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
		{
			checkbox.toggle();
			return true;
		}
	}
}
