<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title>Dashboard</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/styles.css">
</head>
<body>
<div class="panel contentPanel">
    <iframe title="My Iframe"
            src="http://localhost:3005/d-solo/c766225a-d5cf-4c9f-99a7-6f8291f407eb/dp-dashboard?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
            width="100%"
            height="100%"></iframe>
</div>
<script>
    document.getElementById('selectFileButton').addEventListener('click', function() {
        document.getElementById('fileInput').click();
    });

    document.getElementById('fileInput').addEventListener('change', function() {
        if (this.files && this.files.length > 0) {
            document.getElementById('fileNameField').value = this.files[0].name;
        }
    });

    document.getElementById('submitButton').addEventListener('click', function() {
        let postEndpoint= "http://localhost:8000/public/api/v1/consumer/search/csv";
        let fileInput = document.getElementById('fileInput');
        let file = fileInput.files[0];
        let formData = new FormData();
        formData.append('file', file);

        fetch(postEndpoint, {
            method: 'POST',
            body: formData
        }).then(response => {
            console.log(response);
        }).catch(error => {
            console.error(error);
        });
    });
</script>
</body>
</html>