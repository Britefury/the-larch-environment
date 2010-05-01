//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Painter;

import java.awt.Graphics2D;
import java.awt.Shape;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentViewContext;

public abstract class Painter implements Presentable
{
	public abstract void drawShape(Graphics2D graphics, Shape shape);
	public abstract void drawShapes(Graphics2D graphics, Shape shapes[]);



	@Override
	public DPElement present(GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		PrimitiveStyleSheet p = PrimitiveStyleSheet.instance;
		return styleSheet.objectBox( getClass().getName(), p.border( p.withShapePainter( this ).box( 50.0, 25.0 ) ) );
	}
}
