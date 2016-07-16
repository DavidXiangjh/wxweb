/*
	Copyright (c) 2007, yuchengtech! Inc. All rights reserved.
	EMP connection js, to use Yahoo UI connection
	version: 1.0
*/


/**
  根据当前工作区定义，用href URL的内容更新工作区
*/

	function updateWorkingArea( href )
	{
		var divObj = getWorkingAreaDiv();

		var sUrl = href;
		var callback = {
		  success:handleSuccess,
		  failure:handleFailure,
		  argument: { divObj:divObj }
		}
		
		setDivWaiting( divObj );
		
		var obj1 = YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
	
	}
	
	
	
	/**
	   提交form的内容，并将返回结果更新到当前工作区
	*/
	function submitFormToWorkingArea( form ) 
	{
		 	var url = form.action;
		 	var postData;
		 	
			var divObj = getWorkingAreaDiv();
		 	
		 	if( form.enctype == "multipart/form-data")
			 	postData = YAHOO.util.Connect.setForm(form, true);
		 	else
			 	postData = YAHOO.util.Connect.setForm(form);

			//alert('submit form[' + form.name + '] with:\r\n' + postData );
			
			var callback = {
			  success:handleSuccess,
			  failure:handleFailure,
			  upload:handleUpload,
			  argument: { divObj:divObj }
			}

			setDivWaiting( divObj );
			
//			var divObj = document.getElementById( div );
//			divObj.innerHTML = 'on loading ...';
			var obj1 = YAHOO.util.Connect.asyncRequest('POST', url, callback, postData);
			
			return false;
	}



 /**
   获取当前工作区的DIV对象
 */
	function getWorkingAreaDiv()
	{
		var divObj = null;
		
		//检查是否使用EMP Workbench		
		try{
			if( EMP.widget.WorkBench.instance != null )
				divObj = EMP.widget.WorkBench.instance.getCurrentDiv();
		}catch(e)
		{
			//alert( e );
		}
		
		if( divObj != null )
			return divObj;
		
		try{				
			if( EMP.widget.EMPMenu.instance != null )
			{
				var divId = EMP.widget.EMPMenu.instance.getContentDivID();
			//	alert( divId );
				divObj = document.getElementById( divId );
			}
		}catch(e)
		{
		}
				
		return divObj;	
	}
	

	/**
		查找指定的Div，首先从WorkingArea中查找，如果没有，则从Document中查找
	*/
	function getDiv( divId )
	{
		var workAreaDiv = getWorkingAreaDiv();
		if( workAreaDiv != null )
		{
			var divs = workAreaDiv.getElementsByTagName( 'div' );
			for( var i=0; i<divs.length; i++)
			{
				if( divs[i].id != null && divs[i].id == divId )
					return divs[i];
			} 
		}
	
		return document.getElementById(divId );
		
	}
	

  /**
	* 使用指定的HREF URL 更新指定DIV的内容，
	* 参数为：sUrl, divId, '', sUrl, divId, '', ....三个一组
	*/

	function updateDivContent()
	{
	
		var len = arguments.length;
		for( var i = 0; i<len; i+=3)
		{
			if( i+3 > len )
				break;
		
			var sUrl = arguments[i];
			var divIdValue = arguments[i+1];
			var upate_method=arguments[i+2];
			
			var divObj = getDiv(divIdValue );
			if( divObj == null )
			{
				alert( 'the div named[' + divIdValue + '] not found!');
				continue;
			}
			
		
			setDivWaiting( divObj );
			
			var callback = {
			  success:handleSuccess,
			  failure:handleFailure,
			  argument: { divObj:divObj }
			}
			
			var obj1;
			if(upate_method == 'POST')
				obj1= YAHOO.util.Connect.asyncRequest('POST', sUrl, callback);
			else
				obj1 = YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
		}
	}


	/**
	  ajax http 请求的成功回调函数
	*/
	var handleSuccess = function(o){
		if(o.responseText !== undefined)
		{
			var divId = o.argument.divId;
			var div = null;
			if( divId != null )
			{
//				var workingAreaDiv = getWorkingAreaDiv();
//				div = workingAreaDiv.getElementById( divId );
				div = document.getElementById( divId );
			}
			else
				div = o.argument.divObj;
				
				
			if( div == null )
			{
				alert("Div named " + divId + "not defined in document!");
				
				return;
			}
			var va = o.responseText;
			updateScript(div, va);
		}
	}
	
	/**
	  ajax http 请求的失败回调函数
	*/

	var handleFailure = function(o){
		if(o.responseText !== undefined){

			var divId = o.argument.divId;
			var div = null;
			if( divId != null )
			{
				//var workingAreaDiv = getWorkingAreaDiv();
				//div = workingAreaDiv.getElementById( divId );
				div = document.getElementById( divId );
			}
			else
				div = o.argument.divObj;

			if( div == null )
			{
				alert("Div named " + divId + "not defined in document!");
				return;
			}

			div.innerHTML = "<li>Transaction id: " + o.tId + "</li>";
			div.innerHTML += "<li>HTTP status: " + o.status + "</li>";
			div.innerHTML += "<li>Status code message: " + o.statusText + "</li>";
		}
	}

	var handleUpload = function(o){
	
			var divId = o.argument.divId;
			var div = null;
			if( divId != null )
				div = document.getElementById( divId );
			else
				div = o.argument.divObj;
		
			if( div == null )
			{
				alert("Div named " + divId + "not defined in document!");
				return;
			}
			var va = o.responseText;
			
			updateScript(div, va);
	
	}
	

 /**
   更新Script，也就是将通过AJAX http请求返回的内容中的JS内容激活
 */
	function updateScript(div, va )
	{
		    var ua = navigator.userAgent.toLowerCase();
		    if (ua.indexOf('msie') >= 0 && ua.indexOf('opera') < 0) 
		    {

		        var htmlCode = '<div style="display:none">for IE</div>' + va;
		        htmlCode = htmlCode.replace(/<script([^>]*)>/gi, '<script$1 defer >');
		  //      htmlCode = htmlCode.replace(/<SCRIPT([^>]*)>/gi, '<SCRIPT$1 defer >');
		 //       alert( htmlCode );
   				div.innerHTML = '';
		        div.innerHTML = htmlCode;
		        div.removeChild(div.firstChild);
		    } 
		    else 
		    {
		        var el_next = div.nextSibling;
		        var el_parent = div.parentNode;
		        el_parent.removeChild(div);
		        div.innerHTML = va;
	        	if (el_next) 
	        	{
	            	el_parent.insertBefore(div, el_next)
	        	} 
	        	else 
	        	{
	            	el_parent.appendChild(div);
   				}
			}
	
	}
	

	/*
		将form的内容提交后，将响应内容更新到div指定的区域
	*/
	function submitTheForm( form, div) 
	{
		 	var url = form.action;
		 	var postData;
		 	
		 	
		 	if( form.enctype == "multipart/form-data")
			 	postData = YAHOO.util.Connect.setForm(form, true);
		 	else
			 	postData = YAHOO.util.Connect.setForm(form);


			var divObj = getDiv( div );
			if( divObj == null )
			{
				alert( 'the div named[' + divIdValue + '] not found!');
				return false;
			}

			//alert('submit form[' + form.name + '] with:\r\n' + postData );
			
			var callback = {
			  success:handleSuccess,
			  failure:handleFailure,
			  upload:handleUpload,
			  argument: { divObj:divObj }
			}
			
//			var divObj = document.getElementById( div );
//			divObj.innerHTML = 'on loading ...';

			setDivWaiting( divObj );
			
			var obj1 = YAHOO.util.Connect.asyncRequest('POST', url, callback, null);
			
			return false;
	}


	/**
	  在更新某个DIV过程中，将DIV的内容设定为提示等待。
	*/
	function setDivWaiting( divObj )
	{
		divObj.innerHTML = "<center> <br> <img src=\"images/wait.gif\" alt=\"waiting...\"/><br>Please waiting...</center>";
	}
	
