//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ParserHelpers.TraceNode;
import BritefuryJ.ParserHelpers.ParseResultInterface;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectPresStyle;
import BritefuryJ.Pres.ObjectPres.HorizontalField;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.ObjectPres.VerticalField;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.StyleSheet.StyleSheet;

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

	protected ParseResult withValueFrom(ParseResult res)
	{
		return new ParseResult( res.value, begin, end, false, res.bValid, bMerge, bindings );
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
	
	
	protected TracedParseResult debug(TraceNode debugNode)
	{
		return new TracedParseResult( value, begin, end, bSuppressed, bValid, bMerge, bindings, debugNode );
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



	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres fields[];
		
		if ( isValid() )
		{
			Pres status = parseResultStyle.applyTo( new HorizontalField( "Status:", successStyle.applyTo( new Label( "Success" ) ) ) );
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


	protected static final StyleSheet successStyle = StyleSheet.style( Primitive.fontItalic.as( true ), Primitive.fontSize.as( 12 ), Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );
	protected static final StyleSheet failStyle = StyleSheet.style( Primitive.fontItalic.as( true ), Primitive.fontSize.as( 12 ), Primitive.foreground.as( new Color( 0.5f, 0.0f, 0.0f ) ) );
	protected static final StyleSheet rangeStyle = StyleSheet.style( Primitive.fontSize.as( 12 ), Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.5f ) ) );
	protected static final StyleSheet parseResultStyle = StyleSheet.style( ObjectPresStyle.objectTitlePaint.as( new Color( 0.4f, 0.4f, 0.4f ) ), ObjectPresStyle.objectBorderPaint.as( new Color( 0.6f, 0.6f, 0.6f ) ) );
}
