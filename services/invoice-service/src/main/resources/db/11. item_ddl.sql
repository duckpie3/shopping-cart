DROP TABLE IF EXISTS invoice_item;

CREATE TABLE invoice_item(
	invoice_item_id INT NOT NULL AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    gtin CHAR(13) NOT NULL,
    quantity INT NOT NULL,
	unit_price DECIMAL(12,2) NOT NULL,
	subtotal DECIMAL(12,2) NOT NULL,
	taxes DECIMAL(12,2) NOT NULL,
	total DECIMAL(12,2) NOT NULL,
    status BOOLEAN NOT NULL,
    PRIMARY KEY (invoice_item_id),
    FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id),
    FOREIGN KEY (gtin) REFERENCES product(gtin)
);
