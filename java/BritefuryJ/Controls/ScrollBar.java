//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class ScrollBar extends ControlPres
{
	public class ScrollBarControl extends Control
	{
		private Range range;
		
		private DPElement element;
		
		
		
		public ScrollBarControl(PresentationContext ctx, StyleValues style, Range range, DPElement element, DPElement dragBox, ScrollBarHelper.Axis axis,
				double dragBoxPadding, double dragBoxRounding, double dragBoxMinSize, Painter dragBoxPainter, Painter dragBoxHoverPainter)
		{
			super( ctx, style );
			
			this.range = range;
			this.element = element;
			element.setFixedValue( range );
			
			ScrollBarHelper.ScrollBarDragBarInteractor dragBoxInteractor = new ScrollBarHelper.ScrollBarDragBarInteractor( dragBox, axis, range,
					dragBoxPadding, dragBoxRounding, dragBoxMinSize, dragBoxPainter, dragBoxHoverPainter );
			dragBox.addElementInteractor( dragBoxInteractor );
			dragBox.addPainter( dragBoxInteractor );
		}
		
		
		public Range getRange()
		{
			return range;
		}
		
		
		@Override
		public DPElement getElement()
		{
			return element;
		}
	}
	
	
	private Range range;
	
	public ScrollBar(Range range)
	{
		this.range = range;
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		double scrollBarSize = style.get( Controls.scrollBarSize, Double.class ); 
		double dragBoxPadding = style.get( Controls.scrollBarArrowDragboxPadding, Double.class ); 
		double dragBoxRounding = style.get( Controls.scrollBarArrowDragboxRounding, Double.class );
		double dragBoxMinSize = style.get( Controls.scrollBarArrowDragboxMinSize, Double.class );
		Painter dragBoxPainter = style.get( Controls.scrollBarDragBoxPainter, Painter.class );
		Painter dragBoxHoverPainter = style.get( Controls.scrollBarDragBoxHoverPainter, Painter.class );
		
		StyleSheet dragBoxStyle = Controls.scrollBarDragBoxStyle.get( style );
		
		
		Pres dragBar = dragBoxStyle.applyTo( createDragBox( scrollBarSize ) );
		DPElement dragBarElement = dragBar.present( ctx, style );
		Pres p = new Bin( Pres.coerce( dragBarElement ) );
		
		DPElement element = p.present( ctx, style );
		
		return new ScrollBarControl( ctx, style, range, element, dragBarElement, getAxis(), dragBoxPadding, dragBoxRounding, dragBoxMinSize, dragBoxPainter, dragBoxHoverPainter );
	}
	
	
	protected abstract ScrollBarHelper.Axis getAxis();
	protected abstract Pres createDecArrow(double arrowSize);
	protected abstract Pres createIncArrow(double arrowSize);
	protected abstract Pres createDragBox(double scrollBarSize);
}
