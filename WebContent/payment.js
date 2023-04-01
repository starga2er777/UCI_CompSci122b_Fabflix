let payment_form = $("#payment-form");

function handlePaymentResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    if (resultDataJson["status"] === "success") {
        $("#message_box").html(
            `<div class="alert alert-success"><strong>Payment Success!</strong> Your order is being processed.</div>`
        );
        window.location.href = "payment-confirmation.html";
    } else {
        $("#message_box").html(
            `<div class="alert alert-warning"><strong>Payment Failed.</strong> ${resultDataJson["message"]}</div>`
        );
    }
}

function submitCreditCardForm(formSubmitEvent) {
    console.log("submit login form");
    console.log(payment_form.serialize());

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/payment", {
            method: "POST",
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}

payment_form.submit(submitCreditCardForm);

