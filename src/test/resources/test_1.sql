INSERT INTO item (item_id, name, brand, catalogue, type, description, brand_id, catalogue_id, itemurl, image) VALUES
                    (1, 'Product 1', 'Brand 1', 'Catalogue 1', 'Type 1', 'Description 1', 1, 3, 'https://www.example.com/product1', 'https://www.example.com/images/photo1.jpg'),
                    (2, 'Product 2', 'Brand 2', 'Catalogue 2', 'Type 2', 'Description 2', 2, 4, 'https://www.example.com/product2', 'https://www.example.com/images/photo2.jpg'),
                    (3, 'Product 3', 'Brand 3', 'Catalogue 3', 'Type 1', 'Description 3', 3, 5, 'https://www.example.com/product3', 'https://www.example.com/images/photo3.jpg');

INSERT INTO item_sku (item_id, sku) VALUES
                                        (1, '111'),
                                        (2, '123'),
                                        (3, '444');

