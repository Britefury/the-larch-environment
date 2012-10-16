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
import BritefuryJ.LSpace.SequentialRichStringVisitor;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;
import BritefuryJ.Util.RichString.RichString;

public abstract class UnparsedEditFilter extends SRRichStringEditFilter
{
	protected String getLogName()
	{
		return null;
	}
	
	protected boolean isValueValid(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, RichString value)
	{
		return true;
	}
	
	protected boolean isValueEmpty(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, RichString value)
	{
		return getSyntaxRecognizingController().isValueEmpty( value );
	}
	
	protected boolean shouldApplyToInnerFragment(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, RichString value)
	{
		return true;
	}
	
	protected HandleEditResult handleInvalidValue(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, RichString value)
	{
		return HandleEditResult.NOT_HANDLED;
	}
	
	protected abstract HandleEditResult handleUnparsed(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, RichString value);
	
	protected abstract HandleEditResult handleInnerUnparsed(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, RichString value);
	
	

	@Override
	protected HandleEditResult handleRichStringEdit(LSElement element, LSElement sourceElement, FragmentView fragment,
			EditEvent event, Object model, RichString value)
	{
		String logName = getLogName();
		if ( isValueValid( element, sourceElement, fragment, event, model, value ) )
		{
			// Attempt to create an unparsed node to replace only the node corresponding to the innermost fragment surrounding
			// the element that sent the edit event
			FragmentView sourceFragment = (FragmentView)sourceElement.getFragmentContext();
			if ( sourceFragment == fragment  ||  !shouldApplyToInnerFragment( element, sourceElement, fragment, event, model, value ) )
			{
				// Either:
				// - Source fragment is this fragment
				// or:
				// - This filter should only apply to current fragment, not inner fragments.
				// Replace this model with an unparsed model
				if ( logName != null )
				{
					Log log = fragment.getView().getLog();
					if ( log.isRecording() )
					{
						log.log( new LogEntry( getSequentialController().getName() ).hItem( "description", logName + " (unparsed) - apply to model" ).vItem( "editedRichStr", value ) );
					}
				}
				return handleUnparsed( element, sourceElement, fragment, event, model, value );
			}
			else
			{
				LSElement sourceFragmentElement = sourceFragment.getFragmentContentElement();
				Object sourceModel = sourceFragment.getModel();
				SequentialRichStringVisitor visitor = event.getRichStringVisitor();
				RichString sourceValue = visitor.getRichString( sourceFragmentElement );
				
				if ( sourceValue.isEmpty()  ||  isValueEmpty( sourceFragmentElement, sourceElement, sourceFragment, event, sourceModel, sourceValue ) )
				{
					// Value is empty - replace this node with an unparsed node
					if ( logName != null )
					{
						Log log = fragment.getView().getLog();
						if ( log.isRecording() )
						{
							log.log( new LogEntry( getSequentialController().getName() ).hItem( "description", logName + " (unparsed) - sub-model deleted" ).vItem( "editedRichStr", value ) );
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
						log.log( new LogEntry( getSequentialController().getName() ).hItem( "description", logName + " (unparsed) - apply to sub-model" ).vItem( "editedRichStr", sourceValue ) );
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
					log.log( new LogEntry( getSequentialController().getName() ).hItem( "description", logName + " (unparsed) - invalid" ).vItem( "editedRichStr", value ) );
				}
			}
			
			return handleInvalidValue( element, sourceElement, fragment, event, model, value );
		}
	}
}
