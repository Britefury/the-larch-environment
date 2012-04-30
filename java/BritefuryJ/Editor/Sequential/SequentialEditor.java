//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.Editor.Sequential.EditFilter.HandleEditResult;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSContentLeaf;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.SequentialRichStringVisitor;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.TextSelection;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.Util.RichString.RichString;

public abstract class SequentialEditor
{
	public static interface HandleEditEventFn
	{
		EditFilter.HandleEditResult handleEditEvent(LSElement element, LSElement sourceElement, EditEvent event);
	}
	
	public static interface HandleRichStringFn
	{
		HandleEditResult handleValue(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value);
	}
	
	
	
	protected static class ClearNeighbourEditEvent extends EditEvent
	{
		protected ClearNeighbourEditEvent(SequentialRichStringVisitor richStringVisitor)
		{
			super( richStringVisitor );
		}
	}
	
	
	protected class ClearStructuralValueListener implements TreeEventListener
	{
		@Override
		public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
		{
			if ( event instanceof EditEvent )
			{
				EditEvent editEvent = (EditEvent)event;
				if ( event instanceof TextEditEvent  ||  isSelectionEditEvent( editEvent )  ||  isEditEvent( editEvent )  ||  event instanceof ClearNeighbourEditEvent )
				{
					// If event is a selection edit event, and its source element is @element, then @element has had its fixed value
					// set by a SequentialClipboardHandler - so don't clear it.
					// Otherwise, clear it
					if ( !( isSelectionEditEvent( editEvent )  &&  getEventSourceElement( editEvent ) == element ) )
					{
						editEvent.getRichStringVisitor().ignoreElementFixedValue( element );
					}
				}
			}
			return false;
		}
	}
	
	
	
	protected static class ClearNeighbouringStructuralValueListener implements TreeEventListener
	{
		@Override
		public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
		{
			if ( event instanceof TextEditEvent )
			{
				TextEditEvent editEvent = (TextEditEvent)event;
				
				LSElement prevNeighbourCommonAncestor = editEvent.getPrevNeighbourCommonAncestor();
				LSElement nextNeighbourCommonAncestor = editEvent.getNextNeighbourCommonAncestor();
				
				ClearNeighbourEditEvent clearEvent = new ClearNeighbourEditEvent( editEvent.getRichStringVisitor() );
				
				LSRegion eventRegion = LSRegion.regionOf( sourceElement );
				
				if ( prevNeighbourCommonAncestor != null  &&  prevNeighbourCommonAncestor.isInSubtreeRootedAt( element ) )
				{
					LSContentLeaf prev = editEvent.getPrevNeighbour();
					if ( LSRegion.regionOf( prev )  ==  eventRegion )
					{
						prev.postTreeEventUntil( clearEvent, element );
					}
				}

				if ( nextNeighbourCommonAncestor != null  &&  nextNeighbourCommonAncestor.isInSubtreeRootedAt( element ) )
				{
					LSContentLeaf next = editEvent.getNextNeighbour();
					if ( LSRegion.regionOf( next )  ==  eventRegion )
					{
						next.postTreeEventUntil( clearEvent, element );
					}
				}
			}
			return false;
		}
	}
	
	
	
	
	
	
	protected class _FnEditFilter extends EditFilter
	{
		private HandleEditEventFn handleEditEventFn;
		
		
		protected _FnEditFilter(HandleEditEventFn handleEditEventFn)
		{
			this.handleEditEventFn = handleEditEventFn;
		}
		

		@Override
		protected SequentialEditor getSequentialEditor()
		{
			return SequentialEditor.this;
		}

		@Override
		protected HandleEditResult handleEdit(LSElement element, LSElement sourceElement, EditEvent event)
		{
			return handleEditEventFn.handleEditEvent( element, sourceElement, event );
		}
	}
	
	
	
	
	protected class _FnRichStringEditFilter extends RichStringEditFilter
	{
		private HandleRichStringFn handleRichStringFn;
		
		
		protected _FnRichStringEditFilter(HandleRichStringFn handleRichStringFn)
		{
			this.handleRichStringFn = handleRichStringFn;
		}
		

		@Override
		protected SequentialEditor getSequentialEditor()
		{
			return SequentialEditor.this;
		}

		@Override
		protected HandleEditResult handleRichStringEdit(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value)
		{
			return handleRichStringFn.handleValue( element, sourceElement, fragment, event, model, value );
		}
	}
	
	
	
	
	protected ClearStructuralValueListener clearListener = new ClearStructuralValueListener();
	protected static ClearNeighbouringStructuralValueListener clearNeighbourListener = new ClearNeighbouringStructuralValueListener();
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
	
	public static ClearNeighbouringStructuralValueListener getClearNeighbouringStructuralValueListener()
	{
		return clearNeighbourListener;
	}
	
	public boolean isClearNeighbouringStructuresEnabled()
	{
		return false;
	}
	
	
	
	public Region region(Object child)
	{
		return new Region( child, clipboardHandler );
	}
	
	
	
	public static SequentialEditor getEditorForElement(LSElement element)
	{
		SequentialClipboardHandler clipboardHandler = (SequentialClipboardHandler)element.getRegion().getClipboardHandler();
		return clipboardHandler.getSequentialEditor();
	}
	
	
	
	public EditFilter editFilter(HandleEditEventFn handleEditEventFn)
	{
		return new _FnEditFilter( handleEditEventFn );
	}
	
	public RichStringEditFilter richStringEditFilter(HandleRichStringFn handleRichStringFn)
	{
		return new _FnRichStringEditFilter( handleRichStringFn );
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

	protected SequentialBuffer createSelectionBuffer(Object richString)
	{
		return new SequentialBuffer( richString, clipboardHandler );
	}
	
	protected Class<? extends SequentialBuffer> getSelectionBufferType()
	{
		return SequentialBuffer.class;
	}
	
	public boolean canImportFromSequentialEditor(SequentialEditor editor)
	{
		return editor == this;
	}
	
	
	
	public abstract Object getSequentialContentInSelection(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, TextSelection selection);
	public abstract Object spliceForInsertion(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, Marker prefixEnd, Marker suffixStart, Object insertedContent);
	public abstract Object spliceForDeletion(FragmentView subtreeRootFragment, LSElement subtreeRootFragmentElement, Marker selectionStart, Marker selectionEnd);



	protected SelectionEditTreeEvent createSelectionEditTreeEvent(LSElement sourceElement)
	{
		return new SelectionEditTreeEvent( this, sourceElement );
	}



	protected static LSElement getEventSourceElement(EditEvent event)
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
