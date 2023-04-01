sessionStorage.setItem("jumpURL", window.location.pathname + window.location.search);

function handleMainPageResult(resultData) {
    let grid = jQuery("#grid");
    let quarterHTML = ""
    let prefix = "https://image.tmdb.org/t/p/w500";
    for (let i = 0; i < 16; i++) {
        // Concatenate the html tags with resultData jsonObject
        quarterHTML += `<div class= "w3-quarter">`;
        if (resultData[i]['poster'] === 'N/A')
            quarterHTML += `<th><a href="single-movie.html?id=${resultData[i]['movie_id']}"><img src="assets/no-poster.jpg" alt="${resultData[i]['title']}" width="300" height="410"></a></th>`;
        else
            quarterHTML += `<th><a href="single-movie.html?id=${resultData[i]['movie_id']}"><img src="${prefix + resultData[i]['poster']}" alt="${resultData[i]['title']}" width="300" height="410"></a></th>`;
        quarterHTML += "<h4>" + resultData[i]['title'] + "</h4>";
        quarterHTML += "<p>" + resultData[i]['genres'] + "</p>";
        quarterHTML += "<p>" + resultData[i]['year'] + "</p>";
        quarterHTML += "</div>";
    }
    grid.append(quarterHTML);
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/mainpg",
    success: (resultData) => handleMainPageResult(resultData)
});