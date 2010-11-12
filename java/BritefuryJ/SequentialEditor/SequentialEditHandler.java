//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.SequentialEditor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Clipboard.DataTransfer;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.GSym.View.FragmentViewFilter;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class SequentialEditHandler implements EditHandler
{
	private FragmentViewFilter editLevelFragmentFilter = new FragmentViewFilter()
	{
		@Override
		public boolean testFragmentView(GSymFragmentView fragment)
		{
			return isEditLevelFragmentView( fragment );
		}
	};
	
	
	private FragmentViewFilter commonRootEditLevelFragmentFilter = new FragmentViewFilter()
	{
		@Override
		public boolean testFragmentView(GSymFragmentView fragment)
		{
			return isCommonRootEditLevelFragmentView( fragment );
		}
	};
	
	
	private DataFlavor bufferFlavor;
	
	
	public SequentialEditHandler(DataFlavor bufferFlavor)
	{
		this.bufferFlavor = bufferFlavor;
	}
	
	
	
	
	protected abstract boolean isEditLevelFragmentView(GSymFragmentView fragment);
	
	
	protected boolean isCommonRootEditLevelFragmentView(GSymFragmentView fragment)
	{
		return isEditLevelFragmentView( fragment );
	}
	
	
	protected abstract SequentialBuffer createSelectionBuffer(StreamValue stream);
	
	protected String filterTextForImport(String text)
	{
		return null;
	}
	

	
	
	@Override
	public void deleteSelection(Selection selection)
	{
		replaceSelection( selection, null );
	}
	
	@Override
	public void replaceSelectionWithText(Selection selection, Caret caret, String replacement)
	{
		replaceSelection( selection, replacement );
	}
	
	
	private void replaceSelection(Selection selection, Object replacement)
	{
		if ( !selection.isEmpty() )
		{
			Marker startMarker = selection.getStartMarker();
			Marker endMarker = selection.getEndMarker();
			
			// Get the edit-level fragments that contain the start and end markers
			GSymFragmentView startFragment = GSymFragmentView.getEnclosingFragment( startMarker.getElement(), editLevelFragmentFilter );
			GSymFragmentView endFragment = GSymFragmentView.getEnclosingFragment( endMarker.getElement(), editLevelFragmentFilter );
			
			// Determine the 
			GSymFragmentView editRootFragment = null;
			DPElement editRootFragmentElement = null;
			if ( startFragment == endFragment )
			{
				editRootFragment = startFragment;
			}
			else
			{
				// Get the common root edit-level fragment, and its content element
				editRootFragment = GSymFragmentView.getCommonRootFragment( startFragment, endFragment, commonRootEditLevelFragmentFilter );
			}
			editRootFragmentElement = editRootFragment.getFragmentContentElement();

			
			if ( replacement != null )
			{
				StreamValue replacementStream = null;
				if ( replacement instanceof SequentialBuffer )
				{
					replacementStream = ((SequentialBuffer)replacement).stream;
				}
				else if ( replacement instanceof String )
				{
					StreamValueBuilder builder = new StreamValueBuilder();
					builder.appendTextValue( (String)replacement );
					replacementStream = builder.stream();
				}
				
				
				if ( replacementStream != null )
				{
					// Get the item streams for the root element content, before and after the selected region
					StreamValue before = editRootFragmentElement.getStreamValueFromStartToMarker( startMarker );
					StreamValue after = editRootFragmentElement.getStreamValueFromMarkerToEnd( endMarker );
					
					// Join
					StreamValue joinedStream = joinStreamsForInsertion( editRootFragment, before, replacementStream, after );
					
					// Store the joined stream in the structural value of the root element
					editRootFragmentElement.setFixedValue( joinedStream );
					// Clear the selection
					selection.clear();
					// Post a tree event
					editRootFragmentElement.postTreeEvent( createSelectionEditTreeEvent( editRootFragmentElement ) );
				}
			}
			else
			{
				// Get the item streams for the root element content, before and after the selected region
				StreamValue before = editRootFragmentElement.getStreamValueFromStartToMarker( startMarker );
				StreamValue after = editRootFragmentElement.getStreamValueFromMarkerToEnd( endMarker );
				
				StreamValue joinedStream = joinStreamsForDeletion( editRootFragment, before, after );
				
				// Store the joined stream in the structural value of the root element
				editRootFragmentElement.setFixedValue( joinedStream );
				// Clear the selection
				selection.clear();
				// Post a tree event
				editRootFragmentElement.postTreeEvent( createSelectionEditTreeEvent( editRootFragmentElement ) );
			}
		}
	}
	
	

	private void insertAtMarker(Marker marker, Object data)
	{
		StreamValue stream = null;
		
		
		if ( data instanceof SequentialBuffer )
		{
			SequentialBuffer buffer = (SequentialBuffer)data;
			stream = buffer.stream;
		}
		else if ( data instanceof String )
		{
			StreamValueBuilder builder = new StreamValueBuilder();
			builder.appendTextValue( (String)data );
			stream = builder.stream();
		}
		
			
		if ( stream != null )
		{
			GSymFragmentView insertionPointFragment = GSymFragmentView.getEnclosingFragment( marker.getElement(), editLevelFragmentFilter );
			DPElement insertionPointElement = insertionPointFragment.getFragmentContentElement();
			
			// Get the item streams for the root element content, before and after the selected region
			StreamValue before = insertionPointElement.getStreamValueFromStartToMarker( marker );
			StreamValue after = insertionPointElement.getStreamValueFromMarkerToEnd( marker );
			
			StreamValue joinedStream = joinStreamsForInsertion( insertionPointFragment, before, stream, after );
			
			// Store the joined stream in the structural value of the root element
			insertionPointElement.setFixedValue( joinedStream );
			// Post a tree event
			insertionPointElement.postTreeEvent( createSelectionEditTreeEvent( insertionPointElement ) );
		};
	}
	
	

	@Override
	public int getExportActions(Selection selection)
	{
		return COPY_OR_MOVE;
	}
	
	@Override
	public Transferable createExportTransferable(Selection selection)
	{
		if ( !selection.isEmpty() )
		{
			Marker startMarker = selection.getStartMarker();
			GSymFragmentView startFragment = GSymFragmentView.getEnclosingFragment( startMarker.getElement(), editLevelFragmentFilter );
			
			
			PresentationComponent.RootElement rootElement = startFragment.getFragmentContentElement().getRootElement();
			StreamValue stream = rootElement.getStreamValueInSelection( selection );
			
			StreamValueBuilder builder = new StreamValueBuilder();
			for (StreamValue.Item item: stream.getItems())
			{
				if ( item instanceof StreamValue.StructuralItem )
				{
					StreamValue.StructuralItem structuralItem = (StreamValue.StructuralItem)item;
					builder.appendStructuralValue( copyStructuralValue( structuralItem.getStructuralValue() ) );
				}
				else if ( item instanceof StreamValue.TextItem )
				{
					StreamValue.TextItem textItem = (StreamValue.TextItem)item;
					builder.appendTextValue( textItem.getTextValue() );
				}
			}
			
			SequentialBuffer buffer = createSelectionBuffer( builder.stream() );
			return new SequentialEditTransferable( buffer, bufferFlavor );
		}
		
		return null;
	}
	
	
	
	
	@Override
	public void exportDone(Selection selection, Transferable transferable, int action)
	{
		if ( action == MOVE )
		{
			deleteSelection( selection );
		}
	}
	
	
	@Override
	public boolean canImport(Caret caret, Selection selection, DataTransfer dataTransfer)
	{
		return dataTransfer.isDataFlavorSupported( bufferFlavor )  ||  dataTransfer.isDataFlavorSupported( DataFlavor.stringFlavor );
	}
	
	@Override
	public boolean importData(Caret caret, Selection selection, DataTransfer dataTransfer)
	{
		if ( !canImport( caret, selection, dataTransfer ) )
		{
			return false;
		}
		
		
		
		Object data = null;
		
		
		if ( dataTransfer.isDataFlavorSupported( bufferFlavor ) )
		{
			try
			{
				data = dataTransfer.getTransferData( bufferFlavor );
			}
			catch (UnsupportedFlavorException e)
			{
			}
			catch (IOException e)
			{
			}
		}
		
		
		if ( dataTransfer.isDataFlavorSupported( DataFlavor.stringFlavor ) )
		{
			try
			{
				data = dataTransfer.getTransferData( DataFlavor.stringFlavor );
				data = filterTextForImport( (String)data );
			}
			catch (UnsupportedFlavorException e)
			{
			}
			catch (IOException e)
			{
			}
		}
		
		
		if ( data != null )
		{
			// Paste
			if ( selection.isEmpty() )
			{
				Marker caretMarker = caret.getMarker();
				if ( caretMarker.isValid() )
				{
					insertAtMarker( caretMarker, data );
					return true;
				}
				return false;
			}
			else
			{
				replaceSelection( selection, data );
				return true;
			}
		}
		else
		{
			return false;
		}
	}
	
	
	
	public StreamValue joinStreamsForInsertion(GSymFragmentView subtreeRootFragment, StreamValue before, StreamValue insertion, StreamValue after)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		builder.extend( before );
		builder.extend( insertion );
		builder.extend( after );
		return builder.stream();
	}
	
	public StreamValue joinStreamsForDeletion(GSymFragmentView subtreeRootFragment, StreamValue before, StreamValue after)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		builder.extend( before );
		builder.extend( after );
		return builder.stream();
	}

	public abstract Object copyStructuralValue(Object x);
	public abstract SelectionEditTreeEvent createSelectionEditTreeEvent(DPElement sourceElement);
}
