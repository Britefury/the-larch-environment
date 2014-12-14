//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.BorderWithHeaderBar;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Arrow;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.awt.*;

public class DropDownExpander extends Expander
{
	protected static class DropDownExpanderInteractor implements ClickElementInteractor
	{
		ExpanderControl control;
		
		protected DropDownExpanderInteractor()
		{
		}
		
		
		public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
		{
			control.toggle();
			return true;
		}
	}



	private Pres contents;
	private Pres header;

	
	public DropDownExpander(Object header, Object contents, boolean initialState, ExpanderListener listener)
	{
		super( initialState, listener );
		
		this.header = coerce( header );
		this.contents = coerce( contents );
	}
	
	public DropDownExpander(Object header, Object contents, ExpanderListener listener)
	{
		this( header, contents, false, listener );
	}
	
	public DropDownExpander(Object header, Object contents)
	{
		this( header, contents, false, null );
	}
	
	public DropDownExpander(Object header, Object contents, LiveInterface state, ExpanderListener listener)
	{
		super( state, listener );
		
		this.header = coerce( header );
		this.contents = coerce( contents );
	}
	
	public DropDownExpander(Object header, Object contents, LiveValue state)
	{
		super( state );
		
		this.header = coerce( header );
		this.contents = coerce( contents );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useDropDownExpanderAttrs( style );

		StyleSheet arrowStyle = StyleSheet.style( Primitive.shapePainter.as( style.get( Controls.dropDownExpanderHeaderArrowPainter, Painter.class ) ) );
		double arrowSize = style.get(Controls.dropDownExpanderHeaderArrowSize, Double.class);
		double padding = style.get( Controls.dropDownExpanderPadding, Double.class );
		AbstractBorder expanderBorder = style.get( Controls.dropDownExpanderBorder, AbstractBorder.class );
		Paint headerPaint = style.get( Controls.dropDownExpanderHeaderPaint, Paint.class );
		Paint headerHoverPaint = style.get( Controls.dropDownExpanderHeaderHoverPaint, Paint.class );

		
		Pres expandedArrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.DOWN, arrowSize ) );
		Pres contractedArrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.RIGHT, arrowSize ) );
		
		DropDownExpanderInteractor headerInteractor = new DropDownExpanderInteractor();

		StyleSheet headerRowStyle = StyleSheet.style( Primitive.rowSpacing.as( style.get( Controls.dropDownExpanderHeaderContentsSpacing, Double.class ) ) );

		BorderWithHeaderBar body = new BorderWithHeaderBar(expanderBorder, headerPaint, headerHoverPaint);

		Pres expandedHeader = headerRowStyle.applyTo( new Row(
				new Pres[] { expandedArrow.alignHPack().alignHPack().alignVCentre(), header.alignHExpand() } ) ).withElementInteractor( headerInteractor ).alignHExpand();
		Pres expanded = body.surround(expandedHeader, contents.padX(padding, 0.0));

		Pres contractedHeader = headerRowStyle.applyTo( new Row(
				new Pres[] { contractedArrow.alignHPack().alignHPack().alignVCentre(), header.alignHExpand() } ) ).withElementInteractor( headerInteractor ).alignHExpand();
		Pres contracted = body.surroundHeader(contractedHeader);
		
		LiveInterface state = stateSource.getLive();

		LiveFunction contentsLive = createContentsFn( state, expanded, contracted );
		
		Pres expander = new Bin( contentsLive );
		LSBin expanderElement = (LSBin)expander.present( ctx, usedStyle );
		
		ExpanderControl control = new ExpanderControl( ctx, usedStyle, expanderElement, state, listener );
		headerInteractor.control = control;
		return control;
	}
}
