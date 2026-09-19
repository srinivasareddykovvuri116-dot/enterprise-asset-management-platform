ALTER TABLE projects
ADD CONSTRAINT uk_project_organization_name
UNIQUE (organization_id, name);