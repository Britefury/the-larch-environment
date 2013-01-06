//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace.Marker;

import java.util.ArrayList;

import BritefuryJ.LSpace.AbstractTextRepresentationManager;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSContentLeaf;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSElement.IsNotInSubtreeException;
import BritefuryJ.LSpace.TreeTraversal;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Util.HashUtils;

public class Marker implements Comparable<Marker>
{
	public static class InvalidMarkerPosition extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public InvalidMarkerPosition(String message)
		{
			super( message );
		}
	}
	
	public static class CannotFindLeafInSubtreeException extends Exception
	{
		static final long serialVersionUID = 0L;
	}

	
	public enum Bias { START, END }
	
	
	protected LSContentLeafEditable element;
	protected int position;
	protected Bias bias;
	protected ArrayList<MarkerListener> listeners;
	
	
	
	public Marker()
	{
		this.element = null;
		this.position = 0;
		this.bias = Bias.START;
	}
	
	public Marker(LSContentLeafEditable element, int position, Bias bias)
	{
		if ( !element.isRealised() )
		{
			throw new RuntimeException( "Cannot create a marker within unrealised element " + element );
		}
		
		checkPositionAndBias( element, position, bias );
		
		this.element = element;
		this.position = position;
		this.bias = bias;
		
		this.element.registerMarker( this );
	}
	
	
	public void addMarkerListener(MarkerListener listener)
	{
		if ( listeners == null )
		{
			listeners = new ArrayList<MarkerListener>();
		}
		listeners.add( listener );
	}
	
	public void removeMarkerListener(MarkerListener listener)
	{
		if ( listeners != null )
		{
			listeners.remove( listener );
			if ( listeners.isEmpty() )
			{
				listeners = null;
			}
		}
	}
	
	
	
	public boolean isValid()
	{
		return element != null  &&  element.isRealised();
	}
	

	
	public LSContentLeafEditable getElement()
	{
		return element;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public int getPositionInSubtree(LSElement subtreeRoot) throws IsNotInSubtreeException
	{
		if ( subtreeRoot == element )
		{
			return getPosition();
		}
		else
		{
			if ( subtreeRoot instanceof LSContainer )
			{
				LSContainer b = (LSContainer)subtreeRoot;
				if ( element != null  &&  element.isInSubtreeRootedAt( b ) )
				{
					return getPosition() + element.getTextRepresentationOffsetInSubtree( b );
				}
				else
				{
					throw new LSElement.IsNotInSubtreeException();
				}
			}
			else if ( subtreeRoot instanceof LSContentLeaf )
			{
				if ( element != null  &&  element == subtreeRoot )
				{
					return getPosition();
				}
				else
				{
					throw new LSElement.IsNotInSubtreeException();
				}
			}
			else
			{
				throw new LSElement.IsNotInSubtreeException();
			}
		}
	}

	public Bias getBias()
	{
		return bias;
	}
	
	
	public int getIndex()
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	public int getClampedIndex()
	{
		return Math.min( bias == Bias.END  ?  position + 1  :  position,  element.getMarkerRange() );
	}
	
	public int getClampedIndexInSubtree(LSElement subtreeRoot) throws IsNotInSubtreeException
	{
		int p = getPositionInSubtree( subtreeRoot );
		int clampedIndex = getClampedIndex();
		int offset = clampedIndex - position;
		return p + offset;
	}
	

	public void setPosition(int position)
	{
		this.position = position;
		changed();
	}
	
	public void setPositionAndBias(int position, Bias bias)
	{
		this.position = position;
		this.bias = bias;
		changed();
	}
	
	
	public void moveTo(LSContentLeafEditable element, int position, Bias bias)
	{
		if ( element != null )
		{
			if ( !element.isRealised() )
			{
				throw new RuntimeException( "Cannot move marker into unrealised leaf element " + element );
			}
			checkPositionAndBias( element, position, bias );
		}
		
		if ( this.element != null )
		{
			this.element.unregisterMarker( this );
		}
		
		this.element = element;
		this.position = position;
		this.bias = bias;
		
		if ( this.element != null )
		{
			this.element.registerMarker( this );
		}
		
		changed();
	}
	
	
	public void moveTo(Marker marker)
	{
		moveTo( marker.element, marker.position, marker.bias );
	}
	
	
	
	public void moveToStartOfLeaf(LSContentLeafEditable leaf)
	{
		moveTo( leaf, 0, Bias.START );
	}
	
	public void moveToStartOfLeafPlusOne(LSContentLeafEditable leaf)
	{
		moveTo( leaf, Math.min( 1, leaf.getMarkerRange() ), Bias.START );
	}
	
	public void moveToEndOfLeaf(LSContentLeafEditable leaf)
	{
		// We must ensure that an element with no content CANNOT have a marker given a bias of END, otherwise
		// empty text elements will consume a backspace character.
		int range = leaf.getMarkerRange();
		if ( range == 0 )
		{
			moveTo( leaf, 0, Bias.START );
		}
		else
		{
			moveTo( leaf, Math.max( range - 1, 0 ), Bias.END );
		}
	}
	
	public void moveToEndOfLeafMinusOne(LSContentLeafEditable leaf)
	{
		moveTo( leaf, Math.max( leaf.getMarkerRange() - 1, 0 ), Marker.Bias.START );
	}
	
	public void moveToPointInLeaf(LSContentLeafEditable leaf, Point2 localPos)
	{
		int markerPos = leaf.getMarkerPositonForPoint( localPos );
		moveTo( leaf, markerPos, Marker.Bias.START );
	}
	
	
	
	public void moveToStartOf(LSElement element, boolean editable) throws CannotFindLeafInSubtreeException
	{
		LSContentLeafEditable leaf = null;
		
		if ( element instanceof LSContentLeafEditable )
		{
			leaf = (LSContentLeafEditable)element;
		}
		else
		{
			leaf = (LSContentLeafEditable)TreeTraversal.findFirstElementInSubtree( element, LSElement.internalBranchChildrenFn,
					editable ? LSContentLeafEditable.editableRealisedLeafElementFilter : LSContentLeafEditable.selectableRealisedLeafElementFilter );
		}
		
		if ( leaf != null )
		{
			moveToStartOfLeaf( leaf );
		}
		else
		{
			throw new CannotFindLeafInSubtreeException();
		}
	}
	
	public void moveToStartOf(LSElement element, ElementFilter elementFilter) throws CannotFindLeafInSubtreeException
	{
		LSContentLeafEditable leaf = null;
		
		if ( element instanceof LSContentLeafEditable )
		{
			leaf = (LSContentLeafEditable)element;
		}
		else
		{
			leaf = (LSContentLeafEditable)TreeTraversal.findFirstElementInSubtree( element, LSElement.internalBranchChildrenFn, elementFilter );
		}
		
		if ( leaf != null )
		{
			moveToStartOfLeaf( leaf );
		}
		else
		{
			throw new CannotFindLeafInSubtreeException();
		}
	}
	
	public void moveToEndOf(LSElement element, boolean editable) throws CannotFindLeafInSubtreeException
	{
		LSContentLeafEditable leaf = null;
		
		if ( element instanceof LSContentLeafEditable )
		{
			leaf = (LSContentLeafEditable)element;
		}
		else
		{
			leaf = (LSContentLeafEditable)TreeTraversal.findLastElementInSubtree( element, LSElement.internalBranchChildrenFn,
					editable ? LSContentLeafEditable.editableRealisedLeafElementFilter : LSContentLeafEditable.selectableRealisedLeafElementFilter );
		}
		
		if ( leaf != null )
		{
			moveToEndOfLeaf( leaf );
		}
		else
		{
			throw new CannotFindLeafInSubtreeException();
		}
	}
	
	public void moveToEndOf(LSElement element, ElementFilter elementFilter) throws CannotFindLeafInSubtreeException
	{
		LSContentLeafEditable leaf = null;
		
		if ( element instanceof LSContentLeafEditable )
		{
			leaf = (LSContentLeafEditable)element;
		}
		else
		{
			leaf = (LSContentLeafEditable)TreeTraversal.findLastElementInSubtree( element, LSElement.internalBranchChildrenFn, elementFilter );
		}
		
		if ( leaf != null )
		{
			moveToEndOfLeaf( leaf );
		}
		else
		{
			throw new CannotFindLeafInSubtreeException();
		}
	}
	
	
	public void moveToPointIn(LSElement element, Point2 localPos, boolean editable) throws CannotFindLeafInSubtreeException
	{
		LSContentLeafEditable leaf = (LSContentLeafEditable)element.getLeafClosestToLocalPoint( localPos,
				editable ? LSContentLeafEditable.editableRealisedLeafElementFilter : LSContentLeafEditable.selectableRealisedLeafElementFilter );
		
		if ( leaf != null )
		{
			Xform2 x = leaf.getAncestorToLocalXform( element );
			
			moveToPointInLeaf( leaf, x.transform( localPos ) );
		}
		else
		{
			throw new CannotFindLeafInSubtreeException();
		}
	}
	
	
	
	
	
	public boolean isAtStartOf(LSContentLeafEditable leaf)
	{
		if ( leaf == element )
		{
			return getIndex() == 0;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isAtEndOf(LSContentLeafEditable leaf)
	{
		if ( leaf == element )
		{
			// The index (position and bias) is at the last position,
			// OR
			// range is 0, and position is 0, bias is 1, which would make index 1
			return getIndex() == leaf.getMarkerRange()  ||  getPosition() == leaf.getMarkerRange();
		}
		else
		{
			return false;
		}
	}
	
	
	

	public void clear()
	{
		if ( element != null )
		{
			element.unregisterMarker( this );
			element = null;
			position = 0;
			bias = Bias.START;
			changed();
		}
		else
		{
			if ( position != 0  ||  bias != Bias.START )
			{
				position = 0;
				bias = Bias.START;
				changed();
			}
		}
	}
	
	
	
	public Marker copy()
	{
		if ( element != null )
		{
			return new Marker( element, position, bias );
		}
		else
		{
			return new Marker();
		}
	}
	
	
		
	
	
	protected void changed()
	{
		if ( listeners != null )
		{
			for (MarkerListener listener: listeners)
			{
				listener.markerChanged( this );
			}
		}
	}




	public boolean equals(Marker m)
	{
		if ( m == this )
		{
			return true;
		}
		else
		{
			return element == m.element  &&  position == m.position  &&  bias == m.bias;
		}
	}
	
	public int hashCode()
	{
		int elementHash = element != null  ?  element.hashCode()  :  0;
		return HashUtils.tripleHash( elementHash, position, bias.hashCode() );
	}
	
	
	
	private static void checkPositionAndBias(LSContentLeafEditable w, int position, Bias bias)
	{
		int markerRange = w.getMarkerRange();
		if ( position < 0 )
		{
			throw new InvalidMarkerPosition( "Cannot place marker at " + position + ":" + bias + " - range is " + markerRange );
		}
		
		if ( position > markerRange )
		{
			throw new InvalidMarkerPosition( "Cannot place marker at " + position + ":" + bias + " - range is " + markerRange );
		}
		
		if ( markerRange == 0 && position == 0 )
		{
			bias = Marker.Bias.START;
		}
		
		if ( position == markerRange  &&  bias == Marker.Bias.END )
		{
			throw new InvalidMarkerPosition( "Cannot place marker at " + position + ":" + bias + " - range is " + markerRange );
		}
	}
	
	
	
	
	public void moveToPositionAndBiasWithinSubtree(LSElement subtree, AbstractTextRepresentationManager textRepManager, int newPosition, Bias newBias, ElementFilter leafFilter)
	{
		String subtreeTextRep = textRepManager.getTextRepresentationOf( subtree );
		int subtreeTextRepLength = subtreeTextRep.length(); 
		
		int newIndex = newBias == Bias.END   ?  newPosition + 1 : newPosition;
		
		if ( leafFilter == null )
		{
			leafFilter = LSContentLeafEditable.editableRealisedFilter;
		}
		
		if ( newPosition < 0 )
		{
			try
			{
				moveToStartOf( subtree, leafFilter );
				return;
			}
			catch (CannotFindLeafInSubtreeException e)
			{
			}
			newPosition = 0;
			newBias = Marker.Bias.START;
		}
		else if ( newPosition >= subtreeTextRepLength )
		{
			try
			{
				moveToEndOf( subtree, leafFilter );
				return;
			}
			catch (CannotFindLeafInSubtreeException e)
			{
			}
			newPosition = subtreeTextRepLength - 1;
			newBias = Marker.Bias.END;
		}

		
		LSContentLeaf leaf = textRepManager.getLeafAtPositionInSubtree( subtree, newPosition );
		if ( leaf != null )
		{
			int elemOffset = -1;
			if ( leaf == subtree )
			{
				elemOffset = 0;
			}
			else
			{
				elemOffset = textRepManager.getPositionOfElementInSubtree( (LSContainer)subtree, leaf );
			}
			int leafPosition = newPosition - elemOffset;
			
			
			if ( leaf instanceof LSContentLeafEditable  &&  ( leafFilter == null  ||  leafFilter.testElement( leaf ) ) )
			{
				int range = ((LSContentLeafEditable)leaf).getMarkerRange();
				leafPosition = Math.max( 0, Math.min( range, leafPosition ) );
				if ( leafPosition == range )
				{
					newBias = Bias.START;
				}
				
				moveTo( (LSContentLeafEditable)leaf, leafPosition, newBias );
				return;
			}
			else
			{
				// The leaf is not a LSContentLeafEditable, or it fails the filter test. We must choose a nearby leaf that satisfies the requirements
				
				// Get a leaf before and after @leaf
				LSElement prev = TreeTraversal.previousElement( leaf, null, LSElement.internalBranchChildrenFn, leafFilter );
				LSElement next = TreeTraversal.nextElement( leaf, null, LSElement.internalBranchChildrenFn, leafFilter );
				
				// Get the distance from the edge of each to the desired position
				if ( prev != null )
				{
					if ( !( prev instanceof LSContentLeafEditable) )
					{
						throw new RuntimeException( "Leaf filter passed an element that is not a LSContentLeafEditable - it passed a " + prev );
					}
				}

				if ( next != null )
				{
					if ( !( next instanceof LSContentLeafEditable) )
					{
						throw new RuntimeException( "Leaf filter passed an element that is not a LSContentLeafEditable - it passed a " + next );
					}
				}

				// Pick the best place
				// First step, if only one neighbour was found, use that
				if ( prev == null  &&  next == null )
				{
					clear();
					return;
				}
				else if ( prev != null  &&  next == null )
				{
					moveToEndOfLeaf( (LSContentLeafEditable)prev );
					return;
				}
				else if ( prev == null  &&  next != null )
				{
					moveToStartOfLeaf( (LSContentLeafEditable)next );
					return;
				}
				else
				{
					// Two neighbours were found. Choose the best one.
				
					int prevDistance = -1, nextDistance = -1;
					int prevLength = textRepManager.getTextRepresentationOf( prev ).length();
					int prevEnd = textRepManager.getPositionOfElementInSubtree( (LSContainer)subtree, prev )  +  prevLength;
					prevEnd = Math.max( 0, prevEnd );
					prevDistance = newIndex - prevEnd;

					int nextStart = textRepManager.getPositionOfElementInSubtree( (LSContainer)subtree, next );
					nextStart = Math.min( nextStart, subtreeTextRepLength );
					nextDistance = nextStart - newIndex;
					
					// Choose the best neighbour by distance.
					// Avoid crossing a newline character if possible
					if ( prevDistance < nextDistance )
					{
						boolean crossingANewLinePrev = subtreeTextRep.substring( prevEnd, newIndex ).contains( "\n" );
						
						if ( !crossingANewLinePrev )
						{
							moveToEndOfLeaf( (LSContentLeafEditable)prev );
							return;
						}
						else
						{
							// We are crossing a newline going to the previous neighbour - what about the next?
							boolean crossingANewLineNext = subtreeTextRep.substring( newIndex, nextStart ).contains( "\n" );
							
							if ( crossingANewLineNext )
							{
								moveToEndOfLeaf( (LSContentLeafEditable)prev );
								return;
							}
							else
							{
								moveToStartOfLeaf( (LSContentLeafEditable)next );
								return;
							}
						}
					}
					else
					{
						boolean crossingANewLineNext = subtreeTextRep.substring( newIndex, nextStart ).contains( "\n" );
						
						if ( !crossingANewLineNext )
						{
							moveToStartOfLeaf( (LSContentLeafEditable)next );
							return;
						}
						else
						{
							// We are crossing a newline going to the previous neighbour - what about the next?
							boolean crossingANewLinePrev = subtreeTextRep.substring( prevEnd, newIndex ).contains( "\n" );
							
							if ( crossingANewLinePrev )
							{
								moveToStartOfLeaf( (LSContentLeafEditable)next );
								return;
							}
							else
							{
								moveToEndOfLeaf( (LSContentLeafEditable)prev );
								return;
							}
						}
					}
				}
			}
		}
		
		clear();
	}
	
	
	public static Marker markerAtPositionAndBiasWithinSubtree(LSElement subtree, AbstractTextRepresentationManager textRepManager, int newPosition, Bias newBias, ElementFilter leafFilter)
	{
		Marker m = new Marker();
		m.moveToPositionAndBiasWithinSubtree( subtree, textRepManager, newPosition, newBias, leafFilter );
		return m;
	}
	
	
	@Override
	public String toString()
	{
		return "Marker[" + System.identityHashCode( this ) + "]( element=" + element + ", position=" + position + ", bias=" + bias + " )";
	}
	
	
	
	// Returns:
	//   1 : a is first
	//   -1 : b is first
	//   0 : equal
	@Override
	public int compareTo(Marker b)
	{
		if ( element == b.element )
		{
			if ( position == b.position )
			{
				if ( bias == b.bias )
				{
					return 0;
				}
				else
				{
					return bias == Bias.START  ?  1  :  -1;
				}
			}
			else
			{
				return position < b.position  ?  1 : -1;
			}
		}
		else
		{
			return LSElement.areElementsInOrder( element, b.element )  ?  1  :  -1;
		}
	}
	
	
	
	
	public static Marker atStartOfLeaf(LSContentLeafEditable leaf)
	{
		Marker m = new Marker();
		m.moveToStartOfLeaf( leaf );
		return m;
	}
	
	public static Marker atStartOfLeafPlusOne(LSContentLeafEditable leaf)
	{
		Marker m = new Marker();
		m.moveToStartOfLeafPlusOne( leaf );
		return m;
	}
	
	public static Marker atEndOfLeaf(LSContentLeafEditable leaf)
	{
		Marker m = new Marker();
		m.moveToEndOfLeaf( leaf );
		return m;
	}
	
	public static Marker atEndOfLeafMinusOne(LSContentLeafEditable leaf)
	{
		Marker m = new Marker();
		m.moveToEndOfLeafMinusOne( leaf );
		return m;
	}
	
	public static Marker atPointInLeaf(LSContentLeafEditable leaf, Point2 localPos)
	{
		Marker m = new Marker();
		m.moveToPointInLeaf( leaf, localPos );
		return m;
	}
	
	
	
	public static Marker atStartOf(LSElement element, boolean editable)
	{
		Marker m = new Marker();
		try
		{
			m.moveToStartOf( element, editable );
			return m;
		}
		catch (CannotFindLeafInSubtreeException e)
		{
			return null;
		}
	}
	
	public static Marker atEndOf(LSElement element, boolean editable)
	{
		Marker m = new Marker();
		try
		{
			m.moveToEndOf( element, editable );
			return m;
		}
		catch (CannotFindLeafInSubtreeException e)
		{
			return null;
		}
	}
	
	
	public static Marker atPointIn(LSElement element, Point2 localPos, boolean editable)
	{
		Marker m = new Marker();
		try
		{
			m.moveToPointIn( element, localPos, editable );
			return m;
		}
		catch (CannotFindLeafInSubtreeException e)
		{
			return null;
		}
	}
}
