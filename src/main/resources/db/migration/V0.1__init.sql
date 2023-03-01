CREATE TABLE subscriptions
(
    id   UUID  PRIMARY KEY,
    url  TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()

);

CREATE TABLE price_history
(
    subscription_id      UUID    NOT NULL   REFERENCES subscriptions (id) ON DELETE CASCADE,
    price NUMERIC NOT NULL,
    checked_at TIMESTAMP NOT NULL DEFAULT NOW()
);
