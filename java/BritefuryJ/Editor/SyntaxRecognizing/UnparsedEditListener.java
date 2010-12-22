//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;

public abstract class UnparsedEditListener extends SRStreamEditListener
{
	protected String getLogName()
	{
		return null;
	}
	
	protected boolean isValueValid(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return true;
	}
	
	protected boolean isValueEmpty(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return getSyntaxRecognizingEditor().isValueEmpty( value );
	}
	
	protected HandleEditResult handleInvalidValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return HandleEditResult.NOT_HANDLED;
	}
	
	protected abstract HandleEditResult handleUnparsed(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value);
	
	protected abstract HandleEditResult handleInnerUnparsed(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value);
	
	

	@Override
	protected HandleEditResult handleValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		String logName = getLogName();
		if ( isValueValid( element, sourceElement, fragment, event, model, value ) )
		{
			// Attempt to create an unparsed node to replace only the node corresponding to the innermost fragment surrounding
			// the element that sent the edit event
			GSymFragmentView sourceFragment = (GSymFragmentView)sourceElement.getFragmentContext();
			if ( sourceFragment == fragment )
			{
				// Source fragment is this fragment - replace this node with an unparsed node
				if ( logName != null )
				{
					Log log = fragment.getView().getPageLog();
					if ( log.isRecording() )
					{
						log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " (unparsed) - apply to model" ).vItem( "editedStream", value ) );
					}
				}
				return handleUnparsed( element, sourceElement, fragment, event, model, value );
			}
			else
			{
				DPElement sourceFragmentElement = sourceFragment.getFragmentContentElement();
				Object sourceModel = sourceFragment.getModel();
				StreamValue sourceValue = sourceFragmentElement.getStreamValue();
				
				if ( value.isEmpty()  ||  isValueEmpty( sourceFragmentElement, sourceElement, sourceFragment, event, sourceModel, sourceValue ) )
				{
					// Value is empty - replace this node with an unparsed node
					if ( logName != null )
					{
						Log log = fragment.getView().getPageLog();
						if ( log.isRecording() )
						{
							log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " (unparsed) - sub-model deleted" ).vItem( "editedStream", value ) );
						}
					}
					return handleUnparsed( element, sourceElement, fragment, event, model, value );
				}
				
				// Value is valid - replace the innermost node with an unparsed node
				if ( logName != null )
				{
					Log log = fragment.getView().getPageLog();
					if ( log.isRecording() )
					{
						log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " (unparsed) - apply to sub-model" ).vItem( "editedStream", value ) );
					}
				}
				return handleInnerUnparsed( sourceFragmentElement, sourceElement, sourceFragment, event, sourceModel, sourceValue );
			}
		}
		else
		{
			if ( logName != null )
			{
				Log log = fragment.getView().getPageLog();
				if ( log.isRecording() )
				{
					log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " (unparsed) - invalid" ).vItem( "editedStream", value ) );
				}
			}
			
			return handleInvalidValue( element, sourceElement, fragment, event, model, value );
		}
	}
}
