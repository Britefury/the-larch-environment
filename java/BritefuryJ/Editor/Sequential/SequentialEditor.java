//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Primitive.Region;

public abstract class SequentialEditor
{
	protected class ClearStructuralValueListener implements TreeEventListener
	{
		@Override
		public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
		{
			if ( event instanceof EditEvent )
			{
				EditEvent editEvent = (EditEvent)event;
				if ( event instanceof TextEditEvent  ||  isSelectionEditEvent( editEvent )  ||  isEditEvent( editEvent ) )
				{
					// If event is a selection edit event, and its source element is @element, then @element has had its fixed value
					// set by a SequentialClipboardHandler - so don't clear it.
					// Otherwise, clear it
					if ( !( isSelectionEditEvent( editEvent )  &&  getEventSourceElement( editEvent ) == element ) )
					{
						editEvent.getStreamValueVisitor().ignoreElementFixedValue( element );
					}
				}
			}
			return false;
		}
	}
	
	
	protected ClearStructuralValueListener clearListener = new ClearStructuralValueListener();
	protected SequentialClipboardHandler clipboardHandler;
	
	
	
	public SequentialEditor()
	{
		this.clipboardHandler = new SequentialClipboardHandler( this );
	}
	
	
	
	public abstract String getName();
	
	
	
	public SequentialClipboardHandler getClipboardHandler()
	{
		return clipboardHandler;
	}
	
	
	
	public ClearStructuralValueListener getClearStructuralValueListener()
	{
		return clearListener;
	}
	
	
	
	public Region region(Object child)
	{
		return new Region( child, clipboardHandler );
	}
	
	
	
	public static SequentialEditor getEditorForElement(DPElement element)
	{
		SequentialClipboardHandler clipboardHandler = (SequentialClipboardHandler)element.getRegion().getClipboardHandler();
		return clipboardHandler.getSequentialEditor();
	}
	
	

	
	protected boolean isEditEvent(EditEvent event)
	{
		return false;
	}

	protected boolean isSelectionEditEvent(EditEvent event)
	{
		if ( event instanceof SelectionEditTreeEvent )
		{
			SelectionEditTreeEvent selEvent = (SelectionEditTreeEvent)event;
			return selEvent.getSequentialEditor() == this;
		}
		else
		{
			return false;
		}
	}



	
	//
	//
	// CLIPBOARD EDIT METHODS  --  CAN OVERRIDE
	//
	//
	
	protected boolean isClipboardEditLevelFragmentView(FragmentView fragment)
	{
		return true;
	}

	protected abstract Object textToSequentialForImport(String text);
	protected abstract boolean canConvertSequentialToTextForExport(Object sequential);
	protected abstract String sequentialToTextForExport(Object sequential);

	protected SequentialBuffer createSelectionBuffer(Object stream)
	{
		return new SequentialBuffer( stream, clipboardHandler );
	}
	
	protected Class<? extends SequentialBuffer> getSelectionBufferType()
	{
		return SequentialBuffer.class;
	}
	
	public boolean canImportFromSequentialEditor(SequentialEditor editor)
	{
		return editor == this;
	}
	
	
	
	public abstract Object getSequentialContentInSelection(TextSelection selection);
	public abstract Object spliceForInsertion(FragmentView subtreeRootFragment, DPElement subtreeRootFragmentElement, Marker prefixEnd, Marker suffixStart, Object insertedContent);
	public abstract Object spliceForDeletion(FragmentView subtreeRootFragment, DPElement subtreeRootFragmentElement, Marker selectionStart, Marker selectionEnd);



	protected SelectionEditTreeEvent createSelectionEditTreeEvent(DPElement sourceElement)
	{
		return new SelectionEditTreeEvent( this, sourceElement );
	}



	protected static DPElement getEventSourceElement(EditEvent event)
	{
		if ( event instanceof SelectionEditTreeEvent )
		{
			return ((SelectionEditTreeEvent)event).getSourceElement();
		}
		else
		{
			throw new RuntimeException( "Cannot get event source element for an event that is not a SelectionEditTreeEvent" );
		}
	}
}
