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
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
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
import BritefuryJ.Controls.ResizeableBin;
import BritefuryJ.Controls.ScrolledViewport;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentInspector;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.IncrementalView;
import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.PersistentState.PersistentStateStore;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.*;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.Projection.Subject;
import BritefuryJ.Projection.SubjectPath;
import BritefuryJ.Projection.TransientSubject;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Browser implements PaneManager
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
	
	
	private class ResolveErrorSubject extends TransientSubject
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



	private class PaneSubject extends TransientSubject
	{
		private Object focus;
		private AbstractPerspective perspective;


		public PaneSubject(Object focus, AbstractPerspective perspective)
		{
			super( rootSubject );

			this.focus = focus;
			this.perspective = perspective;
		}

		@Override
		public Object getFocus()
		{
			return focus;
		}

		@Override
		public AbstractPerspective getPerspective()
		{
			return perspective;
		}

		@Override
		public String getTitle()
		{
			return "Pane";
		}
	}



	
	
	private class Pane
	{
		private IncrementalView view;
		private ScrolledViewport.ScrolledViewportControl viewport;


		public Pane()
		{
			Pres childPres = new Blank();
			ScrolledViewport vp = new ScrolledViewport( childPres, 0.0, 0.0, history.getCurrentState().getViewportState() );
			viewport = (ScrolledViewport.ScrolledViewportControl)vp.createControl( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );
		}

		public Pane(Subject s, PersistentStateStore stateStore)
		{
			view = new IncrementalView( s, inspector, stateStore );
			Pres childPres = Pres.coerce( view.getViewPres() );
			ScrolledViewport vp = new ScrolledViewport( childPres, 0.0, 0.0, history.getCurrentState().getViewportState() );
			viewport = (ScrolledViewport.ScrolledViewportControl)vp.createControl( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );
		}


		public LSElement getElement()
		{
			return viewport.getElement();
		}


		public void viewportReset()
		{
			viewport.getViewportElement().resetXform();
		}

		public void viewportOneToOne()
		{
			viewport.getViewportElement().oneToOne();
		}

		public PersistentStateStore storePersistentState()
		{
			return view != null  ?  view.storePersistentState()  :  null;
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


	private BrowserTrail trail;
	private JPanel panel;

	private RootPresentationComponent presComponent;
	private CommandBar commandBar;
	private BrowserHistory history;
	
	private Subject rootSubject, subject;
	private FragmentInspector inspector;
	private BrowserListener listener;
	
	private Pane mainPane;
	private Pane leftPane, rightPane, topPane, bottomPane;
	private LSRow horizontalSection;
	private LSColumn verticalSection;

	
	
	public Browser(Subject rootSubject, Subject subject, FragmentInspector inspector, PageController pageController, CommandConsoleFactory commandConsoleFactory)
	{
		this.rootSubject = rootSubject;
		this.subject = subject;
		this.inspector = inspector;
		history = new BrowserHistory( subject );

		mainPane = new Pane();
		
		presComponent = new RootPresentationComponent();
		presComponent.setPageController( pageController );
		presComponent.setPaneManager( this );
		presComponent.getRootElement().setChild( mainPane.getElement() );
		
		ActionMap actionMap = presComponent.getActionMap();
		actionMap.put( TransferHandler.getCutAction().getValue( Action.NAME ), TransferHandler.getCutAction() );
		actionMap.put( TransferHandler.getCopyAction().getValue( Action.NAME ), TransferHandler.getCopyAction() );
		actionMap.put( TransferHandler.getPasteAction().getValue( Action.NAME ), TransferHandler.getPasteAction() );


		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		initialiseToolbar(toolbar);
		
		toolbar.add( Box.createHorizontalStrut( 10 ) );
		
		trail = new BrowserTrail();
		trail.setPageController( pageController );
		trail.setMaximumSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
		toolbar.add( trail );
		
		
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
		
		setSubjectAsPage( subject );
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
		onPreHistoryChange();
		history.visit( subject );
		setSubjectAsPage( subject );
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
		viewportReset();
		setSubjectAsPage( subject );
	}
	
	
	public void viewportReset()
	{
		mainPane.viewportReset();
	}

	public void viewportOneToOne()
	{
		mainPane.viewportOneToOne();
	}



	@Override
	public void setLeftPaneContent(Object contents, AbstractPerspective perspective)
	{
		boolean wasSet = leftPane != null;
		leftPane = createPaneFromContents( contents, perspective );
		Pres r = new ResizeableBin( new PresentElement( leftPane.getElement() ) ).resizeRight( 100.0 );
		LSElement elem = r.present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignVExpand() );
		if ( wasSet )
		{
			horizontalSection.set( 0, elem );
		}
		else
		{
			horizontalSection.insert( 0, elem );
		}
	}

	@Override
	public void clearLeftPaneContent()
	{
		if ( leftPane != null )
		{
			horizontalSection.__delitem__( 0 );
			leftPane = null;
		}
	}


	@Override
	public void setRightPaneContent(Object contents, AbstractPerspective perspective)
	{
		boolean wasSet = rightPane != null;
		rightPane = createPaneFromContents( contents, perspective );
		Pres r = new ResizeableBin( new PresentElement( rightPane.getElement() ) ).resizeLeft( 100.0 );
		LSElement elem = r.present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignVExpand() );
		if ( wasSet )
		{
			horizontalSection.__delitem__( horizontalSection.size() - 1 );
		}
		horizontalSection.append( elem );
	}

	@Override
	public void clearRightPaneContent()
	{
		if ( rightPane != null )
		{
			horizontalSection.__delitem__( horizontalSection.size() - 1 );
			rightPane = null;
		}
	}



	@Override
	public void setTopPaneContent(Object contents, AbstractPerspective perspective)
	{
		boolean wasSet = topPane != null;
		topPane = createPaneFromContents( contents, perspective );
		Pres r = new ResizeableBin( new PresentElement( topPane.getElement() ) ).resizeBottom( 100.0 );
		LSElement elem = r.present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand() );
		if ( wasSet )
		{
			verticalSection.set( 0, elem );
		}
		else
		{
			verticalSection.insert( 0, elem );
		}
	}

	@Override
	public void clearTopPaneContent()
	{
		if ( topPane != null )
		{
			verticalSection.__delitem__( 0 );
			topPane = null;
		}
	}


	@Override
	public void setBottomPaneContent(Object contents, AbstractPerspective perspective)
	{
		boolean wasSet = bottomPane != null;
		bottomPane = createPaneFromContents( contents, perspective );
		Pres r = new ResizeableBin( new PresentElement( bottomPane.getElement() ) ).resizeTop( 100.0 );
		LSElement elem = r.present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand() );
		if ( wasSet )
		{
			verticalSection.__delitem__( verticalSection.size() - 1 );
		}
		verticalSection.append( elem );
	}

	@Override
	public void clearBottomPaneContent()
	{
		if ( bottomPane != null )
		{
			verticalSection.__delitem__( verticalSection.size() - 1 );
			bottomPane = null;
		}
	}




	
	
	private Pane createPaneFromContents(Object contents, AbstractPerspective perspective)
	{
		BrowserState state = history.getCurrentState();
		PersistentStateStore stateStore = state.getPagePersistentState();
		PaneSubject subject = new PaneSubject( contents, perspective );
		return new Pane( subject, stateStore );
	}


	
	protected void back()
	{
		if ( history.canGoBack() )
		{
			onPreHistoryChange();
			history.back();
			resolvePath();
		}
	}
	
	protected void forward()
	{
		if ( history.canGoForward() )
		{
			onPreHistoryChange();
			history.forward();
			resolvePath();
		}
	}
	
	protected void reload()
	{
		resolvePath();
	}

	
	

	private void resolvePath()
	{
		// Get the path to resolve
		BrowserState state = history.getCurrentState();
		SubjectPath path = state.getSubjectPath();
		Subject s;
		try
		{
			s = path.followFrom( rootSubject );
		}
		catch (Throwable t)
		{
			s = new ResolveErrorSubject( t );
		}
		
		setSubjectAsPage( s );
	}
	
	private void setSubjectAsPage(Subject s)
	{
		BrowserState state = history.getCurrentState();
		PersistentStateStore stateStore = state.getPagePersistentState();

		mainPane = new Pane( s, stateStore );
		leftPane = rightPane = topPane = bottomPane = null;

		horizontalSection = (LSRow)new Row( new Pres[] { new PresentElement( mainPane.getElement() ) } ).present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );
		verticalSection = (LSColumn)new Column( new Pres[] { new PresentElement( horizontalSection ) } ).present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );

		commandBar.pageChanged( s );
		presComponent.getRootElement().setChild( verticalSection );
		
		trail.setTrail( s.getTrail() );

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


	private void onPreHistoryChange()
	{
		PersistentStateStore store = mainPane.storePersistentState();
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
