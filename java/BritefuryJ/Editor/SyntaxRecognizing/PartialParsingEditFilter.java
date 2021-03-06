//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Util.RichString.RichString;

public abstract class PartialParsingEditFilter extends ParsingEditFilter
{
	public PartialParsingEditFilter(ParserExpression parser)
	{
		super( parser );
	}

	
	@Override
	protected HandleEditResult handleParseSuccess(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value, Object parsed)
	{
		event.getRichStringVisitor().setElementFixedValue( element, parsed );
		return HandleEditResult.PASS_TO_PARENT;
	}
}
