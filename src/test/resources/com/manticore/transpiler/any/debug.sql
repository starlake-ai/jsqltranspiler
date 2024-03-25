-- provided
SELECT ASCII('abcd') as A, ASCII('a') as B, ASCII('') as C, ASCII(NULL) as D;

-- result
"A","B","C","D"
"97","97","0",""