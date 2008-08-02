function EditAsTextClass(ctrlKey, shiftKey, altKey, ctrlMask, shiftMask, altMask)
{
	this.ctrlKey = ctrlKey;
	this.shiftKey = shiftKey;
	this.altKey = altKey;
	this.ctrlMask = ctrlMask;
	this.shiftMask = shiftMask;
	this.altMask = altMask;
}


EditAsTextClass.prototype.startEditing = function(element, event)
{
    var editor = this;
    
    var editorID = element.id;

    function inputBlur = function(event)
    {
        editor.commit( element, editorID, this.value );
    }
    
    function inputKeyDown = function(event)
    {
        switch ( event.keyCode )
        {
        case 13:
            editor.commit( element, editorID, this.value );
        default:
            break;
        }
    }
    
    //var inputID = editorID + "_input";
    
    //element.html( "<input type=\"text\" id=\"" + inputID + "\">" );
    element.html( "<input type=\"text\" id=\">" );
    //var inputElement = $( "#" + inputID );
    var inputElement = element.children();
    inputElement.blur( inputBlur );
    inputElement.keydown( inputKeyDown );
}

EditAsTextClass.prototype.commit = function(element, editorID, text)
{
    element.html( "..." );
	sendObject( new TextEditCommit( editorID, text ) );
}

EditAsTextClass.prototype.onClick = function(element, event)
{
    var ctrlKey = event.ctrlKey == true;
    var shiftKey = event.shiftKey == true;
    var altKey = event.altKey == true;
	if ( ( ctrlKey == this.ctrlKey  ||  !this.ctrlMask )   &&   ( shiftKey == this.shiftKey  ||  !this.shiftMask )   &&   ( altKey == this.altKey  ||  !this.altMask ) )
	{
        this.startEditing( element, event );
	}
}



EditAsTextClass.prototype.applyTo = function(element)
{
    var editor = this;
	element.click(
		function (event)
		{
			editor.onClick( element, event );
		}
	);
}



