sessionStorage.setItem("jumpURL", window.location.pathname + window.location.search);

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    //  check past query results first
    if(query.length < 3) {
        var data = JSON.parse(sessionStorage.getItem("History"));
        handleLookupAjaxSuccess(data, query, doneCallback);
        return;
    }
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/autocomplete?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    var jsonData = data;
    if(jsonData === null)jsonData = [];
    console.log(jsonData);

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}

function handleSelectSuggestion(suggestion){
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"]);
    var history = sessionStorage.getItem("History");
    if(history === null) {
        history = [];
    } else {
        history = JSON.parse(history);
    }
    history = history.filter(item => item["data"]["movieId"] !== suggestion["data"]["movieId"]);
    history.push(suggestion);
    if(history.length >= 10)history.shift();
    sessionStorage.setItem("History", JSON.stringify(history.reverse()));
    window.location.href = "single-movie.html?id=" + suggestion["data"]["movieId"];
}

$('#input-title').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    minLength: 0
});
