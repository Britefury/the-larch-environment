//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Parser.ParseResult;
import BritefuryJ.Parser.ParserExpression;

public abstract class ParsingEditListener extends StreamEditListener
{
	protected ParserExpression parser;
	
	
	public ParsingEditListener(ParserExpression parser)
	{
		this.parser = parser;
	}
	
	
	protected boolean testValueEmpty(DPElement element, GSymFragmentView fragment, Object model, StreamValue value)
	{
		return false;
	}
	
	protected boolean testValue(DPElement element, GSymFragmentView fragment, Object model, StreamValue value)
	{
		return true;
	}
	
	
	protected Object postParseResult(Object value)
	{
		return value;
	}

	
	protected boolean clearFixedValuesOnPath()
	{
		return true;
	}

	protected HandleEditResult handleEmptyValue(DPElement element, GSymFragmentView fragment, EditEvent event, Object model)
	{
		return HandleEditResult.NOT_HANDLED;
	}
	
	protected abstract HandleEditResult handleParseSuccess(DPElement element, DPElement sourceElement, GSymFragmentView fragment, EditEvent event, Object model, StreamValue value, Object parsed);
	
	
	protected HandleEditResult handleParseFailure(DPElement element, DPElement sourceElement, GSymFragmentView fragment, EditEvent event, Object model, StreamValue value)
	{
		return HandleEditResult.NOT_HANDLED;
	}
	
	
	
	protected HandleEditResult handleValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment, EditEvent event, Object model, StreamValue value)
	{
		if ( value.isEmpty()  ||  testValueEmpty( element, fragment, model, value ) )
		{
			return handleEmptyValue( element, fragment, event, model );
		}
		else if ( testValue( element, fragment, model, value ) )
		{
			Object parsed[] = parseStream( value );
			
			if ( parsed != null )
			{
				return handleParseSuccess( element, sourceElement, fragment, event, model, value, parsed[0] );
			}
			else
			{
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
