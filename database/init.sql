CREATE TABLE public.recommendations (
    rid serial PRIMARY KEY,
    uid INT NOT NULL,
    recommendations VARCHAR(500),
    score VARCHAR(500),
    recommend_time TIMESTAMP
);

CREATE TABLE public.performance (
    id serial PRIMARY KEY,
    rmse FLOAT,
    precision FLOAT,
    recall FLOAT,
    f1 FLOAT,
    time TIMESTAMP
);

CREATE TABLE public.users (
	user_id serial PRIMARY KEY,
	user_name VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE public.movies (
    id serial PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    id_long INT UNIQUE NOT NULL,
    imdb_id VARCHAR(50),
    title VARCHAR(50)
);

CREATE TABLE public.ratings (
    rid serial PRIMARY KEY,
    uid INT,
    mid INT,
    rating FLOAT,
    rating_time TIMESTAMP,
    CONSTRAINT fk_movie
      FOREIGN KEY(mid)
	    REFERENCES public.movies(id_long)
);

