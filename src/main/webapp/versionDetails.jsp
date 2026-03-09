<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.documentapp.model.FileVersion" %>
<%@ page import="com.documentapp.model.Files" %>

<%
    FileVersion version = (FileVersion) request.getAttribute("version");
    Files file = (Files) request.getAttribute("file");

    // Redirect back if version or file not available
    if (version == null || file == null) {
        response.sendRedirect("files");
        return;
    }
%>

<html>
<head>
    <title>Version Details</title>

    <style>
        body { font-family: Arial; }
        .center-box {
            width: 700px;
            margin: 30px auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            border: 1px solid #ccc;
            padding: 8px;
            text-align: left;
        }

        th {
            width: 35%;
            font-weight: bold;
        }

        .actions a {
            margin-right: 10px;
            text-decoration: none;
            color: #007BFF;
        }

        .actions a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

<div class="center-box">

<h2>Version Details</h2>

<div class="actions">
    <a href="view?versionId=<%= version.getId() %>">View File</a>
    <a href="download?versionId=<%= version.getId() %>">Download</a>
    <a href="versions?fileId=<%= file.getFileId() %>">Back to Versions</a>
</div>

<br>

<table>
    <tr>
        <th>File Name</th>
        <td><%= file.getFileName() %></td>
    </tr>

    <tr>
        <th>Version</th>
        <td>v<%= version.getVersionNumber() %></td>
    </tr>

    <tr>
        <th>File Type</th>
        <td><%= version.getFileType() != null ? version.getFileType() : "-" %></td>
    </tr>

    <tr>
        <th>File Size (bytes)</th>
        <td><%= version.getFileSize() %></td>
    </tr>

    <tr>
        <th>Uploaded Time</th>
        <td><%= version.getUploadedTime() != null ? version.getUploadedTime() : "-" %></td>
    </tr>

    <tr>
        <th>Notes</th>
        <td><%= (version.getNotes() != null && !version.getNotes().isEmpty()) ? version.getNotes() : "No notes" %></td>
    </tr>

    <tr>
        <th>Stored File Path</th>
        <td><%= version.getFilePath() %></td>
    </tr>
</table>

</div>

</body>
</html>