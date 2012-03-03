//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalView;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import BritefuryJ.LSpace.LSContentLeaf;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Caret.Caret;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.Marker.Marker.Bias;
import BritefuryJ.LSpace.Selection.Selection;
import BritefuryJ.LSpace.Selection.TextSelection;
import BritefuryJ.LSpace.Selection.TextSelectionPoint;
import BritefuryJ.Util.StringDiff;

public class NodeElementChangeListenerDiff implements IncrementalView.NodeElementChangeListener
{
	private static final int DIFF_THRESHHOLD = 65536;
	
	
	
	public static class MonitoredMarker
	{
		private Marker marker;
		private ElementFilter leafFilter;
		private MarkerState state;
		
		
		private MonitoredMarker(Marker marker, ElementFilter leafFilter)
		{
			this.marker = marker;
			this.leafFilter = leafFilter;
		}
		
		
		private boolean canReposition()
		{
			return state != null  &&  state.subtree != null;
		}
		
		
		public Marker markerAtNewPosition()
		{
			if ( canReposition() )
			{
				return Marker.markerAtPositionAndBiasWithinSubtree( state.subtree, state.position, state.bias, leafFilter );
			}
			else
			{
				return null;
			}
		}
	}
	
	
	private static class MarkerState
	{
		private LSElement subtree;
		private int position;
		private Marker.Bias bias;
		
		public MarkerState(int position, Marker.Bias bias)
		{
			this.position = position;
			this.bias = bias;
		}
		
		
		public void setSubtreeElement(LSElement subtree)
		{
			this.subtree = subtree;
		}

		public void reposition(int position, Marker.Bias bias)
		{
			this.position = position;
			this.bias = bias;
		}

		public void reposition(int position)
		{
			this.position = position;
		}
	}
	
	
	private static class NodeState
	{
		private FragmentView node;
		private String textRepresentation;
		
		private ArrayList<MarkerState> markerStates  =  new ArrayList<MarkerState>();
		
		
		public NodeState(FragmentView node)
		{
			this.node = node;
			textRepresentation = node.getFragmentContentElement().getTextRepresentation();
		}
		
		
		public MarkerState addMarkerStateFor(Marker marker, int position)
		{
			Marker.Bias bias = marker.getBias();
			for (MarkerState state: markerStates)
			{
				if ( state.position == position  &&  state.bias == bias )
				{
					return state;
				}
			}
			
			MarkerState state = new MarkerState( position, bias );
			markerStates.add( state );
			return state;
		}
		
		
		
		public void handleModification()
		{
			LSElement nodeElement = node.getFragmentContentElement();
			
			// Invoking child.refresh() above can cause this method to be invoked on another node; recursively;
			// Ensure that only the inner-most recursion level handles the caret
			if ( nodeElement != null  &&  !markerStates.isEmpty() )
			{
				String newTextRepresentation = nodeElement.getTextRepresentation();
				
				for (MarkerState state: markerStates)
				{
					state.setSubtreeElement( nodeElement );
				}
				
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
					
					//int oldIndex = position  +  ( bias == Marker.Bias.END  ?  1  :  0 );
					
					if ( origChangeRegionLength <= 0  ||  newChangeRegionLength <= 0 )
					{
						for (MarkerState state: markerStates)
						{
							int newPosition = state.position;
							Marker.Bias newBias = state.bias;
							
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
							
							state.reposition( newPosition, newBias );
						}
					}
					else if ( ( origChangeRegionLength * newChangeRegionLength)  >  DIFF_THRESHHOLD )
					{
						// If the m*n > DIFF_THRESHOLD, use a simpler method; this prevents slow downs

						// HACK HACK HACK
						// FIXME FIXME FIXME
						System.out.println( "Computing caret position using non-diff hack; " + textRepresentation.length() + " (" + prefixLen + ":" + origChangeRegionLength + ":" + suffixLen + ")  ->  " +
								newTextRepresentation.length()  +  " (" + prefixLen + ":" + newChangeRegionLength + ":" + suffixLen + ")" );
						for (MarkerState state: markerStates)
						{
							int newPosition = state.position;
							
							if ( state.position > prefixLen )
							{
								int rel = state.position - prefixLen;
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
							
							state.reposition( newPosition );
						}
						// HACK HACK HACK
						// FIXME FIXME FIXME
					}
					else
					{
						ArrayList<StringDiff.Operation> operations = null;

						for (MarkerState state: markerStates)
						{
							int newPosition = state.position;
							Marker.Bias newBias = state.bias;
							
							
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
								if ( operations == null )
								{
									String origChangeRegion = textRepresentation.substring( prefixLen, textRepresentation.length() - suffixLen );
									String newChangeRegion = newTextRepresentation.substring( prefixLen, newTextRepresentation.length() - suffixLen );
								
								
									operations = StringDiff.levenshteinDiff( origChangeRegion, newChangeRegion );
	
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
								}
	
								// Find the operation which covers the caret
								for (StringDiff.Operation op: operations)
								{
									if ( state.position >= op.aBegin  &&  state.position < op.aEnd )
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
											newPosition = state.position + op.bBegin - op.aBegin;
										}
									}
								}
							}
							
							
							state.reposition( newPosition, newBias );
						}
					}
				}
			}
		}
	}
	
	
	private IdentityHashMap<Marker, MarkerState> markerToState = new IdentityHashMap<Marker, MarkerState>();
	private IdentityHashMap<FragmentView, NodeState> nodeToState = new IdentityHashMap<FragmentView, NodeState>();
	private ArrayList<MonitoredMarker> markers = new ArrayList<MonitoredMarker>();
	
	private MonitoredMarker caretMon, selStartMon, selEndMon;
	
	
	public NodeElementChangeListenerDiff(IncrementalView view)
	{
	}
	
	
	public void begin(IncrementalView view)
	{
		markerToState.clear();
		nodeToState.clear();
		
		
		caretMon = selStartMon = selEndMon = null;
		
		
		// Gather the markers that we are watching
		markers.clear();
		Caret caret = view.getCaret();
		if ( caret != null  &&  caret.isValid() )
		{
			caretMon = monitorMarker( caret.getMarker(), new LSContentLeaf.EditableLeafElementFilter() );
		}
		
		Selection selection = view.getSelection();
		if ( selection instanceof TextSelection )
		{
			TextSelection ts = (TextSelection)selection;
			if ( ts.isValid() )
			{
				selStartMon = monitorMarker( ts.getStartMarker(), null );
				selEndMon = monitorMarker( ts.getEndMarker(), null );
			}
		}
	}
	
	public void end(IncrementalView view)
	{
		if ( caretMon != null  &&  caretMon.canReposition() )
		{
			view.getCaret().moveTo( caretMon.markerAtNewPosition() );
		}

		if ( selStartMon != null  &&  selStartMon.canReposition()  &&  selEndMon != null  &&  selEndMon.canReposition() )
		{
			TextSelectionPoint start = new TextSelectionPoint( selStartMon.markerAtNewPosition() ); 
			TextSelectionPoint end = new TextSelectionPoint( selEndMon.markerAtNewPosition() ); 
			view.getPresentationRootElement().setSelection( start.createSelectionTo( end ) );
		}
	}
	
	
	public MonitoredMarker monitorMarker(Marker marker, ElementFilter leafFilter)
	{
		MonitoredMarker m = new MonitoredMarker( marker, leafFilter );
		markers.add( m );
		return m;
	}
	
	
	private NodeState validNodeStateFor(FragmentView node)
	{
		NodeState state = nodeToState.get( node );
		if ( state == null )
		{
			state = new NodeState( node );
			nodeToState.put( node, state );
		}
		return state;
	}

	
	public void elementChangeFrom(FragmentView node, LSElement element)
	{
		for (MonitoredMarker m: markers)
		{
			Marker marker = m.marker;
			if ( markerToState.get( marker ) == null )
			{
				LSElement nodeElement = node.getFragmentContentElement();
				try
				{
					int pos = marker.getPositionInSubtree( nodeElement );
					
					NodeState nodeState = validNodeStateFor( node );
					MarkerState state = nodeState.addMarkerStateFor( marker, pos );
					markerToState.put( marker, state );
					m.state = state;
				}
				catch (LSElement.IsNotInSubtreeException e)
				{
					// Caret is not in this sub-tree - do nothing
				}
			}
		}
	}

	public void elementChangeTo(FragmentView node, LSElement element)
	{
		NodeState nodeState = nodeToState.get( node );
		if ( nodeState != null )
		{
			nodeState.handleModification();
		}
	}
}
