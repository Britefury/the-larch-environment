//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Map;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.HorizontalField;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBoxWithFields;
import BritefuryJ.GSym.GenericPerspective.PresCom.VerticalField;
import BritefuryJ.GSym.PresCom.InnerFragment;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Parser.TracedParseResultView.ParseView;
import BritefuryJ.ParserHelpers.TraceNode;
import BritefuryJ.ParserHelpers.TracedParseResultInterface;

public class TracedParseResult extends ParseResult implements TracedParseResultInterface, Presentable
{
	public TraceNode traceNode;
	
	
	protected TracedParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, boolean bMerge, Map<String, Object> bindings, TraceNode traceNode)
	{
		super( value, begin, end, bSuppressed, bValid, bMerge, bindings );
		this.traceNode = traceNode;
	}
	

	public TraceNode getDebugNode()
	{
		return traceNode;
	}


	@Override
	public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres fields[];
		
		if ( isValid() )
		{
			Pres status = parseResultStyle.applyTo( new HorizontalField( "Status:", successStyle.applyTo( new StaticText( "Success" ) ) ) );
			Pres range = parseResultStyle.applyTo( new HorizontalField( "Range:",
					new Paragraph( new Pres[] { 
							rangeStyle.applyTo( new StaticText( String.valueOf( getBegin() ) ) ),
							new StaticText( " to " ),
							rangeStyle.applyTo( new StaticText( String.valueOf( getEnd() ) ) ) } ) ) );
			
			Pres valueView = new InnerFragment( getValue() );
			Pres value = parseResultStyle.applyTo( new VerticalField( "Value:", valueView ) );
			Pres trace = parseResultStyle.applyTo( new VerticalField( "Trace:", ParseView.presentTracedParseResult( this, fragment, inheritedState ) ) );
			fields = new Pres[] { status, range, value, trace.alignHExpand().alignVExpand() };
		}
		else
		{
			Pres status = parseResultStyle.applyTo( new HorizontalField( "Status:", failStyle.applyTo( new StaticText( "Fail" ) ) ) );
			Pres trace = parseResultStyle.applyTo( new VerticalField( "Trace:", ParseView.presentTracedParseResult( this, fragment, inheritedState ) ) );
			fields = new Pres[] { status, trace.alignHExpand().alignVExpand() };
		}
		
		return parseResultStyle.applyTo( new ObjectBoxWithFields( "BritefuryJ.Parser.ParseResult", fields ) ).alignHExpand();
	}
}