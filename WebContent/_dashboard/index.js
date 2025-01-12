$(document).ready(function() {
    // Function to handle form submission for adding a new star
    $("#insertStarForm").submit(function(event) {
        event.preventDefault();
        const starName = $("#starName").val();
        const birthYear = $("#birthYear").val();

        console.log(starName);
        console.log(birthYear);

        $.ajax({
            url: "api/addStar?action=check",
            method: "POST",
            dataType: "json",
            data: { starName: starName,
                birthYear: birthYear
            },
            success: function(data) {
                alert(data.message);
            },
            error: function(xhr, status, error) {
                console.error("Error adding star:", error);
            }
        });
    });

    // Function to fetch metadata of the database
    function fetchMetadata() {
        $.ajax({
            url: "api/fetchMetadata",
            method: "GET",
            dataType: 'json',
            success: function(data) {
                $("#metadata").text(JSON.stringify(data, null, 2));
            },
            error: function(xhr, status, error) {
                console.error("Error fetching metadata:", error);
            }
        });
    }

    // Function to handle form submission for adding a new movie
    $("#addMovieForm").submit(function(event) {
        event.preventDefault();
        const movieTitle = $("#movieTitle").val();
        const releaseYear = $("#releaseYear").val();
        const starNameForMovie = $("#starNameForMovie").val();
        const directorNameForMovie = $("#directorNameForMovie").val();
        const genreName = $("#genreName").val();

        $.ajax({
            url: "api/addMovie",
            method: "POST",
            dataType: "json",
            data: { movieTitle: movieTitle,
                releaseYear: releaseYear,
                directorNameForMovie: directorNameForMovie,
                starNameForMovie: starNameForMovie,
                genreName: genreName
            },
            success: function(data) {
                alert(data.message);
            },
            error: function(xhr, status, error) {
                console.error("Error adding movie:", error);
            }
        });
    });

    // Fetch metadata when the page loads
    $("#showMetadataButton").click(function () {
        fetchMetadata();
    });
});
