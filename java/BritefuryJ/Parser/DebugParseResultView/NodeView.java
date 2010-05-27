//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.DebugParseResultView;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParseResultInterface;

public class NodeView implements FragmentContext
{
	private static class NodeInteractor extends ElementInteractor
	{
		private NodeView nodeView;
		private DPElement element;
		private boolean bSelected, bHighlight;
		
		public NodeInteractor(NodeView nodeView, DPElement element)
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
		
		
		
		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
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
		
		public void onEnter(DPElement element, PointerMotionEvent event)
		{
			bHighlight = true;
			element.queueFullRedraw();
		}

		public void onLeave(DPElement element, PointerMotionEvent event)
		{
			bHighlight = false;
			element.queueFullRedraw();
		}


		
		public void drawBackground(DPElement element, Graphics2D graphics)
		{
			Color backgroundColour = bHighlight  ?  new Color( 0.7f, 0.85f, 1.0f )  :  Color.white;
			backgroundColour = bSelected  ?  new Color( 1.0f, 1.0f, 0.6f )  :  backgroundColour;
			graphics.setColor( backgroundColour );
			graphics.fill( new Rectangle2D.Double( 0.0, 0.0, element.getWidth(), element.getHeight() ) );
		}
	}

	
	
	static int MAX_STRING_LENGTH = 64;
	
	static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;

	static PrimitiveStyleSheet debugNameStyle = styleSheet.withFontBold( true ).withFontSize( 24 ).withForeground( Color.blue );
	static PrimitiveStyleSheet classNameStyle = styleSheet.withFontSize( 18 ).withForeground( new Color( 0.0f, 0.0f, 0.5f ) );
	static PrimitiveStyleSheet rangeStyle = styleSheet.withFontSize( 12 );
	static PrimitiveStyleSheet inputStyle = styleSheet.withFontSize( 12 );
	static PrimitiveStyleSheet valueStyle = styleSheet.withFontSize( 16 );
	static PrimitiveStyleSheet failStyle = styleSheet.withFontItalic( true ).withFontSize( 16 ).withForeground( new Color( 0.5f, 0.0f, 0.0f ) );;
	
	static Border titleSuccessBorder = new FilledBorder( 0.0, 0.0, 0.0, 0.0, new Color( 0.85f, 0.95f, 0.85f ) );
	static Border titleFailBorder = new FilledBorder( 0.0, 0.0, 0.0, 0.0, new Color( 1.0f, 0.85f, 0.85f ) );
	static Border nodeBorder = new SolidBorder( 1.0, 1.0, Color.black, null );
	
	
	
	private DPElement nodeElement, mainElement;
	private DPBin nodeBinElement;
	private NodeInteractor nodeInteractor;
	private ParseView parseView;
	private ArrayList<NodeView> children;
	private DebugNode data;

	
	
	public NodeView(ParseView parseView, DebugNode data)
	{
		this.parseView = parseView;
		this.data = data;
		
		nodeElement = makeNodeElement( data );
		
		ArrayList<DPElement> childElements = new ArrayList<DPElement>();
		children = new ArrayList<NodeView>();
		for (DebugNode child: data.getCallChildren())
		{
			NodeView childView = parseView.buildNodeView( child );
			children.add( childView );
			childElements.add( childView.getElement().padY( 3.0 ) );
		}
		
		DPElement childrenVBox = styleSheet.withVBoxSpacing( 3.0 ).vbox( childElements.toArray( new DPElement[0] ) );
		
		mainElement = styleSheet.withHBoxSpacing( 80.0 ).hbox( new DPElement[] { nodeElement.alignVCentre(), childrenVBox.alignVCentre() } );
	}
	
	
	public DPElement getElement()
	{
		return mainElement;
	}
	
	protected DPElement getNodeElement()
	{
		return nodeElement;
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
	
	
	
	
	
	
	private DPElement makeTitleElement(DebugNode data)
	{
		ParserExpression expr = data.getExpression();
		String exprName = expr.getExpressionName();
		
		String className = expr.getClass().getName();
		if ( className.contains( "." ) )
		{
			String[] nameParts = className.split( Pattern.quote( "." ) );
			className = nameParts[ nameParts.length - 1 ];
		}
		
		
		DPElement classText = classNameStyle.staticText( "[" + className + "]" );
		if ( exprName != null )
		{
			DPElement exprText = debugNameStyle.staticText( exprName );
			return styleSheet.withHBoxSpacing( 10.0 ).hbox( new DPElement[] { exprText, classText } );
		}
		else
		{
			return classText;
		}
	}
	
	private DPElement makeTitleBoxElement(DebugNode data)
	{
		DPElement titleElement = makeTitleElement( data );
		
		Border b = data.getResult().isValid()  ?  titleSuccessBorder  :  titleFailBorder;
		
		return styleSheet.withBorder( b ).border( titleElement.alignVCentre() );
	}
	
	private DPElement makeRangeElement(DebugNode data)
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
	private DPElement makeInputElement(DebugNode data)
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

	private DPElement makeValueElement(DebugNode data)
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
	
	private DPElement makeContentBoxElement(DebugNode data)
	{
		DPElement rangeElement = makeRangeElement( data );
		DPElement inputElement = makeInputElement( data );
		DPElement valueElement = makeValueElement( data );
		
		return styleSheet.vbox( new DPElement[] { rangeElement, inputElement, valueElement } );
	}
	
	private DPElement makeNodeElement(DebugNode data)
	{
		DPElement titleBoxElement = makeTitleBoxElement( data );
		DPElement contentBoxElement = makeContentBoxElement( data );
		
		DPVBox nodeBoxElement = styleSheet.vbox( new DPElement[] { titleBoxElement.alignHExpand(), contentBoxElement.alignHExpand() } );
		
		nodeBinElement = styleSheet.bin( nodeBoxElement );
		
		nodeInteractor = new NodeInteractor( this, nodeBinElement );
		nodeBinElement.addInteractor( nodeInteractor );
		
		return styleSheet.withBorder( nodeBorder ).border( nodeBinElement );
	}
}
