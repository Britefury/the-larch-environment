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
			Pres exc = Pres.coercePresentingNull(exception);
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
		private LSElement element;


		public Pane()
		{
			Pres childPres = new Blank();
		}

		public Pane(Subject s, PersistentStateStore stateStore)
		{
			view = new IncrementalView( s, inspector, stateStore );
			Pres childPres = Pres.coerce( view.getViewPres() );
			element = childPres.present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );
		}


		public LSElement getElement()
		{
			return element;
		}
	}


	private class ScrolledPane
	{
		private IncrementalView view;
		private ScrolledViewport.ScrolledViewportControl viewport;


		public ScrolledPane()
		{
			Pres childPres = new Blank();
			ScrolledViewport vp = new ScrolledViewport( childPres, 0.0, 0.0, history.getCurrentState().getViewportState() );
			viewport = (ScrolledViewport.ScrolledViewportControl)vp.createControl( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );
		}

		public ScrolledPane(Subject s, PersistentStateStore stateStore)
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


	private static abstract class BrowserEdgePane implements EdgePane
	{
		private Browser browser;
		private Pane pane;
		private LSBin container;
		private boolean vertical;



		private BrowserEdgePane(Browser browser, LSBin container, boolean vertical)
		{
			this.browser = browser;
			this.container = container;
			this.vertical = vertical;
		}


		@Override
		public void setContent(Object contents, AbstractPerspective perspective, double size)
		{
			StyleValues style = vertical  ?  StyleValues.getRootStyle().alignVExpand()  :  StyleValues.getRootStyle().alignHExpand();
			pane = browser.createPaneFromContents( contents, perspective );
			Pres r = resize( new ResizeableBin( new PresentElement( pane.getElement() ) ), size);
			LSElement elem = r.present( PresentationContext.defaultCtx, style );
			container.setChild( elem );
		}

		@Override
		public void clearContent()
		{
			container.setChild( null );
		}

		protected abstract ResizeableBin resize(ResizeableBin bin, double size);
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
	
	private ScrolledPane mainPane;
	private BrowserEdgePane leftEdge, rightEdge, topEdge, bottomEdge;

	
	
	public Browser(Subject rootSubject, Subject subject, FragmentInspector inspector, PageController pageController, CommandConsoleFactory commandConsoleFactory)
	{
		this.rootSubject = rootSubject;
		this.subject = subject;
		this.inspector = inspector;
		history = new BrowserHistory( subject );

		mainPane = new ScrolledPane();
		
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
	public EdgePane getLeftEdgePane()
	{
		return leftEdge;
	}

	@Override
	public EdgePane getRightEdgePane()
	{
		return rightEdge;
	}

	@Override
	public EdgePane getTopEdgePane()
	{
		return topEdge;
	}

	@Override
	public EdgePane getBottomEdgePane()
	{
		return bottomEdge;
	}


	
	
	private Pane createPaneFromContents(Object contents, AbstractPerspective perspective)
	{
		BrowserState state = history.getCurrentState();
		PersistentStateStore stateStore = state.getPagePersistentState();
		PaneSubject subject = new PaneSubject( contents, perspective );
		return new Pane( subject, stateStore );
	}

	private ScrolledPane createScrolledPaneFromContents(Object contents, AbstractPerspective perspective)
	{
		BrowserState state = history.getCurrentState();
		PersistentStateStore stateStore = state.getPagePersistentState();
		PaneSubject subject = new PaneSubject( contents, perspective );
		return new ScrolledPane( subject, stateStore );
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

		mainPane = new ScrolledPane( s, stateStore );

		LSBin leftContainer = (LSBin)new Bin().present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignVExpand() );
		LSBin rightContainer = (LSBin)new Bin().present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignVExpand() );
		LSBin topContainer = (LSBin)new Bin().present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand() );
		LSBin bottomContainer = (LSBin)new Bin().present( PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand() );

		LSRow horizontalSection = (LSRow)new Row( new Pres[] { new PresentElement( leftContainer ), new PresentElement( mainPane.getElement() ), new PresentElement( rightContainer ) } ).present(
				PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );
		LSColumn verticalSection = (LSColumn)new Column( new Pres[] { new PresentElement( topContainer ), new PresentElement( horizontalSection ), new PresentElement( bottomContainer ) } ).present(
				PresentationContext.defaultCtx, StyleValues.getRootStyle().alignHExpand().alignVExpand() );

		leftEdge = new BrowserEdgePane( this, leftContainer, true ) {
			protected ResizeableBin resize(ResizeableBin bin, double size)
			{
				return bin.resizeRight( size );
			}
		};
		rightEdge = new BrowserEdgePane( this, rightContainer, true ) {
			protected ResizeableBin resize(ResizeableBin bin, double size)
			{
				return bin.resizeLeft( size );
			}
		};
		topEdge = new BrowserEdgePane( this, topContainer, false ) {
			protected ResizeableBin resize(ResizeableBin bin, double size)
			{
				return bin.resizeBottom( size );
			}
		};
		bottomEdge = new BrowserEdgePane( this, bottomContainer, false ) {
			protected ResizeableBin resize(ResizeableBin bin, double size)
			{
				return bin.resizeTop( size );
			}
		};

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
