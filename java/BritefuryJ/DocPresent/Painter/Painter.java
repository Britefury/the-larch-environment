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
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class Painter implements Presentable
{
	public abstract void drawShape(Graphics2D graphics, Shape shape);
	public abstract void drawShapes(Graphics2D graphics, Shape shapes[]);



	@Override
	public Pres present(GSymFragmentView fragment, AttributeTable inheritedState)
	{
		return new ObjectBox( getClass().getName(), new Border( StyleSheet2.instance.withAttr( Primitive.shapePainter, this ).applyTo( new Box( 50.0, 25.0 ) ) ) );
	}
}
