INSERT INTO item (item_id, name, brand, catalogue, type, description, brand_id, catalogue_id, itemurl, image) VALUES
    (1, 'Product 1', 'Brand 1', 'Catalogue 1', 'Type 1', 'Description 1', 1, 3, 'https://www.example.com/product1', 'https://www.example.com/images/photo1.jpg'),
    (2, 'Product 2', 'Brand 2', 'Catalogue 2', 'Type 2', 'Description 2', 2, 4, 'https://www.example.com/product2', 'https://www.example.com/images/photo2.jpg'),
    (3, 'Product 3', 'Brand 3', 'Catalogue 3', 'Type 1', 'Description 3', 3, 5, 'https://www.example.com/product3', 'https://www.example.com/images/photo3.jpg');

INSERT INTO remain (item_id, region_id, price) VALUES
                                                   (1, 1, 10.99),
                                                   (1, 2, 12.99),
                                                   (2, 1, 8.99),
                                                   (2, 2, 9.99),
                                                   (3, 1, 15.99),
                                                   (3, 2, 18.99);


INSERT INTO item_sku (item_id, sku) VALUES
                                        (1, 'SKU001'),
                                        (2, 'SKU002'),
                                        (3, 'SKU003');

INSERT INTO catalogue (catalogue_id, name, realcatname, image, parent_id) VALUES
                                                                (1, 'Category 1', 'category1', 'image1.jpg', NULL),
                                                                (2, 'Category 2', 'category2', 'image2.jpg', NULL),
                                                                (3, 'Subcategory 1', 'subcategory1', 'sub_image1.jpg', 1),
                                                                (4, 'Subcategory 2', 'subcategory2', 'sub_image2.jpg', 1),
                                                                (5, 'Subcategory 2', 'subcategory2', 'sub_image2.jpg', 2);


