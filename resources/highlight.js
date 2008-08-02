_highlightClasses = new Array();


function HighlightClass(normalStyle, highlightedStyle, ctrlKey, shiftKey, altKey, ctrlMask, shiftMask, altMask)
{
	this.normalStyle = normalStyle;
	this.highlightedStyle = highlightedStyle;
	this.ctrlKey = ctrlKey;
	this.shiftKey = shiftKey;
	this.altKey = altKey;
	this.ctrlMask = ctrlMask;
	this.shiftMask = shiftMask;
	this.altMask = altMask;
	this.bOn = false;
	this.stack = new Array();
	
	_highlightClasses.push( this );
}


HighlightClass.prototype.getHighlightedElement = function()
{
	if ( this.bOn )
	{
		if ( this.stack.length > 0 )
		{
			return this.stack[ this.stack.length-1 ];
		}
	}
	
	return undefined
}

HighlightClass.prototype.handleKeyState = function(event)
{
    var ctrlKey = event.ctrlKey == true;
    var shiftKey = event.shiftKey == true;
    var altKey = event.altKey == true;
	if ( ( ctrlKey == this.ctrlKey  ||  !this.ctrlMask )   &&   ( shiftKey == this.shiftKey  ||  !this.shiftMask )   &&   ( altKey == this.altKey  ||  !this.altMask ) )
	{
		this.bOn = true;
	}
	else
	{
		this.bOn = false;
	}
}

HighlightClass.prototype.handleHighlightChange = function(prev, cur)
{
	if ( prev != cur )
	{
		if ( prev != undefined )
		{
			if ( this.highlightedStyle != "" )
			{
				prev.removeClass( this.highlightedStyle );
			}
			if ( this.normalStyle != "" )
			{
				prev.addClass( this.normalStyle );
			}
		}

		if ( cur != undefined )
		{
			if ( this.normalStyle != "" )
			{
				cur.removeClass( this.normalStyle );
			}
			if ( this.highlightedStyle != "" )
			{
				cur.addClass( this.highlightedStyle );
			}
		}
	}
}

HighlightClass.prototype.onEnter = function(element, event)
{
	prev = this.getHighlightedElement();
	this.stack.push( element );
	this.handleKeyState( event );
	cur = this.getHighlightedElement();
	this.handleHighlightChange( prev, cur );
}

HighlightClass.prototype.onLeave = function(element, event)
{
	prev = this.getHighlightedElement();
	index = this.stack.lastIndexOf( element );
	if ( index != -1 )
	{
		this.stack.splice( index, 1 );
	}
	this.handleKeyState( event );
	cur = this.getHighlightedElement();
	this.handleHighlightChange( prev, cur );
}

HighlightClass.prototype.onKey = function(event)
{
	switch ( event.keyCode )
	{
	case 16:
	case 17:
	case 18:
		prev = this.getHighlightedElement();
		this.handleKeyState( event );
		cur = this.getHighlightedElement();
		this.handleHighlightChange( prev, cur );
		break;
	default:
		break;
	}
}


HighlightClass.prototype.applyTo = function(element)
{
    var highlighter = this;
	element.hover(
		function (event)
		{
			highlighter.onEnter( element, event );
		},
		function (event)
		{
			highlighter.onLeave( element, event );
		}
	);
}



$(document).ready( function()
	{
		$(document).keydown(
			function(event)
			{
				for (i in _highlightClasses)
				{
					_highlightClasses[i].onKey( event );
				}
			}
		);
		$(document).keyup(
			function(event)
			{
				for (i in _highlightClasses)
				{
					_highlightClasses[i].onKey( event );
				}
			}
		);
	}
);
