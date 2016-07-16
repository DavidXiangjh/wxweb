
EMP.widget.Wizzard  = function(dataSrc){
		this.init(dataSrc);
};


EMP.widget.Wizzard.prototype = {

	dataObj:null,
	
	tabView:null,
	tabs:null,
	pageDatas:null,
	curPageIdx:0,
		
	init:function(dataSrc){
		this.tabView = new YAHOO.widget.TabView(dataSrc);
		this.dataObj = dataSrc;
		this.tabs = [];
		this.pageDatas = [];

		var activeChangeHandler = function(e, wizzard) {
			
			var previous = e.prevValue;
			if( !wizzard.validateTabForm( previous ) )			
				return false;
				
			return true;	
		};

		this.tabView.addListener('beforeActiveTabChange', activeChangeHandler, this );
		
	},

	addWizzardPage:function( dataObj ){
		var tab = new YAHOO.widget.Tab( dataObj );
		this.tabs[this.tabs.length] = tab;
		this.tabView.addTab( tab );
		this.pageDatas[ this.pageDatas.length] = dataObj;

		var contentChangeHandler = function(e, tab ) {
			
			var div = tab.get("contentEl");
			updateScript(div, e.newValue );
			return true;
		};
		
		tab.addListener('contentChange', contentChangeHandler, tab );
		
	},
	
	appendTo:function( div ){
	
		this.tabView.appendTo( div );
	},
	
	/**
		submit the wizzard form
	*/
	submit:function(){
	
		var url = this.dataObj.actionUrl;
		var postData = [];

		//first do the form validate
		for( var i=0; i<this.tabs.length; i++)
		{
			if( ! this.validateTabForm( this.tabs[i] ) )
				return;
		}		
		
		
		for( var i=0; i<this.tabs.length; i++)
		{
			var div = this.tabs[i].get("contentEl");
			
			var form = div.getElementsByTagName('form')[0];
			if( form == null )
				continue;

		 	var postData;
		 	
		 	if( postData.length != 0 )
		 		postData[postData.length] = "&";
		 		
		 	if( form.enctype == "multipart/form-data")
			 	postData[postData.length] = YAHOO.util.Connect.setForm(form, true);
		 	else
			 	postData[postData.length] = YAHOO.util.Connect.setForm(form);
		}

			var obj = YAHOO.util.Connect.createXhrObject( -1 );
			//first get the url to set the locale value
			var httpReq = obj.conn;
			httpReq.open("POST", url, false);

			httpReq.setRequestHeader("Content-Type","application/x-www-form-urlencoded;  charset=UTF-8");
			
			postData[postData.length] = "&command=submit";
			
			var sendData = postData.join("");
			alert( sendData );

			httpReq.send(sendData);
			var content = httpReq.responseText;
			
			var divId = this.dataObj.id;
			var div = document.getElementById(divId).parentNode;
			alert( content );
			div.innerHTML = content;
			
	},
	

	/**
		call the form validate function 
	*/	
	validateTabForm:function( tab ){
	
		//do the current page's validator
		var div = tab.get("contentEl");
			
		var form = div.getElementsByTagName('form')[0];
		if( form != null )
		{
			var functionName = "validateForm_" + form.name + "(form)";	
			var ret = eval( functionName );
			
			return ret;		
		}
		
		return true;
	
	},


	activeNextPage:function(){
	
		var actIdx = this.tabView.get("activeIndex");
		if( (actIdx + 1) >= this.tabs.length )
			return;
		
		this.tabView.set('activeIndex', actIdx + 1 );
	
	},

	activeLastPage:function(){
		var actIdx = this.tabView.get("activeIndex");
		if( actIdx == 0 )
			return;
		
		this.tabView.set('activeIndex', actIdx - 1 );
		
	}

	
};


