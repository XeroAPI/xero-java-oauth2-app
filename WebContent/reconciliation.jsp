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
                <h2>
                    Select account to feed
                </h2>
                <select class="accounts" id="accounts" onchange='changeAccount(this)'>
                    <option></option>
                    <option>Bank Main Accounts</option>
                    <option>Bank Savings Accounts</option>
                </select>
                <div class="file">
                    <h2>
                        Statement lines imported from your file...
                    </h2>
                    <div class="form">
                        <div class="entries">
                            <span>Statement line <i
                                    id="currentPage">1</i> of <i>${ sessionScope.size }</i> </span>
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
                                <div><input name="firstRowImport" id="firstRowImport" type="checkBox" checked></div>
                                <label
                                        for="firstRowImport">Don't import the first line because they are column
                                    headings</label>
                            </div>
                        </fieldset>
                        <div class="actions">
                            <div class="right" id="buttons">
                                <a onclick="submitForm();" id="saveButton" class="successBtn" href="javascript:">
                                    Save
                                </a>
                                <a onclick="window.location = './import-file'" class="cancelBtn">Cancel</a>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="previews">
                    <h2>
                        Based on the statement line options you have assigned...
                    </h2>
                    <div id="mapping">
                        <table class="previews" id="previews"></table>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="./js/reconciliation.js"></script>
<script>
    var entries = ${ sessionScope.entries };
    var accounts = ${ sessionScope.accounts };
    var currentEntry = entries[0];
    var account = null;

    displayCurrentEntry(currentEntry);
    displayAccounts(accounts);

    function changeEntryPosition(increment) {
        try {
            var currentPage = document.getElementById("currentPage");
            var currentPosition = Number(currentPage.innerHTML) + increment;
            if (currentPosition <= 0 || currentPosition > ${ sessionScope.size })
                return;
        } catch (e) {
            return;
        }

        document.getElementById("entries").innerHTML = "";
        currentPage.innerHTML = currentPosition;
        currentEntry = entries[currentPosition - 1];
        displayCurrentEntry(currentEntry);
    }

    function changeAccount(e) {
        if (e.value === '-1')
            account = null;
        else
            account = e.value;
    }

    function submitForm() {
        if (!account) {
            alert('Please select the account to feed');
            return;
        }
        document.getElementById("buttons").innerHTML = "<a class='successBtn'><div class='loader'></div></a>";
        var url = "reconciliation?accountID="+account;
        $.ajax({
            type: 'POST',
            url: url,
            type: "POST",
            dataType: 'json',
            data: JSON.stringify(entries),
            success: function (data) {
                document.getElementById("buttons").innerHTML =
                    "<a onclick='submitForm();' id='saveButton' class='successBtn'>Save</a>" +
                    "<a href='./import-file' class='cancelBtn'>Cancel</a>";
            },
            error: function (status) {
                document.getElementById("buttons").innerHTML =
                    "<a onclick='submitForm();' id='saveButton' class='successBtn'>Save</a>" +
                    "<a href='./import-file' class='cancelBtn'>Cancel</a>"
            }
        });
    }
</script>
</body>

</html>