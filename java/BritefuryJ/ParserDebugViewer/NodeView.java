//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserDebugViewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParseResultInterface;

public class NodeView implements ElementContext
{
	private static class DPNodeBin extends DPBin
	{
		private NodeView nodeView;
		private boolean bSelected, bHighlight;
		
		public DPNodeBin(ElementContext context, NodeView nodeView)
		{
			super( context, ContainerStyleSheet.defaultStyleSheet );
			
			this.nodeView = nodeView;
			bSelected = false;
			bHighlight = false;
		}
		
		
		
		void select()
		{
			if ( !bSelected )
			{
				bSelected = true;
				queueFullRedraw();
			}
		}
		
		void unselect()
		{
			if ( bSelected )
			{
				bSelected = false;
				queueFullRedraw();
			}
		}
		
		
		
		protected boolean onButtonDown(PointerButtonEvent event)
		{
			boolean bResult = super.onButtonDown( event );
			
			if ( event.getButton() == 1 )
			{
				nodeView.onClicked();
				return true;
			}
			else
			{
				return bResult;
			}
		}
		
		protected void onEnter(PointerMotionEvent event)
		{
			super.onEnter( event );
			
			bHighlight = true;
			queueFullRedraw();
		}

		protected void onLeave(PointerMotionEvent event)
		{
			super.onLeave( event );

			bHighlight = false;
			queueFullRedraw();
		}


		
		protected void drawBackground(Graphics2D graphics)
		{
			Color backgroundColour = bHighlight  ?  new Color( 0.7f, 0.85f, 1.0f )  :  Color.white;
			backgroundColour = bSelected  ?  new Color( 1.0f, 1.0f, 0.6f )  :  backgroundColour;
			graphics.setColor( backgroundColour );
			graphics.fill( new Rectangle2D.Double( 0.0, 0.0, getAllocationX(), getAllocationY() ) );
		}
	}

	
	
	static int MAX_STRING_LENGTH = 64;
	
	static TextStyleSheet debugNameStyle = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 24 ), Color.blue );
	static TextStyleSheet classNameStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 18 ), new Color( 0.0f, 0.0f, 0.5f ) );
	static TextStyleSheet rangeStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
	static TextStyleSheet inputStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
	static TextStyleSheet valueStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.black );
	static TextStyleSheet failStyle = new TextStyleSheet( new Font( "Sans serif", Font.ITALIC, 16 ), new Color( 0.5f, 0.0f, 0.0f ) );
	static HBoxStyleSheet titleTextHBoxStyle = new HBoxStyleSheet( 10.0 );
	static Border titleSuccessBorder = new EmptyBorder( 0.0, 0.0, 0.0, 0.0, new Color( 0.85f, 0.95f, 0.85f ) );
	static Border titleFailBorder = new EmptyBorder( 0.0, 0.0, 0.0, 0.0, new Color( 1.0f, 0.85f, 0.85f ) );
	static Border nodeBorder = new SolidBorder( 1.0, 1.0, Color.black, null );
	
	static VBoxStyleSheet childrenVBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, 3.0 );
	static HBoxStyleSheet mainHBoxStyle = new HBoxStyleSheet( 80.0 );
	
	
	
	private DPWidget nodeWidget, mainWidget;
	private DPNodeBin nodeBinWidget;
	private ParseView parseView;
	private ArrayList<NodeView> children;
	private DebugNode data;

	
	
	public NodeView(ParseView parseView, DebugNode data)
	{
		this.parseView = parseView;
		this.data = data;
		
		nodeWidget = makeNodeWidget( data );
		
		ArrayList<DPWidget> childWidgets = new ArrayList<DPWidget>();
		children = new ArrayList<NodeView>();
		for (DebugNode child: data.getCallChildren())
		{
			NodeView childView = parseView.buildNodeView( child );
			children.add( childView );
			childWidgets.add( childView.getWidget().padY( 3.0 ) );
		}
		
		DPVBox childrenVBox = new DPVBox( this, childrenVBoxStyle );
		childrenVBox.setChildren( childWidgets );
		
		DPHBox mainHBox = new DPHBox( this, mainHBoxStyle );
		mainHBox.setChildren( new DPWidget[] { nodeWidget.alignVCentre(), childrenVBox.alignVCentre() } );
		
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
	
	
	protected DebugNode getDebugNode()
	{
		return data;
	}
	
	
	protected void registerEdges()
	{
		for (NodeView child: children)
		{
			parseView.addCallEdge( this, child );
		}
		
		for (DebugNode child: data.getMemoChildren())
		{
			parseView.addMemoEdge( this, parseView.getNodeView( child ) );
		}

		for (NodeView child: children)
		{
			child.registerEdges();
		}
	}
	
	
	
	private void onClicked()
	{
		parseView.onNodeSelected( this );
	}
	
	
	protected void select()
	{
		nodeBinWidget.select();
	}
	
	protected void unselect()
	{
		nodeBinWidget.unselect();
	}
	
	
	
	
	
	
	private DPWidget makeTitleWidget(DebugNode data)
	{
		ParserExpression expr = data.getExpression();
		String exprName = expr.getExpressionName();
		
		String className = expr.getClass().getName();
		if ( className.contains( "." ) )
		{
			String[] nameParts = className.split( Pattern.quote( "." ) );
			className = nameParts[ nameParts.length - 1 ];
		}
		
		
		DPText classText = new DPText( this, classNameStyle, "[" + className + "]" );
		if ( exprName != null )
		{
			DPText exprText = new DPText( this, debugNameStyle, exprName );
			DPHBox textBox = new DPHBox( this, titleTextHBoxStyle );
			DPWidget[] children = { exprText, classText };
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
		
		Border b = data.getResult().isValid()  ?  titleSuccessBorder  :  titleFailBorder;
		DPBorder border = new DPBorder( this, b );
		border.setChild( titleWidget.alignVCentre() );
		
		return border;
	}
	
	private DPWidget makeRangeWidget(DebugNode data)
	{
		ParseResultInterface result = data.getResult();
		String rangeText = "";
		
		if ( result.isValid() )
		{
			rangeText = String.valueOf( result.getBegin() ) + "   :   " + String.valueOf( result.getEnd() );
		}
		else
		{
			rangeText = String.valueOf( data.getStart() ) + "   :   " + String.valueOf( result.getEnd() );
		}

		return new DPText( this, rangeStyle, rangeText );
	}
	
	@SuppressWarnings("unchecked")
	private DPWidget makeInputWidget(DebugNode data)
	{
		Object inputObject = data.getInput();
		String inputString;
		
		if ( inputObject instanceof String )
		{
			inputString = (String)inputObject;
			inputString = inputString.substring( data.getResult().getBegin(), data.getResult().getEnd() );
		}
		else if ( inputObject instanceof ItemStreamAccessor )
		{
			ItemStream stream = ((ItemStreamAccessor)inputObject).getStream();
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
			
		if ( inputString.length() > MAX_STRING_LENGTH )
		{
			inputString = inputString.substring( 0, MAX_STRING_LENGTH )  +  "...";
		}

		return new DPText( this, inputStyle, inputString );
	}

	private DPWidget makeValueWidget(DebugNode data)
	{
		ParseResultInterface result = data.getResult();
		
		if ( result.isValid() )
		{
			Object value = result.getValue();
			String valueString = value == null  ?  "<null>"  :  value.toString();
			if ( valueString.length() > MAX_STRING_LENGTH )
			{
				valueString = valueString.substring( 0, MAX_STRING_LENGTH )  +  "...";
			}
			return new DPText( this, valueStyle, valueString );
		}
		else
		{
			return new DPText( this, failStyle, "<fail>" );
		}
	}
	
	private DPWidget makeContentBoxWidget(DebugNode data)
	{
		DPWidget rangeWidget = makeRangeWidget( data );
		DPWidget inputWidget = makeInputWidget( data );
		DPWidget valueWidget = makeValueWidget( data );
		
		DPVBox contentBoxWidget = new DPVBox( this );
		DPWidget[] children = { rangeWidget, inputWidget, valueWidget };
		contentBoxWidget.setChildren( Arrays.asList( children ) );
		return contentBoxWidget;
	}
	
	private DPWidget makeNodeWidget(DebugNode data)
	{
		DPWidget titleBoxWidget = makeTitleBoxWidget( data );
		DPWidget contentBoxWidget = makeContentBoxWidget( data );
		
		DPVBox nodeBoxWidget = new DPVBox( this );
		nodeBoxWidget.setChildren( new DPWidget[] { titleBoxWidget.alignHExpand(), contentBoxWidget.alignHExpand() } );
		
		nodeBinWidget = new DPNodeBin( this, this );
		nodeBinWidget.setChild( nodeBoxWidget );
		
		DPBorder nodeBorderWidget = new DPBorder( this, nodeBorder );
		nodeBorderWidget.setChild( nodeBinWidget );
		
		return nodeBorderWidget;
	}
}
