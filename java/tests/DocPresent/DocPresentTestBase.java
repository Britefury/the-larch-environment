//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPWidget;

public abstract class DocPresentTestBase
{
	protected JMenuBar menuBar;
	DPWidget contentWidget;
	
	protected abstract DPWidget createWidget();

	public void initFrame(JFrame frame)
	{
		DPPresentationArea area = new DPPresentationArea();
		
		
		menuBar = new JMenuBar();
		
		frame.setJMenuBar( menuBar );
		
		

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		
		long t1 = System.currentTimeMillis();
		contentWidget = createWidget();
		long t2 = System.currentTimeMillis();
		System.out.println( "Widget creation time: " + (double)( t2 - t1 ) / 1000.0 );
		area.setChild( contentWidget );
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
