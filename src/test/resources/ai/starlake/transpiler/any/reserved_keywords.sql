-- prolog
create table "character" (
    "character" VARCHAR(255)
);

-- provided
select character as character from character as character;

-- expected;
select "character" as "character" from "character" as "character";

-- count
0
