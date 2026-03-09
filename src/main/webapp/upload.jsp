<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.documentapp.util.AuthUtil" %>
<%@ page import="com.documentapp.dao.UserDAO" %>
<%@ page import="com.documentapp.model.User" %>
<%@ page import="com.documentapp.util.DBConnection" %>
<%@ page import="java.sql.Connection" %>

<%
Long userId = AuthUtil.getUserIdFromCookie(request);

if(userId == null){
    response.sendRedirect("login.jsp");
    return;
}

User user = null;
Connection con = null;

try{
    con = DBConnection.getConnection();
    UserDAO userDAO = new UserDAO();
    user = userDAO.getUserById(con,userId);
    if(user == null){
        response.sendRedirect("login.jsp");
        return;
    }
} finally {
    DBConnection.close(con);
}

String fileIdParam = request.getParameter("fileId");
boolean isNewVersion = (fileIdParam != null && !fileIdParam.isEmpty());
%>

<html>
<head>
<title>Upload File</title>

<style>
.center-box{ width:450px; margin:100px auto; font-family:Arial; }
progress{ width:100%; height:20px; }
button{ padding:10px 20px; font-size:16px; cursor:pointer; }
textarea{ width:100%; }
</style>

</head>
<body>

<div class="center-box">

<h2>Welcome, <%= user.getName() %></h2>
<hr>

<% if(isNewVersion){ %>
    <h3>Upload New Version</h3>
<% } else { %>
    <h3>Upload New File</h3>
<% } %>

<form id="uploadForm">

<% if(isNewVersion){ %>
    <input type="hidden" id="fileId" value="<%=fileIdParam%>">
<% } %>

Select File:<br>
<input type="file" id="file" required><br><br>

<% if(isNewVersion){ %>
Notes:<br>
<textarea id="notes" rows="4"></textarea><br><br>
<% } %>

<button type="button" onclick="startUpload()">Upload</button>
</form>

<br>
<progress id="progressBar" value="0" max="100"></progress>

</div>

<script>
const CHUNK_SIZE = 2 * 1024 * 1024; // 2MB
const MAX_PARALLEL = 4;

function startUpload() {
    const file = document.getElementById("file").files[0];
    if(!file) return alert("Select a file!");

    const notes = document.getElementById("notes")?.value;
    const fileId = document.getElementById("fileId")?.value;

    const uploadId = Date.now() + "_" + Math.random();
    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);

    let uploadedChunks = 0;
    let queue = [];
    for(let i=0;i<totalChunks;i++) queue.push(i);
    let running = 0;

    function uploadChunk(chunkIndex){
        const start = chunkIndex * CHUNK_SIZE;
        const end = Math.min(start + CHUNK_SIZE, file.size);
        const blob = file.slice(start,end);

        let formData = new FormData();
        formData.append("uploadId", uploadId);
        formData.append("chunkIndex", chunkIndex);
        formData.append("totalChunks", totalChunks);
        formData.append("chunk", blob);
        formData.append("fileName", file.name);
        if(notes) formData.append("notes", notes);
        if(fileId) formData.append("fileId", fileId);

        fetch("uploadChunk", { method:"POST", body:formData })
        .then(r => r.text())
        .then(()=> {
            uploadedChunks++;
            document.getElementById("progressBar").value = (uploadedChunks / totalChunks) * 100;
            running--;
            runQueue();
            if(uploadedChunks === totalChunks){
                mergeFile();
            }
        }).catch(e=>{
            console.error("Chunk upload failed",e);
            running--;
            runQueue();
        });
    }

    function runQueue(){
        while(queue.length && running < MAX_PARALLEL){
            const idx = queue.shift();
            running++;
            uploadChunk(idx);
        }
    }

    runQueue();

    function mergeFile(){
        let formData = new FormData();
        formData.append("uploadId", uploadId);
        formData.append("fileName", file.name);
        formData.append("totalChunks", totalChunks);
        if(notes) formData.append("notes", notes);
        if(fileId) formData.append("fileId", fileId);

        fetch("mergeChunks",{ method:"POST", body:formData })
        .then(r=>r.text())
        .then(()=> {
            // Redirect to files list page automatically
            window.location.href = "files";
        });
    }
}
</script>

</body>
</html>