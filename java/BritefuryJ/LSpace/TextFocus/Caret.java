//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace.TextFocus;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.KeyEvent;

import BritefuryJ.LSpace.LSContentLeaf;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSegment;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.Focus.SelectionPoint;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.Marker.MarkerListener;
import BritefuryJ.Math.Point2;
import BritefuryJ.Util.AnimUtils;

public class Caret extends Target implements MarkerListener
{
	private static final double BLINK_TIME = 0.5;
	
	
	public static boolean blinkingEnabled = true;
	
	
	protected Marker marker;
	protected LSElement grabElement = null;
	
	
	
	public Caret()
	{
		marker = new Marker();
		marker.addMarkerListener( this );
	}
	
	
	@Override
	public void draw(Graphics2D graphics)
	{
		if ( isValid() )
		{
			LSContentLeafEditable element = getElement();
			
			if ( element != null )
			{
				Paint prevPaint = graphics.getPaint();

				if ( blinkingEnabled )
				{
					double time = System.nanoTime() * 1.0e-9;
					float alpha = (float)AnimUtils.scurveSeesaw( time, BLINK_TIME );
					graphics.setPaint( new Color( 0.0f, 0.0f, 1.0f, alpha ) );
				}
				else
				{
					graphics.setPaint( new Color( 0.0f, 0.0f, 1.0f ) );
				}
				
				element.drawCaret( graphics, this );
				graphics.setPaint( prevPaint );
			}
		}
	}
	
	@Override
	public boolean isAnimated()
	{
		return blinkingEnabled;
	}
	
	
	
	@Override
	public LSContentLeafEditable getKeyboardInputElement()
	{
		return marker.getElement();
	}
	
	
	@Override
	public boolean isEditable()
	{
		return isValid()  &&  marker.getElement().isEditable();
	}
	
	
	@Override
	public SelectionPoint createSelectionPoint()
	{
		if ( isValid() )
		{
			return new TextSelectionPoint( marker.copy() );
		}
		else
		{
			return null;
		}
	}
	

	
	@Override
	public boolean isValid()
	{
		if ( marker != null )
		{
			return marker.isValid();
		}
		else
		{
			return false;
		}
	}
	
	
	
	@Override
	public boolean onContentKeyPress(KeyEvent event)
	{
		LSContentLeafEditable leaf = getElement();
		if ( leaf != null  &&  leaf.isEditable() )
		{
			return leaf.onContentKeyPress( this, event );
		}
		return false;
	}

	@Override
	public boolean onContentKeyRelease(KeyEvent event)
	{
		LSContentLeafEditable leaf = getElement();
		if ( leaf != null  &&  leaf.isEditable() )
		{
			return leaf.onContentKeyRelease( this, event );
		}
		return false;
	}

	@Override
	public boolean onContentKeyTyped(KeyEvent event)
	{
		LSContentLeafEditable leaf = getElement();
		if ( leaf != null  &&  leaf.isEditable() )
		{
			return leaf.onContentKeyTyped( this, event );
		}
		return false;
	}



	public Marker getMarker()
	{
		return marker;
	}
	
	
	@Override
	public LSContentLeafEditable getElement()
	{
		return marker.getElement();
	}
	
	public int getPosition()
	{
		return marker.getPosition();
	}
	
	public int getIndex()
	{
		return marker.getIndex();
	}
	
	public Marker.Bias getBias()
	{
		return marker.getBias();
	}
	
	public int getPositionInSubtree(LSElement subtreeRoot)
	{
		return marker.getPositionInSubtree( subtreeRoot );
	}
	
	public int getClampedIndexInSubtree(LSElement subtreeRoot)
	{
		return marker.getClampedIndexInSubtree( subtreeRoot );
	}
	
	
	
	public void markerChanged(Marker m)
	{
		changed();
	}
	
	
	protected void changed()
	{
		notifyListenersOfChange();
	}
	
	
	@Override
	public void ensureVisible()
	{
		LSElement element = getElement();
		if ( element != null )
		{
			element.ensureVisible();
		}
	}
	
	
	
	//
	//
	// CARET GRAB METHODS
	//
	//
	
	public void grab(LSElement element)
	{
		if ( element != grabElement )
		{
			grabElement = element;
			
			if ( grabElement != null )
			{
				LSContentLeafEditable current = getElement(); 
				if ( current == null  ||  !current.isInSubtreeRootedAt( grabElement ) )
				{
					grabElement.moveMarkerToStart( marker );
				}
			}
		}
	}
	
	public void ungrab(LSElement element)
	{
		if ( element == grabElement )
		{
			grabElement = null;
		}
	}
	
	protected boolean isElementWithinGrabSubtree(LSElement element)
	{
		if ( grabElement == null )
		{
			return true;
		}
		else
		{
			return element.isInSubtreeRootedAt( grabElement );
		}
	}
	
	
	
	
	
	//
	//
	// CARET NAVIGATION METHODS
	//
	//
	
	@Override
	public void moveLeft()
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		if ( leaf.isMarkerAtStart( marker ) )
		{
			LSContentLeaf left = leaf.getContentLeafToLeft();
			boolean bSkippedLeaves = false;
			

			while ( left != null  &&  !left.isEditable() )
			{
				left = left.getContentLeafToLeft();
				bSkippedLeaves = true;
			}

			if ( left != null  &&  isElementWithinGrabSubtree( left ) )
			{
				LSContentLeafEditable editableLeft = (LSContentLeafEditable)left;
				if ( bSkippedLeaves )
				{
					editableLeft.moveMarkerToEnd( marker );
				}
				else
				{
					editableLeft.moveMarkerToEndMinusOne( marker );
				}
			}
		}
		else
		{
			leaf.moveMarker( marker, marker.getIndex() - 1, Marker.Bias.START );
		}
	}

	@Override
	public void moveRight()
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		if ( leaf.isMarkerAtEnd( marker ) )
		{
			LSContentLeaf right = leaf.getContentLeafToRight();
			boolean bSkippedLeaves = false;
			

			while ( right != null  &&  !right.isEditable() )
			{
				right = right.getContentLeafToRight();
				bSkippedLeaves = true;
			}

			if ( right != null  &&  isElementWithinGrabSubtree( right ) )
			{
				LSContentLeafEditable editableRight = (LSContentLeafEditable)right;
				if ( bSkippedLeaves )
				{
					editableRight.moveMarkerToStart( marker );
				}
				else
				{
					editableRight.moveMarkerToStartPlusOne( marker );
				}
			}
		}
		else
		{
			leaf.moveMarker( marker, marker.getIndex(), Marker.Bias.END );
		}
	}
	
	@Override
	public void moveUp()
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		Point2 cursorPos = leaf.getMarkerPosition( marker );
		LSContentLeafEditable above = leaf.getEditableContentLeafAbove( cursorPos );
		if ( above != null  &&  isElementWithinGrabSubtree( above ) )
		{
			Point2 cursorPosInAbove = leaf.getLocalPointRelativeTo( above, cursorPos );
			int contentPos = above.getMarkerPositonForPoint( cursorPosInAbove );
			above.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	@Override
	public void moveDown()
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		Point2 cursorPos = leaf.getMarkerPosition( marker );
		LSContentLeafEditable below = leaf.getEditableContentLeafBelow( cursorPos );
		if ( below != null  &&  isElementWithinGrabSubtree( below ) )
		{
			Point2 cursorPosInBelow = leaf.getLocalPointRelativeTo( below, cursorPos );
			int contentPos = below.getMarkerPositonForPoint( cursorPosInBelow );
			below.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	

	@Override
	public void moveToHome()
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		LSSegment segment = null;
		LSContentLeaf homeElement = null;
		segment = leaf.getSegment();
		homeElement = segment != null  ?  segment.getFirstEditableLeafInSubtree()  :  null;
		if ( segment != null  &&  leaf == homeElement  &&  leaf.isMarkerAtStart( marker ) )
		{
			segment = segment.getParent().getSegment();
			homeElement = segment != null  ?  segment.getFirstEditableLeafInSubtree()  :  null;
		}
		
		if ( homeElement != null )
		{
			if ( isElementWithinGrabSubtree( homeElement ) )
			{
				moveToStartOfElement( homeElement );
			}
			else
			{
				moveToStartOfElement( grabElement );
			}
		}
	}
	
	@Override
	public void moveToEnd()
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		LSSegment segment = null;
		LSContentLeaf endElement = null;
		segment = leaf.getSegment();
		endElement = segment != null  ?  segment.getLastEditableLeafInSubtree()  :  null;
		if ( segment != null  &&  leaf == endElement  &&  leaf.isMarkerAtEnd( marker ) )
		{
			segment = segment.getParent().getSegment();
			endElement = segment != null  ?  segment.getLastEditableLeafInSubtree()  :  null;
		}
		
		if ( endElement != null )
		{
			if ( isElementWithinGrabSubtree( endElement ) )
			{
				moveToEndOfElement( endElement );
			}
			else
			{
				moveToEndOfElement( grabElement );
			}
		}
	}
	
	
	public boolean moveTo(Marker m)
	{
		if ( isElementWithinGrabSubtree( m.getElement() ) )
		{
			marker.moveTo( m );
			return true;
		}
		else
		{
			return false;
		}
	}



	public void moveToStartOfElement(LSElement element)
	{
		LSContentLeafEditable leaf = element.getLeftEditableContentLeaf();
		if ( leaf != null  &&  leaf.isRealised() )
		{
			if ( leaf.isEditable() )
			{
				moveTo( leaf.markerAtStart() );
			}
			else
			{
				LSContentLeaf right = leaf.getNextLeaf( new LSElement.SubtreeElementFilter( element ), null, new LSContentLeaf.EditableLeafElementFilter() );
				if ( right != null  &&  right.isRealised() )
				{
					moveTo( right.markerAtStart() );
				}
			}
		}
	}
	
	
	public void moveToEndOfElement(LSElement element)
	{
		LSContentLeafEditable leaf = element.getRightEditableContentLeaf();
		if ( leaf != null  &&  leaf.isRealised() )
		{
			if ( leaf.isEditable() )
			{
				moveTo( leaf.markerAtEnd() );
			}
			else
			{
				LSContentLeaf left = leaf.getPreviousLeaf( new LSElement.SubtreeElementFilter( element ), null, new LSContentLeaf.EditableLeafElementFilter() );
				if ( left != null  &&  left.isRealised() )
				{
					moveTo( left.markerAtEnd() );
				}
			}
		}
	}
	
	
	public void moveToPositionAndBiasWithinSubtree(LSElement subtree, int newPosition, Marker.Bias newBias)
	{
		marker.moveToPositionAndBiasWithinSubtree( subtree, newPosition, newBias, new LSContentLeaf.EditableLeafElementFilter() );
	}


	public void moveToStartOfNextItem()
	{
		if ( marker.getBias() == Marker.Bias.END )
		{
			LSContentLeafEditable leaf = marker.getElement();
			
			if ( leaf.isMarkerAtEnd( marker ) )
			{
				LSContentLeaf right = leaf.getContentLeafToRight();
				
				while ( right != null  &&  !right.isEditable() )
				{
					right = right.getContentLeafToRight();
				}
	
				if ( right != null  &&  isElementWithinGrabSubtree( right ) )
				{
					LSContentLeafEditable editableRight = (LSContentLeafEditable)right;
					editableRight.moveMarkerToStart( marker );
				}
			}
			else
			{
				leaf.moveMarker( marker, marker.getIndex(), Marker.Bias.START );
			}
		}
	}
	
	
	
	public void makeCurrentTarget()
	{
		LSElement element = getElement();
		if ( element != null )
		{
			PresentationComponent.RootElement root = element.getRootElement();
			if ( root != null )
			{
				root.setCaretAsTarget();
			}
		}
	}
}
