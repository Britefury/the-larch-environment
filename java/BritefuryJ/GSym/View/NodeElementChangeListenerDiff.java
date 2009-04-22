//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementTree.BranchElement;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.LeafElement;
import BritefuryJ.DocPresent.ElementTree.SegmentElement;
import BritefuryJ.DocPresent.ElementTree.Caret.ElementCaret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;

public abstract class NodeElementChangeListenerDiff implements DVNode.NodeElementChangeListener
{
	private static int DIFF_THRESHHOLD = 65536;
	
	private enum Direction
	{
		DIR_NONE,
		FORWARD,
		BACKWARD
	}
	
	
	private DVNode caretNode;
	private String textRepresentation;
	private Marker.Bias bias;
	private int position;
	
	
	public NodeElementChangeListenerDiff()
	{
		caretNode = null;
		textRepresentation = null;
		bias = Marker.Bias.START;
		position = -1;
	}
	
	
	public void reset(DocView view)
	{
		caretNode = null;
		textRepresentation = null;
		bias = Marker.Bias.START;
		position = -1;
	}

	
	public void elementChangeFrom(DVNode node, Element element)
	{
		if ( caretNode == null )
		{
			// Get and store initial state
			Element nodeElement = node.getInnerElementNoRefresh();
			if ( nodeElement != null )
			{
				ElementTree tree = nodeElement.getElementTree();
				ElementCaret caret = tree.getCaret();
	
				String text = nodeElement.getTextRepresentation();
				int pos = -1;
				
				try
				{
					pos = caret.getMarker().getPositionInSubtree( nodeElement );
					caretNode = node;
					textRepresentation = text;
					bias = caret.getMarker().getBias();
					position = pos;
				}
				catch (DPWidget.IsNotInSubtreeException e)
				{
				}
			}
		}
	}

	public void elementChangeTo(DVNode node, Element element)
	{
		if ( caretNode == node )
		{
			Element nodeElement = node.getInnerElementNoRefresh();
			
			// Invoking child.refresh() above can cause this method to be invoked on another node; recursively;
			// Ensure that only the inner-most recursion level handles the caret
			if ( nodeElement != null  &&  position != -1 )
			{
				String newTextRepresentation = nodeElement.getTextRepresentation();
				
				int newPosition = position;
				Marker.Bias newBias = bias;
				
				//int oldIndex = position  +  ( bias == Marker.Bias.END  ?  1  :  0 );
				
				if ( !newTextRepresentation.equals( textRepresentation ) )
				{
					// Compute the difference between the old content and the new content in order to update the cursor position
					
					// String differencing is a O(mn) algorithm, where m and n are the lengths of the source and destination strings respectively
					// In order to prevent awful performance, string differencing must be applied to as little text as possible.
					// In most cases, the edit operation affects part of the content in the middle of the string; large parts of the beginning and end
					// will remain unchanged
					// By computing the common prefix and common suffix of the two strings, we can narrow down the window to which differencing
					// is applied.
					// Limiting the scope can cause the differencing algorithm to produce a different result, than if it was applied to the whole string.
					// In order to keeps the result consistent with that of a complete string difference, an extra character at the start and end of the
					// change region is included.
					
					// Get the size of the common prefix and suffix
					int prefixLen = StringDiff.getCommonPrefixLength( textRepresentation, newTextRepresentation );
					int suffixLen = StringDiff.getCommonSuffixLength( textRepresentation, newTextRepresentation );
					// Include 1 extra character at each end if available
					prefixLen = Math.max( 0, prefixLen - 1 );
					suffixLen = Math.max( 0, suffixLen - 1 );
					// Compute the lengths of the change region in the original content and the new content
					int origChangeRegionLength = textRepresentation.length() - prefixLen - suffixLen;
					int newChangeRegionLength = newTextRepresentation.length() - prefixLen - suffixLen;
					
					// If the m*n > DIFF_THRESHOLD, use a simpler method; this prevents slow downs
					if ( ( origChangeRegionLength * newChangeRegionLength)  >  DIFF_THRESHHOLD )
					{
						// HACK HACK HACK
						// FIXME FIXME FIXME
						System.out.println( "Computing caret position using non-diff hack; " + textRepresentation.length() + " (" + prefixLen + ":" + origChangeRegionLength + ":" + suffixLen + ")  ->  " +
								newTextRepresentation.length()  +  " (" + prefixLen + ":" + newChangeRegionLength + ":" + suffixLen + ")" );
						if ( position > prefixLen )
						{
							int rel = position = prefixLen;
							if ( rel > origChangeRegionLength )
							{
								rel += newChangeRegionLength - origChangeRegionLength;
							}
							else
							{
								rel = Math.min( rel, newChangeRegionLength );
							}
							newPosition = rel + prefixLen; 
						}
						// HACK HACK HACK
						// FIXME FIXME FIXME
					}
					else
					{
						// Cannot simply use contentString[prefixLen:-suffixLen], since suffixLen may be 0
						if ( newPosition < prefixLen )
						{
							// Within prefix; leave it as it is
						}
						else if ( newPosition >= textRepresentation.length() - suffixLen )
						{
							// Within suffix; offset by change in length
							newPosition += newTextRepresentation.length() - textRepresentation.length();
						}
						else
						{
							String origChangeRegion = textRepresentation.substring( prefixLen, textRepresentation.length() - suffixLen ).toString();
							String newChangeRegion = newTextRepresentation.substring( prefixLen, newTextRepresentation.length() - suffixLen ).toString();
							
							Marker.Bias newBiasArray[] = new Marker.Bias[] { newBias };
							newPosition = computeNewPositionWithDiff( position, bias, newBiasArray, textRepresentation, newTextRepresentation, prefixLen, suffixLen, origChangeRegion, newChangeRegion );
							newBias = newBiasArray[0];
						}
					}
				}
				
				
				ElementTree elementTree = nodeElement.getElementTree();
				ElementCaret caret = elementTree.getCaret();
				
				
				int newIndex = newPosition  +  ( newBias == Marker.Bias.END  ?  1  :  0 );
				
				
//				if ( bias == Marker.Bias.START )
//				{
//					System.out.println( textRepresentation.substring( 0, oldIndex ).toString().replace( "\n", "\\n" )  +  ">|"  +  textRepresentation.substring( oldIndex, textRepresentation.length() ).toString().replace( "\n", "\\n" ) );
//				}
//				else
//				{
//					System.out.println( textRepresentation.substring( 0, oldIndex ).toString().replace( "\n", "\\n" )  +  "|<"  +  textRepresentation.substring( oldIndex, textRepresentation.length() ).toString().replace( "\n", "\\n" ) );
//				}
//
//				if ( newBias == Marker.Bias.START )
//				{
//					System.out.println( newTextRepresentation.substring( 0, newIndex ).toString().replace( "\n", "\\n" )  +  ">|"  +  newTextRepresentation.substring( newIndex, newTextRepresentation.length() ).toString().replace( "\n", "\\n" ) );
//				}
//				else
//				{
//					System.out.println( newTextRepresentation.substring( 0, newIndex ).toString().replace( "\n", "\\n" )  +  "|<"  +  newTextRepresentation.substring( newIndex, newTextRepresentation.length() ).toString().replace( "\n", "\\n" ) );
//				}
				
				
				newPosition = Math.max( 0, newPosition );
				if ( newPosition >= newTextRepresentation.length() )
				{
					newPosition = newTextRepresentation.length() - 1;
					newBias = Marker.Bias.END;
				}
				
				
				LeafElement leaf = nodeElement.getLeafAtTextRepresentationPosition( newPosition );
				if ( leaf != null )
				{
					int leafOffset = -1;
					if ( leaf == nodeElement )
					{
						leafOffset = 0;
					}
					else
					{
						leafOffset = leaf.getTextRepresentationOffsetInSubtree( (BranchElement)nodeElement );
					}
					int leafPosition = newPosition - leafOffset;
					
					
					if ( leaf.isEditableEntry() )
					{
						leaf.moveMarker( caret.getMarker(), leafPosition, newBias );
					}
					else
					{
						// The leaf is not editable. We must choose a nearby leaf to place the caret in
						
						SegmentElement.SegmentFilter segFilter = new SegmentElement.SegmentFilter( leaf.getSegment() );
						LeafElement.LeafFilterEditableEntry elemFilter = new LeafElement.LeafFilterEditableEntry();
						
						
						// First, we must decide whether we should search backwards or forwards
						Direction direction = Direction.DIR_NONE;
						String leafTextRepresentation = leaf.getTextRepresentation();
						int leafTextReprLength = leafTextRepresentation.length();
						
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
							LeafElement left = leaf.getPreviousLeaf( segFilter, null, elemFilter );
							if ( left != null )
							{
								left.moveMarkerToEnd( caret.getMarker() );
							}
							else
							{
								// Searching backwards failed; search forwards
								LeafElement right = leaf.getNextLeaf( segFilter, null, elemFilter );
								if ( right != null )
								{
									right.moveMarkerToStart( caret.getMarker() );
								}
								else
								{
									// Searching backwards and forwards failed; place the cursor in the non-editable leaf and hope for the best
									leaf.moveMarker( caret.getMarker(), leafPosition, newBias );
								}
							}
						}
						else if ( direction == Direction.FORWARD )
						{
							// Search forwards
							LeafElement right = leaf.getNextLeaf( segFilter, null, elemFilter );
							if ( right != null )
							{
								right.moveMarkerToStart( caret.getMarker() );
							}
							else
							{
								// Searching forwards failed; search backwards
								LeafElement left = leaf.getPreviousLeaf( segFilter, null, elemFilter );
								if ( left != null )
								{
									left.moveMarkerToEnd( caret.getMarker() );
								}
								else
								{
									// Searching forwards and backwards failed; place the cursor in the non-editable leaf and hope for the best
									leaf.moveMarker( caret.getMarker(), leafPosition, newBias );
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
		}
	}
	
	
	
	
	public abstract int computeNewPositionWithDiff(int position, Marker.Bias bias, Marker.Bias newBiasArray[], String contentString, String newContentString, int prefixLen, int suffixLen,
			String origChangeRegion, String newChangeRegion);
}
