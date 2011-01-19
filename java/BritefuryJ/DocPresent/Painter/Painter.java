//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Painter;

import java.awt.Graphics2D;
import java.awt.Shape;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DefaultPerspective.PresCom.ObjectBox;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.IncrementalView.FragmentView;

public abstract class Painter implements Presentable
{
	public abstract void drawShape(Graphics2D graphics, Shape shape);
	public abstract void drawShapes(Graphics2D graphics, Shape shapes[]);



	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new ObjectBox( getClass().getName(), new Border( StyleSheet.instance.withAttr( Primitive.shapePainter, this ).applyTo( new Box( 50.0, 25.0 ) ) ) );
	}
}
