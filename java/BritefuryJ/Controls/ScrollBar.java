//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class ScrollBar extends ControlPres
{
	public class ScrollBarControl extends Control
	{
		private Range range;
		
		private DPElement element;
		
		
		
		public ScrollBarControl(PresentationContext ctx, StyleValues style, Range range, DPElement element, DPElement decArrow, DPElement incArrow, DPElement dragBox, ScrollBarHelper.Axis axis,
				double dragBoxPadding, double dragBoxRounding, double dragBoxMinSize, Painter dragBoxPainter)
		{
			super( ctx, style );
			
			this.range = range;
			this.element = element;
			element.setFixedValue( range );
			
			decArrow.addElementInteractor( new ScrollBarHelper.ScrollBarArrowInteractor( ScrollBarHelper.ScrollBarArrowInteractor.Direction.DECREASE, range ) );
			incArrow.addElementInteractor( new ScrollBarHelper.ScrollBarArrowInteractor( ScrollBarHelper.ScrollBarArrowInteractor.Direction.INCREASE, range ) );
			ScrollBarHelper.ScrollBarDragBarInteractor dragBoxInteractor = new ScrollBarHelper.ScrollBarDragBarInteractor( dragBox, axis, range,
					dragBoxPadding, dragBoxRounding, dragBoxMinSize, dragBoxPainter );
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
		double arrowPadding = style.get( Controls.scrollBarArrowPadding, Double.class ); 
		double arrowSpacing = style.get( Controls.scrollBarArrowSpacing, Double.class ); 
		double scrollBarSize = style.get( Controls.scrollBarSize, Double.class ); 
		double dragBoxPadding = style.get( Controls.scrollBarArrowDragboxPadding, Double.class ); 
		double dragBoxRounding = style.get( Controls.scrollBarArrowDragboxRounding, Double.class );
		double dragBoxMinSize = style.get( Controls.scrollBarArrowDragboxMinSize, Double.class );
		double arrowSize = scrollBarSize - arrowPadding * 2.0;
		Painter dragBoxPainter = style.get( Controls.scrollBarDragBoxPainter, Painter.class );
		
		StyleSheet arrowStyle = Controls.scrollBarArrowStyle.get( style );
		StyleSheet dragBoxStyle = Controls.scrollBarDragBoxStyle.get( style );
		
		Pres decArrow = arrowStyle.applyTo( createDecArrow( arrowSize ) ).pad( arrowPadding, arrowPadding );
		DPElement decArrowElement = decArrow.present( ctx, style );
		Pres incArrow = arrowStyle.applyTo( createIncArrow( arrowSize ) ).pad( arrowPadding, arrowPadding );
		DPElement incArrowElement = incArrow.present( ctx, style );
		
		
		Pres dragBar = dragBoxStyle.applyTo( createDragBox( scrollBarSize ) );
		DPElement dragBarElement = dragBar.present( ctx, style );
		Pres p = createScrollBarPres( arrowSpacing, decArrowElement, dragBarElement, incArrowElement );
		
		DPElement element = p.present( ctx, style );
		
		return new ScrollBarControl( ctx, style, range, element, decArrowElement, incArrowElement, dragBarElement, getAxis(), dragBoxPadding, dragBoxRounding, dragBoxMinSize, dragBoxPainter );
	}
	
	
	protected abstract ScrollBarHelper.Axis getAxis();
	protected abstract Pres createDecArrow(double arrowSize);
	protected abstract Pres createIncArrow(double arrowSize);
	protected abstract Pres createDragBox(double scrollBarSize);
	protected abstract Pres createScrollBarPres(double spacing, DPElement decArrowElement, DPElement dragBarElement, DPElement incArrowElement);
}
