-- provided
SELECT LISTAGG(sellerid, ', ')
WITHIN GROUP (ORDER BY dateid) AS list
FROM sales
WHERE eventid = 4337;

-- expected
SELECT LISTAGG(sellerid, ', ' ORDER BY dateid) AS list
FROM sales
WHERE eventid = 4337;

"list"
"41498, 47188, 1178, 47188, 1178, 1178, 380, 45676, 46324, 32043, 32043, 48294, 32432, 12905, 8117, 38750, 32432, 32043, 2731, 380, 38669"

