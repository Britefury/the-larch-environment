//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Marker.Marker.Bias;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.Logging.Log;
import BritefuryJ.Utils.StringDiff;

public class NodeElementChangeListenerDiff implements DocView.NodeElementChangeListener
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
	private Log log;
	
	
	public NodeElementChangeListenerDiff(Log log)
	{
		caretNode = null;
		textRepresentation = null;
		bias = Marker.Bias.START;
		position = -1;
		this.log = log;
	}
	
	
	public void reset(DocView view)
	{
		caretNode = null;
		textRepresentation = null;
		bias = Marker.Bias.START;
		position = -1;
	}

	
	public void elementChangeFrom(DVNode node, DPElement element)
	{
		if ( caretNode == null )
		{
			// Get and store initial state
			DPElement nodeElement = node.getInnerElementNoRefresh();
			if ( nodeElement != null )
			{
				PresentationComponent.RootElement tree = nodeElement.getRootElement();
				Caret caret = tree.getCaret();
	
				String text = nodeElement.getTextRepresentation();
				int pos = -1;
				
				try
				{
					pos = caret.getPositionInSubtree( nodeElement );
					caretNode = node;
					textRepresentation = text;
					bias = caret.getBias();
					position = pos;
				}
				catch (DPElement.IsNotInSubtreeException e)
				{
				}
			}
		}
	}

	public void elementChangeTo(DVNode node, DPElement element)
	{
		if ( caretNode == node )
		{
			DPElement nodeElement = node.getInnerElementNoRefresh();
			
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
					
					if ( origChangeRegionLength <= 0  ||  newChangeRegionLength <= 0 )
					{
						if ( origChangeRegionLength <= 0  &&  newChangeRegionLength > 0 )
						{
							// Text inserted
							if ( newPosition >= textRepresentation.length() - suffixLen )
							{
								newPosition = newTextRepresentation.length() - ( textRepresentation.length() - newPosition );
							}
						}
						else if ( origChangeRegionLength > 0  &&  newChangeRegionLength <= 0 )
						{
							// Text deleted
							if ( newPosition >= prefixLen  &&  newPosition < textRepresentation.length() - suffixLen )
							{
								newPosition = prefixLen;
								newBias = Bias.START;
							}
						}
						else if ( origChangeRegionLength < 0  &&  newChangeRegionLength < 0 )
						{
							if ( newPosition >= prefixLen )
							{
								newPosition -= ( newTextRepresentation.length() - textRepresentation.length() );
								newPosition = Math.max( newPosition, prefixLen );
							}
						}
					}
					else if ( ( origChangeRegionLength * newChangeRegionLength)  >  DIFF_THRESHHOLD )
					{
						// If the m*n > DIFF_THRESHOLD, use a simpler method; this prevents slow downs

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
							String origChangeRegion = textRepresentation.substring( prefixLen, textRepresentation.length() - suffixLen );
							String newChangeRegion = newTextRepresentation.substring( prefixLen, newTextRepresentation.length() - suffixLen );
							
							newPosition = position;
							newBias = bias;
							
							
							ArrayList<StringDiff.Operation> operations = StringDiff.levenshteinDiff( origChangeRegion, newChangeRegion );

							// Apply the prefix offset
							for (StringDiff.Operation op: operations)
							{
								op.offset( prefixLen );
							}
							
							// Prepend and append some 'equal' operations that cover the prefix and suffix
							if ( suffixLen > 0 )
							{
								operations.add( 0, new StringDiff.Operation( StringDiff.Operation.OpCode.EQUAL, textRepresentation.length() - suffixLen, textRepresentation.length(),
										newTextRepresentation.length() - suffixLen, newTextRepresentation.length() ) );
							}
							if ( prefixLen > 0 )
							{
								operations.add( new StringDiff.Operation( StringDiff.Operation.OpCode.EQUAL, 0, prefixLen, 0, prefixLen ) );
							}

							// Find the operation which covers the caret
							for (StringDiff.Operation op: operations)
							{
								if ( position >= op.aBegin  &&  position < op.aEnd )
								{
									if ( op.opcode == StringDiff.Operation.OpCode.DELETE )
									{
										// Range deleted; move to the start of the range in the destination string, bias:STARt
										newPosition = op.bBegin;
										newBias = Marker.Bias.START;
									}
									else
									{
										// Range replaced, equal, or inserted; offset position be delta between starts of ranges
										newPosition = position + op.bBegin - op.aBegin;
									}
								}
							}
						}
					}
				}
				
				
				PresentationComponent.RootElement elementTree = nodeElement.getRootElement();
				Caret caret = elementTree.getCaret();
				
				
				
				
				if ( newPosition < 0 )
				{
					newPosition = 0;
					newBias = Marker.Bias.START;
				}
				else if ( newPosition >= newTextRepresentation.length() )
				{
					newPosition = newTextRepresentation.length() - 1;
					newBias = Marker.Bias.END;
				}
				
				
				int newIndex = newPosition  +  ( newBias == Marker.Bias.END  ?  1  :  0 );
				
				
//				int oldIndex = position  +  ( bias == Marker.Bias.END  ?  1  :  0 );
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
				
				
				DPContentLeaf leaf = nodeElement.getLeafAtTextRepresentationPosition( newPosition );
				if ( leaf != null )
				{
					int leafOffset = -1;
					if ( leaf == nodeElement )
					{
						leafOffset = 0;
					}
					else
					{
						leafOffset = leaf.getTextRepresentationOffsetInSubtree( (DPContainer)nodeElement );
					}
					int leafPosition = newPosition - leafOffset;
					
					
					if ( leaf.isEditable() )
					{
						caret.moveTo( leaf.marker( leafPosition, newBias ) );
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
							DPContentLeaf left = leaf.getPreviousEditableLeaf( segFilter, null );
							if ( left != null )
							{
								caret.moveTo( left.markerAtEnd() );
							}
							else
							{
								// Searching backwards failed; search forwards
								DPContentLeaf right = leaf.getNextEditableLeaf( segFilter, null );
								if ( right != null )
								{
									caret.moveTo( right.markerAtStart() );
								}
								else
								{
									// Search backwards, this time potentially leaving the segment
									left = leaf.getPreviousEditableLeaf( null, null );
									if ( left != null )
									{
										caret.moveTo( left.markerAtEnd() );
									}
									else
									{
										// Searching backwards failed; search forwards
										right = leaf.getNextEditableLeaf( null, null );
										if ( right != null )
										{
											caret.moveTo( right.markerAtStart() );
										}
										else
										{
											// Searching backwards and forwards failed; place the cursor in the non-editable leaf and hope for the best
											caret.moveTo( leaf.marker( leafPosition, newBias ) );
										}
									}
								}
							}
						}
						else if ( direction == Direction.FORWARD )
						{
							// Search forwards
							DPContentLeaf right = leaf.getNextEditableLeaf( segFilter, null );
							if ( right != null )
							{
								caret.moveTo( right.markerAtStart() );
							}
							else
							{
								// Searching forwards failed; search backwards
								DPContentLeaf left = leaf.getPreviousEditableLeaf( segFilter, null );
								if ( left != null )
								{
									caret.moveTo( left.markerAtEnd() );
								}
								else
								{
									// Search forwards, this time potentially leaving the segment
									right = leaf.getNextEditableLeaf( null, null );
									if ( right != null )
									{
										caret.moveTo( right.markerAtStart() );
									}
									else
									{
										// Searching forwards failed; search backwards
										left = leaf.getPreviousEditableLeaf( null, null );
										if ( left != null )
										{
											caret.moveTo( left.markerAtEnd() );
										}
										else
										{
											// Searching forwards and backwards failed; place the cursor in the non-editable leaf and hope for the best
											caret.moveTo( leaf.marker( leafPosition, newBias ) );
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
		}
	}
}
