DELIMITER $$

CREATE PROCEDURE add_movie(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_genre_name VARCHAR(32),
    IN gen_movie_id VARCHAR(10),
    IN opt_star_id VARCHAR(10)
)
BEGIN
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;

    SELECT id INTO movie_id
    FROM movies
    WHERE title = p_title AND year = p_year and director = p_director;

    IF movie_id IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Duplicate movie not allowed';
    END IF;
    
    SET movie_id = gen_movie_id;

    INSERT INTO movies (id, title, year, director, price)
    VALUES (movie_id, p_title, p_year, p_director, FLOOR(3 + (RAND() * 13)));

    SELECT id INTO star_id
    FROM stars
    WHERE name = p_star_name;

    IF star_id IS NULL THEN
        INSERT INTO stars (id, name)
        VALUES (opt_star_id, p_star_name);
        SELECT id INTO star_id FROM stars WHERE name = p_star_name;
    END IF;

    SELECT id INTO genre_id
    FROM genres
    WHERE name = p_genre_name;

    IF genre_id IS NULL THEN
        INSERT INTO genres (name)
        VALUES (p_genre_name);
        SELECT id INTO genre_id FROM genres WHERE name = p_genre_name;
    END IF;

    INSERT INTO stars_in_movies (starId, movieId)
    VALUES (star_id, movie_id);

    INSERT INTO genres_in_movies (genreId, movieId)
    VALUES (genre_id, movie_id);

END $$

DELIMITER ;
