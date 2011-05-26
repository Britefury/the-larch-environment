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
	private AbstractDataExporter<Object> sequentialExporter = new AbstractDataExporter<Object>()
	{
		@Override
		protected DataFlavor getDataFlavor()
		{
			return dataFlavorForClass( sequentialEditor.getSelectionBufferType() );
		}

		@Override
		protected Object export(Object selectionContents)
		{
			return sequentialEditor.createSelectionBuffer( selectionContents );
		}
		
	};

	
	private AbstractDataExporter<Object> stringExporter = new AbstractDataExporter<Object>()
	{
		@Override
		protected DataFlavor getDataFlavor()
		{
			return DataFlavor.stringFlavor;
		}

		@Override
		protected Object export(Object selectionContents)
		{
			return sequentialEditor.sequentialToTextForExport( selectionContents );
		}

		@Override
		protected boolean canExport(Object selectionContents)
		{
			return sequentialEditor.canConvertSequentialToTextForExport( selectionContents );
		}
	};

	
	
	@SuppressWarnings("unchecked")
	private List<? extends DataExporterInterface<Object>> dataExporters = (List<? extends DataExporterInterface<Object>>)
			Arrays.asList( sequentialExporter, stringExporter );
	private AbstractSelectionExporter<Object, TextSelection> exporter =
		new AbstractSelectionExporter<Object, TextSelection>( TextSelection.class, AbstractSelectionExporter.COPY_OR_MOVE, dataExporters )
	{
		@Override
		protected Object getSelectionContents(TextSelection selection)
		{
			return getSequentialContentInSelection( selection );
		}
		
		@Override
		protected void exportDone(TextSelection selection, Target target, Object selectionContents, int action)
		{
			if ( action == AbstractSelectionExporter.MOVE )
			{
				if ( target instanceof Caret )
				{
					deleteSelection( selection, target );
				}
			}
		}
	};
	
	
	
	
	
	private AbstractDataImporter<Caret> streamImporter = new AbstractDataImporter<Caret>()
	{
		@Override
		protected boolean canImportFlavor(DataFlavor flavor)
		{
			return flavor.equals( dataFlavorForClass( sequentialEditor.getSelectionBufferType() ) );
		}

		@Override
		protected boolean importCheckedData(Caret caret, Selection selection, Object data)
		{
			SequentialBuffer buffer = (SequentialBuffer)data;
			if ( !sequentialEditor.canImportFromSequentialEditor( buffer.clipboardHandler.sequentialEditor ) )
			{
				return false;
			}
			
			return paste( caret, selection, buffer.sequential );
		}
	};
	
	private AbstractDataImporter<Caret> stringImporter = new AbstractDataImporter<Caret>()
	{
		@Override
		protected boolean canImportFlavor(DataFlavor flavor)
		{
			return flavor.equals( DataFlavor.stringFlavor );
		}
		
		@Override
		protected boolean importCheckedData(Caret caret, Selection selection, Object data)
		{
			if ( data instanceof String )
			{
				return paste( caret, selection, sequentialEditor.textToSequentialForImport( ((String)data) ) );
			}
			return false;
		}
	};
	
	
	@SuppressWarnings("unchecked")
	private List<? extends DataImporterInterface<Caret>> dataImporters = (List<? extends DataImporterInterface<Caret>>)
			Arrays.asList( streamImporter, stringImporter );
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
				Object sequentialReplacement = sequentialEditor.textToSequentialForImport( replacement );
				replaceSelection( selection, (Caret)target, sequentialReplacement );
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
		public boolean testFragmentView(FragmentView fragment)
		{
			return isEditLevelFragmentView( fragment );
		}
	};
	
	
	private FragmentViewFilter commonRootEditLevelFragmentFilter = new FragmentViewFilter()
	{
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
	
	
	
	protected SequentialEditor getSequentialEditor()
	{
		return sequentialEditor;
	}
	
	
	private boolean isCommonRootEditLevelFragmentView(FragmentView fragment)
	{
		return isEditLevelFragmentView( fragment );
	}
	
	
	
	
	private boolean isEditLevelFragmentView(FragmentView fragment)
	{
		return sequentialEditor.isClipboardEditLevelFragmentView( fragment );
	}
	
	
	
	private Object getSequentialContentInSelection(TextSelection selection)
	{
		DPElement root = selection.getCommonRoot();
		FragmentView fragment = FragmentView.getEnclosingFragment( root, editLevelFragmentFilter );
		return sequentialEditor.getSequentialContentInSelection( fragment, fragment.getFragmentContentElement(), selection );
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
				// Splice the content before the selection, the inserted content, and the content after the selection
				Object spliced = sequentialEditor.spliceForInsertion( editRootFragment, editRootFragmentElement, startMarker, endMarker, replacement );
				
				// Create the event
				SelectionEditTreeEvent event = sequentialEditor.createSelectionEditTreeEvent( editRootFragmentElement );
				// Store the spliced content in the structural value of the root element
				event.getStreamValueVisitor().setElementFixedValue( editRootFragmentElement, spliced );
				// Take a copy of the end marker
				Marker end = endMarker.copy();
				// Clear the selection
				ts.clear();
				// Move the caret to the end
				caret.moveTo( end );
				// Post a tree event
				editRootFragmentElement.postTreeEvent( event );
			}
			else
			{
				// Splice the content around the selection
				Object spliced = sequentialEditor.spliceForDeletion( editRootFragment, editRootFragmentElement, startMarker, endMarker );

				// Create the event
				SelectionEditTreeEvent event = sequentialEditor.createSelectionEditTreeEvent( editRootFragmentElement );
				// Store the joined stream in the structural value of the root element
				event.getStreamValueVisitor().setElementFixedValue( editRootFragmentElement, spliced );
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
		if ( data != null )
		{
			Marker caretMarker = caret.getMarker();
			FragmentView insertionPointFragment = FragmentView.getEnclosingFragment( caretMarker.getElement(), editLevelFragmentFilter );
			DPElement insertionPointElement = insertionPointFragment.getFragmentContentElement();
			
			// Splice the content before the insertion point, the inserted content, and the content after the insertion point
			Object spliced = sequentialEditor.spliceForInsertion( insertionPointFragment, insertionPointElement, caretMarker, caretMarker, data );
			
			// Store the spliced content in the structural value of the root element
			SelectionEditTreeEvent event = sequentialEditor.createSelectionEditTreeEvent( insertionPointElement );
			event.getStreamValueVisitor().setElementFixedValue( insertionPointElement, spliced );
			// Move the caret to the start of the next item, to ensure that it is placed *after* the inserted content, once the insertion is done.
			caret.moveToStartOfNextItem();
			// Post a tree event
			insertionPointElement.postTreeEvent( event );
		}
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
