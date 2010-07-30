//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.DocPresent.Util.Range;

public abstract class ScrollBar extends ControlPres
{
	public class ScrollBarControl extends Control
	{
		private Range range;
		
		private DPElement element;
		
		
		
		public ScrollBarControl(PresentationContext ctx, StyleValues style, Range range, DPElement element, DPElement decArrow, DPElement incArrow, DPElement dragBox, ScrollBarHelper.Axis axis,
				double dragBoxPadding, double dragBoxRounding, Painter dragBoxPainter)
		{
			super( ctx, style );
			
			this.range = range;
			this.element = element;
			element.setFixedValue( range );
			
			decArrow.addInteractor( new ScrollBarHelper.ScrollBarArrowInteractor( ScrollBarHelper.ScrollBarArrowInteractor.Direction.DECREASE, range ) );
			incArrow.addInteractor( new ScrollBarHelper.ScrollBarArrowInteractor( ScrollBarHelper.ScrollBarArrowInteractor.Direction.INCREASE, range ) );
			dragBox.addInteractor( new ScrollBarHelper.ScrollBarDragBarInteractor( dragBox, axis, range, dragBoxPadding, dragBoxRounding, dragBoxPainter ) );
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
		double arrowSize = scrollBarSize - arrowPadding * 2.0;
		Painter dragBoxPainter = style.get( Controls.scrollBarDragBoxPainter, Painter.class );
		
		StyleSheet2 arrowStyle = StyleSheet2.instance.withAttr( Primitive.shapePainter, style.get( Controls.scrollBarArrowPainter ) );
		StyleSheet2 dragBoxStyle = Controls.scrollBarDragBoxStyle.get( style );
		
		Pres decArrow = arrowStyle.applyTo( createDecArrow( arrowSize ) );
		DPElement decArrowElement = decArrow.present( ctx, style );
		Pres incArrow = arrowStyle.applyTo( createIncArrow( arrowSize ) );
		DPElement incArrowElement = incArrow.present( ctx, style );
		
		
		Pres dragBar = dragBoxStyle.applyTo( createDragBox( scrollBarSize ) );
		DPElement dragBarElement = dragBar.present( ctx, style );
		Pres p = createScrollBarPres( arrowSpacing, decArrowElement.pad( arrowPadding, arrowPadding ), dragBarElement, incArrowElement.pad( arrowPadding, arrowPadding ) );
		
		DPElement element = p.present( ctx, style );
		
		return new ScrollBarControl( ctx, style, range, element, decArrowElement, incArrowElement, dragBarElement, getAxis(), dragBoxPadding, dragBoxRounding, dragBoxPainter );
	}
	
	
	protected abstract ScrollBarHelper.Axis getAxis();
	protected abstract Pres createDecArrow(double arrowSize);
	protected abstract Pres createIncArrow(double arrowSize);
	protected abstract Pres createDragBox(double scrollBarSize);
	protected abstract Pres createScrollBarPres(double spacing, DPElement decArrowElement, DPElement dragBarElement, DPElement incArrowElement);
}
