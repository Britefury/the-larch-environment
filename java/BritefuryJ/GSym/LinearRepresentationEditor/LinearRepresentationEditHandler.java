//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.LinearRepresentationEditor;

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
import BritefuryJ.GSym.View.FragmentViewFilter;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public abstract class LinearRepresentationEditHandler implements EditHandler
{
	private FragmentViewFilter editLevelFragmentFilter, commonRootEditLevelFragmentFilter;
	private SelectionBufferFactory bufferFactory;
	private DataFlavor bufferFlavor;
	
	
	public LinearRepresentationEditHandler(FragmentViewFilter editLevelFragmentFilter, FragmentViewFilter commonRootEditLevelFragmentFilter, SelectionBufferFactory bufferFactory, DataFlavor bufferFlavor)
	{
		this.editLevelFragmentFilter = editLevelFragmentFilter;
		this.commonRootEditLevelFragmentFilter = commonRootEditLevelFragmentFilter;
		this.bufferFactory = bufferFactory;
		this.bufferFlavor = bufferFlavor;
	}
	
	public LinearRepresentationEditHandler(FragmentViewFilter editLevelFragmentFilter, SelectionBufferFactory bufferFactory, DataFlavor bufferFlavor)
	{
		this( editLevelFragmentFilter, editLevelFragmentFilter, bufferFactory, bufferFlavor );
	}
	

	
	
	@Override
	public void deleteSelection(Selection selection)
	{
		replaceSelection( selection, null );
	}
	
	@Override
	public void replaceSelectionWithText(Selection selection, String replacement)
	{
		replaceSelection( selection, null );
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
				ItemStream replacementStream = null;
				if ( replacement instanceof LinearRepresentationBuffer )
				{
					replacementStream = ((LinearRepresentationBuffer)replacement).stream;
				}
				else if ( replacement instanceof String )
				{
					ItemStreamBuilder builder = new ItemStreamBuilder();
					builder.appendTextValue( (String )replacement );
					replacementStream = builder.stream();
				}
				
				
				if ( replacementStream != null )
				{
					// Get the item streams for the root element content, before and after the selected region
					ItemStream before = editRootFragmentElement.getLinearRepresentationFromStartToMarker( startMarker );
					ItemStream after = editRootFragmentElement.getLinearRepresentationFromMarkerToEnd( endMarker );
					
					// Join
					ItemStream joinedStream = joinStreamsForInsertion( editRootFragment, before, replacementStream, after );
					
					// Store the joined stream in the structural value of the root element
					editRootFragmentElement.setStructuralValueStream( joinedStream );
					// Clear the selection
					selection.clear();
					// Post a tree event
					editRootFragmentElement.postTreeEvent( createSelectionEditTreeEvent( editRootFragmentElement ) );
				}
			}
			else
			{
				// Get the item streams for the root element content, before and after the selected region
				ItemStream before = editRootFragmentElement.getLinearRepresentationFromStartToMarker( startMarker );
				ItemStream after = editRootFragmentElement.getLinearRepresentationFromMarkerToEnd( endMarker );
				
				ItemStream joinedStream = joinStreamsForDeletion( editRootFragment, before, after );
				
				// Store the joined stream in the structural value of the root element
				editRootFragmentElement.setStructuralValueStream( joinedStream );
				// Clear the selection
				selection.clear();
				// Post a tree event
				editRootFragmentElement.postTreeEvent( createSelectionEditTreeEvent( editRootFragmentElement ) );
			}
		}
	}
	
	

	private void insertAtMarker(Marker marker, Object data)
	{
		if ( data instanceof LinearRepresentationBuffer )
		{
			LinearRepresentationBuffer buffer = (LinearRepresentationBuffer)data;
			
			
			GSymFragmentView insertionPointFragment = GSymFragmentView.getEnclosingFragment( marker.getElement(), editLevelFragmentFilter );
			DPElement insertionPointElement = insertionPointFragment.getFragmentContentElement();
			
			// Get the item streams for the root element content, before and after the selected region
			ItemStream before = insertionPointElement.getLinearRepresentationFromStartToMarker( marker );
			ItemStream after = insertionPointElement.getLinearRepresentationFromMarkerToEnd( marker );
			
			ItemStream joinedStream = joinStreamsForInsertion( insertionPointFragment, before, buffer.stream, after );
			
			// Store the joined stream in the structural value of the root element
			insertionPointElement.setStructuralValueStream( joinedStream );
			// Post a tree event
			insertionPointElement.postTreeEvent( createSelectionEditTreeEvent( insertionPointElement ) );
		}
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
			ItemStream stream = rootElement.getLinearRepresentationInSelection( selection );
			
			ItemStreamBuilder builder = new ItemStreamBuilder();
			for (ItemStream.Item item: stream.getItems())
			{
				if ( item instanceof ItemStream.StructuralItem )
				{
					ItemStream.StructuralItem structuralItem = (ItemStream.StructuralItem)item;
					builder.appendStructuralValue( copyStructuralValue( structuralItem.getStructuralValue() ) );
				}
				else if ( item instanceof ItemStream.TextItem )
				{
					ItemStream.TextItem textItem = (ItemStream.TextItem)item;
					builder.appendTextValue( textItem.getTextValue() );
				}
			}
			
			LinearRepresentationBuffer buffer = bufferFactory.createBuffer( builder.stream() );
			return new LinearRepresentationEditTransferable( buffer, bufferFlavor );
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
		return dataTransfer.isDataFlavorSupported( bufferFlavor );
	}
	
	@Override
	public boolean importData(Caret caret, Selection selection, DataTransfer dataTransfer)
	{
		if ( !canImport( caret, selection, dataTransfer ) )
		{
			return false;
		}
		
		Object data = null;
		try
		{
			data = dataTransfer.getTransferData( bufferFlavor );
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
		
		
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
	
	
	
	public abstract ItemStream joinStreamsForInsertion(GSymFragmentView subtreeRootFragment, ItemStream before, ItemStream insertion, ItemStream after);
	public abstract ItemStream joinStreamsForDeletion(GSymFragmentView subtreeRootFragment, ItemStream before, ItemStream after);
	public abstract Object copyStructuralValue(Object x);
	public abstract SelectionEditTreeEvent createSelectionEditTreeEvent(DPElement sourceElement);
}
