SELECT 
    u.*
FROM
    users u
WHERE
    (SELECT 
            (SELECT 
                        SUM(IF(u.id = m.user_id, 1, 0))
                    FROM
                        messages m) > 3
        );