//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.CustomSymbolStyleSheet;
import BritefuryJ.Math.Point2;


public class DPCustomSymbol extends DPContentLeafEditableEntry
{
	public interface SymbolInterface
	{
		public double getWidth();
		public double getHeight();
		public void setBoxRequisitionX(LReqBox box);
		public void setBoxRequisitionY(LReqBox box);
		public void draw(Graphics2D graphics);
	}
	
	
	protected SymbolInterface symbol;
	
	
	
	public DPCustomSymbol(SymbolInterface symbol)
	{
		this( CustomSymbolStyleSheet.defaultStyleSheet, symbol );
	}
	
	public DPCustomSymbol(CustomSymbolStyleSheet styleSheet, SymbolInterface symbol)
	{
		super( styleSheet );
		
		this.symbol = symbol;
		
		queueResize();
	}
	
	
	public SymbolInterface getSymbol()
	{
		return symbol;
	}
	
	public void setSymbol(SymbolInterface symbol)
	{
		this.symbol = symbol;
		queueResize();
	}
	
	
	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		graphics.setColor( getColour() );
		symbol.draw( graphics );
	}
	
	
	
	protected void updateRequisitionX()
	{
		symbol.setBoxRequisitionX( layoutReqBox );
	}

	protected void updateRequisitionY()
	{
		symbol.setBoxRequisitionY( layoutReqBox );
	}
	
	
	protected Color getColour()
	{
		return ((CustomSymbolStyleSheet)styleSheet).getColour();
	}

	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		AffineTransform current = pushGraphicsTransform( graphics );
		int index = c.getMarker().getIndex();
		double x = index == 0  ?  0.0  :  symbol.getWidth();
		Line2D.Double line = new Line2D.Double( x, 0.0, x, symbol.getHeight() );
		graphics.draw( line );
		popGraphicsTransform( graphics, current );
	}

	public void drawCaretAtStart(Graphics2D graphics)
	{
		AffineTransform current = pushGraphicsTransform( graphics );
		double h = symbol.getHeight();
		graphics.draw( new Line2D.Double( 0.0, 0.0, 0.0, h) );
		popGraphicsTransform( graphics, current );
	}

	public void drawCaretAtEnd(Graphics2D graphics)
	{
		AffineTransform current = pushGraphicsTransform( graphics );
		double x = symbol.getWidth();
		double h = symbol.getHeight();
		graphics.draw( new Line2D.Double( x, 0.0, x, h) );
		popGraphicsTransform( graphics, current );
	}

	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public void drawSelection(Graphics2D graphics, Marker from, Marker to)
	{
		AffineTransform current = pushGraphicsTransform( graphics );
		int startIndex = from != null  ?  from.getIndex()  :  0;
		int endIndex = to != null  ?  to.getIndex()  :  1;
		double startX = startIndex == 0  ?  0.0  :  symbol.getWidth();
		double endX = endIndex == 0  ?  0.0  :  symbol.getWidth();
		Rectangle2D.Double shape = new Rectangle2D.Double( startX, 0.0, endX - startX, symbol.getHeight() );
		graphics.fill( shape );
		popGraphicsTransform( graphics, current );
	}

	
	
	
	//
	// Marker methods
	//
	
	public int getMarkerRange()
	{
		return 1;
	}
	
	public int getMarkerPositonForPoint(Point2 localPos)
	{
		if ( localPos.x  >=  symbol.getWidth() * 0.5 )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
}


