drop procedure if exists add_movie;
delimiter //
CREATE PROCEDURE add_movie(IN title varchar(100),
                           year integer,
                           director varchar(100),
                           genre varchar(32),
                           star_name varchar(100),
                           star_birth_year integer
)
my_label:
BEGIN
    declare temp integer;
    declare new_movie_id varchar(10);
    declare star_id varchar(10);
    declare genre_id integer;

#     if the movie exists, exit function:
    if exists(select *
              from movies
              where movies.title = title
                and movies.year = year
                and movies.director = director) then
        select concat('Failed. Movie \'', title, '\' already existed.') as message;
        leave my_label;
    end if;

#     set genre id (if doesn't exist, create new genre type)
    if exists(select * from genres where genres.name = genre) then
        set genre_id = (select id from genres where name = genre limit 1);
    else
        set temp = (select max(id) from genres) + 1;
        insert into genres values (temp, genre);
        set genre_id = (select max(id) from genres);

    end if;

#     set new movieId
    set temp = (select count(*) from movies where movies.id like 'Dash%') + 1;
    set new_movie_id = concat('Dash', cast(temp as char));

#     set star id (if doesn't exist, create a new star)
    if exists(select *
              from stars
              where lower(stars.name) = lower(star_name)
                and stars.birthYear = star_birth_year) then
        set star_id = (select id from stars where lower(stars.name) = lower(star_name) limit 1);
    else
        set temp = (select count(*) from stars where stars.id like 'Dash%') + 1;
        set star_id = concat('Dash', cast(temp as char));
        insert into stars values (star_id, star_name, star_birth_year);
    end if;


#     insert into movie table
    insert into movies values (new_movie_id, title, year, director);
#     insert into genres_in_movies
    insert into genres_in_movies values (genre_id, new_movie_id);
#     insert into stars_in_movies
    insert into stars_in_movies values (star_id, new_movie_id);
#     insert price
    if (year > 2008) then
        insert into price_of_movies values (new_movie_id, 24.99);
    else
        insert into price_of_movies values (new_movie_id, 19.99);
    end if;
#     insert into poster
    insert into poster values (new_movie_id, 'N/A', 'N/A');

    select concat('Success. Movie ID: ', new_movie_id, ', Genre ID: ', genre_id, ', Star ID: ', star_id, '.') as message;
END; //

delimiter ;
drop procedure if exists add_star;
delimiter //
CREATE PROCEDURE add_star(IN
                              star_name varchar(100),
                          star_birth_year integer
)
BEGIN
    declare temp integer;
    declare star_id varchar(10);

    set temp = (select count(*) from stars where stars.id like 'Dash%') + 1;
    set star_id = concat('Dash', cast(temp as char));
    insert into stars values (star_id, star_name, star_birth_year);
    select concat('Success. Star ID: ', star_id, '.') as message;
END; //

delimiter ; 


