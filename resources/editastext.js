function EditAsTextClass(ctrlKey, shiftKey, altKey, ctrlMask, shiftMask, altMask)
{
	this.ctrlKey = ctrlKey;
	this.shiftKey = shiftKey;
	this.altKey = altKey;
	this.ctrlMask = ctrlMask;
	this.shiftMask = shiftMask;
	this.altMask = altMask;
}


EditAsTextClass.prototype.startEditing = function(element, event, text)
{
    var editor = this;
    
    var editorID = element.attr( "id" );
    var inputID = editorID + "_input";

    function inputBlur (event)
    {
        editor.commit( inputElement, editorID, this.value, false );
    }
    
    function inputKeyDown (event)
    {
        switch ( event.keyCode )
        {
        case 13:
            editor.commit( inputElement, editorID, this.value, true );
        default:
            break;
        }
    }
    
    element.replaceWith( "<input type=\"text\" id=\"" + inputID + "\" value=\"" + text + "\"></input>" );
    var inputElement = $( "#" + inputID );
    inputElement.blur( inputBlur );
    inputElement.keydown( inputKeyDown );
    inputElement.focus();
}

EditAsTextClass.prototype.commit = function(inputElement, editorID, text, bUserEvent)
{
    inputElement.replaceWith( "..." );
	sendObject( new EditAsTextCommit( editorID, text, bUserEvent ) );
}

EditAsTextClass.prototype.onClick = function(element, event, text)
{
    var ctrlKey = event.ctrlKey == true;
    var shiftKey = event.shiftKey == true;
    var altKey = event.altKey == true;
	if ( ( ctrlKey == this.ctrlKey  ||  !this.ctrlMask )   &&   ( shiftKey == this.shiftKey  ||  !this.shiftMask )   &&   ( altKey == this.altKey  ||  !this.altMask ) )
	{
        this.startEditing( element, event, text );
	}
}



EditAsTextClass.prototype.applyTo = function(element, text)
{
    var editor = this;
	element.click(
		function (event)
		{
			editor.onClick( element, event, text );
            return false;
		}
	);
}



