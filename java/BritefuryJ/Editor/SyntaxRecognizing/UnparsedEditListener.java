//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.StreamValue.SequentialStreamValueVisitor;
import BritefuryJ.LSpace.StreamValue.StreamValue;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;

public abstract class UnparsedEditListener extends SRStreamEditListener
{
	protected String getLogName()
	{
		return null;
	}
	
	protected boolean isValueValid(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return true;
	}
	
	protected boolean isValueEmpty(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return getSyntaxRecognizingEditor().isValueEmpty( value );
	}
	
	protected boolean shouldApplyToInnerFragment(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return true;
	}
	
	protected HandleEditResult handleInvalidValue(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		return HandleEditResult.NOT_HANDLED;
	}
	
	protected abstract HandleEditResult handleUnparsed(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, StreamValue value);
	
	protected abstract HandleEditResult handleInnerUnparsed(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, StreamValue value);
	
	

	@Override
	protected HandleEditResult handleValue(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, StreamValue value)
	{
		String logName = getLogName();
		if ( isValueValid( element, sourceElement, fragment, event, model, value ) )
		{
			// Attempt to create an unparsed node to replace only the node corresponding to the innermost fragment surrounding
			// the element that sent the edit event
			FragmentView sourceFragment = (FragmentView)sourceElement.getFragmentContext();
			if ( sourceFragment == fragment  ||  !shouldApplyToInnerFragment( element, sourceElement, fragment, event, model, value ) )
			{
				// Source fragment is this fragment - replace this node with an unparsed node
				if ( logName != null )
				{
					Log log = fragment.getView().getLog();
					if ( log.isRecording() )
					{
						log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " (unparsed) - apply to model" ).vItem( "editedStream", value ) );
					}
				}
				return handleUnparsed( element, sourceElement, fragment, event, model, value );
			}
			else
			{
				LSElement sourceFragmentElement = sourceFragment.getFragmentContentElement();
				Object sourceModel = sourceFragment.getModel();
				SequentialStreamValueVisitor visitor = event.getStreamValueVisitor();
				StreamValue sourceValue = visitor.getStreamValue( sourceFragmentElement );
				
				if ( value.isEmpty()  ||  isValueEmpty( sourceFragmentElement, sourceElement, sourceFragment, event, sourceModel, sourceValue ) )
				{
					// Value is empty - replace this node with an unparsed node
					if ( logName != null )
					{
						Log log = fragment.getView().getLog();
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
					Log log = fragment.getView().getLog();
					if ( log.isRecording() )
					{
						log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " (unparsed) - apply to sub-model" ).vItem( "editedStream", sourceValue ) );
					}
				}
				return handleInnerUnparsed( sourceFragmentElement, sourceElement, sourceFragment, event, sourceModel, sourceValue );
			}
		}
		else
		{
			if ( logName != null )
			{
				Log log = fragment.getView().getLog();
				if ( log.isRecording() )
				{
					log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " (unparsed) - invalid" ).vItem( "editedStream", value ) );
				}
			}
			
			return handleInvalidValue( element, sourceElement, fragment, event, model, value );
		}
	}
}
