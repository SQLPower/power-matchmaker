-- Creates the demo schema. This script is optimized for PostgreSQL 8+

CREATE TABLE merge_demo.customer (
                id INTEGER NOT NULL,
                name VARCHAR(100) NOT NULL,
                CONSTRAINT customer_pk PRIMARY KEY (id)
);

CREATE TABLE merge_demo.invoice (
                id INTEGER NOT NULL,
                customer_id INTEGER NOT NULL,
                date TIMESTAMP NOT NULL,
                CONSTRAINT invoice_pk PRIMARY KEY (id)
);

CREATE TABLE merge_demo.invoice_line (
                invoice_id INTEGER NOT NULL,
                line_number INTEGER NOT NULL,
                item VARCHAR(100) NOT NULL,
                quantity INTEGER NOT NULL,
                item_price NUMERIC(10,2) NOT NULL,
                CONSTRAINT invoice_line_pk PRIMARY KEY (invoice_id, line_number)
);

CREATE TABLE merge_demo.customer_address (
                id INTEGER NOT NULL,
                customer_id INTEGER NOT NULL,
                address_type VARCHAR(10) NOT NULL,
                email VARCHAR(100),
                phone VARCHAR(100),
                street_addr VARCHAR(100),
                city VARCHAR(100),
                state VARCHAR(100),
                country VARCHAR(100),
                pcode VARCHAR(100),
                CONSTRAINT customer_address_pk PRIMARY KEY (id)
);


ALTER TABLE merge_demo.customer_address ADD CONSTRAINT customer_customer_address_fk
FOREIGN KEY (customer_id)
REFERENCES merge_demo.customer (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;


ALTER TABLE merge_demo.invoice ADD CONSTRAINT customer_invoice_fk
FOREIGN KEY (customer_id)
REFERENCES merge_demo.customer (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;


ALTER TABLE merge_demo.invoice_line ADD CONSTRAINT invoice_invoice_line_fk
FOREIGN KEY (invoice_id)
REFERENCES merge_demo.invoice (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;