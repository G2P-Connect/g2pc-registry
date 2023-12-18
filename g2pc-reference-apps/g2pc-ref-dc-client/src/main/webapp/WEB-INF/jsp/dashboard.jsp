<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="ISO-8859-1">
    <title>Dashboard</title>
    <link rel="stylesheet" type="text/css" href="../resources/css/styles.css">
</head>
<body>
<div class="panel topPanel">
    <div class="subpanel subpanel1">
        <div class="contentPanel contentPanel1">
            <button id="selectFileButton">Select CSV File</button>
            <input type="file" id="fileInput" style="display: none;">
            <label for="fileNameField"></label><input type="text" id="fileNameField" placeholder="Enter text here">
            <button id="submitButton">Submit</button>
            <button id="resetButton">Reset DB</button>
        </div>
        <div class="contentPanel contentPanel2">
            <iframe title="leftPanelIframe"
                    src="${left_panel_url}"
                    width="100%"
                    height="100%"></iframe>
        </div>
    </div>
    <div class="subpanel subpanel2">
        <iframe title="rightPanelIframe"
                src="${right_panel_url}"
                width="100%"
                height="100%"></iframe>
    </div>
</div>
<div class="panel bottomPanel">
    <iframe title="bottomPanelIframe"
            src="${bottom_panel_url}"
            width="100%"
            height="100%"></iframe>
</div>
<script>
    document.getElementById('selectFileButton').addEventListener('click', function () {
        document.getElementById('fileInput').click();
    });

    document.getElementById('fileInput').addEventListener('change', function () {
        if (this.files && this.files.length > 0) {
            document.getElementById('fileNameField').value = this.files[0].name;
        }
    });

    document.getElementById('submitButton').addEventListener('click', function () {
        let userConfirmation = confirm("Are you sure you want to submit the file?");
        if (userConfirmation) {
            let postEndpoint = "${post_endpoint_url}";
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
        }
    });

    document.getElementById('resetButton').addEventListener('click', function () {
        let userConfirmation = confirm("Are you sure you want to reset the database?");
        if (userConfirmation) {
            let clearDcDbEndpointUrl = "${clear_dc_db_endpoint_url}";
            let jwtToken = "${jwtToken}";
            console.log(jwtToken);
            fetch(clearDcDbEndpointUrl, {
                method: 'GET',
                headers: {
                    "Authorization": jwtToken
                }
            }).then(response => {
                console.log(response);
            }).catch(error => {
                console.error(error);
            });
        }
    });
</script>
</body>
</html>