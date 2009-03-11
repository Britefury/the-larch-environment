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
		JMenu viewMenu = new JMenu( "View" );
		viewMenu.add( showMetaTreeAction );
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add( viewMenu );
		
		frame.setJMenuBar( menuBar );
		
		

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		

		tree.getRoot().setChild( createContentNode() );
	     
	     
		DPPresentationArea area = tree.getPresentationArea();
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
