//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.TreeExplorer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Caret.Caret;

public class ElementTreeExplorer implements WindowListener
{
	protected DPPresentationArea tree, metaArea;
	protected boolean bVisible;
	
	
	public void windowActivated(WindowEvent arg0)
	{
	}

	public void windowClosed(WindowEvent arg0)
	{
		tree.shutdownMetaTree();
		bVisible = false;
	}

	public void windowClosing(WindowEvent arg0)
	{
	}

	public void windowDeactivated(WindowEvent arg0)
	{
	}

	public void windowDeiconified(WindowEvent arg0)
	{
	}

	public void windowIconified(WindowEvent arg0)
	{
	}

	public void windowOpened(WindowEvent arg0)
	{
	}

	
	
	public ElementTreeExplorer(DPPresentationArea t)
	{
		JFrame frame = new JFrame( "Tree explorer" );

		
		this.tree = t;
		
		metaArea = this.tree.initialiseMetaTree();
		metaArea.getComponent().setPreferredSize( new Dimension( 640, 480 ) );

	
		AbstractAction resetAction = new AbstractAction( "Reset" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				metaArea.reset();
			}
		};
		
		
		AbstractAction oneToOneAction = new AbstractAction( "1:1" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				metaArea.oneToOne();
			}
		};
		
		
		AbstractAction focusOnCursorAction = new AbstractAction( "Focus on caret" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				Caret caret = tree.getCaret();
				if ( caret != null )
				{
					DPWidget caretElement = caret.getMarker().getWidget();
					if ( caretElement != null )
					{
						DPWidget metaCaretWidget = caretElement.getMetaElement();
						if ( metaCaretWidget != null )
						{
							metaArea.focusOn( metaCaretWidget );
						}
					}
				}
			}
		};
		
		
		// Menu
		JMenu viewMenu = new JMenu( "View" );
		viewMenu.add( resetAction );
		viewMenu.add( oneToOneAction );
		viewMenu.add( focusOnCursorAction );
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add( viewMenu );
		
		frame.setJMenuBar( menuBar );

		
		
		
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.add( metaArea.getComponent() );
		frame.pack();
		frame.setVisible( true );
		frame.addWindowListener( this );
		
		bVisible = true;
	}
	
	
	
	public boolean isVisible()
	{
		return bVisible;
	}
}
