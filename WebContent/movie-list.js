sessionStorage.setItem("jumpURL", window.location.pathname + window.location.search);

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
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

let Genre = getParameterByName('genre');
let Starting = getParameterByName('startingWith');
let Title = getParameterByName('title');
let Year = getParameterByName('year');
let Director = getParameterByName('director');
let Star = getParameterByName('star');
let Page = getParameterByName('page');
let Size = getParameterByName('size');
let Order = getParameterByName('order');
var Settings = jQuery("#settings");

function getUrl() {
    let ret = "";
    let flag = 0;
    if (Genre !== null && Genre !== "" && Genre !== "null") {
        ret += "genre=" + Genre;
        flag = 1;
    }
    if (Starting !== null && Starting !== "" && Starting !== "null") {
        ret += "startingWith=" + Starting;
        flag = 1;
    }
    if (Title !== null && Title !== "" && Title !== "null") {
        if (flag === 1) ret += "&";
        ret += "title=" + Title;
        flag = 1;
    }
    if (Year !== null && Year !== "" && Year !== "null") {
        if (flag === 1) ret += "&";
        ret += "year=" + Year;
        flag = 1;
    }
    if (Director !== null && Director !== "" && Director !== "null") {
        if (flag === 1) ret += "&";
        ret += "director=" + Director;
        flag = 1;
    }
    if (Star !== null && Star !== "" && Star !== "null") {
        if (flag === 1) ret += "&";
        ret += "star=" + Star;
        flag = 1;
    }
    if (Page !== null && Page !== "" && Page !== "null") {
        if (flag === 1) ret += "&";
        ret += "page=" + Page;
        flag = 1;
    }
    if (Size !== null && Size !== "" && Size !== "null") {
        if (flag === 1) ret += "&";
        ret += "size=" + Size;
        flag = 1;
    }
    if (Order !== null && Order !== "" && Order !== "null") {
        if (flag === 1) ret += "&";
        ret += "order=" + Order;
        flag = 1;
    }
    return ret;
}

let urlString = "api/movies?" + getUrl();


let movieTableBodyElement = jQuery("#movie_table_body");

function handleMovieResult(resultData) {
    if (resultData.length === 0 && Page > 1) {
        Page -= 1;
        return;
    }
    if (Page === null || Page === "" || Page === "null") Page = 1;
    $("#page-number").html(Page);

    let prefix = "https://image.tmdb.org/t/p/w500";

    movieTableBodyElement.empty();

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
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        // add genres
        rowHTML += "<th>";
        for (let j = 0; j < resultData[i]["genres"].length; j++) {
            rowHTML += `<a href="movie-list.html?genre=${resultData[i]['genres'][j]}">${resultData[i]["genres"][j]}</a>`;
            if (j !== resultData[i]["genres"].length - 1)
                rowHTML += ", "
        }
        rowHTML += "</th>";
        // add star_name with link
        rowHTML += "<th>";
        for (let j = 0; j < resultData[i]["star_id"].length; j++) {
            rowHTML += '<a href= "single-star.html?id=' + resultData[i]['star_id'][j] + '">' + resultData[i]["star_name"][j]
                + '</a>';
            if (j !== resultData[i]["star_id"].length - 1)
                rowHTML += ", "
        }
        rowHTML += "</th>";
        // add rating
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "<th>" + "$" + resultData[i]["price"] + "</th>";
        rowHTML += `<th><a href="javascript:addToCart('${resultData[i]['movie_id']}')" style="color: #329dd5">Add to Cart</a></th>`;
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }
    document.body.scrollTop = 0;
    document.documentElement.scrollTop = 0;
}

function addToCart(selectedId) {
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            "movieId": selectedId,
            "operation": "1"
        },
        success: alertSuccess
    });

}

function alertSuccess() {
    window.alert("Successfully added to your cart!");
}

function EditSetting(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    let Form = Settings.serializeArray();
    let typ1 = String(Form[1].value);
    let typ2 = String(Form[2].value);
    if (typ1.charAt(0) === typ2.charAt(0)) {
        alert("Error: Conflict instructions!");
    } else {
        Page = 1;
        $.ajax({
            url: urlString,
            method: "POST",
            data: Settings.serialize(),
            success: handleMovieResult
        });
    }
}

Settings.submit(EditSetting);

function flip(x) {
    if (Page === null || Page === "" || Page === "null") Page = 1;
    Page = parseInt(Page);
    Page += x;
    if (Page === 0) {
        Page = 1;
        return;
    }
    urlString = "api/movies?" + getUrl();
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        async: false,
        url: urlString,
        success: (resultData) => handleMovieResult(resultData)
    });
    let Parameters = window.location.search
    let index = Parameters.indexOf('&page');
    if(index !== -1){
        Parameters = Parameters.substring(0, index);
    }
    sessionStorage.setItem("jumpURL", window.location.pathname + Parameters + "&page=" + Page);
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: urlString,
    success: (resultData) => handleMovieResult(resultData)
});