//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Marker.Marker.Bias;
import BritefuryJ.Utils.StringDiff;

public class NodeElementChangeListenerDiff implements GSymView.NodeElementChangeListener
{
	private static int DIFF_THRESHHOLD = 65536;
	
	
	private GSymFragmentView caretNode;
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
	
	
	public void reset(GSymView view)
	{
		caretNode = null;
		textRepresentation = null;
		bias = Marker.Bias.START;
		position = -1;
	}

	
	public void elementChangeFrom(GSymFragmentView node, DPElement element)
	{
		if ( caretNode == null )
		{
			// Get and store initial state
			DPElement nodeElement = node.getFragmentContentElement();
			if ( nodeElement != null )
			{
				PresentationComponent.RootElement root = nodeElement.getRootElement();
				if ( root != null )
				{
					Caret caret = root.getCaret();
		
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
	}

	public void elementChangeTo(GSymFragmentView node, DPElement element)
	{
		if ( caretNode == node )
		{
			DPElement nodeElement = node.getFragmentContentElement();
			
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
				
				caret.moveToPositionAndBiasWithinSubtree( nodeElement, newPosition, newBias );
			}
		}
	}
}
