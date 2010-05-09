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
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class Border implements Presentable
{
	public abstract double getLeftMargin();
	public abstract double getRightMargin();
	public abstract double getTopMargin();
	public abstract double getBottomMargin();
	
	public void draw(Graphics2D graphics, double x, double y, double w, double h)
	{
	}
	
	
	
	protected DPElement presentationSwatch()
	{
		return getSwatchStyle().box( 50.0, 25.0 );
	}
	

	@Override
	public DPElement present(GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		return styleSheet.objectBox( getClass().getName(), PrimitiveStyleSheet.instance.withBorder( this ).border( presentationSwatch() ) );
	}
	
	
	private static PrimitiveStyleSheet _swatchStyle = null;
	private static PrimitiveStyleSheet getSwatchStyle()
	{
		if ( _swatchStyle == null )
		{
			Paint fillPaint = new LinearGradientPaint( 0.0f, 0.0f, 50.0f, 25.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 73, 69, 94 ), new Color( 24, 5, 7 ) } );
			Paint outlinePaint = new LinearGradientPaint( 0.0f, 0.0f, 50.0f, 25.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 98, 95, 115 ), new Color( 126, 125, 135 ) } );
			_swatchStyle = PrimitiveStyleSheet.instance.withShapePainter( new FilledOutlinePainter( fillPaint, outlinePaint ) );
		}
		return _swatchStyle;
	}
}
