sessionStorage.setItem("jumpURL", window.location.pathname + window.location.search);

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");
    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

let id = getParameterByName('movieId');
let op = getParameterByName('operation');

let movieTableBodyElement = jQuery("#movie_table_body");
function handleMovieResult(resultData) {

    let prefix = "https://image.tmdb.org/t/p/w500";

    movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();
    let Total = 0;

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        if (resultData[i]['poster'] === 'N/A')
            rowHTML += `<th><a href="single-movie.html?id=${resultData[i]['movie_id']}"><img src="assets/no-poster.jpg" alt="${resultData[i]['title']}" width="100"></a></th>`;
        else
            rowHTML += `<th><a href="single-movie.html?id=${resultData[i]['movie_id']}"><img src="${prefix + resultData[i]['poster']}" alt="${resultData[i]['title']}" width="100"></a></th>`;

        rowHTML += `<th><a href="single-movie.html?id=${resultData[i]['movie_id']}">${resultData[i]["title"]}</a></th>`;

        // Add quantity modifiers
        rowHTML += `<th><a href="javascript:modifyQuantity('${resultData[i]['movie_id']}', -1)">&laquo;</a>
            ${resultData[i]['quantity']}
            <a href="javascript:modifyQuantity('${resultData[i]['movie_id']}', 1)">»</a></th>`;

        rowHTML += "<th>" + "$" + resultData[i]["price"] + "</th>";
        rowHTML += `<th><a href="javascript:modifyQuantity('${resultData[i]['movie_id']}', -${resultData[i]['quantity']})">Remove</a></th>`;
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);

        Total += resultData[i]["price"];
    }
    $("#total-price").html("$" + Total.toFixed(2));
}

// Triggered by '《' and '》'
function modifyQuantity(movieId, amount) {
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data:{
            "movieId": movieId,
            "operation": amount
        },
        success: (resultData) => handleMovieResult(resultData)
    });
}

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/cart",
    success: (resultData) => handleMovieResult(resultData)
});