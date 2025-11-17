-- provided
SELECT JSON_OBJECT(['a', 'b'], [JSON '10', JSON '"foo"']) AS json_data;

-- expected
SELECT JSON_OBJECT(['a','b']::VARCHAR,[JSON '10',JSON '"foo"'])AS JSON_DATA;

-- result
"json_data"
"{""[a, b]"":[10,""foo""]}"