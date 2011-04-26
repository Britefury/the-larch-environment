//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Controls.ScrollBar.ScrollBarControl;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.Pres.Primitive.Viewport;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class AbstractScrolledViewport extends ControlPres
{
	public class ScrolledViewportControl extends Control
	{
		private DPViewport viewport;
		private DPElement element;
		private ScrollBar.ScrollBarControl xScrollBar, yScrollBar;
		private Range xRange, yRange;
		
		
		
		public ScrolledViewportControl(PresentationContext ctx, StyleValues style, DPViewport viewport, DPElement element,
				ScrollBar.ScrollBarControl xScrollBar, ScrollBar.ScrollBarControl yScrollBar, Range xRange, Range yRange)
		{
			super( ctx, style );
			
			this.viewport = viewport;
			this.element = element;
			this.xScrollBar = xScrollBar;
			this.yScrollBar = yScrollBar;
			this.xRange = xRange;
			this.yRange = yRange;
		}
		
		
		public DPViewport getViewportElement()
		{
			return viewport;
		}
		
		@Override
		public DPElement getElement()
		{
			return element;
		}
		
		public ScrollBar.ScrollBarControl getXScrollBar()
		{
			return xScrollBar;
		}
	
		public ScrollBar.ScrollBarControl getYScrollBar()
		{
			return yScrollBar;
		}
	
		public Range getXRange()
		{
			return xRange;
		}
	
		public Range getYRange()
		{
			return yRange;
		}
	}
	
	

	private Pres child;
	private PersistentState state;
	
	
	public AbstractScrolledViewport(Object child, PersistentState state)
	{
		this.child = coerce( child );
		this.state = state;
	}

	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		double scrollBarSize = style.get( Controls.scrollBarSize, Double.class );

		Range xRange = new Range( 0.0, 1.0, 0.0, 1.0, 0.1 );
		Range yRange = new Range( 0.0, 1.0, 0.0, 1.0, 0.1 );
		
		Viewport viewport = new Viewport( child, xRange, yRange, state );
		DPViewport viewportElement = (DPViewport)viewport.alignHExpand().alignVExpand().present( ctx, style );
		
		Pres bin = createViewportBin( viewportElement );
		HScrollBar xScroll = new HScrollBar( xRange );
		ScrollBar.ScrollBarControl xScrollCtl = (ScrollBarControl)xScroll.createControl( ctx, style.alignHExpand() );
		VScrollBar yScroll = new VScrollBar( yRange );
		ScrollBar.ScrollBarControl yScrollCtl = (ScrollBarControl)yScroll.createControl( ctx, style.alignHPack().alignVExpand() );
		
		Pres row0 = new Row( new Object[] { bin, yScrollCtl.getElement() } );
		Pres row1 = new Row( new Object[] { xScrollCtl.getElement(), new Spacer( scrollBarSize, scrollBarSize ).alignHPack() } ).alignVRefY();
		Pres col = new Column( new Pres[] { row0, row1 } ).alignHExpand().alignVExpand();
		DPElement element = col.present( ctx, style );
		
		return new ScrolledViewportControl( ctx, style, viewportElement, element, xScrollCtl, yScrollCtl, xRange, yRange );
	}



	protected abstract Pres createViewportBin(DPElement child);
}
