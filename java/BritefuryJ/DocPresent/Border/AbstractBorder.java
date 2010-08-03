//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Border;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class AbstractBorder implements Presentable
{
	public abstract double getLeftMargin();
	public abstract double getRightMargin();
	public abstract double getTopMargin();
	public abstract double getBottomMargin();
	
	public void draw(Graphics2D graphics, double x, double y, double w, double h)
	{
	}
	
	
	
	protected Pres presentationSwatch()
	{
		return getSwatchStyle().applyTo( new Box( 50.0, 25.0 ) );
	}
	

	@Override
	public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new ObjectBox( getClass().getName(), StyleSheet2.instance.withAttr( Primitive.border, this ).applyTo( new Border( presentationSwatch() ) ) );
	}
	
	
	// We have to initialise this style sheet on request, otherwise we can end up with a circular class initialisation problem
	private static StyleSheet2 _swatchStyle = null;
	
	private static StyleSheet2 getSwatchStyle()
	{
		if ( _swatchStyle == null )
		{
			_swatchStyle = StyleSheet2.instance.withAttr( Primitive.shapePainter, 
					new FilledOutlinePainter(
							new LinearGradientPaint( 0.0f, 0.0f, 50.0f, 25.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 73, 69, 94 ), new Color( 24, 5, 7 ) } ),
							new LinearGradientPaint( 0.0f, 0.0f, 50.0f, 25.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 98, 95, 115 ), new Color( 126, 125, 135 ) } ) ) );
		}
		return _swatchStyle;
	}
}
