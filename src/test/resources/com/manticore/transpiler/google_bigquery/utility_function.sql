-- provided
select session_user;

-- result
"session_user"
"duckdb"


-- provided
SELECT GENERATE_UUID() AS uuid;

-- expected
SELECT UUID() AS uuid;

-- count
1


