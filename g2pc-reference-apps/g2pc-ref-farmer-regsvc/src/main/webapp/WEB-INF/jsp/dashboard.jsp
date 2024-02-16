<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title>Dashboard</title>
   <style>
       body {
           background-color: rgba(0, 0, 0, 0.1);
           color: #fff;
           display: flex;
           justify-content: space-around;
           align-items: center;
           height: 100vh;
           margin: 0;
           padding: 0;
           flex-direction: column;
       }

       .panel {
           width: 99%;
           height: 97%;
           display: flex;
           justify-content: center;
           align-items: center;
       }

       .contentPanel {
           background-color: lightyellow;
           border: 1px solid #fff;
       }
   </style>
</head>
<body>
<div class="panel contentPanel">
    <iframe title="My Iframe"
            src="${dp_dashboard_url}"
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