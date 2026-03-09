<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.documentapp.model.FileVersion" %>
<%@ page import="com.documentapp.model.Files" %>

<html>
<head>
    <title>File Versions</title>
    <style>
        body { font-family: Arial; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ccc; padding: 20px; font-size: 18px; }
        tr { height: 80px; }
        #loading { text-align: center; padding: 20px; display: none; }
        button { padding: 10px 20px; font-size: 16px; margin-bottom: 20px; cursor: pointer; }
    </style>
</head>
<body>

<%
    Files file = (Files) request.getAttribute("file");
    List<FileVersion> versions = (List<FileVersion>) request.getAttribute("versions");
    String cursor = "";

    if (file == null) {
        response.sendRedirect("files");
        return;
    }
%>

<h2>Versions of <%= file.getFileName() %></h2>
<h3>
    <a href="files">Back to Files</a> |
    <a href="upload.jsp?fileId=<%= file.getFileId() %>">Upload New Version</a>
</h3>

<button id="toggleSort">Latest -> Oldest</button>

<table>
    <thead>
        <tr>
            <th>Version</th>
            <th>Uploaded Time</th>
            <th>Notes</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody id="tableBody">
        <%
            if (versions != null && !versions.isEmpty()) {
                for (FileVersion v : versions) {
                    cursor = v.getUploadedTime() != null ? v.getUploadedTime().toString() : "";
        %>
        <tr>
            <td>v<%= v.getVersionNumber() %></td>
            <td><%= v.getUploadedTime() %></td>
            <td><%= v.getNotes() != null ? v.getNotes() : "No Notes" %></td>
            <td>
                <a href="view?versionId=<%= v.getId() %>">View File</a>
                <a href="download?versionId=<%= v.getId() %>">Download</a>
                <a href="versionDetails?versionId=<%= v.getId() %>">View Details</a>
            </td>
        </tr>
        <%
                }
            }
        %>
    </tbody>
</table>

<div id="loading">Loading more versions...</div>

<script>
let cursor = "<%= cursor %>";
let loading = false;
let noMore = false;
let sortOrder = "desc";
let fileId = "<%= file.getFileId() %>";

document.getElementById("toggleSort").addEventListener("click", function() {
    sortOrder = sortOrder === "desc" ? "asc" : "desc";
    this.innerText = sortOrder === "desc" ? "Latest -> Oldest" : "Oldest -> Latest";

    document.getElementById("tableBody").innerHTML = "";
    cursor = "";
    noMore = false;

    loadMore();
});

async function loadMore() {
    if (loading || noMore) return;
    loading = true;
    document.getElementById("loading").style.display = "block";

    try {
        let response = await fetch("versions?fileId=" + fileId + "&cursor=" + encodeURIComponent(cursor) + "&sort=" + sortOrder);
        let html = await response.text();

        let nextCursor = response.headers.get("nextCursor");
        let hasMore = response.headers.get("hasMore");

        if (html.trim() !== "") {
            document.getElementById("tableBody").insertAdjacentHTML("beforeend", html);
        }

        if (nextCursor) cursor = nextCursor;

        if (hasMore === "false") {
            noMore = true;
            document.getElementById("loading").innerText = "No more versions";
        } else {
            document.getElementById("loading").style.display = "none";
        }
    } catch (err) {
        console.error("Error fetching versions:", err);
    }

    loading = false;
}

window.addEventListener("scroll", function() {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 200) {
        loadMore();
    }
});
</script>

</body>
</html>