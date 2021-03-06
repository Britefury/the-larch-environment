//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.IncrementalView;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.Marker.Marker.Bias;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.LSpace.TextFocus.TextSelectionPoint;
import BritefuryJ.Util.StringDiff;

public class NodeElementChangeListenerDiff implements IncrementalView.NodeElementChangeListener
{
	private static final long DIFF_THRESHHOLD = 65536L;

	private static final String CARET_BOUNDARY = "\ue137";



	public static class CaretDiffTextRepresentationManager extends AbstractTextRepresentationManager {
		@Override
		protected String getElementContent(LSElement e)
		{
			if (e instanceof LSCaretBoundary) {
				return CARET_BOUNDARY;
			}
			else {
				return e.getLeafTextRepresentation();
			}
		}
	}


	
	public class MonitoredMarker
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
				return state.markerAtNewPosition( getCaretTextManager(), leafFilter );
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
		private int positionInSubtree;
		private Marker.Bias bias;
		
		public MarkerState(int positionInSubtree, Marker marker)
		{
			this.positionInSubtree = positionInSubtree;
			this.bias = marker.getBias();
		}
		
		
		public void setSubtreeElement(LSElement subtree)
		{
			this.subtree = subtree;
		}

		public void reposition(int position, Marker.Bias bias)
		{
			this.positionInSubtree = position;
			this.bias = bias;
		}

		public void reposition(int position)
		{
			this.positionInSubtree = position;
		}
		
		public Marker markerAtNewPosition(AbstractTextRepresentationManager textRepresentationManager, ElementFilter leafFilter)
		{
			return Marker.markerAtPositionAndBiasWithinSubtree( subtree, textRepresentationManager, positionInSubtree, bias, leafFilter );
		}
	}
	
	
	private class NodeState
	{
		private FragmentView node;
		private String textRepresentation;
		
		private ArrayList<MarkerState> markerStates  =  new ArrayList<MarkerState>();
		
		
		public NodeState(FragmentView node)
		{
			this.node = node;
			textRepresentation = getCaretTextManager().getTextRepresentationOf(node.getFragmentContentElement());
		}
		
		
		public MarkerState addMarkerStateFor(Marker marker, int positionInSubtree)
		{
			Marker.Bias bias = marker.getBias();
			for (MarkerState state: markerStates)
			{
				if ( state.positionInSubtree == positionInSubtree  &&  state.bias == bias )
				{
					return state;
				}
			}
			
			MarkerState state = new MarkerState( positionInSubtree, marker );
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
				String newTextRepresentation = getCaretTextManager().getTextRepresentationOf(nodeElement);
				
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
							int newPosition = state.positionInSubtree;
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
					else if ( ( (long)origChangeRegionLength * (long)newChangeRegionLength)  >  DIFF_THRESHHOLD )		// Important that we cast to longs to avoid overflows, that can EASILY happen otherwise
					{
						// If the m*n > DIFF_THRESHOLD, use a simpler method; this prevents slow downs

						// HACK HACK HACK
						// FIXME FIXME FIXME
						System.out.println( "Computing caret position using non-diff hack; " + textRepresentation.length() + " (" + prefixLen + ":" + origChangeRegionLength + ":" + suffixLen + ")  ->  " +
								newTextRepresentation.length()  +  " (" + prefixLen + ":" + newChangeRegionLength + ":" + suffixLen + ")" );
						for (MarkerState state: markerStates)
						{
							int newPosition = state.positionInSubtree;
							
							if ( state.positionInSubtree > prefixLen )
							{
								int rel = state.positionInSubtree - prefixLen;
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
							int newPosition = state.positionInSubtree;
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
									if ( state.positionInSubtree >= op.aBegin  &&  state.positionInSubtree < op.aEnd )
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
											newPosition = state.positionInSubtree + op.bBegin - op.aBegin;
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
	private IncrementalView view;
	private AbstractTextRepresentationManager caretTextManager = null;
	
	
	public NodeElementChangeListenerDiff(IncrementalView view)
	{
		this.view = view;
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
			caretMon = monitorMarker( caret.getMarker(), LSContentLeafEditable.editableRealisedLeafElementFilter );
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
		for (MonitoredMarker monitored: markers)
		{
			Marker marker = monitored.marker;
			if ( markerToState.get( marker ) == null )
			{
				LSElement nodeElement = node.getFragmentContentElement();
				try
				{
					int posInSubtree = marker.getPositionInSubtree( nodeElement, getCaretTextManager() );
					
					NodeState nodeState = validNodeStateFor( node );
					MarkerState state = nodeState.addMarkerStateFor( marker, posInSubtree );
					markerToState.put( marker, state );
					monitored.state = state;
				}
				catch (LSElement.IsNotInSubtreeException e)
				{
					// Marker is not in this sub-tree - do nothing
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


	private AbstractTextRepresentationManager getCaretTextManager()
	{
		if (caretTextManager == null) {
			caretTextManager = new CaretDiffTextRepresentationManager();
		}
		return caretTextManager;
		//return view.getPresentationRootElement().getDefaultTextRepresentationManager();
	}
}
