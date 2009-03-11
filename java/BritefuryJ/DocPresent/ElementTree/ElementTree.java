//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.ElementTree.Caret.ElementCaret;

public class ElementTree
{
	protected RootElement root;
	protected ElementCaret caret;
	protected ElementTree metaTree;
	
	
	public ElementTree()
	{
		root = new RootElement();
		root.setElementTree( this );
		caret = new ElementCaret( this, getPresentationArea().getCaret() );
		metaTree = null;
	}
	
	
	public RootElement getRoot()
	{
		return root;
	}
	
	public DPPresentationArea getPresentationArea()
	{
		return root.getWidget();
	}
	
	public ElementCaret getCaret()
	{
		return caret;
	}
	
	
	
	public ElementTree initialiseMetaTree()
	{
		if ( metaTree == null )
		{
			metaTree = new ElementTree();
			metaTree.getRoot().setChild( root.initialiseMetaElement() );
		}
		
		return metaTree;
	}
	
	public void shutdownMetaTree()
	{
		if ( metaTree != null )
		{
			root.shutdownMetaElement();
			metaTree = null;
		}
	}
	
	
	
	
	public void createMetaTreeWindow()
	{
		initialiseMetaTree();
		metaTree.getPresentationArea().getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		
		
		WindowListener listener = new WindowListener()
		{
			public void windowActivated(WindowEvent arg0)
			{
			}

			public void windowClosed(WindowEvent arg0)
			{
				shutdownMetaTree();
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
		};
		
		JFrame metaFrame = new JFrame( "Meta tree" );
		metaFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		metaFrame.add( metaTree.getPresentationArea().getComponent() );
		metaFrame.pack();
		metaFrame.setVisible( true );
		metaFrame.addWindowListener( listener );
	}
}
