package BritefuryJ.GSymViewSwing;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GapContent;

import BritefuryJ.GSymViewSwing.DocLayout.DocLayout;
import BritefuryJ.GSymViewSwing.DocLayout.DocLayoutNode;
import BritefuryJ.GSymViewSwing.ElementViewFactories.DocStructureDocRootViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.DocStructureParagraphViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.DocStructureTextViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.ElementViewFactory;


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
	private DocLayoutNode elementReplaceOperationSpec;
	private AbstractElement elementReplaceOperationElementToReplace;
	private BranchElement elementReplaceOperationParent;
	private int elementReplaceOperationIndexInParent;
	private DocLayout documentLayout;
	
	
	

	public GSymViewDocument()
	{
		this( new GapContent( 1 ) );
	}
	
	protected GSymViewDocument(Content c)
	{
		super( c );
		
		documentLayout = new DocLayout( this );

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
	
	
	
	public void elementReplace(Element elementToReplace, DocLayoutNode replacementElementSpec)
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
			Element replacementElementSubtree = elementReplaceOperationSpec.createElementSubtree( elementReplaceOperationParent, elementReplaceOperationElementToReplace.getStartOffset() );
			elementReplaceOperationSpec.setElement( replacementElementSubtree );
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
		Element content = documentLayout.getRoot().createElementSubtree( para, 0 );
		documentLayout.getRoot().setElement( content );
		Element[] leaves = { content };
		para.replace( 0, 0, leaves );
		Element[] paras = { para };
		root.replace( 0, 0, paras );
		return root;
	}
	
	
	public CustomLeafElement createGSymLeafElement(Element parent, AttributeSet attribs, int start, int end, ElementViewFactory viewFactory)
	{
		return new CustomLeafElement( parent, attribs, start, end, viewFactory );
	}

	public CustomBranchElement createGSymBranchElement(Element parent, AttributeSet attribs, ElementViewFactory viewFactory)
	{
		return new CustomBranchElement( parent, attribs, viewFactory );
	}
	
	
	
	public DocLayout getDocumentLayout()
	{
		return documentLayout;
	}
}
