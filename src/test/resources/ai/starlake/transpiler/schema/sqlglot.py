from sqlglot.executor import execute

tables = {
  "a": [
    {"col1": 1, "col2": 2, "col3": 3, "colAA": "AA", "colAB": 'AB'},
  ],
  "b": [
    {"col1": 1, "col2": 2, "col3": 3 , "colBA": 'BA', "colBB": 'BB'},
  ],
}

// works
execute(
  "SELECT * FROM a ",
  tables=tables
)

// fails
execute(
  "SELECT * FROM ( (SELECT * FROM b) c inner join a on c.col1 = a.col1 ) d ",
  tables=tables
)