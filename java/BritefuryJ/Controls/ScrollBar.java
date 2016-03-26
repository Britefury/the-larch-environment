//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Util.Range;
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
		
		private LSElement element;
		
		
		
		public ScrollBarControl(PresentationContext ctx, StyleValues style, Range range, LSElement element, LSElement dragBox, ScrollBarHelper.Axis axis,
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
		public LSElement getElement()
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
		double dragBoxPadding = style.get( Controls.scrollBarDragboxPadding, Double.class ); 
		double dragBoxRounding = style.get( Controls.scrollBarDragboxRounding, Double.class );
		double dragBoxMinSize = style.get( Controls.scrollBarDragboxMinSize, Double.class );
		Painter dragBoxPainter = style.get( Controls.scrollBarDragBoxPainter, Painter.class );
		Painter dragBoxHoverPainter = style.get( Controls.scrollBarDragBoxHoverPainter, Painter.class );
		
		StyleSheet dragBoxStyle = Controls.scrollBarDragBoxStyle.get( style );
		
		
		Pres dragBar = dragBoxStyle.applyTo( createDragBox( scrollBarSize ) );
		LSElement dragBarElement = dragBar.present( ctx, style );
		Pres p = new Bin( Pres.coerce( dragBarElement ) );
		
		LSElement element = p.present( ctx, style );
		
		return new ScrollBarControl( ctx, style, range, element, dragBarElement, getAxis(), dragBoxPadding, dragBoxRounding, dragBoxMinSize, dragBoxPainter, dragBoxHoverPainter );
	}
	
	
	protected abstract ScrollBarHelper.Axis getAxis();
	protected abstract Pres createDragBox(double scrollBarSize);
}
