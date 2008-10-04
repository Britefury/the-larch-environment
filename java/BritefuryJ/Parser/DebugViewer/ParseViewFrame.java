//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.DebugViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.Parser.DebugNode;
import BritefuryJ.Parser.DebugParseResult;
import BritefuryJ.Parser.ParseResult;

public class ParseViewFrame implements ParseView.ParseViewListener
{
	private ParseView view;
	private JFrame frame;
	private JMenu viewMenu;
	private JMenuBar menuBar;
	private DPPresentationArea area;
	
	private JTextPane inputTextPane, resultTextPane;
	private StyledDocument inputDoc, resultDoc;
	private JScrollPane inputScrollPane, resultScrollPane;
	private JSplitPane textSplitPane, mainSplitPane;
	
	private String input;
	
	public ParseViewFrame(DebugParseResult result, String input)
	{
		this.input = input;
		
		view = new ParseView( result, input );
		view.setListener( this );
		area = view.getPresentationArea();
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		
		frame = new JFrame( "Parse tree" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		
		Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );

		inputTextPane = new JTextPane();
		inputTextPane.setEditable( false );
		inputDoc = inputTextPane.getStyledDocument();
		inputDoc.addStyle( "unused", defaultStyle );
		Style unconsumedStyle = inputDoc.addStyle( "unconsumed", defaultStyle );
		StyleConstants.setForeground( unconsumedStyle, new Color( 0.6f, 0.6f, 0.6f ) );
		Style parsedStyle = inputDoc.addStyle( "parsed", defaultStyle );
		StyleConstants.setForeground( parsedStyle, new Color( 0.0f, 0.5f, 0.0f ) );
		StyleConstants.setBold( parsedStyle, true );
		inputScrollPane = new JScrollPane( inputTextPane );
		inputScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		inputScrollPane.setPreferredSize( new Dimension( 200, 200 ) );
		
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
		
		textSplitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, inputScrollPane, resultScrollPane );
		textSplitPane.setOneTouchExpandable( true );
		textSplitPane.setResizeWeight( 0.5 );
		
		mainSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, area.getComponent(), textSplitPane );
		mainSplitPane.setResizeWeight( 0.75 );
		
		
	
		// VIEW MENU
		
		viewMenu = new JMenu( "View" );
		
		viewMenu.add( new AbstractAction( "Reset" )
		{
			public void actionPerformed(ActionEvent e)
			{
				area.reset();
			}

			private static final long serialVersionUID = 1L;
		} );

		viewMenu.add( new AbstractAction( "1:1" )
		{
			public void actionPerformed(ActionEvent e)
			{
				area.oneToOne();
			}

			private static final long serialVersionUID = 1L;
		} );

		viewMenu.add( new AbstractAction( "Zoom to fit" )
		{
			public void actionPerformed(ActionEvent e)
			{
				area.zoomToFit();
			}

			private static final long serialVersionUID = 1L;
		} );

	
	
		menuBar = new JMenuBar();
		menuBar.add( viewMenu );
		
		
		frame.setJMenuBar( menuBar );

		frame.add( mainSplitPane );
		frame.pack();
		frame.setVisible(true);
		area.zoomToFit();
	}


	
	public void onSelectionChanged(DebugNode selection)
	{
		try
		{
			inputDoc.remove( 0, inputDoc.getLength() );
			resultDoc.remove( 0, resultDoc.getLength() );

			if ( selection != null )
			{
				ParseResult result = selection.getResult();
				if ( result.isValid() )
				{
					inputDoc.insertString( inputDoc.getLength(), input.substring( 0, result.getBegin() ), inputDoc.getStyle( "unused" ) );
					inputDoc.insertString( inputDoc.getLength(), input.substring( result.getBegin(), result.getEnd() ), inputDoc.getStyle( "parsed" ) );
					inputDoc.insertString( inputDoc.getLength(), input.substring( result.getEnd(), input.length() ), inputDoc.getStyle( "unused" ) );
					
					String valueString = "";
					if ( result.getValue() != null )
					{
						valueString = result.getValue().toString();
					}
					
					resultDoc.insertString( 0, valueString, resultDoc.getStyle( "value" ) );
				}
				else
				{
					inputDoc.insertString( inputDoc.getLength(), input.substring( 0, result.getEnd() ), inputDoc.getStyle( "unused" ) );
					inputDoc.insertString( inputDoc.getLength(), input.substring( result.getEnd(), input.length() ), inputDoc.getStyle( "unconsumed" ) );

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
