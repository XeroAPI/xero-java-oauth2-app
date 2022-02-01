function displayCurrentEntry(entry) {
    var tableBody = document.getElementById("entries");
    var previewBody = document.getElementById("previews");
    tableBody.innerHTML = "";
    previewBody.innerHTML = "";
    entry.forEach((ntry, index) => {
        tableBody.innerHTML +=
            "<tr id='col_" + index + "'>" +
            "<td class='em columnName' title='Date'>" + ntry.label + "</td>" +
            "<td class='dataValue'>" + ntry.value + "</td>" +
            "<td class='no-border'>" +
            "<select id='select_" + index + "' class='mapping' onchange='changeSelectedItem(this)'>" +
            "<option value='-1'>Unassigned</option>" +
            "<option class='opt_0" + getOptionStatus(entry, "transactionDate", ntry) + " value='transactionDate'>Transaction Date</option>" +
            "<option class='opt_1" + getOptionStatus(entry, "transactionAmount", ntry) + " value='transactionAmount'>Transaction Amount</option>" +
            "<option class='opt_2" + getOptionStatus(entry, "payee", ntry) + " value='payee'>Payee</option>" +
            "<option class='opt_3" + getOptionStatus(entry, "description", ntry) + " value='description'>Description</option>" +
            "<option class='opt_4" + getOptionStatus(entry, "reference", ntry) + " value='reference'>Reference</option>" +
            "<option class='opt_5" + getOptionStatus(entry, "transactionType", ntry) + " value='transactionType'>Transaction Type</option>" +
            "<option class='opt_6" + getOptionStatus(entry, "checqueNumber", ntry) + " value='checqueNumber'>Cheque No.</option>" +
            "<option class='opt_7" + getOptionStatus(entry, "amountCode", ntry) + " value='amountCode'>Account Code</option>" +
            "<option class='opt_8" + getOptionStatus(entry, "taxType", ntry) + " value='taxType'>Tax Type</option>" +
            "<option class='opt_9" + getOptionStatus(entry, "analysisCode", ntry) + " value='analysisCode'>Analysis Code</option>" +
            "<option class='opt_10" + getOptionStatus(entry, "trackingCategory", ntry) + " value='trackingCategory'>Region</option>" +
            "</select>" +
            "</td>" +
            "</tr>";
    });

    previewBody.innerHTML +=
        "<tbody>"+
        "<tr class='"+getOptionStatus(entry, "transactionDate", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Transaction Date</td>"+
        "<td>"+getPreviewValue(entry, "transactionDate")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "transactionAmount", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Transaction Amount</td>"+
        "<td>"+getPreviewValue(entry, "transactionAmount")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "payee", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Payee</td>"+
        "<td>"+getPreviewValue(entry, "payee")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "description", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Description</td>"+
        "<td>"+getPreviewValue(entry, "description")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "reference", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Reference</td>"+
        "<td>"+getPreviewValue(entry, "reference")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "transactionType", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Transaction Type</td>"+
        "<td>"+getPreviewValue(entry, "transactionType")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "checqueNumber", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Cheque No.</td>"+
        "<td>"+getPreviewValue(entry, "checqueNumber")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "amountCode", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Account Code</td>"+
        "<td>"+getPreviewValue(entry, "amountCode")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "taxType", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Tax Type</td>"+
        "<td>"+getPreviewValue(entry, "taxType")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "analysisCode", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Analysis Code</td>"+
        "<td>"+getPreviewValue(entry, "analysisCode")+"</td>"+
        "</tr>"+
        "<tr class='"+getOptionStatus(entry, "trackingCategory", null, true)+"'>"+
        "<td class='item'><em class='icons'>&nbsp;</em>Region</td>"+
        "<td>"+getPreviewValue(entry, "trackingCategory")+"</td>"+
        "</tr>"+
        "</tbody>";
}

function getOptionStatus(entry, option, ntry, preview = false) {
    for (let i = 0; i < entry.length; i++) {
        let attribute = entry[i];
        if (attribute.targetColumn === option) {
            var response = " selectedOption' disabled";
            if(ntry != null)
                if(ntry.targetColumn === option)
                    response += " selected";
            return !preview ? response : "matched";
        }
    }
    return !preview ? "'" : "unmatched";
}

function getPreviewValue(entry, option) {
    for (let i = 0; i < entry.length; i++) {
        let attribute = entry[i];
        if (attribute.targetColumn === option) {
            return attribute.value;
        }
    }
    return "Unassigned";
}

function displayAccounts(accounts) {
    var accountSelect = document.getElementById("accounts");
    accountSelect.innerHTML = "<option></option>";
    accounts.forEach(account => {
        accountSelect.innerHTML += "<option value='"+account.accountID+"'>"+account.name+"</option>";
    })
}

function changeSelectedItem(e) {
    currentEntry[Number(e.getAttributeNode("id").value.split('_')[1])].targetColumn = e.value !== '-1' ? e.value : null ;
    entries[Number(document.getElementById("currentPage").innerHTML) - 1] = currentEntry;
    displayCurrentEntry(currentEntry);
}