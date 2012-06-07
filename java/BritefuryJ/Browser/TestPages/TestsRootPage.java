//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.util.ArrayList;
import java.util.Collections;

import BritefuryJ.Browser.BrowserPage;
import BritefuryJ.Browser.Location;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.LinkHeaderBar;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;
import BritefuryJ.Pres.UI.SectionHeading2;

public class TestsRootPage extends BrowserPage
{
	public TestsRootPage()
	{
	}
	
	
	
	public String getTitle()
	{
		return "Tests";
	}
	

	
	public Pres getContentsPres()
	{
		Pres title = new TitleBar( "Tests" );
		
		Pres head = new Head( new Pres[] { createLinkHeader( TestsRootPage.LINKHEADER_ROOTPAGE ), title } );
		
		return new Page( new Pres[] { head, createTestsContents() } );
	}

	
	public static int LINKHEADER_ROOTPAGE = 0x1;
	public static int LINKHEADER_SYSTEMPAGE = 0x2;
	
	public static Pres createLinkHeader(int linkHeaderFlags)
	{
		ArrayList<Object> links = new ArrayList<Object>();
		
		if ( ( linkHeaderFlags & LINKHEADER_ROOTPAGE )  !=  0 )
		{
			links.add( new Hyperlink( "HOME PAGE", new Location( "" ) ) );
		}
		
		if ( ( linkHeaderFlags & LINKHEADER_SYSTEMPAGE )  !=  0 )
		{
			links.add( new Hyperlink( "TESTS PAGE", new Location( "tests" ) ) );
		}
		
		return new LinkHeaderBar( links );
	}
	
	
	
	
	
	protected Pres createTestsBox(String title, Hyperlink links[])
	{
		ArrayList<Object> testBoxChildren = new ArrayList<Object>();
		
		Pres heading = new SectionHeading2( title ).pad( 0.0, 0.0, 5.0, 15.0 );
		testBoxChildren.add( heading );

		Collections.addAll( testBoxChildren, links );
		
		return new Column( testBoxChildren ).pad( 10.0, 10.0 ).alignVRefY();
	}
	
	protected Pres createTestsContents()
	{
		ArrayList<Object> testBoxes = new ArrayList<Object>();
		testBoxes.add( createTestsBox( "Primitive elements", primitiveLinks ).pad( 25.0, 5.0 ) );
		testBoxes.add( createTestsBox( "Controls", controlsLinks ).pad( 25.0, 5.0 ) );

		return new Body( new Pres[] { new Row( testBoxes ).alignHPack().alignVTop() } );
	}

	
	
	
	public static final AlignmentTestPage alignment = new AlignmentTestPage();
	public static final BorderTestPage border = new BorderTestPage();
	public static final ColumnTestPage column = new ColumnTestPage();
	public static final ClipboardTestPage clipboard = new ClipboardTestPage();
	public static final DndTestPage dnd = new DndTestPage();
	public static final FlowGridTestPage flowGrid = new FlowGridTestPage();
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
		new Hyperlink( "Alignment", new Location( "tests.alignment" ) ),
		new Hyperlink( "Border", new Location( "tests.border" ) ),
		new Hyperlink( "Column", new Location( "tests.column" ) ),
		new Hyperlink( "Clipboard", new Location( "tests.clipboard" ) ),
		new Hyperlink( "Drag and drop", new Location( "tests.dnd" ) ),
		new Hyperlink( "Flow grid", new Location( "tests.flowGrid" ) ),
		new Hyperlink( "Fraction", new Location( "tests.fraction" ) ),
		new Hyperlink( "Grid", new Location( "tests.grid" ) ),
		new Hyperlink( "Image", new Location( "tests.image" ) ),
		new Hyperlink( "Math root", new Location( "tests.mathRoot" ) ),
		new Hyperlink( "Non-local drag and drop", new Location( "tests.nonLocalDnd" ) ),
		new Hyperlink( "Paragraph", new Location( "tests.paragraph" ) ),
		new Hyperlink( "Paragraph with span", new Location( "tests.paragraphWithSpan" ) ),
		new Hyperlink( "Proxy and span", new Location( "tests.proxyAndSpan" ) ),
		new Hyperlink( "Row", new Location( "tests.row" ) ),
		new Hyperlink( "Script", new Location( "tests.script" ) ),
		new Hyperlink( "Segment", new Location( "tests.segment" ) ),
		new Hyperlink( "Sequence view", new Location( "tests.sequenceView" ) ),
		new Hyperlink( "Shape", new Location( "tests.shape" ) ),
		new Hyperlink( "Table", new Location( "tests.table" ) ),
		new Hyperlink( "Text", new Location( "tests.text" ) ),
		new Hyperlink( "Viewport", new Location( "tests.viewport" ) ),
	};

	
	
	
	public static final BubblePopupTestPage bubblePopup = new BubblePopupTestPage();
	public static final ButtonTestPage button = new ButtonTestPage();
	public static final CheckboxTestPage checkbox = new CheckboxTestPage();
	public static final ColourPickerTestPage colourPicker = new ColourPickerTestPage();
	public static final CommandConsoleTestPage commandConsole = new CommandConsoleTestPage();
	public static final EditableLabelTestPage editableLabel = new EditableLabelTestPage();
	public static final ExpanderTestPage expander = new ExpanderTestPage();
	public static final HyperlinkTestPage hyperlink = new HyperlinkTestPage();
	public static final NumericLabelTestPage numLabel = new NumericLabelTestPage();
	public static final ObjectDropBoxTestPage objectDrop = new ObjectDropBoxTestPage();
	public static final OptionMenuTestPage optionMenu = new OptionMenuTestPage();
	public static final PopupTestPage popup = new PopupTestPage();
	public static final ScrollBarTestPage scrollBar = new ScrollBarTestPage();
	public static final ScrolledViewportTestPage scrolledViewport = new ScrolledViewportTestPage();
	public static final SliderTestPage slider = new SliderTestPage();
	public static final SpinEntryTestPage spinEntry = new SpinEntryTestPage();
	public static final TabbedBoxTestPage tabbedBox = new TabbedBoxTestPage();
	public static final TextAreaTestPage textArea = new TextAreaTestPage();
	public static final TextEntryTestPage textEntry = new TextEntryTestPage();
	
	private static final Hyperlink controlsLinks[] = {
		new Hyperlink( "Bubble popup", new Location( "tests.bubblePopup" ) ),
		new Hyperlink( "Button", new Location( "tests.button" ) ),
		new Hyperlink( "Check box", new Location( "tests.checkbox" ) ),
		new Hyperlink( "Colour picker", new Location( "tests.colourPicker" ) ),
		new Hyperlink( "Command console", new Location( "tests.commandConsole" ) ),
		new Hyperlink( "Editable label", new Location( "tests.editableLabel" ) ),
		new Hyperlink( "Expander", new Location( "tests.expander" ) ),
		new Hyperlink( "Hyperlink", new Location( "tests.hyperlink" ) ),
		new Hyperlink( "Numeric label", new Location( "tests.numLabel" ) ),
		new Hyperlink( "Object drop box", new Location( "tests.objectDrop" ) ),
		new Hyperlink( "Option menu", new Location( "tests.optionMenu" ) ),
		new Hyperlink( "Popup", new Location( "tests.popup" ) ),
		new Hyperlink( "Scroll bar", new Location( "tests.scrollBar" ) ),
		new Hyperlink( "Scrolled viewport", new Location( "tests.scrolledViewport" ) ),
		new Hyperlink( "Slider", new Location( "tests.slider" ) ),
		new Hyperlink( "Spin entry", new Location( "tests.spinEntry" ) ),
		new Hyperlink( "Tabbed box", new Location( "tests.tabbedBox" ) ),
		new Hyperlink( "Text area", new Location( "tests.textArea" ) ),
		new Hyperlink( "Text entry", new Location( "tests.textEntry" ) ),
	};
}
