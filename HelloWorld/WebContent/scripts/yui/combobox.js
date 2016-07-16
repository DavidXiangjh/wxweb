
EMP.widget.ComboboxGroup  = function(dataSrc){
		this.init(dataSrc);
};


EMP.widget.ComboboxGroup.prototype = {

	/*
		the url used for combobox src request url
	*/
	dataSrc:null,
	
	/**
		is't need to update sub sequence combobox's value
	*/
	updateSubSeq:false,
	
	comboxs:null,
	
	init:function(dataSrc){
		this.comboxs = [];
		this.dataSrc = dataSrc;
		var grps = EMP.widget.ComboboxGroup.groups;
		grps[grps.length] = this;
	},

	
	doInitComboboxContent:function( appendParameter, combobox ){
			var callback = {
			  success:this.successComboHandler,
			  failure:this.failureComboHandler,
			  argument: { combox:combobox, comboGrp:this }
			}
					
			try{
				var url = this.dataSrc;
				if( appendParameter != null )
				{
					var idx = url.indexOf('?');
					if( idx != -1 )
						url = url + '&' + appendParameter;
					else
						url = url + '?' + appendParameter;
				}
				
				var obj1 = YAHOO.util.Connect.asyncRequest('GET', url, callback);
			}catch(e)
			{
				alert('failed to init:' + e );
			}
	},


	successComboHandler:function(o){

		//alert('combobox src:' + o.responseText );

		if(o.responseText !== undefined)
		{
			var combobox = o.argument.combox;
			var comboGrp = o.argument.comboGrp;
			comboGrp.updateComboBoxWithStr( combobox, o.responseText );
		}
	},

	

	failureComboHandler:function(o)
	{

		alert( 'failed:' + o.status +  + o.statusText);	
	},
		
	comboboxChangeHandler:function( event ){
	
		var elTarget = YAHOO.util.Event.getTarget(event );     
		var grp = EMP.widget.ComboboxGroup.findGroupFor( elTarget );
		if( grp != null )
			grp.updateSubsequentCombobox( elTarget );
	},
	
	registCombobox:function( combobox ){
		
		//alert( 'regist new combobox: ' + combobox.id);
		if( combobox == null )
		{
			return;
		}
		try{
			this.comboxs[this.comboxs.length] = combobox;
			
			YAHOO.util.Event.addListener(combobox, "change", this.comboboxChangeHandler); 			
		}catch(e)
		{
			alert('failed to reg combo:' + e);
		}
	},
	
	
	updateSubsequentCombobox:function( curComboBox )
	{
		var param = '';
		
		var i=0;
		for( i=0; i<this.comboxs.length; i++)
		{
			var aBox = this.comboxs[i];
			if( param == '')
			{
				param = aBox.name + '=' + aBox.value;
			
			}
			else
			{
				param = param + '&' + aBox.name + '=' + aBox.value;
			}

			if( aBox == curComboBox )
				break;
		}
		
	
		if( (i+1) == this.comboxs.length )
			return;
			
		aBox = this.comboxs[i+1];
		this.doInitComboboxContent(param, aBox );
	},
		

	updateComboBoxWithStr:function(comboBox, src)
	{
		if( src == null )
			return;

		var items = src.split(';');
		if( items.length == 0)
			return;

		if( comboBox == null )
		{
			return false;
		}
		
		comboBox.length = items.length - 1;
			
	   for(var i=0;i<items.length-1; i++)
	   {
		   	var str = items[i];
			var idx = str.indexOf(':');
			if( str == "")
				break;
			comboBox.options[i].value=str.substr(0, idx);
        	comboBox.options[i].text= str.substr(idx+1);
	    }
	    
	    this.updateSubsequentCombobox( comboBox );
	},
	
	isComboxBelongTo:function(combox){
		for( var i=0; i<this.comboxs.length; i++)
		{
			if( this.comboxs[i] == combox )
				return true;
		}
		return false;
	}
};


EMP.widget.ComboboxGroup.groups = [];

EMP.widget.ComboboxGroup.findGroupFor=function(combox){
	var grps = EMP.widget.ComboboxGroup.groups;
	for( var i=0; i< grps.length; i++)
	{
		if( grps[i].isComboxBelongTo( combox ) )
			return grps[i];
	}
	return null;
}

