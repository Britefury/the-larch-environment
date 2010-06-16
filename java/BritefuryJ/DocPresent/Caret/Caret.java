//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Caret;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Marker.MarkerListener;
import BritefuryJ.Math.Point2;

public class Caret implements MarkerListener
{
	protected Marker marker;
	protected CaretListener listener;
	protected DPElement grabElement = null;
	
	
	
	public Caret()
	{
		marker = new Marker();
		marker.addMarkerListener( this );
	}
	
	
	public void setCaretListener(CaretListener listener)
	{
		this.listener = listener;
	}
	
	
	public Marker getMarker()
	{
		return marker;
	}
	
	
	public DPContentLeafEditable getElement()
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
	
	public int getPositionInSubtree(DPElement subtreeRoot)
	{
		return marker.getPositionInSubtree( subtreeRoot );
	}
	
	public int getClampedIndexInSubtree(DPElement subtreeRoot)
	{
		return marker.getClampedIndexInSubtree( subtreeRoot );
	}
	
	
	
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
	
	
	
	public void markerChanged(Marker m)
	{
		changed();
	}
	
	
	protected void changed()
	{
		if ( listener != null )
		{
			listener.caretChanged( this );
		}
	}
	
	
	public void ensureVisible()
	{
		DPElement element = getElement();
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
	
	public void grab(DPElement element)
	{
		if ( element != grabElement )
		{
			grabElement = element;
			
			if ( grabElement != null )
			{
				DPContentLeafEditable current = getElement(); 
				if ( current == null  ||  !current.isInSubtreeRootedAt( grabElement ) )
				{
					grabElement.moveMarkerToStart( marker );
				}
			}
		}
	}
	
	public void ungrab(DPElement element)
	{
		if ( element == grabElement )
		{
			grabElement = null;
		}
	}
	
	protected boolean isElementWithinGrabSubtree(DPElement element)
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
	
	public void moveLeft()
	{
		DPContentLeafEditable leaf = marker.getElement();
		
		if ( leaf.isMarkerAtStart( marker ) )
		{
			DPContentLeaf left = leaf.getContentLeafToLeft();
			boolean bSkippedLeaves = false;
			

			while ( left != null  &&  !left.isEditable() )
			{
				left = left.getContentLeafToLeft();
				bSkippedLeaves = true;
			}

			if ( left != null  &&  isElementWithinGrabSubtree( left ) )
			{
				DPContentLeafEditable editableLeft = (DPContentLeafEditable)left;
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



	public void moveRight()
	{
		DPContentLeafEditable leaf = marker.getElement();
		
		if ( leaf.isMarkerAtEnd( marker ) )
		{
			DPContentLeaf right = leaf.getContentLeafToRight();
			boolean bSkippedLeaves = false;
			

			while ( right != null  &&  !right.isEditable() )
			{
				right = right.getContentLeafToRight();
				bSkippedLeaves = true;
			}

			if ( right != null  &&  isElementWithinGrabSubtree( right ) )
			{
				DPContentLeafEditable editableRight = (DPContentLeafEditable)right;
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
	
	public void moveUp()
	{
		DPContentLeafEditable leaf = marker.getElement();
		
		Point2 cursorPos = leaf.getMarkerPosition( marker );
		DPContentLeafEditable above = leaf.getEditableContentLeafAbove( cursorPos );
		if ( above != null  &&  isElementWithinGrabSubtree( above ) )
		{
			Point2 cursorPosInAbove = leaf.getLocalPointRelativeTo( above, cursorPos );
			int contentPos = above.getMarkerPositonForPoint( cursorPosInAbove );
			above.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	public void moveDown()
	{
		DPContentLeafEditable leaf = marker.getElement();
		
		Point2 cursorPos = leaf.getMarkerPosition( marker );
		DPContentLeafEditable below = leaf.getEditableContentLeafBelow( cursorPos );
		if ( below != null  &&  isElementWithinGrabSubtree( below ) )
		{
			Point2 cursorPosInBelow = leaf.getLocalPointRelativeTo( below, cursorPos );
			int contentPos = below.getMarkerPositonForPoint( cursorPosInBelow );
			below.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	

	public void moveToHome()
	{
		DPContentLeafEditable leaf = marker.getElement();
		
		DPSegment segment = null;
		DPContentLeaf homeElement = null;
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
				homeElement.moveMarkerToStart( marker );
			}
			else
			{
				grabElement.moveMarkerToStart( marker );
			}
		}
	}
	
	public void moveToEnd()
	{
		DPContentLeafEditable leaf = marker.getElement();
		
		DPSegment segment = null;
		DPContentLeaf endElement = null;
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
				endElement.moveMarkerToEnd( marker );
			}
			else
			{
				grabElement.moveMarkerToEnd( marker );
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
}
