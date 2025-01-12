/**
 * Handles the data returned by the API, reads the jsonObject, and populates data into HTML elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData, pageLength) {
    console.log("handleMovieResult: populating movie table from resultData");

    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th><a href='single-movie.html?id=" + resultData[i]["id"] + "'>" + resultData[i]["title"] + "</a></th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";

        // Generate genre links
        let genres = resultData[i]["genres"].split(', ');
        let genresHTML = genres.map(genre => `<a href='#' class='genre-link' data-genre='${genre}'>${genre}</a>`).join(', ');
        rowHTML += "<th>" + genresHTML + "</th>";

        // Generate star links
        let stars = resultData[i]["stars"].split(', ');
        let starsHTML = stars.map(star => {
            let [starId, starName] = star.split(':');
            return `<a href='single-star.html?id=${starId}'>${starName}</a>`;
        }).join(', ');
        rowHTML += "<th>" + starsHTML + "</th>";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "<th>$" + resultData[i]["price"] + "</th>";
        rowHTML += "<th class='quantity-header'><input type='number' id='quantity-"
            + resultData[i]["id"]
            + "' class='quantity-input' value='1' min='1'></th>";
        rowHTML += "<th><button class='add-to-cart-btn' data-movie-id='"
            + resultData[i]["id"]
            + "'>+</button></th>";

        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }

    if (resultData.length < pageLength) {
        jQuery("#next-page").prop("disabled", true);
    } else {
        jQuery("#next-page").prop("disabled", false);
    }

    if (currentPage === 1) {
        jQuery("#prev-page").prop("disabled", true);
    } else {
        jQuery("#prev-page").prop("disabled", false);
    }

    jQuery(".genre-link").click(function(event) {
        event.preventDefault();
        let genre = jQuery(this).data("genre");

        currentPage = 1;

        const encodedGenre = encodeURIComponent(genre);

        let genreParam = ""
        if (encodedGenre !== "") {
            genreParam = `&genre=${encodedGenre}`;
        }
        const url = `index.html?page=${currentPage}` +
            `&limit=${limit}` +
            `&sort=${sort}` +
            genreParam;
        window.location.href = url;
    });
}

/**
 * Fetches movies based on pagination, sorting, and optional genre filtering
 */
function fetchMovies(page, moviesPerPage, sortOption, genre = "", title = "",  director = "", year = "", star = "") {
    // Encode the genre parameter
    const encodedGenre = encodeURIComponent(genre);
    const encodedTitle = encodeURIComponent(title);
    const encodedYear = encodeURIComponent(year);
    const encodedDirector = encodeURIComponent(director);
    const encodedStar = encodeURIComponent(star);
    // console.log(encodedTitle);
    let genreParam = ""
    if (encodedGenre !== "") {
        genreParam = `&genre=${encodedGenre}`;
    }
    let titleParam = ""
    if (encodedTitle !== "") {
        titleParam = `&title=${encodedTitle}`;
    }
    let yearParam = ""
    if (encodedYear !== ""){
        yearParam = `&year=${encodedYear}`;
    }
    let directorParam = ""
    if (encodedDirector !== ""){
        directorParam = `&director=${encodedDirector}`;
    }
    let starParam = ""
    if (encodedStar !== ""){
        starParam = `&star=${encodedStar}`;
    }
    const url = `api/movie-list?page=${page}` +
                `&limit=${moviesPerPage}` +
                `&sort=${sortOption}` +
                genreParam +
                titleParam +
                yearParam +
                directorParam +
                starParam;
    console.log("Request URL:", url);
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: (resultData) => handleMovieResult(resultData, moviesPerPage)
    });
}

// Initialize with first page
let currentPage = parseInt(new URLSearchParams(window.location.search).get('page')) || 1;
let limit = parseInt(new URLSearchParams(window.location.search).get('limit')) || 10; // Default to 10 if not specified
let sort = new URLSearchParams(window.location.search).get('sort') || "rating-desc-title-asc"; // Default sort
let genre = new URLSearchParams(window.location.search).get('genre') || ""; // Default to empty if not specified
let title = new URLSearchParams(window.location.search).get('title') || ""; // Default to empty if not specified
let director = new URLSearchParams(window.location.search).get('director') || "";
let year = new URLSearchParams(window.location.search).get('year') || "";
let star = new URLSearchParams(window.location.search).get('star') || "";
console.log(`Calling fetchMovies with title=${title}, star=${star}, director=${director}, year=${year}`);
fetchMovies(currentPage, limit, sort, genre, title, director, year, star);

// Sort form handler
jQuery("#search-form").on("submit", function (event) {
    event.preventDefault();
    currentPage = 1; // Reset to the first page on sorting
    limit = jQuery("#num-results").val();
    sort = jQuery("#sort-option").val();

    fetchMovies(currentPage, limit, sort, genre, title, director, year, star);
});

// Pagination controls
jQuery("#prev-page").click(function() {
    if (currentPage > 1) {
        currentPage--;
        fetchMovies(currentPage, limit, sort, genre, title, director, year, star);
    }
});

jQuery("#next-page").click(function() {
    currentPage++;
    fetchMovies(currentPage, limit, sort, genre, title, director, year, star);
});

function showToast(message) {
    const toast = $("#toast-message");
    toast.text(message);
    toast.fadeIn(400).delay(2000).fadeOut(400); // Show for 2 seconds, then fade out
}

function addToCart(movieId, quantity) {
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/movie-list?action=addToCart",
        data: { movieId: movieId,
                quantity: quantity},
        success: (response) => {
            if (response["status"] === "success") {
                showToast("Success! Item added to cart.");
            } else {
                showToast("Failed to add item to cart.");
            }
        }
    });
}

jQuery(document).on("click", ".add-to-cart-btn", function () {
    const movieId = jQuery(this).data("movie-id");
    const quantity = jQuery(`#quantity-${movieId}`).val();

    addToCart(movieId, quantity);
});


