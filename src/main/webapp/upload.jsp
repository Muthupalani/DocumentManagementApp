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
	    user = userDAO.getUserById(con, userId);
	
	    if (user == null) {
	        response.sendRedirect("login.jsp");
	        return;
	    }
	
	} catch (Exception e) {
	
	    out.println("<h3 style='color:red'>Error loading user</h3>");
	    e.printStackTrace(new java.io.PrintWriter(out));
	
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
		
		body{
		font-family:Arial;
		}
		
		.center-box{
		width:450px;
		margin:100px auto;
		}
		
		progress{
		width:100%;
		height:20px;
		}
		
		button{
		padding:10px 20px;
		font-size:16px;
		cursor:pointer;
		margin-right:10px;
		}
		
		textarea{
		width:100%;
		}
		
		</style>
		
		</head>
		
		<body>
		
		<div class="center-box">
		
		<h2>Welcome, <%= user.getName() %></h2>
		
		<hr>
		
		<% if (isNewVersion) { %>
		<h3>Upload New Version</h3>
		<% } else { %>
		<h3>Upload New File</h3>
		<% } %>
	
	<form id="uploadForm">
	
	<% if (isNewVersion) { %>
	
	<input type="hidden" id="fileId" value="<%= fileIdParam %>">
	
	Notes (optional):<br>
	<textarea id="notes" rows="4"></textarea>
	
	<br><br>
	
	<% } %>
	
	Select File:<br>
	<input type="file" id="file" required>
	
	<br><br>
	
		<button type="button" onclick="startUpload()">Upload</button>
		<button type="button" onclick="pauseUpload()">Pause</button>
		<button type="button" onclick="resumeUpload()">Resume</button>
	
	</form>
	
	<br>
	
	<progress id="progressBar" value="0" max="100"></progress>
	
	<br><br>
	
	<a href="files">View My Files</a>
	<br><br>
	<a href="logout">Logout</a>
	
	</div>
	
	<script>
	
		const CHUNK_SIZE = 2 * 1024 * 1024;
		const MAX_PARALLEL = 4;
		
		let paused=false;
		let queue=[];
		let running=0;
		let file;
		let totalChunks;
		let uploadedChunks=0;
		let uploadId;
		
		function startUpload(){
	
		file=document.getElementById("file").files[0];
		
		if(!file){
		alert("Select a file");
		return;
		}
		
		const fileId=document.getElementById("fileId")?.value;
		const notesField=document.getElementById("notes");
		const notes=notesField ? notesField.value : "";
		
		uploadId=Date.now()+"_"+Math.random();
		
		totalChunks=Math.ceil(file.size/CHUNK_SIZE);
		
		uploadedChunks=0;
		
		queue=[];
		running=0;
		
		for(let i=0;i<totalChunks;i++){
		queue.push(i);
	}
	
		window.fileId=fileId;
		window.notes=notes;
		
		runQueue();
	
	}
	
	function pauseUpload(){
	
		paused=true;
	
	}
	
	function resumeUpload(){
	
		paused=false;
		
		runQueue();
	
	}
	
	function uploadChunk(index){
	
		const start=index*CHUNK_SIZE;
		const end=Math.min(start+CHUNK_SIZE,file.size);
		const blob=file.slice(start,end);
		
		let formData=new FormData();
		
		formData.append("uploadId",uploadId);
		formData.append("chunkIndex",index);
		formData.append("totalChunks",totalChunks);
		formData.append("chunk",blob);
		formData.append("fileName",file.name);
		
		if(window.fileId) formData.append("fileId",window.fileId);
		if(window.notes) formData.append("notes",window.notes);
		
		fetch("uploadChunk",{  
		method:"POST",
		body:formData
		})
		
		.then(res=>res.text())
		
		.then(result=>{
		
		result=result.trim();
		
		uploadedChunks++;
		
		document.getElementById("progressBar").value=
		Math.floor((uploadedChunks/totalChunks)*100);
		
		running--;
		
		if(result==="MERGED"){
		
			location.href="files";
			return;
		
		}
		
		runQueue();
		
		})
		
	.catch(err=>{
	
		console.error("Upload failed",err);
		
		running--;
		
	runQueue();
	
	});
	
	}
	
	function runQueue(){
	
		if(paused) return;
		
		while(queue.length && running<MAX_PARALLEL){
		
		const index=queue.shift();
		
		running++;
		
		uploadChunk(index);
	
	}
	
	}
	
	</script>
	
	</body>
	</html>