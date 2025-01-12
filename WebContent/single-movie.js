/**
 * Handles the data returned by the API, reads the jsonObject, and populates data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating single movie details from resultData");


    let movie_title = jQuery("#movie-title");

    movie_title.append("<p>" + resultData["title"] + "</p>");

    // movie_title.append("<button id='backToMovieList'>Back to Movie List</button>");
    //
    // let sort = resultData["sort"];
    // let page = resultData["page"];
    // let limit = resultData["limit"];
    // let genre = resultData["genre"];
    //
    // jQuery("#backToMovieList").on("click", function() {
    //     window.location.href = `index.html?page=${page}&limit=${limit}&sort=${sort}&genre=${genre}`;
    //     // window.location.href = `index.html`;
    // });

    jQuery("#movie-year").text(resultData["year"]);
    jQuery("#movie-director").text(resultData["director"]);
    // jQuery("#movie-genres").text(resultData["genres"]);
    console.log(resultData["genres"]);
    let genres = resultData["genres"].split(', ');
    let genresHTML = genres.map(genre => `<a href='index.html?page=1&limit=10&sort=rating-desc-title-asc&genre=${genre}'>${genre}</a>`).join(', ');
    jQuery("#movie-genres").html(genresHTML);


    let starsHtml = "";
    resultData["stars"].forEach(star => {
        starsHtml += `<a href="single-star.html?id=${star.id}">${star.name}</a>, `;
    });


    starsHtml = starsHtml.slice(0, -2);

    jQuery("#movie-stars").html(starsHtml);
    jQuery("#movie-rating").text(resultData["rating"]);
    jQuery("#movie-price").text(`$${resultData["price"]}`);

    let cartHTML = "<div class='cart-quantity-header'>"
        + "<p4>ADD TO CART</p4><br>"
        + "<input type='number' id='cart-quantity-" + resultData["id"]
        + "' class='cart-quantity-input' value='1' min='1'>"
        + "<button class='add-quantity-btn' data-movie-id='" + resultData["id"]
        + "'>+</button></div>";
    jQuery("#single-movie-page").append(cartHTML);
}

/**
 * Helper function to get the 'id' parameter from the URL
 */
function getParameterByName(name) {
    let url = window.location.href;
    name = name.replace(/[\[\]]/g, '\\$&');
    let regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
    let results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

/**
 * Once this .js is loaded, the following scripts will be executed by the browser
 */

// Get the movie ID from the URL
let movieId = getParameterByName("id");

// If the movie ID is found in the URL, fetch the movie details
if (movieId) {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movie?id=" + movieId,
        success: (resultData) => handleMovieResult(resultData),
        error: (error) => {
            console.log("Error fetching movie details: ", error);
            jQuery("#movie-details").text("Error fetching movie details.");
        }
    });
} else {
    jQuery("#movie-details").text("No movie ID found in the URL.");
}

$(document).on('click', '.add-quantity-btn', function() {
    let movieId = $(this).data('movie-id');
    let quantity = $('#cart-quantity-' + movieId).val();

    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/movie?action=addToCart",
        data: {
            movieId: movieId,
            quantity: quantity
        },
        success: function(response) {
            if (response["status"] === "success") {
                showToast("Success! Item added to cart.");
            } else {
                showToast("Failed to add item to cart.");
            }
        }
    });
});

function showToast(message) {
    const toast = $("#toast-message");
    toast.text(message);
    toast.fadeIn(400).delay(2000).fadeOut(400); // Show for 2 seconds, then fade out
}
