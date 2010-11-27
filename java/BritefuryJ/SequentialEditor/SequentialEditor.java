//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.SequentialEditor;

import java.awt.datatransfer.DataFlavor;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Combinators.Primitive.Region;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.GSym.View.GSymFragmentView;

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
	
	public SequentialEditor(DataFlavor bufferFlavor)
	{
		this.clipboardHandler = new SequentialClipboardHandler( this, bufferFlavor );
	}
	
	
	
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
	
	

	
	protected boolean isEditEvent(EditEvent event)
	{
		return false;
	}

	protected boolean isSelectionEditEvent(EditEvent event)
	{
		return getSelectionEditTreeEventClass().isInstance( event );
	}



	
	//
	//
	// CLIPBOARD EDIT METHODS  --  CAN OVERRIDE
	//
	//
	
	protected abstract Class<? extends SelectionEditTreeEvent> getSelectionEditTreeEventClass();
	
	protected boolean isClipboardEditLevelFragmentView(GSymFragmentView fragment)
	{
		return true;
	}

	protected String filterTextForImport(String text)
	{
		return text;
	}

	protected SequentialBuffer createSelectionBuffer(StreamValue stream)
	{
		return new SequentialBuffer( stream, clipboardHandler );
	}
	
	public boolean canImportFromSequentialEditor(SequentialEditor editor)
	{
		return editor == this;
	}
	
	public abstract Object copyStructuralValue(Object x);

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


	protected abstract SelectionEditTreeEvent createSelectionEditTreeEvent(DPElement sourceElement);



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
