//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.BrowserPage;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.Heading4;
import BritefuryJ.StyleSheet.StyleSheet;

public class TestsDirectory extends BrowserPage
{
	public static final AlignmentTestPage alignment = new AlignmentTestPage();
	public static final BorderTestPage border = new BorderTestPage();
	public static final CanvasTestPage canvas = new CanvasTestPage();
	public static final ColumnTestPage column = new ColumnTestPage();
	public static final ClipboardTestPage clipboard = new ClipboardTestPage();
	public static final DndTestPage dnd = new DndTestPage();
	public static final FractionTestPage fraction = new FractionTestPage();
	public static final GridTestPage grid = new GridTestPage();
	public static final ImageTestPage image = new ImageTestPage();
	public static final MathRootTestPage mathRoot = new MathRootTestPage();
	public static final NonLocalDndTestPage nonLocalDnd = new NonLocalDndTestPage();
	public static final ParagraphTestPage paragraph = new ParagraphTestPage();
	public static final ParagraphWithSpanTestPage paragraphWithSpan = new ParagraphWithSpanTestPage();
	public static final ProxyAndSpanTestPage proxyAndSpan = new ProxyAndSpanTestPage();
	public static final RowTestPage row = new RowTestPage();
	public static final ScriptTestPage script = new ScriptTestPage();
	public static final SegmentTestPage segment = new SegmentTestPage();
	public static final SequenceViewTestPage sequenceView = new SequenceViewTestPage();
	public static final ShapeTestPage shape = new ShapeTestPage();
	public static final TableTestPage table = new TableTestPage();
	public static final TextTestPage text = new TextTestPage();
	public static final ViewportTestPage viewport = new ViewportTestPage();
	
	private static final Hyperlink primitiveLinks[] = {
		new Hyperlink( "Alignment", new Location( "system.tests.alignment" ) ),
		new Hyperlink( "Border", new Location( "system.tests.border" ) ),
		new Hyperlink( "Canvas", new Location( "system.tests.canvas" ) ),
		new Hyperlink( "Column", new Location( "system.tests.column" ) ),
		new Hyperlink( "Clipboard", new Location( "system.tests.clipboard" ) ),
		new Hyperlink( "Drag and drop", new Location( "system.tests.dnd" ) ),
		new Hyperlink( "Fraction", new Location( "system.tests.fraction" ) ),
		new Hyperlink( "Grid", new Location( "system.tests.grid" ) ),
		new Hyperlink( "Image", new Location( "system.tests.image" ) ),
		new Hyperlink( "Math root", new Location( "system.tests.mathRoot" ) ),
		new Hyperlink( "Non-local drag and drop", new Location( "system.tests.nonLocalDnd" ) ),
		new Hyperlink( "Paragraph", new Location( "system.tests.paragraph" ) ),
		new Hyperlink( "Paragraph with span", new Location( "system.tests.paragraphWithSpan" ) ),
		new Hyperlink( "Proxy and span", new Location( "system.tests.proxyAndSpan" ) ),
		new Hyperlink( "Row", new Location( "system.tests.row" ) ),
		new Hyperlink( "Script", new Location( "system.tests.script" ) ),
		new Hyperlink( "Segment", new Location( "system.tests.segment" ) ),
		new Hyperlink( "Sequence view", new Location( "system.tests.sequenceView" ) ),
		new Hyperlink( "Shape", new Location( "system.tests.shape" ) ),
		new Hyperlink( "Table", new Location( "system.tests.table" ) ),
		new Hyperlink( "Text", new Location( "system.tests.text" ) ),
		new Hyperlink( "Viewport", new Location( "system.tests.viewport" ) ),
	};

	
	
	
	public static final ButtonTestPage button = new ButtonTestPage();
	public static final CheckboxTestPage checkbox = new CheckboxTestPage();
	public static final CommandConsoleTestPage commandConsole = new CommandConsoleTestPage();
	public static final EditableLabelTestPage editableLabel = new EditableLabelTestPage();
	public static final ExpanderTestPage expander = new ExpanderTestPage();
	public static final HyperlinkTestPage hyperlink = new HyperlinkTestPage();
	public static final OptionMenuTestPage optionMenu = new OptionMenuTestPage();
	public static final PopupTestPage popup = new PopupTestPage();
	public static final ScrollBarTestPage scrollBar = new ScrollBarTestPage();
	public static final ScrolledViewportTestPage scrolledViewport = new ScrolledViewportTestPage();
	public static final SpinEntryTestPage spinEntry = new SpinEntryTestPage();
	public static final TabbedBoxTestPage tabbedBox = new TabbedBoxTestPage();
	public static final TextAreaTestPage textArea = new TextAreaTestPage();
	public static final TextEntryTestPage textEntry = new TextEntryTestPage();
	
	private static final Hyperlink controlsLinks[] = {
		new Hyperlink( "Button", new Location( "system.tests.button" ) ),
		new Hyperlink( "Check box", new Location( "system.tests.checkbox" ) ),
		new Hyperlink( "Command console", new Location( "system.tests.commandConsole" ) ),
		new Hyperlink( "Editable label", new Location( "system.tests.editableLabel" ) ),
		new Hyperlink( "Expander", new Location( "system.tests.expander" ) ),
		new Hyperlink( "Hyperlink", new Location( "system.tests.hyperlink" ) ),
		new Hyperlink( "Option menu", new Location( "system.tests.optionMenu" ) ),
		new Hyperlink( "Popup", new Location( "system.tests.popup" ) ),
		new Hyperlink( "Scroll bar", new Location( "system.tests.scrollBar" ) ),
		new Hyperlink( "Scrolled viewport", new Location( "system.tests.scrolledViewport" ) ),
		new Hyperlink( "Spin entry", new Location( "system.tests.spinEntry" ) ),
		new Hyperlink( "Tabbed box", new Location( "system.tests.tabbedBox" ) ),
		new Hyperlink( "Text area", new Location( "system.tests.textArea" ) ),
		new Hyperlink( "Text entry", new Location( "system.tests.textEntry" ) ),
	};
	
	public TestsDirectory()
	{
	}
	
	
	
	public String getTitle()
	{
		return "System tests";
	}


	private static StyleSheet outlineStyle = StyleSheet.style( Primitive.border.as( new SolidBorder( 2.0, 10.0, new Color( 0.6f, 0.7f, 0.8f ), null ) ) );

	
	
	public Pres getContentsPres()
	{
		return createContents();
	}

	
	protected Pres createTestsBox(String title, Hyperlink links[])
	{
		ArrayList<Object> testBoxChildren = new ArrayList<Object>();
		
		Pres heading = new Heading4( title ).pad( 30.0, 30.0, 5.0, 15.0 );
		testBoxChildren.add( heading );

		Collections.addAll( testBoxChildren, links );
		
		return outlineStyle.applyTo( new Border( new Column( testBoxChildren ).alignVRefY() ) );
	}
	
	protected Pres createContents()
	{
		Pres heading = new Heading2( "Tests" ).alignHCentre();
		
		ArrayList<Object> testBoxes = new ArrayList<Object>();
		testBoxes.add( createTestsBox( "Primitive elements:", primitiveLinks ).pad( 25.0, 5.0 ) );
		testBoxes.add( createTestsBox( "Controls:", controlsLinks ).pad( 25.0, 5.0 ) );

		return new Body( new Pres[] { heading, new Row( testBoxes ).alignHPack().alignVTop() } );
	}
}
