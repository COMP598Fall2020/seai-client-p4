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

CREATE TABLE public.recommendations (
    id serial PRIMARY KEY,
    uid INT,
    mid INT,
    ranking_score FLOAT,
    batch_num INT,
    recommend_time TIMESTAMP,
    CONSTRAINT fk_movie
      FOREIGN KEY(mid) 
	    REFERENCES public.movies(id_long)
);