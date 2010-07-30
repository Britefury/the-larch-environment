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
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Spacer;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Viewport;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.DocPresent.Util.Range;

abstract class AbstractScrolledViewport extends ControlPres
{
	public class ScrolledViewportControl extends Control
	{
		private DPViewport viewport;
		private DPElement element;
		private ScrollBar.ScrollBarControl xScrollBar, yScrollBar;
		private Range xRange, yRange;
		
		
		
		public ScrolledViewportControl(PresentationContext ctx, DPViewport viewport, DPElement element,
				ScrollBar.ScrollBarControl xScrollBar, ScrollBar.ScrollBarControl yScrollBar, Range xRange, Range yRange)
		{
			super( ctx );
			
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
	
	
	public AbstractScrolledViewport(Pres child, PersistentState state)
	{
		this.child = child;
		this.state = state;
	}

	
	@Override
	public Control createControl(PresentationContext ctx)
	{
		StyleValues style = ctx.getStyle();
		double scrollBarSize = style.get( Controls.scrollBarSize, Double.class );

		Range xRange = new Range( 0.0, 1.0, 0.0, 1.0, 0.1 );
		Range yRange = new Range( 0.0, 1.0, 0.0, 1.0, 0.1 );
		
		Viewport viewport = new Viewport( child, xRange, yRange, state );
		DPViewport viewportElement = (DPViewport)viewport.present( ctx );
		
		Pres bin = createViewportBin( viewportElement.alignHExpand().alignVExpand() );
		HScrollBar xScroll = new HScrollBar( xRange );
		ScrollBar.ScrollBarControl xScrollCtl = (ScrollBarControl)xScroll.createControl( ctx );
		VScrollBar yScroll = new VScrollBar( yRange );
		ScrollBar.ScrollBarControl yScrollCtl = (ScrollBarControl)yScroll.createControl( ctx );
		
		Pres hbox0 = new HBox( new Object[] { bin.alignHExpand().alignVExpand(), yScrollCtl.getElement().alignVExpand() } );
		Pres hbox1 = new HBox( new Object[] { xScrollCtl.getElement().alignHExpand(), new Spacer( scrollBarSize, scrollBarSize ) } );
		Pres vbox = new VBox( new Pres[] { hbox0.alignHExpand().alignVExpand(), hbox1.alignHExpand() } );
		DPElement element = vbox.present( ctx );
		
		return new ScrolledViewportControl( ctx, viewportElement, element, xScrollCtl, yScrollCtl, xRange, yRange );
	}



	protected abstract Pres createViewportBin(DPElement child);
}
