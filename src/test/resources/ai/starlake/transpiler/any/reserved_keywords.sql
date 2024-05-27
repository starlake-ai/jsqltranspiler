-- prolog
create table "summarize" (
    "summarize" VARCHAR(255)
);

-- provided
select summarize as summarize from summarize as summarize;

-- expected;
select "summarize" as "summarize" from "summarize" as "summarize";

-- count
0
