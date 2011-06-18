//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeVisitor;

import java.util.HashMap;
import java.util.WeakHashMap;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocModel.DMNodeClass;
import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalValueMonitor;

class AttributeEvaluationTable
{
	WeakHashMap<DMNode, AVResult> nodeToResult;
	
	// If an evaluation function cannot be found for a specific node class, we can use one for a
	// superclass if it is available. At initialisation, evaluation functions are provided for specific
	// classes only; this will not extend to cover subclasses.
	// 
	// Upon receiving a query for an evaluation function for a node class, we could test all
	// super-classes, up to the root, but this could be slow if this is done every time, so instead,
	// we should cache the results.
	//
	// In order to implement this, the table mapping node class to evaluation function maps
	// node class to a single element array of evaluation functions.
	// This way, there are can be three results for the query:
	// 1. table contains entry, array contains non-null evalFn    :    got an evalFn for this node class
	// 2. table contains entry, array contains null evalFn   :   there is no evalFn for this node class
	// 3. table does not contain entry   :   we have not queried for this node class before, we should test superclasses, and cache result
	HashMap<DMNodeClass, AttributeEvaluationFunction[]> nodeClassToEvalFn;
	
	
	public AttributeEvaluationTable()
	{
		nodeToResult = new WeakHashMap<DMNode, AVResult>();
		nodeClassToEvalFn = new HashMap<DMNodeClass, AttributeEvaluationFunction[]>();
	}
	
	protected void registerEvaluationFunction(DMNodeClass nodeClass, AttributeEvaluationFunction evalFn)
	{
		  nodeClassToEvalFn.put( nodeClass, new AttributeEvaluationFunction[] { evalFn } );
	}
	
	protected AVResult getAttributeResult(IncrementalAttributeVisitorEvaluator evaluator, AttributeBase attribute, DMNode node)
	{
		AVResult result = lookupAttributeResult( evaluator, attribute, node );
		
		if ( result != null )
		{
			return result;
		}
		else
		{
			return attribute.getFallbackResult( evaluator, node, this );
		}
	}
	
	protected AVResult lookupAttributeResult(IncrementalAttributeVisitorEvaluator evaluator, AttributeBase attribute, DMNode node)
	{
		AVResult result = nodeToResult.get( node );
		
		if ( result == null )
		{
			IncrementalFunctionMonitor currentComputation = IncrementalValueMonitor.blockAccessTracking();
			DMNodeClass nodeClass = node.getDMNodeClass();
			IncrementalValueMonitor.unblockAccessTracking( currentComputation );
			AttributeEvaluationFunction evalFn = getEvalFn( nodeClass );
			if ( evalFn != null )
			{
				result = createAttributeResult( evaluator, attribute, node, evalFn );
				nodeToResult.put( node, result );
			}
			else
			{
				result = null;
			}
		}

		return result;
	}
	
	private AttributeEvaluationFunction getEvalFn(DMNodeClass nodeClass)
	{
		AttributeEvaluationFunction evalFnArray[] = nodeClassToEvalFn.get( nodeClass );
		
		if ( evalFnArray == null )
		{
			// No entry found; get a result and cache it
			
			DMNodeClass superClass = nodeClass.getSuperclass();
			while ( superClass != null )
			{
				// Query for @superClass
				AttributeEvaluationFunction superClassEntry[] = nodeClassToEvalFn.get( superClass );
				
				if ( superClassEntry != null )
				{
					// We have an entry for @c; this is our result; cache it, and return it
					evalFnArray = superClassEntry;
					nodeClassToEvalFn.put( nodeClass, superClassEntry );
					return superClassEntry[0];
				}
				
				superClass = superClass.getSuperclass();
			}
			
			// We did not find an entry for any superclass - create a cache a null-entry
			evalFnArray = new AttributeEvaluationFunction[] { null };
			nodeClassToEvalFn.put( nodeClass, evalFnArray );
			
			return null;
		}
		else
		{
			return evalFnArray[0];
		}
	}
	
	protected AVResult createAttributeResult(IncrementalAttributeVisitorEvaluator evaluator, AttributeBase attribute, DMNode node, AttributeEvaluationFunction evalFn)
	{
		AVResult result = new AVResult( node, this );
		AVResult prevResult = evaluator.pushAVResult( result );
		
		Object refreshState = result.incr.onRefreshBegin();
		// Ensure that the result is dependent upon the node class
		node.getDMNodeClass();
		result.value = evalFn.evaluateAttribute( node );
		result.incr.onRefreshEnd( refreshState );
		
		evaluator.popAVResult( prevResult );
		
		return result;
	}

	
	protected void onResultInvalidated(DMNode node)
	{
		nodeToResult.remove( node );
	}
}
