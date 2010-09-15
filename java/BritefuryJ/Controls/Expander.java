//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Arrow;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;
import BritefuryJ.DocPresent.Event.AbstractPointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.ClickElementInteractor;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Expander extends ControlPres
{
	public static interface ExpanderListener
	{
		public void onExpander(ExpanderControl expander, boolean bExpanded);
	}
	
	
	
	private static class ExpanderInteractor implements ClickElementInteractor
	{
		private ExpanderControl control;
		
		private ExpanderInteractor()
		{
		}
		
		
		@Override
		public boolean testClickEvent(PointerInputElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(PointerInputElement element, PointerButtonClickedEvent event)
		{
			control.toggle();
			return true;
		}
	}
	
	

	public static class ExpanderControl extends Control
	{
		private DPBin element;
		private Pres expanded, contracted;
		private boolean currentState;
		private ExpanderListener listener;
		
		
		protected ExpanderControl(PresentationContext ctx, StyleValues style, DPBin element, Pres expanded, Pres contracted, boolean initialState, ExpanderListener listener)
		{
			super( ctx, style );
			this.element = element;
			this.expanded = expanded;
			this.contracted = contracted;
			currentState = initialState;
			this.listener = listener;
		}
		
		
		
		
		@Override
		public DPElement getElement()
		{
			return element;
		}
		
		
		
		private void toggle()
		{
			setState( !currentState );
		}
		
		
		public boolean getState()
		{
			return currentState;
		}
		
		public void setState(boolean state)
		{
			if ( state != currentState )
			{
				currentState = state;
				if ( currentState )
				{
					element.setChild( expanded.present( ctx, style ) );
				}
				else
				{
					element.setChild( contracted.present( ctx, style ) );
				}
				if ( listener != null )
				{
					listener.onExpander( this, currentState );
				}
			}
		}
	}

	
	
	

	private Pres header, contents;
	private boolean initialState;
	private ExpanderListener listener;
	
	
	
	
	public Expander(Object header, Object contents, boolean initialState, ExpanderListener listener)
	{
		this.header = coerce( header );
		this.contents = coerce( contents );
		this.initialState = initialState;
		this.listener = listener;
	}
	
	public Expander(Object header, Object contents, boolean initialState)
	{
		this( header, contents, initialState, null );
	}
	
	public Expander(Object header, Object contents, ExpanderListener listener)
	{
		this( header, contents, false, listener );
	}
	
	public Expander(Object header, Object contents)
	{
		this( header, contents, false, null );
	}
	
	
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useExpanderAttrs( style );
		
		StyleSheet arrowStyle = StyleSheet.instance.withAttr( Primitive.shapePainter, style.get( Controls.expanderHeaderArrowPainter, Painter.class ) );
		double arrowSize = style.get( Controls.expanderHeaderArrowSize, Double.class );
		double padding = style.get( Controls.expanderPadding, Double.class );
		Pres expandedArrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.DOWN, arrowSize ) );
		Pres contractedArrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.RIGHT, arrowSize ) );
		
		ExpanderInteractor headerInteractor = new ExpanderInteractor();
		
		StyleSheet headerStyle = StyleSheet.instance.withAttr( Primitive.rowSpacing, style.get( Controls.expanderHeaderContentsSpacing, Double.class ) );
		
		Pres expandedHeader = headerStyle.applyTo( new Row( new Pres[] { expandedArrow.alignVCentre(), header.alignHExpand() } ) ).withElementInteractor( headerInteractor );
		Pres contractedHeader = headerStyle.applyTo( new Row( new Pres[] { contractedArrow.alignVCentre(), header.alignHExpand() } ) ).withElementInteractor( headerInteractor );
		
		
		Pres expanded = new Column( new Pres[] { expandedHeader, contents.padX( padding ) } );
		Pres contracted = contractedHeader;
		
		Pres expander = new Bin( initialState  ?  expanded  :  contracted );
		DPBin expanderElement = (DPBin)expander.present( ctx, usedStyle );
		
		ExpanderControl control = new ExpanderControl( ctx, usedStyle, expanderElement, expanded, contracted, initialState, listener );
		headerInteractor.control = control;
		return control;
	}
}
