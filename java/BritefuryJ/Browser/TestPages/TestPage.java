//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.MotionElementInteractor;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class TestPage extends AbstractTestPage implements Presentable
{
	protected String getDescription()
	{
		return null;
	}
	
	protected abstract Pres createContents();
	
	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres linkHeader = TestsRootPage.createLinkHeader( TestsRootPage.LINKHEADER_SYSTEMPAGE );
		Pres title = new TitleBar( "Test page: " + getTitle() );
		
		Pres head = new Head( new Pres[] { linkHeader, title } );

		ArrayList<Object> bodyChildren = new ArrayList<Object>();
		String description = getDescription();
		if ( description != null )
		{
			bodyChildren.add( staticStyle.applyTo( new NormalText( description ) ) );
		}
		bodyChildren.add( createContents() );
		Body body = new Body( bodyChildren );
		
		return new Page( new Object[] { head, body } );
	}



	private static class HighlightInsertionPointsInteractor implements ElementPainter, MotionElementInteractor
	{
		private HashMap<LSContainerSequence, InsertionPoint> elementToInsertionPoint = new HashMap<LSContainerSequence, InsertionPoint>();
		private Color highlightColour;



		public HighlightInsertionPointsInteractor(Color highlightColour)
		{
			this.highlightColour = new Color( highlightColour.getRed(), highlightColour.getGreen(), highlightColour.getBlue(), 192 );
		}


		public void drawBackground(LSElement element, Graphics2D graphics)
		{
		}

		public void draw(LSElement element, Graphics2D graphics)
		{
			LSContainerSequence container = findContainer( element );
			if ( container != null )
			{
				InsertionPoint ins = elementToInsertionPoint.get( container );

				if ( ins != null )
				{
					Paint oldPaint = graphics.getPaint();
					Stroke oldStroke = graphics.getStroke();

					graphics.setPaint( highlightColour );
					graphics.setStroke( new BasicStroke( 3.0f ) );

					String indexStr = Integer.toString( ins.getIndex() );
					graphics.draw( new Line2D.Double( ins.getStartPoint().x, ins.getStartPoint().y, ins.getEndPoint().x, ins.getEndPoint().y ) );
					graphics.drawString( indexStr, (float)ins.getStartPoint().x, (float)ins.getStartPoint().y );

					graphics.setPaint( oldPaint );
					graphics.setStroke( oldStroke );
				}
			}
		}


		public void pointerMotion(LSElement element, PointerMotionEvent event)
		{
			LSContainerSequence container = findContainer( element );
			if ( container != null )
			{
				Point2 localPos = event.transformed( container.getAncestorToLocalXform( element ) ).getLocalPointerPos();
				elementToInsertionPoint.put( container, container.getInsertionPointClosestToLocalPoint( localPos ) );
				container.queueFullRedraw();
			}
		}

		public void pointerLeaveIntoChild(LSElement element, PointerMotionEvent event)
		{
		}

		public void pointerEnterFromChild(LSElement element, PointerMotionEvent event)
		{
		}

		public void pointerEnter(LSElement element, PointerMotionEvent event)
		{
			LSContainerSequence container = findContainer( element );
			if ( container != null )
			{
				Point2 localPos = event.transformed( container.getAncestorToLocalXform( element ) ).getLocalPointerPos();
				elementToInsertionPoint.put( container, container.getInsertionPointClosestToLocalPoint( localPos ) );
				container.queueFullRedraw();
			}
		}

		public void pointerLeave(LSElement element, PointerMotionEvent event)
		{
			LSContainerSequence container = findContainer( element );
			if ( container != null )
			{
				elementToInsertionPoint.remove( container );
				container.queueFullRedraw();
			}
		}


		private LSContainerSequence findContainer(LSElement element)
		{
			while ( element instanceof LSBorder )
			{
				element = ((LSBorder)element).getChild();
			}
			if ( element instanceof LSContainerSequence )
			{
				return (LSContainerSequence)element;
			}
			else
			{
				return null;
			}
		}
	}



	protected Pres highlightInsertionPoints(Pres p)
	{
		return highlightInsertionPoints( p, new Color( 0.8f, 0.3f, 0.0f ) );
	}

	protected Pres highlightInsertionPoints(Pres p, Color highlightColour)
	{
		HighlightInsertionPointsInteractor interactor = new HighlightInsertionPointsInteractor( highlightColour );
		return p.withPainter( interactor ).withElementInteractor( interactor );
	}




	private static final StyleSheet staticStyle = StyleSheet.style( Primitive.editable.as( false ) );
}
