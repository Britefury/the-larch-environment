//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Marker;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPElement.IsNotInSubtreeException;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.Util.HashUtils;

public class Marker
{
	public static class InvalidMarkerPosition extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public InvalidMarkerPosition(String message)
		{
			super( message );
		}
	}

	
	public enum Bias { START, END }
	
	
	private enum Direction
	{
		DIR_NONE,
		FORWARD,
		BACKWARD
	}

	
	protected DPContentLeafEditable element;
	protected int position;
	protected Bias bias;
	protected ArrayList<MarkerListener> listeners;
	
	
	
	public Marker()
	{
		this.element = null;
		this.position = 0;
		this.bias = Bias.START;
	}
	
	public Marker(DPContentLeafEditable element, int position, Bias bias)
	{
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
	

	
	public DPContentLeafEditable getElement()
	{
		return element;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public int getPositionInSubtree(DPElement subtreeRoot) throws IsNotInSubtreeException
	{
		if ( subtreeRoot == element )
		{
			return getPosition();
		}
		else
		{
			if ( subtreeRoot instanceof DPContainer )
			{
				DPContainer b = (DPContainer)subtreeRoot;
				if ( element != null  &&  element.isInSubtreeRootedAt( b ) )
				{
					return getPosition() + element.getTextRepresentationOffsetInSubtree( b );
				}
				else
				{
					throw new DPElement.IsNotInSubtreeException();
				}
			}
			else if ( subtreeRoot instanceof DPContentLeaf )
			{
				if ( element != null  &&  element == subtreeRoot )
				{
					return getPosition();
				}
				else
				{
					throw new DPElement.IsNotInSubtreeException();
				}
			}
			else
			{
				throw new DPElement.IsNotInSubtreeException();
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
	
	public int getClampedIndexInSubtree(DPElement subtreeRoot) throws IsNotInSubtreeException
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
	
	public void set(DPContentLeafEditable element, int position, Bias bias)
	{
		if ( element != null )
		{
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
		set( marker.element, marker.position, marker.bias );
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
	
	
	
	// Returns:
	//   1 : a is first
	//   -1 : b is first
	//   0 : equal
	public static int markerOrder(Marker a, Marker b)
	{
		if ( a.element == b.element )
		{
			if ( a.position == b.position )
			{
				if ( a.bias == b.bias )
				{
					return 0;
				}
				else
				{
					return a.bias == Bias.START  ?  1  :  -1;
				}
			}
			else
			{
				return a.position < b.position  ?  1 : -1;
			}
		}
		else
		{
			return DPElement.areElementsInOrder( a.element, b.element )  ?  1  :  -1;
		}
	}
	
	
	
	public Marker copy()
	{
		if ( element != null )
		{
			return element.marker( position, bias );
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
	
	
	
	private static void checkPositionAndBias(DPContentLeafEditable w, int position, Bias bias)
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
	
	
	
	
	public static int getIndex(int position, Bias bias)
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	
	
	public void moveToPositionAndBiasWithinSubtree(DPElement subtree, int newPosition, Bias newBias, ElementFilter leafFilter)
	{
		int subtreeTextRepLength = subtree.getTextRepresentationLength();
		
		if ( newPosition < 0 )
		{
			newPosition = 0;
			newBias = Marker.Bias.START;
		}
		else if ( newPosition >= subtreeTextRepLength )
		{
			newPosition = subtreeTextRepLength - 1;
			newBias = Marker.Bias.END;
		}

		
		int newIndex = newPosition  +  ( newBias == Marker.Bias.END  ?  1  :  0 );

		DPContentLeaf leaf = subtree.getLeafAtTextRepresentationPosition( newPosition );
		if ( leaf != null )
		{
			int leafOffset = -1;
			if ( leaf == subtree )
			{
				leafOffset = 0;
			}
			else
			{
				leafOffset = leaf.getTextRepresentationOffsetInSubtree( (DPContainer)subtree );
			}
			int leafPosition = newPosition - leafOffset;
			
			
			if ( leafFilter == null  ||  leafFilter.testElement( leaf ) )
			{
				moveTo( ((DPContentLeafEditable)leaf).marker( leafPosition, newBias ) );
			}
			else
			{
				// The leaf is not editable. We must choose a nearby leaf to place the caret in
				
				DPSegment segment = leaf.getSegment();
				DPSegment.SegmentFilter segFilter = segment != null  ?  new DPSegment.SegmentFilter( segment )  :  null;
				
				
				// First, we must decide whether we should search backwards or forwards
				Direction direction = Direction.DIR_NONE;
				String leafTextRepresentation = leaf.getTextRepresentation();
				int leafTextReprLength = leafTextRepresentation.length();
				
				// First, see if the leaf textual representation contains a new-line. If so, try to stay on the same side of the new line.
				if ( leafTextRepresentation.contains( "\n" ) )
				{
					int leafIndex = newIndex - leafOffset;
					if ( leafTextRepresentation.substring( leafIndex, leafTextReprLength ).contains( "\n" ) )
					{
						// Newline in text after the caret position; search backwards
						direction = Direction.BACKWARD;
					}
					else if ( leafTextRepresentation.substring( 0, leafIndex ).contains( "\n" ) )
					{
						// Newline in text before the caret position; search forwards
						direction = Direction.FORWARD;
					}
				}
				
				
				if ( direction == Direction.DIR_NONE )
				{
					// Decide which way to go by staying on the same side of the centre of the leaf
					if ( (float)leafPosition  <  ((float)leafTextReprLength) * 0.5f )
					{
						direction = Direction.BACKWARD;
					}
					else
					{
						direction = Direction.FORWARD;
					}
				}
				
				
				if ( direction == Direction.BACKWARD )
				{
					// Search backwards
					DPContentLeaf left = leaf.getPreviousLeaf( segFilter, null, leafFilter );
					if ( left != null )
					{
						moveTo( left.markerAtEnd() );
					}
					else
					{
						// Searching backwards failed; search forwards
						DPContentLeaf right = leaf.getNextLeaf( segFilter, null, leafFilter );
						if ( right != null )
						{
							moveTo( right.markerAtStart() );
						}
						else
						{
							// Search backwards, this time potentially leaving the segment
							left = leaf.getPreviousLeaf( null, null, leafFilter );
							if ( left != null )
							{
								moveTo( left.markerAtEnd() );
							}
							else
							{
								// Searching backwards failed; search forwards
								right = leaf.getNextLeaf( null, null, leafFilter );
								if ( right != null )
								{
									moveTo( right.markerAtStart() );
								}
								else
								{
									// Searching backwards and forwards failed; place the cursor in the non-editable leaf and hope for the best
									moveTo( leaf.markerAtStart() );
								}
							}
						}
					}
				}
				else if ( direction == Direction.FORWARD )
				{
					// Search forwards
					DPContentLeaf right = leaf.getNextLeaf( segFilter, null, leafFilter );
					if ( right != null )
					{
						moveTo( right.markerAtStart() );
					}
					else
					{
						// Searching forwards failed; search backwards
						DPContentLeaf left = leaf.getPreviousLeaf( segFilter, null, leafFilter );
						if ( left != null )
						{
							moveTo( left.markerAtEnd() );
						}
						else
						{
							// Search forwards, this time potentially leaving the segment
							right = leaf.getNextLeaf( null, null, leafFilter );
							if ( right != null )
							{
								moveTo( right.markerAtStart() );
							}
							else
							{
								// Searching forwards failed; search backwards
								left = leaf.getPreviousLeaf( null, null, leafFilter );
								if ( left != null )
								{
									moveTo( left.markerAtEnd() );
								}
								else
								{
									// Searching forwards and backwards failed; place the cursor in the non-editable leaf and hope for the best
									moveTo( leaf.markerAtStart() );
								}
							}
						}
					}
				}
				else
				{
					throw new RuntimeException( "invalid direction" );
				}
			}
		}
	}
	
	
	@Override
	public String toString()
	{
		return "Marker[" + System.identityHashCode( this ) + "]( element=" + element + ", position=" + position + ", bias=" + bias + " )";
	}
}
