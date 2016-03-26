//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Graphics;

import java.awt.Graphics2D;
import java.awt.Shape;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class Painter implements Presentable
{
	public abstract void drawShape(Graphics2D graphics, Shape shape);
	public abstract void drawShapes(Graphics2D graphics, Shape shapes[]);



	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new ObjectBox( getClass().getName(), new Border( StyleSheet.style( Primitive.shapePainter.as( this ) ).applyTo( new Box( 50.0, 25.0 ) ) ) );
	}
}
