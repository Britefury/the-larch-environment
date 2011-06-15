//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Parser;

import java.awt.Color;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.regex.Pattern;

import BritefuryJ.Controls.AspectRatioScrolledViewport;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueAccessor;
import BritefuryJ.GraphViz.GraphViz;
import BritefuryJ.ParserHelpers.ParseResultInterface;
import BritefuryJ.ParserHelpers.TraceNode;
import BritefuryJ.ParserHelpers.TracedParseResultInterface;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class TraceGraphViewer
{
	static final int MAX_STRING_LENGTH = 64;
	
	static final double ASPECT_RATIO = 16.0 / 9.0;
	
	
	private IdentityHashMap<TraceNode, String> nodeToName = new IdentityHashMap<TraceNode, String>();
	
	
	private String escape(String data)
	{
		return data.replace( "|", "\\|" ).replace( "<", "\\<" ).replace( ">", "\\>" ).replace( "{", "\\{" ).replace( "}", "\\}" );
	}
	
	private StringBuilder clampedString(StringBuilder dot, String data)
	{
		if ( data.length() > MAX_STRING_LENGTH )
		{
			dot.append( data.substring( 0, MAX_STRING_LENGTH ) ).append( "..." );
		}
		else
		{
			dot.append( data );
		}
		
		return dot;
	}
	
	
	private StringBuilder title(StringBuilder dot, TraceNode data)
	{
		ParserExpression expr = data.getExpression();
		String exprName = expr.getExpressionName();
		
		if ( exprName != null )
		{
			dot.append( exprName );
		}
		
		String className = expr.getClass().getName();
		if ( className.contains( "." ) )
		{
			String[] nameParts = className.split( Pattern.quote( "." ) );
			className = nameParts[ nameParts.length - 1 ];
		}
		
		dot.append( "[" ).append( className ).append( "]" );
		
		return dot;
	}
	
	@SuppressWarnings("unchecked")
	private StringBuilder input(StringBuilder dot, TraceNode data)
	{
		ParseResultInterface result = data.getResult();
		dot.append( String.valueOf( result.getBegin() ) ).append( ":" ).append( String.valueOf( result.getEnd() ) ).append( "   " );
		
		Object inputObject = data.getInput();
		String inputString;
		
		if ( inputObject instanceof String )
		{
			inputString = (String)inputObject;
			inputString = inputString.substring( data.getResult().getBegin(), data.getResult().getEnd() );
		}
		else if ( inputObject instanceof StreamValueAccessor )
		{
			StreamValue stream = ((StreamValueAccessor)inputObject).getStream();
			inputString = stream.subStream( data.getResult().getBegin(), data.getResult().getEnd() ).toString();
		}
		else if ( inputObject instanceof List )
		{
			List<Object> subList = ((List<Object>)inputObject).subList( data.getResult().getBegin(), data.getResult().getEnd() );
			inputString = subList.toString();
		}
		else
		{
			inputString = inputObject.toString();
		}
		
		clampedString( dot, escape( inputString ) );
		
		return dot;
	}

	private StringBuilder value(StringBuilder dot, TraceNode data)
	{
		ParseResultInterface result = data.getResult();
		
		if ( result.isValid() )
		{
			Object value = result.getValue();
			String valueString = value == null  ?  "<null>"  :  value.toString();
			clampedString( dot, escape( valueString ) );
		}
		else
		{
			dot.append( "\\<FAIL\\>" );
		}
		
		return dot;
	}
	
	private StringBuilder label(StringBuilder dot, TraceNode data)
	{
		return value( input( title( dot.append( "<title> " ), data ).append( " | <input> "), data ).append( " | <value> " ), data );
	}
	
	private void node(StringBuilder dot, String name, TraceNode data)
	{
		dot.append( "\t\"" ).append( name ).append( "\"" ).append( " [\n" );
		label( dot.append( "\t\tlabel = \"" ), data ).append( "\"\n" );
		if ( data.getExpression() instanceof Production )
		{
			if ( data.getResult().isValid() )
			{
				dot.append( "\t\tcolor = \"#00a000\"\n" );
			}
			else
			{
				dot.append( "\t\tcolor = \"#e000e0\"\n" );
			}
		}
		else
		{
			if ( !data.getResult().isValid() )
			{
				dot.append( "\t\tcolor = red\n" );
			}
		}
		dot.append( "\t];\n" );
		
		
		for (TraceNode child: data.getCallChildren())
		{
			buildNode( dot, child );
		}
	}
	
	
	private StringBuilder edge(StringBuilder dot, TraceNode parent, TraceNode child, String attrs)
	{
		String parentName = nodeToName.get( parent );
		String childName = nodeToName.get( child );
		
		return dot.append( "\t\"" ).append( parentName ).append( "\" -> \"" ).append( childName ).append( "\" [ " ).append( attrs ).append( " ];\n" );
	}
	
	private StringBuilder callEdge(StringBuilder dot, TraceNode parent, TraceNode child)
	{
		return edge( dot, parent, child, "color=black style=bold" );
	}
	
	private StringBuilder memoEdge(StringBuilder dot, TraceNode parent, TraceNode child)
	{
		return edge( dot, parent, child, "color=darkgreen constraint=false" );
	}
	
	
	private void buildNodeEdges(StringBuilder dot, TraceNode data)
	{
		for (TraceNode child: data.getCallChildren())
		{
			callEdge( dot, data, child );
		}
	
		for (TraceNode child: data.getMemoChildren())
		{
			memoEdge( dot, data, child );
		}

		for (TraceNode child: data.getCallChildren())
		{
			buildNodeEdges( dot, child );
		}
	}
	
	
	private String buildNode(StringBuilder dot, TraceNode data)
	{
		String name = nodeToName.get( data );
		if ( name == null )
		{
			name = "node" + String.valueOf( nodeToName.size() );
			nodeToName.put( data, name );
			node( dot, name, data );
		}
		return name;
	}
	
	
	private static void begin(StringBuilder dot)
	{
		dot.append( "digraph g {\n" );
		dot.append( "\tgraph [ rankdir=LR  ordering=out ];\n" );
		dot.append( "\tnode [ fontsize=11  shape=record ];\n" );
		dot.append( "\tedge [ dir=back ];\n" );
		dot.append( "\n" );
	}
	
	private static void end(StringBuilder dot)
	{
		dot.append( "}\n" );
	}
	
	
	public static String traceGraphDot(TracedParseResultInterface result)
	{
		TraceGraphViewer g = new TraceGraphViewer();
		
		StringBuilder dot = new StringBuilder();
		begin( dot );
		g.buildNode( dot, result.getTraceNode() );
		g.buildNodeEdges( dot, result.getTraceNode() );
		end( dot );
		return dot.toString();
	}
	
	
	public static Pres traceView(TracedParseResultInterface result, PersistentState viewportState)
	{
		String dot = traceGraphDot( result );
		
		Object view = GraphViz.dot( dot );
		Pres viewport = new AspectRatioScrolledViewport( view, 0.0, ASPECT_RATIO, viewportState );
		return viewportBorderStyle.applyTo( new Border( viewport ) ).alignHExpand().alignVExpand();
	}


	private final static StyleSheet viewportBorderStyle = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.black, null ) );
}
