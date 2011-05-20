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
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class AbstractBorder implements Presentable
{
	public abstract double getLeftMargin();
	public abstract double getRightMargin();
	public abstract double getTopMargin();
	public abstract double getBottomMargin();
	
	public void draw(Graphics2D graphics, double x, double y, double w, double h)
	{
	}
	
	
	private StyleSheet style = null;
	
	public Pres surround(Object x)
	{
		if ( style == null )
		{
			style = StyleSheet.instance.withAttr( Primitive.border, this );
		}
		return style.applyTo( new Border( x ) );
	}
	
	public Pres __call__(Object x)
	{
		return surround( x );
	}
	
	
	
	protected Pres presentationSwatch()
	{
		return getSwatchStyle().applyTo( new Box( 50.0, 25.0 ) );
	}
	

	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new ObjectBox( getClass().getName(), StyleSheet.instance.withAttr( Primitive.border, this ).applyTo( new Border( presentationSwatch() ) ) );
	}
	
	
	// We have to initialise this style sheet on request, otherwise we can end up with a circular class initialisation problem
	private static StyleSheet _swatchStyle = null;
	
	private static StyleSheet getSwatchStyle()
	{
		if ( _swatchStyle == null )
		{
			_swatchStyle = StyleSheet.instance.withAttr( Primitive.shapePainter, 
					new FilledOutlinePainter(
							new LinearGradientPaint( 0.0f, 0.0f, 50.0f, 25.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 73, 69, 94 ), new Color( 24, 5, 7 ) } ),
							new LinearGradientPaint( 0.0f, 0.0f, 50.0f, 25.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 98, 95, 115 ), new Color( 126, 125, 135 ) } ) ) );
		}
		return _swatchStyle;
	}
}
