// Load the navbar HTML into the page
document.addEventListener("DOMContentLoaded", function() {
    fetch("navbar.html")
        .then(response => response.text())
        .then(data => {
            document.getElementById("navbar").innerHTML = data;
            initializeSearch();
            updateMovieLink();
        })
        .catch(error => console.error('Error loading navbar:', error));
});

function handle(resultData) {
    let sort = resultData["sort"];
    let page = resultData["page"];
    let limit = resultData["limit"];
    let genre = resultData["genre"];
    let title = resultData["title"];
    let year = resultData["year"];
    let director = resultData["director"]
    let star = resultData["star"]

    // Construct the dynamic URL
    const encodedGenre = encodeURIComponent(genre);
    const encodedTitle = encodeURIComponent(title);
    const encodedYear = encodeURIComponent(year);
    const encodedDirector = encodeURIComponent(director);
    const encodedStar = encodeURIComponent(star);
    let genreParam = ""
    if (genre !== "" && genre != null) {
        genreParam = `&genre=${encodedGenre}`;
    }
    let titleParam = ""
    if (title !== "" && title != null) {
        titleParam = `&title=${encodedTitle}`;
    }
    let yearParam = ""
    if (year !== "" && year != null){
        yearParam = `&year=${encodedYear}`;
    }
    let directorParam = ""
    if (director !== "" && director != null){
        directorParam = `&director=${encodedDirector}`;
    }
    let starParam = ""
    if (star !== "" && star != null){
        starParam = `&star=${encodedStar}`;
    }
    const movieLink = `index.html?page=${page}` +
        `&limit=${limit}` +
        `&sort=${sort}` +
        genreParam +
        titleParam +
        yearParam +
        directorParam +
        starParam;
    // const movieLink = `index.html?page=${page}&limit=${limit}&sort=${sort}&genre=${genre}`;
    console.log(movieLink);
    document.getElementById("dynamic-movie-link").href = movieLink;
}

// Function to update the dynamic movie link
function updateMovieLink() {
    // Retrieve session data (for example, using a GET request to an endpoint)
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: 'api/session-data',
        success: (resultData) => handle(resultData)
    });
}

updateMovieLink();

function initializeSearch() {
    const searchInput = document.getElementById("autocomplete");
    const searchButton = document.getElementById("search-button");

    let selectedIndex = -1; // Index for keyboard navigation

// Autocomplete query handler
    function handleLookup(query, doneCallback) {
        if (sessionStorage.getItem(query)) {
            console.log("Using cached results for query:", query);
            handleLookupAjaxSuccess(JSON.parse(sessionStorage.getItem(query)), query, doneCallback);
        } else {
            console.log("Sending AJAX request for query:", query);
            jQuery.ajax({
                method: "GET",
                dataType: "json",
                url: `api/autocomplete?query=${encodeURIComponent(query)}`,
                success: (data) => {
                    console.log("Received suggestions:", data);
                    handleLookupAjaxSuccess(data, query, doneCallback);
                },
                error: (error) => console.error("Error fetching autocomplete data:", error)
            });
        }
    }

// Display suggestions in the dropdown
    function handleLookupAjaxSuccess(data, query, doneCallback) {
        console.log("lookup ajax successful")

        console.log(data)

        sessionStorage.setItem(query, JSON.stringify(data));

        // call the callback function provided by the autocomplete library
        // add "{suggestions: jsonData}" to satisfy the library response format according to
        //   the "Response Format" section in documentation
        doneCallback( { suggestions: data } );
    }

// Handle suggestion selection
    function handleSelectSuggestion(suggestion) {
        console.log("Selected suggestion:", suggestion["value"]);
        window.location.href = `single-movie.html?id=${suggestion["data"]["movieId"]}`;
    }


    $('#autocomplete').autocomplete({
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
        // TODO: add other parameters, such as minimum characters
        minChars: 3,
        lookupLimit: 10
    });


// Full-text search handler
    function handleFullTextSearch(query) {
        console.log("Performing full-text search for:", query);
        window.location.href = `index.html?title=${encodeURIComponent(query)}`;
    }

// Perform full-text search on button click or enter key
    searchButton.addEventListener("click", () => {
        handleFullTextSearch(searchInput.value.trim());
    });

    searchInput.addEventListener("keypress", (event) => {
        if (event.key === "Enter" && selectedIndex === -1) {
            handleFullTextSearch(searchInput.value.trim());
        }
    });
}