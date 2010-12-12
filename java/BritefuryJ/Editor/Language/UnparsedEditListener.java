//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Language;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.Editor.Sequential.StreamEditListener;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;

public abstract class UnparsedEditListener extends StreamEditListener
{
	protected String getLogName()
	{
		return null;
	}
	
	protected boolean testValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return true;
	}
	
	protected boolean testValueEmpty(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return false;
	}
	
	protected abstract HandleEditResult handleInvalidValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value);
	
	protected abstract HandleEditResult handleUnparsed(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value);
	
	

	@Override
	protected HandleEditResult handleValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		String logName = getLogName();
		if ( testValue( element, sourceElement, fragment, event, model, value ) )
		{
			// Only edit the innermost node around the element that is the source of the event
			GSymFragmentView sourceFragment = (GSymFragmentView)sourceElement.getFragmentContext();
			if ( sourceFragment == fragment )
			{
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
				
				if ( sourceValue.isTextual() )
				{
					if ( testValueEmpty( sourceFragmentElement, sourceElement, sourceFragment, event, sourceModel, sourceValue ) )
					{
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
				}
				
				if ( logName != null )
				{
					Log log = fragment.getView().getPageLog();
					if ( log.isRecording() )
					{
						log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " (unparsed) - apply to sub-model" ).vItem( "editedStream", value ) );
					}
				}
				return handleUnparsed( sourceFragmentElement, sourceElement, sourceFragment, event, sourceModel, sourceValue );
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
