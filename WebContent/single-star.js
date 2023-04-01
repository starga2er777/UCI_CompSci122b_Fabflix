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


function handleResult(resultData) {
    document.title = resultData[0]['movie_title'] + " - Fabflix";
    let api_key = "44910f74d917dd9e37e43b9c0e5881fd";
    let apiUrl = "https://api.themoviedb.org/3/search/person?api_key=" + api_key + "&search_type=ngram&query=" + resultData[0]['star_name'];

    let movieElement = jQuery("#star-movie");
    let posterElement = jQuery("#left-column");
    $.ajax({
        method: 'GET',
        url: apiUrl,
        async: false,
        success: function (data) {
            console.log(data);
            if (data.results.length === 0 || !data.results[0].hasOwnProperty('profile_path') || data['results'][0]['profile_path'] === null)
                posterElement.append(`<img src="assets/no-photo.png" alt="${resultData[0]['star_name']}">`);
            else
                posterElement.append(`<img src="https://image.tmdb.org/t/p/w500${data['results'][0]['profile_path']}" alt="${resultData[0]['star_name']}">`);
        },
        error: function () {
            posterElement.append(`<th><img src="assets/no-photo.png" alt="${resultData[0]['star_name']}"></th>`);
        }
    });
    $("#star-name").html(resultData[0]["star_name"]);
    $("#star-year").html(resultData[0]["star_dob"]);

    for (let i = 0; i < resultData.length; i++) {
        movieElement.append(`<a href="single-movie.html?id=${resultData[i]['movie_id']}">${resultData[i]['movie_title']}</a>`);
        if (i !== resultData.length - 1)
            movieElement.append(", ")
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});