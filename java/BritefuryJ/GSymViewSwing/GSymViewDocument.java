package BritefuryJ.GSymViewSwing;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import BritefuryJ.GSymViewSwing.DocLayout.DocLayout;
import BritefuryJ.GSymViewSwing.DocLayout.DocLayoutNode;
import BritefuryJ.GSymViewSwing.ElementViewFactories.DocStructureDocRootViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.DocStructureParagraphViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.ElementViewFactory;


public class GSymViewDocument extends AbstractDocument implements StyledDocument
{
	public static class InvalidElementTypeException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static class OperationNotSupportedException extends RuntimeException
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
//		DocRootElement root = new DocRootElement( null, null );
//		ParagraphElement para = new ParagraphElement( root, null );
//		Element content = documentLayout.getRoot().createElementSubtree( para, 0 );
//		documentLayout.getRoot().setElement( content );
//		Element[] leaves = { content };
//		para.replace( 0, 0, leaves );
//		Element[] paras = { para };
//		root.replace( 0, 0, paras );
//		return root;

		DocRootElement root = new DocRootElement( null, null );
		Element content = documentLayout.getRoot().createElementSubtree( root, 0 );
		documentLayout.getRoot().setElement( content );
		Element[] leaves = { content };
		root.replace( 0, 0, leaves );
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

	
	
	public Style addStyle(String name, Style parent)
	{
		StyleContext context = (StyleContext)getAttributeContext();
		return context.addStyle( name, parent );
	}
	
	public Color getBackground(AttributeSet attribs)
	{
		StyleContext context = (StyleContext)getAttributeContext();
		return context.getBackground( attribs );
	}

	public Element getCharacterElement(int pos)
	{
		Element e = getDefaultRootElement();
		
		while ( !e.isLeaf() )
		{
			int index = e.getElementIndex( pos );
			e = e.getElement( index );
		}
		return e;
	}

	public Font getFont(AttributeSet attribs)
	{
		StyleContext context = (StyleContext)getAttributeContext();
		Font f = context.getFont( attribs );
		System.out.println( "GSymViewDocument.getFont(): font=" + f.toString() );
		return f;
	}

	public Color getForeground(AttributeSet attribs)
	{
		StyleContext context = (StyleContext)getAttributeContext();
		return context.getForeground( attribs );
	}

	public Style getLogicalStyle(int pos)
	{
		Style s = null;
		Element paragraph = getParagraphElement( pos );

		if ( paragraph != null )
		{
			AttributeSet attribs = paragraph.getAttributes();
			AttributeSet parent = attribs.getResolveParent();
			if ( parent instanceof Style )
			{
				s = (Style)parent;
			}
		}

		return s;
	}

	public Style getStyle(String name)
	{
		StyleContext context = (StyleContext)getAttributeContext();
		return context.getStyle( name );
	}

	public void removeStyle(String name)
	{
		StyleContext context = (StyleContext)getAttributeContext();
		context.removeStyle( name );
	}

	public void setCharacterAttributes(int offset, int length, AttributeSet attribs, boolean replace)
	{
		throw new OperationNotSupportedException();
	}

	public void setLogicalStyle(int pos, Style s)
	{
		throw new OperationNotSupportedException();
	}

	public void setParagraphAttributes(int offset, int length, AttributeSet attribs, boolean replace)
	{
		throw new OperationNotSupportedException();
	}
}
