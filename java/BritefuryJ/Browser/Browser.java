//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.Command.AbstractCommandConsole;
import BritefuryJ.Command.BoundCommandSet;
import BritefuryJ.Command.Command;
import BritefuryJ.Command.CommandBar;
import BritefuryJ.Command.CommandConsoleFactory;
import BritefuryJ.Command.CommandSet;
import BritefuryJ.Controls.ScrolledViewport;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentInspector;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.IncrementalView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.PersistentState.PersistentStateStore;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Projection.Subject;
import BritefuryJ.Projection.SubjectPath;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Browser
{
	protected static interface BrowserListener
	{
		public void onBrowserChangePage(Browser browser, Subject subject, String title);
	}
	
	
	
	private static class ResolveError implements Presentable
	{
		private Throwable exception;
		
		public ResolveError(Throwable exception)
		{
			this.exception = exception;
		}
		
		
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			StyleSheet titleStyle = StyleSheet.style( Primitive.fontSize.as( 24 ) );
			
			Pres errorTitle = titleStyle.applyTo( new Label( "Could not resolve" ) );
			Pres exc = Pres.coerceNonNull( exception );
			Pres body = new Body( new Pres[] { errorTitle.alignHCentre(), exc.alignHCentre() } );
			return new Page( new Pres[] { body } );
		}
	}
	
	
	private class ResolveErrorSubject extends Subject
	{
		ResolveError error;
		
		public ResolveErrorSubject(Throwable exception)
		{
			super( null );
			
			error = new ResolveError( exception );
		}

		@Override
		public Object getFocus()
		{
			return error;
		}

		@Override
		public String getTitle()
		{
			return "Resolve error";
		}
	}
	
	
	
	
	
	
	private static final String COMMAND_BACK = "back";
	private static final String COMMAND_FORWARD = "forward";
	private static final String COMMAND_RELOAD = "reload";
	
	
	private static Command.CommandAction cmdNewTab_action = new Command.CommandAction()
	{
		public void commandAction(Object context, PageController pageController)
		{
			Browser browser = (Browser)context;
			pageController.openSubject( browser.rootSubject, PageController.OpenOperation.OPEN_IN_NEW_TAB );
		}
	};
	
	private static Command.CommandAction cmdNewWindow_action = new Command.CommandAction()
	{
		public void commandAction(Object context, PageController pageController)
		{
			Browser browser = (Browser)context;
			pageController.openSubject( browser.rootSubject, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
		}
	};
	
	
	private static Command.CommandAction cmdViewportOneToOne_action = new Command.CommandAction()
	{
		public void commandAction(Object context, PageController pageController)
		{
			((Browser)context).viewportOneToOne();
		}
	};
	
	private static Command.CommandAction cmdViewportReset_action = new Command.CommandAction()
	{
		public void commandAction(Object context, PageController pageController)
		{
			((Browser)context).viewportReset();
		}
	};
	
	
	private static Command cmdNewTab = new Command( "&New &tab", cmdNewTab_action );
	private static Command cmdNewWindow = new Command( "&New &window", cmdNewWindow_action );
	private static Command cmdViewportOneToOne = new Command( "&Viewport zoom &one-to-one", cmdViewportOneToOne_action );
	private static Command cmdViewportReset = new Command( "&Viewport &reset", cmdViewportReset_action );
	
	
	private static CommandSet commands = new CommandSet( "Larch.Browser", Arrays.asList( new Command[] { cmdNewTab, cmdNewWindow, cmdViewportOneToOne, cmdViewportReset } ) );


	private JTextField locationField;
	private JPanel panel;

	private PresentationComponent presComponent;
	private CommandBar commandBar;
	private ScrolledViewport.ScrolledViewportControl viewport;
	private BrowserHistory history;
	
	private Subject rootSubject, subject;
	private FragmentInspector inspector;
	private IncrementalView view;
	private BrowserListener listener;
	
	
	
	
	public Browser(Subject rootSubject, Subject subject, FragmentInspector inspector, PageController pageController, CommandConsoleFactory commandConsoleFactory)
	{
		this.rootSubject = rootSubject;
		this.subject = subject;
		this.inspector = inspector;
		history = new BrowserHistory( subject );
		
		viewport = makeViewport( new Blank(), history.getCurrentState().getViewportState() );
		
		presComponent = new PresentationComponent();
		presComponent.setPageController( pageController );
		presComponent.getRootElement().setChild( viewport.getElement() );
		
		ActionMap actionMap = presComponent.getActionMap();
		actionMap.put( TransferHandler.getCutAction().getValue( Action.NAME ), TransferHandler.getCutAction() );
		actionMap.put( TransferHandler.getCopyAction().getValue( Action.NAME ), TransferHandler.getCopyAction() );
		actionMap.put( TransferHandler.getPasteAction().getValue( Action.NAME ), TransferHandler.getPasteAction() );


		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		initialiseToolbar(toolbar);
		
		
		locationField = new JTextField( "<TODO: replace>" );
		locationField.setMaximumSize( new Dimension( locationField.getMaximumSize().width, locationField.getMinimumSize().height ) );
		locationField.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) );
		locationField.setDragEnabled( true );
		
		ActionListener locationActionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
			}
		};
		
		locationField.addActionListener( locationActionListener );
		toolbar.add(locationField);
		
		
		JPanel header = new JPanel( new BorderLayout() );
		header.add(toolbar, BorderLayout.PAGE_START );
		
		
		panel = new JPanel( new BorderLayout() );
		panel.add( header, BorderLayout.PAGE_START );
		panel.add( presComponent, BorderLayout.CENTER );
		panel.setBackground( Color.WHITE );

		
		
		if ( commandConsoleFactory != null )
		{
			AbstractCommandConsole commandConsole = commandConsoleFactory.createCommandConsole( presComponent, this );
			commandBar = new CommandBar( presComponent, commandConsole, pageController );
			presComponent.getRootElement().getKeyboard().addInteractor( commandConsole.getShortcutKeyboardInteractor() );
		}
		
		resolve();
	}
	
	
	
	public JComponent getComponent()
	{
		return panel;
	}
	
	public String getTitle()
	{
		return subject.getTitle();
	}
	
	public LSElement getRootElement()
	{
		return presComponent.getRootElement();
	}
	
	
	
	public Subject getSubject()
	{
		return subject;
	}
	
	public void goToSubject(Subject subject)
	{
		locationField.setText( "<TODO: replace (goToPath)>" );
		setSubject( subject );
	}
	
	
	
	
	public void setListener(BrowserListener listener)
	{
		this.listener = listener;
	}
	
	
	
	public ChangeHistory getChangeHistory()
	{
		if ( subject != null )
		{
			return subject.getChangeHistory();
		}
		else
		{
			return null;
		}
	}
	

	
	public void reset(Subject subject)
	{
		history.visit( subject );
		history.clear();
		locationField.setText( "<TODO: replace (reset)>" );
		viewportReset();
		resolve();
	}
	
	
	public void viewportReset()
	{
		viewport.getViewportElement().resetXform();
	}

	public void viewportOneToOne()
	{
		viewport.getViewportElement().oneToOne();
	}

	
	
	
	
	
	
	protected void back()
	{
		if ( history.canGoBack() )
		{
			onPreHistoryChange();
			history.back();
			locationField.setText( "<TODO: replace (back)>" );
			resolve();
		}
	}
	
	protected void forward()
	{
		if ( history.canGoForward() )
		{
			onPreHistoryChange();
			history.forward();
			locationField.setText( "<TODO: replace (forward)>" );
			resolve();
		}
	}
	
	protected void reload()
	{
		resolve();
	}

	
	
	private ScrolledViewport.ScrolledViewportControl makeViewport(Object child, PersistentState state)
	{
		Pres childPres = Pres.coerce( child );
		ScrolledViewport vp = new ScrolledViewport( childPres, 0.0, 0.0, state );
		return (ScrolledViewport.ScrolledViewportControl)vp.createControl( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );
	}
	

	private void resolve()
	{
		// Get the location to resolve
		BrowserState state = history.getCurrentState();
		Subject s = history.getCurrentSubject();
		
		PersistentStateStore stateStore = state.getPagePersistentState();
		
		if ( s == null )
		{
			SubjectPath path = state.getSubjectPath();
			try
			{
				s = path.followFrom( rootSubject );
			}
			catch (Throwable t)
			{
				s = new ResolveErrorSubject( t );
			}
		}
		
		
		view = new IncrementalView( s, inspector, stateStore );

		viewport = makeViewport( view.getViewPres(), history.getCurrentState().getViewportState() );
		commandBar.pageChanged( s );
		presComponent.getRootElement().setChild( viewport.getElement() );
		
		// Set the subject
		if ( s != subject )
		{
			subject = s;
			
			if ( listener != null )
			{
				listener.onBrowserChangePage( this, s, getTitle() );
			}
		}
	}
	
	
	private void setSubject(Subject subject)
	{
		onPreHistoryChange();
		history.visit( subject );
		resolve();
	}
	
	
	private void onPreHistoryChange()
	{
		PersistentStateStore store = view.storePersistentState();
		history.getCurrentState().setPagePersistentState( store );
	}
	
	
	
	public void buildBoundCommandSetList(List<BoundCommandSet> boundCommandSets)
	{
		boundCommandSets.add( commands.bindTo( this ) );
	}



	
	private void initialiseToolbar(JToolBar toolbar)
	{
		ActionListener backListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				back();
			}
		};

		ActionListener forwardListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				forward();
			}
		};
		
		ActionListener reloadListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				reload();
			}
		};
		
		
		toolbar.add( makeToolButton( "back arrow.png", COMMAND_BACK, "Go back", "Back", backListener ) );
		toolbar.add( makeToolButton( "forward arrow.png", COMMAND_FORWARD, "Go forward", "Forward", forwardListener ) );
		toolbar.add( makeToolButton( "reload.png", COMMAND_RELOAD, "Reload page", "Reload", reloadListener ) );
	}
	
	private JButton makeToolButton(String imageFilename, String actionCommand, String tooltipText, String altText, ActionListener listener)
	{
		JButton button = new JButton();
		button.setActionCommand( actionCommand );
		button.setToolTipText( tooltipText );
		button.addActionListener( listener );
		button.setFocusable( false );
		
		ImageIcon icon;
		URL u = getClass().getResource( "/images/" + imageFilename );
		if ( u != null )
		{
			icon = new ImageIcon( u, altText );
		}
		else
		{
			icon = new ImageIcon( "images/" + imageFilename, altText );
		}
		
		if ( icon.getImageLoadStatus() != MediaTracker.ABORTED  &&  icon.getImageLoadStatus() != MediaTracker.ERRORED )
		{
			button.setIcon( icon );
		}
		else
		{
			button.setText( altText );
			System.err.println( "Could not load button image " + imageFilename );
		}
		
		return button;
	}
}
