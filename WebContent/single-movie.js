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

// Get id from URL
let movieId = getParameterByName('id');

function AddToCart(){
    $.ajax({
        method: "POST",
        url: "api/cart",
        data:{
            "movieId": movieId,
            "operation": "1"
        }
    });
    window.alert("Successfully added to your cart!");
}

function handleResult(resultData) {
    document.title = resultData[0]['title'] + " - Fabflix";

    let movieInfoElement = jQuery("#movie_info");

    if (resultData[0]['poster'] === "N/A") {
        $("#left-column").html(`<img src="assets/no-poster.jpg" alt="${resultData[0]['title']}">`);
    } else {
        $("#left-column").html(`<img src="https://image.tmdb.org/t/p/w500${resultData[0]['poster']}" alt="${resultData[0]['title']}">`);
    }
    if (resultData[0]['overview'] === "N/A")
        $("#movie-story").html("No overview.");
    else
        $("#movie-story").html(resultData[0]['overview']);


    $("#movie-title").html(resultData[0]["title"]);

    $("#movie-rating").html(resultData[0]["rating"])

    $("#movie-director").html(resultData[0]["director"]);

    $("#movie-year").html(resultData[0]['year']);



    let genre_data = "", star_data = "", price_data = "$" + resultData[0]['price'];
    for (let i = 0; i < resultData[0]['genres'].length; i++) {
        if(resultData[0]['genres'][i] !== "N/A"){
            genre_data += `<a href="movie-list.html?genre=${resultData[0]['genres'][i]}">${resultData[0]['genres'][i]}</a>`;
        } else {
            genre_data += `<a>${resultData[0]['genres'][i]}</a>`;
        }
        if (i !== resultData[0]["genres"].length - 1)
            genre_data += ", "
    }
    $("#movie-genres").html(genre_data);

    for (let i = 0; i < resultData[0]['star_id'].length; i++) {
        if(resultData[0]['star_id'][i] !== "N/A"){
            star_data += `<a href="single-star.html?id=${resultData[0]['star_id'][i]}">${resultData[0]['star_name'][i]}</a>`;
        } else {
            star_data += `<a>${resultData[0]['star_name'][i]}</a>`;
        }
        if (i !== resultData[0]["star_id"].length - 1)
            star_data += ", "
    }
    $("#movie-stars").html(star_data);

    $("#price_info").html(price_data);

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Year: " + resultData[0]["year"] + "</p>" +
        "<p>Director: " + resultData[0]["director"] + "</p>" +
        "<p>Rating: " + resultData[0]["rating"] + "</p>");
    movieInfoElement.append(`<p>Stars: `);
    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        movieInfoElement.append(`<a href="single-star.html?id=${resultData[i]['star_id']}">${resultData[i]['star_name']}</a>`);
        if (i !== resultData.length - 1)
            movieInfoElement.append(", ")
    }

    movieInfoElement.append(`</p>`);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */



// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});