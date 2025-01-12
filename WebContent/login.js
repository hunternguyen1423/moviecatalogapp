let loginForm = $("#loginForm");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject
 */
function handleLoginResult(resultDataJson) {
    // No need to parse the JSON again
    console.log("handleLoginResult called with response:", resultDataJson);  // Log the raw response

    console.log("Parsed JSON:", resultDataJson);
    console.log("Response status:", resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        console.log("Login successful. Redirecting to index.html");
        window.location.replace("browse.html");
    } else {
        console.log("Login failed with message:", resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submitLoginForm called");
    formSubmitEvent.preventDefault();

    let baseUrl = window.location.origin + window.location.pathname.split('/').slice(0, 2).join('/');
    console.log("Base URL:", baseUrl);

    $.ajax({
        url: baseUrl + "/api/login",
        method: "POST",
        data: loginForm.serialize(),
        success: handleLoginResult,  // No need to parse in handleLoginResult
        error: function(jqXHR, textStatus, errorThrown) {
            console.error("AJAX error:", textStatus, errorThrown);
            $("#login_error_message").text("Login request failed. Please try again.");
        }
    });
}

// Bind the submit action of the form to a handler function
loginForm.submit(submitLoginForm);
