<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
    <meta charset="ISO-8859-1">
    <title>Dashboard-HTTPS</title>
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

        .left-panel-data-table {
            width: 100%;
            max-height: 100%;
            border-collapse: collapse;
            font-family: sans-serif;
            font-size: small;
        }

        .left-panel-data-table thead tr th {
            padding: 8px;
            text-align: left;
            background-color: #000000;
        }

        .left-panel-data-table tr td {
            border: 1px solid #000000;
            background: linear-gradient(to right, #808080, #d3d3d3);
            padding: 8px;
            text-align: left;
        }

        .left-panel-data-table tbody tr:hover td {
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
            <table class="left-panel-data-table" id="leftPanelDataTable">
                <caption style="text-align: left;
                margin-bottom: 10px;">Data Consumer - Request Tracker
                </caption>
                <thead>
                <tr>
                    <th>message_ts</th>
                    <th>transaction_id</th>
                    <th>status</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
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
<div id="dialog"
     title="Confirmation">
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
        let postHttpsEndpointUrl = "${post_https_endpoint_url}";

        let fileInput = document.getElementById('fileInput');
        let file = fileInput.files[0];
        let formData = new FormData();
        formData.append('file', file);

        let userConfirmation = confirm("Are you sure you want to submit the file?");
        if (!userConfirmation) {
            return;
        }
        fetch(postHttpsEndpointUrl, {
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

    let leftPanelDataEndpointUrl = "${left_panel_data_endpoint_url}";
    let rightPanelUrl = "${right_panel_url}";
    let bottomPanelUrl = "${bottom_panel_url}";
    let tableBody = document.querySelector('.left-panel-data-table tbody');
    setInterval(function () {
        fetch(leftPanelDataEndpointUrl)
            .then(response => response.json())
            .then(data => {
                tableBody.innerHTML = '';
                for (let item of data) {
                    let rowData = {
                        messageTs: item.messageTs,
                        transactionId: item.transactionId,
                        status: item.status
                    }
                    let row = document.createElement('tr');
                    row.innerHTML = "<td>" + rowData.messageTs + "</td>" +
                        "<td>" + rowData.transactionId + "</td>" +
                        "<td>" + rowData.status + "</td>";

                    row.addEventListener('click', function () {
                        let tdElements = this.querySelectorAll('td');
                        rowData.messageTs = tdElements[0].textContent;
                        rowData.transactionId = tdElements[1].textContent;
                        rowData.status = tdElements[2].textContent;
                        $("#dialog").html("Action to be performed with transaction_id?")
                            .dialog({
                                resizable: false,
                                height: "auto",
                                width: 400,
                                modal: true,
                                buttons: {
                                    "View-info": function () {
                                        $(this).dialog("close");
                                        let newRightPanelUrl = rightPanelUrl + "&var-transactionId=" + rowData.transactionId;
                                        console.log(newRightPanelUrl);
                                        let iframeRightPanel = document.querySelector('iframe[title="rightPanelIframe"]');
                                        iframeRightPanel.src = newRightPanelUrl.toString();

                                        let newBottomPanelUrl = bottomPanelUrl + "&var-transactionId=" + rowData.transactionId;
                                        console.log(newBottomPanelUrl);
                                        let iframeBottomPanel = document.querySelector('iframe[title="bottomPanelIframe"]');
                                        iframeBottomPanel.src = newBottomPanelUrl.toString();
                                    },
                                    "Check-status": function () {
                                        let dcStatusEndpointUrl = "${dc_status_endpoint_url}";
                                        console.log(dcStatusEndpointUrl);
                                        let transactionId = rowData.transactionId;
                                        let fetchOptions = {
                                            method: 'POST',
                                            headers: {
                                                'Content-Type': 'application/json',
                                            },
                                        };
                                        fetch(dcStatusEndpointUrl + transactionId, fetchOptions)
                                            .then(response => response.json())
                                            .then(data => {
                                                if (data.status === "rcvd") {
                                                    alert("Status checked successfully, please check view info for more details");
                                                } else {
                                                    alert("Status check failed");
                                                }
                                            })
                                            .catch(error => {
                                                console.error('Error:', error);
                                            });

                                        $(this).dialog("close");
                                    },
                                    "Close": function () {

                                        $(this).dialog("close");
                                    }
                                },
                                open: function (event, ui) {
                                    $(this).dialog("widget").find(".ui-button").each(function () {
                                        $(this).css("background-color", "#3498db");
                                        $(this).css("color", "white");
                                        $(this).css("border-color", "#3498db");
                                    });
                                }
                            });
                    });
                    tableBody.appendChild(row);
                }
            })
            .catch(error => {
                console.error('Error fetching data:', error);
            });
    }, 5000);
</script>
</body>
</html>