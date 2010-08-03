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

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueAccessor;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParseResultInterface;

public class NodeView implements FragmentContext
{
	private static class NodeInteractor extends ElementInteractor
	{
		private NodeView nodeView;
		private boolean bSelected, bHighlight;
		
		public NodeInteractor(NodeView nodeView)
		{
			super();
			
			this.nodeView = nodeView;
			bSelected = false;
			bHighlight = false;
		}
		
		
		
		void select()
		{
			if ( !bSelected )
			{
				bSelected = true;
				//element.queueFullRedraw();
			}
		}
		
		void unselect()
		{
			if ( bSelected )
			{
				bSelected = false;
				//element.queueFullRedraw();
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
	
	private static final StyleSheet2 styleSheet = StyleSheet2.instance;

	private static final StyleSheet2 debugNameStyle = styleSheet.withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, 24 ).withAttr( Primitive.foreground,  Color.blue );
	private static final StyleSheet2 classNameStyle = styleSheet.withAttr( Primitive.fontSize, 18 ).withAttr( Primitive.foreground,  new Color( 0.0f, 0.0f, 0.5f ) );
	private static final StyleSheet2 rangeStyle = styleSheet.withAttr( Primitive.fontSize, 12 );
	private static final StyleSheet2 inputStyle = styleSheet.withAttr( Primitive.fontSize, 12 );
	private static final StyleSheet2 valueStyle = styleSheet.withAttr( Primitive.fontSize, 16 );
	private static final StyleSheet2 failStyle = styleSheet.withAttr( Primitive.fontItalic,  true ).withAttr( Primitive.fontSize, 16 ).withAttr( Primitive.foreground,  new Color( 0.5f, 0.0f, 0.0f ) );
	
	private static final AbstractBorder titleSuccessBorder = new FilledBorder( 0.0, 0.0, 0.0, 0.0, new Color( 0.85f, 0.95f, 0.85f ) );
	private static final AbstractBorder titleFailBorder = new FilledBorder( 0.0, 0.0, 0.0, 0.0, new Color( 1.0f, 0.85f, 0.85f ) );
	private static final AbstractBorder nodeBorder = new SolidBorder( 1.0, 1.0, Color.black, null );
	
	
	
	private DPElement mainElement, nodeElement;
	private NodeInteractor nodeInteractor;
	private ParseView parseView;
	private ArrayList<NodeView> children;
	private DebugNode data;

	
	
	public NodeView(ParseView parseView, DebugNode data)
	{
		this.parseView = parseView;
		this.data = data;
		
		nodeElement = makeNodeElement( data ).present();
		
		ArrayList<DPElement> childElements = new ArrayList<DPElement>();
		children = new ArrayList<NodeView>();
		for (DebugNode child: data.getCallChildren())
		{
			NodeView childView = parseView.buildNodeView( child );
			children.add( childView );
			childElements.add( childView.getElement().padY( 3.0 ) );
		}
		
		Pres childrenVBox = styleSheet.withAttr( Primitive.vboxSpacing, 3.0 ).applyTo( new VBox( childElements.toArray( new DPElement[0] ) ) );
		
		mainElement = styleSheet.withAttr( Primitive.hboxSpacing, 80.0 ).applyTo( new HBox( new Object[] { nodeElement.alignVCentre(), childrenVBox.alignVCentre() } ) ).present();
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
	
	
	
	
	
	
	private Pres makeTitleElement(DebugNode data)
	{
		ParserExpression expr = data.getExpression();
		String exprName = expr.getExpressionName();
		
		String className = expr.getClass().getName();
		if ( className.contains( "." ) )
		{
			String[] nameParts = className.split( Pattern.quote( "." ) );
			className = nameParts[ nameParts.length - 1 ];
		}
		
		
		Pres classText = classNameStyle.applyTo( new StaticText( "[" + className + "]" ) );
		if ( exprName != null )
		{
			Pres exprText = debugNameStyle.applyTo( new StaticText( exprName ) );
			return styleSheet.withAttr( Primitive.hboxSpacing, 10.0 ).applyTo( new HBox( new Pres[] { exprText, classText } ) );
		}
		else
		{
			return classText;
		}
	}
	
	private Pres makeTitleBoxElement(DebugNode data)
	{
		Pres titleElement = makeTitleElement( data );
		
		AbstractBorder b = data.getResult().isValid()  ?  titleSuccessBorder  :  titleFailBorder;
		
		return styleSheet.withAttr( Primitive.border, b ).applyTo( new Border( titleElement.alignVCentre() ) );
	}
	
	private Pres makeRangeElement(DebugNode data)
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

		return rangeStyle.applyTo( new StaticText( rangeText ) );
	}
	
	@SuppressWarnings("unchecked")
	private Pres makeInputElement(DebugNode data)
	{
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
			
		if ( inputString.length() > MAX_STRING_LENGTH )
		{
			inputString = inputString.substring( 0, MAX_STRING_LENGTH )  +  "...";
		}

		return inputStyle.applyTo( new StaticText( inputString ) );
	}

	private Pres makeValueElement(DebugNode data)
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
			return valueStyle.applyTo( new StaticText( valueString ) );
		}
		else
		{
			return failStyle.applyTo( new StaticText( "<fail>" ) );
		}
	}
	
	private Pres makeContentBoxElement(DebugNode data)
	{
		Pres rangeElement = makeRangeElement( data );
		Pres inputElement = makeInputElement( data );
		Pres valueElement = makeValueElement( data );
		
		return new VBox( new Pres[] { rangeElement, inputElement, valueElement } );
	}
	
	private Pres makeNodeElement(DebugNode data)
	{
		Pres titleBoxElement = makeTitleBoxElement( data );
		Pres contentBoxElement = makeContentBoxElement( data );
		
		Pres nodeBoxElement = new VBox( new Pres[] { titleBoxElement.alignHExpand(), contentBoxElement.alignHExpand() } );
		
		Pres nodeBinElement = new Bin( nodeBoxElement );
		
		nodeInteractor = new NodeInteractor( this );
		nodeBinElement = nodeBinElement.withInteractor( nodeInteractor );
		
		return styleSheet.withAttr( Primitive.border, nodeBorder ).applyTo( new Border( nodeBinElement ) );
	}
}
