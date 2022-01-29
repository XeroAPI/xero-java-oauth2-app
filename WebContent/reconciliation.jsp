<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="css/reconciliation.css"/>
    <title>Xero | CSV Import Options </title>
</head>
<body>
<div class="container">
    <form method="post">
        <div class="w-content">
            <div class="document">
                <div class="file">
                    <h2>
                        Statement lines imported from your file...
                    </h2>
                    <div class="form">
                        <div class="entries">
                            <span>Statement line <i
                                    id="currentPage">1</i> of <i>${ sessionScope.entries.size() }</i> </span>
                            <a href="#" onclick="changeEntryPosition(-1)">&#8592;
                                Previous</a>
                            <a href="#" onclick="changeEntryPosition(1)">Next
                                &#8594;</a>
                        </div>
                        <fieldset>
                            <legend>
                                Statement Lines
                            </legend>


                            <table class="data">
                                <thead>
                                <tr>
                                    <td colspan="2">Statement data...</td>
                                    <td class="no-border">Assign to...</td>
                                </tr>
                                </thead>
                                <tbody id="mappingData">
                                <tr id="col_0">
                                    <td class="em columnName" title="Date">Date</td>
                                    <td class="dataValue">2022-01-03</td>
                                    <td class="no-border">
                                        <select id="col_0" class="mapping">
                                            <option selected="selected">Unassigned</option>
                                            <option class="opt_0 assignedMapping" value="TransactionDate">Transaction
                                                Date
                                            </option>
                                            <option class="opt_1 assignedMapping" value="Amount">Transaction Amount
                                            </option>
                                            <option class="opt_2" value="Payee">Payee</option>
                                            <option class="opt_3 assignedMapping" value="Notes">Description</option>
                                            <option class="opt_4" value="Reference">Reference</option>
                                            <option class="opt_5" value="Type">Transaction Type</option>
                                            <option class="opt_6" value="ChequeNo">Cheque No.</option>
                                            <option class="opt_7" value="AccountCode">Account Code</option>
                                            <option class="opt_8" value="TaxType">Tax Type</option>
                                            <option class="opt_9" value="AnalysisCode">Analysis Code</option>
                                            <option class="opt_10" value="TrackingCategory1">Region</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr id="col_1">
                                    <td class="em columnName" title="Amount">Amount</td>
                                    <td class="dataValue">110</td>
                                    <td class="no-border">
                                        <select id="col_1" class="mapping">
                                            <option selected="selected">Unassigned</option>
                                            <option class="opt_0 assignedMapping" value="TransactionDate">Transaction
                                                Date
                                            </option>
                                            <option class="opt_1 assignedMapping" value="Amount">Transaction Amount
                                            </option>
                                            <option class="opt_2" value="Payee">Payee</option>
                                            <option class="opt_3 assignedMapping" value="Notes">Description</option>
                                            <option class="opt_4" value="Reference">Reference</option>
                                            <option class="opt_5" value="Type">Transaction Type</option>
                                            <option class="opt_6" value="ChequeNo">Cheque No.</option>
                                            <option class="opt_7" value="AccountCode">Account Code</option>
                                            <option class="opt_8" value="TaxType">Tax Type</option>
                                            <option class="opt_9" value="AnalysisCode">Analysis Code</option>
                                            <option class="opt_10" value="TrackingCategory1">Region</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr id="col_2">
                                    <td class="em columnName" title="Description">Description</td>
                                    <td class="dataValue">Description</td>
                                    <td class="no-border">
                                        <select id="col_2" class="mapping">
                                            <option selected="selected">Unassigned</option>
                                            <option class="opt_0 assignedMapping" value="TransactionDate">Transaction
                                                Date
                                            </option>
                                            <option class="opt_1 assignedMapping" value="Amount">Transaction Amount
                                            </option>
                                            <option class="opt_2" value="Payee">Payee</option>
                                            <option class="opt_3 assignedMapping" value="Notes">Description</option>
                                            <option class="opt_4" value="Reference">Reference</option>
                                            <option class="opt_5" value="Type">Transaction Type</option>
                                            <option class="opt_6" value="ChequeNo">Cheque No.</option>
                                            <option class="opt_7" value="AccountCode">Account Code</option>
                                            <option class="opt_8" value="TaxType">Tax Type</option>
                                            <option class="opt_9" value="AnalysisCode">Analysis Code</option>
                                            <option class="opt_10" value="TrackingCategory1">Region</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr id="col_3">
                                    <td class="em columnName" title="Reference">Reference</td>
                                    <td class="dataValue">1</td>
                                    <td class="no-border">
                                        <select id="col_3" class="mapping">
                                            <option selected="selected">Unassigned</option>
                                            <option class="opt_0 assignedMapping" value="TransactionDate">Transaction
                                                Date
                                            </option>
                                            <option class="opt_1 assignedMapping" value="Amount">Transaction Amount
                                            </option>
                                            <option class="opt_2" value="Payee">Payee</option>
                                            <option class="opt_3 assignedMapping" value="Notes">Description</option>
                                            <option class="opt_4" value="Reference">Reference</option>
                                            <option class="opt_5" value="Type">Transaction Type</option>
                                            <option class="opt_6" value="ChequeNo">Cheque No.</option>
                                            <option class="opt_7" value="AccountCode">Account Code</option>
                                            <option class="opt_8" value="TaxType">Tax Type</option>
                                            <option class="opt_9" value="AnalysisCode">Analysis Code</option>
                                            <option class="opt_10" value="TrackingCategory1">Region</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr id="col_4">
                                    <td class="em columnName" title="Check Number">Check Number</td>
                                    <td class="dataValue">0</td>
                                    <td class="no-border">
                                        <select id="col_4" class="mapping">
                                            <option selected="selected">Unassigned</option>
                                            <option class="opt_0 assignedMapping" value="TransactionDate">Transaction Date</option>
                                            <option class="opt_1 assignedMapping" value="Amount">Transaction Amount</option>
                                            <option class="opt_2" value="Payee">Payee</option>
                                            <option class="opt_3 assignedMapping" value="Notes">Description</option>
                                            <option class="opt_4" value="Reference">Reference</option>
                                            <option class="opt_5" value="Type">Transaction Type</option>
                                            <option class="opt_6" value="ChequeNo">Cheque No.</option>
                                            <option class="opt_7" value="AccountCode">Account Code</option>
                                            <option class="opt_8" value="TaxType">Tax Type</option>
                                            <option class="opt_9" value="AnalysisCode">Analysis Code</option>
                                            <option class="opt_10" value="TrackingCategory1">Region</option>
                                        </select>
                                    </td>
                                </tr>
                                </tbody>
                            </table>


                            <div class="checkbox option">
                                <div><input name="firstRowImport" id="firstRowImport" type="checkBox"></div>
                                <label
                                        for="firstRowImport">Don't import the first line because they are column
                                    headings</label>
                            </div>
                        </fieldset>
                        <div class="actions">
                            <div class="right">
                                <a id="saveButton" class="successBtn" href="javascript:">Save</a>
                                <a class="cancelBtn">Cancel</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script>
    function changeEntryPosition(increment) {
        try {
            var currentPage = document.getElementById("currentPage");
            var currentPosition = Number(currentPage.innerHTML) + increment;
        } catch (e) {
            return;
        }
        var url = "reconciliation?position=" + currentPosition;
        var req = $.ajax({
            type: 'PUT',
            url: url,
            cache: false,
            type: "PUT",
            dataType: 'json',
            data: {position: currentPosition},
            success: function (data) {
                console.log(data);
                var currentPage = document.getElementById("currentPage");
                console.log(data);
                console.log("current page => ", currentPage);
                currentPage.innerHTML = data.position;
            },
            error: function (status) {
                console.log("Je suis erreur", status);
            }

        });
    }
</script>
</body>

</html>