//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Editor.Sequential;

import java.awt.datatransfer.DataFlavor;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.DocPresent.StreamValue.StreamValueVisitor;
import BritefuryJ.DocPresent.Target.Target;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.FragmentViewFilter;
import BritefuryJ.Pres.Clipboard.AbstractDataExporter;
import BritefuryJ.Pres.Clipboard.AbstractDataImporter;
import BritefuryJ.Pres.Clipboard.AbstractSelectionExporter;
import BritefuryJ.Pres.Clipboard.ClipboardHandler;
import BritefuryJ.Pres.Clipboard.DataExporterInterface;
import BritefuryJ.Pres.Clipboard.DataImporterInterface;
import BritefuryJ.Pres.Clipboard.SelectionEditorInterface;
import BritefuryJ.Pres.Clipboard.TargetImporter;

public class SequentialClipboardHandler extends ClipboardHandler
{
	private AbstractDataExporter<StreamValue> streamExporter = new AbstractDataExporter<StreamValue>()
	{
		@Override
		protected DataFlavor getDataFlavor()
		{
			return dataFlavorForClass( sequentialEditor.getSelectionBufferType() );
		}

		@Override
		protected Object export(StreamValue selectionContents)
		{
			return createSelectionBuffer( selectionContents );
		}
		
	};

	
	private AbstractDataExporter<StreamValue> stringExporter = new AbstractDataExporter<StreamValue>()
	{
		@Override
		protected DataFlavor getDataFlavor()
		{
			return DataFlavor.stringFlavor;
		}

		@Override
		protected Object export(StreamValue selectionContents)
		{
			return selectionContents.textualValue();
		}

		@Override
		protected boolean canExport(StreamValue selectionContents)
		{
			return selectionContents.isTextual();
		}
	};

	
	
	@SuppressWarnings("unchecked")
	private List<? extends DataExporterInterface<StreamValue>> dataExporters = (List<? extends DataExporterInterface<StreamValue>>)
			Arrays.asList( new DataExporterInterface[] { streamExporter, stringExporter } );
	private AbstractSelectionExporter<StreamValue, TextSelection> exporter =
		new AbstractSelectionExporter<StreamValue, TextSelection>( TextSelection.class, AbstractSelectionExporter.COPY_OR_MOVE, dataExporters )
	{
		@Override
		protected StreamValue getSelectionContents(TextSelection selection)
		{
			StreamValueVisitor visitor = new StreamValueVisitor();
			StreamValue stream = visitor.getStreamValueInTextSelection( selection );
			
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
			
			return builder.stream();
		}
		
		@Override
		protected void exportDone(TextSelection selection, Target target, StreamValue selectionContents, int action)
		{
			if ( action == AbstractSelectionExporter.MOVE )
			{
				if ( target instanceof Caret )
				{
					deleteSelection( selection, (Caret)target );
				}
			}
		}
	};
	
	
	
	
	
	private AbstractDataImporter<Caret> streamImporter = new AbstractDataImporter<Caret>()
	{
		protected DataFlavor getDataFlavor()
		{
			return dataFlavorForClass( sequentialEditor.getSelectionBufferType() );
		}
		
		protected boolean importData(Caret caret, Selection selection, Object data)
		{
			SequentialBuffer buffer = (SequentialBuffer)data;
			if ( !canImportFromClipboardHandler( buffer.clipboardHandler ) )
			{
				return false;
			}
			
			return paste( caret, selection, buffer.stream );
		}
	};
	
	private AbstractDataImporter<Caret> stringImporter = new AbstractDataImporter<Caret>()
	{
		protected DataFlavor getDataFlavor()
		{
			return DataFlavor.stringFlavor;
		}
		
		protected boolean importData(Caret caret, Selection selection, Object data)
		{
			return paste( caret, selection, data );
		}
	};
	
	
	@SuppressWarnings("unchecked")
	private List<? extends DataImporterInterface<Caret>> dataImporters = (List<? extends DataImporterInterface<Caret>>)
			Arrays.asList( new DataImporterInterface[] { streamImporter, stringImporter } );
	private TargetImporter<Caret> importer = new TargetImporter<Caret>( Caret.class, dataImporters );
	
	
	
	
	private SelectionEditorInterface selectionEditor = new SelectionEditorInterface()
	{
		@Override
		public Class<? extends Selection> getSelectionClass()
		{
			return TextSelection.class;
		}
		
		@Override
		public boolean replaceSelectionWithText(Selection selection, Target target, String replacement)
		{
			if ( target instanceof Caret )
			{
				replaceSelection( selection, (Caret)target, replacement );
				return true;
			}
			return false;
		}

		@Override
		public boolean deleteSelection(Selection selection, Target target)
		{
			if ( target instanceof Caret )
			{
				replaceSelection( selection, (Caret)target, null );
				return true;
			}
			return false;
		}
	};
	
	
	
	
	
	
	
	private FragmentViewFilter editLevelFragmentFilter = new FragmentViewFilter()
	{
		@Override
		public boolean testFragmentView(FragmentView fragment)
		{
			return isEditLevelFragmentView( fragment );
		}
	};
	
	
	private FragmentViewFilter commonRootEditLevelFragmentFilter = new FragmentViewFilter()
	{
		@Override
		public boolean testFragmentView(FragmentView fragment)
		{
			return isCommonRootEditLevelFragmentView( fragment );
		}
	};
	
	
	private SequentialEditor sequentialEditor;
	
	
	
	
	
	public SequentialClipboardHandler(SequentialEditor sequentialEditor)
	{
		this.sequentialEditor = sequentialEditor;
		
		addExporter( exporter );
		addImporter( importer );
		addSelectionEditor( selectionEditor );
	}
	
	
	
	public SequentialEditor getSequentialEditor()
	{
		return sequentialEditor;
	}
	
	
	protected boolean isCommonRootEditLevelFragmentView(FragmentView fragment)
	{
		return isEditLevelFragmentView( fragment );
	}
	
	
	
	
	//
	//
	// OVERRIDE THESE METHODS
	//
	//
	
	public StreamValue joinStreamsForInsertion(FragmentView subtreeRootFragment, StreamValue before, StreamValue insertion, StreamValue after)
	{
		return sequentialEditor.joinStreamsForInsertion( subtreeRootFragment, before, insertion, after );
	}
	
	public StreamValue joinStreamsForDeletion(FragmentView subtreeRootFragment, StreamValue before, StreamValue after)
	{
		return sequentialEditor.joinStreamsForDeletion( subtreeRootFragment, before, after );
	}
	
	
	
	
	
	protected boolean isEditLevelFragmentView(FragmentView fragment)
	{
		return sequentialEditor.isClipboardEditLevelFragmentView( fragment );
	}
	
	
	protected SequentialBuffer createSelectionBuffer(StreamValue stream)
	{
		return sequentialEditor.createSelectionBuffer( stream );
	}
	
	protected String filterTextForImport(String text)
	{
		return sequentialEditor.filterTextForImport( text );
	}
	

	
	public boolean canImportFromClipboardHandler(SequentialClipboardHandler clipboardHandler)
	{
		return sequentialEditor.canImportFromSequentialEditor( clipboardHandler.sequentialEditor );
	}
	
	
	public Object copyStructuralValue(Object x)
	{
		return sequentialEditor.copyStructuralValue( x );
	}
	
	public SelectionEditTreeEvent createSelectionEditTreeEvent(DPElement sourceElement)
	{
		return sequentialEditor.createSelectionEditTreeEvent( sourceElement );
	}








	private void replaceSelection(Selection selection, Caret caret, Object replacement)
	{
		if ( selection instanceof TextSelection )
		{
			TextSelection ts = (TextSelection)selection;
			Marker startMarker = ts.getStartMarker();
			Marker endMarker = ts.getEndMarker();
			
			// Get the edit-level fragments that contain the start and end markers
			FragmentView startFragment = FragmentView.getEnclosingFragment( startMarker.getElement(), editLevelFragmentFilter );
			FragmentView endFragment = FragmentView.getEnclosingFragment( endMarker.getElement(), editLevelFragmentFilter );
			
			// Determine the 
			FragmentView editRootFragment = null;
			DPElement editRootFragmentElement = null;
			if ( startFragment == endFragment )
			{
				editRootFragment = startFragment;
			}
			else
			{
				// Get the common root edit-level fragment, and its content element
				editRootFragment = FragmentView.getCommonRootFragment( startFragment, endFragment, commonRootEditLevelFragmentFilter );
			}
			editRootFragmentElement = editRootFragment.getFragmentContentElement();

			
			if ( replacement != null )
			{
				StreamValue replacementStream = null;
				if ( replacement instanceof StreamValue )
				{
					replacementStream = (StreamValue)replacement;
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
					StreamValueVisitor visitor = new StreamValueVisitor();
					StreamValue before = visitor.getStreamValueFromStartToMarker( editRootFragmentElement, startMarker );
					StreamValue after = visitor.getStreamValueFromMarkerToEnd( editRootFragmentElement, endMarker );
					
					// Join
					StreamValue joinedStream = joinStreamsForInsertion( editRootFragment, before, replacementStream, after );
					
					// Create the event
					SelectionEditTreeEvent event = createSelectionEditTreeEvent( editRootFragmentElement );
					// Store the joined stream in the structural value of the root element
					event.getStreamValueVisitor().setElementFixedValue( editRootFragmentElement, joinedStream );
					// Take a copy of the end marker
					Marker end = endMarker.copy();
					// Clear the selection
					ts.clear();
					// Move the caret to the end
					caret.moveTo( end );
					// Post a tree event
					editRootFragmentElement.postTreeEvent( event );
				}
			}
			else
			{
				// Get the item streams for the root element content, before and after the selected region
				StreamValueVisitor visitor = new StreamValueVisitor();
				StreamValue before = visitor.getStreamValueFromStartToMarker( editRootFragmentElement, startMarker );
				StreamValue after = visitor.getStreamValueFromMarkerToEnd( editRootFragmentElement, endMarker );
				
				StreamValue joinedStream = joinStreamsForDeletion( editRootFragment, before, after );
				
				// Create the event
				SelectionEditTreeEvent event = createSelectionEditTreeEvent( editRootFragmentElement );
				// Store the joined stream in the structural value of the root element
				event.getStreamValueVisitor().setElementFixedValue( editRootFragmentElement, joinedStream );
				// Take a copy of the end marker
				Marker end = endMarker.copy();
				// Clear the selection
				ts.clear();
				// Move the caret to the end
				caret.moveTo( end );
				// Post a tree event
				editRootFragmentElement.postTreeEvent( event );
			}
		}
	}
	
	

	private void insertAtCaret(Caret caret, Object data)
	{
		StreamValue stream = null;
		
		
		if ( data instanceof StreamValue )
		{
			stream = (StreamValue)data;
		}
		else if ( data instanceof String )
		{
			StreamValueBuilder builder = new StreamValueBuilder();
			builder.appendTextValue( (String)data );
			stream = builder.stream();
		}
		
			
		if ( stream != null )
		{
			Marker caretMarker = caret.getMarker();
			FragmentView insertionPointFragment = FragmentView.getEnclosingFragment( caretMarker.getElement(), editLevelFragmentFilter );
			DPElement insertionPointElement = insertionPointFragment.getFragmentContentElement();
			
			// Get the item streams for the root element content, before and after the selected region
			StreamValueVisitor visitor = new StreamValueVisitor();
			StreamValue before = visitor.getStreamValueFromStartToMarker( insertionPointElement, caretMarker );
			StreamValue after = visitor.getStreamValueFromMarkerToEnd( insertionPointElement, caretMarker );
			
			StreamValue joinedStream = joinStreamsForInsertion( insertionPointFragment, before, stream, after );
			
			// Store the joined stream in the structural value of the root element
			SelectionEditTreeEvent event = createSelectionEditTreeEvent( insertionPointElement );
			event.getStreamValueVisitor().setElementFixedValue( insertionPointElement, joinedStream );
			// Move the caret to the start of the next item, to ensure that it is placed *after* the inserted content, once the insertion is done.
			caret.moveToStartOfNextItem();
			// Post a tree event
			insertionPointElement.postTreeEvent( event );
		};
	}
	
	
	protected boolean paste(Caret caret, Selection selection, Object data)
	{
		// Paste
		if ( !( selection instanceof TextSelection ) )
		{
			if ( caret.isValid() )
			{
				insertAtCaret( caret, data );
				return true;
			}
		}
		else
		{
			replaceSelection( selection, caret, data );
			return true;
		}
		
		return false;
	}
}
