CREATE TABLE sourcestatus_property
	(
        sourcestatus_id BIGINT NOT NULL,
        properties_id BIGINT NOT NULL,
        PRIMARY KEY (sourcestatus_id, properties_id),
        CONSTRAINT fk_sourcestatus_property_properties_id FOREIGN KEY (properties_id)
        REFERENCES property (id),
        CONSTRAINT fk_sourcestatus_property_sourcestatus_id FOREIGN KEY (sourcestatus_id)
        REFERENCES sourcestatus (id)
    );
