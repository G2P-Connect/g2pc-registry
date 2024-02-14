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
            width: 97%;
            padding: 10px;
            max-height: 80%;
            overflow: auto;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            background: black;
            justify-content: flex-start;
            border: 1px solid #fff;
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

        .first-panel-data-table {
            width: 100%;
            max-height: 100%;
            border-collapse: collapse;
            font-family: sans-serif;
            font-size: small;
        }

        .first-panel-data-table thead tr th {
            padding: 8px;
            text-align: left;
            background-color: #000000;
        }

        .first-panel-data-table tr td {
            border: 1px solid #000000;
            background: linear-gradient(to right, #808080, #d3d3d3);
            padding: 8px;
            text-align: left;
        }

        .first-panel-data-table tbody tr:hover td {
            background: green;
            background: linear-gradient(to right, #008000, #00FF00);
        }

        .contentPanel3 {
            width: 97%;
            padding: 10px;
            max-height: 100%;
            overflow: auto;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            background: black;
            justify-content: flex-start;
            border: 1px solid #fff;
        }

        .second-panel-data-table {
            width: 100%;
            max-height: 100%;
            border-collapse: collapse;
            font-family: sans-serif;
            font-size: small;
        }

        .second-panel-data-table thead tr th {
            padding: 8px;
            text-align: left;
            background-color: #000000;
        }

        .second-panel-data-table tr td {
            border: 1px solid #000000;
            background: linear-gradient(to right, #808080, #d3d3d3);
            padding: 8px;
            text-align: left;
        }

        .second-panel-data-table tbody tr:hover td {
            background: green;
            background: linear-gradient(to right, #008000, #00FF00);
        }

        .second-panel-data-table {
            width: 100%;
            max-height: 100%;
            border-collapse: collapse;
            font-family: sans-serif;
            font-size: small;
        }

        .second-panel-data-table thead tr th {
            padding: 8px;
            text-align: left;
            background-color: #000000;
        }

        .second-panel-data-table tr td {
            border: 1px solid #000000;
            background: linear-gradient(to right, #808080, #d3d3d3);
            padding: 8px;
            text-align: left;
        }

        .second-panel-data-table tbody tr:hover td {
            background: green;
            background: linear-gradient(to right, #008000, #00FF00);
        }


        .contentPanel4 {
            width: 100%;
            padding: 10px;
            max-height: 100%;
            overflow: auto;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            background: black;
            justify-content: flex-start;
            border: 1px solid #fff;
        }

        .third-panel-data-table {
            width: 100%;
            max-height: 100%;
            border-collapse: collapse;
            font-family: sans-serif;
            font-size: small;
        }

        .third-panel-data-table thead tr th {
            padding: 8px;
            text-align: left;
            background-color: #000000;
        }

        .third-panel-data-table tr td {
            border: 1px solid #000000;
            background: linear-gradient(to right, #808080, #d3d3d3);
            padding: 8px;
            text-align: left;
        }

        .third-panel-data-table tbody tr:hover td {
            background: green;
            background: linear-gradient(to right, #008000, #00FF00);
        }

        .third-panel-data-table {
            width: 100%;
            max-height: 100%;
            border-collapse: collapse;
            font-family: sans-serif;
            font-size: small;
        }

        .third-panel-data-table thead tr th {
            padding: 8px;
            text-align: left;
            background-color: #000000;
        }

        .third-panel-data-table tr td {
            border: 1px solid #000000;
            background: linear-gradient(to right, #808080, #d3d3d3);
            padding: 8px;
            text-align: left;
        }

        .third-panel-data-table tbody tr:hover td {
            background: green;
            background: linear-gradient(to right, #008000, #00FF00);
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
            <table class="first-panel-data-table" id="firstPanelDataTable">
                <caption style="text-align: left;
                margin-bottom: 10px;">Data Consumer - Inbound
                </caption>
                <thead>
                <tr>
                    <th>message_ts</th>
                    <th>transaction_id</th>
                    <th>file_name</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>
    <div class="subpanel subpanel2">
        <div class="contentPanel contentPanel3">
            <table class="second-panel-data-table" id="secondPanelDataTable">
                <caption style="text-align: left;
                margin-bottom: 10px;">Data Producer - Inbound
                </caption>
                <thead>
                <tr>
                    <th>dp_name</th>
                    <th>message_ts</th>
                    <th>transaction_id</th>
                    <th>file_name</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>
</div>
<div class="panel bottomPanel">
    <div class="contentPanel contentPanel4">
        <table class="third-panel-data-table" id="thirdPanelDataTable">
            <caption style="text-align: left;
                margin-bottom: 10px;">Data Consumer - Outbound
            </caption>
            <thead>
            <tr>
                <th>message_ts</th>
                <th>transaction_id</th>
                <th>file_name</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
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

    let firstPanelTableBody = document.querySelector('.first-panel-data-table tbody');
    let thirdPanelTableBody = document.querySelector('.third-panel-data-table tbody');
    console.log("${sftp_dc_data_endpoint_url}");
    let dcSource = new EventSource("${sftp_dc_data_endpoint_url}");
    dcSource.onmessage = function (event) {
        let data = JSON.parse(event.data);
        console.log(data);
        let rowData = {
            messageTs: data.messageTs,
            transactionId: data.transactionId,
            fileName: data.fileName,
            sftpDirectoryType: data.sftpDirectoryType
        }
        console.log(rowData);
        let row = document.createElement('tr');
        row.innerHTML = "<td>" + rowData.messageTs + "</td>" +
            "<td>" + rowData.transactionId + "</td>" +
            "<td>" + rowData.fileName + "</td>";
        if (rowData.sftpDirectoryType === "INBOUND") {
            firstPanelTableBody.appendChild(row);
        } else if (rowData.sftpDirectoryType === "OUTBOUND") {
            thirdPanelTableBody.appendChild(row);
        }
    };

    dcSource.onerror = function (event) {
        console.error("EventSource failed:", event);
        dcSource = null;
        location.reload();
    };

/*    let secondPanelTableBody = document.querySelector('.second-panel-data-table tbody');

    function appendRowToSecondPanel(event) {
        let data = JSON.parse(event.data);
        let rowData = {
            dpType: data.dpType,
            messageTs: data.messageTs,
            transactionId: data.transactionId,
            fileName: data.fileName
        }
        let row = document.createElement('tr');
        row.innerHTML = "<td>" + rowData.dpType + "</td>" +
            "<td>" + rowData.messageTs + "</td>" +
            "<td>" + rowData.transactionId + "</td>" +
            "<td>" + rowData.fileName + "</td>";
        secondPanelTableBody.appendChild(row);
    }

    let dp1Source = new EventSource("${sftp_dp1_data_endpoint_url}");
    dp1Source.onmessage = function (event) {
        appendRowToSecondPanel(event);
    };
    /!* dp1Source.onerror = function (event) {
         console.error("EventSource failed:", event);
         dp1Source = null;
         location.reload();
     };*!/

    let dp2Source = new EventSource("${sftp_dp2_data_endpoint_url}");
    dp2Source.onmessage = function (event) {
        appendRowToSecondPanel(event);
    };
    /!*dp2Source.onerror = function (event) {
        console.error("EventSource failed:", event);
        dp2Source = null;
        location.reload();
    };*!/*/
</script>
</body>
</html>