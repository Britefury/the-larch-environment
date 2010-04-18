//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserDebugViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.DebugParseResultInterface;
import BritefuryJ.ParserHelpers.ParseResultInterface;

public class ParseViewFrame implements ParseView.ParseViewListener
{
	private ParseView view;
	private JFrame frame;
	private JMenu viewMenu;
	private JMenuBar menuBar;
	private JPanel graphPanel;
	private JLabel graphLabel;
	private PresentationComponent graph;
	
	private JPanel inputPanel, resultPanel, parserPanel;
	private JLabel inputLabel, resultLabel, parserLabel;
	private JTextPane inputTextPane, resultTextPane, parserTextPane;
	private StyledDocument inputDoc, resultDoc, parserDoc;
	private JScrollPane inputScrollPane, resultScrollPane, parserScrollPane;
	private JSplitPane textSplitPane, parserSplitPane, mainSplitPane;
	
	public ParseViewFrame(DebugParseResultInterface result)
	{
		view = new ParseView( result );
		view.setListener( this );
		graph = view.getPresentationComponent();
		graph.setPreferredSize( new Dimension( 640, 480 ) );
		
		frame = new JFrame( "Parse tree" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		graphLabel = new JLabel( "Graph" );
		graphPanel = new JPanel();
		graphPanel.setLayout( new BoxLayout( graphPanel, BoxLayout.PAGE_AXIS ) );
		graphPanel.add( graphLabel );
		graphPanel.add( graph );
		
		
		Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );

		inputLabel = new JLabel( "Input" );
		inputTextPane = new JTextPane();
		inputTextPane.setEditable( false );
		inputDoc = inputTextPane.getStyledDocument();
		inputDoc.addStyle( "unused", defaultStyle );
		Style unconsumedStyle = inputDoc.addStyle( "unconsumed", defaultStyle );
		StyleConstants.setForeground( unconsumedStyle, new Color( 0.6f, 0.6f, 0.6f ) );
		Style parsedStyle = inputDoc.addStyle( "parsed", defaultStyle );
		StyleConstants.setForeground( parsedStyle, new Color( 0.0f, 0.5f, 0.0f ) );
		StyleConstants.setBold( parsedStyle, true );
		Style structuralUnusedStyle = inputDoc.addStyle( "structural_unused", defaultStyle );
		StyleConstants.setItalic( structuralUnusedStyle, true );
		StyleConstants.setForeground( structuralUnusedStyle, new Color( 0.0f, 0.0f, 1.0f ) );
		Style structuralUnconsumedStyle = inputDoc.addStyle( "structural_unconsumed", structuralUnusedStyle );
		StyleConstants.setForeground( structuralUnconsumedStyle, new Color( 0.5f, 0.5f, 0.85f ) );
		Style structuralParsedStyle = inputDoc.addStyle( "structural_parsed", structuralUnusedStyle );
		StyleConstants.setForeground( structuralParsedStyle, new Color( 0.0f, 0.5f, 0.5f ) );
		StyleConstants.setBold( structuralParsedStyle, true );
		inputScrollPane = new JScrollPane( inputTextPane );
		inputScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		inputScrollPane.setPreferredSize( new Dimension( 200, 200 ) );
		inputPanel = new JPanel();
		inputPanel.setLayout( new BoxLayout( inputPanel, BoxLayout.PAGE_AXIS ) );
		inputPanel.add( inputLabel );
		inputPanel.add( inputScrollPane );
	
		resultLabel = new JLabel( "Result" );
		resultTextPane = new JTextPane();
		resultTextPane.setEditable( false );
		resultDoc = resultTextPane.getStyledDocument();
		resultDoc.addStyle( "value", defaultStyle );
		Style failStyle = resultDoc.addStyle( "fail", defaultStyle );
		StyleConstants.setForeground( failStyle, new Color( 0.5f, 0.0f, 0.0f ) );
		StyleConstants.setItalic( failStyle, true );
		resultScrollPane = new JScrollPane( resultTextPane );
		resultScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		resultScrollPane.setPreferredSize( new Dimension( 200, 200 ) );
		resultPanel = new JPanel();
		resultPanel.setLayout( new BoxLayout( resultPanel, BoxLayout.PAGE_AXIS ) );
		resultPanel.add( resultLabel );
		resultPanel.add( resultScrollPane );
		
		parserLabel = new JLabel( "Parser expression" );
		parserTextPane = new JTextPane();
		parserTextPane.setEditable( false );
		parserDoc = parserTextPane.getStyledDocument();
		parserDoc.addStyle( "parser", defaultStyle );
		parserScrollPane = new JScrollPane( parserTextPane );
		parserScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		parserScrollPane.setPreferredSize( new Dimension( 200, 200 ) );
		parserPanel = new JPanel();
		parserPanel.setLayout( new BoxLayout( parserPanel, BoxLayout.PAGE_AXIS ) );
		parserPanel.add( parserLabel );
		parserPanel.add( parserScrollPane );
		
		textSplitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, inputPanel, resultPanel );
		textSplitPane.setOneTouchExpandable( true );
		textSplitPane.setResizeWeight( 0.5 );
		textSplitPane.setPreferredSize( new Dimension( 640, 180 ) );
		
		parserSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, parserPanel, textSplitPane );
		parserSplitPane.setOneTouchExpandable( true );
		parserSplitPane.setResizeWeight( 0.25 );
		parserSplitPane.setPreferredSize( new Dimension( 640, 120 ) );
		
		mainSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, graphPanel, parserSplitPane );
		mainSplitPane.setResizeWeight( 0.75 );
		
		
	
		// VIEW MENU
		
		viewMenu = new JMenu( "View" );
		
		viewMenu.add( new AbstractAction( "Reset" )
		{
			public void actionPerformed(ActionEvent e)
			{
				view.getViewport().resetXform();
			}

			private static final long serialVersionUID = 1L;
		} );

		viewMenu.add( new AbstractAction( "1:1" )
		{
			public void actionPerformed(ActionEvent e)
			{
				view.getViewport().oneToOne();
			}

			private static final long serialVersionUID = 1L;
		} );

		viewMenu.add( new AbstractAction( "Zoom to fit" )
		{
			public void actionPerformed(ActionEvent e)
			{
				view.getViewport().zoomToFit();
			}

			private static final long serialVersionUID = 1L;
		} );

	
	
		menuBar = new JMenuBar();
		menuBar.add( viewMenu );
		
		
		frame.setJMenuBar( menuBar );

		frame.add( mainSplitPane );
		frame.pack();
		frame.setVisible(true);
		view.getViewport().zoomToFit();
	}


	
	@SuppressWarnings("unchecked")
	public void onSelectionChanged(DebugNode selection)
	{
		try
		{
			inputDoc.remove( 0, inputDoc.getLength() );
			resultDoc.remove( 0, resultDoc.getLength() );
			parserDoc.remove( 0, parserDoc.getLength() );

			if ( selection != null )
			{
				ParseResultInterface result = selection.getResult();
				ParserExpression expression = selection.getExpression();
				Object inputObject = selection.getInput();
				
				parserDoc.insertString( 0, expression.toString(), inputDoc.getStyle( "parser" ) );
				
				if ( inputObject instanceof String )
				{
					String input = (String)inputObject;
					if ( result.isValid() )
					{
						inputDoc.insertString( inputDoc.getLength(), input.substring( 0, result.getBegin() ), inputDoc.getStyle( "unused" ) );
						inputDoc.insertString( inputDoc.getLength(), input.substring( result.getBegin(), result.getEnd() ), inputDoc.getStyle( "parsed" ) );
						inputDoc.insertString( inputDoc.getLength(), input.substring( result.getEnd(), input.length() ), inputDoc.getStyle( "unused" ) );
					}
					else
					{
						inputDoc.insertString( inputDoc.getLength(), input.substring( 0, result.getEnd() ), inputDoc.getStyle( "unused" ) );
						inputDoc.insertString( inputDoc.getLength(), input.substring( result.getEnd(), input.length() ), inputDoc.getStyle( "unconsumed" ) );
					}
				}
				else if ( inputObject instanceof ItemStreamAccessor )
				{
					ItemStreamAccessor accessor = (ItemStreamAccessor)inputObject;
					for (ItemStream.Item item: accessor.getStream().getItems())
					{
						if ( item.isStructural() )
						{
							if ( result.isValid() )
							{
								if ( item.getStop() <= result.getBegin() )
								{
									inputDoc.insertString( inputDoc.getLength(), item.toString(), inputDoc.getStyle( "structural_unused" ) );
								}
								else if ( item.getStop() <= result.getEnd() )
								{
									inputDoc.insertString( inputDoc.getLength(), item.toString(), inputDoc.getStyle( "structural_parsed" ) );
								}
								else
								{
									inputDoc.insertString( inputDoc.getLength(), item.toString(), inputDoc.getStyle( "structural_unused" ) );
								}
							}
							else
							{
								if ( item.getStop() <= result.getEnd() )
								{
									inputDoc.insertString( inputDoc.getLength(), item.toString(), inputDoc.getStyle( "structural_unused" ) );
								}
								else
								{
									inputDoc.insertString( inputDoc.getLength(), item.toString(), inputDoc.getStyle( "structural_unconsumed" ) );
								}
							}
						}
						else
						{
							if ( result.isValid() )
							{
								int preUnusedStart = item.getStart();
								int preUnusedStop = Math.min( result.getBegin(), item.getStop() );
								
								int parsedStart = Math.max( result.getBegin(), item.getStart() );
								int parsedStop = Math.min( result.getEnd(), item.getStop() );
	
								int postUnusedStart = Math.max( result.getEnd(), item.getStart() );
								int postUnusedStop = Math.min( accessor.length(), item.getStop() );
	
								if ( preUnusedStart < preUnusedStop )
								{
									inputDoc.insertString( inputDoc.getLength(), item.subItem( preUnusedStart, preUnusedStop, 0 ).toString(), inputDoc.getStyle( "unused" ) );
								}
								if ( parsedStart < parsedStop )
								{
									inputDoc.insertString( inputDoc.getLength(), item.subItem( parsedStart, parsedStop, 0 ).toString(), inputDoc.getStyle( "parsed" ) );
								}
								if ( postUnusedStart < postUnusedStop )
								{
									inputDoc.insertString( inputDoc.getLength(), item.subItem( postUnusedStart, postUnusedStop, 0 ).toString(), inputDoc.getStyle( "unused" ) );
								}
							}
							else
							{
								int preUnusedStart = item.getStart();
								int preUnusedStop = Math.min( result.getEnd(), item.getStop() );
								
								int unconsumedStart = Math.max( result.getEnd(), item.getStart() );
								int unconsumedStop = Math.min( accessor.length(), item.getStop() );
	
								if ( preUnusedStart < preUnusedStop )
								{
									inputDoc.insertString( inputDoc.getLength(), item.subItem( preUnusedStart, preUnusedStop, 0 ).toString(), inputDoc.getStyle( "unused" ) );
								}
								if ( unconsumedStart < unconsumedStop )
								{
									inputDoc.insertString( inputDoc.getLength(), item.subItem( unconsumedStart, unconsumedStop, 0 ).toString(), inputDoc.getStyle( "unconsumed" ) );
								}
							}
						}
					}
				}
				else if ( inputObject instanceof List )
				{
					List<Object> input = (List<Object>)inputObject;
					if ( result.isValid() )
					{
						int parsedIndex = -1, postIndex = -1;
						String content = "[";
						
						for (int i = 0; i < input.size(); i++)
						{
							if ( i == result.getBegin() )
							{
								parsedIndex = content.length();
							}
							if ( i == result.getEnd() )
							{
								postIndex = content.length();
							}
							content += input.get( i ).toString();
							
							if ( i != input.size() - 1 )
							{
								content += ", ";
							}
						}
						
						content += "]";
						
						parsedIndex = parsedIndex == -1  ?  content.length() - 1  :  parsedIndex;
						postIndex = postIndex == -1  ?  content.length() - 1  :  postIndex;
						
						inputDoc.insertString( inputDoc.getLength(), content.substring( 0, parsedIndex ), inputDoc.getStyle( "unused" ) );
						inputDoc.insertString( inputDoc.getLength(), content.substring( parsedIndex, postIndex ), inputDoc.getStyle( "parsed" ) );
						inputDoc.insertString( inputDoc.getLength(), content.substring( postIndex, content.length() ), inputDoc.getStyle( "unused" ) );
					}
					else
					{
						int errorIndex = -1;
						String content = "[";
						
						for (int i = 0; i < input.size(); i++)
						{
							if ( i == result.getEnd() )
							{
								errorIndex = content.length();
							}
							content += input.get( i ).toString();
							
							if ( i != input.size() - 1 )
							{
								content += ", ";
							}
						}
						
						content += "]";
						
						errorIndex = errorIndex == -1  ?  content.length() - 1  :  errorIndex;

						inputDoc.insertString( inputDoc.getLength(), content.substring( 0, errorIndex ), inputDoc.getStyle( "unused" ) );
						inputDoc.insertString( inputDoc.getLength(), content.substring( errorIndex, content.length() ), inputDoc.getStyle( "unconsumed" ) );
					}
				}
				else
				{
					inputDoc.insertString( inputDoc.getLength(), inputObject.toString(), inputDoc.getStyle( "unused" ) );
				}
				
				if ( result.isValid() )
				{
					String valueString = "";
					if ( result.getValue() != null )
					{
						valueString = result.getValue().toString();
					}
					
					resultDoc.insertString( 0, valueString, resultDoc.getStyle( "value" ) );
				}
				else
				{
					resultDoc.insertString( 0, "<fail>", resultDoc.getStyle( "fail" ) );
				}
			}
		}
		catch (BadLocationException e)
		{
			throw new RuntimeException();
		}
	}
}
