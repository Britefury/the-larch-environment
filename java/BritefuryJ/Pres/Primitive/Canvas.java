//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPCanvas;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Canvas.DrawingNode;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Canvas extends Pres
{
	private DrawingNode drawing;
	private double width, height;
	private boolean bShrinkX, bShrinkY;
	private String textRepresentation;
	
	
	public Canvas(DrawingNode drawing, double width, double height, boolean bShrinkX, boolean bShrinkY)
	{
		this( drawing, width, height, bShrinkX, bShrinkY, "" );
	}

	public Canvas(DrawingNode drawing, double width, double height, boolean bShrinkX, boolean bShrinkY, String textRepresentation)
	{
		this.drawing = drawing;
		this.width = width;
		this.height = height;
		this.bShrinkX = bShrinkX;
		this.bShrinkY = bShrinkY;
		this.textRepresentation = textRepresentation;
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return new DPCanvas( textRepresentation, drawing, width, height, bShrinkX, bShrinkY );
	}
}