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
            display: flex;
            justify-content: flex-start;
            align-items: center;
            border: 1px solid #fff;
        }
    </style>
</head>
<body>
<div class="panel topPanel">
    <div class="subpanel subpanel1">
        <div class="contentPanel contentPanel1">
            <button id="selectFileButton">Select CSV File</button>
            <input type="file" id="fileInput" style="display: none;">
            <input type="text" id="fileNameField" placeholder="Enter text here">
            <button id="submitButton">Submit</button>
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
        let postEndpoint = "http://localhost:8000/public/api/v1/consumer/search/csv";
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