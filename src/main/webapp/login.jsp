<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <title>Login</title>
</head>
<body>
<style>
    .center-box {
        width: 350px;
        margin: 120px auto;
    }
</style>
<div class="center-box">
<h2>Login</h2>

<%
    String error = (String) request.getAttribute("error");
    if (error != null) {
%>
        <p style="color:red;"><%= error %></p>
<%
    }
%>

<form action="<%= request.getContextPath() %>/login" method="post">

    Email:
    <input type="email" name="email" required><br><br>

    Password:
    <input type="password" name="password" required><br><br>

    <button type="submit">Login</button>

</form>

<a href="<%= request.getContextPath() %>/signup.jsp">Create account</a>
</div>
</body>
</html>