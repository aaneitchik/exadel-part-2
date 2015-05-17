SELECT 
    *
FROM
    messages
WHERE
    user_id = '1'
        AND text LIKE '%hello%';