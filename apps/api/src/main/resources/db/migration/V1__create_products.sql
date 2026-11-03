CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category ON products (category);

INSERT INTO products (name, description, price, stock, category) VALUES
    ('Aurora Mechanical Keyboard', 'A compact 75% keyboard with tactile switches and a warm backlight.', 89.00, 12, 'Electronics'),
    ('Focus Noise-Cancelling Headphones', 'Over-ear headphones for library sessions and late-night study playlists.', 129.50, 7, 'Electronics'),
    ('Everyday Canvas Tote', 'A sturdy canvas tote with a laptop sleeve and two front pockets.', 24.00, 35, 'Lifestyle'),
    ('Campus Coffee Mug', 'A 350 ml ceramic mug that keeps your morning coffee warm.', 14.50, 48, 'Lifestyle'),
    ('Clean Code Paperback', 'A practical guide to writing readable and maintainable software.', 38.00, 9, 'Books'),
    ('Designing Data-Intensive Applications', 'A broad tour of storage, replication, and distributed systems.', 46.00, 4, 'Books'),
    ('Gel Pen Set (6 Colours)', 'Smooth quick-dry gel pens for notes, diagrams, and revision cards.', 8.90, 60, 'Stationery'),
    ('A5 Dot Grid Notebook', 'A lay-flat dotted notebook with 160 pages of 100 gsm paper.', 12.00, 0, 'Stationery');
