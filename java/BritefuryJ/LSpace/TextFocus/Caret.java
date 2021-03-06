//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.TextFocus;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.Focus.SelectionPoint;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.Marker.Marker.CannotFindLeafInSubtreeException;
import BritefuryJ.LSpace.Marker.MarkerListener;
import BritefuryJ.LSpace.StyleParams.ContentLeafEditableStyleParams;
import BritefuryJ.Math.Point2;
import BritefuryJ.Util.AnimUtils;

public class Caret extends Target implements MarkerListener
{
	private static final double BLINK_TIME = 0.5;
	
	
	public static boolean blinkingEnabled = true;
	
	
	protected Marker marker;
	protected LSElement grabElement = null;
	protected LSContentLeafEditable currentLeaf = null;
	
	
	
	public Caret()
	{
		marker = new Marker();
		marker.addMarkerListener( this );
	}


	@Override
	public boolean drawWhenComponentNotFocused()
	{
		return false;
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
				
				ContentLeafEditableStyleParams elementStyle = (ContentLeafEditableStyleParams)element.getStyleParams();
				Color caretColour = elementStyle.getCaretColour();

				if ( blinkingEnabled )
				{
					double time = System.nanoTime() * 1.0e-9;
					float alpha = (float)AnimUtils.scurveSeesaw( time, BLINK_TIME );
					graphics.setPaint( new Color( caretColour.getRed(), caretColour.getGreen(), caretColour.getBlue(), (int)( alpha * 255.5 ) ) );
				}
				else
				{
					graphics.setPaint( caretColour );
				}
				
				element.drawCaret( graphics, marker );
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
	
	
	// Notify target that is is current
	public void notifyActivate()
	{
		super.notifyActivate();
		if ( isValid() )
		{
			LSContentLeafEditable caretLeaf = getElement();
			
			ArrayList<LSElement> path = caretLeaf.getElementPathFromRoot();
			for (LSElement e: path)
			{
				e.handleCaretEnter( this );
			}
			
			currentLeaf = caretLeaf;
		}
	}
	
	// Notify target that is is no longer current
	public void notifyDeactivate()
	{	
		if ( isValid() )
		{
			LSContentLeafEditable caretLeaf = getElement();
			
			ArrayList<LSElement> path = caretLeaf.getElementPathToRoot();
			for (LSElement e: path)
			{
				e.handleCaretLeave( this );
			}
			
			currentLeaf = null;
		}
		super.notifyDeactivate();
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
		if ( event.getKeyCode() == KeyEvent.VK_BACK_SPACE )
		{
			return handleBackspace();
		}
		else if ( event.getKeyCode() == KeyEvent.VK_DELETE )
		{
			return handleDelete();
		}
		else
		{
			LSContentLeafEditable leaf = getElement();
			if ( leaf != null  &&  leaf.isEditable() )
			{
				return leaf.onContentKeyPress( this, event );
			}
			return false;
		}
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
		notifyModified();
		
		handleChange();
	}
	
	
	private void handleChange()
	{
		if ( active  &&  isValid() )
		{
			if ( getElement().getRootElement().getTarget() != this )
			{
				throw new RuntimeException( "Caret is active, but is not the current target" );
			}

			
			LSContentLeafEditable caretLeaf = getElement();
			
			if ( caretLeaf != currentLeaf )
			{
				ArrayList<LSElement> prevPath = null, curPath = null;
				if ( currentLeaf != null )
				{
					prevPath = currentLeaf.getElementPathToRoot();
				}
				else
				{
					prevPath = new ArrayList<LSElement>();
				}
				
				if ( caretLeaf != null )
				{
					curPath = caretLeaf.getElementPathToRoot();
				}
				else
				{
					curPath = new ArrayList<LSElement>();
				}
				

				int prevPathDivergeIndex = prevPath.size() - 1, curPathDivergeIndex = curPath.size() - 1;
				for (int i = prevPath.size() - 1, j = curPath.size() - 1; i >= 0  &&  j >= 0;  i--, j--)
				{
					LSElement prev = prevPath.get( i ), cur = curPath.get( j );
					if ( prev != cur )
					{
						// Found indices where paths diverge
						prevPathDivergeIndex = i;
						curPathDivergeIndex = j;
						
						break;
					}
				}
				
				
				// Send leave events
				for (int x = 0; x <= prevPathDivergeIndex; x++)
				{
					prevPath.get( x ).handleCaretLeave( this );
				}
				
				currentLeaf = caretLeaf;

				for (int x = curPathDivergeIndex; x >= 0; x--)
				{
					curPath.get( x ).handleCaretEnter( this );
				}
			}
			
			
			getElement().getRootElement().queueFullRedraw();
		}
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
					try
					{
						marker.moveToStartOf( grabElement, true );
					}
					catch (CannotFindLeafInSubtreeException e)
					{
						System.err.println( "Caret.grab(): could not find element to move caret into" );
					}
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
		
		if ( marker.isAtStartOf( leaf ) )
		{
			LSContentLeaf prev = leaf;
			LSContentLeaf left = leaf.getContentLeafToLeft();
			boolean bSkippedLeaves = false;
			

			while ( left != null  &&  ( !left.isEditable()  ||  left.isCaretSlot() ) )
			{
				if ( left.isCaretSlot()  &&  ((LSCaretSlot)left).shouldCaretStopHere( prev ) )
				{
					break;
				}
				prev = left;
				left = left.getContentLeafToLeft();
				bSkippedLeaves = true;
			}

			if ( left != null  &&  isElementWithinGrabSubtree( left ) )
			{
				LSContentLeafEditable editableLeft = (LSContentLeafEditable)left;
				if ( bSkippedLeaves )
				{
					marker.moveToEndOfLeaf( editableLeft );
				}
				else
				{
					marker.moveToEndOfLeafMinusOne( editableLeft );
				}
			}
		}
		else
		{
			marker.moveTo( leaf, marker.getIndex() - 1, Marker.Bias.START );
		}
	}

	@Override
	public void moveRight()
	{
		LSContentLeafEditable leaf = marker.getElement();
		
		if ( marker.isAtEndOf( leaf ) )
		{
			LSContentLeaf prev = leaf;
			LSContentLeaf right = leaf.getContentLeafToRight();
			boolean bSkippedLeaves = false;
			

			while ( right != null  &&  ( !right.isEditable()  ||  right.isCaretSlot() ) )
			{
				if ( right.isCaretSlot()  &&  ((LSCaretSlot)right).shouldCaretStopHere( prev ) )
				{
					break;
				}
				prev = right;
				right = right.getContentLeafToRight();
				bSkippedLeaves = true;
			}

			if ( right != null  &&  isElementWithinGrabSubtree( right ) )
			{
				LSContentLeafEditable editableRight = (LSContentLeafEditable)right;
				if ( bSkippedLeaves )
				{
					marker.moveToStartOfLeaf( editableRight );
				}
				else
				{
					marker.moveToStartOfLeafPlusOne( editableRight );
				}
			}
		}
		else
		{
			marker.moveTo( leaf, marker.getIndex(), Marker.Bias.END );
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
			int contentPos = above.getMarkerPositionForPoint(cursorPosInAbove);
			marker.moveTo( above, contentPos, Marker.Bias.START );
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
			int contentPos = below.getMarkerPositionForPoint(cursorPosInBelow);
			marker.moveTo( below, contentPos, Marker.Bias.START );
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
		if ( segment != null  &&  leaf == homeElement  &&  marker.isAtStartOf( leaf ) )
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
		if ( segment != null  &&  leaf == endElement  &&  marker.isAtEndOf( leaf ) )
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
		if ( m.isValid()  &&  isElementWithinGrabSubtree( m.getElement() ) )
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
				moveTo( Marker.atStartOfLeaf( leaf ) );
			}
			else
			{
				LSContentLeafEditable right = (LSContentLeafEditable)leaf.getNextLeaf( new LSElement.SubtreeElementFilter( element ), null, LSContentLeafEditable.editableRealisedLeafElementFilter );
				if ( right != null  &&  right.isRealised() )
				{
					moveTo( Marker.atStartOfLeaf( right ) );
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
				moveTo( Marker.atEndOfLeaf( leaf ) );
			}
			else
			{
				LSContentLeafEditable left = (LSContentLeafEditable)leaf.getPreviousLeaf( new LSElement.SubtreeElementFilter( element ), null, LSContentLeafEditable.editableRealisedLeafElementFilter );
				if ( left != null  &&  left.isRealised() )
				{
					moveTo( Marker.atEndOfLeaf( left ) );
				}
			}
		}
	}
	
	
	public void moveToPositionAndBiasWithinSubtree(LSElement subtree, AbstractTextRepresentationManager textRepManager, int newPosition, Marker.Bias newBias)
	{
		marker.moveToPositionAndBiasWithinSubtree( subtree, textRepManager, newPosition, newBias, LSContentLeafEditable.editableRealisedLeafElementFilter );
	}


	public void moveToStartOfNextItem()
	{
		if ( marker.getBias() == Marker.Bias.END )
		{
			LSContentLeafEditable leaf = marker.getElement();
			
			if ( marker.isAtEndOf( leaf ) )
			{
				LSContentLeaf right = leaf.getContentLeafToRight();
				
				while ( right != null  &&  !right.isEditable() )
				{
					right = right.getContentLeafToRight();
				}
	
				if ( right != null  &&  isElementWithinGrabSubtree( right ) )
				{
					LSContentLeafEditable editableRight = (LSContentLeafEditable)right;
					marker.moveToStartOfLeaf( editableRight );
				}
			}
			else
			{
				marker.moveTo( leaf, marker.getIndex(), Marker.Bias.START );
			}
		}
	}
	
	
	
	public void makeCurrentTarget()
	{
		LSElement element = getElement();
		if ( element != null )
		{
			LSRootElement root = element.getRootElement();
			if ( root != null )
			{
				root.setCaretAsTarget();
			}
		}
	}
	
	

	//
	// Deleting content
	//

	protected boolean handleBackspace()
	{
		LSContentLeafEditable start = getElement();
		
		if ( getMarker().isAtStartOf( start ) )
		{
			LSContentLeaf prev = start;
			LSContentLeaf left = start.getContentLeafToLeft();
			if ( left == null )
			{
				return false;
			}
			else
			{
				boolean contentDeleted = false;
				LSRegion region = start.getRegion();
				
				// Delete the textual content within any non-editable or empty elements. Stop when something has been deleted.
				while ( !left.isEditable()  ||  left.getTextRepresentationLength() == 0 )
				{
					// Delete the content
					boolean deleted = left.deleteText();
					contentDeleted |= deleted;
					
					if ( !start.isRealised() )
					{
						// Bail out if a response to the deletion of the text is the start element becoming unrealised
						return true;
					}
					
					prev = left;
					left = left.getContentLeafToLeft();
					if ( left == null  ||  left.getRegion() != region )
					{
						// No leaf to left, or leaf is in a different region; bail out
						return true;
					}

					if ( deleted )
					{
						break;
					}
				}
				
				// Jump over non-editable elements until we reach an editable one
				while ( !left.isEditable()  ||  left.isCaretSlot() )
				{
					if ( left.isCaretSlot()  &&  ((LSCaretSlot)left).shouldCaretStopHere( prev ) )
					{
						break;
					}
					prev = left;
					left = left.getContentLeafToLeft();
					if ( left == null  ||  left.getRegion() != region )
					{
						// No leaf to left, or leaf is in a different region; bail out
						return false;
					}
				}
				
				
				LSContentLeafEditable editableLeft = (LSContentLeafEditable)left;
				if ( moveTo( Marker.atEndOfLeaf( editableLeft ) ) )
				{
					if ( !contentDeleted )
					{
						left.removeTextFromEnd( 1 );
					}
					makeCurrentTarget();
				}
				return true;
			}
		}
		else
		{
			if ( start.isEditable() )
			{
				start.removeText( getMarker().getClampedIndex() - 1, 1 );
			}
			return true;
		}
	}
	
	protected boolean handleDelete()
	{
		LSContentLeafEditable start = getElement();
		
		if ( getMarker().isAtEndOf( start ) )
		{
			LSContentLeaf prev = start;
			LSContentLeaf right = start.getContentLeafToRight();
			if ( right == null )
			{
				return false;
			}
			else
			{
				boolean contentDeleted = false;
				LSRegion region = start.getRegion();

				// Delete the textual content within any non-editable or empty elements. Stop when something has been deleted.
				while ( !right.isEditable()  ||  right.getTextRepresentationLength() == 0 )
				{
					// Delete the content
					boolean deleted = right.deleteText();
					contentDeleted |= deleted;
					
					if ( !start.isRealised() )
					{
						// Bail out if a response to the deletion of the text is the start element becoming unrealised
						return true;
					}
					
					prev = right;
					right = right.getContentLeafToLeft();
					if ( right == null  ||  right.getRegion() != region )
					{
						// No leaf to right, or leaf is in a different region; bail out
						return true;
					}

					if ( deleted )
					{
						break;
					}
				}
				
				// Jump over non-editable elements until we reach over an editable one
				while ( !right.isEditable()  ||  right.isCaretSlot()  ||  right.getTextRepresentationLength() == 0 )
				{
					if ( right.isCaretSlot()  &&  ((LSCaretSlot)right).shouldCaretStopHere( prev ) )
					{
						break;
					}
					prev = right;
					right = right.getContentLeafToRight();
					if ( right == null  ||  right.getRegion() != region )
					{
						// No leaf to right, or leaf is in a different region; bail out
						return false;
					}
				}

				LSContentLeafEditable editableRight = (LSContentLeafEditable)right; 
				if ( moveTo( Marker.atStartOfLeaf( editableRight ) ) )
				{
					if ( !contentDeleted )
					{
						right.removeTextFromStart( 1 );
					}
					makeCurrentTarget();
				}
				return true;
			}
		}
		else
		{
			if ( start.isEditable() )
			{
				start.removeText( getMarker(), 1 );
			}
			return true;
		}
	}
	
}
