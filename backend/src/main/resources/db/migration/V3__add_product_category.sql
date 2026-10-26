ALTER TABLE products
ADD COLUMN category 
TEXT NOT NULL DEFAULT ('Uncategorized') CHECK (length(btrim(category)) > 0);