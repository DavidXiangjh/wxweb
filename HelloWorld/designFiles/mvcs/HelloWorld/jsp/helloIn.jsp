<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript">
function submit(){
	document.getElementById('form1').submit();
}
</script>
</head>
<body>
登录成功！
<form action="HelloWorldSubmit.do" name="form1">
用户名：<input id="userId" name="userId" type="text"/>
<input type="submit" value="确定" onclick=" submit();"/>
</form>
</body>
</html>