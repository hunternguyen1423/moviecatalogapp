// Function to fetch genres and dynamically create buttons
function loadGenres() {
    const genreList = $('#genre-list');
    genreList.html('Loading genres...');

    $.ajax({
        url: 'api/genres',  // Call to your GenreServlet endpoint
        method: 'GET',
        dataType: 'json',
        success: function (genres) {
            genreList.html('');  // Clear loading message
            genres.forEach(genre => {
                const genreButton = $('<button></button>')
                    .text(genre.name)
                    .on('click', function () {
                        //.location.href = `/index.html?genre=${encodeURIComponent(genre.name)}`;//`/movie-list?genre=${encodeURIComponent(genre.name)}`;
                        const contextPath = window.location.pathname.split('/')[1];
                        window.location.href = `/${contextPath}/index.html?genre=${encodeURIComponent(genre.name)}`;

                    });
                genreList.append(genreButton).append(' ');  // Space between buttons
            });
        },
        error: function (xhr, status, error) {
            console.error("Error loading genres:", error);
            genreList.html('Error loading genres. Please try again later.');
        }
    });
}

// Function to create A-Z and * title links
function loadTitleLinks() {
    const alphanumCharacters = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*'.split('');
    const titleList = $('#title-list');
    const contextPath = window.location.pathname.split('/')[1];
    alphanumCharacters.forEach(character => {
        const titleLink = $('<a></a>')
            .attr('href', `/${contextPath}/index.html?title=${encodeURIComponent(character)}`)
            .text(character);
        titleList.append(titleLink).append(' ');  // Space between links
    });
}

// Initialize the browse page
function initBrowsePage() {
    loadGenres();  // Load genres dynamically from the server
    loadTitleLinks();  // Generate A-Z and * links
}

document.getElementById("movieSearchForm").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent the default form submission

    // Retrieve values from form inputs
    const title = document.getElementById("title").value;
    const star = document.getElementById("star").value;
    const director = document.getElementById("director").value;
    const year = document.getElementById("year").value;
    //const page = 1;
    //const limit = 10;
    //const sort = document.getElementById("sort-option").value;

    // Construct the query string for redirection
    // const queryParams = new URLSearchParams({
    //     title: title,
    //     star: star,
    //     director: director,
    //     year: year
    // });
    let titleParam = ""
    if (title !== "") {
        titleParam = `&title=${title}`;
    }
    let yearParam = ""
    if (year !== ""){
        yearParam = `&year=${year}`;
    }
    let directorParam = ""
    if (director !== ""){
        directorParam = `&director=${director}`;
    }
    let starParam = ""
    if (star !== ""){
        starParam = `&star=${star}`;
    }
    // const url = "index.html?"
    //     + titleParam
    //     + yearParam
    //     + directorParam
    //     + starParam;

    // Redirect to index.html with the query parameters
    window.location.href = "index.html?"
        + titleParam
        + yearParam
        + directorParam
        + starParam;
});

$(document).ready(initBrowsePage);