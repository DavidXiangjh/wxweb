
EMP.widget.SoftKeyboard  = function(dataSrc){
		this.init(dataSrc);
};


EMP.widget.SoftKeyboard.prototype = {

	isCaps:false,
	keyBoard:null,
	passwordField:null,
	nextField:null,
	header:null,
	width:null,
	height:null,
	dataObj:null,
	
		
	init:function(dataSrc){
	
		try{
			this.width = dataSrc.width;
			this.height = dataSrc.height;
			this.header = dataSrc.header;
			this.bodyDiv = dataSrc.bodyDiv;
			
			this.passwordField = document.getElementById( dataSrc.passwordField);
			this.nextField = document.getElementById(dataSrc.nextField);
			
			if( this.bodyDiv == null )
				this.bodyDiv = "empSoftKeyboard";
						
			this.keyBoard = new YAHOO.widget.Panel("keyBoard", { width:this.width, height:this.height, visible:false, draggable:true, close:true });
			if( this.header != null )
				this.keyBoard.setHeader( this.header );
			else
				this.keyBoard.setHeader("EMP Soft keyboard");
			document.getElementById(this.bodyDiv ).style.display="";
			this.keyBoard.setBody(document.getElementById(this.bodyDiv ));
			this.keyBoard.render(document.body);
		}catch(e)
		{
			alert(e);
		}

	},
	
	showKeyboard:function(){
		var th = this.passwordField;
		var ttop  = th.offsetTop;
		var thei  = th.clientHeight;
		var tleft = th.offsetLeft;
		var ttyp  = th.type;

		while (th = th.offsetParent)
		{
			ttop+=th.offsetTop; 
			tleft+=th.offsetLeft;
		}

		this.keyBoard.moveTo(ttop, tleft);
		this.keyBoard.show();
	},
	
	closeKeyboard:function()
	{
		if( this.keyBoard != null )
		this.keyBoard.hide();
		if( this.nextField != null )
			this.nextField.focus();
	},

	setKeyValue:function( idx )
	{
		var keyValue = "`1234567890-=qwertyuiop[]\asdfghjkl;'zxcvbnm,./";
		var keyValue2 = "~~!@#$%^&*()_+QWERTYUIOP{}|ASDFHJKL:\"ZXCVBNM<>?";
		var value = this.passwordField.value;
		if( value.length < this.passwordField.maxLength )
		{
			if( this.isCaps )
			{
				this.passwordField.value = value + keyValue2.charAt(idx);
			}
			else
			{
				var nValue = value + keyValue.charAt(idx);
				this.passwordField.value = value + keyValue.charAt(idx);
			}
		}
	},

	setNumKeyValue:function( idx )
	{
		var keyValue = "0123456789";
		var value = this.passwordField.value;
		if( value.length < this.passwordField.maxLength )
		{
			this.passwordField.value = value + keyValue.charAt(idx);
		}
	},


	backspace:function()
	{
		var len = this.passwordField.value.length;
		if( len > 0)
		{
			this.passwordField.value = this.passwordField.value.substr(0, len-1);
		}
	},

	setCapsLock:function(srcUrl, capSrcUrl )
	{
		this.isCaps = !this.isCaps;
		if( this.isCaps )
		{
			document.getElementById('EMPKeyBoardImg').src=capSrcUrl;
		}else
		{
			document.getElementById('EMPKeyBoardImg').src=srcUrl;
		}
	}

	
	
}


	