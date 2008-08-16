package BritefuryJ.GSymViewSwing;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GapContent;

import BritefuryJ.GSymViewSwing.ElementViewFactories.DocStructureDocRootViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.DocStructureParagraphViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.DocStructureTextViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.ElementViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.GlyphViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.ParagraphViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.VBoxViewFactory;


public class GSymViewDocument extends AbstractDocument
{
	public static class InvalidElementTypeException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	private static final long serialVersionUID = 1L;
	
	
	
	public class DocRootElement extends BranchElement implements DocumentElement
	{
		private static final long serialVersionUID = 1L;

		public DocRootElement(Element parent, AttributeSet attribs)
		{
			super( parent, attribs );
		}
		
		public ElementViewFactory getViewFactory()
		{
			return DocStructureDocRootViewFactory.viewFactory;
		}
	}

	public class ParagraphElement extends BranchElement implements DocumentElement
	{
		private static final long serialVersionUID = 1L;

		public ParagraphElement(Element parent, AttributeSet attribs)
		{
			super( parent, attribs );
		}
		
		public ElementViewFactory getViewFactory()
		{
			return DocStructureParagraphViewFactory.viewFactory;
		}
	}

	
	public class TextElement extends LeafElement implements DocumentElement
	{
		private static final long serialVersionUID = 1L;

		public TextElement(Element parent, AttributeSet attribs, int off0, int off1)
		{
			super( parent, attribs, off0, off1 );
		}

		public ElementViewFactory getViewFactory()
		{
			return DocStructureTextViewFactory.viewFactory;
		}
	}
	

	public class CustomBranchElement extends BranchElement implements DocumentElement
	{
		private static final long serialVersionUID = 1L;
		private ElementViewFactory viewFactory;

		public CustomBranchElement(Element parent, AttributeSet attribs, ElementViewFactory viewFactory)
		{
			super( parent, attribs );
			this.viewFactory = viewFactory;
		}
		
		public ElementViewFactory getViewFactory()
		{
			return viewFactory;
		}
	}


	public class CustomLeafElement extends LeafElement implements DocumentElement
	{
		private static final long serialVersionUID = 1L;
		private ElementViewFactory viewFactory;

		public CustomLeafElement(Element parent, AttributeSet attribs, int off0, int off1, ElementViewFactory viewFactory)
		{
			super( parent, attribs, off0, off1 );
			this.viewFactory = viewFactory;
		}

		public ElementViewFactory getViewFactory()
		{
			return viewFactory;
		}
	}
	

	
	
	
	private BranchElement defaultRoot;
	private DocElementSpec elementReplaceOperationSpec;
	private AbstractElement elementReplaceOperationElementToReplace;
	private BranchElement elementReplaceOperationParent;
	private int elementReplaceOperationIndexInParent;
	
	
	

	public GSymViewDocument()
	{
		this( new GapContent( 1 ) );
	}
	
	protected GSymViewDocument(Content c)
	{
		super( c );
		
		defaultRoot = createDefaultRoot();
		elementReplaceOperationSpec = null;
		elementReplaceOperationElementToReplace = null;
	}



	public Element getDefaultRootElement()
	{
		return defaultRoot;
	}

	public Element getParagraphElement(int pos)
	{
		return defaultRoot.positionToElement( pos );
	}
	
	
	
	public void elementReplace(Element elementToReplace, DocElementSpec replacementElementSpec)
	{
		try
		{
			writeLock();
			String replacementText = replacementElementSpec.getText();
			elementReplaceOperationSpec = replacementElementSpec;
			elementReplaceOperationElementToReplace = (AbstractElement)elementToReplace;
			elementReplaceOperationParent = (BranchElement)elementReplaceOperationElementToReplace.getParentElement();
			elementReplaceOperationIndexInParent = elementReplaceOperationParent.getIndex( elementReplaceOperationElementToReplace );
			int start = elementToReplace.getStartOffset();
			int end = elementToReplace.getEndOffset();
			end = Math.min( end, getLength() );
			try
			{
				replace( start, end - start, replacementText, null );
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
			elementReplaceOperationSpec = null;
			elementReplaceOperationElementToReplace = null;
			elementReplaceOperationParent = null;
			elementReplaceOperationIndexInParent = -1;
		}
		finally
		{
			writeUnlock();
		}
	}
	
	
	
	
	
	
	protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr)
	{
		if ( elementReplaceOperationSpec == null )
		{
			
		}
		else
		{
			Element replacementElementSubtree = elementReplaceOperationSpec.createElementSubtree( this, elementReplaceOperationParent, elementReplaceOperationElementToReplace.getStartOffset() );
			Element[] oldElements = {};
			Element[] newElements = { replacementElementSubtree };

			ElementEdit ee = new ElementEdit( elementReplaceOperationParent, elementReplaceOperationIndexInParent, oldElements, newElements );
			chng.addEdit( ee );

			elementReplaceOperationParent.replace( elementReplaceOperationIndexInParent, 0, newElements );
		}

		super.insertUpdate( chng, attr );
	}
	
	protected void removeUpdate(DefaultDocumentEvent chng)
	{
		if ( elementReplaceOperationSpec != null )
		{
			Element[] oldElements = { elementReplaceOperationElementToReplace };
			Element[] newElements = {};

			ElementEdit ee = new ElementEdit( elementReplaceOperationParent, elementReplaceOperationIndexInParent, oldElements, newElements );
			chng.addEdit( ee );
			
			elementReplaceOperationParent.replace( elementReplaceOperationIndexInParent, 1, newElements );
		}

		super.removeUpdate(chng);
	}

	
	
	
	protected ElementViewFactory getElementViewFactory(Element e)
	{
		if ( e instanceof DocumentElement )
		{
			DocumentElement docElement = (DocumentElement)e;
			return docElement.getViewFactory();
		}
		else
		{
			throw new InvalidElementTypeException();
		}
	}



	private BranchElement createDefaultRoot()
	{
		DocRootElement root = new DocRootElement( null, null );
		ParagraphElement para = new ParagraphElement( root, null );
		TextElement leaf = new TextElement( para, null, 0, 1 );
		Element[] leaves = { leaf };
		para.replace( 0, 0, leaves );
		Element[] paras = { para };
		root.replace( 0, 0, paras );
		return root;
	}
	
	private DocElementSpec paramSpec()
	{
		DocElementSpecLeaf leaf0 = new DocElementSpecLeaf( "aaaaaaa,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf1 = new DocElementSpecLeaf( "bbbbbbb,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf2 = new DocElementSpecLeaf( "ccccccc,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf3 = new DocElementSpecLeaf( "ddddddd,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf4 = new DocElementSpecLeaf( "eeeeeee,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf5 = new DocElementSpecLeaf( "fffffff,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf6 = new DocElementSpecLeaf( "ggggggg,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf[] leaves = { leaf0, leaf1, leaf2, leaf3, leaf4, leaf5, leaf6 };
		DocElementSpecBranch branch = new DocElementSpecBranch( leaves, null, ParagraphViewFactory.viewFactory );
		return branch;
	}
	
	private DocElementSpec callSpec(DocElementSpec inner)
	{
		DocElementSpecLeaf openParen = new DocElementSpecLeaf( "(", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf closeParen = new DocElementSpecLeaf( ")", null, GlyphViewFactory.viewFactory );
		DocElementSpec[] innerOpen = { inner, openParen };
		DocElementSpec innerOpenBranch = new DocElementSpecBranch( innerOpen, null, VBoxViewFactory.viewFactory );
		DocElementSpec params = paramSpec();
		DocElementSpec[] call = { innerOpenBranch, params, closeParen };
		DocElementSpecBranch callBranch = new DocElementSpecBranch( call, null, ParagraphViewFactory.viewFactory );
		return callBranch;
	}
	
	

	private DocElementSpec getAttrSpec(DocElementSpec inner, String attrName)
	{
		DocElementSpecLeaf dot = new DocElementSpecLeaf( ".", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf attr = new DocElementSpecLeaf( attrName, null, GlyphViewFactory.viewFactory );
		DocElementSpec[] innerDot = { inner, dot };
		DocElementSpecBranch innerDotBranch = new DocElementSpecBranch( innerDot, null, VBoxViewFactory.viewFactory );
		DocElementSpec[] getattr = { innerDotBranch, attr };
		DocElementSpecBranch getattrBranch = new DocElementSpecBranch( getattr, null, ParagraphViewFactory.viewFactory );
		return getattrBranch;
	}
	
	
	
	DocElementSpec getattrSpecRecursive(DocElementSpec inner, int level)
	{
		if ( level == 0 )
		{
			return inner;
		}
		else
		{
			return getattrSpecRecursive( getAttrSpec( inner, "attr" + String.valueOf( level ) ), level - 1 );
		}
	}
	


	public void testFill()
	{
		DocElementSpecLeaf leaf0 = new DocElementSpecLeaf( "a,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf1 = new DocElementSpecLeaf( "b,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf2 = new DocElementSpecLeaf( "c,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf3 = new DocElementSpecLeaf( "d,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf4 = new DocElementSpecLeaf( "e,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf5 = new DocElementSpecLeaf( "f,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf leaf6 = new DocElementSpecLeaf( "g,", null, GlyphViewFactory.viewFactory );
		DocElementSpecLeaf[] leaves = { leaf0, leaf1, leaf2, leaf3, leaf4, leaf5, leaf6 };
		DocElementSpecBranch branch = new DocElementSpecBranch( leaves, null, ParagraphViewFactory.viewFactory );
		
		elementReplace( defaultRoot.getElement( 0 ).getElement( 0 ), branch );
	}
	
	
	
	public void testFill2()
	{
		DocElementSpec inner = new DocElementSpecLeaf( "this", null, GlyphViewFactory.viewFactory );
		inner = getattrSpecRecursive( inner, 4 );
		inner = callSpec( inner );
		inner = getattrSpecRecursive( inner, 4 );
		inner = callSpec( inner );
		inner = getattrSpecRecursive( inner, 4 );
		inner = callSpec( inner );
		
		elementReplace( defaultRoot.getElement( 0 ).getElement( 0 ), inner );
	}
	
	
	
	public CustomLeafElement createGSymLeafElement(Element parent, AttributeSet attribs, int start, int end, ElementViewFactory viewFactory)
	{
		return new CustomLeafElement( parent, attribs, start, end, viewFactory );
	}

	public CustomBranchElement createGSymBranchElement(Element parent, AttributeSet attribs, ElementViewFactory viewFactory)
	{
		return new CustomBranchElement( parent, attribs, viewFactory );
	}
}
