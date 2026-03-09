<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.documentapp.util.AuthUtil" %>
<%@ page import="com.documentapp.dao.UserDAO" %>
<%@ page import="com.documentapp.model.User" %>
<%@ page import="com.documentapp.util.DBConnection" %>
<%@ page import="java.sql.Connection" %>

<%
    Long userId = AuthUtil.getUserIdFromCookie(request);

    if (userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    User user = null;
    Connection con = null;

    try {
        con = DBConnection.getConnection();
        UserDAO userDAO = new UserDAO();
        user = userDAO.getUserById(con, userId); // pass connection to match DAO

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

    } catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect("login.jsp");
        return;
    } finally {
        DBConnection.close(con);
    }

    String success = (String) request.getAttribute("success");
    String error   = (String) request.getAttribute("error");

    String fileIdParam = request.getParameter("fileId");

    boolean isNewVersion = (fileIdParam != null && !fileIdParam.isEmpty());
%>

<html>
<head>
    <title>Upload</title>
</head>

<body>

<style>
    .center-box {
        width: 450px;
        margin: 100px auto;
    }
</style>

<div class="center-box">

<h2>Welcome, <%= user.getName() %></h2>
<hr>

<% if (success != null) { %>
    <p style="color:green;"><%= success %></p>
<% } %>

<% if (error != null) { %>
    <p style="color:red;"><%= error %></p>
<% } %>

<% if (isNewVersion) { %>
    <h3>Upload New Version</h3>
<% } else { %>
    <h3>Upload New File</h3>
<% } %>

<form action="upload" method="post" enctype="multipart/form-data">

    <%-- Only for new version --%>
    <% if (isNewVersion) { %>
        <input type="hidden" name="fileId" value="<%= fileIdParam %>">
    <% } %>

    Select File :
    <input type="file" name="file" required>
    <br><br>

    <%-- Notes only when uploading a new version --%>
    <% if (isNewVersion) { %>
        Notes :
        <br>
        <textarea name="notes" rows="4" cols="40"></textarea>
        <br><br>
    <% } %>

    <button type="submit">
        <% if (isNewVersion) { %>
            Upload Version
        <% } else { %>
            Upload File
        <% } %>
    </button>

</form>

<hr>

<a href="files">View My Files</a>
<br><br>
<a href="logout">Logout</a>

</div>

</body>
</html>