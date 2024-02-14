<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html>
<html>
<head>
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <meta charset="ISO-8859-1">
    <title>Dashboard-SFTP</title>
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
            width: 95%;
            height: 47%;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .topPanel {
            background-color: transparent;
        }

        .bottomPanel {
            background-color: lightyellow;
            border: 1px solid #fff;
        }

        .subpanel {
            width: 50%;
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
            border: 1px solid #fff;
        }

        .subpanel1 {
            background-color: lightblue;
            display: flex;
            flex-direction: column;
            justify-content: flex-start;
        }

        .subpanel2 {
            background-color: lightsteelblue;
            margin-left: 5px;
        }

        .contentPanel {
            width: 98%;
            height: 95%;
            display: flex;
            justify-content: center;
            align-items: center;
            margin-top: 5px;
            margin-bottom: 5px;
        }

        .contentPanel1 {
            display: flex;
            justify-content: flex-start;
            align-items: center;
            border: 1px solid #ffffff;
            height: auto;
        }

        .contentPanel1 button {
            /* add some thing later*/
        }

        .contentPanel1 input[type="text"] {
            flex-grow: 1;
            margin: 0 10px;
        }

        button {
            background-color: #3498db;
            color: white;
            border: none;
            text-align: center;
            text-decoration: none;
            display: flex;
            font-size: 12px;
            margin: 2px 2px;
            cursor: pointer;
            border-radius: 4px;
            width: auto;
            padding: 10px;
            white-space: nowrap;
        }

        input[type="text"] {
            padding: 10px;
            border: none;
            border-radius: 4px;
            width: 100%;
            box-sizing: border-box;
            font-size: 12px;
        }

        .contentPanel2 {
            width: 100%;
            max-height: 100%;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            background: black;
            justify-content: flex-start;
        }

        select {
            background-color: #3498db;
            color: white;
            border: none;
            text-align: center;
            text-decoration: none;
            display: flex;
            font-size: 12px;
            margin: 2px 2px;
            cursor: pointer;
            border-radius: 4px;
            width: auto;
            padding: 10px;
            white-space: nowrap;
        }
    </style>
</head>
<body>
<div class="panel topPanel">
    <div class="subpanel subpanel1">
        <div class="contentPanel contentPanel1">
            <button id="selectFileButton">Select CSV File</button>
            <input type="file" id="fileInput" style="display: none;">
            <label for="fileNameField"></label>
            <input type="text" id="fileNameField" placeholder="Selected csv file name">
            <button id="submitButton">Submit</button>
            <button id="resetButton">Reset</button>
        </div>
        <div class="contentPanel contentPanel2">
            <iframe title="rightPanelIframe"
                    src="${sftp_left_panel_url}"
                    width="100%"
                    height="100%"
                    style="border: none;"></iframe>
        </div>
    </div>
    <div class="subpanel subpanel2">
        <iframe title="rightPanelIframe"
                src="${sftp_right_panel_url}"
                width="100%"
                height="100%"></iframe>
    </div>
</div>
<div class="panel bottomPanel">
    <iframe title="bottomPanelIframe"
            src="${sftp_bottom_panel_url}"
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
        let postSftpEndpointUrl = "${sftp_post_endpoint_url}";

        let fileInput = document.getElementById('fileInput');
        let file = fileInput.files[0];
        let formData = new FormData();
        formData.append('file', file);

        let userConfirmation = confirm("Are you sure you want to submit the file?");
        if (!userConfirmation) {
            return;
        }
        fetch(postSftpEndpointUrl, {
            method: 'POST',
            body: formData
        }).then(response => {
            console.log(response);
        }).catch(error => {
            console.error(error);
        });
    });

      document.getElementById('resetButton').addEventListener('click', function () {
          let userConfirmation = confirm("Are you sure you want to reset the database?");
          if (userConfirmation) {
              let clearDcDbEndpointUrl = "${clear_dc_db_endpoint_url}";
            let jwtToken = "${jwtToken}";
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