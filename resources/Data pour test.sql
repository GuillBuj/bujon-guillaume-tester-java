USE prod;

INSERT INTO ticket (
    PARKING_NUMBER,
    VEHICLE_REG_NUMBER,
    PRICE,
    IN_TIME,
    OUT_TIME
)
VALUES (
    2,
    'ABCDEF',
    5,
    NOW(),
    NULL
);

COMMIT;
