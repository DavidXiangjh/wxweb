


EMP.widget.WorkBench  = function(dataSrc){
		this.init(dataSrc);
};


EMP.widget.WorkBench.prototype = {

	tabView:null,
		
	init:function(dataSrc){
	
		try{
			this.tabView = new YAHOO.widget.TabView( dataSrc ); 
			
		    var div = document.getElementById( dataSrc.id );
		    if( div != null )
		    	this.tabView.appendTo( div );
		    	
		    EMP.widget.WorkBench.instance = this;
		}catch(e)
		{
			alert(e);
		}

	},
	
	
	appendTo:function( div ){
	
		this.tabView.appendTo( div );
	},
		
	appendCloseTag:function( tab )
	{
		try{		
			var labelEl = tab.get("labelEl");
			if( labelEl == null )
				return;
				
			var pEl = labelEl; //.parentNode.parentNode;
			var closeEl = document.createElement("a");
			closeEl.title='close';
			closeEl.href="#";
			var txtNode = document.createTextNode("X");
			closeEl.appendChild( txtNode );

			var txtNode1 = document.createTextNode("  ");

			pEl.appendChild( txtNode1 );
			
			pEl.appendChild( closeEl );

			var eventParam = {tabObj:tab, benchObj:this };	
			YAHOO.util.Event.addListener( closeEl, "click", this.closeClick, eventParam );
		}catch(e)
		{
			alert( e);
		}
	
	},
	
	addTask:function( id, title, href )
	{
		var tab = new YAHOO.widget.Tab({id:id,label:title, dataSrc:href, cacheData:true});
		this.tabView.addTab( tab, 0 );
		
		var contentChangeHandler = function(e, tab ) {
			var div = tab.get("contentEl");
			updateScript(div, e.newValue );
			return true;
			};
		
		tab.addListener('contentChange', contentChangeHandler, tab );
	
		this.tabView.set("activeTab", tab );	
		
		this.appendCloseTag( tab );
	},
	
	
	removeActiveTab:function()
	{
		this.tabView.removeTab( this.tabView.get("activeTab"));
	},
	
	removeTab:function( tab )
	{
		this.tabView.removeTab( tab );
	},
	
	getCurrentDiv:function()
	{
		var tabIdx = this.tabView.get("activeIndex");
		if( tabIdx == -1 )
		{
			var tab = new YAHOO.widget.Tab({id:"EMPTab",label:"???"});
			this.tabView.addTab( tab, 0 );
			return tab.get("contentEl");
		}
		
		var tab = this.tabView.getTab( tabIdx );
		
		if( tab == null )
		{
			tab = new YAHOO.widget.Tab({id:"EMPTab",label:"EMPWorkbench"});
			this.tabView.addTab( tab, 0 );
			this.tabView.set("activeTab", tab );	
	
			this.appendCloseTag( tab );
		
			return tab.get("contentEl");
			
		}
		else
			return tab.get("contentEl");		
		
	},
	
	closeClick:function(e, eventParam)
	{
		var bench = eventParam.benchObj;
		var tabObj = eventParam.tabObj;
		bench.removeTab(tabObj);
	}
	
};

/**
  全局的 Workbench实例，在connection中updateWorkingArea会用到
*/
EMP.widget.WorkBench.instance = null;



	