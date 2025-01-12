-- order matters
CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;
drop table if exists ratings;
drop table if exists sales;
drop table if exists customers;
drop table if exists creditcards;
drop table if exists genres_in_movies;
drop table if exists genres;
drop table if exists stars_in_movies;
drop table if exists stars;
drop table if exists movies;


create table movies (
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    director VARCHAR(100) NOT NULL
);
ALTER TABLE movies ADD FULLTEXT(title);


create table stars (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birthYear INT
);

create table stars_in_movies (
    starId VARCHAR(10) NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    PRIMARY KEY (starId, movieId),
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

create table genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL
);

create table genres_in_movies (
    genreId INT NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    PRIMARY KEY (genreId, movieId),
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

create table creditcards (
    id VARCHAR(20) PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    expiration DATE NOT NULL
);

create table customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccId VARCHAR(20),
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

create table sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customerId INT NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    saleDate DATE NOT NULL,
    FOREIGN KEY (customerId) REFERENCES customers(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

create table ratings (
    movieId VARCHAR(10) NOT NULL,
    rating FLOAT NOT NULL,
    numVotes INT NOT NULL,
    PRIMARY KEY (movieId),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);
