//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


class Merge
{
	private static boolean isIdentity(List<Object> xs, Map<Object, Object> styles)
	{
		return xs.isEmpty()  ||  styles.isEmpty();
	}
	
	
	private static void appendSpan(List<Object> xs, EdStyleSpan b)
	{
		Object last = xs.isEmpty()  ?  null  :  xs.get( xs.size() -1 );
		if ( xs.isEmpty()  ||  !( last instanceof EdStyleSpan ) )
		{
			// @xs is empty,
			// OR:
			// its last item is an inline embedded object
			// OR:
			// @xs is the contents of a style span itself, the last item is not a style span - it may be a string
			
			if ( b.getStyleAttrs().isEmpty() )
			{
				List<Object> bContents = b.getContents();
				Object bFirst = bContents.isEmpty()  ?  null  :  bContents.get( bContents.size() - 1 );
				if ( last instanceof String  &&  bFirst instanceof String )
				{
					// The last item in @xs and the first item in @b are both strings 
					// join the strings and append the rest
					xs.set( xs.size() - 1, ((String)last) + ((String)bFirst) );
					xs.addAll( bContents.subList( 1, bContents.size() ) );
				}
				else
				{
					// @b has no style attributes - join its contents onto @xs
					xs.addAll( b.getContents() );
				}
			}
			else
			{
				// @b has style attributes - append it
				xs.add( b );
			}
		}
		else if ( b.getStyleAttrs().isEmpty() )
		{
			xs.addAll( b.getContents() );
		}
		else
		{
			EdStyleSpan a = (EdStyleSpan)last;
			HashMap<Object, Object> aAttrs = a.getStyleAttrs();
			HashMap<Object, Object> bAttrs = b.getStyleAttrs();
			Set<Object> aKeys = aAttrs.keySet();
			Set<Object> bKeys = bAttrs.keySet();
			
			// Compute the sets of keys in A but not B, and B but not A
			HashSet<Object> aNotB = new HashSet<Object>( aKeys );
			aNotB.removeAll( bKeys );
			HashSet<Object> bNotA = new HashSet<Object>( bKeys );
			bNotA.removeAll( aKeys );
			
			// Compute the set of keys common to A and B, which have the same value
			HashSet<Object> common = new HashSet<Object>( aKeys );
			common.retainAll( bKeys );
			
			// @common now contains the keys common to both, but includes keys with
			// different associated values

			// Compute the set of keys for which values differ
			HashSet<Object> changed = new HashSet<Object>();
			for (Object k: common)
			{
				if ( !aAttrs.get( k ).equals( bAttrs.get( k ) ) )
				{
					changed.add( k );
				}
			}
			
			// Remove the changed keys from @common
			common.removeAll( changed );
			
			
			if ( common.isEmpty() )
			{
				if ( aKeys.isEmpty()  && bKeys.isEmpty() )
				{
					// Handle the case where no style attributes are present on either @a or @b
					// Join the contents of @b onto @a
					appendSpan( a.getContents(), b );
				}
				else
				{
					// No attributes in common - just append @b to the list
					xs.add( b );
				}
			}
			else
			{
				if ( aNotB.isEmpty()  &&  changed.isEmpty() )
				{
					// The style attributes in @b are a superset of those in @a
					// Therefore @b can be contained with @a, with the addition of the relevant attrs
					// Append @b to the contents of @a
					
					// Note that this also handles the case where we have two adjacent spans with identical style attributes
					// The new EdStyleSpan created for the appendSpan() call will create a style span with no attributes
					// which will be handled by the first clause in appendSpan()
					HashMap<Object, Object> bMap = new HashMap<Object, Object>();
					for (Object k: bNotA)
					{
						bMap.put( k, bAttrs.get( k ) );
					}
					appendSpan( a.getContents(), new EdStyleSpan( b.getContents(), bMap ) );
				}
				else if ( bNotA.isEmpty()  &&  changed.isEmpty() )
				{
					// The style attributes in @a are a superset of those in @b
					// Therefore @a can be contained with @b, with the addition of the relevant attrs
					// Replace @a with @b, with @a prepended
					HashMap<Object, Object> aMap = new HashMap<Object, Object>();
					for (Object k: aNotB)
					{
						aMap.put( k, aAttrs.get( k ) );
					}
					b.getContents().add( 0, new EdStyleSpan( a.getContents(), aMap ) );
					xs.set( xs.size() - 1, b );
				}
				else
				{
					HashSet<Object> x = new HashSet<Object>( aKeys );
					x.removeAll( common );
					HashSet<Object> y = new HashSet<Object>( bKeys );
					y.removeAll( common );
					HashMap<Object, Object> aMap = new HashMap<Object, Object>();
					HashMap<Object, Object> bMap = new HashMap<Object, Object>();
					HashMap<Object, Object> commonMap = new HashMap<Object, Object>();
					for (Object k: x)
					{
						aMap.put( k, aAttrs.get( k ) );
					}
					for (Object k: y)
					{
						bMap.put( k, bAttrs.get( k ) );
					}
					for (Object k: common)
					{
						commonMap.put( k, aAttrs.get( k ) );
					}
					List<Object> aContents = a.getContents();
					if ( aContents.get( aContents.size() - 1)  instanceof  EdStyleSpan )
					{
						// The last item in @a is a span
						// We have factored out the attributes common to @a and @b
						// We should attempt to join @b to the end of @a
						EdStyleSpan s = new EdStyleSpan( commonMap );
						if ( !isIdentity( a.getContents(), aMap ) )
						{
							s.getContents().add( new EdStyleSpan( a.getContents(), aMap ) );
						}
						if ( !isIdentity( b.getContents(), bMap ) )
						{
							appendSpan( s.getContents(), new EdStyleSpan( b.getContents(), bMap ) );
						}
						xs.set( xs.size() - 1, s );
					}
					else
					{
						// Replace the last element with a span that contains it, along with b
						EdStyleSpan s = new EdStyleSpan( commonMap );
						if ( !isIdentity( a.getContents(), aMap ) )
						{
							s.getContents().add( new EdStyleSpan( a.getContents(), aMap ) );
						}
						if ( !isIdentity( b.getContents(), bMap ) )
						{
							s.getContents().add( new EdStyleSpan( b.getContents(), bMap ) );
						}
						xs.set( xs.size() - 1, s );
					}
				}
			}
		}
	}
	
	
	// Merges spans together, according to style attributes:
	// Adjacent spans with the same style are joined
	// Adjacent spans where the style attributes of one are a superset of the attributes of the other are joined such that the
	// span with the superset of attributes is nested within the other
	public static ArrayList<Object> mergeSpans(List<Object> xs)
	{
		ArrayList<Object> s = new ArrayList<Object>();
		
		for (Object x: xs)
		{
			if ( x instanceof EdInlineEmbed )
			{
				s.add( x );
			}
			else if ( x instanceof EdStyleSpan )
			{
				appendSpan( s, (EdStyleSpan)x );
			}
			else
			{
				throw new RuntimeException( "mergeSpans() can only handle EdInlineEmbeds and EdStyleSpan items" );
			}
		}
		
		return s;
	}
	
	
	private static class PMerger
	{
		private ArrayList<Object> paragraphs = new ArrayList<Object>();
		private ArrayList<Object> spans = new ArrayList<Object>();
		private Map<Object, Object> attrs = null;
		private boolean bHasContent = false;
		
		
		public PMerger()
		{
		}
		
		
		public ArrayList<Object> paragraphs()
		{
			finish();
			return paragraphs;
		}
		
		public void add(Object p)
		{
			if ( p instanceof EdParagraph  ||  p instanceof EdParagraphEmbed )
			{
				merge();
				paragraphs.add( p );
				attrs = null;
				bHasContent = false;
			}
			else if ( p instanceof TagPStart )
			{
				merge();
				attrs = ((TagPStart)p).getAttrs();
				bHasContent = true;
			}
			else
			{
				spans.add( p );
				bHasContent = true;
			}
		}
		
		private void finish()
		{
			merge();
		}
		
		private void merge()
		{
			if ( bHasContent )
			{
				paragraphs.add( new EdParagraph( null, mergeSpans( spans ), attrs ) );
			}
			spans.clear();
		}
	}
	
	
	// Merges paragraphs
	// Leaves paragraphs in structural form (appear as EdParagraph or EdParagraphEmbed) as is
	// Starts a new paragraph when a paragraph start tag (TagPStart) is encountered
	// Joins any spans within paragraphs using mergeSpans()
	public static ArrayList<Object> mergeParagraphs(List<Object> xs)
	{
		PMerger m = new PMerger();
		for (Object p: xs)
		{
			m.add( p );
		}
		return m.paragraphs();
	}
	
}
