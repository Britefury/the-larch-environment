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

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;

class CheckboxHelper
{
	protected static class CheckboxCheckPainterInteractor extends ElementInteractor
	{
		private Paint paint;
		private Checkbox checkbox;
		private static final Stroke stroke = new BasicStroke( 2.0f );
		
		
		public CheckboxCheckPainterInteractor(Paint paint, Checkbox checkbox)
		{
			this.paint = paint;
			this.checkbox = checkbox;
		}
		
		@Override
		public void draw(DPElement element, Graphics2D graphics)
		{
			if ( checkbox.getState() )
			{
				double w = element.getWidth();
				double h = element.getHeight();
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


	protected static class CheckboxCheckInteractor extends ElementInteractor
	{
		private Checkbox checkbox;
		
		
		public CheckboxCheckInteractor(Checkbox checkbox)
		{
			this.checkbox = checkbox;
		}
		
		
		
		@Override
		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				checkbox.toggle();
				return true;
			}
			else
			{
				return false;
			}
		}
	}
}
