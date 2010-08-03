//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.GenericStyle;
import BritefuryJ.GSym.GenericPerspective.PresCom.HorizontalField;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBoxWithFields;
import BritefuryJ.GSym.GenericPerspective.PresCom.VerticalField;
import BritefuryJ.GSym.PresCom.InnerFragment;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParseResultInterface;

public class ParseResult implements ParseResultInterface, Presentable
{
	protected static class NameAlreadyBoundException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected Object value;
	protected int begin, end;
	protected boolean bSuppressed, bValid, bMerge;
	protected Map<String, Object> bindings;
	
	
	public ParseResult()
	{
		value = null;
		begin = end = 0;
		bSuppressed = false;
		bValid = false;
	}
	
	public ParseResult(Object value, int begin, int end)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		bSuppressed = false;
		bValid = true;
	}
	
	public ParseResult(Object value, int begin, int end, Map<String, Object> bindings)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		bSuppressed = false;
		bValid = true;
		this.bindings = bindings;
	}
	
	
	private ParseResult(int end)
	{
		this.value = null;
		this.begin = 0;
		this.end = end;
		bSuppressed = false;
		bValid = false;
	}

	private ParseResult(Object value, int begin, int end, boolean bSuppressed)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = true;
	}
	
	private ParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = bValid;
	}
	
	protected ParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, boolean bMerge, Map<String, Object> bindings)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = bValid;
		this.bMerge = bMerge;
		this.bindings = bindings;
	}
	
	
	
	protected ParseResult actionValue(Object v, boolean bMergeUp)
	{
		return new ParseResult( v, begin, end, false, true, bMergeUp, bindings );
	}
	
	protected ParseResult withRange(int begin, int end)
	{
		return new ParseResult( value, begin, end, false, true, bMerge, bindings );
	}
	
	
	
	protected ParseResult suppressed()
	{
		return new ParseResult( value, begin, end, true, bValid, bMerge, bindings );
	}
	
	protected ParseResult peek()
	{
		return new ParseResult( null, begin, begin, true, true, bMerge, bindings );
	}
	
	
	protected ParseResult bind(String name, Object bindingValue)
	{
		HashMap<String, Object> b = new HashMap<String, Object>();
		
		if ( bindings != null )
		{
			b.putAll( bindings );
		}
		
		b.put( name, bindingValue );
		
		return new ParseResult( value, begin, end, bSuppressed, bValid, bMerge, b );
	}
	
	
	protected ParseResult bindValueTo(String name)
	{
		return bind( name, getValue() );
	}
	
	
	protected ParseResult clearBindings()
	{
		if ( bindings == null )
		{
			return this;
		}
		else
		{
			return new ParseResult( value, begin, end, bSuppressed, bValid );
		}
	}
	
	
	
	public static Map<String, Object> addBindings(Map<String, Object> a, Map<String, Object> b)
	{
		if ( a == null )
		{
			return b;
		}
		else
		{
			if ( b == null )
			{
				return a;
			}
			else
			{
				HashMap<String, Object> x = new HashMap<String, Object>();
				x.putAll( a );
				x.putAll( b );
				return x;
			}
		}
	}
	
	
	protected DebugParseResult debug(DebugNode debugNode)
	{
		return new DebugParseResult( value, begin, end, bSuppressed, bValid, bMerge, bindings, debugNode );
	}
	
	
	
	public Object getValue()
	{
		return value;
	}
	
	public int getBegin()
	{
		return begin;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public boolean isSuppressed()
	{
		return bSuppressed;
	}

	public boolean isValid()
	{
		return bValid;
	}
	
	public boolean isMergeable()
	{
		return bMerge  &&  value instanceof List<?>;
	}
	
	public Map<String, Object> getBindings()
	{
		return bindings;
	}
	
	
	
	public static ParseResult failure(int end)
	{
		return new ParseResult( end );
	}
	
	public static ParseResult suppressedNoValue(int begin, int end)
	{
		return new ParseResult( null, begin, end, true );
	}
	
	public static ParseResult mergeableValue(Object value, int begin, int end, HashMap<String, Object> bindings)
	{
		return new ParseResult( value, begin, end, false, true, true, bindings );
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
			fields = new Pres[] { status, range, value };
		}
		else
		{
			fields = new Pres[] { parseResultStyle.applyTo( new HorizontalField( "Status:", failStyle.applyTo( new StaticText( "Fail" ) ) ) ) };
		}
		
		return parseResultStyle.applyTo( new ObjectBoxWithFields( "BritefuryJ.Parser.ParseResult", fields ) );
	}


	private static StyleSheet2 successStyle = StyleSheet2.instance.withAttr( Primitive.fontItalic, true ).withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static StyleSheet2 failStyle = StyleSheet2.instance.withAttr( Primitive.fontItalic, true ).withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.0f ) );
	private static StyleSheet2 rangeStyle = StyleSheet2.instance.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.5f ) );
	private static StyleSheet2 parseResultStyle = StyleSheet2.instance.withAttr( GenericStyle.objectTitlePaint, new Color( 0.4f, 0.4f, 0.4f ) ).withAttr( GenericStyle.objectBorderPaint, new Color( 0.6f, 0.6f, 0.6f ) );
}
