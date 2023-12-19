CREATE TABLE item (
                      item_id INT PRIMARY KEY,
                      name VARCHAR(255),
                      brand VARCHAR(255),
                      catalogue VARCHAR(255),
                      itemurl VARCHAR(255),
                      type VARCHAR(255),
                      description VARCHAR(255),
                      brand_id INT,
                      catalogue_id INT,
                      image VARCHAR(255)
);



CREATE TABLE remain (
                        item_id INT,
                        region_id INT,
                        price DECIMAL(10, 2),
                        PRIMARY KEY (item_id, region_id),
                        FOREIGN KEY (item_id) REFERENCES item(item_id)
);

CREATE TABLE item_sku (
                          id SERIAL PRIMARY KEY,
                          item_id INT,
                          sku VARCHAR(255),
                          FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE catalogue (
                           catalogue_id INT PRIMARY KEY,
                           name VARCHAR(255),
                           realcatname VARCHAR(255),
                           image VARCHAR(255),
                           parent_id INT,
                           FOREIGN KEY (parent_id) REFERENCES catalogue(catalogue_id)
);