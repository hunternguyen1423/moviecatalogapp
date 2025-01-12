ALTER TABLE movies ADD COLUMN price INT DEFAULT 0;

SET SQL_SAFE_UPDATES = 0;

UPDATE movies m
JOIN (
	SELECT m.id AS movieId, FLOOR(3 + (RAND() * (15 - 3 + 1))) AS price
    FROM movies m
) random_movie_prices ON m.id = random_movie_prices.movieId
SET m.price = random_movie_prices.price;

SET SQL_SAFE_UPDATES = 1;