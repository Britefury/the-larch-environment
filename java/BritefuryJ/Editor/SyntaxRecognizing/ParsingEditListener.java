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
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogEntry;
import BritefuryJ.Parser.ParseResult;
import BritefuryJ.Parser.ParserExpression;

public abstract class ParsingEditListener extends SRStreamEditListener
{
	protected ParserExpression parser;
	
	
	public ParsingEditListener(ParserExpression parser)
	{
		this.parser = parser;
	}
	
	
	protected String getLogName()
	{
		return null;
	}
	

	protected boolean isValueEmpty(DPElement element, FragmentView fragment, Object model, StreamValue value)
	{
		return getSyntaxRecognizingEditor().isValueEmpty( value );
	}
	
	protected boolean isValueValid(DPElement element, FragmentView fragment, Object model, StreamValue value)
	{
		return true;
	}
	
	
	protected Object postParseResult(Object value)
	{
		return value;
	}

	
	protected HandleEditResult handleEmptyValue(DPElement element, FragmentView fragment, EditEvent event, Object model)
	{
		return HandleEditResult.NOT_HANDLED;
	}
	
	protected abstract HandleEditResult handleParseSuccess(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value, Object parsed);
	
	
	protected HandleEditResult handleParseFailure(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value)
	{
		return HandleEditResult.NOT_HANDLED;
	}
	
	
	
	protected HandleEditResult handleValue(DPElement element, DPElement sourceElement, FragmentView fragment, EditEvent event, Object model, StreamValue value)
	{
		String logName = getLogName();
		if ( value.isEmpty()  ||  isValueEmpty( element, fragment, model, value ) )
		{
			if ( logName != null )
			{
				Log log = fragment.getView().getLog();
				if ( log.isRecording() )
				{
					log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " - deleted" ).vItem( "editedStream", value ) );
				}
			}
			return handleEmptyValue( element, fragment, event, model );
		}
		else if ( isValueValid( element, fragment, model, value ) )
		{
			Object parsed[] = parseStream( value );
			
			if ( parsed != null )
			{
				if ( logName != null )
				{
					Log log = fragment.getView().getLog();
					if ( log.isRecording() )
					{
						log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " - parse succeeded" ).vItem( "editedStream", value ).hItem( "parser", parser ).vItem( "parsed", parsed[0] ) );
					}
				}
				return handleParseSuccess( element, sourceElement, fragment, event, model, value, parsed[0] );
			}
			else
			{
				if ( logName != null )
				{
					Log log = fragment.getView().getLog();
					if ( log.isRecording() )
					{
						log.log( new LogEntry( getSequentialEditor().getName() ).hItem( "description", logName + " - parse failed" ).vItem( "editedStream", value ).hItem( "parser", parser ) );
					}
				}
				return handleParseFailure( element, sourceElement, fragment, event, model, value );
			}
		}
		else
		{
			return HandleEditResult.NOT_HANDLED;
		}
	}

	
	
	private Object[] parseStream(StreamValue value)
	{
		ParseResult res = parser.parseStreamItems( value );
		if ( res.isValid() )
		{
			if ( res.getEnd() == value.length() )
			{
				return new Object[] { postParseResult( res.getValue() ) };
			}
		}
		return null;
	}
	
	
	public ParserExpression getParser()
	{
		return parser;
	}
}
