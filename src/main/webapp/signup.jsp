<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <title>Signup</title>
</head>
<body>
<style>
    .center-box {
        width: 400px;
        margin: 100px auto;
    }
</style><div class="center-box">
<h2>Signup</h2>

<%
    String error = (String) request.getAttribute("error");
    if (error != null) {
%>
        <p style="color:red;"><%= error %></p>
<%
    }
%>

<form action="<%= request.getContextPath() %>/register" method="post">
    Name:
    <input type="text" name="name" required><br><br>

    Email:
    <input type="email" name="email" required><br><br>

    Password:
    <input type="password" name="password" required><br><br>

    <button type="submit">Signup</button>

</form>

<a href="<%= request.getContextPath() %>/login.jsp">Already have account?</a>
</div>
</body>
</html>