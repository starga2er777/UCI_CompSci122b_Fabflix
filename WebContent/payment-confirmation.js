function updateTable(updateData) {
    let resultDataJson = JSON.parse(updateData);
    console.log(resultDataJson);
    let totalPrice = 0.0;
    let confirmationTable = $("#confirmation_table_body");
    for (let i = 0; i < resultDataJson.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += `<th>${resultDataJson[i]["saleId"]}</th>`;
        rowHTML += `<th>${resultDataJson[i]["title"]}</th>`;
        rowHTML += `<th>${resultDataJson[i]["amount"]}</th>`;
        rowHTML += `<th>${resultDataJson[i]["price"] * resultDataJson[i]["amount"]}</th>`;
        rowHTML += "</tr>";
        totalPrice += resultDataJson[i]["price"] * resultDataJson[i]["amount"];
        confirmationTable.append(rowHTML);
    }
    $("#total-price").html(`Total Price: $${totalPrice}`);
}


$.ajax(
    "api/payment", {
        method: "PUT",
        success: updateTable
    }
);


