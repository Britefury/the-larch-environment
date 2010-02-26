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
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParseResultInterface;

public class NodeView implements ElementContext
{
	private static class NodeInteractor extends ElementInteractor
	{
		private NodeView nodeView;
		private DPWidget element;
		private boolean bSelected, bHighlight;
		
		public NodeInteractor(NodeView nodeView, DPWidget element)
		{
			super();
			
			this.nodeView = nodeView;
			this.element = element;
			bSelected = false;
			bHighlight = false;
		}
		
		
		
		void select()
		{
			if ( !bSelected )
			{
				bSelected = true;
				element.queueFullRedraw();
			}
		}
		
		void unselect()
		{
			if ( bSelected )
			{
				bSelected = false;
				element.queueFullRedraw();
			}
		}
		
		
		
		public boolean onButtonDown(DPWidget element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				nodeView.onClicked();
				return true;
			}
			else
			{
				return false;
			}
		}
		
		public void onEnter(DPWidget element, PointerMotionEvent event)
		{
			bHighlight = true;
			element.queueFullRedraw();
		}

		public void onLeave(DPWidget element, PointerMotionEvent event)
		{
			bHighlight = false;
			element.queueFullRedraw();
		}


		
		public void drawBackground(DPWidget element, Graphics2D graphics)
		{
			Color backgroundColour = bHighlight  ?  new Color( 0.7f, 0.85f, 1.0f )  :  Color.white;
			backgroundColour = bSelected  ?  new Color( 1.0f, 1.0f, 0.6f )  :  backgroundColour;
			graphics.setColor( backgroundColour );
			graphics.fill( new Rectangle2D.Double( 0.0, 0.0, element.getAllocationX(), element.getAllocationY() ) );
		}
	}

	
	
	static int MAX_STRING_LENGTH = 64;
	
	static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;

	static PrimitiveStyleSheet debugNameStyle = styleSheet.withFont( new Font( "Sans serif", Font.BOLD, 24 ) ).withForeground( Color.blue );
	static PrimitiveStyleSheet classNameStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 18 ) ).withForeground( new Color( 0.0f, 0.0f, 0.5f ) );
	static PrimitiveStyleSheet rangeStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) );
	static PrimitiveStyleSheet inputStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) );
	static PrimitiveStyleSheet valueStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 16 ) );
	static PrimitiveStyleSheet failStyle = styleSheet.withFont( new Font( "Sans serif", Font.ITALIC, 16 ) ).withForeground( new Color( 0.5f, 0.0f, 0.0f ) );;
	
	static Border titleSuccessBorder = new EmptyBorder( 0.0, 0.0, 0.0, 0.0, new Color( 0.85f, 0.95f, 0.85f ) );
	static Border titleFailBorder = new EmptyBorder( 0.0, 0.0, 0.0, 0.0, new Color( 1.0f, 0.85f, 0.85f ) );
	static Border nodeBorder = new SolidBorder( 1.0, 1.0, Color.black, null );
	
	
	
	private DPWidget nodeWidget, mainWidget;
	private DPBin nodeBinWidget;
	private NodeInteractor nodeInteractor;
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
		
		DPWidget childrenVBox = styleSheet.withVBoxSpacing( 3.0 ).vbox( childWidgets );
		
		mainWidget = styleSheet.withHBoxSpacing( 80.0 ).hbox( Arrays.asList( new DPWidget[] { nodeWidget.alignVCentre(), childrenVBox.alignVCentre() } ) );
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
		nodeInteractor.select();
	}
	
	protected void unselect()
	{
		nodeInteractor.unselect();
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
		
		
		DPWidget classText = classNameStyle.staticText( "[" + className + "]" );
		if ( exprName != null )
		{
			DPWidget exprText = debugNameStyle.staticText( exprName );
			return styleSheet.withHBoxSpacing( 10.0 ).hbox( Arrays.asList( new DPWidget[] { exprText, classText } ) );
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
		
		return styleSheet.withBorder( b ).border( titleWidget.alignVCentre() );
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

		return rangeStyle.staticText( rangeText );
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

		return inputStyle.staticText( inputString );
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
			return valueStyle.staticText( valueString );
		}
		else
		{
			return failStyle.staticText( "<fail>" );
		}
	}
	
	private DPWidget makeContentBoxWidget(DebugNode data)
	{
		DPWidget rangeWidget = makeRangeWidget( data );
		DPWidget inputWidget = makeInputWidget( data );
		DPWidget valueWidget = makeValueWidget( data );
		
		return styleSheet.vbox( Arrays.asList( new DPWidget[] { rangeWidget, inputWidget, valueWidget } ) );
	}
	
	private DPWidget makeNodeWidget(DebugNode data)
	{
		DPWidget titleBoxWidget = makeTitleBoxWidget( data );
		DPWidget contentBoxWidget = makeContentBoxWidget( data );
		
		DPVBox nodeBoxWidget = styleSheet.vbox( Arrays.asList( new DPWidget[] { titleBoxWidget.alignHExpand(), contentBoxWidget.alignHExpand() } ) );
		
		nodeBinWidget = styleSheet.box( nodeBoxWidget );
		
		nodeInteractor = new NodeInteractor( this, nodeBinWidget );
		nodeBinWidget.setInteractor( nodeInteractor );
		
		return styleSheet.withBorder( nodeBorder ).border( nodeBinWidget );
	}
}
