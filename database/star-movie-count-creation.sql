ALTER TABLE stars ADD COLUMN movie_count INT DEFAULT 0;

UPDATE stars s
JOIN (
    SELECT sim.starId, COUNT(sim.movieId) AS movie_count
    FROM stars_in_movies sim
    GROUP BY sim.starId
) star_movie_count ON s.id = star_movie_count.starId
SET s.movie_count = star_movie_count.movie_count;