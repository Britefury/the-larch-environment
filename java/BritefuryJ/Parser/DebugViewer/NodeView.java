//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.DebugViewer;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.BorderStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.Parser.DebugNode;
import BritefuryJ.Parser.ParseResult;

public class NodeView
{
	static int MAX_STRING_LENGTH = 16;
	
	static TextStyleSheet debugNameStyle = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 24 ), Color.blue );
	static TextStyleSheet classNameStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), new Color( 0.0f, 0.0f, 0.5f ) );
	static TextStyleSheet rangeStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
	static TextStyleSheet inputStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
	static TextStyleSheet valueStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
	static TextStyleSheet failStyle = new TextStyleSheet( new Font( "Sans serif", Font.ITALIC, 12 ), new Color( 0.5f, 0.0f, 0.0f ) );
	static HBoxStyleSheet titleTextHBoxStyle = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 10.0, false, 0.0 );
	static VBoxStyleSheet titleVBoxStyle = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.CENTRE, 0.0, false, 0.0, new Color( 0.8f, 0.8f, 0.8f ) );
	static VBoxStyleSheet contentVBoxStyle = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
	static VBoxStyleSheet nodeVBoxStyle = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 0.0, false, 0.0, Color.white );
	static BorderStyleSheet nodeBorderStyle = new BorderStyleSheet( 1.0, 1.0, 1.0, 1.0, Color.black );
	
	static VBoxStyleSheet childrenVBoxStyle = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 3.0, false, 5.0 );
	static HBoxStyleSheet mainHBoxStyle = new HBoxStyleSheet( DPHBox.Alignment.CENTRE, 80.0, false, 0.0 );
	
	
	
	private DPWidget nodeWidget, mainWidget;
	private ParseView parseView;
	private Vector<NodeView> children;
	private DebugNode data;
	private String input;

	
	
	public NodeView(ParseView parseView, DebugNode data, String input)
	{
		this.parseView = parseView;
		this.data = data;
		this.input = input;
		
		nodeWidget = makeNodeWidget( data, input );
		
		Vector<DPWidget> childWidgets = new Vector<DPWidget>();
		children = new Vector<NodeView>();
		for (DebugNode child: data.getCallChildren())
		{
			NodeView childView = parseView.getNodeView( child, input );
			children.add( childView );
			childWidgets.add( childView.getWidget() );
		}
		
		DPVBox childrenVBox = new DPVBox( childrenVBoxStyle );
		childrenVBox.setChildren( childWidgets );
		
		DPHBox mainHBox = new DPHBox( mainHBoxStyle );
		DPWidget[] mainChildren = { nodeWidget, childrenVBox };
		mainHBox.setChildren( Arrays.asList( mainChildren ) );
		
		mainWidget = mainHBox;
	}
	
	
	public DPWidget getWidget()
	{
		return mainWidget;
	}
	
	protected DPWidget getNodeWidget()
	{
		return nodeWidget;
	}
	
	
	void registerEdges()
	{
		for (NodeView child: children)
		{
			parseView.addCallEdge( this, child );
		}
		
		for (DebugNode child: data.getMemoChildren())
		{
			parseView.addMemoEdge( this, parseView.getNodeView( child, input ) );
		}

		for (NodeView child: children)
		{
			child.registerEdges();
		}
	}
	
	
	
	private DPWidget makeTitleWidget(DebugNode data)
	{
		String debugName = data.getExpression().getDebugName();
		
		String className = data.getExpression().getClass().getName();
		if ( className.contains( "." ) )
		{
			String[] nameParts = className.split( Pattern.quote( "." ) );
			className = nameParts[ nameParts.length - 1 ];
		}
		
		DPText classText = new DPText( classNameStyle, "[" + className + "]" );
		if ( debugName != "" )
		{
			DPText debugText = new DPText( debugNameStyle, debugName );
			DPHBox textBox = new DPHBox( titleTextHBoxStyle );
			DPWidget[] children = { debugText, classText };
			textBox.setChildren( Arrays.asList( children ) );
			return textBox;
		}
		else
		{
			return classText;
		}
	}
	
	private DPWidget makeTitleBoxWidget(DebugNode data)
	{
		DPWidget titleWidget = makeTitleWidget( data );
		
		DPVBox titleBoxWidget = new DPVBox( titleVBoxStyle );
		
		DPWidget[] children = { titleWidget };
		titleBoxWidget.setChildren( Arrays.asList( children ) );
		return titleBoxWidget;
	}
	
	private DPWidget makeRangeWidget(DebugNode data)
	{
		ParseResult result = data.getResult();
		String rangeText = "";
		
		if ( result.isValid() )
		{
			rangeText = ":" + String.valueOf( result.getEnd() );
		}
		else
		{
			rangeText = String.valueOf( result.getBegin() ) + "   :   " + String.valueOf( result.getEnd() );
		}

		return new DPText( rangeStyle, rangeText );
	}
	
	private DPWidget makeInputWidget(DebugNode data, String input)
	{
		input = input.substring( data.getResult().getBegin(), data.getResult().getEnd() );
		
		if ( input.length() > MAX_STRING_LENGTH )
		{
			input = input.substring( 0, MAX_STRING_LENGTH )  +  "...";
		}
		
		return new DPText( inputStyle, input );
	}

	private DPWidget makeValueWidget(DebugNode data)
	{
		ParseResult result = data.getResult();
		
		if ( result.isValid() )
		{
			String value = result.getValue().toString();
			if ( value.length() > MAX_STRING_LENGTH )
			{
				value = value.substring( 0, MAX_STRING_LENGTH )  +  "...";
			}
			return new DPText( valueStyle, value );
		}
		else
		{
			return new DPText( failStyle, "<fail>" );
		}
	}
	
	private DPWidget makeContentBoxWidget(DebugNode data, String input)
	{
		DPWidget rangeWidget = makeRangeWidget( data );
		DPWidget inputWidget = makeInputWidget( data, input );
		DPWidget valueWidget = makeValueWidget( data );
		
		DPVBox contentBoxWidget = new DPVBox( contentVBoxStyle );
		DPWidget[] children = { rangeWidget, inputWidget, valueWidget };
		contentBoxWidget.setChildren( Arrays.asList( children ) );
		return contentBoxWidget;
	}
	
	private DPWidget makeNodeWidget(DebugNode data, String input)
	{
		DPWidget titleBoxWidget = makeTitleBoxWidget( data );
		DPWidget contentBoxWidget = makeContentBoxWidget( data, input );

		DPVBox nodeBoxWidget = new DPVBox( nodeVBoxStyle );
		DPWidget[] children = { titleBoxWidget, contentBoxWidget };
		nodeBoxWidget.setChildren( Arrays.asList( children ) );
		
		DPBorder nodeBorderWidget = new DPBorder( nodeBorderStyle );
		nodeBorderWidget.setChild( nodeBoxWidget );
		
		return nodeBorderWidget;
	}
}
