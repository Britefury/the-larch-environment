//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.List;

import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRow;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class TabbedBox extends ControlPres
{
	public static interface TabbedBoxListener
	{
		public void onTab(TabbedBoxControl expander, int tab);
	}


	public static class TabbedBoxControl extends Control
	{
		private LSElement element;
		private LSBin contentsElement;
		
		private Pres tabContents[];
		private int currentTab;
		private TabbedBoxListener listener;
		
		
		protected TabbedBoxControl(PresentationContext ctx, StyleValues style, LSElement element, LSBin contentsElement, Pres tabContents[], int initialTab, TabbedBoxListener listener)
		{
			super( ctx, style );
			this.element = element;
			this.contentsElement = contentsElement;

			this.tabContents = tabContents;
			currentTab = -1;
			setTab( initialTab );
			this.listener = listener;
		}
		
		
		
		
		@Override
		public LSElement getElement()
		{
			return element;
		}
		
		
		public int getTab()
		{
			return currentTab;
		}
		
		public void setTab(int tab)
		{
			if ( tab != currentTab )
			{
				currentTab = tab;
				
				if ( tabContents.length > 0 )
				{
					Pres contents = tabContents[currentTab];
					contentsElement.setChild( contents.present( ctx, style ).layoutWrap( style.get( Primitive.hAlign, HAlignment.class ), style.get( Primitive.vAlign, VAlignment.class ) ) );
					if ( listener != null )
					{
						listener.onTab( this, currentTab );
					}
				}
			}
		}
	}

	
	
	protected static class TabbedBoxHeaderInteractor implements ClickElementInteractor, ElementPainter
	{
		TabbedBoxControl control;
		double spacing, rounding, tabRounding;
		Paint fillPaint, outlinePaint;
		Paint inactiveFillPaint, inactiveOutlinePaint;
		
		protected TabbedBoxHeaderInteractor(double spacing, double rounding, double tabRounding, Paint fillPaint, Paint outlinePaint,
				Paint inactiveFillPaint, Paint inactiveOutlinePaint)
		{
			this.spacing = spacing;
			this.rounding = rounding;
			this.tabRounding = tabRounding;
			this.fillPaint = fillPaint;
			this.outlinePaint = outlinePaint;
			this.inactiveFillPaint = inactiveFillPaint;
			this.inactiveOutlinePaint = inactiveOutlinePaint;
		}
		
		
		@Override
		public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
		{
			LSRow header = (LSRow)element;
			Point2 clickPos = event.getLocalPointerPos();
			List<LSElement> children = header.getChildren();
			int i = 0;
			// Ignore first and last elements - they are for padding
			for (LSElement child: children.subList( 1, children.size() - 1 ))
			{
				Point2 clickPosInChildSpace = child.getAncestorToLocalXform( element ).transform( clickPos );
				if ( child.containsLocalSpacePoint( clickPosInChildSpace ) )
				{
					control.setTab( i );
					return true;
				}
				i++;
			}
			return false;
		}

		
		private void addChildPath(Path2D.Double path, LSElement child, Vector2 size, boolean start)
		{
			Point2 childPos = child.getPositionInParentSpace();
			Vector2 childSize = child.getActualSizeInParentSpace();

			// Start of child
			if ( start )
			{
				path.moveTo( childPos.x, size.y );
			}
			else
			{
				path.lineTo( childPos.x, size.y );
			}
			// Top left of child
			path.append( new Arc2D.Double( childPos.x, spacing, tabRounding * 2.0, tabRounding * 2.0, 180.0, -90.0, Arc2D.OPEN ), true );
			// Top right of child
			path.append( new Arc2D.Double( childPos.x + childSize.x - tabRounding * 2.0, spacing, tabRounding * 2.0, tabRounding * 2.0, 90.0, -90.0, Arc2D.OPEN ), true );
			// End of child
			path.lineTo( childPos.x + childSize.x, size.y );
		}

		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
			Vector2 size = element.getActualSize();
			LSRow header = (LSRow)element;
			
			List<LSElement> children = header.getChildren();
			LSElement selectedChild = children.size() > 2  ?  children.get( control.currentTab + 1 )  :  null;
			
			// Path is going counter clock-wise
			Path2D.Double headerShape = new Path2D.Double();

			// Start at the top right corner, with a circle for the rounding
			headerShape.append( new Arc2D.Double( size.x - rounding * 2.0, 0.0, rounding * 2.0, rounding * 2.0, 0.0, 90.0, Arc2D.OPEN ), false );
			// A rounding circle at the top left
			headerShape.append( new Arc2D.Double( 0.0, 0.0, rounding * 2.0, rounding * 2.0, 90.0, 90.0, Arc2D.OPEN ), true );
			// Point at the bottom left
			headerShape.lineTo( 0.0, size.y );
			// Selected child
			if ( selectedChild!= null )
			{
				addChildPath( headerShape, selectedChild, size, false );
			}
			// Point at bottom right
			headerShape.lineTo( size.x, size.y );
			
			headerShape.closePath();
			
			Stroke prevStroke = graphics.getStroke();
			Paint prevPaint = graphics.getPaint();
			
			graphics.setStroke( new BasicStroke( 1.0f ) );

			// Fill the header
			graphics.setPaint( fillPaint );
			graphics.fill( headerShape );
			
			// Draw tabs for the other children
			// Ignore first and last elements - they are for padding
			for (LSElement child: children.subList( 1, children.size() - 1 ))
			{
				if ( child != selectedChild )
				{
					Path2D.Double childTabShape = new Path2D.Double();
					
					addChildPath( childTabShape, child, size, true );
					
					graphics.setPaint( inactiveFillPaint );
					graphics.fill( childTabShape );
					graphics.setPaint( inactiveOutlinePaint );
					graphics.draw( childTabShape );
				}
			}

			// Now draw the header outline
			graphics.setPaint( outlinePaint );
			graphics.draw( headerShape );

			// Restore
			graphics.setStroke( prevStroke );
			graphics.setPaint( prevPaint );
		}


		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
		}
	}




	
	protected static class TabbedBoxContentBoxPainter implements ElementPainter
	{
		double rounding;
		Paint outlinePaint;
		
		protected TabbedBoxContentBoxPainter(double rounding, Paint outlinePaint)
		{
			this.rounding = rounding;
			this.outlinePaint = outlinePaint;
		}
		
		
		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
			Vector2 size = element.getActualSize();
			
			Path2D.Double path = new Path2D.Double();
			path.moveTo( 0.0, 0.0 );
			path.append( new Arc2D.Double( 0.0, size.y - rounding * 2.0, rounding * 2.0, rounding * 2.0, 180.0, 90.0, Arc2D.OPEN ), true );
			path.append( new Arc2D.Double( size.x - rounding * 2.0, size.y - rounding * 2.0, rounding * 2.0, rounding * 2.0, 270.0, 90.0, Arc2D.OPEN ), true );
			path.lineTo( size.x, 0.0 );

			Stroke prevStroke = graphics.getStroke();
			Paint prevPaint = graphics.getPaint();
			
			graphics.setStroke( new BasicStroke( 1.0f ) );
			graphics.setPaint( outlinePaint );
			graphics.draw( path );
			
			graphics.setStroke( prevStroke );
			graphics.setPaint( prevPaint );
		}


		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
		}
	}




	
	private Pres tabs[][];
	private int initialTab;
	private TabbedBoxListener listener;

	
	public TabbedBox(Object tabs[][], int initialTab, TabbedBoxListener listener)
	{
		super();

		this.tabs = new Pres[tabs.length][];
		int i = 0;
		for (Object tab[]: tabs)
		{
			this.tabs[i] = new Pres[2];
			this.tabs[i][0] = Pres.coerce( tab[0] );
			this.tabs[i][1] = Pres.coerce( tab[1] );
			i++;
		}
		this.initialTab = initialTab;
		this.listener = listener;
	}

	public TabbedBox(Object tabs[][], TabbedBoxListener listener)
	{
		this( tabs, 0, listener );
	}




	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useTabsAttrs( style );
		
		double contentsPadding = style.get( Controls.tabbedBoxContentsPadding, Double.class );
		double tabPadding = style.get( Controls.tabbedBoxTabPadding, Double.class );
		double headerSpacing = style.get( Controls.tabbedBoxHeaderSpacing, Double.class );
		double rounding = style.get( Controls.tabbedBoxRounding, Double.class );
		double headerTabRounding = style.get( Controls.tabbedBoxHeaderTabRounding, Double.class );
		Paint headerFillPaint = style.get( Controls.tabbedBoxHeaderFillPaint, Paint.class );
		Paint headerOutlinePaint = style.get( Controls.tabbedBoxHeaderOutlinePaint, Paint.class );
		Paint inactiveFillPaint = style.get( Controls.tabbedBoxHeaderInactiveTabFillPaint, Paint.class );
		Paint inactiveOutlinePaint = style.get( Controls.tabbedBoxHeaderInactiveTabOutlinePaint, Paint.class );

		
		Pres headers[] = new Pres[tabs.length+2];
		Pres contents[] = new Pres[tabs.length];
		for (int i = 0; i < tabs.length; i++)
		{
			headers[i+1] = new Bin( tabs[i][0].pad( tabPadding, tabPadding, headerSpacing + tabPadding, 0.0 ) ).alignHPack();
			contents[i] = tabs[i][1];
		}
		// A blank element at each end, in order to add the relevant amount of space at each end
		headers[0] = headers[tabs.length+1] = new Blank();

		
		// Build the header
		TabbedBoxHeaderInteractor headerInteractor = new TabbedBoxHeaderInteractor( headerSpacing, rounding, headerTabRounding, headerFillPaint, headerOutlinePaint,
				inactiveFillPaint, inactiveOutlinePaint );
		StyleSheet rowStyle = StyleSheet.style( Primitive.rowSpacing.as( headerSpacing ) );
		Pres header = rowStyle.applyTo( new Row( headers ) ).withElementInteractor( headerInteractor ).withPainter( headerInteractor ).alignHExpand();
		
		
		
		// Contents
		TabbedBoxContentBoxPainter contentBoxPainter = new TabbedBoxContentBoxPainter( rounding, headerOutlinePaint );
		Pres contentsBox = new Bin( new Blank() ).alignHExpand().alignVExpand();
		LSBin contentsElement = (LSBin)contentsBox.present( ctx, usedStyle );
		Pres contentsPadded = Pres.coerce( contentsElement ).pad( contentsPadding, contentsPadding ).withPainter( contentBoxPainter ).alignHExpand().alignVExpand();
		LSElement contentsPaddedElement = contentsPadded.present( ctx, usedStyle );
		
		
		// Tabs
		Pres tabs = new Column( new Object[] { header, contentsPaddedElement } );
		LSElement element = tabs.present( ctx, usedStyle );
		
		
		// Control
		TabbedBoxControl control = new TabbedBoxControl( ctx, usedStyle, element, contentsElement, contents, initialTab, listener );
		headerInteractor.control = control;
		return control;
	}
}
