<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.documentapp.model.Files" %>

<html>
<head>
<title>My Files</title>

<style>
body{
    font-family:Arial;
}

table{
    width:100%;
    border-collapse:collapse;
}

th,td{
    border:1px solid #ccc;
    padding:20px;
    font-size:18px;
}

tr{
    height:80px;
}

button{
    padding:10px 20px;
    font-size:16px;
    margin-bottom:20px;
    cursor:pointer;
}
</style>

</head>
<body>

<h2>My Files</h2>
<h3><a href="<%= request.getContextPath() %>/upload.jsp">Upload New File</a></h3>

<button id="toggleSort">Latest -> Oldest</button>

<table>
<thead>
<tr>
    <th>File Name</th>
    <th>Version</th>   
    <th>Last Modified</th>
    <th>Actions</th>
</tr>
</thead>
<tbody id="tableBody">
<%
    List<Files> files = (List<Files>) request.getAttribute("files");
    String cursor = "";

    if (files != null && !files.isEmpty()) {
        for (Files file : files) {
            cursor = file.getModifiedTime() != null ? file.getModifiedTime().toString() : "";
%>
<tr>
    <td><%=file.getFileName()%></td>
    <td><%=file.getLatestVersion()%></td>  
    <td><%=file.getModifiedTime()%></td>
    <td>
        <a href="view?fileId=<%=file.getFileId()%>">View File</a>
        <a href="download?fileId=<%=file.getFileId()%>">Download</a>
        <a href="versions?fileId=<%=file.getFileId()%>">View Versions</a>
        <a href="upload.jsp?fileId=<%=file.getFileId()%>">Upload New Version</a>
    </td>
</tr>
<%
        }
    }
%>
</tbody>
</table>

<div id="loading" style="text-align:center;padding:20px;display:none;">
    Loading more files...
</div>

<script>
let cursor = "<%=cursor%>";
let loading = false;
let noMore = false;
let sortOrder = "desc"; 

document.getElementById("toggleSort").addEventListener("click", function(){
    sortOrder = sortOrder === "desc" ? "asc" : "desc";
    this.innerText = sortOrder === "desc" ? "Latest -> Oldest" : "Oldest -> Latest";

    document.getElementById("tableBody").innerHTML = "";
    cursor = "";
    noMore = false;

    loadMore(); 
});

async function loadMore(){
    if(loading || noMore) return;

    loading = true;
    document.getElementById("loading").style.display="block";

    try {
        let response = await fetch("files?cursor=" + encodeURIComponent(cursor) + "&sort=" + sortOrder);
        let html = await response.text();

        let nextCursor = response.headers.get("nextCursor");
        let hasMore = response.headers.get("hasMore");

        if(html.trim() !== ""){
            document.getElementById("tableBody").insertAdjacentHTML("beforeend", html);
        }

        if(nextCursor){
            cursor = nextCursor;
        }

        if(hasMore === "false"){
            noMore = true;
            document.getElementById("loading").innerText = "No more files";
        } else {
            document.getElementById("loading").style.display="none";
        }

    } catch (err){
        console.error("Error fetching files:", err);
    }

    loading = false;
}

window.addEventListener("scroll", function(){
    if(window.innerHeight + window.scrollY >= document.body.offsetHeight - 200){
        loadMore();
    }
});
</script>

</body>
</html>