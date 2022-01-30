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
                                <tbody id="entries"></tbody>
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
    var entries = [];
    entries.push(${ sessionScope.currentEntry });
    displayCurrentEntry(entries[0]);

    function changeEntryPosition(increment) {
        try {
            var currentPage = document.getElementById("currentPage");
            var currentPosition = Number(currentPage.innerHTML) + increment;
            if(currentPosition <= 0 || currentPosition > ${ sessionScope.entries.size() })
                return;
        } catch (e) {
            return;
        }
        var url = "reconciliation?position=" + currentPosition;
        $.ajax({
            type: 'PUT',
            url: url,
            cache: false,
            type: "PUT",
            dataType: 'json',
            success: function (data) {
                document.getElementById("entries").innerHTML = "";
                var currentPage = document.getElementById("currentPage");
                currentPage.innerHTML = data[0].value;
                if (!entries.map(entry => entry[0].value).includes(data[0].value))
                    entries.push(data);
                displayCurrentEntry(data);
                console.log("Je suis le tableau magique => ", entries);
            },
            error: function (status) {
                console.log("Je suis erreur", status);
            }

        });
    }

    function displayCurrentEntry(entry) {
        entry.shift();
        var tableBody = document.getElementById("entries");
        entry.forEach(ntry => {
            console.log("NTRY => ", ntry);
            tableBody.innerHTML+=
                    "<tr id='col_0'>"+
                    "<td class='em columnName' title='Date'>"+ntry.label+"</td>"+
                    "<td class='dataValue'>"+ntry.value+"</td>"+
                    "<td class='no-border'>"+
                        "<select id='col_0' class='mapping'>"+
                            "<option selected='selected'>Unassigned</option>"+
                            "<option class='opt_0"+getOptionStatus(entry, "transactionDate")+"' value='TransactionDate'>Transaction Date</option>"+
                            "<option class='opt_1"+getOptionStatus(entry, "transactionAmount")+"' value='Amount'>Transaction Amount </option>"+
                            "<option class='opt_2"+getOptionStatus(entry, "payee")+"' value='Payee'>Payee</option>"+
                            "<option class='opt_3"+getOptionStatus(entry, "description")+"' value='Notes'>Description</option>"+
                            "<option class='opt_4"+getOptionStatus(entry, "reference")+"' value='Reference'>Reference</option>"+
                            "<option class='opt_5"+getOptionStatus(entry, "analisysCode")+"' value='Type'>Transaction Type</option>"+
                            "<option class='opt_6"+getOptionStatus(entry, "checqueNumber")+"' value='ChequeNo'>Cheque No.</option>"+
                            "<option class='opt_7"+getOptionStatus(entry, "amountCode")+"' value='AccountCode'>Account Code</option>"+
                            "<option class='opt_8"+getOptionStatus(entry, "taxType")+"' value='TaxType'>Tax Type</option>"+
                            "<option class='opt_9"+getOptionStatus(entry, "analysisCode")+"' value='AnalysisCode'>Analysis Code</option>"+
                            "<option class='opt_10"+getOptionStatus(entry, "trackingCategory")+"' value='TrackingCategory1'>Region</option>"+
                        "</select>"+
                    "</td>"+
                "</tr>";
        });
    }

    function getOptionStatus(entry, option) {
        for (let i = 0; i < entry.length; i++) {
            let attribute = entry[i];
            if(attribute.targetColumn === option) {
                return ' selectedOption';
            }
        }
        return '';
    }
</script>
</body>

</html>