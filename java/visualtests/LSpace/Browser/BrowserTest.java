//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package visualtests.LSpace.Browser;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Browser.Browser;
import BritefuryJ.Browser.TabbedBrowser;
import BritefuryJ.Browser.TestPages.TestsRootPage;
import BritefuryJ.Command.AbstractCommandConsole;
import BritefuryJ.Command.CommandConsole;
import BritefuryJ.Command.CommandConsoleFactory;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;
import BritefuryJ.Projection.TransientSubject;
import BritefuryJ.Projection.Subject;
import BritefuryJ.StyleSheet.StyleSheet;

public class BrowserTest implements TabbedBrowser.TabbedBrowserListener
{
	private static class DefaultRootPage implements Presentable
	{
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Pres linkHeader = TestsRootPage.createLinkHeader( TestsRootPage.LINKHEADER_SYSTEMPAGE );
			Pres title = new TitleBar( "Default Root Page" );
			
			Pres contents = StyleSheet.style( Primitive.fontSize.as( 16 ) ).applyTo( new Label( "Empty document" ) ).alignHCentre();
			
			Pres head = new Head( new Pres[] { linkHeader, title } );
			
			return new Page( new Pres[] { head, contents } );
		}
	}
	
	
	private static class TransferActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			JComponent focusOwner = (JComponent)manager.getPermanentFocusOwner();
			if ( focusOwner != null )
			{
				String action = e.getActionCommand();
				Action a = focusOwner.getActionMap().get( action );
				if ( a != null )
				{
					a.actionPerformed( new ActionEvent( focusOwner, ActionEvent.ACTION_PERFORMED, null ) );
				}
			}
		}
	}
	
	
	
	private static class RootSubject extends TransientSubject
	{
		private Object focus;
		
		public RootSubject(Object focus)
		{
			super( null );
			this.focus = focus;
		}

		@Override
		public Object getFocus()
		{
			return focus;
		}

		@Override
		public String getTitle()
		{
			return "root";
		}
	}
	
	
	
	private static DefaultRootPage root = new DefaultRootPage();
	private static Subject rootSubject = new RootSubject( root );

	
	public static void main(final String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		
		BrowserTest test = new BrowserTest();
		
		test.createNewBrowserWindow( rootSubject );
	}

	
	public void createNewBrowserWindow(Subject subject)
	{
		// EDIT MENU
		
		CommandConsoleFactory fac = new CommandConsoleFactory()
		{
			@Override
			public AbstractCommandConsole createCommandConsole(PresentationComponent pres, Browser browser)
			{
				return new CommandConsole( browser, pres );
			}
		};
		
		
		
		
		final TabbedBrowser browser = new TabbedBrowser( rootSubject, subject, null, this, fac );

		
		
		
		TransferActionListener transferActionListener = new TransferActionListener();
		
		JMenu editMenu = new JMenu( "Edit" );
		
		JMenuItem editCutItem = new JMenuItem( "Cut" );
		editCutItem.setActionCommand( (String)TransferHandler.getCutAction().getValue( Action.NAME ) );
		editCutItem.addActionListener( transferActionListener );
		editCutItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK ) );
		editCutItem.setMnemonic( KeyEvent.VK_T );
		editMenu.add( editCutItem );
		
		JMenuItem editCopyItem = new JMenuItem( "Copy" );
		editCopyItem.setActionCommand( (String)TransferHandler.getCopyAction().getValue( Action.NAME ) );
		editCopyItem.addActionListener( transferActionListener );
		editCopyItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK ) );
		editCopyItem.setMnemonic( KeyEvent.VK_C );
		editMenu.add( editCopyItem );
		
		JMenuItem editPasteItem = new JMenuItem( "Paste" );
		editPasteItem.setActionCommand( (String)TransferHandler.getPasteAction().getValue( Action.NAME ) );
		editPasteItem.addActionListener( transferActionListener );
		editPasteItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK ) );
		editPasteItem.setMnemonic( KeyEvent.VK_P );
		editMenu.add( editPasteItem );

		
		// VIEW MENU
		
		AbstractAction elementExplorerAction = new AbstractAction( "Show element tree explorer" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Browser currentTab = browser.getCurrentBrowser();
				LSElement.ElementTreeExplorer treeExplorer = currentTab.getRootElement().treeExplorer();
				Subject subject = DefaultPerspective.instance.objectSubject( treeExplorer );
				browser.openSubjectInNewWindow( subject );
			}
		};
		
		JMenu viewMenu = new JMenu( "View" );
		
		JMenuItem viewElementTreeExplorerItem = new JMenuItem( elementExplorerAction );
		viewMenu.add( viewElementTreeExplorerItem );
		

		
		// MENU BAR
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add( editMenu );
		menuBar.add( viewMenu );
		
		
		JFrame frame = new JFrame( "Browser test" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		
		browser.getComponent().setPreferredSize( new Dimension( 800, 600 ) );
		frame.setJMenuBar( menuBar );
		frame.add( browser.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}


	@Override
	public void onTabbledBrowserChangePage(TabbedBrowser browser)
	{
	}
}
