DROP TABLE IF EXISTS invoice;

CREATE TABLE invoice(
	invoice_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL ,
	subtotal DECIMAL(12,2) NOT NULL,
	taxes DECIMAL(12,2) NOT NULL,
	total DECIMAL(12,2) NOT NULL,
    created_at DATE NOT NULL,
    status BOOLEAN NOT NULL,
    PRIMARY KEY (invoice_id)
);
