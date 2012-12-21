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

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.FragmentViewFilter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelection;
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
			return dataFlavorForClass( sequentialController.getSelectionBufferType() );
		}

		@Override
		protected Object export(Object selectionContents)
		{
			return sequentialController.createSelectionBuffer( selectionContents );
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
			return sequentialController.sequentialToTextForExport( selectionContents );
		}

		@Override
		protected boolean canExport(Object selectionContents)
		{
			return sequentialController.canConvertSequentialToTextForExport( selectionContents );
		}
	};

	
	
	@SuppressWarnings("unchecked")
	private List<? extends DataExporterInterface<Object>> dataExporters = (List<? extends DataExporterInterface<Object>>)Arrays.asList( sequentialExporter, stringExporter );
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
	
	
	
	
	
	private AbstractDataImporter<Caret> richStringImporter = new AbstractDataImporter<Caret>()
	{
		@Override
		protected boolean canImportFlavor(DataFlavor flavor)
		{
			return flavor.equals( dataFlavorForClass( sequentialController.getSelectionBufferType() ) );
		}

		@Override
		protected boolean importCheckedData(Caret caret, Selection selection, Object data)
		{
			SequentialBuffer buffer = (SequentialBuffer)data;
			if ( !sequentialController.canImportFromSequentialEditor( buffer.clipboardHandler.sequentialController ) )
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
				return paste( caret, selection, sequentialController.textToSequentialForImport( ((String)data) ) );
			}
			return false;
		}
	};
	
	
	@SuppressWarnings("unchecked")
	private List<? extends DataImporterInterface<Caret>> dataImporters = (List<? extends DataImporterInterface<Caret>>)
			Arrays.asList( richStringImporter, stringImporter );
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
				Object sequentialReplacement = sequentialController.textToSequentialForImport( replacement );
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
	
	
	private SequentialController sequentialController;
	
	
	
	
	
	public SequentialClipboardHandler(SequentialController sequentialController)
	{
		this.sequentialController = sequentialController;
		
		addExporter( exporter );
		addImporter( importer );
		addSelectionEditor( selectionEditor );
	}
	
	
	
	protected SequentialController getSequentialEditor()
	{
		return sequentialController;
	}
	
	
	private boolean isCommonRootEditLevelFragmentView(FragmentView fragment)
	{
		return isEditLevelFragmentView( fragment );
	}
	
	
	
	
	private boolean isEditLevelFragmentView(FragmentView fragment)
	{
		return sequentialController.isClipboardEditLevelFragmentView( fragment );
	}
	
	
	
	private boolean isSelectionConsistent(TextSelection selection)
	{
		LSRegion startRegion = selection.getStartMarker().getElement().getRegion();
		LSRegion endRegion = selection.getEndMarker().getElement().getRegion();
		return startRegion == endRegion  &&  startRegion.getClipboardHandler() == this;
	}
	
	private Object getSequentialContentInSelection(TextSelection selection)
	{
		LSElement root = selection.getCommonRoot();
		FragmentView fragment = FragmentView.getEnclosingFragment( root, editLevelFragmentFilter );
		if ( isSelectionConsistent( selection ) )
		{
			return sequentialController.getSequentialContentInSelection( fragment, fragment.getFragmentContentElement(), selection );
		}
		else
		{
			return null;
		}
	}

	private void replaceSelection(Selection selection, Caret caret, Object replacement)
	{
		if ( selection instanceof TextSelection )
		{
			TextSelection ts = (TextSelection)selection;
			
			if ( ts.isValid()  &&  ts.isEditable()  &&  isSelectionConsistent( ts ) )
			{
				Marker startMarker = ts.getStartMarker();
				Marker endMarker = ts.getEndMarker();
				
				// Get the edit-level fragments that contain the start and end markers
				FragmentView startFragment = FragmentView.getEnclosingFragment( startMarker.getElement(), editLevelFragmentFilter );
				FragmentView endFragment = FragmentView.getEnclosingFragment( endMarker.getElement(), editLevelFragmentFilter );
				
				// Determine the 
				FragmentView editRootFragment = null;
				LSElement editRootFragmentElement = null;
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
					Object spliced = sequentialController.spliceForInsertion( editRootFragment, editRootFragmentElement, startMarker, endMarker, replacement );
					
					// Create the event
					SelectionEditTreeEvent event = sequentialController.createSelectionEditTreeEvent( editRootFragmentElement );
					// Store the spliced content in the structural value of the root element
					event.getRichStringVisitor().setElementFixedValue( editRootFragmentElement, spliced );
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
					Object spliced = sequentialController.spliceForDeletion( editRootFragment, editRootFragmentElement, startMarker, endMarker );
	
					// Create the event
					SelectionEditTreeEvent event = sequentialController.createSelectionEditTreeEvent( editRootFragmentElement );
					// Store the joined rich string in the structural value of the root element
					event.getRichStringVisitor().setElementFixedValue( editRootFragmentElement, spliced );
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
	}
	
	

	private void insertAtCaret(Caret caret, Object data)
	{
		if ( data != null )
		{
			Marker caretMarker = caret.getMarker();
			FragmentView insertionPointFragment = FragmentView.getEnclosingFragment( caretMarker.getElement(), editLevelFragmentFilter );
			LSElement insertionPointElement = insertionPointFragment.getFragmentContentElement();
			
			// Splice the content before the insertion point, the inserted content, and the content after the insertion point
			Object spliced = sequentialController.spliceForInsertion( insertionPointFragment, insertionPointElement, caretMarker, caretMarker, data );
			
			// Store the spliced content in the structural value of the root element
			SelectionEditTreeEvent event = sequentialController.createSelectionEditTreeEvent( insertionPointElement );
			event.getRichStringVisitor().setElementFixedValue( insertionPointElement, spliced );
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
