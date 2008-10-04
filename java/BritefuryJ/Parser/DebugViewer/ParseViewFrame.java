//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.DebugViewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.Parser.DebugParseResult;

public class ParseViewFrame
{
	private ParseView view;
	private JFrame frame;
	private JMenu viewMenu;
	private JMenuBar menuBar;
	private DPPresentationArea area;
	
	public ParseViewFrame(DebugParseResult result, String input)
	{
		view = new ParseView( result, input );
		area = view.getPresentationArea();
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		
		frame = new JFrame( "Parse tree" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		

	
	
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

		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
		area.zoomToFit();
	}
}
