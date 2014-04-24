-- sql script to delete message. change the where clause to fit your needs.

DELETE
FROM
    public.message_property
WHERE
    message_id IN
    (
        SELECT
            id
        FROM
            public.message mes
        WHERE
            mes.publicationdate >= '2013-10-14' );
DELETE
FROM
    public.property
WHERE
    property.id IN
    (
        SELECT
            p.properties_id
        FROM
            public.message mes,
            public.message_property p
        WHERE
            mes.id = p.message_id
        AND mes.publicationdate >= '2013-10-14' );

DELETE
FROM
    public.message_messagepart
WHERE
    message_messagepart.message_id IN
    (
        SELECT
            id
        FROM
            public.message
        WHERE
            message.publicationdate >= '2013-10-14');
DELETE
FROM
    public.messagepart
WHERE
    messagepart.id IN
    (
        SELECT
            mp.messageparts_id
        FROM
            public.message mes,
            public.message_messagepart mp
        WHERE
            mes.id = mp.message_id
        AND mes.publicationdate >= '2013-10-14' );
DELETE
FROM
    MESSAGE
WHERE
    message.publicationdate >= '2013-10-14';
    
DELETE FROM public.hashwithdate
where hashwithdate.time >= '2013-10-14';