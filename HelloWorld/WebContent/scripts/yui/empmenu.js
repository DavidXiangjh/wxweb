	
	
	
	/**
	 * 初始化EMP菜单
	 * 使用YUI Connect访问该菜单的数据来源(Ajax请求)，取得返回对象
	 * @param empMenu 要初始化的菜单
	 */
	function initEMPMenu( empMenu )
	{
			var callback = {
			  success:successHd,
			  failure:failureHd,
			  argument: { empMenu:empMenu }
			}
					
			var obj1 = YAHOO.util.Connect.asyncRequest('GET', empMenu.dataSrc, callback);
	
	}
	
	/**
	 * 初始化EMP菜单Ajax请求的成功处理函数
	 * 调用EMPMenu的setMenuContent方法设置菜单内容
	 * @param o 从服务端取得的数据
	 */	
	var successHd = function(o){
		if(o.responseText !== undefined)
		{
			//alert( o.responseText);
			var empMenu = o.argument.empMenu;
			empMenu.setMenuContent( o );                   //设置菜单内容
		
		//
			var menuId = null;
			try{
				if( curState != null )
					menuId = curState.split(".")[1];              //取得当前状态对应的菜单项ID
			}catch(e){}
			
			if( menuId == null )
				menuId = empMenu.getDefaultMenuId();
				
			empMenu.showMenu( menuId );                  //菜单展示
		}
		
	}
	
	/**
	 * 初始化EMP菜单Ajax请求的失败处理函数
	 * @param o 从服务端取得的数据
	 */	
	var failureHd = function(o){
	
	/**
		if(o.responseText !== undefined){
	
			var divId = o.argument.divId;
		
			var div = document.getElementById( divId );
			
			div.innerHTML = "<li>Transaction id: " + o.tId + "</li>";
			div.innerHTML += "<li>HTTP status: " + o.status + "</li>";
			div.innerHTML += "<li>Status code message: " + o.statusText + "</li>";
		}
		*/
		
	}


/*************************************************
 *                EMPMenu   EMP菜单类                   *
 *************************************************/


/**
 * EMPMenu的构造函数
 * 调用init方法
 * @param id
 */
EMP.widget.EMPMenu = function(id){
		this.init(id);
};


EMP.widget.EMPMenu.prototype = {

	
	menuBars:null,                //该菜单包含的MenuBar(每级菜单对应一个)
	
	rootNode:null,                //EMP菜单模型的根节点(MenuNode)

	contentAreaID : null,           //工作区ID	
	contentArea : null,              //工作区DIV节点

	relativeAreaID : null,            //关联区ID
	relativeArea : null,               //关联区DIV节点
	
	helpAreaID : null,                //帮助区ID
	helpArea : null,                    //帮助区DIV节点

	advertAreaID : null,             //广告区ID
	advertArea : null,                //广告区DIV节点

	menuNodes : null,            //保存该菜单的所有节点供全局查找
		
	suportHistory : true,          //是否支持页面回退

	dataSrc : null,                    //数据来源
		
	EMPWorkBench : null,
	
	defaultMenuId : null,

	/**
	 * 初始化方法
	 * 初始化EMPMenu的各属性和成员变量
	 * @param id 菜单的ID(字符串)
	 */
	init:function( id )
	{
		this.menuBars =[];
		
		this.menuNodes = [];
		
        if ("string" !== typeof id) {
            this._el = id;
            this.id = this.generateId(id);
        }
        else
        	this.id = id;

		this.rootNode = new  EMP.widget.MenuNode({id:"root", label:"root"});          //生成根节点
		this.rootNode.empMenu = this;
		
		EMP.widget.EMPMenu.instance = this;
			
        EMP.widget.EMPMenu.menus[this.id] = this;                  //保存全局引用<id,EMPMenu>

		
	},	

	
	setDefaultMenuId:function( value )
	{
		this.defaultMenuId = value;
	},
	
	getDefaultMenuId:function( value )
	{
		return this.defaultMenuId;
	},
	
	setEMPWorkBench:function( o )
	{		
		this.EMPWorkBench = o;
	},
	
   /**
    * 设置菜单内容
    * @param o 从服务端取得的数据
    */
	setMenuContent:function( o )
	{
		var root = o.responseXML.documentElement;
		
		if( root == null )                                 //没有根节点，无效数据
		{                                                         //默认根节点标签名为TaskModel，未做判断
			alert('error task tree!');
			alert( o.responseText );
			return;
		}

		this.buildMenuNode(this.rootNode, root);
	},	
	
	/**
	 * 生成菜单节点
	 * 将数据XML节点(node)的子节点处理成MenuNode并挂在对应的菜单节点(element)下
	 * @param node 数据XML节点
	 * @param element EMP菜单模型节点
	 */
	buildMenuNode:function( node, element )
	{

		var elements = element.childNodes; // getElementsByTagName('TaskGroup');
		for( var i=0; i<elements.length; i++)
		{
			var aElement = elements[i];
			
			 var nodeName = aElement.nodeName;
			if( 'TaskGroup' == nodeName )                                        //处理菜单分组
			{
				var idStr = aElement.getAttribute("id");
				var labelStr = idStr;
								
				if( aElement.getAttribute("label") != null )
					labelStr = aElement.getAttribute("label");
	
				var dftTaskStr = null;
				if( aElement.getAttribute("dftTaskId") != null )
					dftTaskStr = aElement.getAttribute("dftTaskId");

				var hrefStr = null;
				if( aElement.getAttribute('action') != null )
					hrefStr = aElement.getAttribute('action');
				
				var targetStr = null;
				if( aElement.getAttribute('target') != null )
					targetStr = aElement.getAttribute('target');

	
				debugOut ( 'Create taskGroup: id=' + idStr + '; label=' + labelStr + '; dftTaskId=' + dftTaskStr + '; action=' + hrefStr + '; target=' + targetStr );	
				
				var obj = {id:idStr, label:labelStr, defaultTask:dftTaskStr,href:hrefStr,target:targetStr};
				var aNode = new EMP.widget.MenuNode(obj);	
				node.addChild( aNode );
				
				debugOut ( 'Parse child nodes for taskGroup [' + idStr + ']');	
				this.buildMenuNode(aNode, aElement);                         //处理菜单分组的子节点
				debugOut ( 'Parse child nodes for taskGroup [' + idStr + '] ends.');	
			}

			else if( 'TaskInfo' == nodeName )						                //处理菜单项
			{
				var taskInfoNode = this.buildTaskMenuNode(aElement);
				node.addChild( taskInfoNode );			
			}
		}
	},
	
	/**
	 * 处理菜单项
	 * 由buildMenuNode调用，将属性较多的菜单项(TaskInfo)处理单独写成一个函数
	 * @param element EMP菜单模型节点
	 * @return 生成的菜单项节点
	 */
	buildTaskMenuNode:function( element )
	{
		//处理基本属性
		var idStr = element.getAttribute('id');
		var labelStr = idStr;
		if( element.getAttribute('label') != null )
			labelStr = element.getAttribute('label');
		
		var hrefStr = null;
		if( element.getAttribute('action') != null )
			hrefStr = element.getAttribute('action');
		
		var targetStr = null;
		if( element.getAttribute('target') != null )
			targetStr = element.getAttribute('target');
				
		debugOut ( 'Create taskInfo: id=' + idStr + '; label=' + labelStr + '; action=' + hrefStr + '; target=' + targetStr  );	

		var obj = {id:idStr, label:labelStr, href:hrefStr,target:targetStr };
		var aNode = new EMP.widget.MenuNode(obj);	

		//处理关联任务
		var elements = element.getElementsByTagName('relatedTask');

		for( var i =0; i<elements.length; i++)
		{
			var relElement = elements[i];
			var relTaskId = relElement.getAttribute('taskId');
			aNode.addRelativeMenu(relTaskId);
			debugOut ( 'Add relatedTask  for taskInfo [' + idStr + ']: taskId=' + relTaskId);	
		}
		
		//处理帮助
		elements = element.getElementsByTagName('helpItem');

		for( var i =0; i<elements.length; i++)
		{
			var hlpElement = elements[i];

			var labelStr = idStr;
			if( hlpElement.getAttribute('label') != null )
				labelStr = hlpElement.getAttribute('label');	
			
			var hrefStr = null;
			if( hlpElement.getAttribute('href') != null )
				hrefStr = hlpElement.getAttribute('href');
				
			var obj = {label:labelStr, href:hrefStr};
			var hlpNode = new EMP.widget.HelpItem(obj);	

			aNode.addHelpItem( hlpNode );				
			debugOut ( 'Add helpItem for taskInfo [' + idStr + ']: label=' + labelStr + '; href=' + hrefStr);	
		}
		
		//处理广告
		elements = element.getElementsByTagName('advertItem');
		if( elements != null && elements.length > 0 )
		{
			var hrefStr = elements[0].getAttribute('href');
			var obj = {href:hrefStr};
			aNode.advertData = obj;				
			debugOut ( 'Add advertItem for taskInfo [' + idStr + ']: href=' + hrefStr);	
		}
		
		return aNode;
	},
	
		
	
	/**
	 * 重置方法
	 * 将EMPMenu恢复初始化状态
	 * @param id 菜单的ID(字符串)
	 */
	reset:function()
	{
		this.rootNode.reset();
		for (var i in this.menuNodes) 
		{
            var n = this.menuNodes[i];
            n.reset();
        }

		//menuBars没有清空？
		this.menuNodes = [];
		this.rootNode = new  EMP.widget.MenuNode({id:"root", label:"root"});
		this.rootNode.empMenu = this;
	},

	/**
	 * 读取菜单内容
	 * 调用initEMPMenu发送Ajax请求获得菜单内容
	 * @param dataSrc 数据来源URL
	 */
	loadMenuData:function(dataSrc ){
	
		if(  dataSrc != null  )
		{
			this.dataSrc = dataSrc;
			initEMPMenu(this);
		}
	
	},	

	/**
	 * 将MenuBar注册到该EMPMenu中（展现时的数据对应按照注册顺序）
	 * @param menuBar 要注册的MenuBar
	 */	
	registMenuBar:function(menuBar)
	{
		this.menuBars[this.menuBars.length] = menuBar;
	},

	/**
	 * 以下几个函数用于注册各布局区域（工作区、关联区、帮助区、广告区）
	 * @param divId 要注册的DIV的id
	 */
	registContentArea:function(areaId )
	{
		this.contentAreaID = areaId;
		this.contentArea = document.getElementById( areaId );
	},
	
	getContentAreaID:function()
	{
		return this.contentAreaID;
	},
	
	registRelativeArea:function( areaId )
	{
		this.relativeAreaID = areaId;
		this.relativeArea = document.getElementById( areaId );
	},
	
	registHelpArea:function( areaId )
	{
		this.helpAreaID = areaId;
		this.helpArea = document.getElementById( areaId );
		
	},

	registAdvertArea:function( areaId )
	{
		this.advertAreaID = areaId;
		this.advertArea = document.getElementById( areaId );
	},

	/**
	 * 往菜单中添加一个节点（供全局查找，不是挂在某个节点下）
	 */
	addMenuNode:function( node )
	{
		this.menuNodes[ this.menuNodes.length] = node;
//		this.rootNode.addChild(node);
//		node.parent = this.rootNode;
	},
	

    /**
     * 自动生成菜单ID
     * 若el的id存在则直接使用，否则顺序生成EMP_AUTO_MU_序列号形式的ID
     * @param el
     * @return id 生成的id
     */
    generateId: function(el) {
        var id = el.id;

        if (!id) {
            id = "EMP_AUTO_MU_" + EMP.widget.EMPMenu.menuCount;
            ++EMP.widget.EMPMenu.menuCount;
        }

        return id;
    },
    
    

    /**
     * 显示menuId指定的菜单节点，并存储历史记录供页面回退
     * 若该节点是taskInfo，则在工作区等DIV中载入相应页面
     * 若该节点是taskGroup，则显示子菜单节点
     * @param menuId 要显示的菜单节点id
     * @return id 生成的id 
     * call by user click
     */
	showMenu:function( menuId ) {
	
		var activeNode;
		if( menuId == null )	         //若没有指定，则显示rootNode的当前选中子节点
		{
			activeNode = this.rootNode.getActivedChild();
			if( activeNode.href == null )
			{
				var dftTask = activeNode.defaultTask;
				if( dftTask != null )
				{
					var dftNode = this.getNodeByProperty( "id", dftTask );
					if( dftNode != null && dftNode.href != null )
						dftNode.setParentActived();
				}
			}
			else
				activeNode.setParentActived();

			this.showSubMenu( this.rootNode, 0);	
		}
		else
		{

			var node = this.getNodeByProperty( "id", menuId );			
	
			activeNode = node;
			
			this.rootNode.isActive = false;

			this.rootNode.resetActive();
			node.setParentActived();

			if( node.href == null )
			{
				var dftTask = node.defaultTask;
				if( dftTask != null )
				{
					var dftNode = this.getNodeByProperty( "id", dftTask );
					if( dftNode != null && dftNode.href != null )
						dftNode.setParentActived();
				}
				
				this.showSubMenu( this.rootNode,  0 );				
			} 
			else //if( node.data.target == null )				
				this.showSubMenu( this.rootNode,  0 );
							
			
		}
			
		debugOut ('showMenu: href='+activeNode.href+'; dftTask='+activeNode.defaultTask);


			var target = activeNode.data.target;      //在布局页面以外打开

			if( target != null && target == 'parent')         //整页
			{
				if( activeNode.id == this.defaultMenuId )
					return false;
				location.href = activeNode.href;
				return false;	
			}
			else if( target != null && target == 'newwin')   //弹出新窗口
			{
				if( activeNode.id == this.defaultMenuId )
					return false;
				javascript:window.open(activeNode.href); 
				return false;
			}


		/**
			do show the content div's content
		*/
		if( this.contentArea != null )
		{
			if( activeNode.href == null )
			{
				var dftTask = activeNode.defaultTask;
				if( dftTask == null )
					return false;
				activeNode = this.getNodeByProperty( "id", dftTask );
				if( activeNode == null || activeNode.href == null )
					return false;
			}

			if( this.EMPWorkBench != null )
				this.EMPWorkBench.addTask( activeNode.id, activeNode.label, activeNode.href );
			else	
				this.updateAreaContent(activeNode.href, this.contentAreaID, "" );
		}
		
		if( this.relativeAreaID != null )
		{
			var relHtml = activeNode.getRelativeMenuHtml(this.id);
			//alert( relHtml );
			this.relativeArea.innerHTML = relHtml;	
		}
		
		if( this.helpAreaID != null )
		{
			var helpHtml = activeNode.getHelpItemHtml(this.id);
			
		//	alert( helpHtml );
			this.helpArea.innerHTML = helpHtml;
		}
			
		if( this.advertArea != null && activeNode.advertData != null  )
		{
			//alert(activeNode.advertData.href);
			this.updateAreaContent(activeNode.advertData.href, this.advertAreaID, '');
		}
			
			menuId = activeNode.id;
			
			if( this.suportHistory && menuId != null)
			{
		        try {
                	curState = this.id + '.' + menuId;
                    YAHOO.util.History.navigate( "emp", curState );
					debugOut ('showMenu save history:'+ curState);
                } catch ( e ) {
                    debugOut ('failed to store Yahoo history!' + e);
                }
			
			}
			return false;
		},
		
		/**
		 * 点击菜单后对特定页面区域的更新操作
		 */
		updateAreaContent: function( href, areaId, param ) {
			var areaObj = document.getElementById(areaId);
			if(areaObj.tagName == 'DIV'){
				updateDivContent(href, areaId, "" );
			} else if(areaObj.tagName == 'IFRAME'){
				areaObj.src = href;
			} else{
				alert("区域["+areaId+"]既不是div也不是iframe!");
			}
		},



	/**
		show menuId's menu content, call by history engine when user navigate throw back/forward
	*/
	showMenuWithoutHistory: function( menuId ) {

		var activeNode;
		if( menuId == null )	
		{
			activeNode = this.rootNode.getActivedChild();
			if( activeNode.href == null )
			{
				var dftTask = activeNode.defaultTask;
				if( dftTask != null )
				{
					var dftNode = this.getNodeByProperty( "id", dftTask );
					if( dftNode != null && dftNode.href != null )
						dftNode.setParentActived();
				}
			}
			else
				activeNode.setParentActived();

			this.showSubMenu( this.rootNode, 0);	
		}
		else
		{

			var	node = this.getNodeByProperty( "id", menuId );
			
	
			activeNode = node;
			
			this.rootNode.isActive = false;

			this.rootNode.resetActive();
			node.setParentActived();

			if( node.href == null )
			{
				var dftTask = node.defaultTask;
				if( dftTask != null )
				{
					var dftNode = this.getNodeByProperty( "id", dftTask );
					if( dftNode != null && dftNode.href != null )
						dftNode.setParentActived();
				}
			}
							
			this.showSubMenu( this.rootNode,  0 );
			
		}

		
		/**
			do show the content div's content
		*/
		if( this.contentArea != null )
		{
			if( activeNode.href == null )
			{
				var dftTask = activeNode.defaultTask;
				if( dftTask == null )
					return;
				activeNode = this.getNodeByProperty( "id", dftTask );
				if( activeNode == null || activeNode.href == null )
					return;
			}
			
			this.updateAreaContent(activeNode.href, this.contentAreaID, "" );

		}
		
		if( this.relativeAreaID != null )
		{
			var relHtml = activeNode.getRelativeMenuHtml(this.id);
			//alert( relHtml );
			this.relativeArea.innerHTML = relHtml;	
		}
		
		if( this.helpAreaID != null )
		{
			var helpHtml = activeNode.getHelpItemHtml(this.id);
			
		//	alert( helpHtml );
			this.helpArea.innerHTML = helpHtml;
		}

		if( this.advertArea != null && activeNode.advertData != null  )
		{
		//	alert( activeNode.advertData.href );
			this.updateAreaContent(activeNode.advertData.href, this.advertAreaID, '');
		}

		
	},
	
	showContentOfMenu:function( node ){

		var activeNode = node;
		debugOut ('showContentOfMenu: href='+activeNode.href+'; dftTask='+activeNode.defaultTask);
		/**
			do show the content div's content
		*/
		if( this.contentArea != null )
		{
			if( activeNode.href == null )
			{
				var dftTask = activeNode.defaultTask;
				if( dftTask == null )
					return;
				activeNode = this.getNodeByProperty( "id", dftTask );
				if( activeNode == null || activeNode.href == null )
					return;
			}
			
			if( this.EMPWorkBench != null )
				this.EMPWorkBench.addTask( activeNode.id, activeNode.label, activeNode.href );
			else	
				this.updateAreaContent(activeNode.href, this.contentAreaID, "" );
		}
		
		if( this.relativeAreaID != null )
		{
			var relHtml = activeNode.getRelativeMenuHtml(this.id);
			//alert( relHtml );
			this.relativeArea.innerHTML = relHtml;	
		}
		
		if( this.helpAreaID != null )
		{
			var helpHtml = activeNode.getHelpItemHtml(this.id);
			
		//	alert( helpHtml );
			this.helpArea.innerHTML = helpHtml;
		}

		if( this.advertArea != null && activeNode.advertData != null  )
		{
			//alert( activeNode.advertData.href );
			this.updateAreaContent(activeNode.advertData.href, this.advertAreaID, '');
		}
			
		menuId = activeNode.id;
			
		if( this.suportHistory && menuId != null && this.EMPWorkBench == null)
		{
	        try {
               	curState = this.id + '.' + menuId;
               //	alert( currentState);
					YAHOO.util.History.navigate( "emp", curState );
                  
					debugOut ('showContentOfMenu save history:'+ curState);
              } catch ( e ) {
           //        alert("failed to store Yahoo history!" + e);
              }
			
		}
		
	
	},
	
	
	showSubMenu:function( node, level ){

		var aNode = node;
		
		try {	
		for( var i= level; i<this.menuBars.length; i++)
		{

			var actNode = aNode.getActivedChild();

			var menuBar = this.menuBars[i];
			
			
			if( menuBar.getStyle() != null && 'tree' == menuBar.getStyle() )
			{   //树型菜单
				var tree = aNode.getYUITree(this.id, menuBar._el);
				if( tree != null )
					tree.draw();
			}
			else if(  menuBar.getStyle() != null && 'menuBar' == menuBar.getStyle() )
			{   //下拉菜单
				aNode.getYUIMenuBar( this.id, menuBar._el );
				debugOut ('Dropdown menu for MenuBar ['+menuBar.id+'] is: '+menuBar._el.innerHTML );
			}
			else
			{   //普通的菜单条
				var element = menuBar._el;
				var htmlText = aNode.getMenuBarHtml(this.id, menuBar.getStyle(), menuBar.getActiveClass(), menuBar.getNormalClass());
				htmlText = htmlText + '<div class=\"clear\"></div>';
				debugOut ('HTML text for MenuBar ['+menuBar.id+'] is: '+htmlText );
				element.innerHTML = htmlText;
			}
			
			if( actNode == null )
				return;
			actNode.setActive( true );
			aNode = actNode;
		}
		} catch (e) {}
	},
	
    /**
     * 按某属性的取值查找符合的节点
     * @param property 属性
     * @param value 取值
     * @return 符合查找条件的第一个节点，若没有则返回null
     */
    getNodeByProperty: function(property, value) {
    
    	var ns = this.menuNodes; 
    
        for (var i = 0; i< ns.length; i++ ) {
            var n = ns[i];
            if (n.data && value == n.data[property]) {
                return n;
            }
        }

        return null;
    },

    /**
     * 按某属性的取值查找符合的多个节点
     * @param property 属性
     * @param value 取值
     * @return 符合查找条件的多个节点，若没有则返回null
     */
    getNodesByProperty: function(property, value) {
        var values = [];
        for (var i in this.menuNodes) {
            var n = this.menuNodes[i];
            if (n.data && value == n.data[property]) {
                values.push(n);
            }
        }

        return (values.length) ? values : null;
    }
};


EMP.widget.EMPMenu.menus = [];                         //当前页面上的所有EMPMenu对象
EMP.widget.EMPMenu.menuCount = 0;                 //菜单序列号计数，供自动产生id用

EMP.widget.EMPMenu.instance = null;

/**
 * 获得指定id的EMPMenu对象
 * @param id 所要取得菜单对象的id
 * @return EMPMenu对象
 */ 
EMP.widget.EMPMenu.getEMPMenu = function( id ){
	return EMP.widget.EMPMenu.menus[id];
};




/*************************************************
 *                MenuBar   EMP菜单条类                  *
 *************************************************/

/**
 * MenuBar的构造函数
 * 调用init方法
 * @param oData 初始化参数
 */
EMP.widget.MenuBar = function(oData){
	if(oData)
		this.init(oData);
};

EMP.widget.MenuBar.prototype = {

    id: null,                       //MenuBar的id，通常为所在DIV的id

    _el: null,                     //所在DIV节点

	style:null,                   //样式

	data:null,                    //保存初始化参数

	/**
	 * MenuBar的初始化方法
	 * @param oData 初始化参数
	 */
    init: function(oData) {
       
        if (typeof oData == "string") {          //若oData是字符串则用该字符串作为id
            oData = { id: oData };
        }
        this.data = oData;

        this.id = oData.id;
		this.style = oData.style;

	//	alert( this.id );		
		this._el = document.getElementById( this.id );
		
 	},

	/**
	 * 以下是各扩展参数的get方法
	 */
	getActiveClass:function(){
		return this.data.activeClass;
	},	
	
	getNormalClass:function(){	
		return this.data.normalClass;
	},
	
	getStyle:function(){
		return this.data.style;
	}
		
};




/*************************************************
 *              MenuNode   EMP菜单节点类              *
 *************************************************/

/**
 * The default node presentation.  The first parameter should be
 * either a string that will be used as the node's label, or an object
 * that has a string propery called label.  By default, the clicking the
 * label will toggle the expanded/collapsed state of the node.  By
 * changing the href property of the instance, this behavior can be
 * changed so that the label will go to the specified href.
 * @namespace YAHOO.widget
 * @class TextNode
 * @extends YAHOO.widget.Node
 * @constructor
 * @param oData {object} a string or object containing the data that will
 * be used to render this node
 * @param oParent {YAHOO.widget.Node} this node's parent node
 * @param expanded {boolean} the initial expanded/collapsed state
 */
 
 /**
 * MenuNode的构造函数
 * 调用init方法
 * @param oData 初始化参数
 * @param oParent 父节点
 */
EMP.widget.MenuNode = function(oData, oParent) {

    if (oData) { 
        this.init(oData, oParent);
    }
};

EMP.widget.MenuNode.prototype = {
    
    label: null,                          //显示文字	
	href:null,                           //链接URL	
	id:null,                              //节点id
	data:null,                          //存放初始化参数
		
	parent:null,                       //父节点
	childs:null,                        //子节点集合	
	isActive:false,                    //是否当前选中
	defaultTask:null,                //默认菜单项
	
	relativeMenuIds:null,         //关联菜单项集合	
	helpItems:null,                  //帮助项目集合
	advertData:null,                 //广告信息
	
	tree:null,	                        //树型展现	
	treeDiv:null,                      //树型展现DIV
	treeParentDiv:null,            //treeDiv所属的DIV
	
	empMenu:null,                 //该节点所属的EMPMenu
	oMenuBar:null,                 //该节点所属的MenuBar
	menuBarParentDiv:null,    //MenuBarDiv所属的DIV
	menuBarDiv:null,              //MenuBar的DIV
	
	/**
	 * MenuNode的初始化方法
	 * @param oData 初始化参数
	 * @param oParent 父节点
	 */
    init: function(oData, oParent) { 
        
		this.parent = oParent;
       
  		this.childs = [];
		this.helpItems= [];
		this.relativeMenuIds = [];
       
        if (typeof oData == "string") {          //若oData是字符串则用该字符串作为label
            oData = { label: oData };
        }
        this.label = oData.label;
		this.data = oData;
		        
        if (oData.href) {
            this.href = oData.href;
        }

        if (oData.defaultTask) {
            this.defaultTask = oData.defaultTask;
        }

		if( oData.id )	{
			this.id = oData.id;
		}
		
		if( oParent )
			oParent.addChild(this );
			
//		EMP.widget.EMPMenu.menuNodes[EMP.widget.EMPMenu.menuNodes.length] = this;

    },





	/**
	 * getYUITree和getYUITreeNode两个函数用于生成树型菜单
	 */

	/**
		return the YUI tree object for empMenuNode, the element is the DIV used to contain
		the tree DIV
	*/
	getYUITree:function(menuId, element )
	{
		if( this.tree != null )
			return this.tree;
		
		
		var treeDiv = document.createElement( 'div' );
		treeDiv.id = this.id;
		this.treeDiv = treeDiv;
		element.appendChild( treeDiv );

		this.treeParentDiv = element;
					
		this.tree = new YAHOO.widget.TreeView( treeDiv );  	
		var root = this.tree.getRoot();	

		for( var i = 0; i<this.childs.length; i++)
		{
		//	alert( node );
			var node = this.childs[i];
			node.getYUITreeNode(menuId, root );
		}
		
		this.tree.subscribe("labelClick", function(node) 
			{
				var menuNode = node.data.menuNode;
				var menuId = node.data.menuId;
				if( menuNode.href != null )
				{
					EMP.widget.EMPMenu.getEMPMenu( menuId ).showContentOfMenu( menuNode );
//					alert( menuNode.href);
				}
			}); 
			
		return this.tree;  
			
	},


	getYUITreeNode:function(menuIdStr, treeNode ){
	
		var newNode = new YAHOO.widget.TextNode({label:this.label, menuId:menuIdStr, menuNode:this}, treeNode, false);
		for( var i = 0; i<this.childs.length; i++)
		{
			var node = this.childs[i];
			node.getYUITreeNode(menuIdStr, newNode );
		}
		  
	},




	/**
	 * getYUIMenuBar、getYUIMenu和getYUIMenuItem三个函数用于生成下拉式菜单
	 */
	 
	/**
	 * 用当前MenuNode生成下拉式菜单，格式为
	 * <div class=yuimenubar>
	 *      <div class=bd>
	 *           <ul>
	 *                <li class=yuimenubaritem first-of-type><a>label</a></li>
	 *                <li class=yuimenubaritem><a>label</a></li>
	 *                <li class=yuimenubaritem hassubmenu>
	 *                     <a class=hassubmenu>label</a>
	 *                     <em class=submenuindicator></em>
	 *                     <div class=yuimenu>
	 *                           <div class=bd>
	 *                                 <ul>
	 *                                      <li class=yuimenuitem first-of-type><a>label</a></li>  2级以下的样式均相同
	 *                                 </ul>
	 *                           </div>
	 *                     </div>
	 *                 </li>
	 *           </ul>
	 *      </div>
	 * </div>
	 * @param menuIdStr EMPMenu的id
	 * @param element 该下拉式菜单所属的DIV
	 */
	getYUIMenuBar:function( menuIdStr, element )
	{
		if( this.oMenuBar != null )         //下拉菜单已经生成，直接返回
			return;
   

		var menuBarDiv = document.createElement( 'div' );    //新建整体DIV
		menuBarDiv.className = "yuimenubar";
		element.appendChild( menuBarDiv );
		
		this.menuBarParentDiv = element;
		this.menuBarDiv = menuBarDiv;
		
		var bodyDiv = document.createElement('div' );    //新建 bd DIV
		bodyDiv.className = "bd";			
		menuBarDiv.appendChild( bodyDiv );
		
		var ulElement = document.createElement('ul' );    //新建 UL
		ulElement.className = "first-of-type";
		bodyDiv.appendChild( ulElement );
		
		for( var i = 0; i<this.childs.length; i++)
		{
			var node = this.childs[i];
			var liElement = document.createElement('li');

			liElement.innerHTML = node.label;
			ulElement.appendChild( liElement );
			liElement.className = "yuimenubaritem";
		}
							
		
		var oMenuBar = new YAHOO.widget.MenuBar(menuBarDiv, 
            		{ autosubmenudisplay:true, showdelay:250, hidedelay:750});
		
		for( var i = 0; i<this.childs.length; i++)
		{
			var node = this.childs[i];
			if( node.childs == null || node.childs.length == 0 ) {
				var onClickObj = {fn:node.onMenuItemClick, obj:node };
				oMenuBar.getItem(i).cfg.setProperty("onclick", onClickObj);
			} else {
				var menu = node.getYUIMenu(menuIdStr);
				oMenuBar.getItem(i).cfg.setProperty("submenu", menu );	
			}
		}

		oMenuBar.render( element );
		oMenuBar.show();		

		this.oMenuBar = oMenuBar;
	
	},
	
	/**
	 * 单击菜单项时的操作
	 */
	onMenuItemClick:function(p_sType, p_aArgs, p_oValue) 	
	{
		var menuNode = p_oValue;
		menuNode.empMenu.showContentOfMenu( menuNode );
	},
	
	/**
	 * 得到菜单项
	 * 若无子节点，则生成链接；否则生成子菜单
	 * @param menuIdStr EMPMenu的id
	 */
	getYUIMenuItem:function( menuIdStr )
	{
		if( this.childs == null || this.childs.length == 0 )
		{
			var onClickObj = {fn:this.onMenuItemClick, obj:this };
			
			var menuItem = new YAHOO.widget.MenuItem( this.label, {onclick:onClickObj} );
			return menuItem;
		}
		else
		{
			var yuiMenu = this.getYUIMenu(menuIdStr);
			var menuItem = new YAHOO.widget.MenuItem(this.label );
	
			menuItem.cfg.setProperty("submenu", yuiMenu);				
			return menuItem;
		}
	
	},	

	/**
	 * 得到子菜单，与getYUIMenuItem互相递归调用
	 * @param menuIdStr EMPMenu的id
	 */
	getYUIMenu:function(menuIdStr)
	{
		var menu = new YAHOO.widget.Menu( this.id );

		for( var i = 0; i<this.childs.length; i++)
		{
			var node = this.childs[i];
			var menuItem = node.getYUIMenuItem(menuIdStr);
			menu.addItem( menuItem );		
		}
		return menu;
	},

	/**
	 * MenuNode的重置方法
	 */
	reset:function(){
	
		if( this.treeParentDiv != null && this.treeDiv != null )
		{
			this.treeParentDiv.removeChild( this.treeDiv );
		}
		
		if( this.oMenuBar != null )
		{
			this.oMenuBar.destroy();
			this.oMenuBar = null;
		}
			
	/*	if( this.menuBarDiv != null && this.menuBarParentDiv != null )
		{
			this.menuBarParentDiv.removeChild( this.menuBarDiv );
		}
	*/	
	},
		
	/**
	 * 为MenuNode添加子节点
	 * @param child 子节点
	 */
	addChild:function( child )
	{
	//	alert( 'addChild to ' + this.label + '!' + this.childs.length );
		this.childs[this.childs.length] = child;
		child.parent = this;
		child.empMenu = this.empMenu;
		if( empMenu != null )
			empMenu.addMenuNode( child );             //将该子节点放在EMPMenu的节点集合中供全局调用
	},

	/**
	 * 为MenuNode添加帮助项目
	 * @param aItem 帮助项目
	 */
	addHelpItem:function( aItem )
	{
		this.helpItems[ this.helpItems.length] = aItem;
	},
	
	/**
	 * 为MenuNode添加关联菜单
	 * @param menuId 关联菜单id
	 */
	addRelativeMenu:function( menuId )
	{
		this.relativeMenuIds[ this.relativeMenuIds.length] = menuId;
	},
	
	/**
	 * 重置该MenuNode以下所有节点的选中状态（设为false）
	 */
	resetActive:function()
	{
		for( var i=0; i< this.childs.length; i++)
		{
			var node = this.childs[i];
			
			if( node.isActive )
			{
	//			alert( 'set node ' + node.id + 'to not act!');
				node.setActive( false );
				node.resetActive();
			}
		}
	},	
	
	/**
	 * 设置该MenuNode的选中状态
	 * @param value 选中状态 true/false
	 */
	setActive:function( value ){
		if( value )
		{
			if( this.treeDiv != null )
				this.treeDiv.style.display='';
			if( this.menuBarDiv != null )
				this.menuBarDiv.style.display='';			
		}
		else
		{
			if( this.isActive && this.treeDiv != null )    //isActive？
				this.treeDiv.style.display = 'none';
			if( this.menuBarDiv != null )
				this.menuBarDiv.style.display='none';
		}
		this.isActive = value;
	},
	
	/**
	 * 设置父节点为选中状态
	 */
	setParentActived:function()
	{
		this.setActive( true );

		if( this.parent != null )
		{
			//this.parent.setActive = true;
			this.parent.setParentActived();
		}
	},	
	
	/**
	 * 取得当前MenuNode的被选中子节点
	 * 若没有设置则默认返回第一个子节点
	 * @return 被选中子节点
	 */
	getActivedChild:function()
	{
		for( var i=0; i< this.childs.length; i++)
		{
			var node = this.childs[i];
			
			if( node.isActive )
			{
				return node;
			}
		}
		
		return this.getFirstChild();
	},
	
	/**
	 * 取得当前MenuNode的第一个子节点
	 * 若没有则返回null
	 * @return 第一个子节点
	 */
	getFirstChild:function()
	{
		if( this.childs == null || this.childs.length == 0 )
			return null;
		
		return this.childs[0];
	},
	
	
	/**
	 * getMenuBarHtml和getMenuHtml两个函数用于生成普通的HTML菜单条
	 */
	 
	/**
	 * 用当前MenuNode生成普通的HTML菜单条
	 * @param mId EMPMenu的id
	 * @param style 样式，horizontal(默认)/vertical
	 * @param actCls 选中时的CSS样式名
	 * @param norCls 未选中时的CSS样式名
	 */
	getMenuBarHtml:function(mId, style, actCls, norCls){
		var sb = [];

	//	if( 'vertical' == style )             //是否考虑将vertical参数取消？都通过css来配置更灵活
	//	{
	//		sb[sb.length] = '<ul>';
	//	}
		for( var i = 0; i<this.childs.length; i++)
		{
			var node = this.childs[i];
			sb[sb.length] = node.getMenuHtml(mId, style, actCls, norCls);
		}
		
	//	if( 'vertical' == style )
	//	{
	//		sb[sb.length] = '</ul>';
	//	}	
		return sb.join("");
	},


	/**
	 * 用当前MenuNode生成普通的HTML菜单项，格式如下：
	 * <div id=emp_yuimenu_menuId_nodeId class=norCls/actCls>
	 *        <a>label</a>
	 * </div>
	 * <div id=emp_yuimenu_menuId_nodeId_delim class=></div>
	 * @param mId EMPMenu的id
	 * @param style 样式，horizontal(默认)/vertical
	 * @param actCls 选中时的CSS样式名
	 * @param norCls 未选中时的CSS样式名
	 */
	getMenuHtml:function(mId, style, actCls, norCls){
        var sb = [];	
		
//		if( 'vertical' == style )
//		{
//			sb[sb.length] = '<li';
//	        if( this.isActive )
//		        sb[sb.length] = ' class=\"' + actCls + '\"';
//		    else
//		        sb[sb.length] = ' class=\"' + norCls + '\"';
//		    sb[sb.length] = '>';
//		}	
		
		//菜单项DIV的ID为 emp_yuimenu_菜单ID_菜单项ID
		sb[sb.length] = '<div id=\"emp_yuimenu_' + mId + '_' + this.id
		if( this.isActive )
	        sb[sb.length] = '\"  class=\"' + actCls + '\">';
	    else
	        sb[sb.length] = '\"  class=\"' + norCls + '\">';
	        
        sb[sb.length] = '<a href=\"#';
        sb[sb.length] = this.id;	    
        sb[sb.length] = '\" onclick=\"return EMP.widget.EMPMenu.getEMPMenu(\'' 
                                + mId + '\').showMenu(\'' + this.id + "\');\">";
        sb[sb.length] = this.label;
        sb[sb.length] = '</a>';
	    sb[sb.length] = '</div>';
	    
/*	    //菜单项DIV的ID为 emp_yuimenu_菜单ID_菜单项ID_delim for CITIC
	    sb[sb.length] = '<div id=\"emp_yuimenu_' + mId + '_' + this.id 
	                            +'_delim\" class=\"menu1_div\"></div>';*/
	    
//		if( 'vertical' == style )
//		{
//			sb[sb.length] = '</li>';
//		}
		
        return sb.join("");
	
	},

	/**
	 * 用当前MenuNode生成帮助项目的HTML，格式如下：
	 * @param mId EMPMenu的id
	 */
	getHelpItemHtml:function(mId ){
		if( this.helpItems == null || this.helpItems.length== 0 )
			return '';
        var sb = [];

		sb[sb.length] = '<ul>';
		for( var i =0; i<this.helpItems.length; i++)
		{
			var hItem = this.helpItems[i];
			
			sb[sb.length]='<li><a ';		
	        sb[sb.length] = ' onclick=\'javascript:window.open (\"';
    	    sb[sb.length] = hItem.href;
        	sb[sb.length] = '\", \"exit_snapshot\", \"height=650, width=800, top=0, left=100, toolbar=no, menubar=no, scrollbars=no,status=no ,resizable=yes\")\'>';
         	sb[sb.length] = hItem.label;
         	sb[sb.length] = '</a></li>';
		}
		sb[sb.length] = '</ul>';
		return sb.join("");
	},

	/**
	 * 用当前MenuNode生成关联项目的HTML，格式如下：
	 * @param mId EMPMenu的id
	 */
	getRelativeMenuHtml:function( mId ){
		if( this.relativeMenuIds == null || this.relativeMenuIds.length == 0 )
			return '';
							
        var sb = [];
		sb[sb.length] = '<ul>';
		for( var i =0; i<this.relativeMenuIds.length; i++)
		{
			var menuId = this.relativeMenuIds[i];
			
			sb[sb.length]='<li><a href=\"#';
			sb[sb.length] = menuId;		
	        sb[sb.length] = '\" onclick=\"return EMP.widget.EMPMenu.getEMPMenu(\'' + mId + '\').showMenu(\'' + menuId + "\');\">";
			
			var menuNode = EMP.widget.EMPMenu.getEMPMenu(mId).getNodeByProperty("id", menuId );
	        sb[sb.length] = menuNode.label;
         	sb[sb.length] = '</a></li>';
		}
		sb[sb.length] = '</ul>';
		return sb.join("");			
	},

	
	/**
	 * MenuNode类的字符串展现
	 * @return 字符串展现
	 */
    toString: function() { 
        return "MenuNode [" + this.id + "] " + this.label;
    }

};





/*************************************************
 *            HelpItem   EMP菜单帮助项目类             *
 *************************************************/
 
 
EMP.widget.HelpItem = function(oData) {

    if (oData) { 
        this.init(oData);
    }
};

EMP.widget.HelpItem.prototype = {
    
    /**
     * The text for the label.  It is assumed that the oData parameter will
     * either be a string that will be used as the label, or an object that
     * has a property called "label" that we will use.
     * @property label
     * @type string
     */
    label:null,
	href:null,
	
	init:function(oData){
		if( !oData )
			return;
		this.label = oData.label;
		this.href = oData.href;
	}	
};



/*************************************************
 *                            公用函数                              *
 *************************************************/

/**
	change user's locale settings
*/
function setLocale( url )
{
	var obj = YAHOO.util.Connect.createXhrObject( -1 );
	//first get the url to set the locale value
	var httpReq = obj.conn;
	httpReq.open("GET", url, false);
	httpReq.send(null);
	var content = httpReq.responseText;

	var state = curState;
	var tmp = state.split(".");
					
	//do the menu initialize
	EMP.widget.EMPMenu.menus[tmp[0]].reset();
	initEMPMenu( EMP.widget.EMPMenu.menus[tmp[0] ] );
	
}

/**
 * DEBUG输出
 * 在页面上寻找名为debugOut的textarea，如果有则向其中追加信息
 * @param string DEBUG信息
 */
function debugOut (string) {
	var debugOutTextarea = document.getElementById ("debugOut");
	if (debugOutTextarea) {
		debugOutTextarea.value += "\n"+string;
	}
}
	

