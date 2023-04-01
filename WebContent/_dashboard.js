let add_movie_form = $("#add-movie-form");
let add_star_form = $("#add-star-form");

function loadMetadata(resultData) {
    let metadataGroup = $("#metadata-group");
    let str = "";
    for (let i = 0; i < resultData.length; i++) {
        str += `<div class="panel panel-default">
        <div class="panel-heading" role="tab" id="heading${i}">
          <h4 class="panel-title">
            <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapse${i}" aria-expanded="false" aria-controls="collapseTwo">
              ${resultData[i]['table_name']}
            </a>
          </h4>
        </div>
        <div id="collapse${i}" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading${i}">
          <table class="table table-hover">`;
        for (let j = 0; j < resultData[i]['column_info'].length; j++) {
            str += `<tr>`;
            str += `<td>${j + 1}</td>`;
            str += `<td>${resultData[i]['column_info'][j]['column_name']}</td>`;
            str += `<td>${resultData[i]['column_info'][j]['data_type']}</td>`;
            str += `</tr>`;
        }
        str += `</table></div></div>`;
    }
    metadataGroup.append(str);
    console.log(resultData);
}

function handleMessage(resultData) {
    let resultDataJson = JSON.parse(resultData);
    alert(resultDataJson["message"]);
}

function submitAddMovieForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/add-movie", {
            method: "POST",
            data: add_movie_form.serialize(),
            success: handleMessage
        }
    );
}

function submitAddStarForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/add-star", {
            method: "POST",
            data: add_star_form.serialize(),
            success: handleMessage
        }
    );
}


add_movie_form.submit(submitAddMovieForm);
add_star_form.submit(submitAddStarForm);

$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/metadata",
    success: (resultData) => loadMetadata(resultData)
});
