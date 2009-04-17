//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.ElementTree;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;

public abstract class ElementTreeTestBase
{
	protected abstract Element createContentNode();

	public void initFrame(JFrame frame)
	{
		final ElementTree tree = new ElementTree();
		
		
		AbstractAction showMetaTreeAction = new AbstractAction( "Show element tree explorer" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				tree.createTreeExplorer();
			}
		};
		
		
		// Menu
		JMenu debugMenu = new JMenu( "Debug" );
		debugMenu.add( showMetaTreeAction );
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add( debugMenu );
		
		frame.setJMenuBar( menuBar );
		
		

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		
		long t1 = System.currentTimeMillis();
		Element element = createContentNode();
		long t2 = System.currentTimeMillis();
		System.out.println( "Element tree creation time: " + (double)( t2 - t1 ) / 1000.0 );
		tree.getRoot().setChild( element );
	     
	     
		DPPresentationArea area = tree.getPresentationArea();
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
